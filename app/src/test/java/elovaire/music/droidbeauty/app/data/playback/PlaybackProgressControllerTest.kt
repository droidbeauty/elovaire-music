package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class PlaybackProgressControllerTest {
    @Test
    fun pendingSeekSurvivesUnknownSnapshotAndSettlesAtClampedDuration() {
        val controller = PlaybackProgressController()
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
        val controller = PlaybackProgressController()
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
}
