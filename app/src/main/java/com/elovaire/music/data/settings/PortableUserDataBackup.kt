package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import android.util.AtomicFile
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import elovaire.music.droidbeauty.app.data.library.MediaIdentityResolver
import elovaire.music.droidbeauty.app.data.library.PortableMediaIdentityProjection
import elovaire.music.droidbeauty.app.data.library.TrackMatchConfidence
import elovaire.music.droidbeauty.app.data.library.TrackMatchIdentity
import elovaire.music.droidbeauty.app.data.playlists.normalizePlaylistName
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.data.smartplaylists.deserializeSmartPlaylists
import elovaire.music.droidbeauty.app.data.smartplaylists.serializeSmartPlaylists
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.json.JSONArray
import org.json.JSONObject

/** Versioned user-data transfer format. Device-specific locators and credentials are excluded. */
@Suppress("TooGenericExceptionCaught")
internal class PortableUserDataBackup(
    context: Context,
    private val clock: AppClock = AndroidAppClock,
    private val fileName: String = DEFAULT_FILE_NAME,
) {
    private val atomicFile by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        allowStrictModeDiskReads {
            AtomicFile(context.applicationContext.filesDir.resolve(fileName))
        }
    }
    private val lock = Any()
    private var preparedMediaIdentityProjection: PortableMediaIdentityProjection? = null

    fun write(
        snapshot: UserDataSnapshot,
        songs: List<Song>,
        appVersion: String = BuildConfig.VERSION_NAME,
        userDataRevision: Long = 0L,
        contentRevision: String = "",
    ) = synchronized(lock) {
        val bytes = encodeLocked(
            snapshot = snapshot,
            songs = songs,
            createdAtMs = clock.wallTimeMs(),
            appVersion = appVersion,
            userDataRevision = userDataRevision,
            contentRevision = contentRevision,
        )
        val output = atomicFile.startWrite()
        try {
            output.write(bytes)
            output.flush()
            atomicFile.finishWrite(output)
        } catch (failure: Throwable) {
            atomicFile.failWrite(output)
            throw failure
        }
    }

    fun encode(
        snapshot: UserDataSnapshot,
        songs: List<Song>,
        createdAtMs: Long,
        appVersion: String = BuildConfig.VERSION_NAME,
        userDataRevision: Long = 0L,
        contentRevision: String = "",
    ): ByteArray = synchronized(lock) {
        encodeLocked(snapshot, songs, createdAtMs, appVersion, userDataRevision, contentRevision)
    }

    private fun encodeLocked(
        snapshot: UserDataSnapshot,
        songs: List<Song>,
        createdAtMs: Long,
        appVersion: String,
        userDataRevision: Long,
        contentRevision: String,
    ): ByteArray {
        val identityProjection = contentRevision.takeIf(String::isNotBlank)?.let { revision ->
            preparedMediaIdentityProjection?.takeIf { it.revision == revision }
                ?: MediaIdentityResolver.portableIdentityProjection(
                    songs = songs,
                    revision = revision,
                ).also {
                    preparedMediaIdentityProjection = it
                }
        }
        return encodePortableUserData(
            snapshot = snapshot,
            songs = songs,
            createdAtMs = createdAtMs,
            appVersion = appVersion,
            userDataRevision = userDataRevision,
            preparedSongIdentities = identityProjection?.identitiesBySongId,
        )
    }

    fun readBytes(): ByteArray? = synchronized(lock) {
        val file = atomicFile.baseFile
        if (!file.isFile || file.length() !in 1L..MAX_FILE_BYTES) return@synchronized null
        runCatching { atomicFile.openRead().use { input -> input.readBytes() } }.getOrNull()
    }

    fun read(): PortableUserData? = readBytes()?.let(::decodePortableUserData)

    fun restore(
        current: UserDataSnapshot,
        songs: List<Song>,
    ): PortableUserDataImport? = read()?.mergeInto(current, songs)
}

internal data class PortableUserData(
    val createdAtMs: Long,
    val appVersion: String,
    val userDataRevision: Long = 0L,
    val playlists: List<PortablePlaylist>,
    val smartPlaylists: List<SmartPlaylist>,
    val favoriteSongs: List<TrackMatchIdentity>,
    val songPlayCounts: List<PortableSongPlayCount>,
    val recentSongs: List<TrackMatchIdentity>,
)

