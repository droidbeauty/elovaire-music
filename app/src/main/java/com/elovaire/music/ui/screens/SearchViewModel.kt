package elovaire.music.droidbeauty.app.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.data.library.LibraryReader
import elovaire.music.droidbeauty.app.data.playback.PlaybackReader
import elovaire.music.droidbeauty.app.data.settings.SearchSettingsStore
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.search.NormalizedSearchQuery
import elovaire.music.droidbeauty.app.domain.search.SearchArtistResult
import elovaire.music.droidbeauty.app.domain.search.SearchIndex
import elovaire.music.droidbeauty.app.domain.search.SearchLibrarySnapshot
import elovaire.music.droidbeauty.app.domain.search.SearchSortMode
import elovaire.music.droidbeauty.app.domain.search.albumSearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.search.artistSearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.search.buildSearchResults
import elovaire.music.droidbeauty.app.domain.search.buildSuggestedAlbums
import elovaire.music.droidbeauty.app.domain.search.playbackSourceLabel
import elovaire.music.droidbeauty.app.domain.search.sanitizeSearchHistory
import elovaire.music.droidbeauty.app.domain.search.toSearchIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update

internal data class SearchUiState(
    val query: String = "",
    val showAllSongResults: Boolean = false,
    val searchSongSortMode: SearchSongSortMode = SearchSongSortMode.Title,
    val showSearchSongSortOptions: Boolean = false,
    val contentMode: SearchContentMode = SearchContentMode.Discover,
    val recentSearches: List<SearchHistoryEntry> = emptyList(),
    val allMatchingSongs: List<Song> = emptyList(),
    val matchingSongs: List<Song> = emptyList(),
    val totalSongMatchCount: Int = 0,
    val matchingAlbums: List<Album> = emptyList(),
    val matchingArtists: List<SearchArtistResult> = emptyList(),
    val matchingAudiobooks: List<elovaire.music.droidbeauty.app.domain.model.Audiobook> = emptyList(),
    val suggestedAlbums: List<Album> = emptyList(),
    val isSearchPending: Boolean = false,
    val resultQuery: String = "",
    val currentSongId: Long? = null,
    val isPlaybackActive: Boolean = false,
)

internal data class SearchInteractionConfig(
    val query: String = "",
    val showAllSongs: Boolean = false,
    val sortMode: SearchSongSortMode = SearchSongSortMode.Title,
    val showSortOptions: Boolean = false,
    val queryGeneration: Long = 0L,
) {
    fun normalized(): SearchInteractionConfig {
        val showAllSongs = showAllSongs && query.trim().isNotBlank()
        return copy(
            showAllSongs = showAllSongs,
            showSortOptions = showSortOptions && showAllSongs,
        )
    }
}

internal data class SearchResultKey(
    val queryGeneration: Long,
    val sortMode: SearchSongSortMode,
    val includeAllSongs: Boolean,
    val indexRevision: String,
) {
    fun matches(config: SearchInteractionConfig, indexRevision: String): Boolean {
        return queryGeneration == config.queryGeneration &&
            sortMode == config.sortMode &&
            includeAllSongs == config.showAllSongs &&
            this.indexRevision == indexRevision
    }
}

