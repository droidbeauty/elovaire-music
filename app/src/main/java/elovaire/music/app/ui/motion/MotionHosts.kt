package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.FiniteAnimationSpec

@Composable
fun MotionVisibilityHost(
    visible: Boolean,
    enter: EnterTransition,
    exit: ExitTransition,
    modifier: Modifier = Modifier,
    onExitFinished: (() -> Unit)? = null,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val state = remember { MutableTransitionState(false) }
    state.targetState = visible
    AnimatedVisibility(
        visibleState = state,
        modifier = modifier,
        enter = enter,
        exit = exit,
        content = content,
    )
    LaunchedEffect(state.currentState, state.targetState, state.isIdle) {
        if (state.isIdle && !state.currentState && !state.targetState) {
            onExitFinished?.invoke()
        }
    }
}

@Composable
fun PlayerOverlayMotionHost(
    visible: Boolean,
    onExitFinished: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val transitions = rememberMotionTransitions()
    MotionVisibilityHost(
        visible = visible,
        enter = transitions.playerOverlayEnter(),
        exit = transitions.playerOverlayExit(),
        modifier = modifier,
        onExitFinished = onExitFinished,
        content = content,
    )
}

@Composable
fun ElovaireAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition,
    exit: ExitTransition,
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
