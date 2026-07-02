package elovaire.music.droidbeauty.app.domain.search

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SearchIndexTest {
    @Test
    fun scoreMatch_keepsScoreWithPrecomputedComposite() {
        val query = NormalizedSearchQuery.from("dream theater awake")
        val normalizedTitle = normalizeSearchText("Awake")
        val normalizedArtist = normalizeSearchText("Dream Theater")
        val normalizedAlbum = normalizeSearchText("Awake")
        val composite = listOf(normalizedTitle, normalizedArtist, normalizedAlbum).joinToString(" ")

        val computedScore = scoreMatch(
            query = query,
            normalizedTitle = normalizedTitle,
            normalizedArtist = normalizedArtist,
            normalizedAlbum = normalizedAlbum,
        )
        val precomputedScore = scoreMatch(
            query = query,
            normalizedTitle = normalizedTitle,
            normalizedArtist = normalizedArtist,
            normalizedAlbum = normalizedAlbum,
            normalizedComposite = composite,
        )

        assertEquals(computedScore, precomputedScore)
        assertNotNull(precomputedScore)
    }

    @Test
    fun scoreMatch_matchesTokensAcrossFields() {
        val score = scoreMatch(
            query = NormalizedSearchQuery.from("kind davis blue"),
            normalizedTitle = normalizeSearchText("Kind of Blue"),
            normalizedArtist = normalizeSearchText("Miles Davis"),
            normalizedAlbum = "",
            normalizedComposite = normalizeSearchText("Kind of Blue Miles Davis"),
        )

        assertNotNull(score)
    }

    @Test
    fun scoreMatch_rejectsQueryWhenAnyTokenIsMissing() {
        val score = scoreMatch(
            query = NormalizedSearchQuery.from("kind coltrane blue"),
            normalizedTitle = normalizeSearchText("Kind of Blue"),
            normalizedArtist = normalizeSearchText("Miles Davis"),
            normalizedAlbum = "",
            normalizedComposite = normalizeSearchText("Kind of Blue Miles Davis"),
        )

        assertNull(score)
    }

    @Test
    fun buildSearchIndex_groupsArtistsByAlbumArtist() {
        val index = buildSearchIndex(
            songs = listOf(
                song(id = 1L, artist = "Main Artist feat. Guest", albumArtist = "Main Artist"),
                song(id = 2L, artist = "Main Artist", albumArtist = "Main Artist"),
            ),
            albums = emptyList(),
        )

        assertEquals(listOf("Main Artist"), index.artists.map { it.displayName })
        assertEquals(2, index.artists.single().songCount)
    }

    private fun song(
        id: Long,
        artist: String,
        albumArtist: String?,
    ): Song {
        return Song(
            id = id,
            title = "Song $id",
            isExplicit = false,
            artist = artist,
            album = "Album",
            releaseYear = null,
            genre = "",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "$id.mp3",
            albumId = 1L,
            durationMs = 180_000L,
            trackNumber = id.toInt(),
            discNumber = 1,
            dateAddedSeconds = id,
            uri = TestUri(),
            artUri = null,
            albumArtist = albumArtist,
        )
    }
}
