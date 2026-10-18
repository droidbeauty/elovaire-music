package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioTrack
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import elovaire.music.droidbeauty.app.domain.model.UpscalerSettings
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/** Streaming, linear-phase Blackman-windowed sinc interpolation, independent for each channel. */
@UnstableApi
internal class UpscalerAudioProcessor(
    settings: UpscalerSettings,
    private val supportsOutput: (Int, Int) -> Boolean = { rate, channels ->
        val mask = Util.getAudioTrackChannelConfig(channels)
        mask != 0 && AudioTrack.getMinBufferSize(rate, mask, C.ENCODING_PCM_16BIT) > 0
    },
) : BaseAudioProcessor() {
    private val settings = settings.sanitized()
    private val radius = if (settings.highPrecision) 32 else 16
    private val gain = 10.0.pow(-this.settings.headroomDb / 20.0).toFloat()
    private var factor = 1
    private var pendingFactor = 1
    private var kernels = emptyArray<FloatArray>()
    private var history = FloatArray(0)
    private var frameSamples = FloatArray(0)
    private var inputFrames = 0L
    private var outputFrames = 0L
    private var tailOutput = AudioProcessor.EMPTY_BUFFER
    private val capacity = radius * 2 + 1

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        pendingFactor = 1
        if (!settings.enabled || inputAudioFormat.channelCount !in 1..8 ||
            inputAudioFormat.sampleRate <= 0 ||
            inputAudioFormat.encoding !in intArrayOf(C.ENCODING_PCM_16BIT, C.ENCODING_PCM_FLOAT)
        ) return AudioProcessor.AudioFormat.NOT_SET
        var candidate = settings.multiplier
        while (candidate >= 2) {
            val rate = inputAudioFormat.sampleRate.toLong() * candidate
            if (rate <= MAX_OUTPUT_RATE && supportsOutput(rate.toInt(), inputAudioFormat.channelCount)) {
                pendingFactor = candidate
                return AudioProcessor.AudioFormat(rate.toInt(), inputAudioFormat.channelCount, C.ENCODING_PCM_16BIT)
            }
            candidate /= 2
        }
        return AudioProcessor.AudioFormat.NOT_SET
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        tailOutput = AudioProcessor.EMPTY_BUFFER
        factor = pendingFactor
        inputFrames = 0
        outputFrames = 0
        frameSamples = FloatArray(inputAudioFormat.channelCount.coerceAtLeast(0))
        history = FloatArray(capacity * frameSamples.size)
        kernels = if (factor > 1) Array(factor) { phase -> kernel(phase.toDouble() / factor) } else emptyArray()
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val channels = inputAudioFormat.channelCount
        val frames = (inputBuffer.remaining() / inputAudioFormat.bytesPerFrame).coerceAtMost(BLOCK_FRAMES)
        if (frames == 0) return
        val output = replaceOutputBuffer(frames * factor * channels * 2)
        repeat(frames) {
            val offset = (inputFrames % capacity).toInt() * channels
            repeat(channels) { channel ->
                history[offset + channel] = if (inputAudioFormat.encoding == C.ENCODING_PCM_FLOAT) {
                    inputBuffer.float.let { if (it.isFinite()) it.coerceIn(-1f, 1f) else 0f }
                } else {
                    inputBuffer.short / 32768f
                }
            }
            inputFrames++
            while (outputFrames / factor + radius < inputFrames) writeFrame(output)
        }
        output.flip()
    }

    override fun onQueueEndOfStream() {
        // Lookahead is zero-padded; exactly N * factor frames are emitted, without an added tail.
        val remaining = inputFrames * factor - outputFrames
        // Keep the tail separate: the caller may still hold the previous output buffer.
        tailOutput = ByteBuffer.allocateDirect((remaining * inputAudioFormat.channelCount * 2).toInt())
            .order(ByteOrder.nativeOrder())
        while (outputFrames < inputFrames * factor) writeFrame(tailOutput)
        tailOutput.flip()
    }

    override fun getOutput(): ByteBuffer {
        val output = super.getOutput()
        if (output.hasRemaining()) return output
        return tailOutput.also { tailOutput = AudioProcessor.EMPTY_BUFFER }
    }

    override fun isEnded(): Boolean = super.isEnded() && !tailOutput.hasRemaining()

    private fun writeFrame(output: ByteBuffer) {
        val center = outputFrames / factor
        val coefficients = kernels[(outputFrames % factor).toInt()]
        val channels = inputAudioFormat.channelCount
        frameSamples.fill(0f)
        for (tap in coefficients.indices) {
            val frame = center + tap - radius + 1
            if (frame >= 0 && frame < inputFrames) {
                val offset = (frame % capacity).toInt() * channels
                val weight = coefficients[tap]
                repeat(channels) { channel ->
                    frameSamples[channel] += history[offset + channel] * weight
                }
            }
        }
        repeat(channels) { channel ->
            output.putShort((frameSamples[channel] * gain * 32768f).roundToInt().coerceIn(-32768, 32767).toShort())
        }
        outputFrames++
    }

    private fun kernel(fraction: Double): FloatArray {
        val weights = DoubleArray(radius * 2) { tap ->
            val distance = tap - radius + 1 - fraction
            val sinc = if (distance == 0.0) 1.0 else sin(PI * distance) / (PI * distance)
            val window = 0.42 + 0.5 * cos(PI * distance / radius) + 0.08 * cos(2 * PI * distance / radius)
            sinc * window
        }
        val sum = weights.sum()
        return FloatArray(weights.size) { (weights[it] / sum).toFloat() }
    }

    override fun onReset() {
        history = FloatArray(0)
        frameSamples = FloatArray(0)
        kernels = emptyArray()
        inputFrames = 0
        outputFrames = 0
        factor = 1
        pendingFactor = 1
        tailOutput = AudioProcessor.EMPTY_BUFFER
    }

    private companion object {
        const val BLOCK_FRAMES = 256
        const val MAX_OUTPUT_RATE = 384_000L
    }
}
