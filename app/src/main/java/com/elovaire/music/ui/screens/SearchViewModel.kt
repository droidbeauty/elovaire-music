package elovaire.music.droidbeauty.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.playback.PlaybackReader
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.search.NormalizedSearchQuery
import elovaire.music.droidbeauty.app.domain.search.SearchArtistResult
import elovaire.music.droidbeauty.app.domain.search.SearchLibrarySnapshot
import elovaire.music.droidbeauty.app.domain.search.SearchableAlbum
import elovaire.music.droidbeauty.app.domain.search.albumSearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.search.artistSearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.search.buildSearchResults
import elovaire.music.droidbeauty.app.domain.search.buildSuggestedAlbums
import elovaire.music.droidbeauty.app.domain.search.playbackSourceLabel
import elovaire.music.droidbeauty.app.domain.search.sanitizeSearchHistory
import elovaire.music.droidbeauty.app.domain.search.toSearchIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal data class SearchUiState(
    val query: String = "",
    val showAllSongResults: Boolean = false,
    val searchSongSortMode: SearchSongSortMode = SearchSongSortMode.Title,
    val showSearchSongSortOptions: Boolean = false,
    val contentMode: SearchContentMode = SearchContentMode.Discover,
    val recentSearches: List<SearchHistoryEntry> = emptyList(),
    val allMatchingSongs: List<Song> = emptyList(),
    val matchingSongs: List<Song> = emptyList(),
    val matchingAlbums: List<Album> = emptyList(),
    val matchingArtists: List<SearchArtistResult> = emptyList(),
    val suggestedAlbums: List<Album> = emptyList(),
    val currentSongId: Long? = null,
    val isPlaybackActive: Boolean = false,
)

internal class SearchViewModel(
    libraryRepository: LibraryRepository,
    private val preferenceStore: PreferenceStore,
    playbackReader: PlaybackReader,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    private val _showAllSongResults = MutableStateFlow(false)
    private val _searchSongSortMode = MutableStateFlow(SearchSongSortMode.Title)
    private val _showSearchSongSortOptions = MutableStateFlow(false)
    private val searchUiConfig = combine(
        _query,
        _showAllSongResults,
        _searchSongSortMode,
        _showSearchSongSortOptions,
    ) { query, showAllSongs, sortMode, showSortOptions ->
        SearchUiConfig(
            query = query,
            showAllSongs = showAllSongs,
            sortMode = sortMode,
            showSortOptions = showSortOptions,
        )
    }
        .distinctUntilChanged()

    private val searchIndex = libraryRepository.contentState
        .map { content ->
            SearchLibrarySnapshot(
                songs = content.songs,
                albums = content.albums,
            )
        }
        .distinctUntilChanged()
        .map(SearchLibrarySnapshot::toSearchIndex)
        .flowOn(Dispatchers.Default)

    private val normalizedQuery = _query
        .map(NormalizedSearchQuery::from)
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val searchResults = combine(
        normalizedQuery,
        _searchSongSortMode,
        searchIndex,
    ) { query, sortMode, index ->
        SearchRequest(
            query = query,
            sortMode = sortMode,
            index = index,
        )
    }
        .mapLatest { request ->
            buildSearchResults(
                query = request.query,
                sortMode = request.sortMode,
                index = request.index,
            )
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

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
    ) { index, albumPlayCounts, recentAlbumIds ->
        buildSuggestedAlbums(
            albums = index.albums.map(SearchableAlbum::album),
            albumPlayCounts = albumPlayCounts,
            recentAlbumIds = recentAlbumIds,
        )
    }
        .distinctUntilChanged()

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

    val uiState: StateFlow<SearchUiState> = combine(
        searchUiConfig,
        recentSearches,
        searchResults,
        suggestedAlbums,
        playbackSnapshot,
    ) { config, history, results, suggested, playback ->
        val trimmedQuery = config.query.trim()
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
            allMatchingSongs = results.allMatchingSongs,
            matchingSongs = results.matchingSongs,
            matchingAlbums = results.matchingAlbums,
            matchingArtists = results.matchingArtists,
            suggestedAlbums = if (trimmedQuery.isBlank()) suggested else emptyList(),
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
        _query.value = query
        if (query.trim().isBlank()) {
            _showAllSongResults.value = false
            _showSearchSongSortOptions.value = false
        }
    }

    fun onShowAllSongResultsChange(show: Boolean) {
        _showAllSongResults.value = show
    }

    fun onSearchSongSortModeChange(mode: SearchSongSortMode) {
        _searchSongSortMode.value = mode
    }

    fun onShowSearchSongSortOptionsChange(show: Boolean) {
        _showSearchSongSortOptions.value = show
    }

    fun resetSearchUi() {
        _query.value = ""
        _showAllSongResults.value = false
        _showSearchSongSortOptions.value = false
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
        data class SearchUiConfig(
            val query: String,
            val showAllSongs: Boolean,
            val sortMode: SearchSongSortMode,
            val showSortOptions: Boolean,
        )

        data class PlaybackSearchSnapshot(
            val currentSongId: Long?,
            val isPlaybackActive: Boolean,
        )

        data class SearchRequest(
            val query: NormalizedSearchQuery,
            val sortMode: SearchSongSortMode,
            val index: elovaire.music.droidbeauty.app.domain.search.SearchIndex,
        )
    }
}
