package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

internal class RootPlayerLayerController(
    stateName: String,
    private val setStateName: (String) -> Unit,
    transitionSnapshot: NowPlayingTransitionSnapshot?,
    private val setTransitionSnapshot: (NowPlayingTransitionSnapshot?) -> Unit,
    private val hasCurrentSong: () -> Boolean,
) {
    val state: PlayerLayerState = stateName.toPlayerLayerStateOrDefault()
    val transitionSnapshot: NowPlayingTransitionSnapshot? = transitionSnapshot

    fun requestOpen(snapshot: NowPlayingTransitionSnapshot?) {
        if (!hasCurrentSong() || state == PlayerLayerState.Expanded) {
            return
        }
        setStateName(PlayerLayerState.Expanded.name)
        setTransitionSnapshot(snapshot)
    }

    fun hide(returnToCompact: Boolean) {
        setStateName(playerLayerStateAfterHide(returnToCompact, hasCurrentSong()).name)
    }

    fun resetIfSongMissing(currentSongPresent: Boolean) {
        if (!currentSongPresent) {
            setStateName(PlayerLayerState.Compact.name)
            setTransitionSnapshot(null)
        }
    }

    fun clearTransitionSnapshot() {
        setTransitionSnapshot(null)
    }

    fun finishReturnToCompact() {
        setStateName(PlayerLayerState.Compact.name)
    }
}

@Composable
internal fun rememberRootPlayerLayerController(
    currentSongId: Long?,
    hasCurrentSong: () -> Boolean,
): RootPlayerLayerController {
    var transitionSnapshot by remember { mutableStateOf<NowPlayingTransitionSnapshot?>(null) }
    var stateName by rememberSaveable { mutableStateOf(PlayerLayerState.Compact.name) }
    val controller = RootPlayerLayerController(
        stateName = stateName,
        setStateName = { stateName = it },
        transitionSnapshot = transitionSnapshot,
        setTransitionSnapshot = { transitionSnapshot = it },
        hasCurrentSong = hasCurrentSong,
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
