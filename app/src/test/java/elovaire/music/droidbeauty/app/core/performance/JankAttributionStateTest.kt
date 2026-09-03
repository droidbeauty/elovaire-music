package elovaire.music.droidbeauty.app.core.performance

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JankAttributionStateTest {
    @Test
    fun boundaryOnlyChangesAfterTheFirstValue() {
        val state = JankAttributionState()

        assertFalse(state.update("screen", "home"))
        assertFalse(state.update("screen", "home"))
        assertTrue(state.update("screen", "search"))
        assertFalse(state.update("unrelated", "value"))
    }

    @Test
    fun removingActiveBoundaryStateCreatesOneBoundary() {
        val state = JankAttributionState()

        state.update("interaction", "navigation")
        assertTrue(state.remove("interaction"))
        assertFalse(state.remove("interaction"))
    }
}