internal data class PortablePlaylist(
    val id: Long,
    val name: String,
    val songs: List<TrackMatchIdentity>,
)

internal data class PortableSongPlayCount(
    val song: TrackMatchIdentity,
    val count: Int,
)

internal data class PortableUserDataImport(
    val snapshot: UserDataSnapshot,
    val unresolvedReferenceCount: Int,
)

internal fun encodePortableUserData(
    snapshot: UserDataSnapshot,
    songs: List<Song>,
    createdAtMs: Long,
    appVersion: String,
    userDataRevision: Long = 0L,
    preparedSongIdentities: Map<Long, TrackMatchIdentity>? = null,
): ByteArray {
    require(appVersion.length <= MAX_APP_VERSION_CHARS)
    val songsById = if (preparedSongIdentities == null) songs.associateBy(Song::id) else null
    fun reference(songId: Long): TrackMatchIdentity? {
        return preparedSongIdentities?.get(songId)
            ?: songsById?.get(songId)?.let(MediaIdentityResolver::trackMatchIdentity)?.copy(sourceStableKey = null)
    }
    fun JSONArray.putReferences(songIds: Iterable<Long>) {
        songIds.forEach { songId -> reference(songId)?.let { put(it.toJson()) } }
    }

    val root = JSONObject()
        .put(KEY_SCHEMA_VERSION, FORMAT_VERSION)
        .put(KEY_CREATED_AT_MS, createdAtMs.coerceAtLeast(0L))
        .put(KEY_APP_VERSION, appVersion)
        .put(KEY_USER_DATA_REVISION, userDataRevision.coerceAtLeast(0L))
        .put(
            KEY_PLAYLISTS,
            JSONArray().apply {
                snapshot.playlists.filterNot(Playlist::isSystem).forEach { playlist ->
                    put(
                        JSONObject()
                            .put(KEY_ID, playlist.id)
                            .put(KEY_NAME, normalizePlaylistName(playlist.name))
                            .put(KEY_SONGS, JSONArray().apply { putReferences(playlist.songIds) }),
                    )
                }
            },
        )
        .put(KEY_SMART_PLAYLISTS, serializeSmartPlaylists(snapshot.smartPlaylists))
        .put(KEY_FAVORITES, JSONArray().apply { putReferences(snapshot.favoriteSongIds) })
        .put(
            KEY_SONG_COUNTS,
            JSONArray().apply {
                snapshot.songPlayCounts.forEach { (songId, count) ->
                    reference(songId)?.let { identity ->
                        put(JSONObject().put(KEY_SONG, identity.toJson()).put(KEY_COUNT, count.coerceAtLeast(0)))
                    }
                }
            },
        )
        .put(KEY_RECENT_SONGS, JSONArray().apply { putReferences(snapshot.recentSongIds) })
    val withChecksum = root.put(KEY_CHECKSUM, portableUserDataChecksum(root, CHECKSUM_KEYS_V2))
    return withChecksum.toString().toByteArray(StandardCharsets.UTF_8).also { bytes ->
        require(bytes.size <= MAX_FILE_BYTES) { "Portable user-data backup is too large." }
    }
}

