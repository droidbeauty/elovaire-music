package elovaire.music.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object ElovaireMotion {
    val Quick = 120
    val Standard = 200
    val Spacious = 280
    val Screen = 260
    val ScreenFade = 180
    val ScreenSlide = 240
    val ScreenExpand = 320
    val PlayerScreen = 360
    val PlayerFade = 180
    val Controls = 140
    val ChromeResize = 180

    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    fun <T> colorFadeSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = Controls,
        easing = FastOutSlowInEasing,
    )

    fun <T> contentFadeInSpec(delayMillis: Int = 0): FiniteAnimationSpec<T> = tween(
        durationMillis = Standard,
        delayMillis = delayMillis,
        easing = LinearOutSlowInEasing,
    )

    fun <T> contentFadeOutSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = Quick,
        easing = FastOutLinearInEasing,
    )

    fun <T> pressDownSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = 90,
        easing = FastOutSlowInEasing,
    )

    fun <T> releaseSpringSpec(
        dampingRatio: Float = 0.82f,
        stiffness: Float = 560f,
    ): FiniteAnimationSpec<T> = spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
    )

    fun <T> bounceSpringSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.68f,
        stiffness = 420f,
    )

    fun <T> overscrollSpringSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.9f,
        stiffness = 680f,
    )

    fun <T> iconSwapInSpec(delayMillis: Int = 0): FiniteAnimationSpec<T> = tween(
        durationMillis = 180,
        delayMillis = delayMillis,
        easing = LinearOutSlowInEasing,
    )

    fun <T> iconSwapOutSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = 110,
        easing = FastOutLinearInEasing,
    )

    fun <T> emphasizedEnterSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = ScreenExpand,
        easing = EmphasizedDecelerate,
    )

    fun <T> emphasizedExitSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = ScreenFade,
        easing = EmphasizedAccelerate,
    )
}
