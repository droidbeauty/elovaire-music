package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioSink
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal object CrossfadeSilencePolicy {
    const val BASE_LEVEL_DB = -80f
    const val MIN_SILENCE_DURATION_MS = 100L
    const val MAX_EARLY_START_MS = CrossfadeDurationPolicy.DEFAULT_DURATION_MS

    // -80 dBFS expressed as a linear PCM sample amplitude.
    const val BASE_AMPLITUDE_THRESHOLD = 0.0001f
}

internal class CrossfadeSilenceDetector {
    private var format: Format? = null
    private var silentStartUs = Long.MIN_VALUE
    private var silentEndUs = Long.MIN_VALUE

    fun configure(inputFormat: Format) {
        format = inputFormat
        reset()
    }

    fun observe(buffer: ByteBuffer, presentationTimeUs: Long) {
        val inputFormat = format ?: return
        val bytesPerSample = bytesPerSample(inputFormat.pcmEncoding) ?: return
        val channelCount = inputFormat.channelCount.takeIf { it > 0 } ?: return
        val bytesPerFrame = bytesPerSample * channelCount
        val frameCount = buffer.remaining() / bytesPerFrame
        if (frameCount <= 0 || inputFormat.sampleRate <= 0) return

        val sampleBuffer = buffer.duplicate().order(ByteOrder.nativeOrder())
        var silent = true
        while (sampleBuffer.remaining() >= bytesPerSample) {
            if (sampleAmplitude(sampleBuffer, inputFormat.pcmEncoding) > CrossfadeSilencePolicy.BASE_AMPLITUDE_THRESHOLD) {
                silent = false
                break
            }
        }

        val durationUs = frameCount * 1_000_000L / inputFormat.sampleRate
        if (!silent) {
            silentStartUs = Long.MIN_VALUE
            silentEndUs = Long.MIN_VALUE
            return
        }

        val endUs = presentationTimeUs + durationUs
        if (silentEndUs == presentationTimeUs || silentEndUs == Long.MIN_VALUE) {
            silentStartUs = if (silentEndUs == Long.MIN_VALUE) presentationTimeUs else silentStartUs
            silentEndUs = endUs
        } else if (presentationTimeUs <= silentEndUs) {
            silentEndUs = maxOf(silentEndUs, endUs)
        } else {
            silentStartUs = presentationTimeUs
            silentEndUs = endUs
        }
    }

    fun isSilentAt(positionUs: Long, minimumDurationMs: Long): Boolean {
        if (silentStartUs == Long.MIN_VALUE || silentEndUs == Long.MIN_VALUE) return false
        val minimumDurationUs = minimumDurationMs.coerceAtLeast(0L) * 1_000L
        return positionUs >= silentStartUs && silentEndUs - positionUs >= minimumDurationUs
    }

    fun reset() {
        silentStartUs = Long.MIN_VALUE
        silentEndUs = Long.MIN_VALUE
    }

    private fun bytesPerSample(encoding: Int): Int? = when (encoding) {
        C.ENCODING_PCM_16BIT -> 2
        C.ENCODING_PCM_24BIT -> 3
        C.ENCODING_PCM_32BIT, C.ENCODING_PCM_FLOAT -> 4
        else -> null
    }

    private fun sampleAmplitude(buffer: ByteBuffer, encoding: Int): Float {
        return when (encoding) {
            C.ENCODING_PCM_16BIT -> kotlin.math.abs(buffer.short.toFloat() / 32_768f)
            C.ENCODING_PCM_24BIT -> {
                val value = buffer.get().toInt() and 0xff or
                    ((buffer.get().toInt() and 0xff) shl 8) or
                    (buffer.get().toInt() shl 16)
                kotlin.math.abs(value / 8_388_608f)
            }
            C.ENCODING_PCM_32BIT -> kotlin.math.abs(buffer.int.toFloat() / 2_147_483_648f)
            C.ENCODING_PCM_FLOAT -> kotlin.math.abs(buffer.float)
            else -> 1f
        }
    }
}

@UnstableApi
internal class CrossfadeSilenceDetectingAudioSink(
    private val delegate: AudioSink,
    private val detector: CrossfadeSilenceDetector,
) : AudioSink by delegate {
    private var inputFormat: Format? = null

    override fun configure(inputFormat: Format, specifiedBufferSize: Int, outputChannels: IntArray?) {
        this.inputFormat = inputFormat
        detector.configure(inputFormat)
        delegate.configure(inputFormat, specifiedBufferSize, outputChannels)
    }

    override fun handleBuffer(
        buffer: ByteBuffer,
        presentationTimeUs: Long,
        encodedAccessUnitCount: Int,
    ): Boolean {
        if (inputFormat?.pcmEncoding != Format.NO_VALUE) {
            detector.observe(buffer.duplicate(), presentationTimeUs)
        }
        return delegate.handleBuffer(buffer, presentationTimeUs, encodedAccessUnitCount)
    }

    override fun flush() {
        detector.reset()
        delegate.flush()
    }

    override fun reset() {
        detector.reset()
        inputFormat = null
        delegate.reset()
    }
}
