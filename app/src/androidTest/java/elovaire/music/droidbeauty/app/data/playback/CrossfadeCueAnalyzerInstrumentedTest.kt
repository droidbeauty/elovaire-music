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
import kotlin.math.roundToInt
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

    @Test
    fun expandsBackwardWhenTheInitialTailBlockIsEntirelySilent() = runBlocking {
        val uri = insertFixture(
            name = "elovaire-crossfade-long-silent-tail.wav",
            durationSeconds = 35,
            toneEndSecond = 10,
        )
        try {
            val song = fixtureSong(uri = uri, durationMs = 35_000L)
            val cue = withTimeout(30_000L) {
                CrossfadeCueAnalyzer(context, scope).analyzePair(song, song)
            }

            assertTrue(cue.outgoingAnalysisSucceeded)
            assertWithin(10_000L, cue.outgoingMixOutMs)
            assertWithin(25_000L, cue.outgoingTrailingSilenceMs)
        } finally {
            resolver.delete(uri, null, null)
        }
    }

    @Test
    fun thresholdChangesCueForQuietTailAndUsesThresholdAwareCache() = runBlocking {
        val uri = insertFixture(
            name = "elovaire-crossfade-threshold.wav",
            durationSeconds = 6,
            toneEndSecond = 2,
            quietTailStartSecond = 2,
            quietTailEndSecond = 4,
            // Two 16-bit PCM steps are approximately -84 dBFS: below -80, above -90.
            quietTailAmplitude = 2.0 / Short.MAX_VALUE,
        )
        try {
            val song = fixtureSong(uri = uri, durationMs = 6_000L)
            val analyzer = CrossfadeCueAnalyzer(context, scope)
            val atMinus80 = withTimeout(30_000L) {
                analyzer.analyzePair(song, song, silenceLevelDb = -80f)
            }
            val atMinus90 = withTimeout(30_000L) {
                analyzer.analyzePair(song, song, silenceLevelDb = -90f)
            }

            assertWithin(2_000L, atMinus80.outgoingMixOutMs)
            assertWithin(4_000L, atMinus90.outgoingMixOutMs)
            assertTrue(atMinus90.outgoingMixOutMs > atMinus80.outgoingMixOutMs + 1_000L)
        } finally {
            resolver.delete(uri, null, null)
        }
    }

    @Test
    fun decodesCommonCompressedFixtureFormats() = runBlocking {
        val analyzer = CrossfadeCueAnalyzer(context, scope)
        listOf(
            "flac" to "audio/flac",
            "m4a" to "audio/mp4",
            "mp3" to "audio/mpeg",
        ).forEach { (extension, mimeType) ->
            val uri = insertAssetFixture(extension, mimeType)
            try {
                val cue = withTimeout(30_000L) {
                    analyzer.analyzePair(
                        outgoing = fixtureSong(uri = uri, durationMs = 500L),
                        incoming = fixtureSong(uri = uri, durationMs = 500L),
                    )
                }

                assertTrue("$extension outgoing analysis failed", cue.outgoingAnalysisSucceeded)
                assertTrue("$extension incoming analysis failed", cue.incomingAnalysisSucceeded)
            } finally {
                resolver.delete(uri, null, null)
            }
        }
    }

    private fun assertWithin(expectedMs: Long, actualMs: Long) {
        assertTrue("expected=$expectedMs actual=$actualMs", kotlin.math.abs(expectedMs - actualMs) <= 100L)
    }

    private fun insertFixture(
        name: String = "elovaire-crossfade-cue.wav",
        durationSeconds: Int = 20,
        toneEndSecond: Int = 15,
        quietTailStartSecond: Int? = null,
        quietTailEndSecond: Int? = null,
        quietTailAmplitude: Double = 0.0,
    ): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, name)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/ElovaireCrossfadeTest")
            if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Unable to create crossfade fixture")
        resolver.openOutputStream(uri)?.use { output ->
            output.write(
                createWaveFixture(
                    durationSeconds = durationSeconds,
                    toneEndSecond = toneEndSecond,
                    quietTailStartSecond = quietTailStartSecond,
                    quietTailEndSecond = quietTailEndSecond,
                    quietTailAmplitude = quietTailAmplitude,
                ),
            )
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

    private fun insertAssetFixture(extension: String, mimeType: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "elovaire-crossfade-decoder.$extension")
            put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/ElovaireCrossfadeTest")
            if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Unable to create compressed crossfade fixture")
        resolver.openOutputStream(uri)?.use { output ->
            instrumentation.context.assets.open("media-metadata/write-fixture.$extension").use { input ->
                input.copyTo(output)
            }
        } ?: error("Unable to write compressed crossfade fixture")
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

    private fun fixtureSong(
        uri: Uri = fixtureUri,
        durationMs: Long = 20_000L,
    ) = Song(
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
        durationMs = durationMs,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 0L,
        uri = uri,
        artUri = null,
    )

    private fun createWaveFixture(
        durationSeconds: Int,
        toneEndSecond: Int,
        quietTailStartSecond: Int? = null,
        quietTailEndSecond: Int? = null,
        quietTailAmplitude: Double = 0.0,
    ): ByteArray {
        val sampleRate = 44_100
        val channelCount = 1
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
            val sample = if (quietTailStartSecond != null &&
                quietTailEndSecond != null &&
                second in quietTailStartSecond until quietTailEndSecond
            ) {
                (quietTailAmplitude * Short.MAX_VALUE).roundToInt().toShort()
            } else if (second in 1 until toneEndSecond) {
                (sin(index * 440.0 * 2.0 * PI / sampleRate) * 8_000.0).toInt().toShort()
            } else {
                0.toShort()
            }
            wave.putShort(sample)
        }
        return wave.array()
    }
}
