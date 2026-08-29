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
import elovaire.music.droidbeauty.app.domain.search.SearchableAlbum
import elovaire.music.droidbeauty.app.domain.search.SearchablePlaylist
import elovaire.music.droidbeauty.app.domain.search.SearchableSong
import elovaire.music.droidbeauty.app.domain.search.normalizeSearchText
import elovaire.music.droidbeauty.app.domain.search.searchArtistsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchIndexedAlbumsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchIndexedPlaylists
import elovaire.music.droidbeauty.app.domain.search.searchIndexedSongsForPicker
import elovaire.music.droidbeauty.app.domain.search.toSearchableAlbum
import elovaire.music.droidbeauty.app.domain.search.toSearchableSong
import java.util.Locale

private val songContextComparator = compareBy<Song>(
    { it.album.lowercase(Locale.ROOT) },
    { it.discNumber },
    { it.trackNumber },
    { it.title.lowercase(Locale.ROOT) },
    { it.id },
)

internal interface MediaLibraryBrowser {
    fun childrenOf(id: ElovaireMediaId): List<MediaItem>
    fun childrenOfPage(id: ElovaireMediaId, page: Int, pageSize: Int): List<MediaItem> {
        val items = childrenOf(id)
        val from = page.toLong() * pageSize.toLong()
        if (page < 0 || pageSize <= 0 || from >= items.size) return emptyList()
        val to = (from + pageSize.toLong()).coerceAtMost(items.size.toLong())
        return items.subList(from.toInt(), to.toInt())
    }
    fun item(mediaId: String): MediaItem?
    fun search(query: String, limit: Int = 50): List<MediaItem>

    fun searchRevision(): String = ""

    fun searchPage(query: String, offset: Int, limit: Int): List<MediaItem> {
        if (offset < 0 || limit <= 0) return emptyList()
        val endExclusive = offset.toLong() + limit.toLong()
        if (endExclusive > MediaLibraryRequestPolicy.MAX_SEARCH_RESULT_ITEMS) return emptyList()
        return search(query, endExclusive.toInt())
            .drop(offset)
            .take(limit)
    }

