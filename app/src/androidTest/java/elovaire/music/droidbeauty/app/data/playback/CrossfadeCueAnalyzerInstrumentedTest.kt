package elovaire.music.droidbeauty.app.data.playback

import android.content.ContentValues
import android.media.MediaFormat
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.Song
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CrossfadeCueAnalyzerInstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val resolver = context.contentResolver
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var fixtureUri: Uri

    @Before
    fun setUp() {
        fixtureUri = insertFixture()
    }

    @After
    fun tearDown() {
        resolver.delete(fixtureUri, null, null)
        scope.cancel()
    }

    @Test
    fun analyzesGeneratedWaveWithLeadingAndTrailingSilence() = runBlocking {
        val song = fixtureSong()
        val cue = withTimeout(30_000L) {
            CrossfadeCueAnalyzer(context, scope).analyzePair(song, song)
        }

        assertTrue(cue.outgoingAnalysisSucceeded)
        assertTrue(cue.incomingAnalysisSucceeded)
        assertWithin(15_000L, cue.outgoingMixOutMs)
        assertWithin(5_000L, cue.outgoingTrailingSilenceMs)
        assertWithin(1_000L, cue.incomingMixInMs)
        assertWithin(1_000L, cue.incomingLeadingSilenceMs)
    }

    private fun assertWithin(expectedMs: Long, actualMs: Long) {
        assertTrue("expected=$expectedMs actual=$actualMs", kotlin.math.abs(expectedMs - actualMs) <= 100L)
    }

    private fun insertFixture(): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "elovaire-crossfade-cue.wav")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/ElovaireCrossfadeTest")
            if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Unable to create crossfade fixture")
        resolver.openOutputStream(uri)?.use { output ->
            output.write(createWaveFixture())
        } ?: error("Unable to write crossfade fixture")
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

    private fun fixtureSong() = Song(
        id = 9_001L,
        title = "Generated Crossfade Cue",
        isExplicit = false,
        artist = "Elovaire Test",
        album = "Elovaire Crossfade",
        releaseYear = null,
        genre = "",
        audioFormat = MediaFormat.MIMETYPE_AUDIO_RAW,
        audioQuality = null,
        fileName = "elovaire-crossfade-cue.wav",
        albumId = 9_001L,
        durationMs = 20_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 0L,
        uri = fixtureUri,
        artUri = null,
    )

    private fun createWaveFixture(): ByteArray {
        val sampleRate = 44_100
        val channelCount = 1
        val durationSeconds = 20
        val sampleCount = sampleRate * durationSeconds
        val pcmBytes = sampleCount * 2
        val wave = ByteBuffer.allocate(44 + pcmBytes).order(ByteOrder.LITTLE_ENDIAN)
        wave.put("RIFF".toByteArray())
        wave.putInt(36 + pcmBytes)
        wave.put("WAVE".toByteArray())
        wave.put("fmt ".toByteArray())
        wave.putInt(16)
        wave.putShort(1)
        wave.putShort(channelCount.toShort())
        wave.putInt(sampleRate)
        wave.putInt(sampleRate * channelCount * 2)
        wave.putShort((channelCount * 2).toShort())
        wave.putShort(16)
        wave.put("data".toByteArray())
        wave.putInt(pcmBytes)
        repeat(sampleCount) { index ->
            val second = index / sampleRate
            val sample = if (second in 1 until 15) {
                (sin(index * 440.0 * 2.0 * PI / sampleRate) * 8_000.0).toInt().toShort()
            } else {
                0.toShort()
            }
            wave.putShort(sample)
        }
        return wave.array()
    }
}
