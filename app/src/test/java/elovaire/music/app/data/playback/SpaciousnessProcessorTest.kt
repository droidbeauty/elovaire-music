package elovaire.music.app.data.playback

import elovaire.music.app.domain.model.SpaciousnessMode
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpaciousnessProcessorTest {
    @Test
    fun offModeIsExactBypass() {
        val processor = configuredProcessor(
            SpaciousnessConfig(
                enabled = true,
                mode = SpaciousnessMode.Off,
                amountNormalized = 1f,
            ),
        )
        val frame = floatArrayOf(0.4f, -0.2f)

        processor.processFrame(frame, 2)

        assertEquals(0.4f, frame[0], 0.0000001f)
        assertEquals(-0.2f, frame[1], 0.0000001f)
        assertTrue(processor.isBypassed())
    }

    @Test
    fun zeroAmountIsExactBypass() {
        val processor = configuredProcessor(
            SpaciousnessConfig(
                enabled = true,
                mode = SpaciousnessMode.HaasSpace,
                amountNormalized = 0f,
            ),
        )
        val frame = floatArrayOf(-0.18f, 0.23f)

        processor.processFrame(frame, 2)

        assertEquals(-0.18f, frame[0], 0.0000001f)
        assertEquals(0.23f, frame[1], 0.0000001f)
    }

    @Test
    fun stereoWidthIncreasesSideEnergy() {
        val processor = configuredProcessor(
            SpaciousnessConfig(
                enabled = true,
                mode = SpaciousnessMode.StereoWidth,
                amountNormalized = 0.85f,
            ),
        )
        val input = stereoTone(leftFrequencyHz = 1_000f, rightFrequencyHz = 1_000f, phaseOffset = PI / 2.0)
        val output = processFrames(processor, input)

        val inputSide = sideEnergy(input)
        val outputSide = sideEnergy(output)
        val inputMid = midEnergy(input)
        val outputMid = midEnergy(output)

        assertTrue(outputSide > inputSide * 1.05f)
        assertTrue(outputMid > inputMid * 0.82f)
    }

    @Test
    fun crossfeedDepthLeaksSmallOppositeChannelContent() {
        val processor = configuredProcessor(
            SpaciousnessConfig(
                enabled = true,
                mode = SpaciousnessMode.CrossfeedDepth,
                amountNormalized = 1f,
            ),
        )
        val frames = MutableList(240) { floatArrayOf(0f, 0f) }
        frames[0][0] = 1f

        val output = processFrames(processor, frames)
        val rightEnergy = output.drop(1).sumOf { abs(it[1]).toDouble() }.toFloat()

        assertTrue(rightEnergy > 0.01f)
    }

    @Test
    fun earlyReflectionRoomProducesFiniteShortReflections() {
        val processor = configuredProcessor(
            SpaciousnessConfig(
                enabled = true,
                mode = SpaciousnessMode.EarlyReflectionRoom,
                amountNormalized = 1f,
            ),
        )
        val frames = MutableList(1_200) { floatArrayOf(0f, 0f) }
        frames[0][0] = 1f
        frames[0][1] = 1f

        val output = processFrames(processor, frames)
        val tailEnergy = output.drop(1).sumOf { (abs(it[0]) + abs(it[1])).toDouble() }.toFloat()
        val peak = output.maxOf { maxOf(abs(it[0]), abs(it[1])) }

        assertTrue(tailEnergy > 0.02f)
        assertTrue(peak <= 1.2f)
        assertTrue(output.all { frame -> frame.all { sample -> sample.isFinite() } })
    }

    @Test
    fun haasSpaceMaintainsAcceptableMonoSum() {
        val processor = configuredProcessor(
            SpaciousnessConfig(
                enabled = true,
                mode = SpaciousnessMode.HaasSpace,
                amountNormalized = 1f,
            ),
        )
        val input = stereoTone(leftFrequencyHz = 800f, rightFrequencyHz = 1_100f)
        val output = processFrames(processor, input)
        val monoSumPeak = output.maxOf { abs((it[0] + it[1]) * 0.5f) }

        assertTrue(monoSumPeak > 0.05f)
        assertTrue(output.all { frame -> frame.all { sample -> sample.isFinite() } })
    }

    @Test
    fun harmonicAirAffectsHighFrequenciesMoreThanBass() {
        val processor = configuredProcessor(
            SpaciousnessConfig(
                enabled = true,
                mode = SpaciousnessMode.HarmonicAir,
                amountNormalized = 1f,
            ),
        )
        val lowSource = stereoTone(leftFrequencyHz = 90f, rightFrequencyHz = 90f)
        val lowOutput = processFrames(processor, lowSource)
        processor.reset()
        val highSource = stereoTone(leftFrequencyHz = 6_000f, rightFrequencyHz = 7_200f, phaseOffset = PI / 3.0)
        val highOutput = processFrames(processor, highSource)

        val lowDifference = averageDelta(lowOutput, lowSource)
        val highDifference = averageDelta(highOutput, highSource)

        assertTrue(highDifference > 0f)
    }

    @Test
    fun resetClearsDelayMemory() {
        val processor = configuredProcessor(
            SpaciousnessConfig(
                enabled = true,
                mode = SpaciousnessMode.EarlyReflectionRoom,
                amountNormalized = 1f,
            ),
        )
        processFrames(processor, listOf(floatArrayOf(1f, 0f)) + List(200) { floatArrayOf(0f, 0f) })
        processor.reset()
        val silentOutput = processFrames(processor, List(180) { floatArrayOf(0f, 0f) })

        val residual = silentOutput.sumOf { (abs(it[0]) + abs(it[1])).toDouble() }.toFloat()
        assertTrue(residual < 0.0001f)
    }

    @Test
    fun bitPerfectBypassLeavesFramesUntouched() {
        val processor = configuredProcessor(
            SpaciousnessConfig(
                enabled = true,
                mode = SpaciousnessMode.StereoWidth,
                amountNormalized = 1f,
            ),
        )
        val frame = floatArrayOf(0.22f, -0.37f)
        processor.setBitPerfectBypass(true)

        processor.processFrame(frame, 2)

        assertEquals(0.22f, frame[0], 0.0000001f)
        assertEquals(-0.37f, frame[1], 0.0000001f)
        assertTrue(processor.diagnosticsSnapshot().bypassed)
    }

    @Test
    fun eachModeProducesDistinctOutput() {
        val source = stereoTone(leftFrequencyHz = 440f, rightFrequencyHz = 880f)
        val signatures = SpaciousnessMode.entries
            .filterNot { it == SpaciousnessMode.Off }
            .associateWith { mode ->
                val processor = configuredProcessor(
                    SpaciousnessConfig(
                        enabled = true,
                        mode = mode,
                        amountNormalized = 0.75f,
                    ),
                )
                processFrames(processor, source).take(48).flatMap { listOf(it[0], it[1]) }
            }

        val distinctPairwise = signatures.values.zipWithNext().all { (first, second) ->
            averageDelta(first.chunked(2).map { floatArrayOf(it[0], it[1]) }, second.chunked(2).map { floatArrayOf(it[0], it[1]) }) > 0.0005f
        }
        assertTrue(distinctPairwise)
    }

    @Test
    fun monoInputDoesNotCreateUnstableStereo() {
        val processor = configuredProcessor(
            SpaciousnessConfig(
                enabled = true,
                mode = SpaciousnessMode.HarmonicAir,
                amountNormalized = 1f,
            ),
            channelCount = 1,
        )
        val frame = floatArrayOf(0.25f)
        processor.processFrame(frame, 1)
        assertEquals(0.25f, frame[0], 0.0000001f)
        assertTrue(processor.isBypassed())
    }

    private fun configuredProcessor(
        config: SpaciousnessConfig,
        sampleRateHz: Int = 48_000,
        channelCount: Int = 2,
    ): SpaciousnessProcessor {
        return SpaciousnessProcessor().apply {
            configure(sampleRateHz = sampleRateHz, channelCount = channelCount)
            setConfig(config)
        }
    }

    private fun processFrames(
        processor: SpaciousnessProcessor,
        frames: List<FloatArray>,
    ): List<FloatArray> {
        return frames.map { source ->
            val copy = source.copyOf()
            processor.processFrame(copy, copy.size)
            copy
        }
    }

    private fun stereoTone(
        leftFrequencyHz: Float,
        rightFrequencyHz: Float,
        phaseOffset: Double = 0.0,
        frames: Int = 1_024,
        sampleRateHz: Int = 48_000,
    ): List<FloatArray> {
        return List(frames) { index ->
            floatArrayOf(
                (0.35f * sin(2.0 * PI * leftFrequencyHz * index / sampleRateHz)).toFloat(),
                (0.35f * sin((2.0 * PI * rightFrequencyHz * index / sampleRateHz) + phaseOffset)).toFloat(),
            )
        }
    }

    private fun sideEnergy(frames: List<FloatArray>): Float {
        return rms(frames.map { (it[0] - it[1]) * 0.5f }.toFloatArray())
    }

    private fun midEnergy(frames: List<FloatArray>): Float {
        return rms(frames.map { (it[0] + it[1]) * 0.5f }.toFloatArray())
    }

    private fun averageDelta(
        first: List<FloatArray>,
        second: List<FloatArray>,
    ): Float {
        val frameCount = min(first.size, second.size)
        var total = 0f
        repeat(frameCount) { index ->
            total += abs(first[index][0] - second[index][0]) + abs(first[index][1] - second[index][1])
        }
        return total / frameCount.toFloat()
    }

    private fun rms(samples: FloatArray): Float {
        val meanSquare = samples.fold(0.0) { acc, sample -> acc + sample * sample } / samples.size.toDouble()
        return sqrt(meanSquare).toFloat()
    }
}
