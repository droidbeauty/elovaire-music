package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.core.Spring
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
}
