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

    private fun album(id: Long, addedAtSeconds: Long): Album {
        val song = Song(
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
            albumId = id,
            durationMs = 1_000L,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = addedAtSeconds,
            uri = TestUri("content://media/$id"),
            artUri = null,
        )
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
}
