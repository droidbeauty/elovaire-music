package elovaire.music.droidbeauty.app.data.playback.library

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaLibrarySessionCommandTest {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var playbackManager: PlaybackManager
    private lateinit var controller: MediaController

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val song = testSong()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            playbackManager = PlaybackManager(context, scope)
            playbackManager.setMediaLibrarySessionCallback(
                ElovaireMediaLibrarySessionCallback(
                    browser = EmptyBrowser,
                    commandResolver = FixedCommandResolver(song),
                    playbackManager = playbackManager,
                ),
            )
        }
        controller = MediaController.Builder(context, playbackManager.mediaSessionToken)
            .buildAsync()
            .get(5, TimeUnit.SECONDS)
    }

    @After
    fun tearDown() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            controller.release()
            playbackManager.release()
        }
        scope.cancel()
    }

    @Test
    fun resolvingExternalMediaItem_doesNotStartPlaybackBeforePlayCommand() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            controller.setMediaItem(MediaItem.Builder().setMediaId(ElovaireMediaIds.song(1L)).build())
        }
        instrumentation.waitForIdleSync()
        var playWhenReady = true
        instrumentation.runOnMainSync { playWhenReady = controller.playWhenReady }

        assertEquals(listOf(1L), playbackManager.state.value.queue.map(Song::id))
        assertFalse(playbackManager.state.value.transportShowsPause)
        assertFalse(playWhenReady)
        assertEquals(0L, playbackManager.manualPlaybackStartVersion.value)
    }

    private fun testSong() = Song(
        id = 1L,
        title = "Fixture",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "fixture.mp3",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 0L,
        uri = Uri.parse("content://elovaire.test/media/1"),
        artUri = null,
    )
}

private object EmptyBrowser : MediaLibraryBrowser {
    override fun childrenOf(id: ElovaireMediaId) = emptyList<MediaItem>()
    override fun item(mediaId: String): MediaItem? = null
    override fun search(query: String, limit: Int) = emptyList<MediaItem>()
}

private class FixedCommandResolver(song: Song) : MediaLibraryCommandResolver {
    private val resolved = ResolvedPlayableQueue(song, listOf(song), "Fixture", null)

    override fun resolvePlayableQueue(mediaId: String) = resolved
    override fun resolveSearchQueue(query: String) = resolved
    override fun defaultPlayableQueue() = resolved
    override fun resumptionQueue() = resolved
}
