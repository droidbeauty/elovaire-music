package elovaire.music.droidbeauty.app.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryKind
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal data class SearchArtistResult(
    val name: String,
    val songCount: Int,
    val artUri: Uri?,
)

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
    playbackManager: PlaybackManager,
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
        .map { it.toSearchIndex() }
        .distinctUntilChanged()

    private val normalizedQuery = _query
        .map { normalizeSearchText(it.trim()) }
        .distinctUntilChanged()

    private val searchResults = combine(
        normalizedQuery,
        _searchSongSortMode,
        searchIndex,
    ) { query, sortMode, index ->
        buildSearchResults(
            normalizedQuery = query,
            sortMode = sortMode,
            index = index,
        )
    }
        .distinctUntilChanged()

    private val playbackSnapshot = combine(
        playbackManager.recentPlaybackState,
        playbackManager.nowPlayingState,
        playbackManager.transportState,
    ) { recentPlayback, nowPlaying, transport ->
        PlaybackSearchSnapshot(
            recentAlbumIds = recentPlayback.recentAlbumIds,
            currentSongId = nowPlaying.currentSong?.id,
            isPlaybackActive = transport.isPlaying,
        )
    }
        .distinctUntilChanged()

    private val suggestedAlbums = combine(
        searchIndex,
        preferenceStore.albumPlayCounts,
        playbackManager.recentPlaybackState.map { it.recentAlbumIds }.distinctUntilChanged(),
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
            val recentAlbumIds: List<Long>,
            val currentSongId: Long?,
            val isPlaybackActive: Boolean,
        )

        data class SearchResults(
            val allMatchingSongs: List<Song> = emptyList(),
            val matchingSongs: List<Song> = emptyList(),
            val matchingAlbums: List<Album> = emptyList(),
            val matchingArtists: List<SearchArtistResult> = emptyList(),
        )

        fun buildSearchResults(
            normalizedQuery: String,
            sortMode: SearchSongSortMode,
            index: SearchIndex,
        ): SearchResults {
            if (normalizedQuery.isBlank()) return SearchResults()

            val rankedSongs = index.songs.mapNotNull { searchable ->
                scoreMatch(
                    normalizedQuery = normalizedQuery,
                    normalizedTitle = searchable.normalizedTitle,
                    normalizedArtist = searchable.normalizedArtist,
                    normalizedAlbum = searchable.normalizedAlbum,
                    normalizedComposite = searchable.normalizedComposite,
                )?.let { score ->
                    RankedResult(
                        value = searchable.song,
                        score = score,
                    )
                }
            }
            val allMatchingSongs = sortRankedSongs(
                ranked = rankedSongs,
                sortMode = sortMode,
            )

            val matchingAlbums = index.albums
                .mapNotNull { searchable ->
                    scoreMatch(
                        normalizedQuery = normalizedQuery,
                        normalizedTitle = searchable.normalizedTitle,
                        normalizedArtist = searchable.normalizedArtist,
                        normalizedAlbum = "",
                        normalizedComposite = searchable.normalizedComposite,
                    )?.let { score ->
                        RankedResult(
                            value = searchable.album,
                            score = score,
                        )
                    }
                }
                .sortedWith(
                    compareByDescending<RankedResult<Album>> { it.score }
                        .thenBy { normalizeSearchText(it.value.artist) }
                        .thenBy { normalizeSearchText(it.value.title) }
                        .thenBy { it.value.id },
                )
                .map(RankedResult<Album>::value)
                .take(12)

            val matchingArtists = index.artists
                .mapNotNull { artist ->
                    scoreMatch(
                        normalizedQuery = normalizedQuery,
                        normalizedTitle = artist.normalizedName,
                        normalizedArtist = "",
                        normalizedComposite = artist.normalizedName,
                    )?.let { score ->
                        RankedResult(
                            value = artist,
                            score = score,
                        )
                    }
                }
                .sortedWith(
                    compareByDescending<RankedResult<SearchableArtist>> { it.score }
                        .thenByDescending { it.value.songCount }
                        .thenBy { normalizeSearchText(it.value.displayName) },
                )
                .map { rankedArtist ->
                    SearchArtistResult(
                        name = rankedArtist.value.displayName,
                        songCount = rankedArtist.value.songCount,
                        artUri = rankedArtist.value.artUri,
                    )
                }
                .take(6)

            return SearchResults(
                allMatchingSongs = allMatchingSongs,
                matchingSongs = allMatchingSongs.take(20),
                matchingAlbums = matchingAlbums,
                matchingArtists = matchingArtists,
            )
        }

        fun buildSuggestedAlbums(
            albums: List<Album>,
            albumPlayCounts: Map<Long, Int>,
            recentAlbumIds: List<Long>,
        ): List<Album> {
            val recentAlbumIdSet = recentAlbumIds.toSet()
            val rarePlayedAlbums = albums
                .mapNotNull { album ->
                    val playCount = albumPlayCounts[album.id] ?: 0
                    if (playCount > 0) album to playCount else null
                }
                .sortedWith(
                    compareBy<Pair<Album, Int>> { it.second }
                        .thenBy { album -> if (album.first.id in recentAlbumIdSet) 1 else 0 }
                        .thenBy { normalizeSearchText(it.first.artist) }
                        .thenBy { normalizeSearchText(it.first.title) },
                )
                .map { it.first }

            val neverPlayedAlbums = albums
                .filter { (albumPlayCounts[it.id] ?: 0) == 0 }
                .sortedWith(
                    compareBy<Album> { if (it.id in recentAlbumIdSet) 1 else 0 }
                        .thenBy { normalizeSearchText(it.artist) }
                        .thenBy { normalizeSearchText(it.title) },
                )

            return buildList {
                (rarePlayedAlbums + neverPlayedAlbums).forEach { album ->
                    if (none { it.id == album.id }) add(album)
                    if (size == 6) return@buildList
                }
            }
        }

        fun sanitizeSearchHistory(
            history: List<SearchHistoryEntry>,
            index: SearchIndex,
        ): List<SearchHistoryEntry> {
            return history.mapNotNull { entry ->
                when (entry.kind) {
                    SearchHistoryKind.Album -> {
                        entry.albumId
                            ?.let(index.albumsById::get)
                            ?.let(::albumSearchHistoryEntry)
                    }

                    SearchHistoryKind.Artist -> {
                        val normalizedArtist = normalizeSearchText(entry.query ?: entry.title)
                        val artist = index.artistsByNormalizedName[normalizedArtist] ?: return@mapNotNull null
                        entry.copy(
                            key = "artist:${artist.normalizedName}",
                            title = artist.displayName,
                            artUri = artist.artUri,
                            query = artist.displayName,
                        )
                    }
                }
            }.distinctBy(SearchHistoryEntry::key)
        }

        fun List<Song>.playbackSourceLabel(fallbackAlbum: String): String {
            val distinctAlbums = asSequence().map { it.album }.filter { it.isNotBlank() }.distinct().toList()
            return when {
                distinctAlbums.size == 1 -> distinctAlbums.first()
                distinctAlbums.isNotEmpty() -> "Search"
                else -> fallbackAlbum
            }
        }
    }
}
