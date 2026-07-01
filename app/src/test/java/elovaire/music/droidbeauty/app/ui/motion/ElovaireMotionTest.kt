package elovaire.music.droidbeauty.app.ui.motion

import org.junit.Assert.assertEquals
import org.junit.Test

class ElovaireMotionTest {
    @Test
    fun scaleDurationMillis_ignoresAnimatorScale() {
        assertEquals(100L, ElovaireMotion.scaleDurationMillis(100, 0.5f))
        assertEquals(100L, ElovaireMotion.scaleDurationMillis(100, 1f))
        assertEquals(100L, ElovaireMotion.scaleDurationMillis(100, 2f))
    }

    @Test
    fun scaleDurationMillis_keepsDefaultDurationAtZeroScale() {
        assertEquals(100L, ElovaireMotion.scaleDurationMillis(100, 0f))
    }
}
