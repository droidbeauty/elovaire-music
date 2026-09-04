package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Song
import android.net.TestUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AudioMediaKindClassifierTest {
    @Test
    fun `explicit media store flag wins for every supported extension`() {
        listOf("mp3", "m4a", "m4b", "flac").forEach { extension ->
            assertEquals(
                AudioMediaKind.Audiobook,
                AudioMediaKindClassifier.classify(true, extension, "Music/book.$extension", null),
            )
        }
    }

    @Test
    fun `m4b and audiobook path are conservative fallbacks`() {
        assertEquals(AudioMediaKind.Audiobook, AudioMediaKindClassifier.classify(false, "m4b", "Music/book.m4b", null))
        assertEquals(AudioMediaKind.Audiobook, AudioMediaKindClassifier.classify(null, "mp3", "Audiobooks/Test/01.mp3", null))
        assertEquals(AudioMediaKind.Audiobook, AudioMediaKindClassifier.classify(false, "mp3", "Music/Test/01.mp3", null, "Audiobooks/Test"))
        assertEquals(AudioMediaKind.Audiobook, AudioMediaKindClassifier.classify(null, "flac", "music\\AUDIOBOOKS\\Test\\01.flac", null))
        assertEquals(AudioMediaKind.Music, AudioMediaKindClassifier.classify(false, "mp3", "Podcasts/talk.mp3", null))
        assertEquals(AudioMediaKind.Music, AudioMediaKindClassifier.classify(false, "amr", "Recordings/voice.amr", null))
        assertEquals(AudioMediaKind.Music, AudioMediaKindClassifier.classify(false, "3gp", "Voice/voice.3gp", null))
        assertEquals(AudioMediaKind.Music, AudioMediaKindClassifier.classify(false, "mp3", "Music/long-song.mp3", null))
        assertEquals(AudioMediaKind.Music, AudioMediaKindClassifier.classify(null, "m4a", "Music/song.m4a", null))
    }

    @Test
    fun `catalog groups parts by source identity and orders them`() {
        val first = song(id = 1L, path = "Audiobooks/Book/02.mp3", track = 2)
        val second = song(id = 2L, path = "Audiobooks/Book/01.mp3", track = 1)
        val otherSource = song(id = 3L, path = "Audiobooks/Book/01.mp3", track = 1, uri = "content://other/3")
        val books = AudiobookCatalog.build(listOf(first, otherSource, second))

        assertEquals(2, books.size)
        assertEquals(listOf(2L, 1L), books.first { it.parts.size == 2 }.parts.map { it.song.id })
        assertNotEquals(books[0].stableKey, books[1].stableKey)
    }

    @Test
    fun `catalog keeps same titled books separate by author and source`() {
        val authorOne = song(id = 1L, path = "Audiobooks/Book/01.mp3", track = 1, author = "Author One", albumId = 0L)
        val authorTwo = song(id = 2L, path = "Audiobooks/Book/01.mp3", track = 1, author = "Author Two", albumId = 0L)
        val otherSource = song(
            id = 3L,
            path = "Audiobooks/Book/01.mp3",
            track = 1,
            author = "Author One",
            albumId = 0L,
            uri = "content://other/3",
        )

        val books = AudiobookCatalog.build(listOf(authorOne, authorTwo, otherSource))

        assertEquals(3, books.size)
    }

    private fun song(
        id: Long,
        path: String,
        track: Int,
        uri: String = "content://media/external/audio/media/$id",
        author: String = "Author",
        albumId: Long = 42L,
    ) = Song(
        id = id,
        title = "Part $track",
        isExplicit = false,
        artist = author,
        album = "Book",
        releaseYear = null,
        genre = "Unknown Genre",
        audioFormat = path.substringAfterLast('.').uppercase(),
        audioQuality = null,
        fileName = path.substringAfterLast('/'),
        albumId = albumId,
        durationMs = 1_000L,
        trackNumber = track,
        discNumber = 1,
        dateAddedSeconds = 1L,
        uri = TestUri(uri),
        artUri = null,
        libraryPath = path,
        mediaKind = AudioMediaKind.Audiobook,
    )
}
