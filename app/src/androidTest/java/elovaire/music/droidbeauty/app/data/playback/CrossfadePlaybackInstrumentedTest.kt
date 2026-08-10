package elovaire.music.droidbeauty.app.data.playback

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CrossfadePlaybackInstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val resolver = context.contentResolver
    private lateinit var firstUri: Uri
    private lateinit var secondUri: Uri
    private lateinit var scope: CoroutineScope
    private lateinit var playbackManager: PlaybackManager

    @Before
    fun setUp() {
        firstUri = insertFixture("elovaire-crossfade-outgoing.wav")
        secondUri = insertFixture("elovaire-crossfade-incoming.wav")
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    @After
    fun tearDown() {
        instrumentation.runOnMainSync {
            if (::playbackManager.isInitialized) playbackManager.release()
        }
        scope.cancel()
        resolver.delete(firstUri, null, null)
        resolver.delete(secondUri, null, null)
    }

    @Test
    fun promotesPreparedIncomingPlayerAfterCueDrivenEqualPowerFade() {
        val outgoing = fixtureSong(1L, firstUri, 20_000L)
        val incoming = fixtureSong(2L, secondUri, 20_000L)
        val album = Album(
            id = 9_002L,
            title = "Generated Crossfade",
            artist = "Elovaire Test",
            artUri = null,
            songCount = 2,
            durationMs = outgoing.durationMs + incoming.durationMs,
            songs = listOf(outgoing, incoming),
        )

        instrumentation.runOnMainSync {
            playbackManager = PlaybackManager(context, scope)
            playbackManager.setCrossfadeEnabled(true)
            playbackManager.playAlbum(album)
        }
        val initialPlayerVersion = playbackManager.playerInstanceVersion.value
        val deadline = SystemClock.elapsedRealtime() + 25_000L
        while (SystemClock.elapsedRealtime() < deadline) {
            if (playbackManager.state.value.currentSong?.id == incoming.id) break
            Thread.sleep(100L)
        }

        assertEquals(incoming.id, playbackManager.state.value.currentSong?.id)
        assertTrue(playbackManager.playerInstanceVersion.value > initialPlayerVersion)
    }

    private fun insertFixture(name: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, name)
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

    private fun fixtureSong(id: Long, uri: Uri, durationMs: Long) = Song(
        id = id,
        title = "Generated Crossfade $id",
        isExplicit = false,
        artist = "Elovaire Test",
        album = "Generated Crossfade",
        releaseYear = null,
        genre = "",
        audioFormat = "WAV",
        audioQuality = null,
        fileName = uri.lastPathSegment.orEmpty(),
        albumId = 9_002L,
        durationMs = durationMs,
        trackNumber = id.toInt(),
        discNumber = 1,
        dateAddedSeconds = 0L,
        uri = uri,
        artUri = null,
    )

    private fun createWaveFixture(): ByteArray {
        val sampleRate = 44_100
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
        wave.putShort(1)
        wave.putInt(sampleRate)
        wave.putInt(sampleRate * 2)
        wave.putShort(2)
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
