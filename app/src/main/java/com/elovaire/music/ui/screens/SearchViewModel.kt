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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
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

    private val searchResults = combine(
        normalizedQuery,
        _searchSongSortMode,
        searchIndex,
    ) { query, sortMode, index ->
        buildSearchResults(
            query = query,
            sortMode = sortMode,
            index = index,
        )
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    private val playbackSnapshot = combine(
        playbackManager.nowPlayingState,
        playbackManager.transportState,
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
            val currentSongId: Long?,
            val isPlaybackActive: Boolean,
        )

        data class SearchResults(
            val allMatchingSongs: List<Song> = emptyList(),
            val matchingSongs: List<Song> = emptyList(),
            val matchingAlbums: List<Album> = emptyList(),
            val matchingArtists: List<SearchArtistResult> = emptyList(),
        )

        data class SuggestedAlbumCandidate(
            val album: Album,
            val playCount: Int,
            val isRecent: Boolean,
            val normalizedArtist: String,
            val normalizedTitle: String,
        )

        fun buildSearchResults(
            query: NormalizedSearchQuery,
            sortMode: SearchSongSortMode,
            index: SearchIndex,
        ): SearchResults {
            if (query.value.isBlank()) return SearchResults()

            val rankedSongs = index.songs.rankMatching(
                query = query,
                normalizedTitle = SearchableSong::normalizedTitle,
                normalizedArtist = SearchableSong::normalizedArtist,
                normalizedAlbum = SearchableSong::normalizedAlbum,
                normalizedComposite = SearchableSong::normalizedComposite,
            )
            val allMatchingSongs = sortRankedSongs(
                ranked = rankedSongs,
                sortMode = sortMode,
            )

            val matchingAlbums = index.albums
                .rankMatching(
                    query = query,
                    normalizedTitle = SearchableAlbum::normalizedTitle,
                    normalizedArtist = SearchableAlbum::normalizedArtist,
                    normalizedComposite = SearchableAlbum::normalizedComposite,
                )
                .let(::sortRankedAlbums)
                .take(12)

            val matchingArtists = index.artists
                .rankMatching(
                    query = query,
                    normalizedTitle = SearchableArtist::normalizedName,
                    normalizedArtist = { "" },
                    normalizedComposite = SearchableArtist::normalizedName,
                )
                .sortedWith(
                    compareByDescending<RankedResult<SearchableArtist>> { it.score }
                        .thenByDescending { it.value.songCount }
                        .thenBy { it.value.normalizedName },
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
            val recentAlbumIdSet = recentAlbumIds.toHashSet()
            val candidates = albums.map { album ->
                SuggestedAlbumCandidate(
                    album = album,
                    playCount = albumPlayCounts[album.id] ?: 0,
                    isRecent = album.id in recentAlbumIdSet,
                    normalizedArtist = normalizeSearchText(album.artist),
                    normalizedTitle = normalizeSearchText(album.title),
                )
            }

            val seen = HashSet<Long>(6)
            val output = ArrayList<Album>(6)

            fun addIfNeeded(album: Album): Boolean {
                if (!seen.add(album.id)) return false
                output += album
                return output.size == 6
            }

            candidates
                .asSequence()
                .filter { it.playCount > 0 }
                .sortedWith(
                    compareBy<SuggestedAlbumCandidate> { it.playCount }
                        .thenBy { if (it.isRecent) 1 else 0 }
                        .thenBy(SuggestedAlbumCandidate::normalizedArtist)
                        .thenBy(SuggestedAlbumCandidate::normalizedTitle),
                )
                .map(SuggestedAlbumCandidate::album)
                .forEach { album ->
                    if (addIfNeeded(album)) return output
                }

            candidates
                .asSequence()
                .filter { it.playCount == 0 }
                .sortedWith(
                    compareBy<SuggestedAlbumCandidate> { if (it.isRecent) 1 else 0 }
                        .thenBy(SuggestedAlbumCandidate::normalizedArtist)
                        .thenBy(SuggestedAlbumCandidate::normalizedTitle),
                )
                .map(SuggestedAlbumCandidate::album)
                .forEach { album ->
                    if (addIfNeeded(album)) return output
                }

            return output
        }

        fun sanitizeSearchHistory(
            history: List<SearchHistoryEntry>,
            index: SearchIndex,
        ): List<SearchHistoryEntry> {
            val sanitized = ArrayList<SearchHistoryEntry>(history.size)
            val seenKeys = HashSet<String>(history.size)
            history.forEach { entry ->
                val sanitizedEntry = when (entry.kind) {
                    SearchHistoryKind.Album -> {
                        entry.albumId
                            ?.let(index.albumsById::get)
                            ?.let(::albumSearchHistoryEntry)
                    }

                    SearchHistoryKind.Artist -> {
                        val normalizedArtist = normalizeSearchText(entry.query ?: entry.title)
                        val artist = index.artistsByNormalizedName[normalizedArtist] ?: return@forEach
                        entry.copy(
                            key = "artist:${artist.normalizedName}",
                            title = artist.displayName,
                            artUri = artist.artUri,
                            query = artist.displayName,
                        )
                    }
                }
                if (sanitizedEntry != null && seenKeys.add(sanitizedEntry.key)) {
                    sanitized += sanitizedEntry
                }
            }
            return sanitized
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
