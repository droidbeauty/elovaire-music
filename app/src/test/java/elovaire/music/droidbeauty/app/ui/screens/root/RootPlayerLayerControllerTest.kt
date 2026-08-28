package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class RootPlayerLayerControllerTest {
    @Test
    fun requestOpen_withCurrentSongPresent_opensWithoutSnapshot() {
        var stateName = PlayerLayerState.Compact.name
        var snapshot: NowPlayingTransitionSnapshot? = null
        val controller = rootPlayerLayerController(
            stateName = stateName,
            setStateName = { stateName = it },
            setTransitionSnapshot = { snapshot = it },
            currentSongPresent = true,
        )

        controller.requestOpen(null)

        assertEquals(PlayerLayerState.Expanded.name, stateName)
        assertNull(snapshot)
    }

    @Test
    fun requestOpen_withCurrentSongPresent_storesSnapshot() {
        var stateName = PlayerLayerState.Compact.name
        var snapshot: NowPlayingTransitionSnapshot? = null
        val expectedSnapshot = NowPlayingTransitionSnapshot(
            songId = 7L,
            barBounds = Rect(0f, 0f, 100f, 80f),
            artworkBounds = Rect(8f, 8f, 56f, 56f),
        )
        val controller = rootPlayerLayerController(
            stateName = stateName,
            setStateName = { stateName = it },
            setTransitionSnapshot = { snapshot = it },
            currentSongPresent = true,
        )

        controller.requestOpen(expectedSnapshot)

        assertEquals(PlayerLayerState.Expanded.name, stateName)
        assertSame(expectedSnapshot, snapshot)
    }

    @Test
    fun requestOpen_withoutCurrentSong_doesNotOpen() {
        var stateName = PlayerLayerState.Compact.name
        var snapshot: NowPlayingTransitionSnapshot? = null
        val controller = rootPlayerLayerController(
            stateName = stateName,
            setStateName = { stateName = it },
            setTransitionSnapshot = { snapshot = it },
            currentSongPresent = false,
        )

        controller.requestOpen(null)

        assertEquals(PlayerLayerState.Compact.name, stateName)
        assertNull(snapshot)
    }

    @Test
    fun requestOpen_whenAlreadyExpanded_doesNotReplaceSnapshot() {
        var stateName = PlayerLayerState.Expanded.name
        val existingSnapshot = NowPlayingTransitionSnapshot(
            songId = 1L,
            barBounds = Rect(0f, 0f, 100f, 80f),
            artworkBounds = Rect(8f, 8f, 56f, 56f),
        )
        var snapshot: NowPlayingTransitionSnapshot? = existingSnapshot
        val controller = rootPlayerLayerController(
            stateName = stateName,
            transitionSnapshot = snapshot,
            setStateName = { stateName = it },
            setTransitionSnapshot = { snapshot = it },
            currentSongPresent = true,
        )

        controller.requestOpen(null)

        assertEquals(PlayerLayerState.Expanded.name, stateName)
        assertSame(existingSnapshot, snapshot)
    }

    @Test
    fun requestOpen_twiceBeforeRecomposition_keepsTheFirstSnapshot() {
        var stateName = PlayerLayerState.Compact.name
        var snapshot: NowPlayingTransitionSnapshot? = null
        val first = NowPlayingTransitionSnapshot(
            songId = 1L,
            barBounds = Rect(0f, 0f, 100f, 80f),
            artworkBounds = Rect(8f, 8f, 56f, 56f),
        )
        val second = first.copy(songId = 2L)
        val controller = rootPlayerLayerController(
            stateName = stateName,
            setStateName = { stateName = it },
            setTransitionSnapshot = { snapshot = it },
            currentSongPresent = true,
        )

        controller.requestOpen(first)
        controller.requestOpen(second)

        assertEquals(PlayerLayerState.Expanded.name, stateName)
        assertSame(first, snapshot)
    }

    @Test
    fun staleExitCompletion_cannotClearOrCollapseANewerOpen() {
        var stateName = PlayerLayerState.Compact.name
        var snapshot: NowPlayingTransitionSnapshot? = null
        var generation = 0L
        val first = NowPlayingTransitionSnapshot(
            songId = 1L,
            barBounds = Rect(0f, 0f, 100f, 80f),
            artworkBounds = Rect(8f, 8f, 56f, 56f),
        )
        val second = first.copy(songId = 2L)
        val controller = rootPlayerLayerController(
            stateName = stateName,
            setStateName = { stateName = it },
            setTransitionSnapshot = { snapshot = it },
            currentSongPresent = true,
            transitionGeneration = generation,
            setTransitionGeneration = { generation = it },
        )

        controller.requestOpen(first)
        val staleGeneration = controller.transitionGeneration
        controller.hide(returnToCompact = true)
        controller.requestOpen(second)

        controller.clearTransitionSnapshot(staleGeneration)
        controller.finishReturnToCompact(staleGeneration)

        assertEquals(PlayerLayerState.Expanded.name, stateName)
        assertSame(second, snapshot)
    }

    @Test
    fun resetIfSongMissing_returnsToCompactAndClearsSnapshot() {
        var stateName = PlayerLayerState.Expanded.name
        var snapshot: NowPlayingTransitionSnapshot? = NowPlayingTransitionSnapshot(
            songId = 4L,
            barBounds = Rect(0f, 0f, 100f, 80f),
            artworkBounds = Rect(8f, 8f, 56f, 56f),
        )
        val controller = rootPlayerLayerController(
            stateName = stateName,
            transitionSnapshot = snapshot,
            setStateName = { stateName = it },
            setTransitionSnapshot = { snapshot = it },
            currentSongPresent = true,
        )

        controller.resetIfSongMissing(false)

        assertEquals(PlayerLayerState.Compact.name, stateName)
        assertNull(snapshot)
    }

    @Test
    fun finishReturnToCompact_setsCompactState() {
        var stateName = PlayerLayerState.ReturningToCompact.name
        val controller = rootPlayerLayerController(
            stateName = stateName,
            setStateName = { stateName = it },
            currentSongPresent = true,
        )

        controller.finishReturnToCompact()

        assertEquals(PlayerLayerState.Compact.name, stateName)
    }

    @Test
    fun playerLayerStateAfterHide_returnsToCompactOnlyWhenSongStillExists() {
        assertEquals(
            PlayerLayerState.ReturningToCompact,
            playerLayerStateAfterHide(returnToCompact = true, currentSongPresent = true),
        )
        assertEquals(
            PlayerLayerState.Compact,
            playerLayerStateAfterHide(returnToCompact = true, currentSongPresent = false),
        )
        assertEquals(
            PlayerLayerState.Compact,
            playerLayerStateAfterHide(returnToCompact = false, currentSongPresent = true),
        )
    }

    private fun rootPlayerLayerController(
        stateName: String,
        transitionSnapshot: NowPlayingTransitionSnapshot? = null,
        setStateName: (String) -> Unit = {},
        setTransitionSnapshot: (NowPlayingTransitionSnapshot?) -> Unit = {},
        currentSongPresent: Boolean,
        transitionGeneration: Long = 0L,
        setTransitionGeneration: (Long) -> Unit = {},
    ): RootPlayerLayerController {
        return RootPlayerLayerController(
            stateName = stateName,
            setStateName = setStateName,
            transitionSnapshot = transitionSnapshot,
            setTransitionSnapshot = setTransitionSnapshot,
            currentSongPresent = currentSongPresent,
            transitionGeneration = transitionGeneration,
            setTransitionGeneration = setTransitionGeneration,
        )
    }
}
