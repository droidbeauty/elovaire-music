package elovaire.music.droidbeauty.app.ui.interaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion

@Composable
fun PlayerOverlayHost(
    visible: Boolean,
    onExitFinished: () -> Unit,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn(
        animationSpec = ElovaireMotion.standardTween(
            durationMillis = 220,
            easing = ElovaireMotion.FadeIn,
        ),
    ) + scaleIn(
        initialScale = 0.995f,
        animationSpec = ElovaireMotion.standardTween(
            durationMillis = 260,
            easing = ElovaireMotion.RefinedDecelerate,
        ),
    ),
    exit: ExitTransition = fadeOut(
        animationSpec = ElovaireMotion.standardTween(
            durationMillis = 180,
            easing = ElovaireMotion.FadeOut,
        ),
    ) + scaleOut(
        targetScale = 0.992f,
        animationSpec = ElovaireMotion.standardTween(
            durationMillis = 180,
            easing = ElovaireMotion.RefinedAccelerate,
        ),
    ),
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val state = remember { MutableTransitionState(false) }
    state.targetState = visible

    AnimatedVisibility(
        visibleState = state,
        enter = enter,
        exit = exit,
        modifier = modifier,
        content = content,
    )

    LaunchedEffect(state.currentState, state.targetState, state.isIdle) {
        if (state.isIdle && !state.currentState && !state.targetState) {
            onExitFinished()
        }
    }
}
