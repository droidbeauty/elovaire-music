package elovaire.music.droidbeauty.app.ui.motion

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import kotlin.math.roundToInt

object ElovaireMotion {
    private const val QuickBase = 75
    private const val FastBase = 100
    private const val StandardBase = 150
    private const val MediumBase = 160
    private const val SpaciousBase = 200
    private const val ScreenBase = 160
    private const val ScreenFadeBase = 100
    private const val ScreenSlideBase = 200
    private const val ScreenExpandBase = 250
    private const val PlayerScreenBase = 300
    private const val PlayerFadeBase = 90
    private const val ControlsBase = 100
    private const val ChromeResizeBase = 100
    private const val MicroBase = 70
    private const val ComponentBase = 150
    private const val EmphasizedBase = 250
    private const val TopLevelEnterBase = 100
    private const val TopLevelExitBase = 100
    private const val DetailEnterBase = 250
    private const val DetailExitBase = 200
    private const val AlbumDetailTransitionBase = 450
    private const val FullScreenEnterBase = 200
    private const val FullScreenExitBase = 150

    val Quick: Int get() = scaledDurationMillis(QuickBase)
    val Fast: Int get() = scaledDurationMillis(FastBase)
    val Standard: Int get() = scaledDurationMillis(StandardBase)
    val Medium: Int get() = scaledDurationMillis(MediumBase)
    val Spacious: Int get() = scaledDurationMillis(SpaciousBase)
    val Screen: Int get() = scaledDurationMillis(ScreenBase)
    val ScreenFade: Int get() = scaledDurationMillis(ScreenFadeBase)
    val ScreenSlide: Int get() = scaledDurationMillis(ScreenSlideBase)
    val ScreenExpand: Int get() = scaledDurationMillis(ScreenExpandBase)
    val PlayerScreen: Int get() = scaledDurationMillis(PlayerScreenBase)
    val PlayerFade: Int get() = scaledDurationMillis(PlayerFadeBase)
    val Controls: Int get() = scaledDurationMillis(ControlsBase)
    val ChromeResize: Int get() = scaledDurationMillis(ChromeResizeBase)
    val Micro: Int get() = scaledDurationMillis(MicroBase)
    val Component: Int get() = scaledDurationMillis(ComponentBase)
    val Emphasized: Int get() = scaledDurationMillis(EmphasizedBase)

    val SoftOut: Easing = FastOutSlowInEasing
    val FadeIn: Easing = LinearOutSlowInEasing
    val FadeOut: Easing = FastOutLinearInEasing
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val GentleDecelerate: Easing = CubicBezierEasing(0.16f, 0.88f, 0.22f, 1f)
    val GentleAccelerate: Easing = CubicBezierEasing(0.4f, 0f, 0.82f, 0.18f)

    private var systemDurationScale by mutableFloatStateOf(1f)

    fun updateSystemDurationScale(scale: Float) {
        systemDurationScale = scale.coerceAtLeast(0f)
    }

    private fun scaledDurationMillis(durationMillis: Int): Int = when {
        durationMillis <= 0 -> 0
        systemDurationScale <= 0f -> 0
        else -> (durationMillis * systemDurationScale).roundToInt().coerceAtLeast(1)
    }

    private fun scaledDelayMillis(delayMillis: Int): Int = when {
        delayMillis <= 0 -> 0
        systemDurationScale <= 0f -> 0
        else -> (delayMillis * systemDurationScale).roundToInt().coerceAtLeast(0)
    }

    private fun scaledSpringStiffness(stiffness: Float): Float {
        if (systemDurationScale <= 0f) return stiffness
        return (stiffness / (systemDurationScale * systemDurationScale)).coerceIn(25f, 10_000f)
    }

    private fun <T> scaledTween(
        durationMillis: Int,
        delayMillis: Int = 0,
        easing: Easing = SoftOut,
    ): FiniteAnimationSpec<T> = tween(
        durationMillis = scaledDurationMillis(durationMillis),
        delayMillis = scaledDelayMillis(delayMillis),
        easing = easing,
    )

