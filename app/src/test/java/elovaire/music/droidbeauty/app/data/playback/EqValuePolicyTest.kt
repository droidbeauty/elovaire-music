package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EqValuePolicyTest {
    @Test
    fun sanitize_clampsAndQuantizesAllValues() {
        val sanitized = EqValuePolicy.sanitize(
            EqSettings(
                bands = listOf(-2f, 0.14f, 2f),
                bass = 2f,
                midrange = -2f,
                treble = 0.126f,
                spaciousness = -1f,
                spaciousnessMode = SpaciousnessMode.StereoWidth,
                reverbDurationMs = 523,
            ),
        )

        assertEquals(EqualizerDspModel.BAND_COUNT, sanitized.bands.size)
        assertEquals(-1f, sanitized.bands[0], 0f)
        assertEquals(0.125f, sanitized.bands[1], 0f)
        assertEquals(1f, sanitized.bands[2], 0f)
        assertEquals(1f, sanitized.bass, 0f)
        assertEquals(-1f, sanitized.midrange, 0f)
        assertEquals(0.13f, sanitized.treble, 0f)
        assertEquals(0f, sanitized.spaciousness, 0f)
        assertEquals(SpaciousnessMode.Off, sanitized.spaciousnessMode)
        assertEquals(500, sanitized.reverbDurationMs)
    }

    @Test
    fun signalAlteringEffects_neutralStateIsEligibleForDirectPlayback() {
        assertFalse(EqValuePolicy.hasSignalAlteringEffects(EqSettings()))
    }

    @Test
    fun signalAlteringEffects_detectsEveryEffectFamily() {
        assertTrue(EqValuePolicy.hasSignalAlteringEffects(EqSettings(bands = listOf(0.1f))))
        assertTrue(EqValuePolicy.hasSignalAlteringEffects(EqSettings(bass = 0.1f)))
        assertTrue(EqValuePolicy.hasSignalAlteringEffects(EqSettings(midrange = -0.1f)))
        assertTrue(EqValuePolicy.hasSignalAlteringEffects(EqSettings(treble = 0.1f)))
        assertTrue(EqValuePolicy.hasSignalAlteringEffects(EqSettings(reverbDurationMs = 50)))
        assertTrue(EqValuePolicy.hasSignalAlteringEffects(EqSettings(monoEnabled = true)))
        assertTrue(
            EqValuePolicy.hasSignalAlteringEffects(
                EqSettings(spaciousness = 0.1f, spaciousnessMode = SpaciousnessMode.StereoWidth),
            ),
        )
    }
}
