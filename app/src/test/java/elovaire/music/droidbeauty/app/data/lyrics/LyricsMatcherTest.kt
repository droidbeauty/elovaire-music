package elovaire.music.droidbeauty.app.data.lyrics

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LyricsMatcherTest {
    @Test
    fun acceptsStrongMatchWhenLocalDurationIsMissing() {
        val score = scoreLrcLibMatch(
            song = song(durationMs = 0L),
            response = lrcResponse(duration = 239.0),
            songDurationSec = 0.0,
        )

        assertNotNull(score)
    }

    @Test
    fun acceptsStrongMatchWhenProviderDurationIsMissing() {
        val score = scoreLrcLibMatch(
            song = song(durationMs = 239_000L),
            response = lrcResponse(duration = 0.0),
            songDurationSec = 239.0,
        )

        assertNotNull(score)
    }

    @Test
    fun rejectsMismatchedKnownDurations() {
        val score = scoreLrcLibMatch(
            song = song(durationMs = 239_000L),
            response = lrcResponse(duration = 120.0),
            songDurationSec = 239.0,
        )

        assertNull(score)
    }

    @Test
    fun prefersSyncedLyricsForComparableMatches() {
        val ranked = rankLrcLibMatches(
            song = song(durationMs = 239_000L),
            responses = listOf(
                lrcResponse(id = 1, plainLyrics = "plain", syncedLyrics = null),
                lrcResponse(id = 2, plainLyrics = null, syncedLyrics = "[00:01.00]synced"),
            ),
        )

        assertEquals(2, ranked.first().response.id)
    }

    private fun song(durationMs: Long): Song {
        return Song(
            id = 1L,
            title = "Creep",
            isExplicit = false,
            artist = "Radiohead",
            album = "Pablo Honey",
            releaseYear = null,
            genre = "",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "creep.mp3",
            albumId = 1L,
            durationMs = durationMs,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = 1L,
            uri = TestUri(),
            artUri = null,
        )
    }

    private fun lrcResponse(
        id: Int = 1,
        duration: Double = 239.0,
        plainLyrics: String? = "plain",
        syncedLyrics: String? = null,
    ): LrcLibResponse {
        return LrcLibResponse(
            id = id,
            name = "Creep",
            artistName = "Radiohead",
            albumName = "Pablo Honey",
            duration = duration,
            plainLyrics = plainLyrics,
            syncedLyrics = syncedLyrics,
            instrumental = false,
        )
    }
}
