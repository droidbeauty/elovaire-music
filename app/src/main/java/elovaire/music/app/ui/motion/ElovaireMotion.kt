package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import java.util.LinkedHashMap

object ElovaireMotion {
    private const val QuickBase = MotionDuration.Quick
    private const val FastBase = MotionDuration.Fast
    private const val StandardBase = MotionDuration.Standard
    private const val MediumBase = MotionDuration.Medium
    private const val SpaciousBase = MotionDuration.Spacious
    private const val ScreenBase = MotionDuration.Screen
    private const val ScreenFadeBase = MotionDuration.ScreenFade
    private const val ScreenSlideBase = MotionDuration.ScreenSlide
    private const val ScreenExpandBase = MotionDuration.ScreenExpand
    private const val PlayerScreenBase = MotionDuration.Player
    private const val ControlsBase = 120
    private const val ChromeResizeBase = 180
    private const val MicroBase = MotionDuration.Micro
    private const val ComponentBase = MotionDuration.Component
    private const val TopBarActionExitBase = MotionDuration.TopBarActionExit
    private const val TopBarActionEnterBase = MotionDuration.TopBarActionEnter
    private const val EmphasizedBase = MotionDuration.Emphasized
    private const val FullScreenEnterBase = MotionDuration.FullScreenEnter
    private const val FullScreenExitBase = MotionDuration.FullScreenExit
    private const val QueueMenuEnterBase = MotionDuration.QueueMenuEnter
    private const val ListPlacementBase = MotionDuration.ListPlacement

    private val tweenSpecs = BoundedSpecCache<LegacyTweenKey, FiniteAnimationSpec<*>>(MAX_CACHED_SPECS)
    private val springSpecs = BoundedSpecCache<LegacySpringKey, FiniteAnimationSpec<*>>(MAX_CACHED_SPECS)

    val Quick: Int get() = scaledDurationMillis(QuickBase)
    val Fast: Int get() = scaledDurationMillis(FastBase)
    val Standard: Int get() = scaledDurationMillis(StandardBase)
    val Medium: Int get() = scaledDurationMillis(MediumBase)
    val Screen: Int get() = scaledDurationMillis(ScreenBase)
    val PlayerScreen: Int get() = scaledDurationMillis(PlayerScreenBase)

    val SoftOut: Easing = MotionEasing.SoftOut
    val FadeIn: Easing = MotionEasing.FadeIn
    val FadeOut: Easing = MotionEasing.FadeOut
    val EmphasizedDecelerate: Easing = MotionEasing.EmphasizedDecelerate
    val EmphasizedAccelerate: Easing = MotionEasing.EmphasizedAccelerate
    val RefinedDecelerate: Easing = MotionEasing.RefinedDecelerate
    val RefinedAccelerate: Easing = MotionEasing.RefinedAccelerate
    val GentleDecelerate: Easing = RefinedDecelerate
    val GentleAccelerate: Easing = RefinedAccelerate

    private fun scaledDurationMillis(durationMillis: Int): Int = when {
        durationMillis <= 0 -> 0
        else -> durationMillis
    }

    private fun scaledDelayMillis(delayMillis: Int): Int = when {
        delayMillis <= 0 -> 0
        else -> delayMillis
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> scaledTween(
        durationMillis: Int,
        delayMillis: Int = 0,
        easing: Easing = SoftOut,
    ): FiniteAnimationSpec<T> {
        val key = LegacyTweenKey(
            durationMillis = scaledDurationMillis(durationMillis),
            delayMillis = scaledDelayMillis(delayMillis),
            easing = easing,
        )
        return tweenSpecs.getOrCreate(key) {
            tween<Any?>(
                durationMillis = key.durationMillis,
                delayMillis = key.delayMillis,
                easing = key.easing,
            )
        } as FiniteAnimationSpec<T>
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> scaledSpring(
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float,
    ): FiniteAnimationSpec<T> {
        val key = LegacySpringKey(dampingRatio, stiffness)
        return springSpecs.getOrCreate(key) {
            spring<Any?>(
                dampingRatio = key.dampingRatio,
                stiffness = key.stiffness,
            )
        } as FiniteAnimationSpec<T>
    }

    fun <T> fadeFast(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = QuickBase,
        easing = FadeOut,
    )

    fun <T> fadeMedium(delayMillis: Int = 0): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = ScreenFadeBase,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> fadeSlow(delayMillis: Int = 0): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = SpaciousBase,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> scaleSoft(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = StandardBase,
        easing = SoftOut,
    )

    fun <T> offsetSoft(
        durationMillis: Int = ScreenSlideBase,
        delayMillis: Int = 0,
    ): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = SoftOut,
    )

    fun <T> sizeSoft(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = ChromeResizeBase,
        easing = RefinedDecelerate,
    )

    fun <T> standardTween(
        durationMillis: Int = StandardBase,
        delayMillis: Int = 0,
        easing: Easing = SoftOut,
    ): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = easing,
    )

