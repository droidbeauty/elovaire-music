package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.TransformOrigin

@Stable
class MotionTransitions internal constructor(
    private val specs: MotionSpecs,
) {
    fun playerOverlayEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Screen,
            easing = MotionEasing.FadeIn,
        ),
    ) + scaleIn(
        initialScale = MotionScale.PlayerOverlayEnter,
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Spacious,
            easing = MotionEasing.RefinedDecelerate,
        ),
    )

    fun playerOverlayExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Standard,
            easing = MotionEasing.FadeOut,
        ),
    ) + scaleOut(
        targetScale = MotionScale.PlayerOverlayExit,
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Standard,
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
        return fadeIn(
            animationSpec = specs.tween(
                durationMillis = MotionDuration.Emphasized,
                easing = MotionEasing.FadeIn,
            ),
            initialAlpha = 0.68f,
        ) + slideInVertically(
            initialOffsetY = { it / 8 },
            animationSpec = specs.tween(
                durationMillis = MotionDuration.Emphasized,
                easing = MotionEasing.RefinedDecelerate,
            ),
        )
    }

    fun compactBarExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Standard,
            easing = MotionEasing.FadeOut,
        ),
        targetAlpha = 0.92f,
    ) + slideOutVertically(
        targetOffsetY = { it / 12 },
        animationSpec = specs.tween(
            durationMillis = MotionDuration.Standard,
            easing = MotionEasing.RefinedAccelerate,
        ),
    )

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
    ) + slideInHorizontally(
        initialOffsetX = { it / 14 },
        animationSpec = specs.tween(
            durationMillis = MotionDuration.QueueMenuEnter,
            easing = MotionEasing.RefinedDecelerate,
        ),
    ) + slideInVertically(
        initialOffsetY = { it / 9 },
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
    ) + slideOutVertically(
        targetOffsetY = { it / 12 },
        animationSpec = specs.tween(
            durationMillis = MotionDuration.QueueMenuExit,
            easing = MotionEasing.RefinedAccelerate,
        ),
    )

    fun fullScreenForwardEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.01f,
    ) + scaleIn(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.RefinedDecelerate),
        initialScale = 0.988f,
    ) + slideInHorizontally(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.RefinedDecelerate),
        initialOffsetX = { it / 64 },
    )

    fun fullScreenForwardExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.FadeOut),
    ) + scaleOut(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.RefinedAccelerate),
        targetScale = 0.996f,
    ) + slideOutHorizontally(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.RefinedAccelerate),
        targetOffsetX = { -(it / 96) },
    )

    fun fullScreenBackEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.08f,
    ) + scaleIn(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.RefinedDecelerate),
        initialScale = 0.997f,
    ) + slideInHorizontally(
        animationSpec = specs.tween(MotionDuration.FullScreenEnter, easing = MotionEasing.RefinedDecelerate),
        initialOffsetX = { -(it / 96) },
    )

    fun fullScreenBackExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.FadeOut),
    ) + scaleOut(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.RefinedAccelerate),
        targetScale = 0.992f,
    ) + slideOutHorizontally(
        animationSpec = specs.tween(MotionDuration.FullScreenExit, easing = MotionEasing.RefinedAccelerate),
        targetOffsetX = { it / 72 },
    )

    fun topLevelEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(MotionDuration.TopLevelEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.02f,
    ) + scaleIn(
        animationSpec = specs.tween(MotionDuration.TopLevelEnter, easing = MotionEasing.RefinedDecelerate),
        initialScale = 0.988f,
    )

    fun topLevelExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(MotionDuration.TopLevelExit, easing = MotionEasing.FadeOut),
    ) + scaleOut(
        animationSpec = specs.tween(MotionDuration.TopLevelExit, easing = MotionEasing.RefinedAccelerate),
        targetScale = 0.998f,
    )

    fun detailForwardEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.08f,
    ) + scaleIn(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.RefinedDecelerate),
        initialScale = 0.99f,
    ) + slideInVertically(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.RefinedDecelerate),
        initialOffsetY = { it / 96 },
    )

    fun detailForwardExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.FadeOut),
    ) + scaleOut(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.RefinedAccelerate),
        targetScale = 0.998f,
    )

    fun detailBackEnter(): EnterTransition = fadeIn(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.FadeIn),
        initialAlpha = 0.1f,
    ) + scaleIn(
        animationSpec = specs.tween(MotionDuration.DetailEnter, easing = MotionEasing.RefinedDecelerate),
        initialScale = 0.998f,
    )

    fun detailBackExit(): ExitTransition = fadeOut(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.FadeOut),
    ) + scaleOut(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.RefinedAccelerate),
        targetScale = 0.99f,
    ) + slideOutVertically(
        animationSpec = specs.tween(MotionDuration.DetailExit, easing = MotionEasing.RefinedAccelerate),
        targetOffsetY = { it / 96 },
    )
}

@Composable
fun rememberMotionTransitions(): MotionTransitions {
    val specs = rememberMotionSpecs()
    return remember(specs) { MotionTransitions(specs) }
}
