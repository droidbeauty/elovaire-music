package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class EqualizerAudioProcessorTest {
    @Test
    fun bypassReadinessWaitsForTheEffectTransitionToSettle() {
        val processor = EqualizerAudioProcessor()
        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        assertTrue(processor.isSettledForBypass)
        processor.updateSettings(EqSettings(preampDb = -6f))
        assertFalse(processor.isSettledForBypass)
        val input = ByteBuffer.allocateDirect(4_096 * 4).order(ByteOrder.nativeOrder())
        repeat(4_096 * 2) { input.putShort(2_000) }
        repeat(12) {
            input.flip()
            processor.queueInput(input)
            processor.getOutput()
        }
        processor.updateSettings(EqSettings())
        assertFalse(processor.isSettledForBypass)
        repeat(12) {
            input.flip()
            processor.queueInput(input)
            processor.getOutput()
        }
        input.flip()
        processor.queueInput(input)
        processor.getOutput()
        assertTrue(processor.isSettledForBypass)
        processor.reset()
    }
    @Test
    fun fragmentedPcmFramesProduceTheSameProcessedStreamAsContiguousInput() {
        val samples = ByteBuffer.allocate(1_028).order(ByteOrder.nativeOrder())
        repeat(514) { samples.putShort(((it * 7919 % 20_001) - 10_000).toShort()) }
        val input = samples.array()
        val settings = EqSettings(
            preampDb = -1f,
            bands = List(EqualizerDspModel.BAND_COUNT) { if (it == 8) 0.5f else 0f },
        )

        fun process(chunkSizes: IntArray): ByteArray {
            val processor = EqualizerAudioProcessor()
            processor.updateSettings(settings)
            processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
            processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
            val result = ByteArrayOutputStream()
            var position = 0
            var chunk = 0
            while (position < input.size) {
                val size = minOf(chunkSizes[chunk++ % chunkSizes.size], input.size - position)
                val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
                buffer.put(input, position, size).flip()
                processor.queueInput(buffer)
                assertEquals("input bytes must be consumed", size, buffer.position())
                val output = processor.output
                result.write(ByteArray(output.remaining()).also(output::get))
                position += size
            }
            processor.queueEndOfStream()
            val tail = processor.output
            result.write(ByteArray(tail.remaining()).also(tail::get))
            assertTrue(processor.isEnded)
            processor.reset()
            return result.toByteArray()
        }

        assertArrayEquals(process(intArrayOf(input.size)), process(intArrayOf(1, 2, 7, 3, 11, 4)))
    }

    @Test
    fun fragmentedNeutralPcmPreservesBytesForEverySupportedFormat() {
        for ((encoding, bytesPerSample) in listOf(
            C.ENCODING_PCM_16BIT to 2,
            C.ENCODING_PCM_24BIT to 3,
            C.ENCODING_PCM_32BIT to 4,
            C.ENCODING_PCM_FLOAT to 4,
        )) {
            for (channels in listOf(1, 2, 6)) {
                val processor = EqualizerAudioProcessor()
                processor.configure(AudioProcessor.AudioFormat(48_000, channels, encoding))
                processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
                val input = ByteArray(257 * channels * bytesPerSample)
                java.util.Random((encoding * 31L) + channels).nextBytes(input)
                val actual = ByteArrayOutputStream()
                var offset = 0
                while (offset < input.size) {
                    val count = minOf((offset % 11) + 1, input.size - offset)
                    val buffer = ByteBuffer.allocateDirect(count).order(ByteOrder.nativeOrder())
                    buffer.put(input, offset, count).flip()
                    processor.queueInput(buffer)
                    assertEquals(count, buffer.position())
                    val output = processor.output
                    actual.write(ByteArray(output.remaining()).also(output::get))
                    offset += count
                }
                processor.queueEndOfStream()
                assertTrue(processor.isEnded)
                assertArrayEquals("encoding=$encoding channels=$channels", input, actual.toByteArray())
                processor.reset()
            }
        }
    }

    @Test
    fun fragmentedActivePcmMatchesContiguousProcessingForEverySupportedFormat() {
        for ((encoding, bytesPerSample) in listOf(
            C.ENCODING_PCM_16BIT to 2,
            C.ENCODING_PCM_24BIT to 3,
            C.ENCODING_PCM_32BIT to 4,
            C.ENCODING_PCM_FLOAT to 4,
        )) {
            for (channels in listOf(1, 2, 6)) {
                val input = ByteArray(257 * channels * bytesPerSample)
                java.util.Random((encoding * 97L) + channels).nextBytes(input)
                fun process(fragmented: Boolean): ByteArray {
                    val processor = EqualizerAudioProcessor()
                    processor.updateSettings(EqSettings(preampDb = -2f, bass = 0.2f))
                    processor.configure(AudioProcessor.AudioFormat(48_000, channels, encoding))
                    processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
                    val result = ByteArrayOutputStream()
                    var offset = 0
                    while (offset < input.size) {
                        val count = if (fragmented) minOf((offset % 13) + 1, input.size - offset) else input.size
                        val buffer = ByteBuffer.allocateDirect(count).order(ByteOrder.nativeOrder())
                        buffer.put(input, offset, count).flip()
                        processor.queueInput(buffer)
                        assertEquals(count, buffer.position())
                        val output = processor.output
                        result.write(ByteArray(output.remaining()).also(output::get))
                        offset += count
                    }
                    processor.queueEndOfStream()
                    assertTrue(processor.isEnded)
                    processor.reset()
                    return result.toByteArray()
                }
                assertArrayEquals("encoding=$encoding channels=$channels", process(false), process(true))
            }
        }
    }

    @Test
    fun incompleteFinalFrameIsReportedAndFlushClearsTheFragment() {
        val processor = EqualizerAudioProcessor()
        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        val incomplete = ByteBuffer.allocateDirect(3).order(ByteOrder.nativeOrder())
        incomplete.put(byteArrayOf(1, 2, 3)).flip()
        processor.queueInput(incomplete)
        assertEquals(3, incomplete.position())
        assertEquals(0, processor.output.remaining())
        assertThrows(IllegalStateException::class.java) { processor.queueEndOfStream() }

        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        val complete = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())
        complete.put(byteArrayOf(4, 5, 6, 7)).flip()
        processor.queueInput(complete)
        val output = processor.output
        assertArrayEquals(byteArrayOf(4, 5, 6, 7), ByteArray(output.remaining()).also(output::get))
        processor.queueEndOfStream()
        assertTrue(processor.isEnded)
        processor.reset()
    }

    @Test
    fun processedIntegerSamplesUseTheFullSignedScale() {
        for ((encoding, bytesPerSample, magnitude) in listOf(
            Triple(C.ENCODING_PCM_16BIT, 2, 20_000),
            Triple(C.ENCODING_PCM_24BIT, 3, 7_000_000),
            Triple(C.ENCODING_PCM_32BIT, 4, 1_500_000_000),
        )) {
            val processor = EqualizerAudioProcessor()
            // This nonzero setting rounds to unity in the processor's float gain calculation.
            processor.updateSettings(EqSettings(preampDb = Float.MIN_VALUE))
            processor.configure(AudioProcessor.AudioFormat(48_000, 2, encoding))
            processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
            val input = ByteBuffer.allocateDirect(2 * bytesPerSample).order(ByteOrder.nativeOrder())
            fun putInteger(value: Int) {
                when (bytesPerSample) {
                    2 -> input.putShort(value.toShort())
                    3 -> input.put(value.toByte()).put((value shr 8).toByte()).put((value shr 16).toByte())
                    else -> input.putInt(value)
                }
            }
            putInteger(magnitude)
            putInteger(-magnitude)
            input.flip()
            val expected = ByteArray(input.remaining()).also { input.duplicate().get(it) }

            processor.queueInput(input)
            val output = processor.output
            val actual = ByteArray(output.remaining()).also(output::get)
            assertArrayEquals("encoding=$encoding", expected, actual)
            processor.reset()
        }
    }

    @Test
    fun processedPcm16PreservesUnityGainValuesBelowTheLimiterKnee() {
        val processor = EqualizerAudioProcessor()
        processor.updateSettings(EqSettings(preampDb = Float.MIN_VALUE))
        processor.configure(AudioProcessor.AudioFormat(48_000, 1, C.ENCODING_PCM_16BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        val input = ByteBuffer.allocateDirect(56_001 * 2).order(ByteOrder.nativeOrder())
        for (sample in -28_000..28_000) input.putShort(sample.toShort())
        input.flip()
        val expected = ByteArray(input.remaining()).also { input.duplicate().get(it) }

        processor.queueInput(input)
        val output = processor.output
        val actual = ByteArray(output.remaining()).also(output::get)
        assertArrayEquals(expected, actual)
        processor.reset()
    }

    @Test
    fun processedPcm24PreservesRepresentableUnityGainSamplesBelowTheLimiterKnee() {
        val processor = EqualizerAudioProcessor()
        processor.updateSettings(EqSettings(preampDb = Float.MIN_VALUE))
        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_24BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        val random = java.util.Random(0x50434dL)
        val input = ByteBuffer.allocateDirect(20_000 * 3).order(ByteOrder.nativeOrder())
        repeat(20_000) {
            val sample = random.nextInt(14_000_001) - 7_000_000
            input.put(sample.toByte()).put((sample shr 8).toByte()).put((sample shr 16).toByte())
        }
        input.flip()
        val expected = ByteArray(input.remaining()).also { input.duplicate().get(it) }

        processor.queueInput(input)
        val output = processor.output
        val actual = ByteArray(output.remaining()).also(output::get)
        assertArrayEquals(expected, actual)
        processor.reset()
    }

    @Test
    fun processedPcm32QuantizationRemainsWithinFloatPrecision() {
        val processor = EqualizerAudioProcessor()
        processor.updateSettings(EqSettings(preampDb = Float.MIN_VALUE))
        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_32BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        val random = java.util.Random(0x333250L)
        val samples = IntArray(20_000) { random.nextInt(1_700_000_001) - 850_000_000 }
        val input = ByteBuffer.allocateDirect(samples.size * 4).order(ByteOrder.nativeOrder())
        samples.forEach(input::putInt)
        input.flip()

        processor.queueInput(input)
        val output = processor.output.order(ByteOrder.nativeOrder())
        assertEquals(samples.size * 4, output.remaining())
        var maximumError = 0L
        samples.forEach { expected ->
            val actual = output.int
            maximumError = maxOf(maximumError, kotlin.math.abs(actual.toLong() - expected.toLong()))
        }
        assertTrue("PCM32 maximum error was $maximumError integer counts", maximumError <= 128L)
        println("PCM32 unity-path maximum absolute error: $maximumError integer counts")
        processor.reset()
    }

    @Test
    fun nonFiniteFloatInputDoesNotPoisonSubsequentProcessedSamples() {
        val processor = EqualizerAudioProcessor()
        processor.updateSettings(EqSettings(bands = List(18) { if (it == 8) 0.5f else 0f }))
        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_FLOAT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        val input = ByteBuffer.allocateDirect(256 * 2 * 4).order(ByteOrder.nativeOrder())
        input.putFloat(Float.NaN).putFloat(Float.POSITIVE_INFINITY)
        repeat(255) { input.putFloat(0.1f).putFloat(-0.1f) }
        input.flip()

        processor.queueInput(input)
        val output = processor.output.order(ByteOrder.nativeOrder())
        assertEquals(256 * 2 * 4, output.remaining())
        var left = 0f
        var right = 0f
        while (output.hasRemaining()) {
            left = output.float
            right = output.float
            assertTrue(left.isFinite() && right.isFinite())
        }
        assertTrue(left > 0f && right < 0f)
        processor.reset()
    }

    @Test
    fun preampChangesGainWhenAllOtherControlsAreFlat() {
        for (gainDb in listOf(-6f, 6f)) {
            val processor = EqualizerAudioProcessor()
            processor.updateSettings(EqSettings(preampDb = gainDb))
            processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
            processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
            val input = ByteBuffer.allocateDirect(48_000 * 4).order(ByteOrder.nativeOrder())
            repeat(48_000 * 2) { input.putShort(2_000) }
            input.flip()
            processor.queueInput(input)
            val output = processor.output.order(ByteOrder.nativeOrder())
            output.position(output.limit() - 4)
            val expected = 2_000 * Math.pow(10.0, gainDb / 20.0)
            assertEquals(expected, output.short.toDouble(), 3.0)
            assertEquals(expected, output.short.toDouble(), 3.0)
            processor.reset()
        }
    }

    @Test
    fun neutralProcessingPreservesEveryInputByteAcrossFormatsAndChunks() {
        val random = java.util.Random(0x454c4fL)
        for ((encoding, bytesPerSample) in listOf(
            C.ENCODING_PCM_16BIT to 2,
            C.ENCODING_PCM_24BIT to 3,
            C.ENCODING_PCM_32BIT to 4,
            C.ENCODING_PCM_FLOAT to 4,
        )) {
            for (channels in listOf(1, 2, 6)) {
                val processor = EqualizerAudioProcessor()
                val format = AudioProcessor.AudioFormat(48_000, channels, encoding)
                processor.configure(format)
                processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
                for (frames in listOf(1, 17, 257)) {
                    val expected = ByteArray(frames * channels * bytesPerSample).also(random::nextBytes)
                    val input = ByteBuffer.allocateDirect(expected.size).order(ByteOrder.nativeOrder())
                    input.put(expected).flip()
                    processor.queueInput(input)
                    val output = processor.output
                    val actual = ByteArray(output.remaining()).also(output::get)
                    assertArrayEquals("encoding=$encoding channels=$channels frames=$frames", expected, actual)
                    assertEquals(input.limit(), input.position())
                }
                processor.queueEndOfStream()
                assertTrue(processor.isEnded)
                processor.reset()
            }
        }
    }

    @Test
    fun queueInput_copiesPcmThroughWhenEffectsAreOff() {
        val processor = EqualizerAudioProcessor()
        val input = ByteBuffer.allocateDirect(8)
            .order(ByteOrder.nativeOrder())
            .putShort((-12_000).toShort())
            .putShort(4_000.toShort())
            .putShort(8_000.toShort())
            .putShort((-2_000).toShort())
            .flip() as ByteBuffer
        val expected = ByteArray(input.remaining()).also { input.duplicate().get(it) }

        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        processor.queueInput(input)
        val output = processor.getOutput()
        val actual = ByteArray(output.remaining()).also(output::get)

        assertArrayEquals(expected, actual)
        assertTrue(processor.debugSnapshot().dspBypassed)
    }

    @Test
    fun surroundChannelsBypassStereoSceneEffectsAndRestoreThemForStereo() {
        val processor = EqualizerAudioProcessor()
        processor.updateSettings(EqSettings(spaciousness = 0.8f, reverbDurationMs = 500))
        listOf(6, 7, 8).forEach { channels ->
            processor.configure(AudioProcessor.AudioFormat(48_000, channels, C.ENCODING_PCM_16BIT))
            processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
            val input = ByteBuffer.allocateDirect(channels * 2 * 64).order(ByteOrder.nativeOrder())
            repeat(64) { repeat(channels) { channel -> input.putShort((1000 * (channel + 1)).toShort()) } }
            input.flip()
            val expected = ByteArray(input.remaining()).also { input.duplicate().get(it) }

            processor.queueInput(input)
            val output = processor.getOutput()
            val actual = ByteArray(output.remaining()).also(output::get)

            assertArrayEquals("$channels channels must retain their samples and order", expected, actual)
            assertTrue(processor.debugSnapshot().dspBypassed)
        }

        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        processor.queueInput(pcmBuffer())
        assertTrue(!processor.debugSnapshot().dspBypassed)
        processor.reset()
    }

    @Test
    fun queueInput_processesOnlyActiveEqBands() {
        val processor = EqualizerAudioProcessor()
        processor.updateSettings(
            EqSettings(bands = List(EqualizerDspModel.BAND_COUNT) { index -> if (index == 3) 0.6f else 0f }),
        )
        val input = ByteBuffer.allocateDirect(16)
            .order(ByteOrder.nativeOrder())
            .putShort(8_000.toShort())
            .putShort((-4_000).toShort())
            .putShort(6_000.toShort())
            .putShort((-3_000).toShort())
            .putShort(4_000.toShort())
            .putShort((-2_000).toShort())
            .putShort(2_000.toShort())
            .putShort((-1_000).toShort())
            .flip() as ByteBuffer

        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        processor.queueInput(input)

        val diagnostics = processor.debugSnapshot()
        assertEquals(1, diagnostics.activeFilterCount)
        assertEquals(1L, diagnostics.coefficientPlanBuilds)
        assertEquals(52L, diagnostics.coefficientApplications)
    }

    @Test
    fun controlThreadSettingsAreAppliedOnTheAudioThread() {
        val processor = EqualizerAudioProcessor()
        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        val activeSettings = EqSettings(
            bands = List(EqualizerDspModel.BAND_COUNT) { index -> if (index == 3) 0.6f else 0f },
        )
        val controlThread = Thread {
            repeat(200) { index ->
                processor.updateSettings(if (index % 2 == 0) EqSettings() else activeSettings)
            }
            processor.updateSettings(activeSettings)
        }
        val audioThread = Thread {
            repeat(200) {
                processor.queueInput(pcmBuffer())
                processor.getOutput()
            }
        }

        controlThread.start()
        audioThread.start()
        controlThread.join()
        audioThread.join()
        processor.queueInput(pcmBuffer())

        assertEquals(1, processor.debugSnapshot().activeFilterCount)
    }

    @Test
    fun flushRestoresTheSameGainAndOutputAsAFreshProcessor() {
        val settings = EqSettings(preampDb = 3f, bass = 0.7f)
        fun processor() = EqualizerAudioProcessor().apply {
            configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
            updateSettings(settings)
            flush(AudioProcessor.StreamMetadata.DEFAULT)
        }
        fun output(processor: EqualizerAudioProcessor): ByteArray {
            processor.queueInput(pcmBuffer())
            val buffer = processor.getOutput()
            return ByteArray(buffer.remaining()).also(buffer::get)
        }
        val reused = processor()
        repeat(100) { output(reused) }
        reused.flush(AudioProcessor.StreamMetadata.DEFAULT)

        assertArrayEquals(output(processor()), output(reused))
    }

    @Test
    fun limiterBoundsBoostedSamples() {
        val processor = EqualizerAudioProcessor()
        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
        processor.updateSettings(EqSettings(preampDb = 6f))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        val input = ByteBuffer.allocateDirect(2_048 * 2).order(ByteOrder.nativeOrder())
        repeat(2_048) { input.putShort(if (it % 2 == 0) 32_000 else -32_000) }
        input.flip()

        processor.queueInput(input)

        val output = processor.getOutput().order(ByteOrder.nativeOrder())
        assertEquals(2_048 * 2, output.remaining())
        while (output.hasRemaining()) assertTrue(kotlin.math.abs(output.short.toInt()) <= 29_197)
        assertTrue(processor.debugSnapshot().limiterEvents > 0)
    }

    private fun pcmBuffer(): ByteBuffer = ByteBuffer.allocateDirect(16)
        .order(ByteOrder.nativeOrder())
        .putShort(8_000.toShort())
        .putShort((-4_000).toShort())
        .putShort(6_000.toShort())
        .putShort((-3_000).toShort())
        .putShort(4_000.toShort())
        .putShort((-2_000).toShort())
        .putShort(2_000.toShort())
        .putShort((-1_000).toShort())
        .flip() as ByteBuffer
}
