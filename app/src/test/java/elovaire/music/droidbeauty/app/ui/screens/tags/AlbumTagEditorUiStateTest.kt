package elovaire.music.droidbeauty.app.ui.screens.tags

import android.net.TestUri
import elovaire.music.droidbeauty.app.data.tags.TagFieldEdit
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumTagEditorUiStateTest {
    @Test
    fun toAlbumTagEditRequest_includesVisibleAlbumAndTrackEdits() {
        val album = testAlbum()
        val state = album.toTagEditorUiState().copy(
            albumTitle = "Edited Album",
            albumArtist = "Edited Album Artist",
            releaseYear = "2026",
            genre = "Ambient",
            tracks = album.toTagEditorUiState().tracks.map { track ->
                if (track.songId == 1L) {
                    track.copy(
                        title = "Edited Song",
                        artist = "Edited Artist",
                        trackNumber = "7",
                        discNumber = "2",
                    )
                } else {
                    track
                }
            },
        ).recalculateFlags()

        val request = requireNotNull(state.toAlbumTagEditRequest())

        assertEquals(TagFieldEdit.Value("Edited Album"), request.albumTitle)
        assertEquals(TagFieldEdit.Value("Edited Album Artist"), request.albumArtist)
        assertEquals(TagFieldEdit.Value(2026), request.releaseYear)
        assertEquals(TagFieldEdit.Value("Ambient"), request.genre)
        assertEquals(1, request.tracks.size)
        assertEquals("Edited Song", request.tracks.single().title)
        assertEquals("Edited Artist", request.tracks.single().artist)
        assertEquals(7, request.tracks.single().trackNumber)
        assertEquals(2, request.tracks.single().discNumber)
        assertTrue(state.canSave)
    }

    @Test
    fun recalculateFlags_disablesSaveWhileSaving() {
        val state = testAlbum().toTagEditorUiState()
            .copy(albumTitle = "Edited Album", isSaving = true)
            .recalculateFlags()

        assertTrue(state.hasUnsavedChanges)
        assertFalse(state.canSave)
    }

    private fun testAlbum(): Album {
        val songs = listOf(
            Song(
                id = 1L,
                title = "Original Song",
                isExplicit = false,
                artist = "Original Artist",
                album = "Original Album",
                releaseYear = 2024,
                genre = "Rock",
                audioFormat = "MP3",
                audioQuality = null,
                fileName = "song.mp3",
                albumId = 10L,
                durationMs = 60_000L,
                trackNumber = 1,
                discNumber = 1,
                dateAddedSeconds = 1L,
                uri = TestUri("content://media/song/1"),
                artUri = TestUri("content://media/song/1/art"),
                albumArtist = "Original Album Artist",
            ),
            Song(
                id = 2L,
                title = "Second Song",
                isExplicit = false,
                artist = "Original Artist",
                album = "Original Album",
                releaseYear = 2024,
                genre = "Rock",
                audioFormat = "MP3",
                audioQuality = null,
                fileName = "song-2.mp3",
                albumId = 10L,
                durationMs = 62_000L,
                trackNumber = 2,
                discNumber = 1,
                dateAddedSeconds = 1L,
                uri = TestUri("content://media/song/2"),
                artUri = TestUri("content://media/song/2/art"),
                albumArtist = "Original Album Artist",
            ),
        )
        return Album(
            id = 10L,
            title = "Original Album",
            artist = "Original Album Artist",
            artUri = TestUri("content://media/album/10/art"),
            songCount = songs.size,
            durationMs = songs.sumOf(Song::durationMs),
            songs = songs,
        )
    }
}
