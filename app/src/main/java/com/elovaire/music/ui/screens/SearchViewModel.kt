package elovaire.music.droidbeauty.app.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
    private val searchConfig = combine(
        _query,
        _showAllSongResults,
        _searchSongSortMode,
        _showSearchSongSortOptions,
    ) { query, showAllSongs, sortMode, showSortOptions ->
        SearchConfig(
            query = query,
            showAllSongs = showAllSongs,
            sortMode = sortMode,
            showSortOptions = showSortOptions,
        )
    }
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

    val uiState: StateFlow<SearchUiState> = combine(
        searchConfig,
        libraryRepository.contentState,
        preferenceStore.searchHistory,
        preferenceStore.albumPlayCounts,
        playbackSnapshot,
    ) { searchConfig, libraryContent, recentSearches, albumPlayCounts, playbackSnapshot ->
        val trimmedQuery = searchConfig.query.trim()
        val allMatchingSongs = buildMatchingSongs(
            query = trimmedQuery,
            songs = libraryContent.songs,
            sortMode = searchConfig.sortMode,
        )
        val contentMode = when {
            searchConfig.showAllSongs && trimmedQuery.isNotBlank() -> SearchContentMode.AllSongs
            trimmedQuery.isBlank() -> SearchContentMode.Discover
            else -> SearchContentMode.Results
        }
        SearchUiState(
            query = searchConfig.query,
            showAllSongResults = searchConfig.showAllSongs,
            searchSongSortMode = searchConfig.sortMode,
            showSearchSongSortOptions = searchConfig.showSortOptions,
            contentMode = contentMode,
            recentSearches = recentSearches,
            allMatchingSongs = allMatchingSongs,
            matchingSongs = allMatchingSongs.take(20),
            matchingAlbums = buildMatchingAlbums(trimmedQuery, libraryContent),
            matchingArtists = buildMatchingArtists(trimmedQuery, libraryContent),
            suggestedAlbums = buildSuggestedAlbums(
                libraryContent = libraryContent,
                albumPlayCounts = albumPlayCounts,
                recentAlbumIds = playbackSnapshot.recentAlbumIds,
            ),
            currentSongId = playbackSnapshot.currentSongId,
            isPlaybackActive = playbackSnapshot.isPlaybackActive,
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
        data class SearchConfig(
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

        fun buildMatchingSongs(
            query: String,
            songs: List<Song>,
            sortMode: SearchSongSortMode,
        ): List<Song> {
            if (query.isBlank()) return emptyList()
            val filteredSongs = songs.filter { song ->
                searchMatchesComposite(
                    query = query,
                    fields = listOf(song.title, song.artist, song.album),
                )
            }
            return when (sortMode) {
                SearchSongSortMode.Title -> filteredSongs.sortedWith(
                    compareBy<Song> { it.title.lowercase() }
                        .thenBy { it.artist.lowercase() }
                        .thenBy { it.album.lowercase() },
                )

                SearchSongSortMode.Artist -> filteredSongs.sortedWith(
                    compareBy<Song> { it.artist.lowercase() }
                        .thenBy { it.title.lowercase() }
                        .thenBy { it.album.lowercase() },
                )
            }
        }

        fun buildMatchingAlbums(
            query: String,
            libraryContent: LibraryContentState,
        ): List<Album> {
            if (query.isBlank()) return emptyList()
            return libraryContent.albums.filter { album ->
                searchMatchesComposite(
                    query = query,
                    fields = listOf(album.title, album.artist),
                )
            }.take(12)
        }

        fun buildMatchingArtists(
            query: String,
            libraryContent: LibraryContentState,
        ): List<SearchArtistResult> {
            if (query.isBlank()) return emptyList()
            return libraryContent.songs
                .groupBy { it.artist }
                .values
                .map { artistSongs ->
                    SearchArtistResult(
                        name = artistSongs.first().artist,
                        songCount = artistSongs.size,
                        artUri = artistSongs.first().artUri,
                    )
                }
                .filter { artist ->
                    searchMatchesComposite(
                        query = query,
                        fields = listOf(artist.name),
                    )
                }
                .take(6)
        }

        fun buildSuggestedAlbums(
            libraryContent: LibraryContentState,
            albumPlayCounts: Map<Long, Int>,
            recentAlbumIds: List<Long>,
        ): List<Album> {
            val recentAlbumIdSet = recentAlbumIds.toSet()
            val rarePlayedAlbums = libraryContent.albums
                .mapNotNull { album ->
                    val playCount = albumPlayCounts[album.id] ?: 0
                    if (playCount > 0) album to playCount else null
                }
                .sortedWith(
                    compareBy<Pair<Album, Int>> { it.second }
                        .thenBy { album -> if (album.first.id in recentAlbumIdSet) 1 else 0 }
                        .thenBy { it.first.artist.lowercase() }
                        .thenBy { it.first.title.lowercase() },
                )
                .map { it.first }

            val neverPlayedAlbums = libraryContent.albums
                .filter { (albumPlayCounts[it.id] ?: 0) == 0 }
                .sortedWith(
                    compareBy<Album> { if (it.id in recentAlbumIdSet) 1 else 0 }
                        .thenBy { it.artist.lowercase() }
                        .thenBy { it.title.lowercase() },
                )

            return buildList {
                (rarePlayedAlbums + neverPlayedAlbums).forEach { album ->
                    if (none { it.id == album.id }) add(album)
                    if (size == 6) return@buildList
                }
            }
        }

        fun searchMatchesComposite(
            query: String,
            fields: List<String>,
        ): Boolean {
            val normalizedQuery = query.trim().lowercase()
            if (normalizedQuery.isBlank()) return true
            val tokens = normalizedQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
            if (tokens.isEmpty()) return true
            val haystack = fields.asSequence()
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() }
                .joinToString(separator = " ")
            return tokens.all { token -> haystack.contains(token) }
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
