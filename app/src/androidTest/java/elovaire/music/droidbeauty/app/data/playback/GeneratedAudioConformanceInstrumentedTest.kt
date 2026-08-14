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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.PI
import kotlin.math.sin
import org.junit.After
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

    private data class ConformanceFixture(
        val name: String,
        val frequencyHz: Double,
        val leadingSilenceMs: Int,
        val trailingSilenceMs: Int,
    ) {
        fun wavBytes(): ByteArray {
            val sampleRate = 44_100
            val durationMs = 1_500
            val sampleCount = sampleRate * durationMs / 1_000
            val pcmBytes = sampleCount * 2
            val output = ByteBuffer.allocate(44 + pcmBytes).order(ByteOrder.LITTLE_ENDIAN)
            output.put("RIFF".toByteArray())
            output.putInt(36 + pcmBytes)
            output.put("WAVEfmt ".toByteArray())
            output.putInt(16)
            output.putShort(1)
            output.putShort(1)
            output.putInt(sampleRate)
            output.putInt(sampleRate * 2)
            output.putShort(2)
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
                output.putShort(sample)
            }
            return output.array()
        }
    }

    private companion object {
        val CONFORMANCE_FIXTURES = listOf(
            ConformanceFixture("pcm_tone", 440.0, 0, 0),
            ConformanceFixture("pcm_leading_silence", 330.0, 250, 0),
            ConformanceFixture("pcm_trailing_silence", 550.0, 0, 250),
        )
    }
}
