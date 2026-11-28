package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioTrack
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.ToInt16PcmAudioProcessor
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.ForwardingAudioSink
import java.nio.ByteBuffer

/** Keeps the stock sink's float path available while routing requested processing through PCM16. */
@UnstableApi
internal class PrecisionSwitchingAudioSink(
    sink: AudioSink,
    private val processingRequired: () -> Boolean,
    private val processorsSettled: () -> Boolean,
) : ForwardingAudioSink(sink) {
    private val converter = ToInt16PcmAudioProcessor()
    private var originalConfig: AudioSink.AudioSinkConfig? = null
    private var highResolution = false
    private var maySwitch = false
    private var converted = false
    private var playbackParameters = PlaybackParameters.DEFAULT
    private var skipSilence = false
    private var pendingInput: ByteBuffer? = null
    private var pendingOutput = AudioProcessor.EMPTY_BUFFER
    private var pendingInputStart = 0
    private var inputBytesPerSample = 0

    override fun supportsFormat(format: Format): Boolean = getFormatSupport(format) != AudioSink.SINK_FORMAT_UNSUPPORTED

    override fun getFormatSupport(format: Format): Int {
        val originalSupport = super.getFormatSupport(format)
        if (format.sampleMimeType != MimeTypes.AUDIO_RAW || !Util.isEncodingHighResolutionPcm(format.pcmEncoding)) {
            return originalSupport
        }
        val requiresConversion = originalSupport == AudioSink.SINK_FORMAT_UNSUPPORTED ||
            format.encoderDelay != 0 || format.encoderPadding != 0 || needsProcessing()
        if (!requiresConversion) return originalSupport
        val pcm16 = format.buildUpon().setPcmEncoding(C.ENCODING_PCM_16BIT).build()
        return if (super.getFormatSupport(pcm16) == AudioSink.SINK_FORMAT_UNSUPPORTED) {
            AudioSink.SINK_FORMAT_UNSUPPORTED
        } else {
            AudioSink.SINK_FORMAT_SUPPORTED_WITH_TRANSCODING
        }
    }

    override fun configure(audioSinkConfig: AudioSink.AudioSinkConfig) {
        originalConfig = audioSinkConfig
        val format = audioSinkConfig.format
        highResolution = format.sampleMimeType == MimeTypes.AUDIO_RAW &&
            Util.isEncodingHighResolutionPcm(format.pcmEncoding)
        // Reconfiguring Media3's trimming processor mid-stream would discard its held padding.
        maySwitch = highResolution && format.encoderDelay == 0 && format.encoderPadding == 0 &&
            super.getFormatSupport(format) != AudioSink.SINK_FORMAT_UNSUPPORTED
        pendingInput = null
        pendingOutput = AudioProcessor.EMPTY_BUFFER
        converted = highResolution && (!maySwitch || needsProcessing())
        configureDelegate(audioSinkConfig, converted)
    }

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
        this.playbackParameters = playbackParameters
        super.setPlaybackParameters(playbackParameters)
    }

    override fun setSkipSilenceEnabled(skipSilenceEnabled: Boolean) {
        skipSilence = skipSilenceEnabled
        super.setSkipSilenceEnabled(skipSilenceEnabled)
    }

    override fun handleBuffer(buffer: ByteBuffer, presentationTimeUs: Long, encodedAccessUnitCount: Int): Boolean {
        if (pendingInput == null) {
            val config = checkNotNull(originalConfig)
            val nextConverted = highResolution && (!maySwitch || needsProcessing())
            if (nextConverted != converted) {
                try {
                    configureDelegate(config, nextConverted)
                } catch (failure: AudioSink.ConfigurationException) {
                    throw AudioSink.InitializationException(
                        "Unable to switch PCM processing format", AudioTrack.STATE_UNINITIALIZED,
                        config.format, true, failure,
                    )
                }
                converted = nextConverted
            }
            pendingInput = buffer
            if (converted) {
                val bytesPerFrame = Util.getPcmFrameSize(config.format.pcmEncoding, config.format.channelCount)
                check(bytesPerFrame > 0 && buffer.remaining() % bytesPerFrame == 0) {
                    "PCM input is not frame aligned"
                }
                pendingInputStart = buffer.position()
                inputBytesPerSample = bytesPerFrame / config.format.channelCount
                converter.queueInput(buffer.duplicate().order(buffer.order()))
                pendingOutput = converter.output
            }
        } else {
            check(pendingInput === buffer) { "Audio input buffer changed before it was consumed" }
        }
        val output = if (converted) pendingOutput else buffer
        val complete = super.handleBuffer(output, presentationTimeUs, encodedAccessUnitCount)
        if (converted) {
            val consumed = if (complete) buffer.limit() - pendingInputStart else
                (output.position() / 2) * inputBytesPerSample
            buffer.position(pendingInputStart + consumed)
        }
        if (complete) {
            pendingInput = null
            pendingOutput = AudioProcessor.EMPTY_BUFFER
        }
        return complete
    }

    override fun flush() {
        pendingInput = null
        pendingOutput = AudioProcessor.EMPTY_BUFFER
        if (converted) converter.flush(AudioProcessor.StreamMetadata.DEFAULT)
        super.flush()
    }

    override fun reset() {
        pendingInput = null
        pendingOutput = AudioProcessor.EMPTY_BUFFER
        originalConfig = null
        highResolution = false
        maySwitch = false
        converted = false
        converter.reset()
        super.reset()
    }

    private fun needsProcessing(): Boolean =
        playbackParameters != PlaybackParameters.DEFAULT || skipSilence || processingRequired() || !processorsSettled()

    @Suppress("DEPRECATION")
    private fun configureDelegate(config: AudioSink.AudioSinkConfig, convert: Boolean) {
        if (!convert) {
            super.configure(config)
            return
        }
        val format = config.format
        converter.configure(AudioProcessor.AudioFormat(format))
        converter.flush(AudioProcessor.StreamMetadata.DEFAULT)
        val pcm16 = format.buildUpon().setPcmEncoding(C.ENCODING_PCM_16BIT).build()
        val mapped = AudioSink.AudioSinkConfig.Builder(pcm16)
            .setPreferredBufferSizeOverride(config.preferredBufferSizeOverride)
            .setOutputChannelMapping(config.outputChannelMapping)
            .setTimeline(config.timeline)
            .setMediaPeriodId(config.mediaPeriodId)
            .build()
        super.configure(mapped)
    }
}
