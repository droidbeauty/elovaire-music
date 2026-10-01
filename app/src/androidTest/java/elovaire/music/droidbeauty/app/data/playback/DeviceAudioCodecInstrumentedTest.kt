package elovaire.music.droidbeauty.app.data.playback

import android.Manifest
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
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Before
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

@UnstableApi
class DeviceAudioCodecInstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val testContext = instrumentation.context
    private val insertedUris = mutableListOf<Uri>()

    @After
    fun tearDown() {
        insertedUris.forEach { uri -> runCatching { context.contentResolver.delete(uri, null, null) } }
    }

    @Before
    fun grantAudioPermission() {
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, audioPermission())
    }

    @Test
    fun availableRepresentativeDeviceFormatsReachAudioOutput() {
        val availableMedia = REPRESENTATIVE_FIXTURES.map { (assetName, mimeType) ->
            mimeType to insertFixture(assetName, mimeType)
        }
        assumeTrue("No representative audio formats are available", availableMedia.isNotEmpty())
        availableMedia.forEach { (mimeType, uri) ->
            assertAudioOutputStarts(mimeType, uri)
        }
    }

    private fun assertAudioOutputStarts(
        mimeType: String,
        uri: Uri,
    ) {
        val outputStarted = CountDownLatch(1)
        val playbackFailure = AtomicReference<PlaybackException?>()
        var player: ExoPlayer? = null
        instrumentation.runOnMainSync {
            player = playerFactory().create(enableSignalProcessing = false).apply {
                volume = 0f
                addListener(
                    object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            playbackFailure.set(error)
                            outputStarted.countDown()
                        }
                    },
                )
                addAnalyticsListener(
                    object : AnalyticsListener {
                        override fun onAudioPositionAdvancing(
                            eventTime: AnalyticsListener.EventTime,
                            playoutStartSystemTimeMs: Long,
                        ) {
                            outputStarted.countDown()
                        }
                    },
                )
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                play()
            }
        }
        try {
            assertTrue("$mimeType did not reach audio output", outputStarted.await(20, TimeUnit.SECONDS))
            assertNull("$mimeType failed to decode", playbackFailure.get())
        } finally {
            instrumentation.runOnMainSync { player?.release() }
        }
    }

    private fun playerFactory(): PlaybackPlayerFactory {
        return PlaybackPlayerFactory(
            context = context,
            dataSourceFactory = DefaultDataSource.Factory(context),
            extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true),
            playbackAudioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            audioProcessorsProvider = { emptyArray() },
            preferredOutputDevice = { null },
        )
    }

    private fun insertFixture(assetName: String, mimeType: String): Uri {
        val uri = context.contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "elovaire-codec-$assetName")
                put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/ElovaireCodecTest")
                if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Audio.Media.IS_PENDING, 1)
            },
        ) ?: error("Unable to create codec fixture")
        insertedUris += uri
        context.contentResolver.openOutputStream(uri)?.use { output ->
            testContext.assets.open("media-metadata/$assetName").use { input -> input.copyTo(output) }
        } ?: error("Unable to write codec fixture")
        if (Build.VERSION.SDK_INT >= 29) {
            context.contentResolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) },
                null,
                null,
            )
        }
        return uri
    }

    private fun audioPermission(): String {
        return if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    private companion object {
        val REPRESENTATIVE_FIXTURES = listOf(
            "write-fixture.mp3" to "audio/mpeg",
            "write-fixture.flac" to "audio/flac",
            "write-fixture.m4a" to "audio/mp4",
        )
    }
}
