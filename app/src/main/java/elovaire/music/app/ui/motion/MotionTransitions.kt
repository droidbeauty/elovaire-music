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
import elovaire.music.droidbeauty.app.BuildConfig
import kotlin.math.roundToInt

@Stable
class MotionTransitions internal constructor(
    private val specs: MotionSpecs,
) {
    private val cachedTransitions = mutableMapOf<StaticMotionTransition, Any>()

    @Suppress("UNCHECKED_CAST")
    private inline fun <T : Any> cached(
        key: StaticMotionTransition,
        create: () -> T,
    ): T {
        return cachedTransitions.getOrPut(key) {
            MotionTransitionDebugCounters.recordConstruction(key)
            create()
        } as T
    }

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

    fun overlayFadeEnter(initialAlpha: Float = 0.78f): EnterTransition {
        if (initialAlpha != DEFAULT_OVERLAY_ENTER_ALPHA) return buildOverlayFadeEnter(initialAlpha)
        return cached(StaticMotionTransition.OverlayFadeEnter) {
            buildOverlayFadeEnter(initialAlpha)
        }
    }

    fun overlayFadeExit(targetAlpha: Float = 0.92f): ExitTransition {
        if (targetAlpha != DEFAULT_OVERLAY_EXIT_ALPHA) return buildOverlayFadeExit(targetAlpha)
        return cached(StaticMotionTransition.OverlayFadeExit) {
            buildOverlayFadeExit(targetAlpha)
        }
    }

    private fun buildOverlayFadeEnter(initialAlpha: Float): EnterTransition {
        return fadeIn(
            animationSpec = specs.tween(
                durationMillis = MotionDuration.Standard,
                easing = MotionEasing.FadeIn,
            ),
            initialAlpha = initialAlpha,
        )
    }

    private fun buildOverlayFadeExit(targetAlpha: Float): ExitTransition {
        return fadeOut(
            animationSpec = specs.tween(
                durationMillis = MotionDuration.Fast,
                easing = MotionEasing.FadeOut,
            ),
            targetAlpha = targetAlpha,
        )
    }

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

    fun standardEnter(): EnterTransition = cached(StaticMotionTransition.StandardEnter) { fadeSlideVerticalEnter(
        fadeDuration = MotionDuration.ScreenFade,
        slideDuration = MotionDuration.ScreenSlide,
        initialAlpha = 0f,
        initialOffsetY = { it / 8 },
        slideEasing = MotionEasing.SoftOut,
    ) }

    fun standardExit(): ExitTransition = cached(StaticMotionTransition.StandardExit) { fadeSlideVerticalExit(
        fadeDuration = MotionDuration.Quick,
        slideDuration = MotionDuration.Fast,
        targetOffsetY = { it / 10 },
        slideEasing = MotionEasing.SoftOut,
    ) }

    fun playerOverlayEnter(): EnterTransition = cached(StaticMotionTransition.PlayerOverlayEnter) { fadeIn(
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
    ) }

    fun playerOverlayExit(): ExitTransition = cached(StaticMotionTransition.PlayerOverlayExit) { fadeOut(
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
    ) }

    fun compactBarEnter(returningFromPlayer: Boolean): EnterTransition {
        if (returningFromPlayer) {
            return cached(StaticMotionTransition.CompactBarReturnEnter) { fadeIn(
                animationSpec = specs.tween(
                    durationMillis = MotionDuration.PlayerFade,
                    easing = MotionEasing.FadeIn,
                ),
                initialAlpha = 0.01f,
            ) }
        }
        return cached(StaticMotionTransition.CompactBarEnter) { fadeSlideVerticalEnter(
            fadeDuration = MotionDuration.Emphasized,
            slideDuration = MotionDuration.Emphasized,
            initialAlpha = 0.68f,
            initialOffsetY = { it / 8 },
        ) }
    }

    fun compactBarExit(): ExitTransition = cached(StaticMotionTransition.CompactBarExit) { fadeSlideVerticalExit(
        fadeDuration = MotionDuration.Standard,
        slideDuration = MotionDuration.Standard,
        targetAlpha = 0.92f,
        targetOffsetY = { it / 12 },
    ) }

    fun softContentTransform(): ContentTransform =
        cached(StaticMotionTransition.SoftContentTransform) { (fadeIn(animationSpec = specs.fadeIn(MotionDuration.Standard)) +
            slideInVertically(
                animationSpec = specs.refinedEnter(MotionDuration.Standard),
                initialOffsetY = { -it / 10 },
            )) togetherWith fadeOut(animationSpec = specs.fadeOut(MotionDuration.Quick)) }

    fun quickContentSwapTransform(): ContentTransform =
        cached(StaticMotionTransition.QuickContentSwapTransform) {
            fadeIn(animationSpec = specs.fadeIn(MotionDuration.Component)) togetherWith
                fadeOut(animationSpec = specs.fadeOut(MotionDuration.Fast))
        }

    fun queueMenuEnter(
        origin: TransformOrigin = TransformOrigin(1f, 1f),
    ): EnterTransition {
        if (origin != DEFAULT_QUEUE_ORIGIN) return buildQueueMenuEnter(origin)
        return cached(StaticMotionTransition.QueueMenuEnter) { buildQueueMenuEnter(origin) }
    }

    private fun buildQueueMenuEnter(origin: TransformOrigin): EnterTransition = fadeIn(
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
    ): ExitTransition {
        if (origin != DEFAULT_QUEUE_ORIGIN) return buildQueueMenuExit(origin)
        return cached(StaticMotionTransition.QueueMenuExit) { buildQueueMenuExit(origin) }
    }

    private fun buildQueueMenuExit(origin: TransformOrigin): ExitTransition = fadeOut(
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

    fun fullScreenForwardEnter(): EnterTransition = cached(StaticMotionTransition.FullScreenForwardEnter) { fadeIn(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.08f,
    ) + slideInHorizontally(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.RefinedDecelerate),
        initialOffsetX = { it / 5 },
    ) }

    fun fullScreenForwardExit(): ExitTransition = cached(StaticMotionTransition.FullScreenForwardExit) { fadeOut(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.FadeOut),
        targetAlpha = 0f,
    ) + slideOutHorizontally(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.RefinedAccelerate),
        targetOffsetX = { -(it / 6) },
    ) }

    fun fullScreenBackEnter(): EnterTransition = cached(StaticMotionTransition.FullScreenBackEnter) { fadeIn(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.08f,
    ) + slideInHorizontally(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.RefinedDecelerate),
        initialOffsetX = { -(it / 6) },
    ) }

    fun fullScreenBackExit(): ExitTransition = cached(StaticMotionTransition.FullScreenBackExit) { fadeOut(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.FadeOut),
        targetAlpha = 0f,
    ) + slideOutHorizontally(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.RefinedAccelerate),
        targetOffsetX = { it / 5 },
    ) }

    fun topLevelEnter(): EnterTransition = cached(StaticMotionTransition.TopLevelEnter) { fadeIn(
        animationSpec = specs.tween(MotionDuration.TopLevelEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.02f,
    ) }

    fun topLevelExit(): ExitTransition = cached(StaticMotionTransition.TopLevelExit) { fadeOut(
        animationSpec = specs.tween(MotionDuration.TopLevelExit, easing = MotionEasing.FadeOut),
    ) }

    fun detailForwardEnter(): EnterTransition = cached(StaticMotionTransition.DetailForwardEnter) { fadeIn(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.08f,
    ) + slideInHorizontally(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.RefinedDecelerate),
        initialOffsetX = { it / 5 },
    ) }

    fun detailForwardExit(): ExitTransition = cached(StaticMotionTransition.DetailForwardExit) { fadeOut(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.FadeOut),
        targetAlpha = 0f,
    ) + slideOutHorizontally(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.RefinedAccelerate),
        targetOffsetX = { -(it / 6) },
    ) }

    fun detailBackEnter(): EnterTransition = cached(StaticMotionTransition.DetailBackEnter) { fadeIn(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.1f,
    ) + slideInHorizontally(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.RefinedDecelerate),
        initialOffsetX = { -(it / 6) },
    ) }

    fun detailBackExit(): ExitTransition = cached(StaticMotionTransition.DetailBackExit) { fadeOut(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.FadeOut),
        targetAlpha = 0f,
    ) + slideOutHorizontally(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.RefinedAccelerate),
        targetOffsetX = { it / 5 },
    ) }

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

    fun albumDetailForwardExit(): ExitTransition = cached(StaticMotionTransition.AlbumDetailForwardExit) { fadeOut(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.FadeOut),
    ) + scaleOut(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.RefinedAccelerate),
        targetScale = 0.994f,
    ) }

    fun albumDetailBackEnter(): EnterTransition = cached(StaticMotionTransition.AlbumDetailBackEnter) { fadeIn(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.FadeIn),
        initialAlpha = 0.08f,
    ) + scaleIn(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.RefinedDecelerate),
        initialScale = 0.994f,
    ) }

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

    private companion object {
        const val DEFAULT_OVERLAY_ENTER_ALPHA = 0.78f
        const val DEFAULT_OVERLAY_EXIT_ALPHA = 0.92f
        val DEFAULT_QUEUE_ORIGIN = TransformOrigin(1f, 1f)
    }
}

