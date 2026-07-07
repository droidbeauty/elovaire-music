package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RootRouteArgumentsTest {
    @Test
    fun routeLongArg_returnsNullForMissingAndZeroArguments() {
        assertNull(normalizedRouteLongArg(containsArg = false, value = null))
        assertNull(normalizedRouteLongArg(containsArg = true, value = 0L))
    }

    @Test
    fun routeLongArg_preservesNegativeAndGeneratedIds() {
        assertEquals(-27L, normalizedRouteLongArg(containsArg = true, value = -27L))
        assertEquals(51L, normalizedRouteLongArg(containsArg = true, value = 51L))
    }
}
