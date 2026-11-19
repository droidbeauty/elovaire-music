package elovaire.music.droidbeauty.app.data.playback.library

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.DefaultPlaybackResumptionGateway
import elovaire.music.droidbeauty.app.data.playback.PersistedPlaybackSession
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.data.playback.resolveMediaButtonResumption
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
                    catalog = TestMediaCatalog(EmptyBrowser, FixedCommandResolver(song)),
                    playback = PlaybackManagerMediaPlaybackPort(playbackManager),
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

    @Test
    fun mediaButtonResumptionRequiresTheEntirePersistedQueue() {
        val persisted = PersistedPlaybackSession(
            queueSongIds = listOf(2L, 1L),
            currentSongId = 1L,
            currentIndex = 1,
            positionMs = 500L,
            repeatMode = PlaybackRepeatMode.All,
            shuffleEnabled = true,
            sourcePlaylistId = null,
            wasPlaying = true,
            savedAtWallTimeMs = 1L,
        )

        assertNull(resolveMediaButtonResumption(persisted, listOf(testSong())))

        val resolved = resolveMediaButtonResumption(
            persisted,
            listOf(testSong(), testSong().copy(id = 2L, uri = Uri.parse("content://elovaire.test/media/2"))),
        )
        assertEquals(listOf(2L, 1L), resolved?.queue?.queue?.map(Song::id))
        assertEquals(1L, resolved?.queue?.startSong?.id)
        assertEquals(500L, resolved?.persisted?.positionMs)
        assertEquals(PlaybackRepeatMode.All, resolved?.persisted?.repeatMode)
        assertEquals(true, resolved?.persisted?.shuffleEnabled)
    }

    @Test
    fun mediaButtonResumptionPreservesDuplicatesAndTheLastLibraryMatch() {
        val first = testSong()
        val last = first.copy(title = "Updated")
        val other = first.copy(id = 2L, uri = Uri.parse("content://elovaire.test/media/2"))
        val persisted = PersistedPlaybackSession(
            queueSongIds = listOf(1L, 2L, 1L), currentSongId = 1L, currentIndex = 2,
            positionMs = 500L, repeatMode = PlaybackRepeatMode.Off, shuffleEnabled = false,
            sourcePlaylistId = null, wasPlaying = false, savedAtWallTimeMs = 1L,
        )
        val unrelated = (3L..1_000L).map { first.copy(id = it) }

        val resolved = resolveMediaButtonResumption(persisted, listOf(first, other) + unrelated + last)

        assertEquals(listOf(last, other, last), resolved?.queue?.queue)
        assertEquals(last, resolved?.queue?.startSong)
        assertNull(resolveMediaButtonResumption(persisted.copy(currentSongId = null), listOf(first, other)))
        assertNull(resolveMediaButtonResumption(persisted.copy(queueSongIds = emptyList()), listOf(first)))
    }

    @Test
    fun mediaButtonGatewaySkipsLibraryReadForAnUnusableSession() = runBlocking {
        val persisted = PersistedPlaybackSession(
            queueSongIds = listOf(1L), currentSongId = 2L, currentIndex = 0, positionMs = 0L,
            repeatMode = PlaybackRepeatMode.Off, shuffleEnabled = false, sourcePlaylistId = null,
            wasPlaying = false, savedAtWallTimeMs = 1L,
        )
        var libraryReads = 0
        val gateway = DefaultPlaybackResumptionGateway(
            hasAudioReadPermission = { true },
            persistedSessionReader = { persisted },
            librarySongsReader = { libraryReads++; listOf(testSong()) },
        )

        assertNull(gateway.resolve())
        assertEquals(0, libraryReads)
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

private class TestMediaCatalog(
    browser: MediaLibraryBrowser,
    commandResolver: MediaLibraryCommandResolver,
) : MediaCatalogPort,
    MediaLibraryBrowser by browser,
    MediaLibraryCommandResolver by commandResolver
