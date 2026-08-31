package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin

/**
 * Compatibility facade for older call sites. New code should use [MotionSpecs] and
 * [MotionTransitions] directly so runtime-aware ownership stays with the composition.
 */
object ElovaireMotion {
    private val defaultSpecs = MotionSpecs(MotionRuntime(durationScale = 1f))

    val Quick: Int get() = MotionDuration.Quick
    val Fast: Int get() = MotionDuration.Fast
    val Standard: Int get() = MotionDuration.Standard
    val Medium: Int get() = MotionDuration.Medium
    val Screen: Int get() = MotionDuration.Screen
    val PlayerScreen: Int get() = MotionDuration.Player

    val SoftOut: Easing = MotionEasing.SoftOut
    val FadeIn: Easing = MotionEasing.FadeIn
    val FadeOut: Easing = MotionEasing.FadeOut
    val EmphasizedDecelerate: Easing = MotionEasing.EmphasizedDecelerate
    val EmphasizedAccelerate: Easing = MotionEasing.EmphasizedAccelerate
    val RefinedDecelerate: Easing = MotionEasing.RefinedDecelerate
    val RefinedAccelerate: Easing = MotionEasing.RefinedAccelerate
    val GentleDecelerate: Easing = RefinedDecelerate
    val GentleAccelerate: Easing = RefinedAccelerate

