package elovaire.music.droidbeauty.app.data.playback

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.math.PI
import kotlin.math.sin
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@UnstableApi
class GeneratedAudioConformanceInstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val resolver = context.contentResolver
    private val insertedUris = mutableListOf<Uri>()

    @After
    fun tearDown() {
        insertedUris.forEach { resolver.delete(it, null, null) }
    }

    @Test
    fun deterministicGeneratedPcmCorpusReachesAudioOutput() {
        CONFORMANCE_FIXTURES.forEach { fixture ->
            val uri = insertFixture(fixture)
            assertAudioOutputStarts(fixture.name, uri)
        }
    }

    @Test
    fun generatedStereoFixtureReachesTheAppPlaybackPipeline() {
        val fixture = ConformanceFixture(
            name = "pcm_app_pipeline_stereo_48khz",
            frequencyHz = 440.0,
            leadingSilenceMs = 0,
            trailingSilenceMs = 0,
            sampleRate = 48_000,
            channelCount = 2,
            durationMs = 3_000,
        )
        val uri = insertFixture(fixture)
        val song = fixtureSong(fixture, uri, id = 9_001L)
        val album = Album(
            id = 9_002L,
            title = "Generated App Pipeline",
            artist = "Elovaire Test",
            artUri = null,
            songCount = 1,
            durationMs = fixture.durationMs.toLong(),
            songs = listOf(song),
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        var playbackManager: PlaybackManager? = null
        try {
            instrumentation.runOnMainSync {
                val manager = PlaybackManager(context, scope)
                playbackManager = manager
                manager.playAlbum(album)
            }
            val deadline = System.currentTimeMillis() + 10_000L
            var advanced = false
            while (System.currentTimeMillis() < deadline) {
                val manager = playbackManager ?: break
                if (
                    manager.state.value.currentSong?.id == song.id &&
                    manager.state.value.isPlaying
                ) {
                    advanced = true
                    break
                }
                Thread.sleep(50L)
            }
            assertTrue("app playback pipeline did not advance the generated fixture", advanced)
            assertEquals(song.id, playbackManager?.state?.value?.currentSong?.id)
            assertEquals(1, playbackManager?.state?.value?.queue?.size)
        } finally {
            instrumentation.runOnMainSync { playbackManager?.release() }
            scope.cancel()
        }
    }

    private fun assertAudioOutputStarts(name: String, uri: Uri) {
        val outputStarted = CountDownLatch(1)
        val failure = AtomicReference<PlaybackException?>()
        var player: ExoPlayer? = null
        instrumentation.runOnMainSync {
            player = PlaybackPlayerFactory(
                context = context,
                dataSourceFactory = DefaultDataSource.Factory(context),
                extractorsFactory = DefaultExtractorsFactory(),
                playbackAudioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                audioProcessorsProvider = { emptyArray() },
                preferredOutputDevice = { null },
            ).create(enableSignalProcessing = false).apply {
                volume = 0f
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        failure.set(error)
                        outputStarted.countDown()
                    }
                })
                addAnalyticsListener(object : AnalyticsListener {
                    override fun onAudioPositionAdvancing(
                        eventTime: AnalyticsListener.EventTime,
                        playoutStartSystemTimeMs: Long,
                    ) {
                        outputStarted.countDown()
                    }
                })
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                play()
            }
        }
        try {
            assertTrue("$name did not reach audio output", outputStarted.await(10, TimeUnit.SECONDS))
            assertNull("$name failed to decode", failure.get())
        } finally {
            instrumentation.runOnMainSync { player?.release() }
        }
    }

    private fun insertFixture(fixture: ConformanceFixture): Uri {
        val uri = resolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "${fixture.name}.wav")
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                if (Build.VERSION.SDK_INT >= 29) {
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/ElovaireConformance")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
            },
        ) ?: error("Unable to insert conformance fixture")
        insertedUris += uri
        resolver.openOutputStream(uri)?.use { it.write(fixture.wavBytes()) }
            ?: error("Unable to write conformance fixture")
        if (Build.VERSION.SDK_INT >= 29) {
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) },
                null,
                null,
            )
        }
        return uri
    }

    private fun fixtureSong(fixture: ConformanceFixture, uri: Uri, id: Long) = Song(
        id = id,
        title = fixture.name,
        isExplicit = false,
        artist = "Elovaire Test",
        album = "Generated App Pipeline",
        releaseYear = null,
        genre = "",
        audioFormat = "WAV",
        audioQuality = null,
        fileName = "${fixture.name}.wav",
        albumId = 9_002L,
        durationMs = fixture.durationMs.toLong(),
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 0L,
        uri = uri,
        artUri = null,
    )

    private data class ConformanceFixture(
        val name: String,
        val frequencyHz: Double,
        val leadingSilenceMs: Int,
        val trailingSilenceMs: Int,
        val sampleRate: Int = 44_100,
        val channelCount: Int = 1,
        val durationMs: Int = 1_500,
    ) {
        fun wavBytes(): ByteArray {
            val sampleCount = sampleRate * durationMs / 1_000
            val pcmBytes = sampleCount * channelCount * 2
            val output = ByteBuffer.allocate(44 + pcmBytes).order(ByteOrder.LITTLE_ENDIAN)
            output.put("RIFF".toByteArray())
            output.putInt(36 + pcmBytes)
            output.put("WAVEfmt ".toByteArray())
            output.putInt(16)
            output.putShort(1)
            output.putShort(channelCount.toShort())
            output.putInt(sampleRate)
            output.putInt(sampleRate * channelCount * 2)
            output.putShort((channelCount * 2).toShort())
            output.putShort(16)
            output.put("data".toByteArray())
            output.putInt(pcmBytes)
            val leading = sampleRate * leadingSilenceMs / 1_000
            val trailing = sampleRate * trailingSilenceMs / 1_000
            repeat(sampleCount) { index ->
                val audible = index >= leading && index < sampleCount - trailing
                val sample = if (audible) {
                    (sin(index * frequencyHz * 2.0 * PI / sampleRate) * 8_000.0).toInt().toShort()
                } else {
                    0
                }
                repeat(channelCount) { channel ->
                    output.putShort(if (channel == 0) sample else (sample * 0.7f).toInt().toShort())
                }
            }
            return output.array()
        }
    }

    private companion object {
        val CONFORMANCE_FIXTURES = listOf(
            ConformanceFixture("pcm_tone", 440.0, 0, 0),
            ConformanceFixture("pcm_leading_silence", 330.0, 250, 0),
            ConformanceFixture("pcm_trailing_silence", 550.0, 0, 250),
            ConformanceFixture("pcm_48khz_stereo", 660.0, 0, 0, sampleRate = 48_000, channelCount = 2),
            ConformanceFixture("pcm_short", 770.0, 0, 0, sampleRate = 48_000, durationMs = 250),
            ConformanceFixture("pcm_longer", 220.0, 100, 300, durationMs = 3_000),
        )
    }
}
