package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class PlaybackProgressControllerTest {
    @Test
    fun replacingAPendingSeekStartsANewSettlementDeadline() {
        var nowMs = 0L
        val controller = PlaybackProgressController { nowMs }
        fun snapshot() = controller.onPlayerSnapshot(
            mediaId = 1L, positionMs = 0L, durationMs = 60_000L,
            bufferedPositionMs = 0L, isPlaying = false,
        )
        snapshot()
        controller.beginScrub()
        controller.finishScrub(45_000L)
        nowMs = 4_000L
        controller.beginScrub()
        controller.finishScrub(20_000L)

        nowMs = 5_000L
        assertEquals(20_000L, snapshot().displayPositionMs)
        assertTrue(controller.needsActivePolling())
        nowMs = 9_000L
        assertEquals(0L, snapshot().displayPositionMs)
        assertFalse(controller.needsActivePolling())
    }

    @Test
    fun pendingSeekSurvivesUnknownSnapshotAndSettlesAtClampedDuration() {
        val controller = PlaybackProgressController { 0L }
        controller.onPlayerSnapshot(
            mediaId = 1L,
            positionMs = 0L,
            durationMs = 60_000L,
            bufferedPositionMs = 0L,
            isPlaying = true,
        )
        controller.beginScrub()
        val commit = controller.finishScrub(50_000L)

        assertEquals(50_000L, commit.seekPositionMs)
        val pending = controller.onPlayerSnapshot(
            mediaId = 1L,
            positionMs = 0L,
            durationMs = 0L,
            bufferedPositionMs = 0L,
            isPlaying = true,
        )
        assertEquals(50_000L, pending.displayPositionMs)
        assertTrue(controller.needsActivePolling())

        val settled = controller.onPlayerSnapshot(
            mediaId = 1L,
            positionMs = 30_000L,
            durationMs = 30_000L,
            bufferedPositionMs = 30_000L,
            isPlaying = true,
        )
        assertEquals(30_000L, settled.displayPositionMs)
        assertFalse(controller.needsActivePolling())
    }

    @Test
    fun largeSeekDifferenceDoesNotOverflowAndKeepPendingSeekActive() {
        val controller = PlaybackProgressController { 0L }
        controller.onPlayerSnapshot(
            mediaId = 1L,
            positionMs = Long.MAX_VALUE,
            durationMs = 0L,
            bufferedPositionMs = 0L,
            isPlaying = true,
        )
        controller.beginScrub()
        controller.finishScrub(0L)

        controller.onPlayerSnapshot(
            mediaId = 1L,
            positionMs = Long.MAX_VALUE,
            durationMs = 0L,
            bufferedPositionMs = 0L,
            isPlaying = true,
        )

        assertTrue(controller.needsActivePolling())
    }

    @Test
    fun repeatedQueueOccurrenceClearsPendingSeekForSameSongId() {
        val controller = PlaybackProgressController { 0L }
        controller.onPlayerSnapshot(
            mediaId = 1L,
            mediaItemOccurrence = 1L,
            positionMs = 0L,
            durationMs = 60_000L,
            bufferedPositionMs = 0L,
            isPlaying = true,
        )
        controller.beginScrub()
        controller.finishScrub(45_000L)

        val nextOccurrence = controller.onPlayerSnapshot(
            mediaId = 1L,
            mediaItemOccurrence = 2L,
            positionMs = 0L,
            durationMs = 60_000L,
            bufferedPositionMs = 0L,
            isPlaying = true,
        )

        assertEquals(0L, nextOccurrence.displayPositionMs)
        assertFalse(controller.needsActivePolling())
        assertFalse(nextOccurrence.isUserScrubbing)
    }
}
