package elovaire.music.droidbeauty.app.ui.screens

import android.net.TestUri
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenFormattersTest {
    @Test
    fun recentlyAddedAlbumsFor_sortsRecentAlbumsByLatestTimestamp() {
        val nowSeconds = System.currentTimeMillis() / 1_000L
        val albums = (1L..5L).map { id -> album(id, addedAtSeconds = nowSeconds - id) }

        val result = recentlyAddedAlbumsFor(LibraryUiState(albums = albums))

        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), result.map(Album::id))
    }

    @Test
    fun buildPlaylistPreviewBoundsArtworkSongsButKeepsFullDuration() {
        val songs = (1L..6L).map { id ->
            song(
                id = id,
                albumId = if (id == 2L) 1L else id,
                durationMs = id * 1_000L,
            )
        }

        val result = buildPlaylistPreview(
            songIds = listOf(1L, 2L, 3L, 4L, 5L, 6L, 999L),
            songsById = songs.associateBy(Song::id),
        )

        assertEquals(listOf(1L, 3L, 4L, 5L), result.songs.map(Song::id))
        assertEquals(21_000L, result.durationMs)
    }

    private fun album(id: Long, addedAtSeconds: Long): Album {
        val song = song(id, id, 1_000L).copy(dateAddedSeconds = addedAtSeconds)
        return Album(
            id = id,
            title = "Album $id",
            artist = "Artist",
            artUri = null,
            songCount = 1,
            durationMs = 1_000L,
            songs = listOf(song),
        )
    }

    private fun song(id: Long, albumId: Long, durationMs: Long): Song {
        return Song(
            id = id,
            title = "Song $id",
            isExplicit = false,
            artist = "Artist",
            album = "Album $id",
            releaseYear = null,
            genre = "",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "$id.mp3",
            albumId = albumId,
            durationMs = durationMs,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = 0L,
            uri = TestUri("content://media/$id"),
            artUri = null,
        )
    }
}
