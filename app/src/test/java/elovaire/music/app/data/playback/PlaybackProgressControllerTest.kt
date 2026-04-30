package elovaire.music.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackProgressControllerTest {
    @Test
    fun trackChangeResetsScrubAndIncrementsGeneration() {
        val controller = PlaybackProgressController()
        controller.onPlayerSnapshot(mediaId = 11L, positionMs = 12_000L, durationMs = 200_000L, bufferedPositionMs = 40_000L, isPlaying = true)
        controller.beginScrub()
        controller.updateScrubPosition(66_000L)

        val updated = controller.onPlayerSnapshot(
            mediaId = 22L,
            positionMs = 1_000L,
            durationMs = 180_000L,
            bufferedPositionMs = 10_000L,
            isPlaying = true,
        )

        assertEquals(22L, updated.currentMediaId)
        assertFalse(updated.isUserScrubbing)
        assertNull(updated.scrubPreviewPositionMs)
        assertEquals(1_000L, updated.displayPositionMs)
        assertEquals(2L, updated.generation)
    }

    @Test
    fun scrubPreviewOverridesPolledPositionUntilCommit() {
        val controller = PlaybackProgressController()
        controller.onPlayerSnapshot(mediaId = 33L, positionMs = 5_000L, durationMs = 120_000L, bufferedPositionMs = 20_000L, isPlaying = true)

        controller.beginScrub()
        val scrubbing = controller.updateScrubPosition(44_000L)

        assertTrue(scrubbing.isUserScrubbing)
        assertEquals(44_000L, scrubbing.displayPositionMs)
        assertEquals(5_000L, scrubbing.positionMs)
    }

    @Test
    fun finishScrubProducesSingleFinalSeek() {
        val controller = PlaybackProgressController()
        controller.onPlayerSnapshot(mediaId = 44L, positionMs = 8_000L, durationMs = 100_000L, bufferedPositionMs = 25_000L, isPlaying = true)
        controller.beginScrub()

        val result = controller.finishScrub(63_000L)

        assertEquals(63_000L, result.seekPositionMs)
        assertFalse(result.state.isUserScrubbing)
        assertEquals(63_000L, result.state.displayPositionMs)
    }

    @Test
    fun staleScrubCommitOnDifferentTrackIsIgnored() {
        val controller = PlaybackProgressController()
        controller.onPlayerSnapshot(mediaId = 55L, positionMs = 3_000L, durationMs = 90_000L, bufferedPositionMs = 18_000L, isPlaying = true)
        controller.beginScrub()
        controller.updateScrubPosition(40_000L)
        controller.onPlayerSnapshot(mediaId = 56L, positionMs = 500L, durationMs = 110_000L, bufferedPositionMs = 4_000L, isPlaying = true)

        val result = controller.finishScrub(42_000L)

        assertNull(result.seekPositionMs)
        assertEquals(500L, result.state.displayPositionMs)
        assertEquals(56L, result.state.currentMediaId)
    }

    @Test
    fun playerSnapshotClearsPendingSeekWhenPositionSettles() {
        val controller = PlaybackProgressController()
        controller.onPlayerSnapshot(mediaId = 77L, positionMs = 0L, durationMs = 200_000L, bufferedPositionMs = 0L, isPlaying = true)
        controller.beginScrub()
        val commit = controller.finishScrub(88_000L)
        assertEquals(88_000L, commit.state.displayPositionMs)

        val settled = controller.onPlayerSnapshot(
            mediaId = 77L,
            positionMs = 88_120L,
            durationMs = 200_000L,
            bufferedPositionMs = 100_000L,
            isPlaying = true,
        )

        assertEquals(88_120L, settled.displayPositionMs)
        assertEquals(88_120L, settled.positionMs)
    }

    @Test
    fun clearResetsEverything() {
        val controller = PlaybackProgressController()
        controller.onPlayerSnapshot(mediaId = 99L, positionMs = 10_000L, durationMs = 200_000L, bufferedPositionMs = 20_000L, isPlaying = true)
        val cleared = controller.clear()

        assertNull(cleared.currentMediaId)
        assertEquals(0L, cleared.positionMs)
        assertEquals(0L, cleared.durationMs)
        assertEquals(0L, cleared.bufferedPositionMs)
        assertEquals(0L, cleared.displayPositionMs)
        assertFalse(cleared.isPlaying)
    }
}
