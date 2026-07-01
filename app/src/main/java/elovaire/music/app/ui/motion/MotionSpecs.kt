package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring as composeSpring
import androidx.compose.animation.core.tween as composeTween
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
    ): TweenSpec<T> = composeTween(
        durationMillis = runtime.duration(durationMillis),
        delayMillis = runtime.delay(delayMillis),
        easing = easing,
    )

    fun <T> spring(
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float = 520f,
    ): FiniteAnimationSpec<T> = composeSpring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
    )

    fun <T> fadeIn(
        durationMillis: Int = MotionDuration.ScreenFade,
        delayMillis: Int = 0,
    ): FiniteAnimationSpec<T> = tween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = MotionEasing.FadeIn,
    )

    fun <T> fadeOut(
        durationMillis: Int = MotionDuration.Quick,
    ): FiniteAnimationSpec<T> = tween(
        durationMillis = durationMillis,
        easing = MotionEasing.FadeOut,
    )

    fun <T> refinedEnter(
        durationMillis: Int,
        delayMillis: Int = 0,
    ): FiniteAnimationSpec<T> = tween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = MotionEasing.RefinedDecelerate,
    )

    fun <T> refinedExit(
        durationMillis: Int,
    ): FiniteAnimationSpec<T> = tween(
        durationMillis = durationMillis,
        easing = MotionEasing.RefinedAccelerate,
    )

    fun <T> contentSize(): FiniteAnimationSpec<T> = tween(
        durationMillis = MotionDuration.ChromeResize,
        easing = MotionEasing.RefinedDecelerate,
    )

    fun <T> listReveal(delayMillis: Int = 0): FiniteAnimationSpec<T> = tween(
        durationMillis = MotionDuration.ListReveal,
        delayMillis = delayMillis,
        easing = MotionEasing.RefinedDecelerate,
    )

    fun listRevealDelay(index: Int): Int {
        return runtime.delay((index.coerceAtLeast(0) * 12).coerceAtMost(72))
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
