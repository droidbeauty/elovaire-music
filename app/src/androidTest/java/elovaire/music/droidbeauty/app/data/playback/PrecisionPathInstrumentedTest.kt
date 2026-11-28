package elovaire.music.droidbeauty.app.data.playback

import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@UnstableApi
class PrecisionPathInstrumentedTest {
    @Test
    fun liveEffectChangesSwitchPrecisionWithoutReplacingThePlayer() {
        verifyLiveTransitions(changeSpeed = false)
    }

    @Test
    fun playbackSpeedChangesSwitchToTheProcessingChain() {
        verifyLiveTransitions(changeSpeed = true)
    }

    private fun verifyLiveTransitions(changeSpeed: Boolean) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val frames = 48_000 * 4
        val wav = ByteBuffer.allocate(44 + frames * 6).order(ByteOrder.LITTLE_ENDIAN)
        wav.put("RIFF".toByteArray(Charsets.US_ASCII)).putInt(36 + frames * 6)
        wav.put("WAVEfmt ".toByteArray(Charsets.US_ASCII)).putInt(16)
        wav.putShort(1).putShort(2).putInt(48_000).putInt(48_000 * 6).putShort(6).putShort(24)
        wav.put("data".toByteArray(Charsets.US_ASCII)).putInt(frames * 6)
        repeat(frames * 2) { wav.put(0x40).put(0x42).put(0x0f) }
        val file = File.createTempFile("precision-path-", ".wav", context.cacheDir)
        file.writeBytes(wav.array())
        val effects = PlaybackEffectsController()
        val initialized = Collections.synchronizedList(mutableListOf<Int>())
        val positions = Collections.synchronizedList(mutableListOf<Long>())
        val finished = CountDownLatch(1)
        val failure = AtomicReference<PlaybackException?>()
        var player: ExoPlayer? = null
        try {
            instrumentation.runOnMainSync {
                player = PlaybackPlayerFactory(
                    context = context,
                    dataSourceFactory = DefaultDataSource.Factory(context),
                    extractorsFactory = DefaultExtractorsFactory(),
                    playbackAudioAttributes = AudioAttributes.DEFAULT,
                    audioProcessorsProvider = effects::audioProcessors,
                    preferredOutputDevice = { null },
                    processingRequired = effects::hasSignalAlteringEffects,
                ).create(enableSignalProcessing = true).apply {
                    volume = 0f
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_ENDED) finished.countDown()
                        }
                        override fun onPlayerError(error: PlaybackException) {
                            failure.set(error)
                            finished.countDown()
                        }
                    })
                    addAnalyticsListener(object : AnalyticsListener {
                        override fun onAudioTrackInitialized(
                            eventTime: AnalyticsListener.EventTime,
                            audioTrackConfig: AudioSink.AudioTrackConfig,
                        ) {
                            initialized.add(audioTrackConfig.encoding)
                            positions.add(player?.currentPosition ?: 0L)
                            when (initialized.toList()) {
                                listOf(C.ENCODING_PCM_FLOAT) -> {
                                    if (changeSpeed) player?.playbackParameters = PlaybackParameters(1.5f)
                                    else effects.applyEffectSettings(EqSettings(preampDb = -6f))
                                }
                                listOf(C.ENCODING_PCM_FLOAT, C.ENCODING_PCM_16BIT) -> {
                                    if (changeSpeed) player?.playbackParameters = PlaybackParameters.DEFAULT
                                    else effects.applyEffectSettings(EqSettings())
                                }
                                listOf(C.ENCODING_PCM_FLOAT, C.ENCODING_PCM_16BIT, C.ENCODING_PCM_FLOAT) -> finished.countDown()
                            }
                        }
                    })
                    setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                    prepare()
                    play()
                }
            }
            assertTrue("Precision transitions did not complete", finished.await(12, TimeUnit.SECONDS))
            assertNull(failure.get())
            assertEquals(listOf(C.ENCODING_PCM_FLOAT, C.ENCODING_PCM_16BIT, C.ENCODING_PCM_FLOAT), initialized.toList())
            assertTrue("Playback position moved backwards: $positions", positions.zipWithNext().all { (a, b) -> b >= a })
        } finally {
            instrumentation.runOnMainSync { player?.release() }
            file.delete()
        }
    }
}
