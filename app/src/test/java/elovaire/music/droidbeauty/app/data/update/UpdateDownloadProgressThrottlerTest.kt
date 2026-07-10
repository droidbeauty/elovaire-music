package elovaire.music.droidbeauty.app.data.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateDownloadProgressThrottlerTest {
    @Test
    fun suppressesBufferLevelUpdatesUntilProgressOrTimeThresholdIsReached() {
        val throttler = UpdateDownloadProgressThrottler(
            minimumProgressDelta = 0.01f,
            minimumIntervalMs = 150L,
        )

        assertTrue(throttler.shouldEmit(progress = 0.001f, nowMs = 0L))
        assertFalse(throttler.shouldEmit(progress = 0.005f, nowMs = 50L))
        assertTrue(throttler.shouldEmit(progress = 0.005f, nowMs = 150L))
        assertTrue(throttler.shouldEmit(progress = 1f, nowMs = 151L))
    }
}
