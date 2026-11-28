package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioOutput
import androidx.media3.exoplayer.audio.AudioOutputProvider
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.AudioTrackAudioOutputProvider
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.ForwardingAudioOutput
import androidx.media3.exoplayer.audio.ForwardingAudioOutputProvider
import androidx.media3.exoplayer.audio.ForwardingAudioSink
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@UnstableApi
class PrecisionSwitchingSinkInstrumentedTest {
    @Test fun nonUnitySpeedChangesTheNumberOfSubmittedFrames() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val frames = 96_000
        val wav = ByteBuffer.allocate(44 + frames * 6).order(ByteOrder.LITTLE_ENDIAN)
        wav.put("RIFF".toByteArray(Charsets.US_ASCII)).putInt(36 + frames * 6)
        wav.put("WAVEfmt ".toByteArray(Charsets.US_ASCII)).putInt(16)
        wav.putShort(1).putShort(2).putInt(48_000).putInt(48_000 * 6).putShort(6).putShort(24)
        wav.put("data".toByteArray(Charsets.US_ASCII)).putInt(frames * 6)
        repeat(frames * 2) { index ->
            val value = if ((index / 2) % 100 < 50) 1_000_000 else -1_000_000
            wav.put(value.toByte()).put((value shr 8).toByte()).put((value shr 16).toByte())
        }
        val file = java.io.File.createTempFile("precision-speed-", ".wav", context.cacheDir)
        file.writeBytes(wav.array())
        val writtenFrames = AtomicInteger()
        val encoding = AtomicInteger()
        val ended = CountDownLatch(1)
        val failure = AtomicReference<PlaybackException?>()
        var player: ExoPlayer? = null
        try {
            instrumentation.runOnMainSync {
                val provider = object : ForwardingAudioOutputProvider(AudioTrackAudioOutputProvider.Builder(context).build()) {
                    override fun getAudioOutput(config: AudioOutputProvider.OutputConfig): AudioOutput {
                        encoding.set(config.encoding)
                        return object : ForwardingAudioOutput(super.getAudioOutput(config)) {
                            override fun write(buffer: ByteBuffer, encodedAccessUnitCount: Int, presentationTimeUs: Long): Boolean {
                                val before = buffer.position()
                                val result = super.write(buffer, encodedAccessUnitCount, presentationTimeUs)
                                writtenFrames.addAndGet((buffer.position() - before) / 4)
                                return result
                            }
                        }
                    }
                }
                val renderers = object : DefaultRenderersFactory(context) {
                    override fun buildAudioSink(context: Context, enableFloatOutput: Boolean, enableAudioOutputPlaybackParameters: Boolean): AudioSink {
                        val stock = DefaultAudioSink.Builder(context).setAudioOutputProvider(provider)
                            .setEnableFloatOutput(enableFloatOutput).build()
                        return PrecisionSwitchingAudioSink(stock, { false }, { true })
                    }
                }.setEnableAudioFloatOutput(true)
                player = ExoPlayer.Builder(context).setRenderersFactory(renderers).build().apply {
                    volume = 0f
                    playbackParameters = PlaybackParameters(1.5f)
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) { if (playbackState == Player.STATE_ENDED) ended.countDown() }
                        override fun onPlayerError(error: PlaybackException) { failure.set(error); ended.countDown() }
                    })
                    setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                    prepare()
                    play()
                }
            }
            assertTrue("Speed fixture did not finish", ended.await(10, TimeUnit.SECONDS))
            assertNull(failure.get())
            assertEquals(C.ENCODING_PCM_16BIT, encoding.get())
            Log.i("PrecisionSinkTest", "speed=1.5 inputFrames=$frames outputFrames=${writtenFrames.get()}")
            assertTrue("Speed was not applied: ${writtenFrames.get()} output frames", writtenFrames.get() in 62_000..66_000)
        } finally {
            instrumentation.runOnMainSync { player?.release() }
            file.delete()
        }
    }

    @Test fun switchesWithoutLosingFramesAndExecutesRequestedProcessor() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val frames = 96_000
        val wav = ByteBuffer.allocate(44 + frames * 6).order(ByteOrder.LITTLE_ENDIAN)
        wav.put("RIFF".toByteArray(Charsets.US_ASCII)).putInt(36 + frames * 6)
        wav.put("WAVEfmt ".toByteArray(Charsets.US_ASCII)).putInt(16)
        wav.putShort(1).putShort(2).putInt(48_000).putInt(48_000 * 6).putShort(6).putShort(24)
        wav.put("data".toByteArray(Charsets.US_ASCII)).putInt(frames * 6)
        repeat(frames * 2) { wav.put(0x40).put(0x42).put(0x0f) }
        val file = java.io.File.createTempFile("precision-switch-", ".wav", context.cacheDir)
        file.writeBytes(wav.array())
        val completed = AtomicInteger()
        val processed = AtomicInteger()
        val configs = Collections.synchronizedList(mutableListOf<Int>())
        val payloads = Collections.synchronizedList(mutableListOf<ByteArrayOutputStream>())
        val writtenFrames = AtomicInteger()
        val end = CountDownLatch(1)
        val error = AtomicReference<PlaybackException?>()
        var player: ExoPlayer? = null
        try {
            instrumentation.runOnMainSync {
                val processor = object : BaseAudioProcessor() {
                    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat = inputAudioFormat
                    override fun queueInput(inputBuffer: ByteBuffer) {
                        processed.addAndGet(inputBuffer.remaining() / 4)
                        val output = replaceOutputBuffer(inputBuffer.remaining())
                        while (inputBuffer.hasRemaining()) { inputBuffer.get(); output.put(0) }
                        output.flip()
                    }
                }
                val provider = object : ForwardingAudioOutputProvider(AudioTrackAudioOutputProvider.Builder(context).build()) {
                    override fun getAudioOutput(config: AudioOutputProvider.OutputConfig): AudioOutput {
                        configs.add(config.encoding)
                        val payload = ByteArrayOutputStream()
                        payloads.add(payload)
                        return object : ForwardingAudioOutput(super.getAudioOutput(config)) {
                            override fun write(buffer: ByteBuffer, encodedAccessUnitCount: Int, presentationTimeUs: Long): Boolean {
                                val before = buffer.position()
                                val snapshot = ByteArray(buffer.remaining())
                                buffer.duplicate().get(snapshot)
                                val result = super.write(buffer, encodedAccessUnitCount, presentationTimeUs)
                                val consumed = buffer.position() - before
                                payload.write(snapshot, 0, consumed)
                                writtenFrames.addAndGet(consumed / if (config.encoding == C.ENCODING_PCM_FLOAT) 8 else 4)
                                return result
                            }
                        }
                    }
                }
                val renderers = object : DefaultRenderersFactory(context) {
                    override fun buildAudioSink(context: Context, enableFloatOutput: Boolean, enableAudioOutputPlaybackParameters: Boolean): AudioSink {
                        val stock = DefaultAudioSink.Builder(context).setAudioOutputProvider(provider)
                            .setAudioProcessors(arrayOf(processor)).setEnableFloatOutput(enableFloatOutput).build()
                        val switching = PrecisionSwitchingAudioSink(stock, { completed.get() in 4..8 }, { true })
                        return object : ForwardingAudioSink(switching) {
                            override fun handleBuffer(buffer: ByteBuffer, presentationTimeUs: Long, encodedAccessUnitCount: Int): Boolean {
                                val result = super.handleBuffer(buffer, presentationTimeUs, encodedAccessUnitCount)
                                if (result) completed.incrementAndGet()
                                return result
                            }
                        }
                    }
                }.setEnableAudioFloatOutput(true)
                player = ExoPlayer.Builder(context).setRenderersFactory(renderers).build().apply {
                    volume = 0f
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) { if (playbackState == Player.STATE_ENDED) end.countDown() }
                        override fun onPlayerError(failure: PlaybackException) { error.set(failure); end.countDown() }
                    })
                    setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                    prepare()
                    play()
                }
            }
            assertTrue("playback did not finish", end.await(20, TimeUnit.SECONDS))
            assertNull(error.get())
            assertTrue("processor did not run", processed.get() > 0)
            assertEquals(frames, writtenFrames.get())
            Log.i("PrecisionSinkTest", "inputFrames=$frames outputFrames=${writtenFrames.get()} processedFrames=${processed.get()} encodings=$configs")
            assertTrue("float output missing: $configs", C.ENCODING_PCM_FLOAT in configs)
            assertTrue("PCM16 output missing: $configs", C.ENCODING_PCM_16BIT in configs)
            assertEquals(listOf(C.ENCODING_PCM_FLOAT, C.ENCODING_PCM_16BIT, C.ENCODING_PCM_FLOAT), configs)
            payloads.forEachIndexed { index, payload ->
                val bytes = payload.toByteArray()
                if (configs[index] == C.ENCODING_PCM_16BIT) {
                    assertTrue(bytes.isNotEmpty())
                    assertTrue(bytes.all { it == 0.toByte() })
                } else {
                    val samples = ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder())
                    assertTrue(samples.remaining() > 960 * 8)
                    samples.position(960 * 8)
                    while (samples.hasRemaining()) assertEquals(1_000_000f / 8_388_608f, samples.float, 0f)
                }
            }
        } finally {
            instrumentation.runOnMainSync { player?.release() }
            file.delete()
        }
    }
}
