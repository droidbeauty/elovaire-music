package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class EqMacroKnobTest {
    @Test
    fun valuesSnapToOnePercentStepsWithinRange() {
        assertEquals(0.36f, snapEqMacroValue(0.364f, 0f..1f), 0.0001f)
        assertEquals(-0.42f, snapEqMacroValue(-0.416f, -1f..1f), 0.0001f)
    }

    @Test
    fun valuesClampToRangeAndRejectNonFiniteInput() {
        assertEquals(1f, snapEqMacroValue(4f, 0f..1f), 0f)
        assertEquals(-1f, snapEqMacroValue(-4f, -1f..1f), 0f)
        assertEquals(0f, snapEqMacroValue(Float.NaN, -1f..1f), 0f)
    }
}
