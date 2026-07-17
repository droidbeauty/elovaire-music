package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads

internal data class PersistedPlaybackSession(
    val queueSongIds: List<Long>,
    val currentSongId: Long?,
    val currentIndex: Int,
    val positionMs: Long,
    val repeatMode: PlaybackRepeatMode,
    val shuffleEnabled: Boolean,
    val sourcePlaylistId: Long?,
    val wasPlaying: Boolean,
    val savedAtWallTimeMs: Long,
)

internal class PlaybackSessionStore(
    context: Context,
    private val clock: AppClock = AndroidAppClock,
) {
    private val preferences = allowStrictModeDiskReads {
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }
    private var lastSavedSession: PersistedPlaybackSession? = null

    fun load(): PersistedPlaybackSession? {
        if (!isSupportedPlaybackSessionVersion(preferences.getInt(KEY_FORMAT_VERSION, LEGACY_FORMAT_VERSION))) {
            clear()
            return null
        }
        val savedAtMs = preferences.getLong(KEY_SAVED_AT, 0L)
        if (savedAtMs <= 0L || clock.wallTimeMs() - savedAtMs !in 0L..MAX_SESSION_AGE_MS) {
            clear()
            return null
        }
        val ids = preferences.getString(KEY_QUEUE_IDS, null)
            ?.split(',')
            ?.asSequence()
            ?.mapNotNull(String::toLongOrNull)
            ?.filter { it > 0L }
            ?.take(MAX_QUEUE_SIZE)
            ?.toList()
            .orEmpty()
        if (ids.isEmpty()) {
            clear()
            return null
        }
        return normalizePersistedPlaybackSession(
            PersistedPlaybackSession(
                queueSongIds = ids,
                currentSongId = preferences.getLong(KEY_CURRENT_SONG_ID, -1L).takeIf { it > 0L },
                currentIndex = preferences.getInt(KEY_CURRENT_INDEX, -1),
                positionMs = preferences.getLong(KEY_POSITION_MS, 0L),
                repeatMode = preferences.getString(KEY_REPEAT_MODE, null)
                    ?.let { stored -> PlaybackRepeatMode.entries.firstOrNull { it.name == stored } }
                    ?: PlaybackRepeatMode.Off,
                shuffleEnabled = preferences.getBoolean(KEY_SHUFFLE, false),
                sourcePlaylistId = preferences.getLong(KEY_SOURCE_PLAYLIST_ID, -1L).takeIf { it > 0L },
                wasPlaying = preferences.getBoolean(KEY_WAS_PLAYING, false),
                savedAtWallTimeMs = savedAtMs,
            ),
        ).also { lastSavedSession = it.withoutSavedAt() }
    }

    fun save(session: PersistedPlaybackSession) {
        val normalized = normalizePersistedPlaybackSession(session)
        if (normalized.queueSongIds.isEmpty()) {
            clear()
            return
        }
        val comparable = normalized.withoutSavedAt()
        if (lastSavedSession == comparable) return
        lastSavedSession = comparable
        preferences.edit()
            .putInt(KEY_FORMAT_VERSION, CURRENT_FORMAT_VERSION)
            .putString(KEY_QUEUE_IDS, normalized.queueSongIds.joinToString(","))
            .putLong(KEY_CURRENT_SONG_ID, normalized.currentSongId ?: -1L)
            .putInt(KEY_CURRENT_INDEX, normalized.currentIndex)
            .putLong(KEY_POSITION_MS, normalized.positionMs)
            .putString(KEY_REPEAT_MODE, normalized.repeatMode.name)
            .putBoolean(KEY_SHUFFLE, normalized.shuffleEnabled)
            .putLong(KEY_SOURCE_PLAYLIST_ID, normalized.sourcePlaylistId ?: -1L)
            .putBoolean(KEY_WAS_PLAYING, normalized.wasPlaying)
            .putLong(KEY_SAVED_AT, clock.wallTimeMs())
            .apply()
    }

    fun clear() {
        lastSavedSession = null
        if (preferences.all.isEmpty()) return
        preferences.edit().clear().apply()
    }

    private companion object {
        const val FILE_NAME = "playback_session"
        const val MAX_QUEUE_SIZE = 10_000
        const val MAX_SESSION_AGE_MS = 7L * 24L * 60L * 60L * 1_000L
        const val KEY_FORMAT_VERSION = "format_version"
        const val KEY_QUEUE_IDS = "queue_song_ids"
        const val KEY_CURRENT_SONG_ID = "current_song_id"
        const val KEY_CURRENT_INDEX = "current_index"
        const val KEY_POSITION_MS = "position_ms"
        const val KEY_REPEAT_MODE = "repeat_mode"
        const val KEY_SHUFFLE = "shuffle_enabled"
        const val KEY_SOURCE_PLAYLIST_ID = "source_playlist_id"
        const val KEY_WAS_PLAYING = "was_playing"
        const val KEY_SAVED_AT = "saved_at_wall_time_ms"
    }
}

internal const val LEGACY_FORMAT_VERSION = 0
internal const val CURRENT_FORMAT_VERSION = 1

internal fun isSupportedPlaybackSessionVersion(version: Int): Boolean {
    return version in LEGACY_FORMAT_VERSION..CURRENT_FORMAT_VERSION
}

private fun PersistedPlaybackSession.withoutSavedAt(): PersistedPlaybackSession = copy(savedAtWallTimeMs = 0L)

internal fun normalizePersistedPlaybackSession(session: PersistedPlaybackSession): PersistedPlaybackSession {
    val ids = session.queueSongIds.asSequence().filter { it > 0L }.take(10_000).toList()
    if (ids.isEmpty()) return session.copy(queueSongIds = emptyList(), currentSongId = null, currentIndex = -1, positionMs = 0L)
    val resolvedIndex = session.currentIndex
        .takeIf { it in ids.indices && ids[it] == session.currentSongId }
        ?: session.currentSongId?.let(ids::indexOf)?.takeIf { it >= 0 }
        ?: session.currentIndex.coerceIn(ids.indices)
    return session.copy(
        queueSongIds = ids,
        currentSongId = ids[resolvedIndex],
        currentIndex = resolvedIndex,
        positionMs = session.positionMs.coerceAtLeast(0L),
        sourcePlaylistId = session.sourcePlaylistId?.takeIf { it > 0L },
    )
}
