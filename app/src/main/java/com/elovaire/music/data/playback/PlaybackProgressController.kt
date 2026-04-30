package elovaire.music.app.data.playback

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

internal class PlaybackProgressController {
    private var currentMediaId: Long? = null
    private var generation: Long = 0L
    private var positionMs: Long = 0L
    private var durationMs: Long = 0L
    private var bufferedPositionMs: Long = 0L
    private var isPlaying: Boolean = false
    private var isUserScrubbing: Boolean = false
    private var scrubPreviewPositionMs: Long? = null
    private var scrubMediaId: Long? = null
    private var pendingSeekPositionMs: Long? = null

    fun onPlayerSnapshot(
        mediaId: Long?,
        positionMs: Long,
        durationMs: Long,
        bufferedPositionMs: Long,
        isPlaying: Boolean,
    ): PlaybackProgressState {
        if (mediaId != currentMediaId) {
            generation += 1L
            currentMediaId = mediaId
            clearScrubState()
            pendingSeekPositionMs = null
        }
        this.positionMs = clampPosition(positionMs, durationMs)
        this.durationMs = durationMs.coerceAtLeast(0L)
        this.bufferedPositionMs = clampPosition(bufferedPositionMs, durationMs)
        this.isPlaying = isPlaying
        if (pendingSeekPositionMs != null && mediaId == currentMediaId) {
            val delta = kotlin.math.abs((pendingSeekPositionMs ?: 0L) - this.positionMs)
            if (delta <= SEEK_SETTLE_TOLERANCE_MS || this.positionMs == 0L || this.durationMs == 0L) {
                pendingSeekPositionMs = null
            }
        }
        return snapshot()
    }

    fun clear(): PlaybackProgressState {
        if (currentMediaId != null || positionMs != 0L || durationMs != 0L || bufferedPositionMs != 0L) {
            generation += 1L
        }
        currentMediaId = null
        positionMs = 0L
        durationMs = 0L
        bufferedPositionMs = 0L
        isPlaying = false
        clearScrubState()
        pendingSeekPositionMs = null
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
        pendingSeekPositionMs = clamped
        return ScrubCommitResult(
            state = snapshot(),
            seekPositionMs = clamped,
        )
    }

    fun cancelScrub(): PlaybackProgressState {
        clearScrubState()
        return snapshot()
    }

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
                ?: pendingSeekPositionMs
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
    }
}