    fun searchCount(query: String): Int = search(query, MediaLibraryRequestPolicy.MAX_SEARCH_RESULT_ITEMS).size
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
                .songsForArtistInContext(ElovaireMediaIds.decodeName(id.encodedName))
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Genre -> snapshot
                .songsForGenreInContext(ElovaireMediaIds.decodeName(id.encodedName))
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Playlist -> snapshot.playlistSongs(id.playlistId).map(ElovaireMediaItems::song)
            is ElovaireMediaId.Bucket -> bucketChildren(id, snapshot)
        }
    }

    override fun childrenOfPage(
        id: ElovaireMediaId,
        page: Int,
        pageSize: Int,
    ): List<MediaItem> {
        val snapshot = snapshot()
        if (!snapshot.permissionGranted) {
            return pageWindow(childrenOf(id), page, pageSize)
        }
        if (snapshot.songs.isEmpty()) {
            return pageWindow(childrenOf(id), page, pageSize)
        }
        return when (id) {
            ElovaireMediaId.Songs -> bucketIfLargePage(
                parent = BUCKET_PARENT_SONGS,
                rows = snapshot.songsByTitle(),
                label = Song::title,
                item = ElovaireMediaItems::song,
                page = page,
                pageSize = pageSize,
            )
            ElovaireMediaId.Albums -> bucketIfLargePage(
                parent = BUCKET_PARENT_ALBUMS,
                rows = snapshot.albumsByTitle(),
                label = Album::title,
                item = ElovaireMediaItems::album,
                page = page,
                pageSize = pageSize,
            )
            ElovaireMediaId.Artists -> bucketIfLargePage(
                parent = BUCKET_PARENT_ARTISTS,
                rows = snapshot.artistNames(),
                label = { it },
                item = ElovaireMediaItems::artist,
                page = page,
                pageSize = pageSize,
            )
            ElovaireMediaId.Genres -> pageWindow(snapshot.genreNames(), page, pageSize)
                .map(ElovaireMediaItems::genre)
            ElovaireMediaId.Playlists -> pageWindow(snapshot.nonEmptyPlaylistsByName(), page, pageSize)
                .map(ElovaireMediaItems::playlist)
            ElovaireMediaId.Favorites -> pageWindow(snapshot.favoriteSongsByTitle(), page, pageSize)
                .map(ElovaireMediaItems::song)
            ElovaireMediaId.RecentlyAdded -> pageWindow(snapshot.recentlyAddedSongs(), page, pageSize)
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Album -> pageWindow(snapshot.album(id.albumId)?.songs.orEmpty(), page, pageSize)
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Artist -> pageWindow(
                snapshot.songsForArtistInContext(ElovaireMediaIds.decodeName(id.encodedName)),
                page,
                pageSize,
            ).map(ElovaireMediaItems::song)
            is ElovaireMediaId.Genre -> pageWindow(
                snapshot.songsForGenreInContext(ElovaireMediaIds.decodeName(id.encodedName)),
                page,
                pageSize,
            ).map(ElovaireMediaItems::song)
            is ElovaireMediaId.Playlist -> pageWindow(snapshot.playlistSongs(id.playlistId), page, pageSize)
                .map(ElovaireMediaItems::song)
            is ElovaireMediaId.Bucket -> bucketChildrenPage(id, snapshot, page, pageSize)
            else -> pageWindow(childrenOf(id), page, pageSize)
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
                snapshot.songsForArtistInContext(artist)
                    .toQueue(artist)
            }
            is ElovaireMediaId.Genre -> {
                val genre = ElovaireMediaIds.decodeName(parsed.encodedName)
                snapshot.songsForGenreInContext(genre)
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
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty() || limit <= 0) return emptyList()
        val normalizedQuery = NormalizedSearchQuery.from(query)
        return searchMediaIds(snapshot, normalizedQuery, limit).mapNotNull { mediaId ->
            searchMediaItem(snapshot, mediaId)
        }
    }

    private fun searchMediaIds(
        snapshot: MediaTreeSnapshot,
        normalizedQuery: NormalizedSearchQuery,
        limit: Int,
    ): List<String> {
        if (limit <= 0) return emptyList()
        if (normalizedQuery.value.isBlank()) {
            return defaultQueue(snapshot)?.queue.orEmpty().take(limit).map { song ->
                ElovaireMediaIds.song(song.id)
            }
        }
        val artistRows = snapshot.artistSearchRows()
        val genreRows = snapshot.genreSearchRows()
        val exactAndStrongTitleSongs = snapshot.searchableSongs()
            .filter {
                it.normalizedTitle == normalizedQuery.value || it.normalizedTitle.startsWith(normalizedQuery.value)
            }
            .sortedBy(SearchableSong::normalizedTitle)
            .map(SearchableSong::song)
        val broaderSongs = searchIndexedSongsForPicker(snapshot.searchableSongs(), normalizedQuery)
        return ArrayList<String>(limit.coerceAtMost(128)).apply {
            val seen = HashSet<String>(limit.coerceAtMost(128))
            fun addDistinctId(mediaId: String) {
                if (size < limit && seen.add(mediaId)) add(mediaId)
            }
            fun <T> addDistinctIds(items: Iterable<T>, id: (T) -> String) {
                for (item in items) {
                    if (size >= limit) return
                    addDistinctId(id(item))
                }
            }
            addDistinctIds(exactAndStrongTitleSongs) { ElovaireMediaIds.song(it.id) }
            addDistinctIds(
                searchIndexedAlbumsForPicker(snapshot.searchableAlbums(), normalizedQuery),
            ) { ElovaireMediaIds.album(it.id) }
            addDistinctIds(
                searchArtistsForPicker(
                    artists = artistRows,
                    query = normalizedQuery,
                    name = NamedSongs::name,
                    songs = NamedSongs::songs,
                    songCount = { it.songs.size },
                ),
            ) { ElovaireMediaIds.artist(it.name) }
            addDistinctIds(
                searchIndexedPlaylists(snapshot.searchablePlaylists(), normalizedQuery),
            ) { ElovaireMediaIds.playlist(it.id) }
            addDistinctIds(
                searchArtistsForPicker(
                    artists = genreRows,
                    query = normalizedQuery,
                    name = NamedSongs::name,
                    songs = NamedSongs::songs,
                    songCount = { it.songs.size },
                ),
            ) { ElovaireMediaIds.genre(it.name) }
            addDistinctIds(broaderSongs) { ElovaireMediaIds.song(it.id) }
        }
    }

    private fun searchMediaItem(snapshot: MediaTreeSnapshot, mediaId: String): MediaItem? {
        return when (val parsed = ElovaireMediaIds.parse(mediaId)) {
            is ElovaireMediaId.Song -> snapshot.song(parsed.songId)?.let(ElovaireMediaItems::song)
            is ElovaireMediaId.Album -> snapshot.album(parsed.albumId)?.let(ElovaireMediaItems::album)
            is ElovaireMediaId.Artist -> ElovaireMediaItems.artist(
                ElovaireMediaIds.decodeName(parsed.encodedName),
            )
            is ElovaireMediaId.Genre -> ElovaireMediaItems.genre(
                ElovaireMediaIds.decodeName(parsed.encodedName),
            )
            is ElovaireMediaId.Playlist -> snapshot.playlist(parsed.playlistId)
                ?.let(ElovaireMediaItems::playlist)
            else -> null
        }
    }

    override fun searchRevision(): String {
        val snapshot = snapshot()
        return buildString {
            append(snapshot.libraryRevision)
            append(':')
            append(System.identityHashCode(snapshot.playlists))
            append(':')
            append(snapshot.permissionGranted)
        }
    }

    override fun searchPage(query: String, offset: Int, limit: Int): List<MediaItem> {
        if (offset < 0 || limit <= 0) return emptyList()
        val endExclusive = offset.toLong() + limit.toLong()
        if (endExclusive > MediaLibraryRequestPolicy.MAX_SEARCH_RESULT_ITEMS) return emptyList()
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return emptyList()
        return searchMediaIds(snapshot, NormalizedSearchQuery.from(query), endExclusive.toInt())
            .drop(offset)
            .mapNotNull { mediaId -> searchMediaItem(snapshot, mediaId) }
    }

    override fun searchCount(query: String): Int {
        val normalizedQuery = NormalizedSearchQuery.from(query)
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return 0
        if (normalizedQuery.value.isBlank()) return defaultQueue(snapshot)?.queue?.size ?: 0
        return searchMediaIds(
            snapshot = snapshot,
            normalizedQuery = normalizedQuery,
            limit = MediaLibraryRequestPolicy.MAX_SEARCH_RESULT_ITEMS,
        ).size
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
        val userData = preferenceStore.userDataSnapshot.value
        return snapshotCache.snapshot(
            permissionGranted = scan.permissionGranted,
            songs = content.songs,
            albums = content.albums,
            libraryRevision = content.contentRevision,
            playlists = userData.playlists,
            favoriteSongIds = userData.favoriteSongIds,
            recentSongIds = userData.recentSongIds,
            lastPlayedCollectionKind = userData.lastPlayedCollectionKind,
            lastPlayedCollectionId = userData.lastPlayedCollectionId,
        )
    }

    private fun List<Song>.toQueue(
        sourceLabel: String,
        sourcePlaylistId: Long? = null,
    ): ResolvedPlayableQueue? {
        val startSong = firstOrNull() ?: return null
        return ResolvedPlayableQueue(startSong, this, sourceLabel, sourcePlaylistId)
    }

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

    private fun bucketChildrenPage(
        id: ElovaireMediaId.Bucket,
        snapshot: MediaTreeSnapshot,
        page: Int,
        pageSize: Int,
    ): List<MediaItem> {
        return when (id.parent) {
            BUCKET_PARENT_SONGS -> pageWindow(
                snapshot.songsByTitle().filter { bucketKey(it.title) == id.key },
                page,
                pageSize,
            ).map(ElovaireMediaItems::song)
            BUCKET_PARENT_ALBUMS -> pageWindow(
                snapshot.albumsByTitle().filter { bucketKey(it.title) == id.key },
                page,
                pageSize,
            ).map(ElovaireMediaItems::album)
            BUCKET_PARENT_ARTISTS -> pageWindow(
                snapshot.artistNames().filter { bucketKey(it) == id.key },
                page,
                pageSize,
            ).map(ElovaireMediaItems::artist)
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

    private inline fun <T> bucketIfLargePage(
        parent: String,
        rows: List<T>,
        crossinline label: (T) -> String,
        item: (T) -> MediaItem,
        page: Int,
        pageSize: Int,
    ): List<MediaItem> {
        if (rows.size <= DIRECT_BROWSE_LIMIT) {
            return pageWindow(rows, page, pageSize).map(item)
        }
        return pageWindow(
            rows
                .map { bucketKey(label(it)) }
                .distinct()
                .sortedWith(compareBy<String> { if (it == SYMBOL_BUCKET) "ZZ" else it }),
            page,
            pageSize,
        ).map { ElovaireMediaItems.bucket(parent, it) }
    }

    private fun <T> pageWindow(rows: List<T>, page: Int, pageSize: Int): List<T> {
        if (page < 0 || pageSize <= 0) return emptyList()
        val from = page.toLong() * pageSize.toLong()
        if (from >= rows.size) return emptyList()
        val to = (from + pageSize.toLong()).coerceAtMost(rows.size.toLong())
        return rows.subList(from.toInt(), to.toInt())
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
        val libraryRevision: String = "",
    ) {
        private val favoriteSongs by lazy(LazyThreadSafetyMode.PUBLICATION) { songs.filter { it.id in favoriteSongIds } }
        private val favoriteSongsByTitle by lazy(LazyThreadSafetyMode.PUBLICATION) {
            favoriteSongs.sortedWith(songTitleComparator)
        }
        private val songsByTitle by lazy(LazyThreadSafetyMode.PUBLICATION) { songs.sortedWith(songTitleComparator) }
        private val albumsByTitle by lazy(LazyThreadSafetyMode.PUBLICATION) {
            albums.sortedWith(
                compareBy<Album>({ it.title.lowercase(Locale.ROOT) }, { it.title }, { it.id }),
            )
        }
        private val nonEmptyPlaylistsByName by lazy(LazyThreadSafetyMode.PUBLICATION) {
            playlists.filter { it.songIds.isNotEmpty() }.sortedWith(
                compareBy<Playlist>({ it.name.lowercase(Locale.ROOT) }, { it.name }, { it.id }),
            )
        }
        private val recentlyAddedSongs by lazy(LazyThreadSafetyMode.PUBLICATION) {
            songs.sortedWith(
                compareByDescending<Song> { it.dateAddedSeconds }
                    .thenBy { it.title.lowercase(Locale.ROOT) }
                    .thenBy(Song::title)
                    .thenBy(Song::id),
            )
        }
        private val artistNames by lazy(LazyThreadSafetyMode.PUBLICATION) {
            songs.map(Song::libraryArtistName).distinct().sortedBy { it.lowercase(Locale.ROOT) }
        }
        private val genreNames by lazy(LazyThreadSafetyMode.PUBLICATION) {
            songs.map { it.genre.ifBlank { UNKNOWN_GENRE } }.distinct().sortedBy { it.lowercase(Locale.ROOT) }
        }
        private val usefulGenres by lazy(LazyThreadSafetyMode.PUBLICATION) {
            songs.any { it.genre.isNotBlank() && it.genre != UNKNOWN_GENRE }
        }
        private val songsById by lazy(LazyThreadSafetyMode.PUBLICATION) { songs.associateBy(Song::id) }
        private val albumsById by lazy(LazyThreadSafetyMode.PUBLICATION) { albums.associateBy(Album::id) }
        private val playlistsById by lazy(LazyThreadSafetyMode.PUBLICATION) { playlists.associateBy(Playlist::id) }
        private val searchableSongs by lazy(LazyThreadSafetyMode.PUBLICATION) { songs.map(Song::toSearchableSong) }
        private val searchableAlbums by lazy(LazyThreadSafetyMode.PUBLICATION) { albums.map(Album::toSearchableAlbum) }
        private val searchablePlaylists by lazy(LazyThreadSafetyMode.PUBLICATION) {
            playlists
                .filter { it.songIds.isNotEmpty() }
                .map { playlist ->
                    val normalizedName = normalizeSearchText(playlist.name)
                    SearchablePlaylist(
                        playlist = playlist,
                        normalizedName = normalizedName,
                        normalizedComposite = normalizedName,
                    )
                }
        }
        private val songsByArtist by lazy(LazyThreadSafetyMode.PUBLICATION) {
            songs.groupBy { it.libraryArtistName().lowercase(Locale.ROOT) }
        }
        private val songsByGenre by lazy(LazyThreadSafetyMode.PUBLICATION) {
            songs.groupBy { it.genre.ifBlank { UNKNOWN_GENRE }.lowercase(Locale.ROOT) }
        }
        private val songsByArtistForContext by lazy(LazyThreadSafetyMode.PUBLICATION) {
            songsByArtist.mapValues { (_, songs) -> songs.sortedWith(songContextComparator) }
        }
        private val songsByGenreForContext by lazy(LazyThreadSafetyMode.PUBLICATION) {
            songsByGenre.mapValues { (_, songs) -> songs.sortedWith(songContextComparator) }
        }
        private val artistSearchRows by lazy(LazyThreadSafetyMode.PUBLICATION) {
            artistNames.map { name -> NamedSongs(name = name, songs = songsForArtist(name)) }
        }
        private val genreSearchRows by lazy(LazyThreadSafetyMode.PUBLICATION) {
            genreNames.map { name -> NamedSongs(name = name, songs = songsForGenre(name)) }
        }

        private companion object {
            val songTitleComparator = compareBy<Song>(
                { it.title.lowercase(Locale.ROOT) },
                { it.title },
                { it.id },
            )
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
        fun searchableAlbums(): List<SearchableAlbum> = searchableAlbums
        fun searchablePlaylists(): List<SearchablePlaylist> = searchablePlaylists
        fun artistSearchRows(): List<NamedSongs> = artistSearchRows
        fun genreSearchRows(): List<NamedSongs> = genreSearchRows
        fun song(id: Long): Song? = songsById[id]
        fun album(id: Long): Album? = albumsById[id]
        fun playlist(id: Long): Playlist? = playlistsById[id]
        fun songsForArtist(name: String): List<Song> = songsByArtist[name.lowercase(Locale.ROOT)].orEmpty()
        fun songsForGenre(name: String): List<Song> = songsByGenre[name.lowercase(Locale.ROOT)].orEmpty()
        fun songsForArtistInContext(name: String): List<Song> =
            songsByArtistForContext[name.lowercase(Locale.ROOT)].orEmpty()
        fun songsForGenreInContext(name: String): List<Song> =
            songsByGenreForContext[name.lowercase(Locale.ROOT)].orEmpty()
        fun hasUsefulGenres(): Boolean = usefulGenres
        fun playlistSongs(playlistId: Long): List<Song> {
            val playlist = playlistsById[playlistId] ?: return emptyList()
            return playlist.songIds.mapNotNull(songsById::get)
        }
    }

    internal data class NamedSongs(
        val name: String,
        val songs: List<Song>,
    )

    private companion object {
        const val DIRECT_BROWSE_LIMIT = 100
        const val BUCKET_PARENT_SONGS = "songs"
        const val BUCKET_PARENT_ALBUMS = "albums"
        const val BUCKET_PARENT_ARTISTS = "artists"
        const val SYMBOL_BUCKET = "#"
        const val UNKNOWN_GENRE = "Unknown Genre"
    }
}

internal class MediaTreeSnapshotCache {
    private var snapshot: ElovaireMediaTree.MediaTreeSnapshot? = null

    @Synchronized
    fun clear() {
        snapshot = null
    }

    @Synchronized
    fun snapshot(
        permissionGranted: Boolean,
        songs: List<Song>,
        albums: List<Album>,
        playlists: List<Playlist>,
        favoriteSongIds: List<Long>,
        recentSongIds: List<Long>,
        lastPlayedCollectionKind: PlaybackCollectionKind?,
        lastPlayedCollectionId: Long?,
        libraryRevision: String = "",
    ): ElovaireMediaTree.MediaTreeSnapshot {
        snapshot?.takeIf {
            it.permissionGranted == permissionGranted &&
                (libraryRevision.isNotBlank() && it.libraryRevision == libraryRevision ||
                    libraryRevision.isBlank() && it.songs === songs && it.albums === albums) &&
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
            libraryRevision = libraryRevision,
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
