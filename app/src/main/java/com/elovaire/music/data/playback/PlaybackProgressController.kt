package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.core.AndroidAppClock

data class PlaybackProgressState(
    val positionMs: Long = 0L,
    val displayPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val isUserScrubbing: Boolean = false,
    val scrubPreviewPositionMs: Long? = null,
    val currentMediaId: Long? = null,
    val generation: Long = 0L,
)

internal data class ScrubCommitResult(
    val state: PlaybackProgressState,
    val seekPositionMs: Long? = null,
)

internal class PlaybackProgressController(
    private val elapsedRealtimeMs: () -> Long = AndroidAppClock::elapsedTimeMs,
) {
    private data class PendingSeek(val positionMs: Long, val requestedAtElapsedMs: Long)
    private var currentMediaId: Long? = null
    private var currentMediaItemOccurrence: Long = 0L
    private var generation: Long = 0L
    private var positionMs: Long = 0L
    private var durationMs: Long = 0L
    private var bufferedPositionMs: Long = 0L
    private var isPlaying: Boolean = false
    private var isUserScrubbing: Boolean = false
    private var scrubPreviewPositionMs: Long? = null
    private var scrubMediaId: Long? = null
    private var pendingSeek: PendingSeek? = null

    fun onPlayerSnapshot(
        mediaId: Long?,
        mediaItemOccurrence: Long = 0L,
        positionMs: Long,
        durationMs: Long,
        bufferedPositionMs: Long,
        isPlaying: Boolean,
    ): PlaybackProgressState {
        if (mediaId != currentMediaId || mediaItemOccurrence != currentMediaItemOccurrence) {
            generation += 1L
            currentMediaId = mediaId
            currentMediaItemOccurrence = mediaItemOccurrence
            clearScrubState()
            pendingSeek = null
        }
        this.positionMs = clampPosition(positionMs, durationMs)
        this.durationMs = durationMs.coerceAtLeast(0L)
        this.bufferedPositionMs = clampPosition(bufferedPositionMs, durationMs)
        this.isPlaying = isPlaying
        pendingSeek?.let { pending ->
            val expectedPositionMs = if (this.durationMs > 0L) {
                pending.positionMs.coerceIn(0L, this.durationMs)
            } else {
                pending.positionMs
            }
            val delta = if (expectedPositionMs >= this.positionMs) {
                expectedPositionMs - this.positionMs
            } else {
                this.positionMs - expectedPositionMs
            }
            if (delta <= SEEK_SETTLE_TOLERANCE_MS ||
                elapsedRealtimeMs() - pending.requestedAtElapsedMs >= SEEK_SETTLE_TIMEOUT_MS
            ) {
                pendingSeek = null
            }
        }
        return snapshot()
    }

    fun clear(): PlaybackProgressState {
        if (currentMediaId != null || positionMs != 0L || durationMs != 0L || bufferedPositionMs != 0L) {
            generation += 1L
        }
        currentMediaId = null
        currentMediaItemOccurrence = 0L
        positionMs = 0L
        durationMs = 0L
        bufferedPositionMs = 0L
        isPlaying = false
        clearScrubState()
        pendingSeek = null
        return snapshot()
    }

    fun beginScrub(): PlaybackProgressState {
        if (currentMediaId == null) return snapshot()
        isUserScrubbing = true
        scrubMediaId = currentMediaId
        scrubPreviewPositionMs = effectiveDisplayPositionMs()
        return snapshot()
    }

    fun updateScrubPosition(positionMs: Long): PlaybackProgressState {
        if (!isUserScrubbing || scrubMediaId != currentMediaId) {
            return snapshot()
        }
        scrubPreviewPositionMs = clampPosition(positionMs, durationMs)
        return snapshot()
    }

    fun finishScrub(positionMs: Long): ScrubCommitResult {
        if (!isUserScrubbing || scrubMediaId != currentMediaId || currentMediaId == null) {
            cancelScrub()
            return ScrubCommitResult(state = snapshot(), seekPositionMs = null)
        }
        val clamped = clampPosition(positionMs, durationMs)
        isUserScrubbing = false
        scrubPreviewPositionMs = null
        scrubMediaId = null
        pendingSeek = PendingSeek(clamped, elapsedRealtimeMs())
        return ScrubCommitResult(
            state = snapshot(),
            seekPositionMs = clamped,
        )
    }

    fun cancelScrub(): PlaybackProgressState {
        clearScrubState()
        return snapshot()
    }

    fun needsActivePolling(): Boolean = pendingSeek != null

    private fun clearScrubState() {
        isUserScrubbing = false
        scrubPreviewPositionMs = null
        scrubMediaId = null
    }

    private fun snapshot(): PlaybackProgressState {
        return PlaybackProgressState(
            positionMs = positionMs,
            displayPositionMs = effectiveDisplayPositionMs(),
            durationMs = durationMs,
            bufferedPositionMs = bufferedPositionMs,
            isPlaying = isPlaying,
            isUserScrubbing = isUserScrubbing,
            scrubPreviewPositionMs = scrubPreviewPositionMs,
            currentMediaId = currentMediaId,
            generation = generation,
        )
    }

    private fun effectiveDisplayPositionMs(): Long {
        return clampPosition(
            scrubPreviewPositionMs
                ?: pendingSeek?.positionMs
                ?: positionMs,
            durationMs,
        )
    }

    private fun clampPosition(
        positionMs: Long,
        durationMs: Long,
    ): Long {
        val safePosition = positionMs.coerceAtLeast(0L)
        val safeDuration = durationMs.coerceAtLeast(0L)
        return if (safeDuration > 0L) {
            safePosition.coerceIn(0L, safeDuration)
        } else {
            safePosition
        }
    }

    private companion object {
        const val SEEK_SETTLE_TOLERANCE_MS = 250L
        const val SEEK_SETTLE_TIMEOUT_MS = 5_000L
    }
}
