package elovaire.music.droidbeauty.app.ui.motion

import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionTransitionsTest {
    @Test
    fun staticTransitions_areReused() {
        val transitions = MotionTransitions(MotionSpecs(MotionRuntime(durationScale = 1f)))

        assertSame(transitions.standardEnter(), transitions.standardEnter())
        assertSame(transitions.detailForwardExit(), transitions.detailForwardExit())
        assertSame(transitions.playerOverlayEnter(), transitions.playerOverlayEnter())
        assertSame(transitions.softContentTransform(), transitions.softContentTransform())
    }

    @Test
    fun exitCallbackGate_requiresAVisibleCycleAndFiresOnce() {
        val gate = MotionExitCallbackGate()

        assertFalse(gate.consumeFinishedExit())
        gate.onVisibilityTargetChanged(true)
        assertTrue(gate.consumeFinishedExit())
        assertFalse(gate.consumeFinishedExit())
    }
}
