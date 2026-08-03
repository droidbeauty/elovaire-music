package elovaire.music.droidbeauty.app.data.playback

import kotlin.math.abs
import java.nio.ByteBuffer
import java.nio.ByteOrder
import androidx.media3.common.C
import androidx.media3.common.Format
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackCrossfadeEnvelopeTest {
    @Test
    fun durationPolicy_hasBoundedTwoPointFiveSecondDefault() {
        assertEquals(1_000L, CrossfadeDurationPolicy.sanitize(0L))
        assertEquals(2_500L, CrossfadeDurationPolicy.DEFAULT_DURATION_MS)
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

    @Test
    fun silenceDetector_usesMinusEightyDbBaseLevel() {
        val detector = CrossfadeSilenceDetector()
        detector.configure(
            Format.Builder()
                .setSampleRate(1_000)
                .setChannelCount(1)
                .setPcmEncoding(C.ENCODING_PCM_16BIT)
                .build(),
        )
        val silentBuffer = ByteBuffer.allocateDirect(100 * 2).order(ByteOrder.nativeOrder())
        repeat(100) { silentBuffer.putShort(3) }
        silentBuffer.flip()

        detector.observe(silentBuffer, presentationTimeUs = 0L)

        assertTrue(
            detector.isSilentAt(
                positionUs = 0L,
                minimumDurationMs = CrossfadeSilencePolicy.MIN_SILENCE_DURATION_MS,
            ),
        )
    }

    @Test
    fun silenceDetector_rejectsSamplesAboveMinusEightyDbBaseLevel() {
        val detector = CrossfadeSilenceDetector()
        detector.configure(
            Format.Builder()
                .setSampleRate(1_000)
                .setChannelCount(1)
                .setPcmEncoding(C.ENCODING_PCM_16BIT)
                .build(),
        )
        val noisyBuffer = ByteBuffer.allocateDirect(100 * 2).order(ByteOrder.nativeOrder())
        repeat(100) { noisyBuffer.putShort(4) }
        noisyBuffer.flip()

        detector.observe(noisyBuffer, presentationTimeUs = 0L)

        assertTrue(!detector.isSilentAt(0L, CrossfadeSilencePolicy.MIN_SILENCE_DURATION_MS))
    }
}
