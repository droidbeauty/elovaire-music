package elovaire.music.droidbeauty.app.data.playback

import android.net.TestUri
import androidx.media3.common.Player
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueControllerTest {
    @Test
    fun stageExternalQueue_doesNotStartPlaybackOrRequestAudioFocus() {
        val runtime = RecordingQueueRuntime(
            PlaybackUiState(
                transportShowsPause = false,
                shuffleEnabled = true,
            ),
        )
        val controller = PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher())
        val songs = listOf(song(1L), song(2L))

        controller.stageExternalQueue(
            songs = songs,
            startIndex = 1,
            sourceLabel = "External",
            sourcePlaylistId = 9L,
            audioPathDelayMs = 80L,
        )

        assertFalse(runtime.audioFocusRequested)
        assertFalse(runtime.playerAccessed)
        assertEquals(songs, runtime.state.queue)
        assertEquals(1, runtime.state.currentIndex)
        assertEquals("External", runtime.state.sourceLabel)
        assertEquals(9L, runtime.state.sourcePlaylistId)
        assertFalse(runtime.state.transportShowsPause)
        assertTrue(runtime.state.shuffleEnabled)
    }

    private fun song(id: Long) = Song(
        id = id,
        title = "Song $id",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "$id.mp3",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = id.toInt(),
        discNumber = 1,
        dateAddedSeconds = 0L,
        uri = TestUri("content://media/$id"),
        artUri = null,
    )
}

private class RecordingQueueRuntime(
    initialState: PlaybackUiState,
) : PlaybackQueueRuntime {
    override var state: PlaybackUiState = initialState
        private set
    var audioFocusRequested = false
    var playerAccessed = false

    override val player: Player
        get() {
            playerAccessed = true
            error("Staging an external queue must not access the player")
        }

    override fun publishState(state: PlaybackUiState) {
        this.state = state
    }

    override fun updateState() = error("Staging must wait for MediaSession to publish player state")

    override fun requestAudioFocus(): Boolean {
        audioFocusRequested = true
        return true
    }

    override fun effectivePlayerGain() = 1f
    override fun cancelPauseFade(resetVolume: Boolean) = Unit
    override fun clearInterruptionResumeState() = Unit
    override fun recordManualPlaybackStart() = Unit
    override fun stopAndClearQueue() = Unit
    override fun resetAudioPathState() = Unit
    override fun resetUnexpectedIdleRecoveryGuard() = Unit
    override fun onQueueReplaced(songs: List<Song>) = Unit
    override fun resolveCurrentQueueIndex(state: PlaybackUiState) = state.currentIndex
    override fun scheduleAudioPathReevaluation(reason: String, delayMs: Long) = Unit
    override fun requestFormatFailureReset() = Unit
    override fun clearFailedPlaybackSongIds() = Unit
}
