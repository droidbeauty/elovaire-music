package elovaire.music.droidbeauty.app.data.playback

import android.Manifest
import android.content.ContentUris
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

    @Before
    fun grantAudioPermission() {
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, audioPermission())
    }

    @Test
    fun availableRepresentativeDeviceFormatsReachAudioOutput() {
        val availableMedia = REPRESENTATIVE_MIME_TYPES.mapNotNull { mimeType ->
            firstMediaUri(mimeType)?.let { uri -> mimeType to uri }
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

    private fun firstMediaUri(mimeType: String): Uri? {
        return context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID),
            "${MediaStore.Audio.Media.MIME_TYPE} = ?",
            arrayOf(mimeType),
            "${MediaStore.Audio.Media._ID} ASC",
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cursor.getLong(0),
            )
        }
    }

    private fun audioPermission(): String {
        return if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    private companion object {
        val REPRESENTATIVE_MIME_TYPES = listOf(
            "audio/mpeg",
            "audio/flac",
            "audio/mp4",
            "audio/ogg",
        )
    }
}