    @Suppress("UNCHECKED_CAST")
    private fun <T> scaledTween(
        durationMillis: Int,
        delayMillis: Int = 0,
        easing: Easing = SoftOut,
    ): FiniteAnimationSpec<T> = defaultSpecs.tween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = easing,
    )

    private fun <T> scaledSpring(
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float,
    ): FiniteAnimationSpec<T> = defaultSpecs.spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
    )

    fun <T> fadeFast(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.Quick,
        easing = FadeOut,
    )

    fun <T> fadeMedium(delayMillis: Int = 0): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.ScreenFade,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> fadeSlow(delayMillis: Int = 0): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.Spacious,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> scaleSoft(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.Standard,
        easing = SoftOut,
    )

    fun <T> offsetSoft(
        durationMillis: Int = MotionDuration.ScreenSlide,
        delayMillis: Int = 0,
    ): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = SoftOut,
    )

    fun <T> sizeSoft(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = 180,
        easing = RefinedDecelerate,
    )

    fun <T> standardTween(
        durationMillis: Int = MotionDuration.Standard,
        delayMillis: Int = 0,
        easing: Easing = SoftOut,
    ): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = easing,
    )

    fun <T> colorFadeSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = 120,
        easing = SoftOut,
    )

    fun <T> contentFadeInSpec(delayMillis: Int = 0): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.Standard,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> contentFadeOutSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.Quick,
        easing = FadeOut,
    )

    fun <T> pressDownSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.Micro,
        easing = SoftOut,
    )

    fun <T> releaseSpringSpec(
        dampingRatio: Float = 0.82f,
        stiffness: Float = 560f,
    ): FiniteAnimationSpec<T> = scaledSpring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
    )

    fun <T> chromeReleaseSpec(): FiniteAnimationSpec<T> = scaledSpring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 620f,
    )

    fun <T> softPressReturnSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = 150,
        easing = RefinedDecelerate,
    )

    fun <T> bounceSpringSpec(): FiniteAnimationSpec<T> = scaledSpring(
        dampingRatio = 0.68f,
        stiffness = 420f,
    )

    fun <T> overscrollSpringSpec(): FiniteAnimationSpec<T> = scaledSpring(
        dampingRatio = 0.9f,
        stiffness = 680f,
    )

    fun <T> iconSwapInSpec(delayMillis: Int = 0): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.ScreenFade,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> iconSwapOutSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.Quick,
        easing = FadeOut,
    )

    fun <T> emphasizedEnterSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.ScreenExpand,
        easing = EmphasizedDecelerate,
    )

    fun <T> queueMenuEnterSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.QueueMenuEnter,
        easing = RefinedDecelerate,
    )

    fun <T> listPlacementSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.ListPlacement,
        easing = RefinedDecelerate,
    )

    fun <T> titleSwapInSpec(delayMillis: Int = 32): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.Component,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> titleSwapOutSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MotionDuration.Fast,
        easing = FadeOut,
    )

    fun titleSwapTransform(): ContentTransform =
        fadeIn(animationSpec = titleSwapInSpec()) togetherWith
            fadeOut(animationSpec = titleSwapOutSpec())

    fun quickContentSwapTransform(): ContentTransform =
        fadeIn(animationSpec = scaledTween(durationMillis = MotionDuration.Component, easing = FadeIn)) togetherWith
            fadeOut(animationSpec = scaledTween(durationMillis = MotionDuration.Fast, easing = FadeOut))

    fun softContentTransform(): ContentTransform =
        (fadeIn(animationSpec = contentFadeInSpec()) +
            slideInVertically(
                animationSpec = offsetSoft(durationMillis = MotionDuration.Standard),
                initialOffsetY = { -it / 10 },
            )) togetherWith fadeOut(animationSpec = contentFadeOutSpec())

    fun sharedTopBarTransform(): ContentTransform =
        (fadeIn(animationSpec = fadeMedium()) +
            slideInVertically(
                animationSpec = offsetSoft(durationMillis = MotionDuration.ScreenFade),
                initialOffsetY = { -it / 5 },
            )) togetherWith fadeOut(animationSpec = fadeFast())

    fun sharedTopBarForwardTransform(): ContentTransform =
        fadeIn(
            animationSpec = fadeMedium(),
            initialAlpha = 0.9f,
        ) togetherWith fadeOut(
            animationSpec = fadeFast(),
            targetAlpha = 0.92f,
        )

    fun sharedTopBarBackTransform(): ContentTransform =
        fadeIn(
            animationSpec = fadeMedium(),
            initialAlpha = 0.94f,
        ) togetherWith fadeOut(
            animationSpec = fadeFast(),
            targetAlpha = 0.96f,
        )

    fun topBarNavigationTransform(): ContentTransform =
        (fadeIn(animationSpec = scaledTween(durationMillis = MotionDuration.Component, easing = FadeIn)) +
            scaleIn(
                animationSpec = scaledTween(durationMillis = MotionDuration.Component, easing = GentleDecelerate),
                initialScale = 0.96f,
                transformOrigin = TransformOrigin.Center,
            )) togetherWith
            (fadeOut(animationSpec = scaledTween(durationMillis = MotionDuration.Quick, easing = FadeOut)) +
                scaleOut(
                    animationSpec = scaledTween(durationMillis = MotionDuration.Quick, easing = GentleAccelerate),
                    targetScale = 0.98f,
                    transformOrigin = TransformOrigin.Center,
                ))

    fun topBarTextForwardTransform(): ContentTransform =
        (fadeIn(
            animationSpec = scaledTween(durationMillis = MotionDuration.Component, delayMillis = 18, easing = FadeIn),
            initialAlpha = 0.72f,
        ) + slideInHorizontally(
            animationSpec = scaledTween(durationMillis = MotionDuration.Component, easing = GentleDecelerate),
            initialOffsetX = { it / 18 },
        )) togetherWith (fadeOut(
            animationSpec = scaledTween(durationMillis = MotionDuration.Fast, easing = FadeOut),
            targetAlpha = 0.9f,
        ) + slideOutHorizontally(
            animationSpec = scaledTween(durationMillis = MotionDuration.Fast, easing = GentleAccelerate),
            targetOffsetX = { -(it / 24) },
        ))

    fun topBarTextBackTransform(): ContentTransform =
        (fadeIn(
            animationSpec = scaledTween(durationMillis = MotionDuration.Component, easing = FadeIn),
            initialAlpha = 0.78f,
        ) + slideInHorizontally(
            animationSpec = scaledTween(durationMillis = MotionDuration.Component, easing = GentleDecelerate),
            initialOffsetX = { -(it / 22) },
        )) togetherWith (fadeOut(
            animationSpec = scaledTween(durationMillis = MotionDuration.Fast, easing = FadeOut),
            targetAlpha = 0.92f,
        ) + slideOutHorizontally(
            animationSpec = scaledTween(durationMillis = MotionDuration.Fast, easing = GentleAccelerate),
            targetOffsetX = { it / 28 },
        ))

    fun topBarActionSwapTransform(): ContentTransform =
        fadeIn(
            animationSpec = scaledTween(
                durationMillis = MotionDuration.TopBarActionEnter,
                delayMillis = MotionDuration.TopBarActionExit,
                easing = FadeIn,
            ),
            initialAlpha = 0f,
        ) togetherWith fadeOut(
            animationSpec = scaledTween(
                durationMillis = MotionDuration.TopBarActionExit,
                easing = FadeOut,
            ),
            targetAlpha = 0f,
        )

    fun fullScreenForwardEnter(
        initialOffsetX: (fullWidth: Int) -> Int = { it / 64 },
    ): EnterTransition = fadeIn(
        animationSpec = scaledTween(
            durationMillis = MotionDuration.FullScreenEnter,
            easing = FadeIn,
        ),
        initialAlpha = 0.01f,
    ) +
        slideInHorizontally(
            animationSpec = scaledTween(
                durationMillis = MotionDuration.FullScreenEnter,
                easing = RefinedDecelerate,
            ),
            initialOffsetX = initialOffsetX,
        )

    fun fullScreenForwardExit(
        targetOffsetX: (fullWidth: Int) -> Int = { -(it / 96) },
    ): ExitTransition = fadeOut(
        animationSpec = scaledTween(
            durationMillis = MotionDuration.FullScreenExit,
            easing = FadeOut,
        ),
        targetAlpha = 0f,
    ) +
        slideOutHorizontally(
            animationSpec = scaledTween(
                durationMillis = MotionDuration.FullScreenExit,
                easing = RefinedAccelerate,
            ),
            targetOffsetX = targetOffsetX,
        )

    fun fullScreenBackEnter(
        initialOffsetX: (fullWidth: Int) -> Int = { -(it / 96) },
    ): EnterTransition = fadeIn(
        animationSpec = scaledTween(
            durationMillis = MotionDuration.FullScreenEnter,
            easing = FadeIn,
        ),
        initialAlpha = 0.08f,
    ) +
        slideInHorizontally(
            animationSpec = scaledTween(
                durationMillis = MotionDuration.FullScreenEnter,
                easing = RefinedDecelerate,
            ),
            initialOffsetX = initialOffsetX,
        )

    fun fullScreenBackExit(
        targetOffsetX: (fullWidth: Int) -> Int = { it / 72 },
    ): ExitTransition = fadeOut(
        animationSpec = scaledTween(
            durationMillis = MotionDuration.FullScreenExit,
            easing = FadeOut,
        ),
        targetAlpha = 0f,
    ) +
        slideOutHorizontally(
            animationSpec = scaledTween(
                durationMillis = MotionDuration.FullScreenExit,
                easing = RefinedAccelerate,
            ),
            targetOffsetX = targetOffsetX,
        )

    fun scaleDurationMillis(
        durationMillis: Long,
        durationScale: Float,
    ): Long = when {
        durationMillis <= 0L -> 0L
        else -> durationMillis
    }

    fun scaleDurationMillis(
        durationMillis: Int,
        durationScale: Float,
    ): Long = scaleDurationMillis(durationMillis.toLong(), durationScale)
}
