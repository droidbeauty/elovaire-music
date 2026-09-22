package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

data class AudiobookProgress(
    val songId: Long,
    val positionMs: Long,
    val completed: Boolean,
    val updatedAtMs: Long,
    val bookElapsedMs: Long? = null,
    val bookDurationMs: Long? = null,
)

internal interface AudiobookProgressRepository {
    fun load(bookKey: String): AudiobookProgress?

    fun save(
        bookKey: String,
        progress: AudiobookProgress,
        force: Boolean = false,
    )

    fun remapBookKey(oldBookKey: String, newBookKey: String): Deferred<Unit>
}

/** App-owned audiobook checkpoints. Song identity is the existing stable media identity. */
internal class AudiobookProgressStore(
    context: Context,
    private val clock: AppClock = AndroidAppClock,
) : AudiobookProgressRepository {
    private val preferences = allowStrictModeDiskReads {
        context.applicationContext.getSharedPreferences(LEGACY_FILE_NAME, Context.MODE_PRIVATE)
    }

    @Synchronized
    override fun load(bookKey: String): AudiobookProgress? = load(bookKey, clock.wallTimeMs())

    @Synchronized
    fun load(bookKey: String, nowMs: Long): AudiobookProgress? {
        if (bookKey.isBlank()) return null
        val raw = preferences.getString(key(bookKey), null) ?: return null
        return try {
            JSONObject(raw).let { json ->
                val bookDurationMs = json.optLong("bookDurationMs")
                    .takeIf { it > 0L }
                val positionMs = json.optLong("positionMs")
                    .coerceAtLeast(0L)
                    .let { position -> bookDurationMs?.let(position::coerceAtMost) ?: position }
                val updatedAtMs = json.optLong("updatedAtMs").takeIf { it > 0L } ?: return@let null
                if (updatedAtMs > nowMs + FUTURE_TIMESTAMP_SKEW_MS) return@let null
                AudiobookProgress(
                    songId = json.optLong("songId").takeIf { it > 0L } ?: return@let null,
                    positionMs = positionMs,
                    completed = json.optBoolean("completed"),
                    updatedAtMs = updatedAtMs,
                    bookElapsedMs = json.optLong("bookElapsedMs")
                        .takeIf { it >= 0L }
                        ?.let { elapsed -> bookDurationMs?.let(elapsed::coerceAtMost) ?: elapsed },
                    bookDurationMs = bookDurationMs,
                )
            }
        } catch (_: JSONException) {
            null
        } catch (_: RuntimeException) {
            null
        }
    }

    /** Reads legacy rows for the one-time Room migration. Keys are already hashed. */
    @Synchronized
    fun readLegacyRows(): Map<String, AudiobookProgress> = buildMap {
        preferences.all.forEach { (preferenceKey, rawValue) ->
            if (!preferenceKey.startsWith(BOOK_KEY_PREFIX) || rawValue !is String) return@forEach
            val progress = runCatching { decodeProgress(rawValue, clock.wallTimeMs()) }.getOrNull() ?: return@forEach
            put(preferenceKey.removePrefix(BOOK_KEY_PREFIX), progress)
        }
    }

    @Synchronized
    fun clearLegacyRows() {
        val editor = preferences.edit()
        preferences.all.keys.filter { it.startsWith(BOOK_KEY_PREFIX) }.forEach(editor::remove)
        editor.apply()
    }

    @Synchronized
    @Suppress("TooGenericExceptionCaught")
    fun save(
        bookKey: String,
        songId: Long,
        positionMs: Long,
        durationMs: Long,
        nowMs: Long,
        bookElapsedMs: Long? = null,
        bookDurationMs: Long? = null,
    ) {
        try {
            if (bookKey.isBlank() || songId <= 0L) return
            val progress = snapshot(
                songId = songId,
                positionMs = positionMs,
                durationMs = durationMs,
                nowMs = nowMs,
                bookElapsedMs = bookElapsedMs,
                bookDurationMs = bookDurationMs,
            )
            val json = JSONObject()
                .put("positionMs", progress.positionMs)
                .put("songId", progress.songId)
                .put("completed", progress.completed)
                .put("updatedAtMs", progress.updatedAtMs)
            progress.bookElapsedMs?.let { json.put("bookElapsedMs", it) }
            progress.bookDurationMs?.let { json.put("bookDurationMs", it) }
            preferences.edit().putString(key(bookKey), json.toString()).apply()
        } catch (_: RuntimeException) {
            // A failed checkpoint must never interrupt playback.
        }
    }

    override fun save(bookKey: String, progress: AudiobookProgress, force: Boolean) {
        if (bookKey.isBlank() || progress.songId <= 0L) return
        val json = JSONObject()
            .put("positionMs", progress.positionMs.coerceAtLeast(0L))
            .put("songId", progress.songId)
            .put("completed", progress.completed)
            .put("updatedAtMs", progress.updatedAtMs.coerceAtLeast(0L))
        progress.bookElapsedMs?.let { json.put("bookElapsedMs", it.coerceAtLeast(0L)) }
        progress.bookDurationMs?.takeIf { it > 0L }?.let { json.put("bookDurationMs", it) }
        preferences.edit().putString(key(bookKey), json.toString()).apply()
    }

    /** Moves a checkpoint when a verified tag edit changes the catalog identity of a book. */
    @Synchronized
    override fun remapBookKey(oldBookKey: String, newBookKey: String): Deferred<Unit> {
        if (oldBookKey.isBlank() || newBookKey.isBlank() || oldBookKey == newBookKey) {
            return CompletableDeferred(Unit)
        }
        val oldPreferenceKey = key(oldBookKey)
        val newPreferenceKey = key(newBookKey)
        val oldRaw = preferences.getString(oldPreferenceKey, null) ?: return CompletableDeferred(Unit)
        val newRaw = preferences.getString(newPreferenceKey, null)
        val selected = selectNewestCheckpoint(oldRaw, newRaw) ?: return CompletableDeferred(Unit)
        if (!preferences.edit().putString(newPreferenceKey, selected).commit()) return CompletableDeferred(Unit)
        preferences.edit().remove(oldPreferenceKey).commit()
        return CompletableDeferred(Unit)
    }

    private fun key(bookKey: String): String = BOOK_KEY_PREFIX + storageKey(bookKey)

    private fun decodeProgress(raw: String, nowMs: Long): AudiobookProgress? {
        return JSONObject(raw).let { json ->
            val bookDurationMs = json.optLong("bookDurationMs").takeIf { it > 0L }
            val positionMs = json.optLong("positionMs").coerceAtLeast(0L)
                .let { position -> bookDurationMs?.let(position::coerceAtMost) ?: position }
            val updatedAtMs = json.optLong("updatedAtMs").takeIf { it > 0L } ?: return@let null
            if (updatedAtMs > nowMs + FUTURE_TIMESTAMP_SKEW_MS) return@let null
            AudiobookProgress(
                songId = json.optLong("songId").takeIf { it > 0L } ?: return@let null,
                positionMs = positionMs,
                completed = json.optBoolean("completed"),
                updatedAtMs = updatedAtMs,
                bookElapsedMs = json.optLong("bookElapsedMs").takeIf { it >= 0L }
                    ?.let { elapsed -> bookDurationMs?.let(elapsed::coerceAtMost) ?: elapsed },
                bookDurationMs = bookDurationMs,
            )
        }
    }

    companion object {
        fun snapshot(
            songId: Long,
            positionMs: Long,
            durationMs: Long,
            nowMs: Long,
            bookElapsedMs: Long? = null,
            bookDurationMs: Long? = null,
        ): AudiobookProgress {
            val position = positionMs.coerceAtLeast(0L)
            val normalizedBookDuration = bookDurationMs?.takeIf { it > 0L }
            val normalizedBookElapsed = bookElapsedMs
                ?.coerceAtLeast(0L)
                ?.let { elapsed -> normalizedBookDuration?.let(elapsed::coerceAtMost) ?: elapsed }
            val completionDuration = normalizedBookDuration ?: durationMs
            val completionPosition = normalizedBookElapsed ?: position.coerceAtMost(durationMs.coerceAtLeast(0L))
            val completed = completionDuration > 0L &&
                completionDuration - completionPosition <= COMPLETION_THRESHOLD_MS
            return AudiobookProgress(
                songId = songId,
                positionMs = position,
                completed = completed,
                updatedAtMs = nowMs.coerceAtLeast(0L),
                bookElapsedMs = normalizedBookElapsed,
                bookDurationMs = normalizedBookDuration,
            )
        }

        fun storageKey(bookKey: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            .digest(bookKey.toByteArray(StandardCharsets.UTF_8))
            return digest.joinToString("") { byte -> "%02x".format(byte) }
        }

        const val LEGACY_FILE_NAME = "audiobook_progress"
        const val LEGACY_SPEED_KEY = "playback_speed"
        const val COMPLETION_THRESHOLD_MS = 10_000L
        const val BOOK_KEY_PREFIX = "book_"
        const val FUTURE_TIMESTAMP_SKEW_MS = 5 * 60 * 1_000L
    }

    private fun selectNewestCheckpoint(first: String, second: String?): String? {
        val firstTimestamp = checkpointTimestamp(first) ?: return second?.takeIf { checkpointTimestamp(it) != null }
        val secondTimestamp = second?.let(::checkpointTimestamp)
        return if (secondTimestamp != null && secondTimestamp >= firstTimestamp) second else first
    }

    private fun checkpointTimestamp(raw: String): Long? {
        return try {
            JSONObject(raw).optLong("updatedAtMs").takeIf { it > 0L }
        } catch (_: JSONException) {
            null
        } catch (_: RuntimeException) {
            null
        }
    }

}