internal fun decodePortableUserData(bytes: ByteArray): PortableUserData? {
    if (bytes.isEmpty() || bytes.size > MAX_FILE_BYTES) return null
    val root = runCatching { JSONObject(bytes.toString(StandardCharsets.UTF_8)) }.getOrNull() ?: return null
    val schemaVersion = root.optInt(KEY_SCHEMA_VERSION, 0)
    val checksumKeys = when (schemaVersion) {
        LEGACY_FORMAT_VERSION -> CHECKSUM_KEYS_V1
        FORMAT_VERSION -> CHECKSUM_KEYS_V2
        else -> return null
    }
    if (root.optString(KEY_CHECKSUM) != portableUserDataChecksum(root, checksumKeys)) return null
    val appVersion = root.optString(KEY_APP_VERSION).takeIf { it.length <= MAX_APP_VERSION_CHARS } ?: return null
    val playlists = root.optJSONArray(KEY_PLAYLISTS)?.let { array ->
        buildList {
            repeat(array.length().coerceAtMost(MAX_PLAYLIST_COUNT)) { index ->
                val item = array.optJSONObject(index) ?: return@repeat
                val id = item.optLong(KEY_ID, Long.MIN_VALUE).takeIf { it > 0L } ?: return@repeat
                val name = normalizePlaylistName(item.optString(KEY_NAME))
                    .takeIf { it.isNotBlank() && it.length <= MAX_PLAYLIST_NAME_CHARS }
                    ?: return@repeat
                val songs = decodeIdentityArray(item.optJSONArray(KEY_SONGS))
                add(PortablePlaylist(id, name, songs))
            }
        }
    }.orEmpty()
    val smartPlaylists = deserializeSmartPlaylists(root.optString(KEY_SMART_PLAYLISTS))
    val favoriteSongs = decodeIdentityArray(root.optJSONArray(KEY_FAVORITES))
    val songPlayCounts = root.optJSONArray(KEY_SONG_COUNTS)?.let { array ->
        buildList {
            repeat(array.length().coerceAtMost(MAX_REFERENCE_COUNT)) { index ->
                val item = array.optJSONObject(index) ?: return@repeat
                val identity = item.optJSONObject(KEY_SONG)?.toTrackMatchIdentity() ?: return@repeat
                val count = item.optInt(KEY_COUNT, -1).takeIf { it >= 0 } ?: return@repeat
                add(PortableSongPlayCount(identity, count))
            }
        }
    }.orEmpty()
    val recentSongs = decodeIdentityArray(root.optJSONArray(KEY_RECENT_SONGS)).take(MAX_RECENT_ITEMS)
    return PortableUserData(
        createdAtMs = root.optLong(KEY_CREATED_AT_MS, 0L).coerceAtLeast(0L),
        appVersion = appVersion,
        userDataRevision = if (schemaVersion == LEGACY_FORMAT_VERSION) {
            0L
        } else {
            root.optLong(KEY_USER_DATA_REVISION, -1L).takeIf { it >= 0L } ?: return null
        },
        playlists = playlists,
        smartPlaylists = smartPlaylists,
        favoriteSongs = favoriteSongs,
        songPlayCounts = songPlayCounts,
        recentSongs = recentSongs,
    )
}

