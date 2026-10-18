package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioManager
import android.net.Uri
import android.os.SystemClock
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.UpscalerSettings
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@UnstableApi
class UpscalerPlaybackInstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun allFactorsReachAudioTrackAndPreserveDecodedChannels() {
        val fixtures = listOf("playback-fixture.wav", "aac-6ch.m4a", "aac-7ch.m4a", "aac-8ch.m4a")
        for (fixture in fixtures) {
            val file = copyFixture(fixture)
            try {
                for (factor in listOf(2, 4, 8)) assertOutput(file, factor)
            } finally {
                assertTrue(file.delete())
            }
        }
    }

    private fun assertOutput(file: File, factor: Int) {
        val settings = UpscalerSettings(true, factor, highPrecision = true)
        val channels = when {
            file.name.contains("6ch") -> 6
            file.name.contains("7ch") -> 7
            file.name.contains("8ch") -> 8
            else -> 1
        }
        val sourceRate = if (file.extension == "wav") 44_100 else 48_000
        val expected = UpscalerAudioProcessor(settings).configure(
            AudioProcessor.AudioFormat(sourceRate, channels, C.ENCODING_PCM_16BIT),
        )
        val outputRate = if (expected == AudioProcessor.AudioFormat.NOT_SET) sourceRate else expected.sampleRate
        val ended = CountDownLatch(1)
        val failure = AtomicReference<PlaybackException?>()
        val trackRate = AtomicInteger()
        val trackChannels = AtomicInteger()
        val underruns = AtomicInteger()
        var player: ExoPlayer? = null
        instrumentation.runOnMainSync {
            player = PlaybackPlayerFactory(
                context, DefaultDataSource.Factory(context), DefaultExtractorsFactory(),
                AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
                audioProcessorsProvider = { PlaybackEffectsController().apply { applyUpscalerSettings(settings) }.audioProcessors() },
                preferredOutputDevice = { null },
            ).create(true).apply {
                volume = 0f
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) { failure.set(error); ended.countDown() }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) ended.countDown()
                    }
                })
                addAnalyticsListener(object : AnalyticsListener {
                    override fun onAudioTrackInitialized(eventTime: AnalyticsListener.EventTime, audioTrackConfig: AudioSink.AudioTrackConfig) {
                        trackRate.set(audioTrackConfig.sampleRate)
                        trackChannels.set(Integer.bitCount(audioTrackConfig.channelConfig))
                    }
                    override fun onAudioUnderrun(eventTime: AnalyticsListener.EventTime, bufferSize: Int, bufferSizeMs: Long, elapsedSinceLastFeedMs: Long) {
                        underruns.incrementAndGet()
                    }
                })
                setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                prepare()
                play()
            }
        }
        try {
            assertTrue("${file.name} ${factor}x did not finish", ended.await(15, TimeUnit.SECONDS))
            assertNull("${file.name} ${factor}x playback failed", failure.get())
            assertEquals("AudioTrack rate for ${file.name} ${factor}x", outputRate, trackRate.get())
            assertEquals(channels, trackChannels.get())
            assertEquals("Audio upscaling must keep up with real-time playback", 0, underruns.get())
        } finally {
            instrumentation.runOnMainSync { player?.release() }
        }
    }

    @Test
    fun changingSettingsRebuildsPlayerAndPreservesQueuePositionAndPauseState() {
        val file = copyFixture("playback-fixture.wav")
        val effects = PlaybackEffectsController()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val audioManager = context.getSystemService(AudioManager::class.java)
        val oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        var manager: PlaybackManager? = null
        try {
            instrumentation.runOnMainSync {
                manager = PlaybackManager(context, scope, audioProcessorsProvider = effects::audioProcessors,
                    hasSignalAlteringEffects = effects::hasSignalAlteringEffects,
                    upscalerSettingsProvider = { effects.upscalerSettings }).apply {
                    setVolume(0f)
                    val song = Song(
                        id = 986001, title = "Upscaler fixture", artist = "Elovaire Test", album = "Upscaler Test",
                        isExplicit = false, releaseYear = null, genre = "", audioFormat = "WAV", audioQuality = null,
                        fileName = file.name, albumId = 986002, durationMs = 3000, trackNumber = 1, discNumber = 1,
                        dateAddedSeconds = 0, uri = Uri.fromFile(file), artUri = null,
                    )
                    playAlbum(Album(986002, "Upscaler Test", "Elovaire Test", null, 1, 3000, listOf(song)))
                }
            }
            val playback = checkNotNull(manager)
            runBlocking { withTimeout(10_000) { playback.state.first { it.isPlaying } } }
            instrumentation.runOnMainSync { playback.togglePlayback() }
            runBlocking { withTimeout(5_000) { playback.state.first { !it.isPlaying } } }
            instrumentation.runOnMainSync { playback.seekTo(800) }
            val originalQueue = playback.state.value.queue.map { it.id }
            for (settings in listOf(UpscalerSettings(true, 2), UpscalerSettings(true, 8, true, 6), UpscalerSettings())) {
                val version = playback.playerInstanceVersion.value
                instrumentation.runOnMainSync { effects.applyUpscalerSettings(settings); playback.reevaluateAudioOutputPath() }
                runBlocking { withTimeout(10_000) { playback.playerInstanceVersion.first { it > version } } }
                instrumentation.runOnMainSync {
                    assertEquals(originalQueue, playback.state.value.queue.map { it.id })
                    assertFalse(playback.playerInstance.playWhenReady)
                    assertEquals(800L, playback.playerInstance.currentPosition)
                    assertEquals(0, playback.playerInstance.currentMediaItemIndex)
                }
            }
        } finally {
            instrumentation.runOnMainSync {
                manager?.release()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolume, 0)
            }
            scope.cancel()
            assertTrue(file.delete())
        }
    }

    @Test
    fun eightChannelHighPrecisionProcessingRunsFasterThanRealtime() {
        val processor = UpscalerAudioProcessor(UpscalerSettings(true, 8, true)) { _, _ -> true }
        processor.configure(AudioProcessor.AudioFormat(48_000, 8, C.ENCODING_PCM_16BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        val buffer = ByteBuffer.allocateDirect(256 * 8 * 2).order(ByteOrder.nativeOrder())
        repeat(256 * 8) { buffer.putShort(1200) }
        val started = SystemClock.elapsedRealtime()
        repeat(188) { buffer.flip(); processor.queueInput(buffer); processor.getOutput() }
        processor.queueEndOfStream()
        processor.getOutput()
        val elapsed = SystemClock.elapsedRealtime() - started
        assertTrue("One second of worst-case processing took ${elapsed}ms", elapsed < 1000)
        processor.reset()
    }

    private fun copyFixture(name: String): File = File.createTempFile("upscaler-", "-$name", context.cacheDir).also { file ->
        instrumentation.context.assets.open("media-metadata/$name").use { input -> file.outputStream().use(input::copyTo) }
    }
}
