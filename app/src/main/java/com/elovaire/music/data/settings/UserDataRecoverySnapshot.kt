package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import android.util.AtomicFile
import android.util.Log
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playlists.deserializePlaylists
import elovaire.music.droidbeauty.app.data.playlists.serializePlaylists
import elovaire.music.droidbeauty.app.data.smartplaylists.deserializeSmartPlaylists
import elovaire.music.droidbeauty.app.data.smartplaylists.serializeSmartPlaylists
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.json.JSONObject

/**
 * A bounded, app-private recovery copy of user-owned state.
 *
 * This is not a second live store: Room remains authoritative whenever it can be read. The
 * copy is used only after a completed Room migration and a later Room read fail, so rebuildable
 * library/index data can never overwrite healthy or intentionally empty user data.
 */
@Suppress("TooGenericExceptionCaught")
internal class UserDataRecoverySnapshot(
    context: Context,
    private val clock: AppClock = AndroidAppClock,
    fileName: String = DEFAULT_FILE_NAME,
) {
    private val atomicFile by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        allowStrictModeDiskReads {
            AtomicFile(context.applicationContext.filesDir.resolve(fileName))
        }
    }
    private val lock = Any()

    fun read(): UserDataSnapshot? = synchronized(lock) {
        val file = atomicFile.baseFile
        if (!file.isFile || file.length() !in 1L..MAX_FILE_BYTES) return@synchronized null
        return@synchronized try {
            val root = atomicFile.openRead().use { input ->
                JSONObject(input.readBytes().toString(StandardCharsets.UTF_8))
            }
            if (root.optInt(KEY_VERSION, 0) != FORMAT_VERSION) return@synchronized null
            if (root.optString(KEY_CHECKSUM) != checksum(root)) return@synchronized null
            decode(root)
        } catch (failure: Exception) {
            Log.w(TAG, "Ignoring invalid user-data recovery snapshot.", failure)
            null
        }
    }

    fun write(snapshot: UserDataSnapshot) = synchronized(lock) {
        val root = encode(snapshot)
        val serialized = root.toString()
        val bytes = serialized.toByteArray(StandardCharsets.UTF_8)
        require(bytes.size <= MAX_FILE_BYTES) { "User-data recovery snapshot is too large." }
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

    private fun encode(snapshot: UserDataSnapshot): JSONObject {
        val root = JSONObject()
            .put(KEY_VERSION, FORMAT_VERSION)
            .put(KEY_PLAYLISTS, serializePlaylists(snapshot.playlists))
            .put(KEY_SMART_PLAYLISTS, serializeSmartPlaylists(snapshot.smartPlaylists))
            .put(KEY_FAVORITES, snapshot.favoriteSongIds.joinToString(","))
            .put(KEY_SONG_COUNTS, PreferenceCollectionCodec.serializePlayCounts(snapshot.songPlayCounts))
            .put(KEY_ALBUM_COUNTS, PreferenceCollectionCodec.serializePlayCounts(snapshot.albumPlayCounts))
            .put(KEY_RECENT_SONGS, snapshot.recentSongIds.joinToString(","))
            .put(KEY_RECENT_ALBUMS, snapshot.recentAlbumIds.joinToString(","))
            .put(KEY_COLLECTION_KIND, snapshot.lastPlayedCollectionKind?.name.orEmpty())
            .put(KEY_COLLECTION_ID, snapshot.lastPlayedCollectionId ?: JSONObject.NULL)
            .put(
                KEY_SEARCH_HISTORY,
                snapshot.searchHistory.joinToString(PreferenceCollectionCodec.RECORD_SEPARATOR) {
                    PreferenceCollectionCodec.serializeSearchHistory(it)
                },
            )
            .put(KEY_SAVED_AT, clock.wallTimeMs())
        return root.put(KEY_CHECKSUM, checksum(root))
    }

    private fun decode(root: JSONObject): UserDataSnapshot? {
        val playlists = deserializePlaylists(root.optString(KEY_PLAYLISTS))
            .filterNot(Playlist::isSystem)
            .distinctBy(Playlist::id)
        val smartPlaylists = deserializeSmartPlaylists(root.optString(KEY_SMART_PLAYLISTS))
            .distinctBy { it.id }
        val collectionKind = root.optString(KEY_COLLECTION_KIND)
            .takeIf(String::isNotBlank)
            ?.let { value -> PlaybackCollectionKind.entries.firstOrNull { it.name == value } }
        val collectionId = if (root.isNull(KEY_COLLECTION_ID)) {
            null
        } else {
            root.optLong(KEY_COLLECTION_ID, Long.MIN_VALUE).takeIf { it != Long.MIN_VALUE }
        }
        return UserDataSnapshot(
            playlists = playlists,
            smartPlaylists = smartPlaylists,
            favoriteSongIds = parseIds(root.optString(KEY_FAVORITES)),
            songPlayCounts = PreferenceCollectionCodec.deserializePlayCounts(root.optString(KEY_SONG_COUNTS)),
            albumPlayCounts = PreferenceCollectionCodec.deserializePlayCounts(root.optString(KEY_ALBUM_COUNTS)),
            recentSongIds = parseIds(root.optString(KEY_RECENT_SONGS)).take(MAX_RECENT_ITEMS),
            recentAlbumIds = parseIds(root.optString(KEY_RECENT_ALBUMS)).take(MAX_RECENT_ITEMS),
            lastPlayedCollectionKind = collectionKind,
            lastPlayedCollectionId = collectionId,
            searchHistory = root.optString(KEY_SEARCH_HISTORY)
                .split(PreferenceCollectionCodec.RECORD_SEPARATOR)
                .mapNotNull(PreferenceCollectionCodec::deserializeSearchHistory)
                .distinctBy(SearchHistoryEntry::key)
                .take(MAX_SEARCH_HISTORY_ITEMS),
        )
    }

    private fun checksum(root: JSONObject): String {
        val canonical = RECOVERY_KEYS.joinToString("\n") { key ->
            val value = root.opt(key)?.toString().orEmpty()
            "$key:${value.length}:$value"
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }

    private companion object {
        const val TAG = "UserDataRecovery"
        const val DEFAULT_FILE_NAME = "user_data_recovery_v1.json"
        const val FORMAT_VERSION = 1
        const val MAX_FILE_BYTES = 4L * 1024L * 1024L
        const val MAX_RECENT_ITEMS = 24
        const val MAX_SEARCH_HISTORY_ITEMS = 6
        const val KEY_VERSION = "version"
        const val KEY_PLAYLISTS = "playlists"
        const val KEY_SMART_PLAYLISTS = "smart_playlists"
        const val KEY_FAVORITES = "favorites"
        const val KEY_SONG_COUNTS = "song_counts"
        const val KEY_ALBUM_COUNTS = "album_counts"
        const val KEY_RECENT_SONGS = "recent_songs"
        const val KEY_RECENT_ALBUMS = "recent_albums"
        const val KEY_COLLECTION_KIND = "collection_kind"
        const val KEY_COLLECTION_ID = "collection_id"
        const val KEY_SEARCH_HISTORY = "search_history"
        const val KEY_SAVED_AT = "saved_at"
        const val KEY_CHECKSUM = "checksum"
        val RECOVERY_KEYS = listOf(
            KEY_VERSION,
            KEY_PLAYLISTS,
            KEY_SMART_PLAYLISTS,
            KEY_FAVORITES,
            KEY_SONG_COUNTS,
            KEY_ALBUM_COUNTS,
            KEY_RECENT_SONGS,
            KEY_RECENT_ALBUMS,
            KEY_COLLECTION_KIND,
            KEY_COLLECTION_ID,
            KEY_SEARCH_HISTORY,
            KEY_SAVED_AT,
        )
    }
}

private fun parseIds(value: String): List<Long> {
    if (value.isBlank()) return emptyList()
    return value.split(',')
        .asSequence()
        .mapNotNull(String::toLongOrNull)
        .filter { it > 0L }
        .take(MAX_RECOVERY_ID_COUNT)
        .toList()
}

private const val MAX_RECOVERY_ID_COUNT = 100_000