internal fun PortableUserData.mergeInto(
    current: UserDataSnapshot,
    songs: List<Song>,
): PortableUserDataImport {
    var unresolved = 0
    val trackMatcher = MediaIdentityResolver.prepareTrackMatcher(songs)
    fun resolve(identity: TrackMatchIdentity): Long? {
        val result = trackMatcher.resolve(identity)
        when (result.confidence) {
            TrackMatchConfidence.Exact,
            TrackMatchConfidence.Strong,
            -> result.song?.id?.let { return it }
            TrackMatchConfidence.Probable,
            TrackMatchConfidence.Ambiguous,
            TrackMatchConfidence.NoMatch,
            -> Unit
        }
        unresolved += 1
        return null
    }

    val mergedPlaylists = current.playlists.toMutableList()
    val playlistIndexById = hashMapOf<Long, Int>()
    val playlistIndexByName = hashMapOf<String, Int>()
    val occupiedPlaylistIds = hashSetOf<Long>()
    mergedPlaylists.forEachIndexed { index, playlist ->
        occupiedPlaylistIds += playlist.id
        if (!playlist.isSystem) {
            playlistIndexById.putIfAbsent(playlist.id, index)
            playlistIndexByName.putIfAbsent(normalizePlaylistName(playlist.name), index)
        }
    }
    var nextPlaylistId = sequenceOf(
        mergedPlaylists.maxOfOrNull(Playlist::id),
        current.smartPlaylists.maxOfOrNull(SmartPlaylist::id),
        playlists.maxOfOrNull(PortablePlaylist::id),
    ).filterNotNull().maxOrNull()?.let { if (it == Long.MAX_VALUE) Long.MAX_VALUE else it + 1L } ?: 1L
    playlists.forEach { portable ->
        val resolvedSongs = linkedSetOf<Long>()
        portable.songs.forEach { identity -> resolve(identity)?.let(resolvedSongs::add) }
        val existingIndex = findPortablePlaylistIndex(
            playlistId = portable.id,
            playlistName = portable.name,
            playlistIndexById = playlistIndexById,
            playlistIndexByName = playlistIndexByName,
        )
        if (existingIndex != null) {
            val existing = mergedPlaylists[existingIndex]
            val mergedSongIds = linkedSetOf<Long>().apply {
                addAll(existing.songIds)
                addAll(resolvedSongs)
            }
            mergedPlaylists[existingIndex] = existing.copy(songIds = mergedSongIds.toList())
        } else {
            val id = portable.id.takeIf { it > 0L && it !in occupiedPlaylistIds }
                ?: nextPlaylistId
            if (nextPlaylistId < Long.MAX_VALUE) nextPlaylistId += 1L
            val index = mergedPlaylists.size
            mergedPlaylists += Playlist(id = id, name = portable.name, songIds = resolvedSongs.toList())
            occupiedPlaylistIds += id
            playlistIndexById.putIfAbsent(id, index)
            playlistIndexByName.putIfAbsent(portable.name, index)
        }
    }

    val mergedSmartPlaylists = current.smartPlaylists.toMutableList()
    val smartPlaylistIds = current.smartPlaylists.mapTo(hashSetOf(), SmartPlaylist::id)
    val smartPlaylistNames = current.smartPlaylists.mapTo(hashSetOf(), SmartPlaylist::name)
    var nextSmartId = sequenceOf(
        mergedSmartPlaylists.maxOfOrNull(SmartPlaylist::id),
        mergedPlaylists.maxOfOrNull(Playlist::id),
    ).filterNotNull().maxOrNull()?.let { if (it == Long.MAX_VALUE) Long.MAX_VALUE else it + 1L } ?: 1L
    smartPlaylists.forEach { imported ->
        if (imported.id !in smartPlaylistIds && imported.name !in smartPlaylistNames) {
            val id = imported.id.takeIf { candidate -> candidate !in smartPlaylistIds } ?: nextSmartId
            if (nextSmartId < Long.MAX_VALUE) nextSmartId += 1L
            mergedSmartPlaylists += imported.copy(id = id)
            smartPlaylistIds += id
            smartPlaylistNames += imported.name
        }
    }

    val favoriteIds = linkedSetOf<Long>().apply { addAll(current.favoriteSongIds) }
    favoriteSongs.forEach { identity -> resolve(identity)?.let(favoriteIds::add) }
    val mergedCounts = current.songPlayCounts.toMutableMap()
    songPlayCounts.forEach { item ->
        resolve(item.song)?.let { songId ->
            mergedCounts[songId] = maxOf(mergedCounts[songId] ?: 0, item.count)
        }
    }
    val recentIds = linkedSetOf<Long>()
    recentSongs.forEach { identity ->
        if (recentIds.size < MAX_RECENT_ITEMS) resolve(identity)?.let(recentIds::add)
    }
    current.recentSongIds.forEach { songId ->
        if (recentIds.size < MAX_RECENT_ITEMS) recentIds += songId
    }
    return PortableUserDataImport(
        snapshot = current.copy(
            playlists = mergedPlaylists,
            smartPlaylists = mergedSmartPlaylists,
            favoriteSongIds = favoriteIds.toList(),
            songPlayCounts = mergedCounts,
            recentSongIds = recentIds.toList(),
        ),
        unresolvedReferenceCount = unresolved,
    )
}

private fun findPortablePlaylistIndex(
    playlistId: Long,
    playlistName: String,
    playlistIndexById: Map<Long, Int>,
    playlistIndexByName: Map<String, Int>,
): Int? {
    val indexById = playlistIndexById[playlistId]
    val indexByName = playlistIndexByName[playlistName]
    return if (indexById != null && indexByName != null) {
        minOf(indexById, indexByName)
    } else {
        indexById ?: indexByName
    }
}

private fun TrackMatchIdentity.toJson(): JSONObject = JSONObject()
    .put(KEY_VERSION, version)
    .put(KEY_SIZE_BYTES, sizeBytes ?: JSONObject.NULL)
    .put(KEY_DURATION_MS, durationMs ?: JSONObject.NULL)
    .put(KEY_TITLE, normalizedTitle)
    .put(KEY_ARTIST, normalizedArtist)
    .put(KEY_ALBUM, normalizedAlbum)
    .put(KEY_ALBUM_ARTIST, normalizedAlbumArtist ?: JSONObject.NULL)
    .put(KEY_FILE_NAME, normalizedFileName ?: JSONObject.NULL)
    .put(KEY_TRACK, trackNumber ?: JSONObject.NULL)
    .put(KEY_DISC, discNumber ?: JSONObject.NULL)