internal class SearchViewModel(
    libraryRepository: LibraryReader,
    private val preferenceStore: SearchSettingsStore,
    playbackReader: PlaybackReader,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private val searchUiConfig = MutableStateFlow(
        SearchInteractionConfig(
            query = savedStateHandle.get<String>(KEY_QUERY).orEmpty(),
            showAllSongs = savedStateHandle[KEY_SHOW_ALL_SONGS] ?: false,
            sortMode = savedStateHandle.get<String>(KEY_SORT_MODE)
                ?.let { saved -> SearchSongSortMode.entries.firstOrNull { it.name == saved } }
                ?: SearchSongSortMode.Title,
            showSortOptions = savedStateHandle[KEY_SHOW_SORT_OPTIONS] ?: false,
        ).normalized(),
    )

    private val searchIndex = libraryRepository.contentState
        .map { content ->
            SearchLibrarySnapshot(
                songs = content.songs.filter { it.mediaKind == AudioMediaKind.Music },
                albums = content.albums,
                audiobooks = content.audiobooks,
                revision = content.contentRevision,
            )
        }
        .map { snapshot -> snapshot to snapshot.signature() }
        .distinctUntilChangedBy { (_, revision) -> revision }
        .map { (snapshot, revision) -> snapshot.toSearchIndex(revision) }
        .flowOn(defaultDispatcher)
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            replay = 1,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val settledQuery = searchUiConfig
        .map { config -> SearchQueryToken(config.query, config.queryGeneration) }
        .distinctUntilChanged()
        .transformLatest { token ->
            if (token.rawQuery.trim().isNotBlank()) {
                delay(SEARCH_QUERY_DEBOUNCE_MS)
            }
            emit(token)
        }
        .flowOn(defaultDispatcher)

    private val uiConfigWithIndex = combine(searchUiConfig, searchIndex) { config, index ->
        SearchUiConfigWithIndex(config = config, indexRevision = index.revision)
    }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val searchResults: Flow<SearchResultSnapshot?> = combine(
        searchUiConfig,
        searchIndex,
        settledQuery,
    ) { config, index, settled ->
        if (settled.matches(config)) {
            SearchRequest(
                key = SearchResultKey(
                    queryGeneration = config.queryGeneration,
                    sortMode = config.sortMode,
                    includeAllSongs = config.showAllSongs,
                    indexRevision = index.revision,
                ),
                rawQuery = config.query,
                query = NormalizedSearchQuery.from(config.query),
                sortMode = config.sortMode.toSearchSortMode(),
                index = index,
                includeAllSongs = config.showAllSongs,
            )
        } else {
            null
        }
    }
        .distinctUntilChanged()
        .transformLatest<SearchRequest?, SearchResultSnapshot?> { request ->
            if (request != null) {
                emit(
                    SearchResultSnapshot(
                        key = request.key,
                        rawQuery = request.rawQuery,
                        results = buildSearchResults(
                            query = request.query,
                            sortMode = request.sortMode,
                            index = request.index,
                            includeAllSongs = request.includeAllSongs,
                        ),
                    ),
                )
            } else {
                emit(null)
            }
        }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    private val playbackSnapshot = combine(
        playbackReader.nowPlayingState,
        playbackReader.transportState,
    ) { nowPlaying, transport ->
        PlaybackSearchSnapshot(
            currentSongId = nowPlaying.currentSong?.id,
            isPlaybackActive = transport.isPlaying,
        )
    }
        .distinctUntilChanged()

    private val suggestedAlbums = combine(
        searchIndex,
        preferenceStore.albumPlayCounts,
        playbackReader.recentPlaybackState.map { it.recentAlbumIds }.distinctUntilChanged(),
        searchUiConfig.map { it.query }.distinctUntilChanged(),
    ) { index, albumPlayCounts, recentAlbumIds, query ->
        if (query.trim().isNotBlank()) {
            emptyList()
        } else {
            buildSuggestedAlbums(
                albums = index.albums,
                albumPlayCounts = albumPlayCounts,
                recentAlbumIds = recentAlbumIds,
            )
        }
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    private val recentSearches = combine(
        preferenceStore.searchHistory,
        searchIndex,
    ) { history, index ->
        sanitizeSearchHistory(
            history = history,
            index = index,
        )
    }
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)

    val uiState: StateFlow<SearchUiState> = combine(
        uiConfigWithIndex,
        recentSearches,
        searchResults,
        suggestedAlbums,
        playbackSnapshot,
    ) { configWithIndex, history, resultSnapshot, suggested, playback ->
        val config = configWithIndex.config
        val trimmedQuery = config.query.trim()
        val results = resultSnapshot?.takeIf {
            it.key.matches(config, configWithIndex.indexRevision)
        }
        val isSearchPending = trimmedQuery.isNotBlank() && results == null
        SearchUiState(
            query = config.query,
            showAllSongResults = config.showAllSongs,
            searchSongSortMode = config.sortMode,
            showSearchSongSortOptions = config.showSortOptions,
            contentMode = when {
                config.showAllSongs && trimmedQuery.isNotBlank() -> SearchContentMode.AllSongs
                trimmedQuery.isBlank() -> SearchContentMode.Discover
                else -> SearchContentMode.Results
            },
            recentSearches = history,
            allMatchingSongs = results?.results?.allMatchingSongs.orEmpty(),
            matchingSongs = results?.results?.matchingSongs.orEmpty(),
            totalSongMatchCount = results?.results?.totalSongMatchCount ?: 0,
            matchingAlbums = results?.results?.matchingAlbums.orEmpty(),
            matchingArtists = results?.results?.matchingArtists.orEmpty(),
            matchingAudiobooks = results?.results?.matchingAudiobooks.orEmpty(),
            suggestedAlbums = if (trimmedQuery.isBlank()) suggested else emptyList(),
            isSearchPending = isSearchPending,
            resultQuery = results?.rawQuery.orEmpty(),
            currentSongId = playback.currentSongId,
            isPlaybackActive = playback.isPlaybackActive,
        )
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SearchUiState(),
        )

    fun onQueryChange(query: String) {
        updateConfig { config ->
            if (config.query == query) config else config.copy(
                query = query,
                queryGeneration = config.queryGeneration + 1,
            )
        }
    }

    fun clearQuery() = updateConfig { config ->
        config.copy(
            query = "",
            queryGeneration = if (config.query.isBlank()) config.queryGeneration else config.queryGeneration + 1,
        )
    }

    fun onShowAllSongResultsChange(show: Boolean) {
        updateConfig { it.copy(showAllSongs = show) }
    }

    fun onSearchSongSortModeChange(mode: SearchSongSortMode) {
        updateConfig { it.copy(sortMode = mode, showSortOptions = false) }
    }

    fun onShowSearchSongSortOptionsChange(show: Boolean) {
        updateConfig { it.copy(showSortOptions = show) }
    }

    fun resetSearchUi() {
        clearQuery()
    }

    fun clearSearchHistory() {
        preferenceStore.clearSearchHistory()
    }

    fun rememberAlbumSearch(album: Album) {
        preferenceStore.addSearchHistoryEntry(albumSearchHistoryEntry(album))
    }

    fun rememberArtistSearch(song: Song) {
        preferenceStore.addSearchHistoryEntry(artistSearchHistoryEntry(song))
    }

    fun playbackSourceLabelFor(queue: List<Song>, fallbackAlbum: String): String {
        return queue.playbackSourceLabel(fallbackAlbum = fallbackAlbum)
    }

    private companion object {
        data class SearchQueryToken(
            val rawQuery: String,
            val generation: Long,
        ) {
            fun matches(config: SearchInteractionConfig): Boolean {
                return rawQuery == config.query && generation == config.queryGeneration
            }
        }

        data class SearchResultSnapshot(
            val key: SearchResultKey,
            val rawQuery: String,
            val results: elovaire.music.droidbeauty.app.domain.search.SearchResults,
        )

        data class SearchUiConfigWithIndex(
            val config: SearchInteractionConfig,
            val indexRevision: String,
        )

        data class PlaybackSearchSnapshot(
            val currentSongId: Long?,
            val isPlaybackActive: Boolean,
        )

        data class SearchRequest(
            val key: SearchResultKey,
            val rawQuery: String,
            val query: NormalizedSearchQuery,
            val sortMode: SearchSortMode,
            val index: SearchIndex,
            val includeAllSongs: Boolean,
        )

        const val SEARCH_QUERY_DEBOUNCE_MS = 150L
        const val KEY_QUERY = "search.query"
        const val KEY_SHOW_ALL_SONGS = "search.show_all_songs"
        const val KEY_SORT_MODE = "search.sort_mode"
        const val KEY_SHOW_SORT_OPTIONS = "search.show_sort_options"
    }

    private fun updateConfig(transform: (SearchInteractionConfig) -> SearchInteractionConfig) {
        searchUiConfig.update { current ->
            transform(current).normalized().also { next ->
                savedStateHandle[KEY_QUERY] = next.query
                savedStateHandle[KEY_SHOW_ALL_SONGS] = next.showAllSongs
                savedStateHandle[KEY_SORT_MODE] = next.sortMode.name
                savedStateHandle[KEY_SHOW_SORT_OPTIONS] = next.showSortOptions
            }
        }
    }

}

private fun SearchSongSortMode.toSearchSortMode(): SearchSortMode {
    return when (this) {
        SearchSongSortMode.Title -> SearchSortMode.Title
        SearchSongSortMode.Artist -> SearchSortMode.Artist
    }
}
