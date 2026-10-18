package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import elovaire.music.droidbeauty.app.domain.model.UpscalerSettings
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpscalerAudioProcessorTest {
    @Test
    fun defaultsBypassAndInvalidPreferencesAreSanitized() {
        assertFalse(UpscalerSettings().enabled)
        assertEquals(UpscalerSettings(multiplier = 2, headroomDb = 6),
            UpscalerSettings(multiplier = 3, headroomDb = 100).sanitized())
        val processor = UpscalerAudioProcessor(UpscalerSettings()) { _, _ -> error("Must bypass") }
        assertEquals(AudioProcessor.AudioFormat.NOT_SET, processor.configure(format()))
        assertFalse(processor.isActive)
    }

    @Test
    fun rateLimitAndOutputSupportReduceFactorWithoutChangingSource() {
        val processor = UpscalerAudioProcessor(UpscalerSettings(enabled = true, multiplier = 8)) { rate, _ -> rate <= 192_000 }
        assertEquals(192_000, processor.configure(format()).sampleRate)
        assertEquals(352_800, UpscalerAudioProcessor(UpscalerSettings(true, 8)) { _, _ -> true }
            .configure(format(rate = 44_100)).sampleRate)
        assertEquals(AudioProcessor.AudioFormat.NOT_SET, processor.configure(format(rate = 384_000)))
        assertEquals(AudioProcessor.AudioFormat.NOT_SET, processor.configure(format(encoding = C.ENCODING_PCM_8BIT)))
        assertEquals(AudioProcessor.AudioFormat.NOT_SET, processor.configure(format(channels = 9)))
    }

    @Test
    fun chunkBoundariesDoNotChangeSamplesAndEveryChannelRetainsExactDuration() {
        for (factor in listOf(2, 4, 8)) for (channels in listOf(1, 2, 6, 7, 8)) {
            for (frames in listOf(0, 1, 17, 33, 257, 1001)) {
                val samples = ShortArray(frames * channels) { ((it * 7919 % 12000) - 6000).toShort() }
                val settings = UpscalerSettings(true, factor, highPrecision = channels == 7, headroomDb = 0)
                val whole = process(samples, channels, settings, 4096)
                val split = process(samples, channels, settings, 7)
                assertEquals(frames * channels * factor, whole.size)
                assertArrayEquals("factor=$factor channels=$channels frames=$frames", whole, split)
                // At each original sampling instant sinc interpolation preserves the source exactly.
                samples.indices.forEach { index ->
                    val outputIndex = index / channels * factor * channels + index % channels
                    assertEquals(samples[index], whole[outputIndex])
                }
            }
        }
    }

    @Test
    fun dcGainAndHeadroomAreNormalizedForAllPhases() {
        val samples = ShortArray(512) { 12_000 }
        for (headroom in listOf(0, 3, 6)) {
            val output = process(samples, 1, UpscalerSettings(true, 8, true, headroom), 13)
            val expected = 12_000 * 10.0.pow(-headroom / 20.0)
            output.sliceArray(512 until output.size - 512).forEach {
                assertEquals(expected, it.toDouble(), 1.0)
            }
        }
    }

    @Test
    fun filterPreservesToneAndSuppressesInterpolationImages() {
        val inputRate = 48_000
        val frequency = 10_000.0
        val input = ShortArray(4800) { (8000 * sin(2 * PI * frequency * it / inputRate)).toInt().toShort() }
        for (highPrecision in listOf(false, true)) {
            val output = process(input, 1, UpscalerSettings(true, 2, highPrecision, 0), 37)
            val steady = output.sliceArray(192 until output.size - 192)
            fun amplitude(f: Double): Double {
                var real = 0.0
                var imaginary = 0.0
                steady.forEachIndexed { i, sample ->
                    real += sample * cos(2 * PI * f * i / 96_000)
                    imaginary += sample * sin(2 * PI * f * i / 96_000)
                }
                return 2 * sqrt(real * real + imaginary * imaginary) / steady.size
            }
            assertEquals(8000.0, amplitude(frequency), 8.0)
            assertTrue("Interpolation image must be below -60 dB", amplitude(inputRate - frequency) < 8.0)
        }
    }

    @Test
    fun flushDiscardsLookaheadAndEndOfStreamDoesNotAppendSilence() {
        val processor = activeProcessor(1, UpscalerSettings(true, 8, true, 0))
        processor.queueInput(pcm(ShortArray(9) { 9000 }))
        processor.getOutput()
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        processor.queueInput(pcm(ShortArray(1) { 3000 }))
        assertFalse(processor.getOutput().hasRemaining())
        processor.queueEndOfStream()
        val result = read(processor)
        assertEquals(8, result.size)
        assertEquals(3000, result[0].toInt())
        assertTrue(processor.isEnded)
        processor.reset()
        assertFalse(processor.isActive)
    }

    @Test
    fun floatInputAndOvershootRemainFiniteAndSaturateSafely() {
        val processor = UpscalerAudioProcessor(UpscalerSettings(true, 4, false, 0)) { _, _ -> true }
        processor.configure(format(encoding = C.ENCODING_PCM_FLOAT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        val buffer = ByteBuffer.allocateDirect(5 * 4).order(ByteOrder.nativeOrder())
        listOf(Float.NaN, Float.POSITIVE_INFINITY, 2f, -2f, 0f).forEach(buffer::putFloat)
        buffer.flip()
        processor.queueInput(buffer)
        processor.getOutput()
        processor.queueEndOfStream()
        val result = read(processor)
        assertEquals(20, result.size)
        assertEquals(0, result[0].toInt())
        assertEquals(32767, result[8].toInt())
        assertEquals(-32768, result[12].toInt())
    }

    @Test
    fun endOfStreamPreservesPendingAndAlreadyReturnedOutputBuffers() {
        for (fetchBeforeEnd in listOf(false, true)) {
            val processor = activeProcessor(1, UpscalerSettings(true, 8, headroomDb = 0))
            processor.queueInput(pcm(ShortArray(50) { 1000 }))
            val held = if (fetchBeforeEnd) processor.getOutput() else null
            val before = held?.duplicate()?.let { b -> ByteArray(b.remaining()).also(b::get) }
            processor.queueEndOfStream()
            assertFalse(processor.isEnded)
            val first = held ?: processor.getOutput()
            assertEquals(272 * 2, first.remaining())
            val actual = ByteArray(first.remaining()).also(first::get)
            if (before != null) assertArrayEquals(before, actual)
            assertEquals(128, read(processor).size)
            assertTrue(processor.isEnded)
        }
    }

    @Test
    fun configuringNextFormatDoesNotChangeTheStreamBeingDrained() {
        val processor = activeProcessor(1, UpscalerSettings(true, 8))
        processor.queueInput(pcm(ShortArray(50) { 1000 }))
        val before = read(processor).size
        processor.configure(format(rate = 384_000))
        processor.queueEndOfStream()
        assertEquals(400, before + read(processor).size)
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        assertFalse(processor.isActive)
    }

    private fun process(samples: ShortArray, channels: Int, settings: UpscalerSettings, chunkFrames: Int): ShortArray {
        val processor = activeProcessor(channels, settings)
        val output = mutableListOf<Short>()
        var offset = 0
        while (offset < samples.size) {
            val end = (offset + chunkFrames * channels).coerceAtMost(samples.size)
            val input = pcm(samples.copyOfRange(offset, end))
            while (input.hasRemaining()) {
                processor.queueInput(input)
                output += read(processor).toList()
            }
            offset = end
        }
        processor.queueEndOfStream()
        output += read(processor).toList()
        assertTrue(processor.isEnded)
        return output.toShortArray()
    }

    private fun activeProcessor(channels: Int, settings: UpscalerSettings) =
        UpscalerAudioProcessor(settings) { _, _ -> true }.apply {
            configure(format(channels))
            flush(AudioProcessor.StreamMetadata.DEFAULT)
        }

    private fun read(processor: UpscalerAudioProcessor): ShortArray {
        val output = processor.getOutput().order(ByteOrder.nativeOrder())
        return ShortArray(output.remaining() / 2) { output.short }
    }

    private fun pcm(samples: ShortArray): ByteBuffer =
        ByteBuffer.allocateDirect(samples.size * 2).order(ByteOrder.nativeOrder()).apply {
            samples.forEach(::putShort)
            flip()
        }

    private fun format(channels: Int = 1, rate: Int = 48_000, encoding: Int = C.ENCODING_PCM_16BIT) =
        AudioProcessor.AudioFormat(rate, channels, encoding)
}
