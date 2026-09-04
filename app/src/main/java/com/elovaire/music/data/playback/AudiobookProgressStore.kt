package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

data class AudiobookProgress(
    val songId: Long,
    val positionMs: Long,
    val completed: Boolean,
    val updatedAtMs: Long,
)

/** App-owned audiobook checkpoints. Song identity is the existing stable media identity. */
internal class AudiobookProgressStore(context: Context) {
    private val preferences = allowStrictModeDiskReads {
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun load(bookKey: String): AudiobookProgress? {
        if (bookKey.isBlank()) return null
        val raw = preferences.getString(key(bookKey), null) ?: return null
        return runCatching {
            JSONObject(raw).let { json ->
                AudiobookProgress(
                    songId = json.optLong("songId").takeIf { it > 0L } ?: return@let null,
                    positionMs = json.optLong("positionMs").coerceAtLeast(0L),
                    completed = json.optBoolean("completed"),
                    updatedAtMs = json.optLong("updatedAtMs").coerceAtLeast(0L),
                )
            }
        }.getOrNull()
    }

    @Synchronized
    fun save(bookKey: String, songId: Long, positionMs: Long, durationMs: Long, nowMs: Long) {
        if (bookKey.isBlank() || songId <= 0L) return
        val position = positionMs.coerceAtLeast(0L)
        val completed = durationMs > 0L && durationMs - position <= COMPLETION_THRESHOLD_MS
        val json = JSONObject()
            .put("positionMs", position)
            .put("songId", songId)
            .put("completed", completed)
            .put("updatedAtMs", nowMs)
        preferences.edit().putString(key(bookKey), json.toString()).apply()
    }

    @Synchronized
    fun remapSongIds(replacements: Map<Long, Long>) {
        if (replacements.isEmpty()) return
        val editor = preferences.edit()
        var changed = false
        preferences.all.forEach { (preferenceKey, rawValue) ->
            if (!preferenceKey.startsWith("book_") || rawValue !is String) return@forEach
            val json = runCatching { JSONObject(rawValue) }.getOrNull() ?: return@forEach
            val currentSongId = json.optLong("songId")
            val replacementSongId = replacements[currentSongId] ?: return@forEach
            editor.putString(preferenceKey, json.put("songId", replacementSongId).toString())
            changed = true
        }
        if (changed) editor.apply()
    }

    fun loadPlaybackSpeed(): Float {
        return preferences.getFloat(SPEED_KEY, 1f).coerceIn(0.5f, 2.5f)
    }

    fun savePlaybackSpeed(speed: Float) {
        preferences.edit().putFloat(SPEED_KEY, speed.coerceIn(0.5f, 2.5f)).apply()
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
    }
}
