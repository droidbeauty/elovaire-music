package elovaire.music.droidbeauty.app.data.playback.library

import androidx.media3.common.MediaItem
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.search.NormalizedSearchQuery
import elovaire.music.droidbeauty.app.domain.search.normalizeSearchText
import elovaire.music.droidbeauty.app.domain.search.searchAlbumsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchArtistsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchPlaylists
import elovaire.music.droidbeauty.app.domain.search.searchSongsForPicker

internal class ElovaireMediaTree(
    private val libraryRepository: LibraryRepository,
    private val preferenceStore: PreferenceStore,
) {
    fun rootChildren(): List<MediaItem> {
        val snapshot = snapshot()
        return when {
            !snapshot.permissionGranted -> listOf(ElovaireMediaItems.permissionRequiredInfo())
            snapshot.songs.isEmpty() -> listOf(ElovaireMediaItems.emptyLibraryInfo())
            else -> listOf(
                ElovaireMediaItems.recentlyAddedRoot(),
                ElovaireMediaItems.favoritesRoot(),
                ElovaireMediaItems.albumsRoot(),
                ElovaireMediaItems.artistsRoot(),
                ElovaireMediaItems.playlistsRoot(),
                ElovaireMediaItems.songsRoot(),
            )
        }
    }

    fun childrenOf(id: ElovaireMediaId): List<MediaItem> {
        val snapshot = snapshot()
        if (!snapshot.permissionGranted) {
            return if (id == ElovaireMediaId.Root) listOf(ElovaireMediaItems.permissionRequiredInfo()) else emptyList()
        }
        if (snapshot.songs.isEmpty()) {
            return if (id == ElovaireMediaId.Root) listOf(ElovaireMediaItems.emptyLibraryInfo()) else emptyList()
        }
        return when (id) {
            ElovaireMediaId.Root -> rootChildren()
            ElovaireMediaId.PermissionRequired,
            ElovaireMediaId.EmptyLibrary,
            -> emptyList()
            ElovaireMediaId.Songs -> snapshot.songs.sortedSongsByTitle().map(ElovaireMediaItems::song)
            ElovaireMediaId.Albums -> snapshot.albums.sortedAlbumsByTitle().map(ElovaireMediaItems::album)
            ElovaireMediaId.Artists -> snapshot.artistNames().map(ElovaireMediaItems::artist)
            ElovaireMediaId.Genres -> snapshot.genreNames().map(ElovaireMediaItems::genre)
            ElovaireMediaId.Playlists -> snapshot.playlists
                .filter { it.songIds.isNotEmpty() }
                .sortedBy { it.name.lowercase() }
                .map(ElovaireMediaItems::playlist)
            ElovaireMediaId.Favorites -> snapshot.favoriteSongs().sortedSongsByTitle().map(ElovaireMediaItems::song)
            ElovaireMediaId.RecentlyAdded -> snapshot.recentlyAddedSongs().map(ElovaireMediaItems::song)
            is ElovaireMediaId.Song -> emptyList()
            is ElovaireMediaId.Album -> snapshot.albums.firstOrNull { it.id == id.albumId }
                ?.songs.orEmpty()
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Artist -> snapshot.songs
                .filter { it.artist.equals(ElovaireMediaIds.decodeName(id.encodedName), ignoreCase = true) }
                .sortedSongsForContext()
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Genre -> snapshot.songs
                .filter { it.genre.equals(ElovaireMediaIds.decodeName(id.encodedName), ignoreCase = true) }
                .sortedSongsForContext()
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Playlist -> snapshot.playlistSongs(id.playlistId).map(ElovaireMediaItems::song)
        }
    }

    fun item(mediaId: String): MediaItem? {
        val parsed = ElovaireMediaIds.parse(mediaId) ?: return null
        val snapshot = snapshot()
        return when (parsed) {
            ElovaireMediaId.Root -> ElovaireMediaItems.root()
            ElovaireMediaId.PermissionRequired -> ElovaireMediaItems.permissionRequiredInfo()
            ElovaireMediaId.EmptyLibrary -> ElovaireMediaItems.emptyLibraryInfo()
            ElovaireMediaId.Songs -> ElovaireMediaItems.songsRoot()
            ElovaireMediaId.Albums -> ElovaireMediaItems.albumsRoot()
            ElovaireMediaId.Artists -> ElovaireMediaItems.artistsRoot()
            ElovaireMediaId.Genres -> ElovaireMediaItems.genresRoot()
            ElovaireMediaId.Playlists -> ElovaireMediaItems.playlistsRoot()
            ElovaireMediaId.Favorites -> ElovaireMediaItems.favoritesRoot()
            ElovaireMediaId.RecentlyAdded -> ElovaireMediaItems.recentlyAddedRoot()
            is ElovaireMediaId.Song -> snapshot.songs.firstOrNull { it.id == parsed.songId }
                ?.let(ElovaireMediaItems::song)
            is ElovaireMediaId.Album -> snapshot.albums.firstOrNull { it.id == parsed.albumId }
                ?.let(ElovaireMediaItems::album)
            is ElovaireMediaId.Artist -> ElovaireMediaItems.artist(ElovaireMediaIds.decodeName(parsed.encodedName))
            is ElovaireMediaId.Genre -> ElovaireMediaItems.genre(ElovaireMediaIds.decodeName(parsed.encodedName))
            is ElovaireMediaId.Playlist -> snapshot.playlists.firstOrNull { it.id == parsed.playlistId }
                ?.let(ElovaireMediaItems::playlist)
        }
    }

    fun resolvePlayableQueue(mediaId: String): ResolvedPlayableQueue? {
        val parsed = ElovaireMediaIds.parse(mediaId) ?: return null
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return null
        return when (parsed) {
            ElovaireMediaId.Songs -> snapshot.songs.sortedSongsByTitle().toQueue("Songs")
            ElovaireMediaId.Favorites -> snapshot.favoriteSongs().sortedSongsByTitle().toQueue("Favorites")
            ElovaireMediaId.RecentlyAdded -> snapshot.recentlyAddedSongs().toQueue("Recently added")
            is ElovaireMediaId.Song -> {
                val song = snapshot.songs.firstOrNull { it.id == parsed.songId } ?: return null
                val album = snapshot.albums.firstOrNull { it.id == song.albumId }
                if (album != null) {
                    ResolvedPlayableQueue(song, album.songs, album.title, null)
                } else {
                    ResolvedPlayableQueue(song, snapshot.songs.sortedSongsByTitle(), song.album, null)
                }
            }
            is ElovaireMediaId.Album -> {
                val album = snapshot.albums.firstOrNull { it.id == parsed.albumId } ?: return null
                album.songs.toQueue(album.title)
            }
            is ElovaireMediaId.Artist -> {
                val artist = ElovaireMediaIds.decodeName(parsed.encodedName)
                snapshot.songs
                    .filter { it.artist.equals(artist, ignoreCase = true) }
                    .sortedSongsForContext()
                    .toQueue(artist)
            }
            is ElovaireMediaId.Genre -> {
                val genre = ElovaireMediaIds.decodeName(parsed.encodedName)
                snapshot.songs
                    .filter { it.genre.equals(genre, ignoreCase = true) }
                    .sortedSongsForContext()
                    .toQueue(genre)
            }
            is ElovaireMediaId.Playlist -> {
                val playlist = snapshot.playlists.firstOrNull { it.id == parsed.playlistId } ?: return null
                snapshot.playlistSongs(playlist.id).toQueue(playlist.name, playlist.id)
            }
            ElovaireMediaId.PermissionRequired,
            ElovaireMediaId.EmptyLibrary,
            ElovaireMediaId.Root,
            ElovaireMediaId.Albums,
            ElovaireMediaId.Artists,
            ElovaireMediaId.Genres,
            ElovaireMediaId.Playlists,
            -> null
        }
    }

    fun search(query: String, limit: Int = SEARCH_RESULT_LIMIT): List<MediaItem> {
        val normalizedQuery = NormalizedSearchQuery.from(query)
        if (normalizedQuery.value.isBlank()) return emptyList()
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return emptyList()
        val artistRows = snapshot.artistNames().map { name ->
            NamedSongs(name = name, songs = snapshot.songs.filter { it.artist.equals(name, ignoreCase = true) })
        }
        val genreRows = snapshot.genreNames().map { name ->
            NamedSongs(name = name, songs = snapshot.songs.filter { it.genre.equals(name, ignoreCase = true) })
        }
        val exactAndStrongTitleSongs = snapshot.songs
            .filter {
                val normalizedTitle = normalizeSearchText(it.title)
                normalizedTitle == normalizedQuery.value || normalizedTitle.startsWith(normalizedQuery.value)
            }
            .sortedSongsByTitle()
        val broaderSongs = searchSongsForPicker(snapshot.songs, normalizedQuery)
        return mutableListOf<MediaItem>().apply {
            addDistinctItems(exactAndStrongTitleSongs, limit, ElovaireMediaItems::song)
            addDistinctItems(searchAlbumsForPicker(snapshot.albums, normalizedQuery), limit, ElovaireMediaItems::album)
            addDistinctItems(
                searchArtistsForPicker(
                    artists = artistRows,
                    query = normalizedQuery,
                    name = NamedSongs::name,
                    songs = NamedSongs::songs,
                    songCount = { it.songs.size },
                ),
                limit,
            ) { ElovaireMediaItems.artist(it.name) }
            addDistinctItems(
                searchPlaylists(snapshot.playlists.filter { it.songIds.isNotEmpty() }, normalizedQuery),
                limit,
                ElovaireMediaItems::playlist,
            )
            addDistinctItems(
                searchArtistsForPicker(
                    artists = genreRows,
                    query = normalizedQuery,
                    name = NamedSongs::name,
                    songs = NamedSongs::songs,
                    songCount = { it.songs.size },
                ),
                limit,
            ) { ElovaireMediaItems.genre(it.name) }
            addDistinctItems(broaderSongs, limit, ElovaireMediaItems::song)
        }
    }

    private fun snapshot(): MediaTreeSnapshot {
        val content = libraryRepository.contentState.value
        val scan = libraryRepository.scanState.value
        return MediaTreeSnapshot(
            permissionGranted = scan.permissionGranted,
            songs = content.songs,
            albums = content.albums,
            playlists = preferenceStore.playlists.value,
            favoriteSongIds = preferenceStore.favoriteSongIds.value.toSet(),
        )
    }

    private fun List<Song>.toQueue(
        sourceLabel: String,
        sourcePlaylistId: Long? = null,
    ): ResolvedPlayableQueue? {
        val startSong = firstOrNull() ?: return null
        return ResolvedPlayableQueue(startSong, this, sourceLabel, sourcePlaylistId)
    }

    private fun List<Song>.sortedSongsByTitle(): List<Song> = sortedBy { it.title.lowercase() }
    private fun List<Song>.sortedSongsForContext(): List<Song> = sortedWith(
        compareBy<Song>(
            { it.album.lowercase() },
            { it.discNumber },
            { it.trackNumber },
            { it.title.lowercase() },
            { it.id },
        ),
    )
    private fun List<Album>.sortedAlbumsByTitle(): List<Album> = sortedBy { it.title.lowercase() }

    private fun MutableList<MediaItem>.addDistinct(item: MediaItem) {
        if (none { it.mediaId == item.mediaId }) {
            add(item)
        }
    }

    private inline fun <T> MutableList<MediaItem>.addDistinctItems(
        items: List<T>,
        limit: Int,
        itemFactory: (T) -> MediaItem,
    ) {
        for (item in items) {
            if (size >= limit) return
            addDistinct(itemFactory(item))
        }
    }

    private data class MediaTreeSnapshot(
        val permissionGranted: Boolean,
        val songs: List<Song>,
        val albums: List<Album>,
        val playlists: List<Playlist>,
        val favoriteSongIds: Set<Long>,
    ) {
        fun favoriteSongs(): List<Song> = songs.filter { it.id in favoriteSongIds }
        fun recentlyAddedSongs(): List<Song> = songs.sortedByDescending(Song::dateAddedSeconds)
        fun artistNames(): List<String> = songs
            .map { it.artist.ifBlank { UNKNOWN_ARTIST } }
            .distinct()
            .sortedBy(String::lowercase)
        fun genreNames(): List<String> = songs
            .map { it.genre.ifBlank { UNKNOWN_GENRE } }
            .distinct()
            .sortedBy(String::lowercase)
        fun playlistSongs(playlistId: Long): List<Song> {
            val playlist = playlists.firstOrNull { it.id == playlistId } ?: return emptyList()
            val songsById = songs.associateBy(Song::id)
            return playlist.songIds.mapNotNull(songsById::get)
        }
    }

    private data class NamedSongs(
        val name: String,
        val songs: List<Song>,
    )

    private companion object {
        const val SEARCH_RESULT_LIMIT = 50
        const val UNKNOWN_ARTIST = "Unknown Artist"
        const val UNKNOWN_GENRE = "Unknown Genre"
    }
}

internal data class ResolvedPlayableQueue(
    val startSong: Song,
    val queue: List<Song>,
    val sourceLabel: String,
    val sourcePlaylistId: Long?,
)
