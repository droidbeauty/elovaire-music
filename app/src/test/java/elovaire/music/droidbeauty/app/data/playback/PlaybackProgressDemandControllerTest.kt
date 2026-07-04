package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackProgressDemandControllerTest {
    @Test
    fun pollingInterval_usesSlowCompactOnlyCadence() {
        val controller = PlaybackProgressDemandController()

        controller.setActive(PlaybackProgressConsumer.CompactDock, true)

        assertTrue(controller.hasAnyDemand())
        assertEquals(500L, controller.pollingIntervalMs())
    }

    @Test
    fun pollingInterval_prefersVisibleFullPlayerAndLyricsCadence() {
        val controller = PlaybackProgressDemandController()

        controller.setActive(PlaybackProgressConsumer.CompactDock, true)
        controller.setActive(PlaybackProgressConsumer.NowPlaying, true)

        assertEquals(250L, controller.pollingIntervalMs())

        controller.setActive(PlaybackProgressConsumer.NowPlaying, false)
        controller.setActive(PlaybackProgressConsumer.SyncedLyrics, true)

        assertEquals(250L, controller.pollingIntervalMs())
    }

    @Test
    fun pollingInterval_prefersScrubbingCadence() {
        val controller = PlaybackProgressDemandController()

        controller.setActive(PlaybackProgressConsumer.CompactDock, true)
        controller.setActive(PlaybackProgressConsumer.Scrubbing, true)

        assertEquals(120L, controller.pollingIntervalMs())
    }

    @Test
    fun clear_removesAllDemand() {
        val controller = PlaybackProgressDemandController()
        controller.setActive(PlaybackProgressConsumer.CompactDock, true)
        controller.setActive(PlaybackProgressConsumer.NowPlaying, true)

        controller.clear()

        assertFalse(controller.hasAnyDemand())
        assertEquals(1_000L, controller.pollingIntervalMs())
    }
}
