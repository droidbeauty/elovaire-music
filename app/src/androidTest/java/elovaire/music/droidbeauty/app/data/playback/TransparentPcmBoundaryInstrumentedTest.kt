package elovaire.music.droidbeauty.app.data.playback

import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import java.io.ByteArrayOutputStream
import java.io.File
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
class TransparentPcmBoundaryInstrumentedTest {
    @Test
    fun neutralAppProcessorPreservesDecodedPcm16AtTheSinkChainBoundary() {
        val pcm = ByteArray(4 * 2_400) { index -> ((index * 97 + 31) and 0xff).toByte() }
        val (format, output) = playAndCapture(pcm, EqSettings())
        assertEquals(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT), format)
        assertArrayEquals(pcm, output)
    }

    @Test
    fun productionSinkTruncatesPcm24BeforeTheNeutralAppProcessor() {
        val pcm = ByteArray(2_400 * 2 * 3)
        val expected = ByteBuffer.allocate(2_400 * 2 * 2).order(ByteOrder.LITTLE_ENDIAN)
        repeat(2_400 * 2) { index ->
            val sample = (index % 255) - 127
            pcm[index * 3] = sample.toByte()
            pcm[index * 3 + 1] = (sample shr 8).toByte()
            pcm[index * 3 + 2] = (sample shr 16).toByte()
            expected.putShort((sample shr 8).toShort())
        }
        val (format, output) = playAndCapture(pcm, EqSettings(), bitsPerSample = 24)
        assertEquals(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT), format)
        assertArrayEquals(expected.array(), output)
    }

    @Test
    fun preampGainIsAppliedInTheMedia3ProcessorChain() {
        val pcm = ByteBuffer.allocate(48_000 * 4).order(ByteOrder.LITTLE_ENDIAN)
        repeat(48_000 * 2) { pcm.putShort(2_000) }
        val (format, output) = playAndCapture(pcm.array(), EqSettings(preampDb = -6f))
        assertEquals(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT), format)
        assertEquals(pcm.capacity(), output.size)
        val lastFrame = ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN).apply {
            position(output.size - 4)
        }
        val expected = 2_000 * Math.pow(10.0, -6.0 / 20.0)
        val left = lastFrame.short.toInt()
        val right = lastFrame.short.toInt()
        Log.i("PcmBoundaryQualification", "preamp=-6dB input=2000 left=$left right=$right")
        assertEquals(expected, left.toDouble(), 3.0)
        assertEquals(expected, right.toDouble(), 3.0)
    }

    @Test
    fun playerCreationFallbackKeepsTheRequestedProcessorChain() {
        val pcm = ByteBuffer.allocate(48_000 * 4).order(ByteOrder.LITTLE_ENDIAN)
        repeat(48_000 * 2) { pcm.putShort(2_000) }
        val (format, output) = playAndCapture(pcm.array(), EqSettings(preampDb = -6f), failFirstProcessorCreation = true)
        assertEquals(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT), format)
        assertEquals(pcm.capacity(), output.size)
        val lastFrame = ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN).apply { position(output.size - 4) }
        val expected = 2_000 * Math.pow(10.0, -6.0 / 20.0)
        assertEquals(expected, lastFrame.short.toDouble(), 3.0)
        assertEquals(expected, lastFrame.short.toDouble(), 3.0)
    }

    @Test
    fun playerCreationFailureDoesNotSilentlyDiscardRequestedProcessing() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        var failure: Exception? = null
        instrumentation.runOnMainSync {
            try {
                PlaybackPlayerFactory(
                    context = context,
                    dataSourceFactory = DefaultDataSource.Factory(context),
                    extractorsFactory = DefaultExtractorsFactory(),
                    playbackAudioAttributes = AudioAttributes.DEFAULT,
                    audioProcessorsProvider = { error("Injected persistent processor failure") },
                    preferredOutputDevice = { null },
                ).create(enableSignalProcessing = true).release()
            } catch (caught: Exception) {
                failure = caught
            }
        }
        val actual = requireNotNull(failure)
        assertEquals("Injected persistent processor failure", actual.message)
        assertEquals(1, actual.suppressed.size)
        assertEquals("Injected persistent processor failure", actual.suppressed.single().message)
    }

    private fun playAndCapture(
        pcm: ByteArray,
        settings: EqSettings,
        failFirstProcessorCreation: Boolean = false,
        bitsPerSample: Int = 16,
    ): Pair<AudioProcessor.AudioFormat?, ByteArray> {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val wav = ByteBuffer.allocate(44 + pcm.size).order(ByteOrder.LITTLE_ENDIAN)
        wav.put("RIFF".toByteArray(Charsets.US_ASCII)).putInt(36 + pcm.size)
        wav.put("WAVEfmt ".toByteArray(Charsets.US_ASCII)).putInt(16)
        val frameSize = 2 * (bitsPerSample / 8)
        wav.putShort(1).putShort(2).putInt(48_000).putInt(48_000 * frameSize)
            .putShort(frameSize.toShort()).putShort(bitsPerSample.toShort())
        wav.put("data".toByteArray(Charsets.US_ASCII)).putInt(pcm.size).put(pcm)
        val file = File.createTempFile("pcm-boundary-", ".wav", context.cacheDir)
        file.writeBytes(wav.array())
        val capture = BoundaryCapture()
        val ended = CountDownLatch(1)
        val failure = AtomicReference<PlaybackException?>()
        var processorCreations = 0
        var player: ExoPlayer? = null
        try {
            instrumentation.runOnMainSync {
                player = PlaybackPlayerFactory(
                    context = context,
                    dataSourceFactory = DefaultDataSource.Factory(context),
                    extractorsFactory = DefaultExtractorsFactory(),
                    playbackAudioAttributes = AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
                    audioProcessorsProvider = {
                        if (failFirstProcessorCreation && processorCreations++ == 0) {
                            error("Injected processor factory failure")
                        }
                        arrayOf(EqualizerAudioProcessor().apply { updateSettings(settings) }, capture)
                    },
                    preferredOutputDevice = { null },
                ).create(enableSignalProcessing = true).apply {
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
            assertTrue("Generated fixture did not finish", ended.await(10, TimeUnit.SECONDS))
            assertNull(failure.get())
            return capture.configuredFormat to capture.bytes.toByteArray()
        } finally {
            instrumentation.runOnMainSync { player?.release() }
            file.delete()
        }
    }

    private class BoundaryCapture : BaseAudioProcessor() {
        val bytes = ByteArrayOutputStream()
        var configuredFormat: AudioProcessor.AudioFormat? = null

        override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
            configuredFormat = inputAudioFormat
            return inputAudioFormat
        }

        override fun queueInput(inputBuffer: ByteBuffer) {
            val data = ByteArray(inputBuffer.remaining())
            inputBuffer.get(data)
            bytes.write(data)
            replaceOutputBuffer(data.size).put(data).flip()
        }
    }
}
