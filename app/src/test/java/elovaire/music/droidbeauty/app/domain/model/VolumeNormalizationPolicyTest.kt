package elovaire.music.droidbeauty.app.domain.model

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeNormalizationPolicyTest {
    @Test
    fun parseGain_acceptsReplayGainDbValues() {
        assertEquals(-7.5f, VolumeNormalizationPolicy.parseGain("-7.50 dB") ?: 0f, 0.001f)
        assertEquals(3.25f, VolumeNormalizationPolicy.parseGain("+3.25 dB") ?: 0f, 0.001f)
    }

    @Test
    fun parseGain_acceptsR128FixedPointValues() {
        assertEquals(-5f, VolumeNormalizationPolicy.parseGain("-1280", r128 = true) ?: 0f, 0.001f)
    }

    @Test
    fun multiplier_usesNeutralGainForMissingOrMalformedMetadata() {
        assertEquals(1f, VolumeNormalizationPolicy.multiplierFor(null), 0.001f)
        assertEquals(null, VolumeNormalizationPolicy.parseGain("not a gain"))
    }

    @Test
    fun multiplier_limitsClippingWhenPeakIsPresent() {
        val multiplier = VolumeNormalizationPolicy.multiplierFor(
            VolumeNormalizationMetadata(
                trackGainDb = 6f,
                trackPeak = 0.8f,
            ),
        )
        assertEquals(1.25f, multiplier, 0.001f)
    }

    @Test
    fun multiplier_reducesNegativeGain() {
        val multiplier = VolumeNormalizationPolicy.multiplierFor(
            VolumeNormalizationMetadata(trackGainDb = -6f),
        )
        assertTrue(abs(multiplier - 0.501f) < 0.01f)
    }
}
