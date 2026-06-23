package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
class MotionSpecs internal constructor(
    private val runtime: MotionRuntime,
) {
    fun <T> tween(
        durationMillis: Int = MotionDuration.Standard,
        delayMillis: Int = 0,
        easing: Easing = MotionEasing.SoftOut,
    ): FiniteAnimationSpec<T> = tween(
        durationMillis = runtime.duration(durationMillis),
        delayMillis = runtime.delay(delayMillis),
        easing = easing,
    )

    fun <T> spring(
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float = 520f,
    ): FiniteAnimationSpec<T> {
        if (runtime.reduceMotion) return tween(durationMillis = 0)
        return spring(
            dampingRatio = dampingRatio,
            stiffness = (stiffness / (runtime.durationScale * runtime.durationScale))
                .coerceIn(25f, 10_000f),
        )
    }

    fun <T> pressDown(): FiniteAnimationSpec<T> = tween(
        durationMillis = MotionDuration.Micro,
        easing = MotionEasing.SoftOut,
    )

    fun <T> chromeRelease(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 620f,
    )

    fun <T> mediaRelease(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.78f,
        stiffness = 620f,
    )
}

@Composable
fun rememberMotionSpecs(): MotionSpecs {
    val runtime = LocalMotionRuntime.current
    return remember(runtime) { MotionSpecs(runtime) }
}
