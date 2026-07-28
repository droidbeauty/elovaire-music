package elovaire.music.droidbeauty.app.data.playback

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackCrossfadeEnvelopeTest {
    @Test
    fun durationPolicy_hasBoundedFiveSecondDefault() {
        assertEquals(1_000L, CrossfadeDurationPolicy.sanitize(0L))
        assertEquals(5_000L, CrossfadeDurationPolicy.DEFAULT_DURATION_MS)
        assertEquals(12_000L, CrossfadeDurationPolicy.sanitize(Long.MAX_VALUE))
    }

    @Test
    fun equalPowerEnvelope_startsAndEndsAtExpectedGains() {
        assertEquals(1f, equalPowerCrossfadeEnvelope(0f).first, 0.0001f)
        assertEquals(0f, equalPowerCrossfadeEnvelope(0f).second, 0.0001f)
        assertEquals(0f, equalPowerCrossfadeEnvelope(1f).first, 0.0001f)
        assertEquals(1f, equalPowerCrossfadeEnvelope(1f).second, 0.0001f)
    }

    @Test
    fun equalPowerEnvelope_preservesPowerAtMidpoint() {
        val (outgoing, incoming) = equalPowerCrossfadeEnvelope(0.5f)
        assertTrue(abs((outgoing * outgoing) + (incoming * incoming) - 1f) < 0.0001f)
    }
}
