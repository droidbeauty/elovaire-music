package elovaire.music.droidbeauty.app.domain.search

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class SearchIndexTest {
    @Test
    fun previewPreservesRankingAcrossTiesSortModesAndInputOrders() {
        val songs = (1L..200L).map { id ->
            song(
                id = id,
                title = if (id % 2 == 0L) "Dréam" else "Dream Song",
                artist = "Dream Artist ${id % 3}",
                albumArtist = null,
            ).copy(album = "Album ${id % 5}")
        }
        for (input in listOf(songs, songs.reversed(), songs.shuffled(kotlin.random.Random(17)))) {
            val index = buildSearchIndex(input, emptyList())
            for (sort in SearchSortMode.entries) {
                for (rawQuery in listOf("dream", "drem", "dream artist", "dréam", "zzzzzz")) {
                    val query = NormalizedSearchQuery.from(rawQuery)
                    val full = buildSearchResults(query, sort, index)
                    val preview = buildSearchResults(query, sort, index, false)
                    assertEquals(full.allMatchingSongs.take(20), preview.matchingSongs)
                    assertEquals(full.totalSongMatchCount, preview.totalSongMatchCount)
                }
            }
        }
    }

    @Test
    fun normalizeSearchText_isLocaleIndependent() {
        val previousLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))

            assertEquals("istanbul", normalizeSearchText("ISTANBUL"))
        } finally {
            Locale.setDefault(previousLocale)
        }
    }

    @Test
    fun queryNormalization_preservesMetadataNoiseWordsAsUserInput() {
        assertEquals("remastered", NormalizedSearchQuery.from("Remastered").value)
        assertEquals("official audio", NormalizedSearchQuery.from("official audio").value)
        assertEquals("feat", NormalizedSearchQuery.from("feat").value)
        assertEquals("", normalizeSearchText("Remastered"))
    }

    @Test
    fun queryNormalization_boundsPastedInputWithoutSplittingCodePoints() {
        val normalized = NormalizedSearchQuery.from("😀".repeat(MAX_SEARCH_QUERY_CODE_POINTS + 8))

        assertEquals(MAX_SEARCH_QUERY_CODE_POINTS, normalized.value.codePointCount(0, normalized.value.length))
        assertFalse(normalized.value.contains('\uFFFD'))
    }

    @Test
    fun scoreMatch_keepsScoreWithPrecomputedComposite() {
        val query = NormalizedSearchQuery.from("glass harbor awake")
        val normalizedTitle = normalizeSearchText("Awake")
        val normalizedArtist = normalizeSearchText("Glass Harbor")
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
            query = NormalizedSearchQuery.from("kind north blue"),
            normalizedTitle = normalizeSearchText("Kind of Blue"),
            normalizedArtist = normalizeSearchText("Miles North"),
            normalizedAlbum = "",
            normalizedComposite = normalizeSearchText("Kind of Blue Miles North"),
        )

        assertNotNull(score)
    }

    @Test
    fun scoreMatch_matchesAlbumArtistAsAnExplicitSearchField() {
        val song = song(
            id = 1L,
            title = "Compilation Track",
            artist = "Featured Performer",
            albumArtist = "Various Artists",
        )
        val result = buildSearchResults(
            query = NormalizedSearchQuery.from("various artists"),
            sortMode = SearchSortMode.Title,
            index = buildSearchIndex(listOf(song), emptyList()),
        )

        assertEquals(listOf(song), result.allMatchingSongs)
    }

    @Test
    fun buildSearchResults_honorsCooperativeCancellationChecks() {
        val index = buildSearchIndex(
            songs = (1L..1_000L).map {
                id -> song(id = id, title = "Track $id", artist = "Artist", albumArtist = null)
            },
            albums = emptyList(),
        )
        val cancellation = CancellationMarker()
        var checks = 0

        try {
            buildSearchResults(
                query = NormalizedSearchQuery.from("track"),
                sortMode = SearchSortMode.Title,
                index = index,
                cancellationCheck = {
                    checks++
                    if (checks == 2) throw cancellation
                },
            )
        } catch (failure: CancellationMarker) {
            assertSame(cancellation, failure)
            assertTrue(checks >= 2)
            return
        }
        throw AssertionError("Search did not honor the cancellation check")
    }

    @Test
    fun scoreMatch_matchesArtistAcronym() {
        val score = scoreMatch(
            query = NormalizedSearchQuery.from("rhcp"),
            normalizedTitle = normalizeSearchText("Californication"),
            normalizedArtist = normalizeSearchText("River Hills City Players"),
            normalizedAlbum = normalizeSearchText("Californication"),
        )

        assertNotNull(score)
    }

    @Test
    fun scoreMatch_allowsConservativeSingleCharacterTypoForLongTokens() {
        val score = scoreMatch(
            query = NormalizedSearchQuery.from("californicaton"),
            normalizedTitle = normalizeSearchText("Californication"),
            normalizedArtist = normalizeSearchText("River Hills City Players"),
            normalizedAlbum = normalizeSearchText("Californication"),
        )

        assertNotNull(score)
    }

    @Test
    fun scoreMatch_rejectsQueryWhenAnyTokenIsMissing() {
        val score = scoreMatch(
            query = NormalizedSearchQuery.from("kind coltrane blue"),
            normalizedTitle = normalizeSearchText("Kind of Blue"),
            normalizedArtist = normalizeSearchText("Miles North"),
            normalizedAlbum = "",
            normalizedComposite = normalizeSearchText("Kind of Blue Miles North"),
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

    @Test
    fun buildSearchResults_previewDoesNotRetainFullSongList() {
        val songs = (1L..30L).map { id ->
            song(
                id = id,
                title = "Dream Song $id",
                artist = "Dream Artist",
                albumArtist = "Dream Artist",
            )
        }
        val index = buildSearchIndex(
            songs = songs,
            albums = listOf(album(songs)),
        )

        val results = buildSearchResults(
            query = NormalizedSearchQuery.from("dream"),
            sortMode = SearchSortMode.Title,
            index = index,
            includeAllSongs = false,
        )

        assertEquals(30, results.totalSongMatchCount)
        assertEquals(20, results.matchingSongs.size)
        assertEquals(emptyList<Song>(), results.allMatchingSongs)
    }

    @Test
    fun buildSearchResults_previewMatchesTheTopSongsFromFullResults() {
        val songs = (1L..60L).map { id ->
            song(
                id = id,
                title = if (id % 2 == 0L) "Dream Song $id" else "Dream Alternate $id",
                artist = if (id % 3 == 0L) "Dream Artist" else "Other Artist",
                albumArtist = null,
            )
        }
        val index = buildSearchIndex(songs = songs, albums = emptyList())
        val query = NormalizedSearchQuery.from("dream")

        val fullResults = buildSearchResults(
            query = query,
            sortMode = SearchSortMode.Artist,
            index = index,
            includeAllSongs = true,
        )
        val previewResults = buildSearchResults(
            query = query,
            sortMode = SearchSortMode.Artist,
            index = index,
            includeAllSongs = false,
        )

        assertEquals(fullResults.totalSongMatchCount, previewResults.totalSongMatchCount)
        assertEquals(fullResults.matchingSongs.take(20), previewResults.matchingSongs)
        assertEquals(emptyList<Song>(), previewResults.allMatchingSongs)
    }

    private fun song(
        id: Long,
        title: String = "Song $id",
        artist: String,
        albumArtist: String?,
    ): Song {
        return Song(
            id = id,
            title = title,
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

    private fun album(songs: List<Song>): Album {
        return Album(
            id = 1L,
            title = "Dream Album",
            artist = "Dream Artist",
            artUri = null,
            songCount = songs.size,
            durationMs = songs.sumOf(Song::durationMs),
            songs = songs,
        )
    }

    private class CancellationMarker : RuntimeException()
}
