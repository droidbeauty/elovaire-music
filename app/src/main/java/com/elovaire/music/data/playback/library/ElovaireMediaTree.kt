package elovaire.music.droidbeauty.app.data.playback.library

import androidx.media3.common.MediaItem
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.data.settings.RootSettingsReader
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.search.NormalizedSearchQuery
import elovaire.music.droidbeauty.app.domain.search.SearchableSong
import elovaire.music.droidbeauty.app.domain.search.searchAlbumsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchArtistsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchPlaylists
import elovaire.music.droidbeauty.app.domain.search.searchIndexedSongsForPicker
import elovaire.music.droidbeauty.app.domain.search.toSearchableSong
import java.util.Locale

internal interface MediaLibraryBrowser {
    fun childrenOf(id: ElovaireMediaId): List<MediaItem>
    fun item(mediaId: String): MediaItem?
    fun search(query: String, limit: Int = 50): List<MediaItem>
}

internal interface MediaLibraryCommandResolver {
    fun resolvePlayableQueue(mediaId: String): ResolvedPlayableQueue?
    fun resolveSearchQueue(query: String): ResolvedPlayableQueue?
    fun defaultPlayableQueue(): ResolvedPlayableQueue?
    fun resumptionQueue(): ResolvedPlayableQueue?
}

internal class ElovaireMediaTree(
    private val libraryRepository: LibraryRepository,
    private val preferenceStore: RootSettingsReader,
) : MediaLibraryBrowser, MediaLibraryCommandResolver {
    private val snapshotCache = MediaTreeSnapshotCache()

    fun onMemoryPressure(pressure: MemoryPressure) {
        if (pressure != MemoryPressure.Normal) snapshotCache.clear()
    }

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

    override fun childrenOf(id: ElovaireMediaId): List<MediaItem> {
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
            is ElovaireMediaId.Album -> snapshot.album(id.albumId)
                ?.songs.orEmpty()
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Artist -> snapshot
                .songsForArtist(ElovaireMediaIds.decodeName(id.encodedName))
                .sortedSongsForContext()
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Genre -> snapshot
                .songsForGenre(ElovaireMediaIds.decodeName(id.encodedName))
                .sortedSongsForContext()
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Playlist -> snapshot.playlistSongs(id.playlistId).map(ElovaireMediaItems::song)
            is ElovaireMediaId.Bucket -> bucketChildren(id, snapshot)
        }
    }

    override fun item(mediaId: String): MediaItem? {
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
            is ElovaireMediaId.Song -> snapshot.song(parsed.songId)
                ?.let(ElovaireMediaItems::song)
            is ElovaireMediaId.Album -> snapshot.album(parsed.albumId)
                ?.let(ElovaireMediaItems::album)
            is ElovaireMediaId.Artist -> ElovaireMediaItems.artist(ElovaireMediaIds.decodeName(parsed.encodedName))
            is ElovaireMediaId.Genre -> ElovaireMediaItems.genre(ElovaireMediaIds.decodeName(parsed.encodedName))
            is ElovaireMediaId.Playlist -> snapshot.playlist(parsed.playlistId)
                ?.let(ElovaireMediaItems::playlist)
            is ElovaireMediaId.Bucket -> ElovaireMediaItems.bucket(parsed.parent, parsed.key)
        }
    }

    override fun resolvePlayableQueue(mediaId: String): ResolvedPlayableQueue? {
        val parsed = ElovaireMediaIds.parse(mediaId) ?: return null
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return null
        return when (parsed) {
            ElovaireMediaId.Songs -> snapshot.songsByTitle().toQueue("Songs")
            ElovaireMediaId.Favorites -> snapshot.favoriteSongsByTitle().toQueue("Favorites")
            ElovaireMediaId.RecentlyAdded -> snapshot.recentlyAddedSongs().toQueue("Recently added")
            is ElovaireMediaId.Song -> {
                val song = snapshot.song(parsed.songId) ?: return null
                val album = snapshot.album(song.albumId)
                if (album != null) {
                    ResolvedPlayableQueue(song, album.songs, album.title, null)
                } else {
                    ResolvedPlayableQueue(song, snapshot.songsByTitle(), song.album, null)
                }
            }
            is ElovaireMediaId.Album -> {
                val album = snapshot.album(parsed.albumId) ?: return null
                album.songs.toQueue(album.title)
            }
            is ElovaireMediaId.Artist -> {
                val artist = ElovaireMediaIds.decodeName(parsed.encodedName)
                snapshot.songsForArtist(artist)
                    .sortedSongsForContext()
                    .toQueue(artist)
            }
            is ElovaireMediaId.Genre -> {
                val genre = ElovaireMediaIds.decodeName(parsed.encodedName)
                snapshot.songsForGenre(genre)
                    .sortedSongsForContext()
                    .toQueue(genre)
            }
            is ElovaireMediaId.Playlist -> {
                val playlist = snapshot.playlist(parsed.playlistId) ?: return null
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

    override fun search(query: String, limit: Int): List<MediaItem> {
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
        val exactAndStrongTitleSongs = snapshot.searchableSongs()
            .filter {
                it.normalizedTitle == normalizedQuery.value || it.normalizedTitle.startsWith(normalizedQuery.value)
            }
            .sortedBy(SearchableSong::normalizedTitle)
            .map(SearchableSong::song)
        val broaderSongs = searchIndexedSongsForPicker(snapshot.searchableSongs(), normalizedQuery)
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

    override fun resolveSearchQueue(query: String): ResolvedPlayableQueue? {
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return null
        val normalizedQuery = NormalizedSearchQuery.from(query)
        if (normalizedQuery.value.isBlank()) return defaultQueue(snapshot)
        search(query, limit = 1).firstOrNull()?.let { return resolvePlayableQueue(it.mediaId) }
        return null
    }

    override fun defaultPlayableQueue(): ResolvedPlayableQueue? {
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return null
        return defaultQueue(snapshot)
    }

    override fun resumptionQueue(): ResolvedPlayableQueue? {
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return null
        val recentSong = snapshot.recentSongIds.firstNotNullOfOrNull { songId ->
            snapshot.song(songId)
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
                ?.let(snapshot::album)
                ?.takeIf { album -> album.songs.any { it.id == recentSong.id } }
                ?.let { album -> ResolvedPlayableQueue(recentSong, album.songs, album.title, null) }
            null -> null
        } ?: snapshot.album(recentSong.albumId)
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
            { it.album.lowercase(Locale.ROOT) },
            { it.discNumber },
            { it.trackNumber },
            { it.title.lowercase(Locale.ROOT) },
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
            favoriteSongs.sortedBy { it.title.lowercase(Locale.ROOT) }
        }
        private val songsByTitle by lazy(LazyThreadSafetyMode.NONE) { songs.sortedBy { it.title.lowercase(Locale.ROOT) } }
        private val albumsByTitle by lazy(LazyThreadSafetyMode.NONE) { albums.sortedBy { it.title.lowercase(Locale.ROOT) } }
        private val nonEmptyPlaylistsByName by lazy(LazyThreadSafetyMode.NONE) {
            playlists.filter { it.songIds.isNotEmpty() }.sortedBy { it.name.lowercase(Locale.ROOT) }
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
        private val albumsById by lazy(LazyThreadSafetyMode.NONE) { albums.associateBy(Album::id) }
        private val playlistsById by lazy(LazyThreadSafetyMode.NONE) { playlists.associateBy(Playlist::id) }
        private val searchableSongs by lazy(LazyThreadSafetyMode.NONE) { songs.map(Song::toSearchableSong) }
        private val songsByArtist by lazy(LazyThreadSafetyMode.NONE) {
            songs.groupBy { it.libraryArtistName().lowercase(Locale.ROOT) }
        }
        private val songsByGenre by lazy(LazyThreadSafetyMode.NONE) {
            songs.groupBy { it.genre.ifBlank { UNKNOWN_GENRE }.lowercase(Locale.ROOT) }
        }

        fun favoriteSongs(): List<Song> = favoriteSongs
        fun favoriteSongsByTitle(): List<Song> = favoriteSongsByTitle
        fun songsByTitle(): List<Song> = songsByTitle
        fun albumsByTitle(): List<Album> = albumsByTitle
        fun nonEmptyPlaylistsByName(): List<Playlist> = nonEmptyPlaylistsByName
        fun recentlyAddedSongs(): List<Song> = recentlyAddedSongs
        fun artistNames(): List<String> = artistNames
        fun genreNames(): List<String> = genreNames
        fun searchableSongs(): List<SearchableSong> = searchableSongs
        fun song(id: Long): Song? = songsById[id]
        fun album(id: Long): Album? = albumsById[id]
        fun playlist(id: Long): Playlist? = playlistsById[id]
        fun songsForArtist(name: String): List<Song> = songsByArtist[name.lowercase(Locale.ROOT)].orEmpty()
        fun songsForGenre(name: String): List<Song> = songsByGenre[name.lowercase(Locale.ROOT)].orEmpty()
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

    fun clear() {
        snapshot = null
    }

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
