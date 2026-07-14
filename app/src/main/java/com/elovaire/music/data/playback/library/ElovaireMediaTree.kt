package elovaire.music.droidbeauty.app.data.playback.library

import androidx.media3.common.MediaItem
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.settings.RootSettingsReader
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
    private val preferenceStore: RootSettingsReader,
) {
    private val snapshotCache = MediaTreeSnapshotCache()

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
                ElovaireMediaItems.genresRoot().takeIf { snapshot.hasUsefulGenres() },
                ElovaireMediaItems.playlistsRoot(),
                ElovaireMediaItems.songsRoot(),
            ).filterNotNull()
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
            ElovaireMediaId.Songs -> bucketIfLarge(
                parent = BUCKET_PARENT_SONGS,
                rows = snapshot.songsByTitle(),
                label = Song::title,
                item = ElovaireMediaItems::song,
            )
            ElovaireMediaId.Albums -> bucketIfLarge(
                parent = BUCKET_PARENT_ALBUMS,
                rows = snapshot.albumsByTitle(),
                label = Album::title,
                item = ElovaireMediaItems::album,
            )
            ElovaireMediaId.Artists -> bucketIfLarge(
                parent = BUCKET_PARENT_ARTISTS,
                rows = snapshot.artistNames(),
                label = { it },
                item = ElovaireMediaItems::artist,
            )
            ElovaireMediaId.Genres -> snapshot.genreNames().map(ElovaireMediaItems::genre)
            ElovaireMediaId.Playlists -> snapshot.nonEmptyPlaylistsByName()
                .map(ElovaireMediaItems::playlist)
            ElovaireMediaId.Favorites -> snapshot.favoriteSongsByTitle().map(ElovaireMediaItems::song)
            ElovaireMediaId.RecentlyAdded -> snapshot.recentlyAddedSongs().map(ElovaireMediaItems::song)
            is ElovaireMediaId.Song -> emptyList()
            is ElovaireMediaId.Album -> snapshot.albums.firstOrNull { it.id == id.albumId }
                ?.songs.orEmpty()
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Artist -> snapshot.songs
                .filter { it.libraryArtistName().equals(ElovaireMediaIds.decodeName(id.encodedName), ignoreCase = true) }
                .sortedSongsForContext()
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Genre -> snapshot.songs
                .filter { it.genre.equals(ElovaireMediaIds.decodeName(id.encodedName), ignoreCase = true) }
                .sortedSongsForContext()
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Playlist -> snapshot.playlistSongs(id.playlistId).map(ElovaireMediaItems::song)
            is ElovaireMediaId.Bucket -> bucketChildren(id, snapshot)
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
            is ElovaireMediaId.Bucket -> ElovaireMediaItems.bucket(parsed.parent, parsed.key)
        }
    }

    fun resolvePlayableQueue(mediaId: String): ResolvedPlayableQueue? {
        val parsed = ElovaireMediaIds.parse(mediaId) ?: return null
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return null
        return when (parsed) {
            ElovaireMediaId.Songs -> snapshot.songsByTitle().toQueue("Songs")
            ElovaireMediaId.Favorites -> snapshot.favoriteSongsByTitle().toQueue("Favorites")
            ElovaireMediaId.RecentlyAdded -> snapshot.recentlyAddedSongs().toQueue("Recently added")
            is ElovaireMediaId.Song -> {
                val song = snapshot.songs.firstOrNull { it.id == parsed.songId } ?: return null
                val album = snapshot.albums.firstOrNull { it.id == song.albumId }
                if (album != null) {
                    ResolvedPlayableQueue(song, album.songs, album.title, null)
                } else {
                    ResolvedPlayableQueue(song, snapshot.songsByTitle(), song.album, null)
                }
            }
            is ElovaireMediaId.Album -> {
                val album = snapshot.albums.firstOrNull { it.id == parsed.albumId } ?: return null
                album.songs.toQueue(album.title)
            }
            is ElovaireMediaId.Artist -> {
                val artist = ElovaireMediaIds.decodeName(parsed.encodedName)
                snapshot.songs
                    .filter { it.libraryArtistName().equals(artist, ignoreCase = true) }
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
            is ElovaireMediaId.Bucket -> bucketQueue(parsed, snapshot)
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
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return emptyList()
        if (normalizedQuery.value.isBlank()) {
            return defaultQueue(snapshot)?.queue.orEmpty().take(limit).map(ElovaireMediaItems::song)
        }
        val artistRows = snapshot.artistNames().map { name ->
            NamedSongs(name = name, songs = snapshot.songsForArtist(name))
        }
        val genreRows = snapshot.genreNames().map { name ->
            NamedSongs(name = name, songs = snapshot.songsForGenre(name))
        }
        val exactAndStrongTitleSongs = snapshot.songs
            .filter {
                val normalizedTitle = normalizeSearchText(it.title)
                normalizedTitle == normalizedQuery.value || normalizedTitle.startsWith(normalizedQuery.value)
            }
            .sortedBy { it.title.lowercase() }
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

    fun resolveSearchQueue(query: String): ResolvedPlayableQueue? {
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return null
        val normalizedQuery = NormalizedSearchQuery.from(query)
        if (normalizedQuery.value.isBlank()) return defaultQueue(snapshot)
        search(query, limit = 1).firstOrNull()?.let { return resolvePlayableQueue(it.mediaId) }
        return null
    }

    fun defaultPlayableQueue(): ResolvedPlayableQueue? {
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return null
        return defaultQueue(snapshot)
    }

    fun resumptionQueue(): ResolvedPlayableQueue? {
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return null
        val recentSong = snapshot.recentSongIds.firstNotNullOfOrNull { songId ->
            snapshot.songs.firstOrNull { it.id == songId }
        } ?: return null
        return when (snapshot.lastPlayedCollectionKind) {
            PlaybackCollectionKind.Playlist -> snapshot.lastPlayedCollectionId
                ?.let(snapshot::playlistSongs)
                ?.takeIf { songs -> songs.any { it.id == recentSong.id } }
                ?.let { songs ->
                    val playlistName = snapshot.playlists.firstOrNull { it.id == snapshot.lastPlayedCollectionId }?.name
                    ResolvedPlayableQueue(recentSong, songs, playlistName ?: "Playlist", snapshot.lastPlayedCollectionId)
                }
            PlaybackCollectionKind.Album -> snapshot.lastPlayedCollectionId
                ?.let { albumId -> snapshot.albums.firstOrNull { it.id == albumId } }
                ?.takeIf { album -> album.songs.any { it.id == recentSong.id } }
                ?.let { album -> ResolvedPlayableQueue(recentSong, album.songs, album.title, null) }
            null -> null
        } ?: snapshot.albums.firstOrNull { it.id == recentSong.albumId }
            ?.let { album -> ResolvedPlayableQueue(recentSong, album.songs, album.title, null) }
            ?: ResolvedPlayableQueue(recentSong, snapshot.songsByTitle(), "Songs", null)
    }

    private fun snapshot(): MediaTreeSnapshot {
        val content = libraryRepository.contentState.value
        val scan = libraryRepository.scanState.value
        return snapshotCache.snapshot(
            permissionGranted = scan.permissionGranted,
            songs = content.songs,
            albums = content.albums,
            playlists = preferenceStore.playlists.value,
            favoriteSongIds = preferenceStore.favoriteSongIds.value,
            recentSongIds = preferenceStore.recentSongIds.value,
            lastPlayedCollectionKind = preferenceStore.lastPlayedCollectionKind.value,
            lastPlayedCollectionId = preferenceStore.lastPlayedCollectionId.value,
        )
    }

    private fun List<Song>.toQueue(
        sourceLabel: String,
        sourcePlaylistId: Long? = null,
    ): ResolvedPlayableQueue? {
        val startSong = firstOrNull() ?: return null
        return ResolvedPlayableQueue(startSong, this, sourceLabel, sourcePlaylistId)
    }

    private fun List<Song>.sortedSongsForContext(): List<Song> = sortedWith(
        compareBy<Song>(
            { it.album.lowercase() },
            { it.discNumber },
            { it.trackNumber },
            { it.title.lowercase() },
            { it.id },
        ),
    )

    private fun bucketChildren(
        id: ElovaireMediaId.Bucket,
        snapshot: MediaTreeSnapshot,
    ): List<MediaItem> {
        return when (id.parent) {
            BUCKET_PARENT_SONGS -> snapshot.songsByTitle()
                .filter { bucketKey(it.title) == id.key }
                .map(ElovaireMediaItems::song)
            BUCKET_PARENT_ALBUMS -> snapshot.albumsByTitle()
                .filter { bucketKey(it.title) == id.key }
                .map(ElovaireMediaItems::album)
            BUCKET_PARENT_ARTISTS -> snapshot.artistNames()
                .filter { bucketKey(it) == id.key }
                .map(ElovaireMediaItems::artist)
            else -> emptyList()
        }
    }

    private fun bucketQueue(
        id: ElovaireMediaId.Bucket,
        snapshot: MediaTreeSnapshot,
    ): ResolvedPlayableQueue? {
        return when (id.parent) {
            BUCKET_PARENT_SONGS -> snapshot.songsByTitle()
                .filter { bucketKey(it.title) == id.key }
                .toQueue("Songs ${id.key}")
            else -> null
        }
    }

    private inline fun <T> bucketIfLarge(
        parent: String,
        rows: List<T>,
        crossinline label: (T) -> String,
        item: (T) -> MediaItem,
    ): List<MediaItem> {
        if (rows.size <= DIRECT_BROWSE_LIMIT) return rows.map(item)
        return rows
            .map { bucketKey(label(it)) }
            .distinct()
            .sortedWith(compareBy<String> { if (it == SYMBOL_BUCKET) "ZZ" else it })
            .map { ElovaireMediaItems.bucket(parent, it) }
    }

    private fun bucketKey(label: String): String {
        val first = label.trim().firstOrNull()?.uppercaseChar() ?: return SYMBOL_BUCKET
        return if (first in 'A'..'Z') first.toString() else SYMBOL_BUCKET
    }

    private fun defaultQueue(snapshot: MediaTreeSnapshot): ResolvedPlayableQueue? {
        return snapshot.favoriteSongsByTitle().toQueue("Favorites")
            ?: snapshot.recentlyAddedSongs().toQueue("Recently added")
            ?: snapshot.songsByTitle().toQueue("Songs")
    }

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

    internal data class MediaTreeSnapshot(
        val permissionGranted: Boolean,
        val songs: List<Song>,
        val albums: List<Album>,
        val playlists: List<Playlist>,
        val favoriteSongIdSource: List<Long>,
        val favoriteSongIds: Set<Long>,
        val recentSongIds: List<Long>,
        val lastPlayedCollectionKind: PlaybackCollectionKind?,
        val lastPlayedCollectionId: Long?,
    ) {
        private val favoriteSongs by lazy(LazyThreadSafetyMode.NONE) { songs.filter { it.id in favoriteSongIds } }
        private val favoriteSongsByTitle by lazy(LazyThreadSafetyMode.NONE) {
            favoriteSongs.sortedBy { it.title.lowercase() }
        }
        private val songsByTitle by lazy(LazyThreadSafetyMode.NONE) { songs.sortedBy { it.title.lowercase() } }
        private val albumsByTitle by lazy(LazyThreadSafetyMode.NONE) { albums.sortedBy { it.title.lowercase() } }
        private val nonEmptyPlaylistsByName by lazy(LazyThreadSafetyMode.NONE) {
            playlists.filter { it.songIds.isNotEmpty() }.sortedBy { it.name.lowercase() }
        }
        private val recentlyAddedSongs by lazy(LazyThreadSafetyMode.NONE) {
            songs.sortedByDescending(Song::dateAddedSeconds)
        }
        private val artistNames by lazy(LazyThreadSafetyMode.NONE) {
            songs.map(Song::libraryArtistName).distinct().sortedBy(String::lowercase)
        }
        private val genreNames by lazy(LazyThreadSafetyMode.NONE) {
            songs.map { it.genre.ifBlank { UNKNOWN_GENRE } }.distinct().sortedBy(String::lowercase)
        }
        private val songsById by lazy(LazyThreadSafetyMode.NONE) { songs.associateBy(Song::id) }
        private val songsByArtist by lazy(LazyThreadSafetyMode.NONE) {
            songs.groupBy { it.libraryArtistName().lowercase() }
        }
        private val songsByGenre by lazy(LazyThreadSafetyMode.NONE) {
            songs.groupBy { it.genre.lowercase() }
        }

        fun favoriteSongs(): List<Song> = favoriteSongs
        fun favoriteSongsByTitle(): List<Song> = favoriteSongsByTitle
        fun songsByTitle(): List<Song> = songsByTitle
        fun albumsByTitle(): List<Album> = albumsByTitle
        fun nonEmptyPlaylistsByName(): List<Playlist> = nonEmptyPlaylistsByName
        fun recentlyAddedSongs(): List<Song> = recentlyAddedSongs
        fun artistNames(): List<String> = artistNames
        fun genreNames(): List<String> = genreNames
        fun songsForArtist(name: String): List<Song> = songsByArtist[name.lowercase()].orEmpty()
        fun songsForGenre(name: String): List<Song> = songsByGenre[name.lowercase()].orEmpty()
        fun hasUsefulGenres(): Boolean = songs.any { it.genre.isNotBlank() && it.genre != UNKNOWN_GENRE }
        fun playlistSongs(playlistId: Long): List<Song> {
            val playlist = playlists.firstOrNull { it.id == playlistId } ?: return emptyList()
            return playlist.songIds.mapNotNull(songsById::get)
        }
    }

    private data class NamedSongs(
        val name: String,
        val songs: List<Song>,
    )

    private companion object {
        const val SEARCH_RESULT_LIMIT = 50
        const val DIRECT_BROWSE_LIMIT = 100
        const val BUCKET_PARENT_SONGS = "songs"
        const val BUCKET_PARENT_ALBUMS = "albums"
        const val BUCKET_PARENT_ARTISTS = "artists"
        const val SYMBOL_BUCKET = "#"
        const val UNKNOWN_ARTIST = "Unknown Artist"
        const val UNKNOWN_GENRE = "Unknown Genre"
    }
}

internal class MediaTreeSnapshotCache {
    private var snapshot: ElovaireMediaTree.MediaTreeSnapshot? = null

    fun snapshot(
        permissionGranted: Boolean,
        songs: List<Song>,
        albums: List<Album>,
        playlists: List<Playlist>,
        favoriteSongIds: List<Long>,
        recentSongIds: List<Long>,
        lastPlayedCollectionKind: PlaybackCollectionKind?,
        lastPlayedCollectionId: Long?,
    ): ElovaireMediaTree.MediaTreeSnapshot {
        snapshot?.takeIf {
            it.permissionGranted == permissionGranted &&
                it.songs === songs &&
                it.albums === albums &&
                it.playlists === playlists &&
                it.favoriteSongIdSource === favoriteSongIds &&
                it.recentSongIds === recentSongIds &&
                it.lastPlayedCollectionKind == lastPlayedCollectionKind &&
                it.lastPlayedCollectionId == lastPlayedCollectionId
        }?.let { return it }
        return ElovaireMediaTree.MediaTreeSnapshot(
            permissionGranted = permissionGranted,
            songs = songs,
            albums = albums,
            playlists = playlists,
            favoriteSongIdSource = favoriteSongIds,
            favoriteSongIds = favoriteSongIds.toSet(),
            recentSongIds = recentSongIds,
            lastPlayedCollectionKind = lastPlayedCollectionKind,
            lastPlayedCollectionId = lastPlayedCollectionId,
        ).also { snapshot = it }
    }
}

private fun Song.libraryArtistName(): String {
    return albumArtist?.takeIf { it.isNotBlank() } ?: artist.ifBlank { "Unknown Artist" }
}

internal data class ResolvedPlayableQueue(
    val startSong: Song,
    val queue: List<Song>,
    val sourceLabel: String,
    val sourcePlaylistId: Long?,
)
