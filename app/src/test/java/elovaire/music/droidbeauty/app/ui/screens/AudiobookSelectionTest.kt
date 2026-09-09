package elovaire.music.droidbeauty.app.ui.screens

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudiobookSelectionTest {
    @Test
    fun togglingSelectionAddsAndRemovesBookKeys() {
        val selected = toggleAudiobookSelection(emptySet(), "book-a")

        assertEquals(setOf("book-a"), selected)
        assertTrue(toggleAudiobookSelection(selected, "book-a").isEmpty())
    }

    @Test
    fun pruningSelectionDropsBooksRemovedFromTheLibrary() {
        val books = listOf(audiobook("book-a", song(1L)))

        assertEquals(
            setOf("book-a"),
            pruneAudiobookSelection(setOf("book-a", "missing"), books),
        )
    }

    @Test
    fun selectedSongsFollowBookOrderAndExcludeDuplicateSyntheticParts() {
        val sharedSong = song(1L)
        val books = listOf(
            audiobook(
                key = "book-a",
                parts = listOf(
                    AudiobookPart(sharedSong, number = 1, titleOverride = "Chapter 1"),
                    AudiobookPart(sharedSong, number = 2, titleOverride = "Chapter 2"),
                ),
            ),
            audiobook("book-b", song(2L)),
        )

        assertEquals(
            listOf(1L, 2L),
            selectedAudiobookSongs(books, setOf("book-a", "book-b")).map(Song::id),
        )
    }

    private fun audiobook(key: String, song: Song): Audiobook = audiobook(key, listOf(AudiobookPart(song, 1)))

    private fun audiobook(key: String, parts: List<AudiobookPart>) = Audiobook(
        stableKey = key,
        title = key,
        author = "Author",
        artUri = null,
        durationMs = parts.sumOf { it.durationMs },
        parts = parts,
    )

    private fun song(id: Long) = Song(
        id = id,
        title = "Part $id",
        isExplicit = false,
        artist = "Author",
        album = "Book",
        releaseYear = null,
        genre = "Audiobook",
        audioFormat = "M4B",
        audioQuality = null,
        fileName = "part$id.m4b",
        albumId = id,
        durationMs = 1_000L,
        trackNumber = id.toInt(),
        discNumber = 1,
        dateAddedSeconds = id,
        uri = TestUri("file:///books/part$id.m4b"),
        artUri = null,
    )
}