internal enum class StaticMotionTransition {
    OverlayFadeEnter,
    OverlayFadeExit,
    StandardEnter,
    StandardExit,
    PlayerOverlayEnter,
    PlayerOverlayExit,
    CompactBarReturnEnter,
    CompactBarEnter,
    CompactBarExit,
    SoftContentTransform,
    QuickContentSwapTransform,
    FullScreenForwardEnter,
    FullScreenForwardExit,
    FullScreenBackEnter,
    FullScreenBackExit,
    TopLevelEnter,
    TopLevelExit,
    DetailForwardEnter,
    DetailForwardExit,
    DetailBackEnter,
    DetailBackExit,
    QueueMenuEnter,
    QueueMenuExit,
    AlbumDetailForwardExit,
    AlbumDetailBackEnter,
}

internal object MotionTransitionDebugCounters {
    private val constructionCounts = IntArray(StaticMotionTransition.entries.size)

    fun recordConstruction(transition: StaticMotionTransition) {
        if (BuildConfig.DEBUG) constructionCounts[transition.ordinal] += 1
    }

    fun constructionCount(transition: StaticMotionTransition): Int {
        return if (BuildConfig.DEBUG) constructionCounts[transition.ordinal] else 0
    }
}

@Composable
fun rememberMotionTransitions(): MotionTransitions {
    val specs = rememberMotionSpecs()
    return remember(specs) { MotionTransitions(specs) }
}
