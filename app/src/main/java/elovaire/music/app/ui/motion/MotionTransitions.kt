package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.TransformOrigin
import kotlin.math.roundToInt

@Stable
class MotionTransitions internal constructor(
    private val specs: MotionSpecs,
) {
    private fun fadeSlideVerticalEnter(
        fadeDuration: Int,
        slideDuration: Int,
        initialAlpha: Float,
        initialOffsetY: (Int) -> Int,
        fadeEasing: Easing = MotionEasing.FadeIn,
        slideEasing: Easing = MotionEasing.RefinedDecelerate,
    ): EnterTransition = fadeIn(
        animationSpec = specs.tween(fadeDuration, easing = fadeEasing),
        initialAlpha = initialAlpha,
    ) + slideInVertically(
        animationSpec = specs.tween(slideDuration, easing = slideEasing),
        initialOffsetY = initialOffsetY,
    )

    private fun fadeSlideVerticalExit(
        fadeDuration: Int,
        slideDuration: Int,
        targetAlpha: Float = 0f,
        targetOffsetY: (Int) -> Int,
        fadeEasing: Easing = MotionEasing.FadeOut,
        slideEasing: Easing = MotionEasing.RefinedAccelerate,
    ): ExitTransition = fadeOut(
        animationSpec = specs.tween(fadeDuration, easing = fadeEasing),
        targetAlpha = targetAlpha,
    ) + slideOutVertically(
        animationSpec = specs.tween(slideDuration, easing = slideEasing),
        targetOffsetY = targetOffsetY,
    )

    fun overlayFadeEnter(initialAlpha: Float = 0.78f): EnterTransition = fadeIn(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Standard,
            easing = MotionEasing.FadeIn,
        ),
        initialAlpha = initialAlpha,
    )

    fun overlayFadeExit(targetAlpha: Float = 0.92f): ExitTransition = fadeOut(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Fast,
            easing = MotionEasing.FadeOut,
        ),
        targetAlpha = targetAlpha,
    )

    fun bottomSheetEnter(): EnterTransition = overlayFadeEnter(initialAlpha = 0.74f) +
        slideInVertically(
            animationSpec = specs.tween(
                durationMillis = MotionDuration.Emphasized,
                easing = MotionEasing.RefinedDecelerate,
            ),
            initialOffsetY = { it / 5 },
        )

    fun bottomSheetExit(): ExitTransition = overlayFadeExit(targetAlpha = 0.9f) +
        slideOutVertically(
            animationSpec = specs.tween(
                durationMillis = MotionDuration.Fast,
                easing = MotionEasing.RefinedAccelerate,
            ),
            targetOffsetY = { it / 8 },
        )

    fun bannerEnter(): EnterTransition = fadeSlideVerticalEnter(
        fadeDuration = MotionDuration.Standard,
        slideDuration = MotionDuration.Screen,
        initialAlpha = 0.82f,
        initialOffsetY = { -(it / 2) },
    )

    fun bannerExit(): ExitTransition = fadeSlideVerticalExit(
        fadeDuration = MotionDuration.Fast,
        slideDuration = MotionDuration.Standard,
        targetAlpha = 0.94f,
        targetOffsetY = { -(it / 3) },
    )

    fun bottomBarEnter(): EnterTransition = fadeSlideVerticalEnter(
        fadeDuration = MotionDuration.Standard,
        slideDuration = MotionDuration.Standard,
        initialAlpha = 0.82f,
        initialOffsetY = { it / 2 },
    )

    fun bottomBarExit(): ExitTransition = fadeSlideVerticalExit(
        fadeDuration = MotionDuration.Fast,
        slideDuration = MotionDuration.Quick,
        targetAlpha = 0.94f,
        targetOffsetY = { it / 2 },
    )

    fun verticalRevealEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Fast,
            easing = MotionEasing.FadeIn,
        ),
    ) + slideInVertically(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Standard,
            easing = MotionEasing.RefinedDecelerate,
        ),
        initialOffsetY = { -it / 10 },
    )

    fun verticalRevealExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Quick,
            easing = MotionEasing.FadeOut,
        ),
    ) + slideOutVertically(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Component,
            easing = MotionEasing.RefinedAccelerate,
        ),
        targetOffsetY = { -it / 12 },
    )

    fun contextMenuEnter(
        origin: TransformOrigin = TransformOrigin(1f, 0f),
    ): EnterTransition = fadeIn(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Component,
            easing = MotionEasing.FadeIn,
        ),
        initialAlpha = 0.72f,
    ) + scaleIn(
        initialScale = 0.96f,
        transformOrigin = origin,
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Standard,
            easing = MotionEasing.RefinedDecelerate,
        ),
    )

    fun contextMenuExit(
        origin: TransformOrigin = TransformOrigin(1f, 0f),
    ): ExitTransition = fadeOut(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Fast,
            easing = MotionEasing.FadeOut,
        ),
        targetAlpha = 0.92f,
    ) + scaleOut(
        targetScale = 0.985f,
        transformOrigin = origin,
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Fast,
            easing = MotionEasing.RefinedAccelerate,
        ),
    )

    fun standardEnter(): EnterTransition = fadeSlideVerticalEnter(
        fadeDuration = MotionDuration.ScreenFade,
        slideDuration = MotionDuration.ScreenSlide,
        initialAlpha = 0f,
        initialOffsetY = { it / 8 },
        slideEasing = MotionEasing.SoftOut,
    )

    fun standardExit(): ExitTransition = fadeSlideVerticalExit(
        fadeDuration = MotionDuration.Quick,
        slideDuration = MotionDuration.Fast,
        targetOffsetY = { it / 10 },
        slideEasing = MotionEasing.SoftOut,
    )

    fun playerOverlayEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.PlayerFade,
            easing = MotionEasing.FadeIn,
        ),
    ) + scaleIn(
        initialScale = MotionScale.PlayerOverlayEnter,
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Player,
            easing = MotionEasing.RefinedDecelerate,
        ),
    )

    fun playerOverlayExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.PlayerFade,
            easing = MotionEasing.FadeOut,
        ),
    ) + scaleOut(
        targetScale = MotionScale.PlayerOverlayExit,
        animationSpec = specs.tween(
            durationMillis = MotionDuration.PlayerFade,
            easing = MotionEasing.RefinedAccelerate,
        ),
    )

    fun compactBarEnter(returningFromPlayer: Boolean): EnterTransition {
        if (returningFromPlayer) {
            return fadeIn(
                animationSpec = specs.tween(
                    durationMillis = MotionDuration.PlayerFade,
                    easing = MotionEasing.FadeIn,
                ),
                initialAlpha = 0.01f,
            )
        }
        return fadeSlideVerticalEnter(
            fadeDuration = MotionDuration.Emphasized,
            slideDuration = MotionDuration.Emphasized,
            initialAlpha = 0.68f,
            initialOffsetY = { it / 8 },
        )
    }

    fun compactBarExit(): ExitTransition = fadeSlideVerticalExit(
        fadeDuration = MotionDuration.Standard,
        slideDuration = MotionDuration.Standard,
        targetAlpha = 0.92f,
        targetOffsetY = { it / 12 },
    )

    fun softContentTransform(): ContentTransform =
        (fadeIn(animationSpec = specs.fadeIn(MotionDuration.Standard)) +
            slideInVertically(
                animationSpec = specs.refinedEnter(MotionDuration.Standard),
                initialOffsetY = { -it / 10 },
            )) togetherWith fadeOut(animationSpec = specs.fadeOut(MotionDuration.Quick))

    fun quickContentSwapTransform(): ContentTransform =
        fadeIn(animationSpec = specs.fadeIn(MotionDuration.Component)) togetherWith
            fadeOut(animationSpec = specs.fadeOut(MotionDuration.Fast))

    fun queueMenuEnter(
        origin: TransformOrigin = TransformOrigin(1f, 1f),
    ): EnterTransition = fadeIn(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.QueueMenuEnter,
            easing = MotionEasing.RefinedDecelerate,
        ),
        initialAlpha = 0.04f,
    ) + scaleIn(
        initialScale = 0.94f,
        transformOrigin = origin,
        animationSpec = specs.tween(
            durationMillis = MotionDuration.QueueMenuEnter,
            easing = MotionEasing.RefinedDecelerate,
        ),
    )

    fun queueMenuExit(
        origin: TransformOrigin = TransformOrigin(1f, 1f),
    ): ExitTransition = fadeOut(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.QueueMenuExit,
            easing = MotionEasing.RefinedAccelerate,
        ),
    ) + scaleOut(
        targetScale = 0.98f,
        transformOrigin = origin,
        animationSpec = specs.tween(
            durationMillis = MotionDuration.QueueMenuExit,
            easing = MotionEasing.RefinedAccelerate,
        ),
    )

    fun fullScreenForwardEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.08f,
    ) + slideInHorizontally(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.RefinedDecelerate),
        initialOffsetX = { it / 5 },
    )

    fun fullScreenForwardExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.FadeOut),
        targetAlpha = 0f,
    ) + slideOutHorizontally(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.RefinedAccelerate),
        targetOffsetX = { -(it / 6) },
    )

    fun fullScreenBackEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.08f,
    ) + slideInHorizontally(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.RefinedDecelerate),
        initialOffsetX = { -(it / 6) },
    )

    fun fullScreenBackExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.FadeOut),
        targetAlpha = 0f,
    ) + slideOutHorizontally(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.RefinedAccelerate),
        targetOffsetX = { it / 5 },
    )

    fun topLevelEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(MotionDuration.TopLevelEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.02f,
    )

    fun topLevelExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(MotionDuration.TopLevelExit, easing = MotionEasing.FadeOut),
    )

    fun detailForwardEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.08f,
    ) + slideInHorizontally(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.RefinedDecelerate),
        initialOffsetX = { it / 5 },
    )

    fun detailForwardExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.FadeOut),
        targetAlpha = 0f,
    ) + slideOutHorizontally(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.RefinedAccelerate),
        targetOffsetX = { -(it / 6) },
    )

    fun detailBackEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.1f,
    ) + slideInHorizontally(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.RefinedDecelerate),
        initialOffsetX = { -(it / 6) },
    )

    fun detailBackExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.FadeOut),
        targetAlpha = 0f,
    ) + slideOutHorizontally(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.RefinedAccelerate),
        targetOffsetX = { it / 5 },
    )

    fun albumDetailForwardEnter(
        transformOrigin: TransformOrigin = TransformOrigin.Center,
    ): EnterTransition = fadeIn(
        animationSpec = specs.tween(MotionDuration.AlbumDetail, easing = MotionEasing.FadeIn),
        initialAlpha = 0.04f,
    ) + scaleIn(
        animationSpec = specs.spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        initialScale = 0.74f,
        transformOrigin = transformOrigin,
    ) + slideInHorizontally(
        animationSpec = specs.spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        initialOffsetX = { fullWidth ->
            ((transformOrigin.pivotFractionX - 0.5f) * fullWidth * 0.24f).roundToInt()
        },
    ) + slideInVertically(
        animationSpec = specs.spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        initialOffsetY = { fullHeight ->
            ((transformOrigin.pivotFractionY - 0.5f) * fullHeight * 0.24f).roundToInt()
        },
    )

    fun albumDetailForwardExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.FadeOut),
    ) + scaleOut(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.RefinedAccelerate),
        targetScale = 0.994f,
    )

    fun albumDetailBackEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.FadeIn),
        initialAlpha = 0.08f,
    ) + scaleIn(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.RefinedDecelerate),
        initialScale = 0.994f,
    )

    fun albumDetailBackExit(
        transformOrigin: TransformOrigin = TransformOrigin.Center,
    ): ExitTransition = fadeOut(
        animationSpec = specs.tween(MotionDuration.AlbumDetail, easing = MotionEasing.FadeOut),
    ) + scaleOut(
        animationSpec = specs.spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        targetScale = 0.74f,
        transformOrigin = transformOrigin,
    ) + slideOutHorizontally(
        animationSpec = specs.spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        targetOffsetX = { fullWidth ->
            ((transformOrigin.pivotFractionX - 0.5f) * fullWidth * 0.24f).roundToInt()
        },
    ) + slideOutVertically(
        animationSpec = specs.spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        targetOffsetY = { fullHeight ->
            ((transformOrigin.pivotFractionY - 0.5f) * fullHeight * 0.24f).roundToInt()
        },
    )
}

@Composable
fun rememberMotionTransitions(): MotionTransitions {
    val specs = rememberMotionSpecs()
    return remember(specs) { MotionTransitions(specs) }
}
