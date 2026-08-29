package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.testing.MainDispatcherRule
import elovaire.music.droidbeauty.app.testing.testSong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackResumptionGatewayTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun deniedPermissionDoesNotReadPlaybackOrLibraryState() = runTest(mainDispatcherRule.scheduler) {
        var sessionReads = 0
        var libraryReads = 0
        val gateway = DefaultPlaybackResumptionGateway(
            hasAudioReadPermission = { false },
            persistedSessionReader = { sessionReads++; null },
            librarySongsReader = { libraryReads++; emptyList() },
            ioDispatcher = mainDispatcherRule.dispatcher,
        )

        assertNull(gateway.resolve())
        assertEquals(0, sessionReads)
        assertEquals(0, libraryReads)
    }

    @Test
    fun resolutionUsesInjectedGatewayExecutionDispatcher() = runTest(mainDispatcherRule.scheduler) {
        val song = testSong()
        val session = PersistedPlaybackSession(
            queueSongIds = listOf(song.id),
            currentSongId = song.id,
            currentIndex = 0,
            positionMs = 10_000L,
            repeatMode = PlaybackRepeatMode.Off,
            shuffleEnabled = false,
            sourcePlaylistId = null,
            wasPlaying = false,
            savedAtWallTimeMs = 1L,
        )
        val gateway = DefaultPlaybackResumptionGateway(
            hasAudioReadPermission = { true },
            persistedSessionReader = { session },
            librarySongsReader = { listOf(song) },
            ioDispatcher = mainDispatcherRule.dispatcher,
        )

        val resumption = gateway.resolve()

        assertTrue(resumption != null)
        assertEquals(listOf(song), resumption?.queue?.queue)
        assertEquals(song.id, resumption?.queue?.startSong?.id)
    }
}
