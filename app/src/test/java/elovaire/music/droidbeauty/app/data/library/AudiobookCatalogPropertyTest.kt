package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AudiobookCatalogPropertyTest {
    @Test
    fun stableBookKeysAndIdsKeepTheirExistingSha256Encoding() {
        val songs = listOf(
            song(2L, "Book", "Author", "/books/book/part-2.m4b", 2),
            song(1L, "Book", "Author", "/books/book/part-1.m4b", 1),
        )
        fun digest(value: String): String = java.security.MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(java.util.Locale.ROOT, it) }
        val book = AudiobookCatalog.build(songs).single()
        val anchor = songs.minOf(MediaIdentityResolver::stableKey)

        assertEquals("book:${digest(anchor)}", book.stableKey)
        assertEquals(digest(book.stableKey), AudiobookCatalog.stableId(book))
    }
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
        assertEquals(
            listOf(1L, 1L, 2L),
            books.first { it.author == "Author" && it.parts.size == 3 }.parts.map { it.song.id },
        )
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

    @Test
    fun stableKeySurvivesEditableMetadataAndMediaStoreAlbumIdChanges() {
        val original = song(1L, "Book", "Author", "/books/book/part-1.m4b", 1).copy(albumId = 42L)
        val rescanned = original.copy(
            album = "Renamed book",
            artist = "Renamed author",
            albumArtist = "Renamed author",
            albumId = 99L,
        )

        val originalBook = AudiobookCatalog.build(listOf(original)).single()
        val rescannedBook = AudiobookCatalog.build(listOf(rescanned)).single()

        assertEquals(originalBook.stableKey, rescannedBook.stableKey)
        assertNotEquals(
            AudiobookCatalog.legacyStableKey(originalBook),
            AudiobookCatalog.legacyStableKey(rescannedBook),
        )
    }

    @Test
    fun durationSaturatesWhenPartsExceedLongRange() {
        val first = song(1L, "Book", "Author", "/books/book/part-1.m4b", 1).copy(durationMs = Long.MAX_VALUE)
        val second = song(2L, "Book", "Author", "/books/book/part-2.m4b", 2).copy(durationMs = 10L)

        assertEquals(Long.MAX_VALUE, AudiobookCatalog.build(listOf(first, second)).single().durationMs)
    }

    @Test
    fun directContextResolutionUsesPhysicalSongIdentityWhenIdsCollide() {
        val songs = listOf(
            song(1L, "Book A", "Author", "/books/a/part-1.m4b", 1),
            song(2L, "Book A", "Author", "/books/a/part-2.m4b", 2),
            song(2L, "Book B", "Author", "/books/b/part-2.m4b", 2),
            song(3L, "Book B", "Author", "/books/b/part-3.m4b", 3),
        )
        val expected = AudiobookCatalog.build(songs).first { book ->
            book.parts.any { part -> part.song.libraryPath == "/books/b/part-2.m4b" }
        }

        assertEquals(expected, AudiobookCatalog.findContaining(songs, songs[2]))
    }

    @Test
    fun bookRetainsDifferentPhysicalPartsWithCollidingNumericIds() {
        val first = song(2L, "Book A", "Author", "/books/a/part-1.m4b", 1)
        val second = song(2L, "Book A", "Author", "/books/a/part-2.m4b", 2)

        assertEquals(2, AudiobookCatalog.build(listOf(first, second)).single().parts.size)
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
