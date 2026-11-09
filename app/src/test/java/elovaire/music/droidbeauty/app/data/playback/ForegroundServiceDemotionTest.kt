package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundServiceDemotionTest {
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