    private fun <T> scaledSpring(
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float,
    ): FiniteAnimationSpec<T> = if (systemDurationScale <= 0f) {
        tween(durationMillis = 0)
    } else {
        spring(
            dampingRatio = dampingRatio,
            stiffness = scaledSpringStiffness(stiffness),
        )
    }

    fun <T> fadeFast(): FiniteAnimationSpec<T> = tween(
        durationMillis = Quick,
        easing = FadeOut,
    )

    fun <T> fadeMedium(delayMillis: Int = 0): FiniteAnimationSpec<T> = tween(
        durationMillis = ScreenFade,
        delayMillis = scaledDelayMillis(delayMillis),
        easing = FadeIn,
    )

    fun <T> fadeSlow(delayMillis: Int = 0): FiniteAnimationSpec<T> = tween(
        durationMillis = Spacious,
        delayMillis = scaledDelayMillis(delayMillis),
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
        easing = SoftOut,
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

    fun <T> standardSpring(
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float = 520f,
    ): FiniteAnimationSpec<T> = scaledSpring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
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

    fun <T> emphasizedExitSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = ScreenFadeBase,
        easing = EmphasizedAccelerate,
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

    fun verticalRevealEnter(): EnterTransition = fadeIn(
        animationSpec = scaledTween(durationMillis = FastBase, easing = FadeIn),
    ) + androidx.compose.animation.expandVertically(
        animationSpec = scaledTween(durationMillis = StandardBase, easing = GentleDecelerate),
    )

    fun verticalRevealExit(): ExitTransition = fadeOut(
        animationSpec = scaledTween(durationMillis = QuickBase, easing = FadeOut),
    ) + androidx.compose.animation.shrinkVertically(
        animationSpec = scaledTween(durationMillis = ComponentBase, easing = GentleAccelerate),
    )

    fun contextMenuEnter(
        verticalOffsetDivisor: Int = 6,
        transformOrigin: androidx.compose.ui.graphics.TransformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 0f),
    ): EnterTransition = fadeIn(
        animationSpec = scaledTween(durationMillis = ComponentBase, easing = FadeIn),
        initialAlpha = 0.72f,
    ) +
        scaleIn(
            initialScale = 0.96f,
            transformOrigin = transformOrigin,
            animationSpec = scaledTween(durationMillis = StandardBase, easing = GentleDecelerate),
        ) +
        slideInVertically(
            initialOffsetY = { -it / verticalOffsetDivisor },
            animationSpec = scaledTween(durationMillis = StandardBase, easing = GentleDecelerate),
        )