private fun JSONObject.toTrackMatchIdentity(): TrackMatchIdentity? {
    val version = optInt(KEY_VERSION, 0).takeIf { it > 0 } ?: return null
    val title = optString(KEY_TITLE).takeIf(String::isNotBlank) ?: return null
    return TrackMatchIdentity(
        version = version,
        sizeBytes = optLong(KEY_SIZE_BYTES, Long.MIN_VALUE).takeIf { it >= 0L },
        durationMs = optLong(KEY_DURATION_MS, Long.MIN_VALUE).takeIf { it > 0L },
        normalizedTitle = title,
        normalizedArtist = optString(KEY_ARTIST),
        normalizedAlbum = optString(KEY_ALBUM),
        normalizedAlbumArtist = optNullableString(KEY_ALBUM_ARTIST),
        normalizedFileName = optNullableString(KEY_FILE_NAME),
        trackNumber = optInt(KEY_TRACK, Int.MIN_VALUE).takeIf { it > 0 },
        discNumber = optInt(KEY_DISC, Int.MIN_VALUE).takeIf { it > 0 },
    )
}

private fun decodeIdentityArray(array: JSONArray?): List<TrackMatchIdentity> {
    if (array == null) return emptyList()
    return buildList {
        repeat(array.length().coerceAtMost(MAX_REFERENCE_COUNT)) { index ->
            array.optJSONObject(index)?.toTrackMatchIdentity()?.let(::add)
        }
    }
}

private fun JSONObject.optNullableString(key: String): String? {
    return if (isNull(key)) null else optString(key).takeIf(String::isNotBlank)
}

private fun portableUserDataChecksum(root: JSONObject, keys: List<String>): String {
    val canonical = keys.joinToString("\n") { key ->
        val value = root.opt(key)?.toString().orEmpty()
        "$key:${value.length}:$value"
    }
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(canonical.toByteArray(StandardCharsets.UTF_8))
    return buildString(digest.size * 2) {
        digest.forEach { byte ->
            val value = byte.toInt() and 0xff
            append("0123456789abcdef"[value ushr 4])
            append("0123456789abcdef"[value and 0x0f])
        }
    }
}

private const val DEFAULT_FILE_NAME = "portable_user_data_v1.json"
private const val LEGACY_FORMAT_VERSION = 1
private const val FORMAT_VERSION = 2
private const val MAX_FILE_BYTES = 4L * 1024L * 1024L
private const val MAX_PLAYLIST_COUNT = 2_048
private const val MAX_REFERENCE_COUNT = 100_000
private const val MAX_RECENT_ITEMS = 24
private const val MAX_APP_VERSION_CHARS = 128
private const val MAX_PLAYLIST_NAME_CHARS = 4_096
private const val KEY_SCHEMA_VERSION = "schema_version"
private const val KEY_CREATED_AT_MS = "created_at_ms"
private const val KEY_APP_VERSION = "app_version"
private const val KEY_USER_DATA_REVISION = "user_data_revision"
private const val KEY_PLAYLISTS = "playlists"
private const val KEY_SMART_PLAYLISTS = "smart_playlists"
private const val KEY_FAVORITES = "favorites"
private const val KEY_SONG_COUNTS = "song_counts"
private const val KEY_RECENT_SONGS = "recent_songs"
private const val KEY_CHECKSUM = "checksum"
private const val KEY_ID = "id"
private const val KEY_NAME = "name"
private const val KEY_SONGS = "songs"
private const val KEY_SONG = "song"
private const val KEY_COUNT = "count"
private const val KEY_VERSION = "version"
private const val KEY_SIZE_BYTES = "size_bytes"
private const val KEY_DURATION_MS = "duration_ms"
private const val KEY_TITLE = "title"
private const val KEY_ARTIST = "artist"
private const val KEY_ALBUM = "album"
private const val KEY_ALBUM_ARTIST = "album_artist"
private const val KEY_FILE_NAME = "file_name"
private const val KEY_TRACK = "track"
private const val KEY_DISC = "disc"
private val CHECKSUM_KEYS_V1 = listOf(
    KEY_SCHEMA_VERSION,
    KEY_CREATED_AT_MS,
    KEY_APP_VERSION,
    KEY_PLAYLISTS,
    KEY_SMART_PLAYLISTS,
    KEY_FAVORITES,
    KEY_SONG_COUNTS,
    KEY_RECENT_SONGS,
)
private val CHECKSUM_KEYS_V2 = CHECKSUM_KEYS_V1 + KEY_USER_DATA_REVISION
