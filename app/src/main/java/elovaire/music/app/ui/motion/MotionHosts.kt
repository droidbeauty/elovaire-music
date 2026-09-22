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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.performance.MotionDiagnosticRecorder

@Composable
fun MotionVisibilityHost(
    visible: Boolean,
    surfaceId: String,
    enter: EnterTransition,
    exit: ExitTransition,
    label: String = surfaceId,
    modifier: Modifier = Modifier,
    onExitFinished: (() -> Unit)? = null,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val state = remember { MutableTransitionState(false) }
    val exitCallbackGate = remember { MotionExitCallbackGate() }
    val motionObservation = remember(surfaceId) {
        if (BuildConfig.DEBUG) MotionVisibilityObservation(surfaceId) else null
    }
    val currentOnExitFinished by rememberUpdatedState(onExitFinished)
    SideEffect {
        exitCallbackGate.onVisibilityTargetChanged(visible)
        motionObservation?.onTargetChanged(visible)
        state.targetState = visible
    }
    AnimatedVisibility(
        visibleState = state,
        modifier = modifier,
        enter = enter,
        exit = exit,
        label = label,
        content = content,
    )
    LaunchedEffect(visible, state.currentState, state.targetState, state.isIdle) {
        exitCallbackGate.onCurrentStateChanged(state.currentState)
        motionObservation?.onSettled(state.currentState, state.isIdle)
        if (
            state.isIdle &&
            !state.currentState &&
            !state.targetState &&
            exitCallbackGate.consumeFinishedExit()
        ) {
            currentOnExitFinished?.invoke()
        }
    }
}

/**
 * Hosts a card-style popup for its full visual lifetime. The logical [visible] target changes
 * immediately, while the content remains mounted until the exit transition completes. This
 * keeps popup dismissal reversible and prevents an exiting card from receiving input or focus.
 */
@Composable
fun PopupCardMotionHost(
    visible: Boolean,
    surfaceId: String,
    modifier: Modifier = Modifier,
    onExitFinished: (() -> Unit)? = null,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val transitions = rememberMotionTransitions()
    MotionVisibilityHost(
        visible = visible,
        surfaceId = surfaceId,
        enter = transitions.popupCardEnter(),
        exit = transitions.popupCardExit(),
        modifier = modifier
            .semantics {
                if (!visible) hideFromAccessibility()
            }
            .pointerInput(visible) {
                if (!visible) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent().changes.forEach { it.consume() }
                        }
                    }
                }
            },
        onExitFinished = onExitFinished,
        content = content,
    )
}

internal class MotionExitCallbackGate {
    private var hasEntered = false
    private var lastVisibilityTarget: Boolean? = null
    private var visibilityGeneration = 0L
    private var pendingExitGeneration: Long? = null

    fun onVisibilityTargetChanged(visible: Boolean) {
        if (lastVisibilityTarget == visible) return
        lastVisibilityTarget = visible
        visibilityGeneration += 1L
        if (visible) {
            // The content is mounted as soon as the target becomes visible. Track that
            // lifetime even when a zero-duration enter never publishes currentState=true.
            hasEntered = true
            pendingExitGeneration = null
        } else if (hasEntered) {
            pendingExitGeneration = visibilityGeneration
        }
    }

    fun onCurrentStateChanged(visible: Boolean) {
        if (visible) hasEntered = true
    }

    fun consumeFinishedExit(): Boolean {
        if (pendingExitGeneration != visibilityGeneration) return false
        pendingExitGeneration = null
        return true
    }
}

@Composable
fun PlayerOverlayMotionHost(
    visible: Boolean,
    surfaceId: String,
    onExitFinished: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val transitions = rememberMotionTransitions()
    MotionVisibilityHost(
        visible = visible,
        surfaceId = surfaceId,
        enter = transitions.playerOverlayEnter(),
        exit = transitions.playerOverlayExit(),
        label = surfaceId,
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
    MotionVisibilityHost(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        surfaceId = label,
        label = label,
        content = content,
    )
}

internal class MotionVisibilityObservation(private val surfaceId: String) {
    private var lastTarget: Boolean? = null
    private var settledTarget: Boolean? = null

    fun onTargetChanged(visible: Boolean) {
        if (lastTarget == visible) return
        val previous = lastTarget
        if (previous == null && !visible) {
            lastTarget = false
            settledTarget = false
            return
        }
        val reversed = previous != null && settledTarget != previous
        MotionDiagnosticRecorder.recordMotion(
            surfaceId = surfaceId,
            phase = if (reversed) "motion_reversed" else if (visible) "motion_enter" else "motion_exit",
            visible = visible,
        )
        lastTarget = visible
    }

    fun onSettled(visible: Boolean, isIdle: Boolean) {
        if (!isIdle || settledTarget == visible) return
        settledTarget = visible
        MotionDiagnosticRecorder.recordMotion(surfaceId, "motion_settled", visible)
    }
}

@Composable
fun <S> ElovaireAnimatedContent(
    targetState: S,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<S>.() -> ContentTransform,
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
fun <S> ElovaireAnimatedContent(
    targetState: S,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    contentKey: (targetState: S) -> Any? = { it },
    label: String,
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    val transitions = rememberMotionTransitions()
    ElovaireAnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = { transitions.softContentTransform() },
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
    animationSpec: FiniteAnimationSpec<Float>? = null,
    label: String,
    content: @Composable (targetState: S) -> Unit,
) {
    val specs = rememberMotionSpecs()
    androidx.compose.animation.Crossfade(
        targetState = targetState,
        modifier = modifier,
        animationSpec = animationSpec ?: specs.fadeIn(),
        label = label,
        content = content,
    )
}
