package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.domain.model.Song
import android.net.TestUri
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackInvariantTest {
    @Test
    fun hostileQueueSequenceNeverLeavesAnInvalidCurrentIndex() {
        var state = PlaybackUiState(queue = listOf(song(1), song(2)), currentIndex = 0)
        state = state.copy(currentIndex = 1)
        assertTrue(playbackInvariantViolations(state).isEmpty())
        state = state.copy(queue = listOf(song(2)), currentIndex = 0)
        assertTrue(playbackInvariantViolations(state).isEmpty())
    }

    @Test
    fun invalidPlayingStateIsReported() {
        val violations = playbackInvariantViolations(
            PlaybackUiState(isPlaying = true),
        )

        assertTrue(violations.any { it.contains("current song") })
    }

    @Test
    fun stalePlayerGenerationCannotPublishAfterReplacement() {
        val gate = PlaybackPlayerGenerationGate<Any>()
        val oldPlayer = Any()
        val newPlayer = Any()
        val oldGeneration = gate.activate(oldPlayer)
        val newGeneration = gate.activate(newPlayer)

        assertTrue(!gate.isCurrent(oldPlayer, oldGeneration))
        assertTrue(gate.isCurrent(newPlayer, newGeneration))

        gate.invalidate(newPlayer)

        assertTrue(!gate.isCurrent(newPlayer, newGeneration))
    }

    private fun song(id: Long) = Song(
        id = id,
        title = "Song $id",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "",
        audioFormat = "WAV",
        audioQuality = null,
        fileName = "song$id.wav",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = id.toInt(),
        discNumber = 1,
        dateAddedSeconds = 0L,
        uri = TestUri("file:///song$id.wav"),
        artUri = null,
    )
}
