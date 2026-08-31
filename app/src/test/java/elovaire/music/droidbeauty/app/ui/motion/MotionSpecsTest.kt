package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.core.Spring
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class MotionSpecsTest {
    @Test
    fun commonSpecs_areReusedWithinRuntimeHolder() {
        val specs = MotionSpecs(MotionRuntime(durationScale = 1f))

        assertSame(
            specs.tween<Float>(MotionDuration.Standard, easing = MotionEasing.SoftOut),
            specs.tween<Float>(MotionDuration.Standard, easing = MotionEasing.SoftOut),
        )
        assertSame(
            specs.spring<Float>(Spring.DampingRatioNoBouncy, 520f),
            specs.spring<Float>(Spring.DampingRatioNoBouncy, 520f),
        )
    }

    @Test
    fun specs_areNotSharedAcrossRuntimeHolders() {
        val first = MotionSpecs(MotionRuntime(durationScale = 1f))
        val second = MotionSpecs(MotionRuntime(durationScale = 1f))

        assertNotSame(first.tween<Float>(), second.tween<Float>())
    }

    @Test
    fun reducedMotionUsesZeroDurationForFiniteSpecs() {
        val specs = MotionSpecs(MotionRuntime(durationScale = 0f))

        assertEquals(0, specs.tween<Float>(MotionDuration.Standard).durationMillis)
        assertEquals(
            0,
            specs.tween<Float>(MotionDuration.Standard, delayMillis = MotionDuration.Medium).durationMillis,
        )
    }
}
