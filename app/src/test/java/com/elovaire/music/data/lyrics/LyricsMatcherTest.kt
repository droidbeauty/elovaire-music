package elovaire.music.app.data.lyrics

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsMatcherTest {

    private val identity = LyricsIdentity(
        title = "This Love",
        artist = "Maroon 5",
        album = "Songs About Jane",
        durationMs = 206_000L,
        mediaId = "1",
        contentUri = "content://song/1",
        normalizedTitle = normalizeTrackTitle("This Love"),
        normalizedArtist = normalizeArtistName("Maroon 5"),
        normalizedAlbum = normalizeAlbumTitle("Songs About Jane"),
        normalizedLookupKey = "maroon 5::this love::206::songs about jane",
        cacheKeys = listOf("maroon 5::this love::206::songs about jane"),
    )

    @Test
    fun `exact candidate scores and matches strongly`() {
        val candidate = LyricsCandidate(
            providerId = "1",
            title = "This Love",
            artist = "Maroon 5",
            album = "Songs About Jane",
            durationMs = 206_000L,
            instrumental = false,
            plainLyrics = "",
            syncedLyrics = "[00:01.00]Line",
        )

        val score = candidate.scoreAgainst(identity)
        assertTrue(score >= 80)
        assertTrue(candidate.isAcceptableMatchFor(identity, score))
    }

    @Test
    fun `wrong artist candidate is rejected`() {
        val candidate = LyricsCandidate(
            providerId = "2",
            title = "This Love",
            artist = "Taylor Swift",
            album = "Songs About Jane",
            durationMs = 206_000L,
            instrumental = false,
            plainLyrics = "Line",
            syncedLyrics = "",
        )

        val score = candidate.scoreAgainst(identity)
        assertFalse(candidate.isAcceptableMatchFor(identity, score))
    }

    @Test
    fun `large duration mismatch is penalized heavily`() {
        val candidate = LyricsCandidate(
            providerId = "3",
            title = "This Love",
            artist = "Maroon 5",
            album = "Songs About Jane",
            durationMs = 245_000L,
            instrumental = false,
            plainLyrics = "Line",
            syncedLyrics = "",
        )

        val score = candidate.scoreAgainst(identity)
        assertTrue(score < 70)
    }
}
