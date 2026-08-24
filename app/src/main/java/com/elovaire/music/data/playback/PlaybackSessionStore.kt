package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import elovaire.music.droidbeauty.app.data.library.isValidMediaId

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
    private val structurePreferences = allowStrictModeDiskReads {
        context.applicationContext.getSharedPreferences(STRUCTURE_FILE_NAME, Context.MODE_PRIVATE)
    }
    private val recoveryPreferences = allowStrictModeDiskReads {
        context.applicationContext.getSharedPreferences(RECOVERY_FILE_NAME, Context.MODE_PRIVATE)
    }
    private val legacyPreferences = allowStrictModeDiskReads {
        context.applicationContext.getSharedPreferences(LEGACY_FILE_NAME, Context.MODE_PRIVATE)
    }
    private var lastSavedSession: PersistedPlaybackSession? = null
    private var legacyPreferencesCleared = false
    private var clearStateKnown = false

    fun load(): PersistedPlaybackSession? {
        return if (structurePreferences.getInt(KEY_FORMAT_VERSION, LEGACY_FORMAT_VERSION) == CURRENT_FORMAT_VERSION) {
            load(
                structurePreferences,
                recoveryPreferences,
            )
        } else {
            val version = legacyPreferences.getInt(KEY_FORMAT_VERSION, LEGACY_FORMAT_VERSION)
            if (!isSupportedPlaybackSessionVersion(version)) {
                clear()
                null
            } else {
                load(legacyPreferences, legacyPreferences)
            }
        }
    }

    private fun load(
        structure: android.content.SharedPreferences,
        recovery: android.content.SharedPreferences,
    ): PersistedPlaybackSession? {
        val savedAtMs = recovery.getLong(KEY_SAVED_AT, 0L)
        if (!isPlaybackSessionFresh(clock.wallTimeMs(), savedAtMs)) {
            clear()
            return null
        }
        val ids = structure.getString(KEY_QUEUE_IDS, null)
            ?.split(',')
            ?.asSequence()
            ?.mapNotNull(String::toLongOrNull)
            ?.filter(::isValidMediaId)
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
                currentSongId = recovery.getLong(KEY_CURRENT_SONG_ID, 0L).takeIf(::isValidMediaId),
                currentIndex = recovery.getInt(KEY_CURRENT_INDEX, -1),
                positionMs = recovery.getLong(KEY_POSITION_MS, 0L),
                repeatMode = structure.getString(KEY_REPEAT_MODE, null)
                    ?.let { stored -> PlaybackRepeatMode.entries.firstOrNull { it.name == stored } }
                    ?: PlaybackRepeatMode.Off,
                shuffleEnabled = structure.getBoolean(KEY_SHUFFLE, false),
                sourcePlaylistId = structure.getLong(KEY_SOURCE_PLAYLIST_ID, -1L).takeIf { it > 0L },
                wasPlaying = recovery.getBoolean(KEY_WAS_PLAYING, false),
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
        val plan = playbackSessionSavePlan(lastSavedSession, comparable)
        if (plan == PlaybackSessionSavePlan.None) return
        lastSavedSession = comparable
        clearStateKnown = false
        if (plan.saveStructure) {
            structurePreferences.edit()
                .putInt(KEY_FORMAT_VERSION, CURRENT_FORMAT_VERSION)
                .putString(KEY_QUEUE_IDS, normalized.queueSongIds.joinToString(","))
                .putString(KEY_REPEAT_MODE, normalized.repeatMode.name)
                .putBoolean(KEY_SHUFFLE, normalized.shuffleEnabled)
                .putLong(KEY_SOURCE_PLAYLIST_ID, normalized.sourcePlaylistId ?: -1L)
                .apply()
        }
        if (plan.saveRecovery) {
            recoveryPreferences.edit()
                .putLong(KEY_CURRENT_SONG_ID, normalized.currentSongId ?: -1L)
                .putInt(KEY_CURRENT_INDEX, normalized.currentIndex)
                .putLong(KEY_POSITION_MS, normalized.positionMs)
                .putBoolean(KEY_WAS_PLAYING, normalized.wasPlaying)
                .putLong(KEY_SAVED_AT, clock.wallTimeMs())
                .apply()
        }
        clearLegacyPreferencesIfNeeded()
    }

    fun clear() {
        lastSavedSession = null
        if (clearStateKnown) return
        listOf(structurePreferences, recoveryPreferences)
            .filter { it.all.isNotEmpty() }
            .forEach { it.edit().clear().apply() }
        clearLegacyPreferencesIfNeeded()
        clearStateKnown = true
    }

    private fun clearLegacyPreferencesIfNeeded() {
        if (legacyPreferencesCleared) return
        legacyPreferencesCleared = true
        if (legacyPreferences.all.isNotEmpty()) legacyPreferences.edit().clear().apply()
    }

    private companion object {
        const val LEGACY_FILE_NAME = "playback_session"
        const val STRUCTURE_FILE_NAME = "playback_session_structure"
        const val RECOVERY_FILE_NAME = "playback_session_recovery"
        const val MAX_QUEUE_SIZE = 10_000
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

private const val MAX_PLAYBACK_SESSION_AGE_MS = 7L * 24L * 60L * 60L * 1_000L

internal const val LEGACY_FORMAT_VERSION = 0
internal const val CURRENT_FORMAT_VERSION = 2

internal fun isPlaybackSessionFresh(
    nowWallTimeMs: Long,
    savedAtWallTimeMs: Long,
): Boolean {
    if (savedAtWallTimeMs <= 0L) return false
    return if (nowWallTimeMs >= savedAtWallTimeMs) {
        nowWallTimeMs - savedAtWallTimeMs <= MAX_PLAYBACK_SESSION_AGE_MS
    } else {
        savedAtWallTimeMs - nowWallTimeMs <= MAX_PLAYBACK_SESSION_AGE_MS
    }
}

internal fun isSupportedPlaybackSessionVersion(version: Int): Boolean {
    return version in LEGACY_FORMAT_VERSION..CURRENT_FORMAT_VERSION
}

private fun PersistedPlaybackSession.withoutSavedAt(): PersistedPlaybackSession = copy(savedAtWallTimeMs = 0L)

internal enum class PlaybackSessionSavePlan(
    val saveStructure: Boolean,
    val saveRecovery: Boolean,
) {
    None(false, false),
    Recovery(false, true),
    StructureAndRecovery(true, true),
}

internal fun playbackSessionSavePlan(
    previous: PersistedPlaybackSession?,
    next: PersistedPlaybackSession,
): PlaybackSessionSavePlan {
    if (previous == next) return PlaybackSessionSavePlan.None
    if (previous == null || previous.structure() != next.structure()) {
        return PlaybackSessionSavePlan.StructureAndRecovery
    }
    return PlaybackSessionSavePlan.Recovery
}

private data class PlaybackSessionStructure(
    val queueSongIds: List<Long>,
    val repeatMode: PlaybackRepeatMode,
    val shuffleEnabled: Boolean,
    val sourcePlaylistId: Long?,
)

private fun PersistedPlaybackSession.structure() = PlaybackSessionStructure(
    queueSongIds = queueSongIds,
    repeatMode = repeatMode,
    shuffleEnabled = shuffleEnabled,
    sourcePlaylistId = sourcePlaylistId,
)

internal fun normalizePersistedPlaybackSession(session: PersistedPlaybackSession): PersistedPlaybackSession {
    val ids = session.queueSongIds.asSequence().filter(::isValidMediaId).take(10_000).toList()
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
