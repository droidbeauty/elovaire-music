package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackRuntimeStateMachineTest {
    @Test
    fun staleRecoveryCompletionCannotClearANewerTransition() {
        val state = PlaybackRuntimeStateMachine()
        assertTrue(state.beginRecovery(1))
        val stale = state.state
        state.complete()
        assertTrue(state.beginRebuild("new route"))
        state.complete(stale)
        assertTrue(state.state is PlaybackRuntimeTransition.Rebuilding)
        state.complete()
        assertTrue(state.beginRecovery(2))
        val current = state.state
        state.complete(current)
        assertTrue(state.state is PlaybackRuntimeTransition.Idle)
    }

    @Test
    fun rebuildAndRecoveryAreMutuallyExclusiveUntilCompletion() {
        val state = PlaybackRuntimeStateMachine()

        assertTrue(state.beginRebuild("route-change"))
        assertFalse(state.beginRecovery(1))
        state.complete()
        assertTrue(state.beginRecovery(1))
        assertFalse(state.beginRebuild("second-route-change"))
    }

    @Test
    fun releaseFencesLaterTransitionsAndCompletion() {
        val state = PlaybackRuntimeStateMachine()

        state.release()
        assertFalse(state.beginRebuild("late"))
        assertFalse(state.beginRecovery(1))
        state.complete()

        assertTrue(state.state is PlaybackRuntimeTransition.Released)
    }
}