    fun <T> colorFadeSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = ControlsBase,
        easing = SoftOut,
    )

    fun <T> contentFadeInSpec(delayMillis: Int = 0): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = StandardBase,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> contentFadeOutSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = QuickBase,
        easing = FadeOut,
    )

    fun <T> pressDownSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = MicroBase,
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
        durationMillis = ScreenFadeBase,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> iconSwapOutSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = QuickBase,
        easing = FadeOut,
    )

    fun <T> emphasizedEnterSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = ScreenExpandBase,
        easing = EmphasizedDecelerate,
    )

    fun <T> queueMenuEnterSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = QueueMenuEnterBase,
        easing = RefinedDecelerate,
    )

    fun <T> listPlacementSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = ListPlacementBase,
        easing = RefinedDecelerate,
    )

    fun <T> titleSwapInSpec(delayMillis: Int = 32): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = ComponentBase,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> titleSwapOutSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = FastBase,
        easing = FadeOut,
    )

    fun titleSwapTransform(): ContentTransform =
        fadeIn(animationSpec = titleSwapInSpec()) togetherWith
            fadeOut(animationSpec = titleSwapOutSpec())

    fun quickContentSwapTransform(): ContentTransform =
        fadeIn(animationSpec = scaledTween(durationMillis = ComponentBase, easing = FadeIn)) togetherWith
            fadeOut(animationSpec = scaledTween(durationMillis = FastBase, easing = FadeOut))

    fun softContentTransform(): ContentTransform =
        (fadeIn(animationSpec = contentFadeInSpec()) +
            slideInVertically(
                animationSpec = offsetSoft(durationMillis = StandardBase),
                initialOffsetY = { -it / 10 },
            )) togetherWith fadeOut(animationSpec = contentFadeOutSpec())

    fun sharedTopBarTransform(): ContentTransform =
        (fadeIn(animationSpec = fadeMedium()) +
            slideInVertically(
                animationSpec = offsetSoft(durationMillis = ScreenFadeBase),
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
        (fadeIn(animationSpec = scaledTween(durationMillis = ComponentBase, easing = FadeIn)) +
            scaleIn(
                animationSpec = scaledTween(durationMillis = ComponentBase, easing = GentleDecelerate),
                initialScale = 0.96f,
                transformOrigin = TransformOrigin.Center,
            )) togetherWith
            (fadeOut(animationSpec = scaledTween(durationMillis = QuickBase, easing = FadeOut)) +
                scaleOut(
                    animationSpec = scaledTween(durationMillis = QuickBase, easing = GentleAccelerate),
                    targetScale = 0.98f,
                    transformOrigin = TransformOrigin.Center,
                ))

    fun topBarTextForwardTransform(): ContentTransform =
        (fadeIn(
            animationSpec = scaledTween(durationMillis = ComponentBase, delayMillis = 18, easing = FadeIn),
            initialAlpha = 0.72f,
        ) + slideInHorizontally(
            animationSpec = scaledTween(durationMillis = ComponentBase, easing = GentleDecelerate),
            initialOffsetX = { it / 18 },
        )) togetherWith (fadeOut(
            animationSpec = scaledTween(durationMillis = FastBase, easing = FadeOut),
            targetAlpha = 0.9f,
        ) + slideOutHorizontally(
            animationSpec = scaledTween(durationMillis = FastBase, easing = GentleAccelerate),
            targetOffsetX = { -(it / 24) },
        ))

    fun topBarTextBackTransform(): ContentTransform =
        (fadeIn(
            animationSpec = scaledTween(durationMillis = ComponentBase, easing = FadeIn),
            initialAlpha = 0.78f,
        ) + slideInHorizontally(
            animationSpec = scaledTween(durationMillis = ComponentBase, easing = GentleDecelerate),
            initialOffsetX = { -(it / 22) },
        )) togetherWith (fadeOut(
            animationSpec = scaledTween(durationMillis = FastBase, easing = FadeOut),
            targetAlpha = 0.92f,
        ) + slideOutHorizontally(
            animationSpec = scaledTween(durationMillis = FastBase, easing = GentleAccelerate),
            targetOffsetX = { it / 28 },
        ))

    fun topBarActionSwapTransform(): ContentTransform =
        fadeIn(
            animationSpec = scaledTween(
                durationMillis = TopBarActionEnterBase,
                delayMillis = TopBarActionExitBase,
                easing = FadeIn,
            ),
            initialAlpha = 0f,
        ) togetherWith fadeOut(
            animationSpec = scaledTween(
                durationMillis = TopBarActionExitBase,
                easing = FadeOut,
            ),
            targetAlpha = 0f,
        )

    fun fullScreenForwardEnter(
        initialOffsetX: (fullWidth: Int) -> Int = { it / 64 },
    ): EnterTransition = fadeIn(
        animationSpec = scaledTween(
            durationMillis = FullScreenEnterBase,
            easing = FadeIn,
        ),
        initialAlpha = 0.01f,
    ) +
        slideInHorizontally(
            animationSpec = scaledTween(
                durationMillis = FullScreenEnterBase,
                easing = RefinedDecelerate,
            ),
            initialOffsetX = initialOffsetX,
        )

    fun fullScreenForwardExit(
        targetOffsetX: (fullWidth: Int) -> Int = { -(it / 96) },
    ): ExitTransition = fadeOut(
        animationSpec = scaledTween(
            durationMillis = FullScreenExitBase,
            easing = FadeOut,
        ),
        targetAlpha = 0f,
    ) +
        slideOutHorizontally(
            animationSpec = scaledTween(
                durationMillis = FullScreenExitBase,
                easing = RefinedAccelerate,
            ),
            targetOffsetX = targetOffsetX,
        )

    fun fullScreenBackEnter(
        initialOffsetX: (fullWidth: Int) -> Int = { -(it / 96) },
    ): EnterTransition = fadeIn(
        animationSpec = scaledTween(
            durationMillis = FullScreenEnterBase,
            easing = FadeIn,
        ),
        initialAlpha = 0.08f,
    ) +
        slideInHorizontally(
            animationSpec = scaledTween(
                durationMillis = FullScreenEnterBase,
                easing = RefinedDecelerate,
            ),
            initialOffsetX = initialOffsetX,
        )

    fun fullScreenBackExit(
        targetOffsetX: (fullWidth: Int) -> Int = { it / 72 },
    ): ExitTransition = fadeOut(
        animationSpec = scaledTween(
            durationMillis = FullScreenExitBase,
            easing = FadeOut,
        ),
        targetAlpha = 0f,
    ) +
        slideOutHorizontally(
            animationSpec = scaledTween(
                durationMillis = FullScreenExitBase,
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

private class BoundedSpecCache<K, V>(
    private val maxEntries: Int,
) {
    private val values = LinkedHashMap<K, V>(maxEntries, 0.75f, true)

    @Synchronized
    fun getOrCreate(key: K, create: () -> V): V {
        values[key]?.let { return it }
        return create().also {
            values[key] = it
            while (values.size > maxEntries) {
                values.entries.iterator().apply {
                    if (hasNext()) {
                        next()
                        remove()
                    }
                }
            }
        }
    }
}

private const val MAX_CACHED_SPECS = 32

private data class LegacyTweenKey(
    val durationMillis: Int,
    val delayMillis: Int,
    val easing: Easing,
)

private data class LegacySpringKey(
    val dampingRatio: Float,
    val stiffness: Float,
)
