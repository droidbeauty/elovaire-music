package elovaire.music.droidbeauty.app.ui.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionRuntimeTest {
    @Test
    fun keepsDurationsUnscaledForComposeMotionDurationScale() {
        val runtime = MotionRuntime(durationScale = 0.5f)

        assertEquals(180, runtime.duration(180))
        assertEquals(12, runtime.delay(12))
        assertEquals(1_500L, runtime.duration(1_500L))
    }

    @Test
    fun zeroScaleCompletesMotionImmediately() {
        val runtime = MotionRuntime(durationScale = 0f)

        assertTrue(runtime.reduceMotion)
        assertEquals(0, runtime.duration(180))
        assertEquals(0, runtime.delay(12))
        assertEquals(0L, runtime.duration(1_500L))
    }
}
