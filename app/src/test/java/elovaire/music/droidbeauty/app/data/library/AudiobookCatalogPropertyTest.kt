package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AudiobookCatalogPropertyTest {
    @Test
    fun groupingSeparatesSourcesAuthorsAndDuplicatePhysicalIds() {
        val firstBook = listOf(
            song(2L, "First", "Author", "/books/first/part-2.m4b", 2),
            song(1L, "First", "Author", "/books/first/part-1.m4b", 1),
            song(1L, "First", "Author", "/books/first/part-1-copy.m4b", 1),
        )
        val secondSource = song(3L, "First", "Author", "/books/second/part-1.m4b", 1)
        val secondAuthor = song(4L, "First", "Other Author", "/books/first/part-3.m4b", 3)

        val books = AudiobookCatalog.build(firstBook + secondSource + secondAuthor)

        assertEquals(3, books.size)
        assertEquals(listOf(1L, 2L), books.first { it.author == "Author" && it.parts.size == 2 }.parts.map { it.song.id })
    }

    @Test
    fun orderingAndStableKeysDoNotDependOnScannerOrder() {
        val songs = listOf(
            song(2L, "Book", "Author", "/books/book/part-2.m4b", 2),
            song(1L, "Book", "Author", "/books/book/part-1.m4b", 1),
        )

        val forward = AudiobookCatalog.build(songs).single()
        val reversed = AudiobookCatalog.build(songs.reversed()).single()

        assertEquals(forward.stableKey, reversed.stableKey)
        assertEquals(listOf(1L, 2L), forward.parts.map { it.song.id })
        assertNotEquals(forward.parts.first().song.id, forward.parts.last().song.id)
    }

    private fun song(
        id: Long,
        album: String,
        artist: String,
        path: String,
        trackNumber: Int,
    ) = Song(
        id = id,
        title = "Part $trackNumber",
        isExplicit = false,
        artist = artist,
        album = album,
        releaseYear = null,
        genre = "",
        audioFormat = "M4B",
        audioQuality = null,
        fileName = path.substringAfterLast('/'),
        albumId = 0L,
        durationMs = 100_000L,
        trackNumber = trackNumber,
        discNumber = 1,
        dateAddedSeconds = 1L,
        libraryPath = path,
        uri = TestUri("file://$path"),
        artUri = null,
        mediaKind = AudioMediaKind.Audiobook,
    )
}
