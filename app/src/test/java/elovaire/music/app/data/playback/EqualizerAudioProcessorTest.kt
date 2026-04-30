package elovaire.music.app.data.playback

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import elovaire.music.app.domain.model.EqSettings
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EqualizerAudioProcessorTest {
    @Test
    fun flatSettingsAreDetectedAsBypass() {
        assertTrue(EqualizerDspModel.isFlat(EqSettings()))
        assertFalse(
            EqualizerDspModel.isFlat(
                EqSettings(bands = List(EqualizerDspModel.BAND_COUNT) { if (it == 3) 0.1f else 0f }),
            ),
        )
    }

    @Test
    fun nyquistUnsafeBandsAreDisabledAtLowerSampleRates() {
        val bands = EqualizerDspModel.activeBandFrequencies(sampleRateHz = 8_000)
        assertEquals(-1f, bands.last(), 0f)
        assertEquals(2_500f, bands[21], 0f)
    }

    @Test
    fun automaticHeadroomGrowsWithPositiveBoosts() {
        val lighter = EqualizerDspModel.automaticHeadroomDb(
            bandGainsDb = FloatArray(EqualizerDspModel.BAND_COUNT) { 0f },
            bassBoostDb = 2f,
            bassPregainDb = -1f,
            trebleBoostDb = 1f,
            spaciousnessAmount = 0.2f,
            sampleRateHz = 48_000,
        )
        val heavier = EqualizerDspModel.automaticHeadroomDb(
            bandGainsDb = FloatArray(EqualizerDspModel.BAND_COUNT) { if (it in 0..6) 6f else 0f },
            bassBoostDb = 6f,
            bassPregainDb = -3f,
            trebleBoostDb = 3f,
            spaciousnessAmount = 1f,
            sampleRateHz = 48_000,
        )
        assertTrue(heavier < lighter)
    }

    @Test
    fun flatEqIsBitAccurateForFloatPcm() {
        val processor = configuredProcessor(settings = EqSettings(), channelCount = 1, encoding = C.ENCODING_PCM_FLOAT)
        val source = floatArrayOf(0.1f, -0.5f, 0.25f, -0.125f, 0.8f, -0.8f)
        val input = source.toFloatBuffer()

        processor.queueInput(input)

        val output = processor.outputAsFloats()
        assertEquals(source.size, output.size)
        output.forEachIndexed { index, sample ->
            assertEquals(source[index], sample, 0.000001f)
        }
        assertTrue(processor.debugSnapshot().dspBypassed)
    }

    @Test
    fun bitPerfectBypassKeepsPcmUntouched() {
        val processor = configuredProcessor(
            settings = EqSettings(bands = List(EqualizerDspModel.BAND_COUNT) { if (it == 10) 0.7f else 0f }),
        )
        val source = shortArrayOf(3000, -4000, 1200, -1300)
        val input = ByteBuffer.allocateDirect(source.size * 2).order(ByteOrder.nativeOrder())
        source.forEach(input::putShort)
        input.flip()

        processor.setBitPerfectBypass(true)
        processor.queueInput(input)

        val output = processor.outputAsShorts()
        assertArrayEquals(source, output.toShortArray())
    }

    @Test
    fun silenceRemainsSilence() {
        val processor = configuredProcessor(
            settings = EqSettings(bands = List(EqualizerDspModel.BAND_COUNT) { if (it == 6) 0.5f else 0f }),
            channelCount = 2,
            encoding = C.ENCODING_PCM_FLOAT,
        )
        val input = FloatArray(128) { 0f }.toFloatBuffer()

        processor.queueInput(input)
        val output = processor.outputAsFloats()

        assertTrue(output.all { it == 0f })
    }

    @Test
    fun boostedLowBandRaisesLowSineMoreThanMidSine() {
        val settings = EqSettings(
            bands = List(EqualizerDspModel.BAND_COUNT) { index ->
                when (index) {
                    5, 6, 7 -> 0.45f
                    else -> 0f
                }
            },
            bass = 0.28f,
        )
        val processor = configuredProcessor(settings = settings, channelCount = 1, encoding = C.ENCODING_PCM_FLOAT)

        val lowGain = rms(
            processMonoSine(
                processor = processor,
                frequencyHz = 80f,
                amplitude = 0.18f,
                sampleRateHz = 48_000,
            ),
        ) / 0.18f

        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)

        val midGain = rms(
            processMonoSine(
                processor = processor,
                frequencyHz = 1_000f,
                amplitude = 0.18f,
                sampleRateHz = 48_000,
            ),
        ) / 0.18f

        assertTrue(lowGain > midGain + 0.08f)
    }

    @Test
    fun nearFullScaleSmileCurveAvoidsHardClipping() {
        val settings = EqSettings(
            bands = List(EqualizerDspModel.BAND_COUNT) { index ->
                when {
                    index in 2..6 -> 0.35f
                    index in 18..23 -> 0.26f
                    index in 10..14 -> -0.15f
                    else -> 0f
                }
            },
            bass = 0.3f,
            treble = 0.22f,
            spaciousness = 0.16f,
        )
        val processor = configuredProcessor(settings = settings, channelCount = 2, encoding = C.ENCODING_PCM_FLOAT)
        val samples = interleavedStereoTone(
            leftFrequencyHz = 70f,
            rightFrequencyHz = 1_600f,
            amplitude = 0.92f,
            frames = 4_096,
            sampleRateHz = 48_000,
        ).toFloatBuffer()

        processor.queueInput(samples)
        val output = processor.outputAsFloats()
        val peak = output.maxOf { abs(it) }
        val diagnostics = processor.debugSnapshot()

        assertTrue(peak <= 1f)
        assertTrue(diagnostics.computedHeadroomDb < 0f)
        assertTrue(diagnostics.limiterPeakReduction < 0.12f)
    }

    @Test
    fun stereoChannelsRemainIndependent() {
        val processor = configuredProcessor(
            settings = EqSettings(bands = List(EqualizerDspModel.BAND_COUNT) { if (it == 17) 0.5f else 0f }),
            channelCount = 2,
            encoding = C.ENCODING_PCM_FLOAT,
        )
        val input = FloatArray(2_048) { frameIndex ->
            if (frameIndex % 2 == 0) {
                (0.35f * sin(2.0 * PI * 1_000.0 * (frameIndex / 2.0) / 48_000.0)).toFloat()
            } else {
                0f
            }
        }.toFloatBuffer()

        processor.queueInput(input)
        val output = processor.outputAsFloats()
        val rightChannelPeak = output.filterIndexed { index, _ -> index % 2 == 1 }.maxOf { abs(it) }

        assertTrue(rightChannelPeak < 0.0001f)
    }

    @Test
    fun diagnosticsReportBypassForFlatCurve() {
        val processor = configuredProcessor(settings = EqSettings())
        processor.queueInput(shortArrayOf(200, -200, 400, -400).toShortBuffer())

        val diagnostics = processor.debugSnapshot()

        assertTrue(diagnostics.dspBypassed)
        assertEquals(0, diagnostics.activeFilterCount)
    }

    private fun configuredProcessor(
        settings: EqSettings,
        channelCount: Int = 2,
        sampleRateHz: Int = 48_000,
        encoding: Int = C.ENCODING_PCM_16BIT,
    ): EqualizerAudioProcessor {
        return EqualizerAudioProcessor().apply {
            updateSettings(settings)
            configure(AudioProcessor.AudioFormat(sampleRateHz, channelCount, encoding))
            flush(AudioProcessor.StreamMetadata.DEFAULT)
        }
    }

    private fun processMonoSine(
        processor: EqualizerAudioProcessor,
        frequencyHz: Float,
        amplitude: Float,
        sampleRateHz: Int,
        frames: Int = 4_096,
    ): FloatArray {
        val buffer = FloatArray(frames) { index ->
            (amplitude * sin(2.0 * PI * frequencyHz * index / sampleRateHz)).toFloat()
        }.toFloatBuffer()
        processor.queueInput(buffer)
        return processor.outputAsFloats().toFloatArray()
    }

    private fun FloatArray.toFloatBuffer(): ByteBuffer {
        return ByteBuffer
            .allocateDirect(size * 4)
            .order(ByteOrder.nativeOrder())
            .apply {
                forEach { putFloat(it) }
                flip()
            }
    }

    private fun ShortArray.toShortBuffer(): ByteBuffer {
        return ByteBuffer
            .allocateDirect(size * 2)
            .order(ByteOrder.nativeOrder())
            .apply {
                forEach { putShort(it) }
                flip()
            }
    }

    private fun interleavedStereoTone(
        leftFrequencyHz: Float,
        rightFrequencyHz: Float,
        amplitude: Float,
        frames: Int,
        sampleRateHz: Int,
    ): FloatArray {
        return FloatArray(frames * 2) { index ->
            val frame = index / 2
            val frequency = if (index % 2 == 0) leftFrequencyHz else rightFrequencyHz
            (amplitude * sin(2.0 * PI * frequency * frame / sampleRateHz)).toFloat()
        }
    }

    private fun rms(samples: FloatArray): Float {
        val meanSquare = samples.fold(0.0) { acc, sample -> acc + sample * sample } / samples.size.toDouble()
        return sqrt(meanSquare).toFloat()
    }

    private fun EqualizerAudioProcessor.outputAsShorts(): List<Short> {
        val output = output.order(ByteOrder.nativeOrder())
        val shorts = mutableListOf<Short>()
        while (output.remaining() >= 2) {
            shorts += output.short
        }
        return shorts
    }

    private fun EqualizerAudioProcessor.outputAsFloats(): List<Float> {
        val output = output.order(ByteOrder.nativeOrder())
        val samples = mutableListOf<Float>()
        while (output.remaining() >= 4) {
            samples += output.float
        }
        return samples
    }
}
