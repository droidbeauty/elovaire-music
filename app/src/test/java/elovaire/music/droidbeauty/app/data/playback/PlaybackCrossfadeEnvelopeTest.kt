package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.C
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
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
    fun settingsPolicies_snapToRequestedRanges() {
        assertEquals(2_000L, CrossfadeDurationPolicy.sanitizeSettingsDuration(0L))
        assertEquals(3_500L, CrossfadeDurationPolicy.sanitizeSettingsDuration(3_499L))
        assertEquals(5_000L, CrossfadeDurationPolicy.sanitizeSettingsDuration(Long.MAX_VALUE))
        assertEquals(-100f, CrossfadeSilencePolicy.sanitizeLevelDb(-120f))
        assertEquals(-85f, CrossfadeSilencePolicy.sanitizeLevelDb(-86f))
        assertEquals(-80f, CrossfadeSilencePolicy.sanitizeLevelDb(0f))
    }

    @Test
    fun silencePolicy_convertsDbfsToLinearAmplitude() {
        assertEquals(0.0001f, CrossfadeSilencePolicy.amplitudeThresholdForDb(-80f), 0.000001f)
        assertEquals(0.00001f, CrossfadeSilencePolicy.amplitudeThresholdForDb(-100f), 0.000001f)
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
    fun trailingCue_trimsOnlyTrueTrailingSilence() {
        val windows = listOf(
            CrossfadeLevelWindow(0L, 100L, 0.2f),
            CrossfadeLevelWindow(100L, 200L, 0f),
            CrossfadeLevelWindow(200L, 300L, 0.4f),
            CrossfadeLevelWindow(300L, 400L, 0f),
        )

        assertEquals(300L to 100L, CrossfadeCueAlgorithm.trailingCue(windows, 400L))
    }

    @Test
    fun trailingCue_keepsFixedTimingWhenSilenceIsShortOrEntireRegionIsSilent() {
        val shortSilence = listOf(CrossfadeLevelWindow(0L, 20L, 0.2f), CrossfadeLevelWindow(20L, 80L, 0f))
        val emptyAudio = listOf(CrossfadeLevelWindow(0L, 80L, 0f))

        assertEquals(80L to 0L, CrossfadeCueAlgorithm.trailingCue(shortSilence, 80L))
        assertEquals(80L to 0L, CrossfadeCueAlgorithm.trailingCue(emptyAudio, 80L))
    }

    @Test
    fun leadingCue_trimsOnlySufficientLeadingSilence() {
        val windows = listOf(
            CrossfadeLevelWindow(0L, 120L, 0f),
            CrossfadeLevelWindow(120L, 140L, 0.2f),
        )

        assertEquals(120L to 120L, CrossfadeCueAlgorithm.leadingCue(windows))
        assertEquals(
            0L to 0L,
            CrossfadeCueAlgorithm.leadingCue(
                windows = listOf(CrossfadeLevelWindow(0L, 80L, 0f), CrossfadeLevelWindow(80L, 100L, 0.2f)),
            ),
        )
    }

    @Test
    fun transitionPlan_usesTwoPointFiveSecondsAndIncomingCue() {
        val plan = CrossfadeTransitionPlan.from(
            cue = CrossfadeCue(
                outgoingMixOutMs = 20_000L,
                incomingMixInMs = 120L,
                outgoingTrailingSilenceMs = 5_000L,
                incomingLeadingSilenceMs = 120L,
                outgoingAnalysisSucceeded = true,
                incomingAnalysisSucceeded = true,
            ),
            outgoingDurationMs = 25_000L,
        )

        assertEquals(20_000L, plan.outgoingMixOutMs)
        assertEquals(17_500L, plan.fadeStartMs)
        assertEquals(2_500L, plan.fadeDurationMs)
        assertEquals(120L, plan.incomingMixInMs)
    }

    @Test
    fun transitionPlan_shortIncomingTrackShortensOverlapToAvoidRunningPastIt() {
        val plan = CrossfadeTransitionPlan.from(
            cue = CrossfadeCue.fallback(outgoingDurationMs = 20_000L).copy(incomingMixInMs = 500L),
            outgoingDurationMs = 20_000L,
            incomingDurationMs = 2_000L,
        )

        assertEquals(1_500L, plan.fadeDurationMs)
        assertEquals(18_500L, plan.fadeStartMs)
    }

    @Test
    fun transitionPlan_usesConfiguredFadeDuration() {
        val plan = CrossfadeTransitionPlan.from(
            cue = CrossfadeCue.fallback(outgoingDurationMs = 20_000L),
            outgoingDurationMs = 20_000L,
            fadeDurationMs = 4_500L,
        )

        assertEquals(4_500L, plan.fadeDurationMs)
        assertEquals(15_500L, plan.fadeStartMs)
    }

    @Test
    fun pcmEnvelopeAccumulator_supportsMedia3PcmEncodings() {
        val samples = listOf(
            C.ENCODING_PCM_8BIT to { buffer: ByteBuffer -> buffer.put(192.toByte()) },
            C.ENCODING_PCM_16BIT to { buffer: ByteBuffer -> buffer.putShort(16_384) },
            C.ENCODING_PCM_24BIT to { buffer: ByteBuffer ->
                buffer.put(0x00).put(0x00).put(0x40)
            },
            C.ENCODING_PCM_32BIT to { buffer: ByteBuffer -> buffer.putInt(1_073_741_824) },
            C.ENCODING_PCM_FLOAT to { buffer: ByteBuffer -> buffer.putFloat(0.5f) },
        )

        samples.forEach { (encoding, writeSample) ->
            val accumulator = PcmEnvelopeAccumulator(
                sampleRate = 1_000,
                channelCount = 1,
                encoding = encoding,
                regionStartUs = 0L,
                regionEndUs = 20_000L,
            )
            val bytesPerSample = when (encoding) {
                C.ENCODING_PCM_8BIT -> 1
                C.ENCODING_PCM_16BIT -> 2
                C.ENCODING_PCM_24BIT -> 3
                else -> 4
            }
            val buffer = ByteBuffer.allocate(bytesPerSample * 20).order(ByteOrder.nativeOrder())
            repeat(20) { writeSample(buffer) }
            buffer.flip()

            accumulator.append(buffer, 0L)

            assertEquals(0.5f, accumulator.finish().single().maxChannelRms, 0.001f)
        }
    }

    @Test
    fun pcmEnvelopeAccumulator_usesLoudestChannelAndRms() {
        val accumulator = PcmEnvelopeAccumulator(
            sampleRate = 1_000,
            channelCount = 2,
            encoding = C.ENCODING_PCM_16BIT,
            regionStartUs = 0L,
            regionEndUs = 20_000L,
        )
        val buffer = ByteBuffer.allocate(20 * 2 * 2).order(ByteOrder.nativeOrder())
        repeat(20) {
            buffer.putShort(0)
            buffer.putShort(16_384)
        }
        buffer.flip()

        accumulator.append(buffer, 0L)

        assertEquals(0.5f, accumulator.finish().single().maxChannelRms, 0.001f)
    }

    @Test
    fun trailingCue_usesMinusEightyDbfsRmsFloor() {
        val belowFloor = CrossfadeLevelWindow(0L, 20L, 3f / 32_768f)
        val aboveFloor = CrossfadeLevelWindow(20L, 40L, 4f / 32_768f)

        assertEquals(
            20L to 0L,
            CrossfadeCueAlgorithm.trailingCue(
                windows = listOf(belowFloor),
                durationMs = 20L,
            ),
        )
        assertEquals(40L to 0L, CrossfadeCueAlgorithm.trailingCue(listOf(belowFloor, aboveFloor), 40L))
    }
}
