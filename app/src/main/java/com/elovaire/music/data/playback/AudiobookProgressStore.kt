package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

data class AudiobookProgress(
    val songId: Long,
    val positionMs: Long,
    val completed: Boolean,
    val updatedAtMs: Long,
    val bookElapsedMs: Long? = null,
    val bookDurationMs: Long? = null,
)

/** App-owned audiobook checkpoints. Song identity is the existing stable media identity. */
internal class AudiobookProgressStore(context: Context) {
    private val preferences = allowStrictModeDiskReads {
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun load(bookKey: String, nowMs: Long = System.currentTimeMillis()): AudiobookProgress? {
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
            val position = positionMs.coerceAtLeast(0L)
            val normalizedBookDuration = bookDurationMs?.takeIf { it > 0L }
            val normalizedBookElapsed = bookElapsedMs
                ?.coerceAtLeast(0L)
                ?.let { elapsed -> normalizedBookDuration?.let(elapsed::coerceAtMost) ?: elapsed }
            val completionDuration = normalizedBookDuration ?: durationMs
            val completionPosition = normalizedBookElapsed ?: position.coerceAtMost(durationMs.coerceAtLeast(0L))
            val completed = completionDuration > 0L &&
                completionDuration - completionPosition <= COMPLETION_THRESHOLD_MS
            val json = JSONObject()
                .put("positionMs", position)
                .put("songId", songId)
                .put("completed", completed)
                .put("updatedAtMs", nowMs.coerceAtLeast(0L))
            normalizedBookElapsed?.let { json.put("bookElapsedMs", it) }
            normalizedBookDuration?.let { json.put("bookDurationMs", it) }
            preferences.edit().putString(key(bookKey), json.toString()).apply()
        } catch (_: RuntimeException) {
            // A failed checkpoint must never interrupt playback.
        }
    }

    @Synchronized
    fun remapSongIds(replacements: Map<Long, Long>) {
        if (replacements.isEmpty()) return
        val editor = preferences.edit()
        var changed = false
        preferences.all.forEach { (preferenceKey, rawValue) ->
            if (!preferenceKey.startsWith("book_") || rawValue !is String) return@forEach
            val json = try {
                JSONObject(rawValue)
            } catch (_: JSONException) {
                null
            } catch (_: RuntimeException) {
                null
            } ?: return@forEach
            val currentSongId = json.optLong("songId")
            val replacementSongId = replacements[currentSongId]?.takeIf { it > 0L } ?: return@forEach
            editor.putString(preferenceKey, json.put("songId", replacementSongId).toString())
            changed = true
        }
        if (changed) editor.apply()
    }

    @Suppress("TooGenericExceptionCaught")
    fun loadPlaybackSpeed(): Float {
        return try {
            preferences.getFloat(SPEED_KEY, 1f).coerceIn(0.5f, 2.5f)
        } catch (_: RuntimeException) {
            1f
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun savePlaybackSpeed(speed: Float) {
        try {
            preferences.edit().putFloat(SPEED_KEY, speed.coerceIn(0.5f, 2.5f)).apply()
        } catch (_: RuntimeException) {
            // A failed preference write must never interrupt playback.
        }
    }

    private fun key(bookKey: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(bookKey.toByteArray(StandardCharsets.UTF_8))
        return "book_" + digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    private companion object {
        const val FILE_NAME = "audiobook_progress"
        const val SPEED_KEY = "playback_speed"
        const val COMPLETION_THRESHOLD_MS = 10_000L
        const val FUTURE_TIMESTAMP_SKEW_MS = 5 * 60 * 1_000L
    }
}
