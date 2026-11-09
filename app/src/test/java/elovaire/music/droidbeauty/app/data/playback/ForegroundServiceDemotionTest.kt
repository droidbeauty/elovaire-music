package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundServiceDemotionTest {
    @Test
    fun failedDemotionCanBeRetriedWithoutInvalidatingNewerStarts() {
        val requests = PlaybackForegroundRequests()
        val started = requests.requestStart(1)!!
        requests.completeStart(1, started)
        val failed = requests.requestDemotion()!!
        try {
            requests.demotionDispatchFailed(failed) { throw IllegalStateException("stop failed") }
        } catch (_: IllegalStateException) {
            // The foreground service remains tracked so a later demotion can retry.
        }
        assertNotNull(requests.requestDemotion())
        val resumed = requests.requestStart(1)!!
        requests.demotionDispatchFailed(failed) { error("stale demotion must not stop playback") }
        assertTrue(requests.isCurrentStart(1, resumed))
    }

    @Test
    fun failedDemotionStopsTheServiceIfTheRequestIsStillCurrent() {
        val requests = PlaybackForegroundRequests()
        val start = requests.requestStart(1)!!
        requests.completeStart(1, start)
        val demotion = requests.requestDemotion()!!
        var stopped = false
        requests.demotionDispatchFailed(demotion) { stopped = true }
        assertTrue(stopped)
        assertFalse(requests.hasActiveRequest())
    }

    @Test
    fun returningToRunningNotificationSupersedesPendingDifferentNotification() {
        val requests = PlaybackForegroundRequests()
        val original = requests.requestStart(1)!!
        requests.completeStart(1, original)
        val pending = requests.requestStart(2)!!
        val latest = requests.requestStart(1)!!
        assertFalse(requests.isCurrentStart(2, pending))
        assertTrue(requests.isCurrentStart(1, latest))
    }

    @Test
    fun delayedDemotionCannotStopANewerPlaybackStart() {
        val requests = PlaybackForegroundRequests()
        val first = requests.requestStart(4101)!!
        requests.completeStart(4101, first)
        val demotion = requests.requestDemotion()!!
        assertTrue(requests.isCurrentDemotion(demotion))
        val resumed = requests.requestStart(4101)!!
        assertFalse(requests.isCurrentDemotion(demotion))
        assertFalse(requests.isCurrentStart(4101, first))
        assertTrue(requests.hasActiveRequest())
        requests.serviceDestroyed()
        assertTrue(requests.isCurrentStart(4101, resumed))
        requests.completeStart(4101, resumed)
        assertFalse(requests.isCurrentDemotion(demotion))
        assertFalse(requests.isCurrentStart(4101, first))
        assertTrue(requests.hasActiveRequest())
        assertNull(requests.requestStart(4101))
    }

    @Test
    fun failedOldStartCannotCancelNewerPlaybackRequest() {
        val requests = PlaybackForegroundRequests()
        val first = requests.requestStart(1)!!
        requests.requestDemotion()
        val resumed = requests.requestStart(1)!!
        requests.startDispatchFailed(first)
        requests.invalidate(first)
        requests.invalidate(0)
        assertTrue(requests.isCurrentStart(1, resumed))
        requests.cancel()
        assertFalse(requests.isCurrentStart(1, resumed))
        assertFalse(requests.hasActiveRequest())
        assertNotNull(requests.requestStart(1))
    }
}
