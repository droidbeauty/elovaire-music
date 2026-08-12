package elovaire.music.droidbeauty.app.ui.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
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

    @Test
    fun commonFacadeSpecs_areReusedAcrossRecompositions() {
        assertSame(
            ElovaireMotion.colorFadeSpec<Float>(),
            ElovaireMotion.colorFadeSpec<Float>(),
        )
        assertSame(
            ElovaireMotion.releaseSpringSpec<Float>(),
            ElovaireMotion.releaseSpringSpec<Float>(),
        )
    }
}
