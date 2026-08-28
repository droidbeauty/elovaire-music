package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

internal class RootPlayerLayerController(
    stateName: String,
    private val setStateName: (String) -> Unit,
    transitionSnapshot: NowPlayingTransitionSnapshot?,
    private val setTransitionSnapshot: (NowPlayingTransitionSnapshot?) -> Unit,
    private val currentSongPresent: Boolean,
    transitionGeneration: Long = 0L,
    private val setTransitionGeneration: (Long) -> Unit = {},
) {
    private var currentState: PlayerLayerState = stateName.toPlayerLayerStateOrDefault()
    private var currentTransitionGeneration: Long = transitionGeneration
    private var currentTransitionSnapshot: NowPlayingTransitionSnapshot? = transitionSnapshot

    val state: PlayerLayerState
        get() = currentState
    val transitionSnapshot: NowPlayingTransitionSnapshot?
        get() = currentTransitionSnapshot
    val transitionGeneration: Long
        get() = currentTransitionGeneration

    fun requestOpen(snapshot: NowPlayingTransitionSnapshot?) {
        if (!currentSongPresent || state == PlayerLayerState.Expanded) {
            return
        }
        advanceTransitionGeneration()
        updateState(PlayerLayerState.Expanded)
        updateTransitionSnapshot(snapshot)
    }

    fun hide(returnToCompact: Boolean) {
        if (state != PlayerLayerState.Compact) {
            advanceTransitionGeneration()
        }
        updateState(playerLayerStateAfterHide(returnToCompact, currentSongPresent))
    }

    fun resetIfSongMissing(currentSongPresent: Boolean) {
        if (!currentSongPresent) {
            if (state != PlayerLayerState.Compact || transitionSnapshot != null) {
                advanceTransitionGeneration()
            }
            updateState(PlayerLayerState.Compact)
            updateTransitionSnapshot(null)
        }
    }

    fun clearTransitionSnapshot(expectedGeneration: Long = transitionGeneration) {
        if (expectedGeneration == transitionGeneration) {
            updateTransitionSnapshot(null)
        }
    }

    fun finishReturnToCompact(expectedGeneration: Long = transitionGeneration) {
        if (expectedGeneration == transitionGeneration && state == PlayerLayerState.ReturningToCompact) {
            updateState(PlayerLayerState.Compact)
        }
    }

    private fun advanceTransitionGeneration() {
        currentTransitionGeneration += 1L
        setTransitionGeneration(currentTransitionGeneration)
    }

    private fun updateState(nextState: PlayerLayerState) {
        currentState = nextState
        setStateName(nextState.name)
    }

    private fun updateTransitionSnapshot(nextSnapshot: NowPlayingTransitionSnapshot?) {
        currentTransitionSnapshot = nextSnapshot
        setTransitionSnapshot(nextSnapshot)
    }
}

@Composable
internal fun rememberRootPlayerLayerController(
    currentSongId: Long?,
    currentSongPresent: Boolean,
): RootPlayerLayerController {
    var transitionSnapshot by remember { mutableStateOf<NowPlayingTransitionSnapshot?>(null) }
    var stateName by rememberSaveable { mutableStateOf(PlayerLayerState.Compact.name) }
    var transitionGeneration by rememberSaveable { mutableLongStateOf(0L) }
    val controller = RootPlayerLayerController(
        stateName = stateName,
        setStateName = { stateName = it },
        transitionSnapshot = transitionSnapshot,
        setTransitionSnapshot = { transitionSnapshot = it },
        currentSongPresent = currentSongPresent,
        transitionGeneration = transitionGeneration,
        setTransitionGeneration = { transitionGeneration = it },
    )
    LaunchedEffect(controller.state.name) {
        if (stateName != controller.state.name) {
            stateName = controller.state.name
        }
    }
    LaunchedEffect(currentSongId) {
        controller.resetIfSongMissing(currentSongId != null)
    }
    return controller
}

internal fun playerLayerStateAfterHide(
    returnToCompact: Boolean,
    currentSongPresent: Boolean,
): PlayerLayerState {
    return if (returnToCompact && currentSongPresent) {
        PlayerLayerState.ReturningToCompact
    } else {
        PlayerLayerState.Compact
    }
}
