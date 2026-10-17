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
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.PlaybackSupport
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.quality.deleteTestOwnedRows
import org.junit.After
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Before
import org.junit.Assert.assertEquals
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
        try {
            context.contentResolver.deleteTestOwnedRows(insertedUris, "device codec")
        } finally {
            insertedUris.clear()
        }
    }

    @Before
    fun grantAudioPermission() {
        if (context.checkSelfPermission(audioPermission()) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            instrumentation.uiAutomation.grantRuntimePermission(context.packageName, audioPermission())
        }
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

    @Test
    fun multichannelAacPreservesChannelsThroughAudioOutput() {
        listOf(6, 7, 8).forEach { channels ->
            val uri = insertFixture("aac-${channels}ch.m4a", "audio/mp4")
            listOf(false, true).forEach { effects ->
                assertAudioOutputStarts("AAC $channels channels effects=$effects", uri, channels, effects)
            }
        }
    }

    @Test
    fun dolbyMp4PlaybackRequiresACompatibleDeviceDecoder() {
        listOf("ac3", "eac3").forEach { codec ->
            val name = "$codec-6ch.m4a"
            val uri = insertFixture(name, "audio/mp4")
            val format = AudioFormatDetector(context).detect(uri, name, "audio/mp4")
            assertTrue("$codec could not be inspected", format.detectionSucceeded)
            assertEquals(6, format.channelCount)
            if (format.decoderAvailable == true) {
                assertEquals(PlaybackSupport.Supported, AudioFormatPolicy.playbackSupport(format))
                assertAudioOutputStarts(codec, uri, 6)
            } else {
                assertEquals(PlaybackSupport.Unsupported, AudioFormatPolicy.playbackSupport(format))
            }
        }
    }

    private fun assertAudioOutputStarts(
        mimeType: String,
        uri: Uri,
        expectedChannels: Int? = null,
        enableSignalProcessing: Boolean = false,
    ) {
        val outputStarted = CountDownLatch(1)
        val playbackFailure = AtomicReference<PlaybackException?>()
        val outputChannels = AtomicReference<Int?>()
        var player: ExoPlayer? = null
        instrumentation.runOnMainSync {
            player = playerFactory().create(enableSignalProcessing).apply {
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
                        override fun onAudioTrackInitialized(
                            eventTime: AnalyticsListener.EventTime,
                            audioTrackConfig: androidx.media3.exoplayer.audio.AudioSink.AudioTrackConfig,
                        ) {
                            outputChannels.set(Integer.bitCount(audioTrackConfig.channelConfig))
                        }

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
            expectedChannels?.let { assertEquals("$mimeType lost channels", it, outputChannels.get()) }
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
            audioProcessorsProvider = {
                arrayOf(EqualizerAudioProcessor().apply {
                    updateSettings(EqSettings(spaciousness = 0.8f, reverbDurationMs = 500))
                })
            },
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
