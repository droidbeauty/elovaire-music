package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@UnstableApi
class FloatSinkBoundaryInstrumentedTest {
    @Test
    fun pcm24LowBitsReachAudioTrackAfterMedia3StartupRamp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val samples = IntArray(2_400 * 2) { (it % 255) - 127 }
        val wav = ByteBuffer.allocate(44 + samples.size * 3).order(ByteOrder.LITTLE_ENDIAN)
        wav.put("RIFF".toByteArray(Charsets.US_ASCII)).putInt(36 + samples.size * 3)
        wav.put("WAVEfmt ".toByteArray(Charsets.US_ASCII)).putInt(16)
        wav.putShort(1).putShort(2).putInt(48_000).putInt(48_000 * 6).putShort(6).putShort(24)
        wav.put("data".toByteArray(Charsets.US_ASCII)).putInt(samples.size * 3)
        samples.forEach { wav.put(it.toByte()).put((it shr 8).toByte()).put((it shr 16).toByte()) }
        val file = java.io.File.createTempFile("float-boundary-", ".wav", context.cacheDir)
        file.writeBytes(wav.array())
        val capture = ByteArrayOutputStream()
        val outputConfig = AtomicReference<AudioOutputProvider.OutputConfig?>()
        val sinkInputFormat = AtomicReference<androidx.media3.common.Format?>()
        val sinkInput = ByteArrayOutputStream()
        val ended = CountDownLatch(1)
        val failure = AtomicReference<PlaybackException?>()
        var player: ExoPlayer? = null
        try {
            instrumentation.runOnMainSync {
                val provider = object : ForwardingAudioOutputProvider(
                    AudioTrackAudioOutputProvider.Builder(context).build(),
                ) {
                    override fun getAudioOutput(config: AudioOutputProvider.OutputConfig): AudioOutput {
                        outputConfig.set(config)
                        return object : ForwardingAudioOutput(super.getAudioOutput(config)) {
                            override fun write(buffer: ByteBuffer, encodedAccessUnitCount: Int, presentationTimeUs: Long): Boolean {
                                val before = buffer.position()
                                val copy = buffer.duplicate()
                                val submitted = ByteArray(copy.remaining()).also(copy::get)
                                val result = super.write(buffer, encodedAccessUnitCount, presentationTimeUs)
                                val consumed = buffer.position() - before
                                capture.write(submitted, 0, consumed)
                                return result
                            }
                        }
                    }
                }
                val renderers = object : DefaultRenderersFactory(context) {
                    override fun buildAudioSink(
                        context: Context,
                        enableFloatOutput: Boolean,
                        enableAudioOutputPlaybackParameters: Boolean,
                    ): AudioSink = object : ForwardingAudioSink(
                        DefaultAudioSink.Builder(context)
                            .setAudioOutputProvider(provider)
                            .setEnableFloatOutput(enableFloatOutput)
                            .build(),
                    ) {
                        override fun configure(audioSinkConfig: AudioSink.AudioSinkConfig) {
                            sinkInputFormat.set(audioSinkConfig.format)
                            super.configure(audioSinkConfig)
                        }
                        override fun handleBuffer(buffer: ByteBuffer, presentationTimeUs: Long, encodedAccessUnitCount: Int): Boolean {
                            val before = buffer.position()
                            val copy = buffer.duplicate()
                            val submitted = ByteArray(copy.remaining()).also(copy::get)
                            val result = super.handleBuffer(buffer, presentationTimeUs, encodedAccessUnitCount)
                            sinkInput.write(submitted, 0, buffer.position() - before)
                            return result
                        }
                    }
                }.setEnableAudioFloatOutput(true)
                player = ExoPlayer.Builder(context).setRenderersFactory(renderers).build().apply {
                    volume = 0f
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_ENDED) ended.countDown()
                        }
                        override fun onPlayerError(error: PlaybackException) {
                            failure.set(error)
                            ended.countDown()
                        }
                    })
                    setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                    prepare()
                    play()
                }
            }
            assertTrue("Fixture did not finish", ended.await(10, TimeUnit.SECONDS))
            assertNull(failure.get())
            val config = requireNotNull(outputConfig.get())
            assertEquals(48_000, config.sampleRate)
            assertEquals(C.ENCODING_PCM_FLOAT, config.encoding)
            assertEquals(C.ENCODING_PCM_24BIT, requireNotNull(sinkInputFormat.get()).pcmEncoding)
            assertArrayEquals(wav.array().copyOfRange(44, wav.capacity()), sinkInput.toByteArray())
            val output = ByteBuffer.wrap(capture.toByteArray()).order(ByteOrder.nativeOrder())
            assertEquals(samples.size * 4, output.remaining())
            // Media3 1.11.1 ramps the first 20 ms inside DefaultAudioSink before AudioOutput.write.
            assertEquals(0f, output.float, 0f)
            output.position(960 * 2 * 4)
            for (index in 960 * 2 until samples.size) {
                assertEquals(samples[index] / 8_388_608f, output.float, 0f)
            }
        } finally {
            instrumentation.runOnMainSync { player?.release() }
            file.delete()
        }
    }
}
