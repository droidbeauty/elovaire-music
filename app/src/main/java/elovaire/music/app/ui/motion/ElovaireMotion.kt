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
import androidx.compose.ui.graphics.TransformOrigin
import kotlin.math.roundToInt

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
    private const val PlayerFadeBase = MotionDuration.PlayerFade
    private const val ControlsBase = 120
    private const val ChromeResizeBase = 180
    private const val MicroBase = MotionDuration.Micro
    private const val ComponentBase = MotionDuration.Component
    private const val EmphasizedBase = MotionDuration.Emphasized
    private const val TopLevelEnterBase = MotionDuration.TopLevelEnter
    private const val TopLevelExitBase = MotionDuration.TopLevelExit
    private const val DetailEnterBase = MotionDuration.DetailEnter
    private const val DetailExitBase = MotionDuration.DetailExit
    private const val AlbumDetailTransitionBase = MotionDuration.AlbumDetail
    private const val FullScreenEnterBase = MotionDuration.FullScreenEnter
    private const val FullScreenExitBase = MotionDuration.FullScreenExit
    private const val QueueMenuEnterBase = MotionDuration.QueueMenuEnter
    private const val QueueMenuExitBase = MotionDuration.QueueMenuExit
    private const val ListRevealBase = MotionDuration.ListReveal
    private const val ListPlacementBase = MotionDuration.ListPlacement

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
    ): FiniteAnimationSpec<T> = spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
    )

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

    fun <T> emphasizedExitSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = ScreenFadeBase,
        easing = EmphasizedAccelerate,
    )

    fun <T> queueMenuEnterSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = QueueMenuEnterBase,
        easing = RefinedDecelerate,
    )

    fun <T> queueMenuExitSpec(): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = QueueMenuExitBase,
        easing = RefinedAccelerate,
    )

    fun <T> listRevealSpec(delayMillis: Int = 0): FiniteAnimationSpec<T> = scaledTween(
        durationMillis = ListRevealBase,
        delayMillis = delayMillis,
        easing = RefinedDecelerate,
    )

    fun listRevealDelay(index: Int): Int = scaledDelayMillis((index.coerceAtLeast(0) * 12).coerceAtMost(72))

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

    fun verticalRevealEnter(): EnterTransition = fadeIn(
        animationSpec = scaledTween(durationMillis = FastBase, easing = FadeIn),
    ) + slideInVertically(
        animationSpec = scaledTween(durationMillis = StandardBase, easing = GentleDecelerate),
        initialOffsetY = { -it / 10 },
    )

    fun verticalRevealExit(): ExitTransition = fadeOut(
        animationSpec = scaledTween(durationMillis = QuickBase, easing = FadeOut),
    ) + slideOutVertically(
        animationSpec = scaledTween(durationMillis = ComponentBase, easing = GentleAccelerate),
        targetOffsetY = { -it / 12 },
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
        )

    fun bottomSheetExit(
        targetOffsetY: (Int) -> Int = { it / 8 },
    ): ExitTransition = overlayFadeExit(targetAlpha = 0.9f) +
        slideOutVertically(
            animationSpec = scaledTween(durationMillis = FastBase, easing = GentleAccelerate),
            targetOffsetY = targetOffsetY,
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
        initialOffsetY: (Int) -> Int = { it / 8 },
    ): EnterTransition = compactBarNormalEnter(initialOffsetY)

    fun compactBarExit(
        targetOffsetY: (Int) -> Int = { it / 12 },
    ): ExitTransition = compactBarNormalExit(targetOffsetY)

    fun compactBarReturnEnter(): EnterTransition = fadeIn(
        animationSpec = scaledTween(durationMillis = PlayerFadeBase, easing = FadeIn),
        initialAlpha = 0.01f,
    )

    fun compactBarNormalEnter(
        initialOffsetY: (Int) -> Int = { it / 8 },
    ): EnterTransition = fadeIn(
        animationSpec = scaledTween(durationMillis = EmphasizedBase, easing = FadeIn),
        initialAlpha = 0.68f,
    ) +
        slideInVertically(
            initialOffsetY = initialOffsetY,
            animationSpec = scaledTween(durationMillis = EmphasizedBase, easing = RefinedDecelerate),
        )

    fun compactBarNormalExit(
        targetOffsetY: (Int) -> Int = { it / 12 },
    ): ExitTransition = fadeOut(
        animationSpec = scaledTween(durationMillis = StandardBase, easing = FadeOut),
        targetAlpha = 0.92f,
    ) +
        slideOutVertically(
            targetOffsetY = targetOffsetY,
            animationSpec = scaledTween(durationMillis = StandardBase, easing = RefinedAccelerate),
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

    fun topLevelEnter(
        forward: Boolean = true,
    ): EnterTransition = fadeIn(
        animationSpec = scaledTween(
            durationMillis = TopLevelEnterBase,
            easing = FadeIn,
        ),
        initialAlpha = 0.02f,
    ) +
        scaleIn(
            animationSpec = scaledTween(
                durationMillis = TopLevelEnterBase,
                easing = RefinedDecelerate,
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
                easing = RefinedAccelerate,
            ),
            targetScale = 0.998f,
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
            animationSpec = scaledSpring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            initialScale = 0.86f,
            transformOrigin = transformOrigin,
        ) +
        slideInHorizontally(
            animationSpec = scaledSpring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            initialOffsetX = { fullWidth ->
                ((transformOrigin.pivotFractionX - 0.5f) * fullWidth * 0.18f).roundToInt()
            },
        ) +
        slideInVertically(
            animationSpec = scaledSpring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            initialOffsetY = { fullHeight ->
                ((transformOrigin.pivotFractionY - 0.5f) * fullHeight * 0.18f).roundToInt()
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
            animationSpec = scaledSpring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            targetScale = 0.86f,
            transformOrigin = transformOrigin,
        ) +
        slideOutHorizontally(
            animationSpec = scaledSpring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            targetOffsetX = { fullWidth ->
                ((transformOrigin.pivotFractionX - 0.5f) * fullWidth * 0.18f).roundToInt()
            },
        ) +
        slideOutVertically(
            animationSpec = scaledSpring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            targetOffsetY = { fullHeight ->
                ((transformOrigin.pivotFractionY - 0.5f) * fullHeight * 0.18f).roundToInt()
            },
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
