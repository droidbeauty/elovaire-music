package elovaire.music.droidbeauty.app.data.tags

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.screens.tags.toAudiobookTagEditRequest
import elovaire.music.droidbeauty.app.ui.screens.tags.toTagEditorUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudiobookTagEditorModelsTest {
    @Test
    fun editorShowsOnePhysicalRowForRepeatedEmbeddedChapterParts() {
        val song = song(1L)
        val book = Audiobook(
            stableKey = "book",
            title = "Book",
            author = "Author",
            artUri = null,
            durationMs = song.durationMs,
            parts = listOf(
                AudiobookPart(song, 1, "Chapter 1", 0L, 10L),
                AudiobookPart(song, 2, "Chapter 2", 10L, 20L),
            ),
        )

        assertEquals(1, book.toTagEditorUiState().parts.size)
    }

    @Test
    fun changedBookFieldsMapToCollectionTagsWithoutFanoutTrackEdits() {
        val song = song(1L)
        val state = Audiobook(
            stableKey = "book",
            title = "Book",
            author = "Author",
            artUri = null,
            durationMs = song.durationMs,
            parts = listOf(AudiobookPart(song, 1)),
            description = "Original description",
        ).toTagEditorUiState().copy(
            bookTitle = "Renamed",
            author = "New Author",
            description = "Updated description",
        )

        val request = state.toAudiobookTagEditRequest()!!

        assertTrue(request.bookTitle is TagFieldEdit.Value)
        assertTrue(request.author is TagFieldEdit.Value)
        assertTrue(request.description is TagFieldEdit.Value)
        assertTrue(request.tracks.isEmpty())
        assertEquals("Renamed", (request.bookTitle as TagFieldEdit.Value).value)
        assertEquals("Updated description", (request.description as TagFieldEdit.Value).value)
    }

    @Test
    fun changingOnePartDoesNotNormalizeMixedBookMetadata() {
        val first = song(1L)
        val second = song(2L).copy(
            albumArtist = "Different Author",
            releaseYear = 2021,
            genre = "Other Genre",
        )
        val book = Audiobook(
            stableKey = "book",
            title = "Book",
            author = "Author",
            artUri = null,
            durationMs = first.durationMs + second.durationMs,
            parts = listOf(AudiobookPart(first, 1), AudiobookPart(second, 2)),
        )

        val state = book.toTagEditorUiState().copy(parts = book.toTagEditorUiState().parts.map { part ->
            if (part.songId == first.id) part.copy(title = "Renamed part") else part
        })
        val request = state.toAudiobookTagEditRequest()!!

        assertTrue(request.collectionTitle is TagFieldEdit.Unchanged)
        assertTrue(request.collectionArtist is TagFieldEdit.Unchanged)
        assertTrue(request.releaseYear is TagFieldEdit.Unchanged)
        assertTrue(request.genre is TagFieldEdit.Unchanged)
        assertEquals(listOf(first.id), request.tracks.map(EditableAlbumTrack::songId))
    }

    @Test
    fun retryForFailuresKeepsOnlyFailedPhysicalSongs() {
        val first = song(1L)
        val second = song(2L)
        val request = AudiobookTagEditRequest(
            bookKey = "book",
            songs = listOf(first, second),
            bookTitle = TagFieldEdit.Value("New Book"),
            author = TagFieldEdit.Unchanged,
            releaseYear = TagFieldEdit.Unchanged,
            genre = TagFieldEdit.Unchanged,
            coverArtUri = null,
            tracks = listOf(
                EditableAlbumTrack(1L, "One", "Author", 1, 1),
                EditableAlbumTrack(2L, "Two", "Author", 2, 1),
            ),
        )

        val retry = request.retryForFailures(setOf(2L))

        assertEquals(listOf(2L), retry.songs.map(Song::id))
        assertEquals(listOf(2L), retry.tracks.map(EditableAlbumTrack::songId))
    }

    private fun song(id: Long) = Song(
        id = id,
        title = "Part $id",
        isExplicit = false,
        artist = "Author",
        album = "Book",
        releaseYear = 2020,
        genre = "Audiobook",
        audioFormat = "M4B",
        audioQuality = null,
        fileName = "part$id.m4b",
        albumId = 1L,
        durationMs = 100L,
        trackNumber = id.toInt(),
        discNumber = 1,
        dateAddedSeconds = id,
        uri = TestUri("file:///part$id.m4b"),
        artUri = null,
        albumArtist = "Author",
        mediaKind = AudioMediaKind.Audiobook,
    )
}