    fun contextMenuExit(
        transformOrigin: androidx.compose.ui.graphics.TransformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 0f),
    ): ExitTransition = fadeOut(
        animationSpec = scaledTween(durationMillis = FastBase, easing = FadeOut),
        targetAlpha = 0.92f,
    ) +
        scaleOut(
            targetScale = 0.985f,
            transformOrigin = transformOrigin,
            animationSpec = scaledTween(durationMillis = FastBase, easing = GentleAccelerate),
        )

    fun overlayFadeEnter(initialAlpha: Float = 0.78f): EnterTransition = fadeIn(
        animationSpec = scaledTween(durationMillis = StandardBase, easing = FadeIn),
        initialAlpha = initialAlpha,
    )

    fun overlayFadeExit(targetAlpha: Float = 0.92f): ExitTransition = fadeOut(
        animationSpec = scaledTween(durationMillis = FastBase, easing = FadeOut),
        targetAlpha = targetAlpha,
    )

    fun bottomSheetEnter(
        initialOffsetY: (Int) -> Int = { it / 5 },
    ): EnterTransition = overlayFadeEnter(initialAlpha = 0.74f) +
        slideInVertically(
            animationSpec = scaledTween(durationMillis = EmphasizedBase, easing = GentleDecelerate),
            initialOffsetY = initialOffsetY,
        ) +
        androidx.compose.animation.expandVertically(
            animationSpec = scaledTween(durationMillis = EmphasizedBase, easing = GentleDecelerate),
            expandFrom = Alignment.Bottom,
        )

    fun bottomSheetExit(
        targetOffsetY: (Int) -> Int = { it / 8 },
    ): ExitTransition = overlayFadeExit(targetAlpha = 0.9f) +
        slideOutVertically(
            animationSpec = scaledTween(durationMillis = FastBase, easing = GentleAccelerate),
            targetOffsetY = targetOffsetY,
        ) +
        androidx.compose.animation.shrinkVertically(
            animationSpec = scaledTween(durationMillis = FastBase, easing = GentleAccelerate),
            shrinkTowards = Alignment.Bottom,
        )

    fun bannerEnter(
        initialOffsetY: (Int) -> Int = { -(it / 2) },
    ): EnterTransition = overlayFadeEnter(initialAlpha = 0.82f) +
        slideInVertically(
            animationSpec = scaledTween(durationMillis = ScreenBase, easing = GentleDecelerate),
            initialOffsetY = initialOffsetY,
        )

    fun bannerExit(
        targetOffsetY: (Int) -> Int = { -(it / 3) },
    ): ExitTransition = overlayFadeExit(targetAlpha = 0.94f) +
        slideOutVertically(
            animationSpec = scaledTween(durationMillis = StandardBase, easing = GentleAccelerate),
            targetOffsetY = targetOffsetY,
        )

    fun compactBarEnter(
        initialOffsetY: (Int) -> Int = { it / 6 },
    ): EnterTransition = fadeIn(
        animationSpec = scaledTween(durationMillis = EmphasizedBase, easing = FadeIn),
        initialAlpha = 0.78f,
    ) +
        androidx.compose.animation.expandVertically(
            expandFrom = Alignment.Bottom,
            animationSpec = scaledTween(durationMillis = EmphasizedBase, easing = GentleDecelerate),
        ) +
        slideInVertically(
            initialOffsetY = initialOffsetY,
            animationSpec = scaledTween(durationMillis = EmphasizedBase, easing = GentleDecelerate),
        )

    fun compactBarExit(
        targetOffsetY: (Int) -> Int = { it / 10 },
    ): ExitTransition = fadeOut(
        animationSpec = scaledTween(durationMillis = StandardBase, easing = FadeOut),
        targetAlpha = 0.9f,
    ) +
        androidx.compose.animation.shrinkVertically(
            shrinkTowards = Alignment.Bottom,
            animationSpec = scaledTween(durationMillis = StandardBase, easing = GentleAccelerate),
        ) +
        slideOutVertically(
            targetOffsetY = targetOffsetY,
            animationSpec = scaledTween(durationMillis = StandardBase, easing = GentleAccelerate),
        )

    fun bottomBarEnter(
        initialOffsetY: (Int) -> Int = { it / 2 },
    ): EnterTransition = overlayFadeEnter(initialAlpha = 0.82f) +
        slideInVertically(
            animationSpec = scaledTween(durationMillis = StandardBase, easing = GentleDecelerate),
            initialOffsetY = initialOffsetY,
        )

    fun bottomBarExit(
        targetOffsetY: (Int) -> Int = { it / 2 },
    ): ExitTransition = overlayFadeExit(targetAlpha = 0.94f) +
        slideOutVertically(
            animationSpec = scaledTween(durationMillis = QuickBase, easing = GentleAccelerate),
            targetOffsetY = targetOffsetY,
        )

    fun standardEnter(
        delayMillis: Int = 0,
        initialOffsetY: (fullHeight: Int) -> Int = { it / 8 },
    ): EnterTransition = fadeIn(animationSpec = fadeMedium(delayMillis)) +
        slideInVertically(
            animationSpec = offsetSoft(durationMillis = ScreenSlideBase, delayMillis = delayMillis),
            initialOffsetY = initialOffsetY,
        )

    fun standardExit(
        targetOffsetY: (fullHeight: Int) -> Int = { it / 10 },
    ): ExitTransition = fadeOut(animationSpec = fadeFast()) +
        slideOutVertically(
            animationSpec = offsetSoft(durationMillis = FastBase),
            targetOffsetY = targetOffsetY,
        )

    fun emphasizedEnter(
        delayMillis: Int = 0,
    ): EnterTransition = fadeIn(animationSpec = contentFadeInSpec(delayMillis)) +
        scaleIn(
            animationSpec = emphasizedEnterSpec(),
            initialScale = 0.985f,
        )

    fun emphasizedExit(): ExitTransition = fadeOut(animationSpec = emphasizedExitSpec()) +
        scaleOut(
            animationSpec = emphasizedExitSpec(),
            targetScale = 0.99f,
        )

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
        (fadeIn(
            animationSpec = fadeMedium(),
            initialAlpha = 0.9f,
        ) + slideInHorizontally(
            animationSpec = offsetSoft(durationMillis = ScreenFadeBase),
            initialOffsetX = { it / 8 },
        )) togetherWith (fadeOut(
            animationSpec = fadeFast(),
            targetAlpha = 0.92f,
        ) + slideOutHorizontally(
            animationSpec = offsetSoft(durationMillis = ScreenFadeBase),
            targetOffsetX = { -(it / 10) },
        ))

    fun sharedTopBarBackTransform(): ContentTransform =
        (fadeIn(
            animationSpec = fadeMedium(),
            initialAlpha = 0.94f,
        ) + slideInHorizontally(
            animationSpec = offsetSoft(durationMillis = ScreenFadeBase),
            initialOffsetX = { -(it / 10) },
        )) togetherWith (fadeOut(
            animationSpec = fadeFast(),
            targetAlpha = 0.96f,
        ) + slideOutHorizontally(
            animationSpec = offsetSoft(durationMillis = ScreenFadeBase),
            targetOffsetX = { it / 8 },
        ))

    fun fullScreenForwardEnter(
        initialOffsetX: (fullWidth: Int) -> Int = { it / 80 },
    ): EnterTransition = fadeIn(
        animationSpec = scaledTween(
            durationMillis = FullScreenEnterBase,
            easing = FadeIn,
        ),
        initialAlpha = 0.04f,
    ) +
        scaleIn(
            animationSpec = scaledTween(
                durationMillis = FullScreenEnterBase,
                easing = GentleDecelerate,
            ),
            initialScale = 0.992f,
        ) +
        slideInHorizontally(
            animationSpec = scaledTween(
                durationMillis = FullScreenEnterBase,
                easing = GentleDecelerate,
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
        scaleOut(
            animationSpec = scaledTween(
                durationMillis = FullScreenExitBase,
                easing = GentleAccelerate,
            ),
            targetScale = 0.997f,
        ) +
        slideOutHorizontally(
            animationSpec = scaledTween(
                durationMillis = FullScreenExitBase,
                easing = GentleAccelerate,
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
        scaleIn(
            animationSpec = scaledTween(
                durationMillis = FullScreenEnterBase,
                easing = GentleDecelerate,
            ),
            initialScale = 0.997f,
        ) +
        slideInHorizontally(
            animationSpec = scaledTween(
                durationMillis = FullScreenEnterBase,
                easing = GentleDecelerate,
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
        scaleOut(
            animationSpec = scaledTween(
                durationMillis = FullScreenExitBase,
                easing = GentleAccelerate,
            ),
            targetScale = 0.992f,
        ) +
        slideOutHorizontally(
            animationSpec = scaledTween(
                durationMillis = FullScreenExitBase,
                easing = GentleAccelerate,
            ),
            targetOffsetX = targetOffsetX,
        )

    fun topLevelEnter(
        forward: Boolean = true,
    ): EnterTransition = fadeIn(
        animationSpec = scaledTween(
            durationMillis = TopLevelEnterBase,
            easing = FadeIn,
        ),
        initialAlpha = 0f,
    ) +
        scaleIn(
            animationSpec = scaledTween(
                durationMillis = TopLevelEnterBase,
                easing = GentleDecelerate,
            ),
            initialScale = 0.988f,
        )

    fun topLevelExit(
        forward: Boolean = true,
    ): ExitTransition = fadeOut(
        animationSpec = scaledTween(
            durationMillis = TopLevelExitBase,
            easing = FadeOut,
        ),
        targetAlpha = 0f,
    ) +
        scaleOut(
            animationSpec = scaledTween(
                durationMillis = TopLevelExitBase,
                easing = GentleAccelerate,
            ),
            targetScale = 0.996f,
        )

    fun detailForwardEnter(): EnterTransition = fadeIn(
        animationSpec = scaledTween(
            durationMillis = DetailEnterBase,
            easing = FadeIn,
        ),
        initialAlpha = 0.08f,
    ) +
        scaleIn(
            animationSpec = scaledTween(
                durationMillis = DetailEnterBase,
                easing = GentleDecelerate,
            ),
            initialScale = 0.99f,
        ) +
        slideInVertically(
            animationSpec = scaledTween(
                durationMillis = DetailEnterBase,
                easing = GentleDecelerate,
            ),
            initialOffsetY = { it / 96 },
        )

    fun detailForwardExit(): ExitTransition = fadeOut(
        animationSpec = scaledTween(
            durationMillis = DetailExitBase,
            easing = FadeOut,
        ),
        targetAlpha = 0f,
    ) +
        scaleOut(
            animationSpec = scaledTween(
                durationMillis = DetailExitBase,
                easing = GentleAccelerate,
            ),
            targetScale = 0.998f,
        )

    fun detailBackEnter(): EnterTransition = fadeIn(
        animationSpec = scaledTween(
            durationMillis = DetailEnterBase,
            easing = FadeIn,
        ),
        initialAlpha = 0.1f,
    ) +
        scaleIn(
            animationSpec = scaledTween(
                durationMillis = DetailEnterBase,
                easing = GentleDecelerate,
            ),
            initialScale = 0.998f,
        )

    fun detailBackExit(): ExitTransition = fadeOut(
        animationSpec = scaledTween(
            durationMillis = DetailExitBase,
            easing = FadeOut,
        ),
        targetAlpha = 0f,
    ) +
        scaleOut(
            animationSpec = scaledTween(
                durationMillis = DetailExitBase,
                easing = GentleAccelerate,
            ),
            targetScale = 0.99f,
        ) +
        slideOutVertically(
            animationSpec = scaledTween(
                durationMillis = DetailExitBase,
                easing = GentleAccelerate,
            ),
            targetOffsetY = { it / 96 },
        )

    fun albumDetailForwardEnter(
        transformOrigin: TransformOrigin = TransformOrigin.Center,
    ): EnterTransition = fadeIn(
        animationSpec = scaledTween(
            durationMillis = AlbumDetailTransitionBase,
            easing = FadeIn,
        ),
        initialAlpha = 0.04f,
    ) +
        scaleIn(
            animationSpec = scaledTween(
                durationMillis = AlbumDetailTransitionBase,
                easing = GentleDecelerate,
            ),
            initialScale = 0.76f,
            transformOrigin = transformOrigin,
        ) +
        slideInHorizontally(
            animationSpec = scaledTween(
                durationMillis = AlbumDetailTransitionBase,
                easing = GentleDecelerate,
            ),
            initialOffsetX = { fullWidth ->
                ((transformOrigin.pivotFractionX - 0.5f) * fullWidth * 0.24f).roundToInt()
            },
        ) +
        slideInVertically(
            animationSpec = scaledTween(
                durationMillis = AlbumDetailTransitionBase,
                easing = GentleDecelerate,
            ),
            initialOffsetY = { fullHeight ->
                ((transformOrigin.pivotFractionY - 0.5f) * fullHeight * 0.24f).roundToInt()
            },
        )

    fun albumDetailForwardExit(): ExitTransition = fadeOut(
        animationSpec = scaledTween(
            durationMillis = DetailExitBase,
            easing = FadeOut,
        ),
        targetAlpha = 0f,
    ) +
        scaleOut(
            animationSpec = scaledTween(
                durationMillis = DetailExitBase,
                easing = GentleAccelerate,
            ),
            targetScale = 0.994f,
        )

    fun albumDetailBackEnter(): EnterTransition = fadeIn(
        animationSpec = scaledTween(
            durationMillis = DetailExitBase,
            easing = FadeIn,
        ),
        initialAlpha = 0.08f,
    ) +
        scaleIn(
            animationSpec = scaledTween(
                durationMillis = DetailExitBase,
                easing = GentleDecelerate,
            ),
            initialScale = 0.994f,
        )

    fun albumDetailBackExit(
        transformOrigin: TransformOrigin = TransformOrigin.Center,
    ): ExitTransition = fadeOut(
        animationSpec = scaledTween(
            durationMillis = AlbumDetailTransitionBase,
            easing = FadeOut,
        ),
        targetAlpha = 0f,
    ) +
        scaleOut(
            animationSpec = scaledTween(
                durationMillis = AlbumDetailTransitionBase,
                easing = GentleAccelerate,
            ),
            targetScale = 0.82f,
            transformOrigin = transformOrigin,
        ) +
        slideOutHorizontally(
            animationSpec = scaledTween(
                durationMillis = AlbumDetailTransitionBase,
                easing = GentleAccelerate,
            ),
            targetOffsetX = { fullWidth ->
                ((transformOrigin.pivotFractionX - 0.5f) * fullWidth * 0.24f).roundToInt()
            },
        ) +
        slideOutVertically(
            animationSpec = scaledTween(
                durationMillis = AlbumDetailTransitionBase,
                easing = GentleAccelerate,
            ),
            targetOffsetY = { fullHeight ->
                ((transformOrigin.pivotFractionY - 0.5f) * fullHeight * 0.24f).roundToInt()
            },
        )

    fun scaleDurationMillis(
        durationMillis: Long,
        durationScale: Float,
    ): Long = when {
        durationMillis <= 0L -> 0L
        durationScale <= 0f -> 0L
        else -> (durationMillis * durationScale).roundToInt().toLong().coerceAtLeast(1L)
    }

    fun scaleDurationMillis(
        durationMillis: Int,
        durationScale: Float,
    ): Long = scaleDurationMillis(durationMillis.toLong(), durationScale)
}

object ElovaireAlbumMotion {
    fun forwardEnter(transformOrigin: TransformOrigin): EnterTransition =
        ElovaireMotion.albumDetailForwardEnter(transformOrigin)

    fun forwardExit(): ExitTransition = ElovaireMotion.albumDetailForwardExit()

    fun backEnter(): EnterTransition = ElovaireMotion.albumDetailBackEnter()

    fun backExit(transformOrigin: TransformOrigin): ExitTransition =
        ElovaireMotion.albumDetailBackExit(transformOrigin)
}

@Composable
fun rememberSystemAnimationScale(): Float {
    val context = LocalContext.current
    val resolver = context.contentResolver
    var scale by remember {
        mutableFloatStateOf(
            runCatching {
                Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
            }.getOrDefault(1f).coerceAtLeast(0f),
        )
    }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                scale = runCatching {
                    Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
                }.getOrDefault(1f).coerceAtLeast(0f)
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose { runCatching { resolver.unregisterContentObserver(observer) } }
    }
    return scale
}

@Composable
fun SyncElovaireMotionScale() {
    val scale = rememberSystemAnimationScale()
    SideEffect {
        ElovaireMotion.updateSystemDurationScale(scale)
    }
}

@Composable
fun ElovaireAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = ElovaireMotion.standardEnter(),
    exit: ExitTransition = ElovaireMotion.standardExit(),
    label: String,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        label = label,
        content = content,
    )
}

@Composable
fun <S> ElovaireAnimatedContent(
    targetState: S,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        ElovaireMotion.softContentTransform()
    },
    contentAlignment: Alignment = Alignment.TopStart,
    contentKey: (targetState: S) -> Any? = { it },
    label: String,
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = transitionSpec,
        contentAlignment = contentAlignment,
        contentKey = contentKey,
        label = label,
        content = content,
    )
}

@Composable
fun <S> ElovaireCrossfade(
    targetState: S,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = ElovaireMotion.fadeMedium(),
    label: String,
    content: @Composable (targetState: S) -> Unit,
) {
    androidx.compose.animation.Crossfade(
        targetState = targetState,
        modifier = modifier,
        animationSpec = animationSpec,
        label = label,
        content = content,
    )
}

fun Modifier.elovaireAnimateContentSize(): Modifier = animateContentSize(
    animationSpec = ElovaireMotion.sizeSoft(),
)
