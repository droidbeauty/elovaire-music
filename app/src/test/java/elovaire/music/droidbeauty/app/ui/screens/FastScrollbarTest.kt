package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FastScrollbarTest {
    @Test
    fun visualChromeIsThinnerThanTouchTarget() {
        assertEquals(28f, FastScrollbarTouchWidth.value)
        assertEquals(1f, FastScrollbarTrackWidth.value)
        assertEquals(3f, FastScrollbarThumbWidth.value)
        assertTrue(FastScrollbarThumbWidth < FastScrollbarTouchWidth)
        assertTrue(FastScrollbarTrackWidth < FastScrollbarTouchWidth)
    }

    @Test
    fun minimumThumbHeightIsPreserved() {
        assertEquals(40f, FastScrollbarMinThumbHeight.value)
    }
}
