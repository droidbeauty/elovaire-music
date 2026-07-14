package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EffectiveDspStateTest {
    @Test
    fun directPlaybackDisablesAllSoftwareSignalChanges() {
        val state = resolveEffectiveDspState(
            effectsRequested = true,
            normalizationRequested = true,
            normalizationMetadata = VolumeNormalizationMetadata(trackGainDb = -6f),
            directPlaybackActive = true,
            softwareGainAllowed = true,
            baseGain = 0.5f,
        )

        assertFalse(state.processorsEnabled)
        assertFalse(state.normalizationEnabled)
        assertFalse(state.altersSignal)
        assertEquals(1f, state.fineGain)
    }

    @Test
    fun normalizationContributesToEffectiveGainOnlyWhenAllowed() {
        val state = resolveEffectiveDspState(
            effectsRequested = false,
            normalizationRequested = true,
            normalizationMetadata = VolumeNormalizationMetadata(trackGainDb = -6f),
            directPlaybackActive = false,
            softwareGainAllowed = true,
            baseGain = 1f,
        )

        assertTrue(state.normalizationEnabled)
        assertTrue(state.altersSignal)
        assertTrue(state.fineGain < 1f)
    }
}
