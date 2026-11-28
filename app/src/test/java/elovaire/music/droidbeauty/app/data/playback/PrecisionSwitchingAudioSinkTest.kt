package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioSink
import java.io.ByteArrayOutputStream
import java.lang.reflect.Proxy
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

@UnstableApi
class PrecisionSwitchingAudioSinkTest {
    private class Target {
        val configs = mutableListOf<AudioSink.AudioSinkConfig>()
        val bytes = ByteArrayOutputStream()
        var shortWrite = false
        var floatSupported = true
        var pcm16Supported = true
        var failConfiguration = false
        val sink: AudioSink = Proxy.newProxyInstance(AudioSink::class.java.classLoader, arrayOf(AudioSink::class.java)) { _, method, args ->
            when (method.name) {
                "configure" -> {
                    val config = args!![0] as AudioSink.AudioSinkConfig
                    if (failConfiguration) throw AudioSink.ConfigurationException("injected failure", config.format)
                    configs += config
                    null
                }
                "getFormatSupport" -> {
                    val format = args!![0] as Format
                    if ((format.pcmEncoding == C.ENCODING_PCM_16BIT && pcm16Supported) ||
                        (format.pcmEncoding != C.ENCODING_PCM_16BIT && floatSupported)
                    ) {
                        AudioSink.SINK_FORMAT_SUPPORTED_DIRECTLY
                    } else AudioSink.SINK_FORMAT_UNSUPPORTED
                }
                "handleBuffer" -> {
                    val input = args!![0] as ByteBuffer
                    val count = if (shortWrite) input.remaining() / 2 else input.remaining()
                    val data = ByteArray(count)
                    input.get(data)
                    bytes.write(data)
                    !input.hasRemaining()
                }
                "toString" -> "test sink"
                else -> null
            }
        } as AudioSink
    }

    private fun config(delay: Int = 0, padding: Int = 0): AudioSink.AudioSinkConfig {
        val format = Format.Builder().setSampleMimeType(MimeTypes.AUDIO_RAW)
            .setSampleRate(48_000).setChannelCount(2).setPcmEncoding(C.ENCODING_PCM_24BIT)
            .setEncoderDelay(delay).setEncoderPadding(padding).build()
        return AudioSink.AudioSinkConfig.Builder(format).build()
    }

    private fun pcm(): ByteBuffer = ByteBuffer.allocateDirect(6).order(ByteOrder.LITTLE_ENDIAN).apply {
        put(byteArrayOf(1, 2, 3, 4, 5, 6)).flip()
    }

    @Test fun selectionAndInputOwnershipFollowLiveEffectChanges() {
        val target = Target()
        var active = false
        var settled = true
        val sink = PrecisionSwitchingAudioSink(target.sink, { active }, { settled })
        sink.configure(config())
        assertEquals(C.ENCODING_PCM_24BIT, target.configs.last().format.pcmEncoding)
        assertTrue(sink.handleBuffer(pcm(), 0, 1))
        assertArrayEquals(byteArrayOf(1,2,3,4,5,6), target.bytes.toByteArray())
        active = true
        target.shortWrite = true
        val buffer = pcm()
        assertFalse(sink.handleBuffer(buffer, 1000, 1))
        assertEquals(3, buffer.position())
        assertEquals(C.ENCODING_PCM_16BIT, target.configs.last().format.pcmEncoding)
        active = false
        settled = false
        target.shortWrite = false
        assertTrue(sink.handleBuffer(buffer, 1000, 1))
        assertEquals(buffer.limit(), buffer.position())
        assertEquals(2, target.configs.size)
        assertTrue(sink.handleBuffer(pcm(), 2000, 1))
        assertEquals(2, target.configs.size)
        settled = true
        assertTrue(sink.handleBuffer(pcm(), 3000, 1))
        assertEquals(C.ENCODING_PCM_24BIT, target.configs.last().format.pcmEncoding)
    }

    @Test fun encoderTrimKeepsStableProcessedFormat() {
        val target = Target()
        var active = false
        val sink = PrecisionSwitchingAudioSink(target.sink, { active }, { true })
        sink.configure(config(100, 200))
        assertEquals(C.ENCODING_PCM_16BIT, target.configs.last().format.pcmEncoding)
        assertEquals(100, target.configs.last().format.encoderDelay)
        assertEquals(200, target.configs.last().format.encoderPadding)
        active = true
        assertTrue(sink.handleBuffer(pcm(), 0, 1))
        active = false
        assertTrue(sink.handleBuffer(pcm(), 1000, 1))
        assertEquals(1, target.configs.size)
    }

    @Test fun speedRequiresProcessing() {
        val target = Target()
        val sink = PrecisionSwitchingAudioSink(target.sink, { false }, { true })
        sink.configure(config())
        sink.setPlaybackParameters(PlaybackParameters(1.5f))
        assertTrue(sink.handleBuffer(pcm(), 0, 1))
        assertEquals(C.ENCODING_PCM_16BIT, target.configs.last().format.pcmEncoding)
    }

    @Test fun unsupportedFloatUsesPcm16() {
        val target = Target().apply { floatSupported = false }
        val sink = PrecisionSwitchingAudioSink(target.sink, { false }, { true })
        assertEquals(AudioSink.SINK_FORMAT_SUPPORTED_WITH_TRANSCODING, sink.getFormatSupport(config().format))
        assertTrue(sink.supportsFormat(config().format))
        sink.configure(config())
        assertEquals(C.ENCODING_PCM_16BIT, target.configs.last().format.pcmEncoding)
        assertTrue(sink.handleBuffer(pcm(), 0, 1))
        assertArrayEquals(byteArrayOf(2, 3, 5, 6), target.bytes.toByteArray())
    }

    @Test fun activeProcessingReportsFloatAsTranscoded() {
        val target = Target()
        val sink = PrecisionSwitchingAudioSink(target.sink, { true }, { true })
        val format = config().format.buildUpon().setPcmEncoding(C.ENCODING_PCM_FLOAT).build()
        assertEquals(AudioSink.SINK_FORMAT_SUPPORTED_WITH_TRANSCODING, sink.getFormatSupport(format))
    }

    @Test fun unsupportedPcm16IsNotReportedAsAValidFallback() {
        val target = Target().apply { floatSupported = false; pcm16Supported = false }
        val sink = PrecisionSwitchingAudioSink(target.sink, { false }, { true })
        assertEquals(AudioSink.SINK_FORMAT_UNSUPPORTED, sink.getFormatSupport(config().format))
        assertFalse(sink.supportsFormat(config().format))
    }

    @Test fun configurationFailureDuringASwitchIsReportedToThePlayer() {
        val target = Target()
        var active = false
        val sink = PrecisionSwitchingAudioSink(target.sink, { active }, { true })
        sink.configure(config())
        active = true
        target.failConfiguration = true
        val failure = assertThrows(AudioSink.InitializationException::class.java) {
            sink.handleBuffer(pcm(), 0, 1)
        }
        assertTrue(failure.isRecoverable)
        assertTrue(failure.cause is AudioSink.ConfigurationException)
        assertEquals(C.ENCODING_PCM_24BIT, failure.format.pcmEncoding)
    }
}
