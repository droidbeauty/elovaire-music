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
        assertSame(transitions.popupCardEnter(), transitions.popupCardEnter())
        assertSame(transitions.popupCardExit(), transitions.popupCardExit())
        assertSame(transitions.bannerEnter(), transitions.bannerEnter())
        assertSame(transitions.bannerExit(), transitions.bannerExit())
        assertSame(transitions.bottomBarEnter(), transitions.bottomBarEnter())
        assertSame(transitions.bottomBarExit(), transitions.bottomBarExit())
        assertSame(transitions.verticalRevealEnter(), transitions.verticalRevealEnter())
        assertSame(transitions.verticalRevealExit(), transitions.verticalRevealExit())
        assertSame(transitions.contextMenuEnter(), transitions.contextMenuEnter())
        assertSame(transitions.contextMenuExit(), transitions.contextMenuExit())
        assertSame(transitions.titleSwapTransform(), transitions.titleSwapTransform())
        assertSame(transitions.topBarNavigationTransform(), transitions.topBarNavigationTransform())
        assertSame(transitions.topBarActionSwapTransform(), transitions.topBarActionSwapTransform())
        assertSame(transitions.sharedTopBarTransform(), transitions.sharedTopBarTransform())
    }

    @Test
    fun exitCallbackGate_requiresAVisibleCycleAndFiresOnce() {
        val gate = MotionExitCallbackGate()

        assertFalse(gate.consumeFinishedExit())
        gate.onVisibilityTargetChanged(true)
        assertFalse(gate.consumeFinishedExit())
        gate.onCurrentStateChanged(true)
        gate.onVisibilityTargetChanged(false)
        assertTrue(gate.consumeFinishedExit())
        assertFalse(gate.consumeFinishedExit())
    }

    @Test
    fun exitCallbackGate_reentryCancelsThePendingExit() {
        val gate = MotionExitCallbackGate()

        gate.onVisibilityTargetChanged(true)
        gate.onCurrentStateChanged(true)
        gate.onVisibilityTargetChanged(false)
        gate.onVisibilityTargetChanged(true)

        assertFalse(gate.consumeFinishedExit())
    }

    @Test
    fun exitCallbackGate_ignoresRepeatedTargetUpdatesFromRecomposition() {
        val gate = MotionExitCallbackGate()

        gate.onVisibilityTargetChanged(true)
        gate.onCurrentStateChanged(true)
        gate.onVisibilityTargetChanged(false)
        gate.onVisibilityTargetChanged(false)

        assertTrue(gate.consumeFinishedExit())
        assertFalse(gate.consumeFinishedExit())
    }
}
