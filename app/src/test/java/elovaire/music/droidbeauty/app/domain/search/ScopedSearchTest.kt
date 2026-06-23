package elovaire.music.droidbeauty.app.domain.search

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class ScopedSearchTest {
    @Test
    fun searchSongsForPicker_matchesAcrossNormalizedFields() {
        val songs = listOf(
            song(
                id = 1L,
                title = "Halo",
                artist = "Beyonce",
                album = "I Am... Sasha Fierce",
            ),
            song(
                id = 2L,
                title = "Another Song",
                artist = "Someone Else",
                album = "Elsewhere",
            ),
        )

        val result = searchSongsForPicker(songs, "beyoncé halo")

        assertEquals(listOf(1L), result.map(Song::id))
    }

    @Test
    fun searchSongsForPicker_matchesArtistAndAlbumTokens() {
        val songs = listOf(
            song(
                id = 1L,
                title = "So What",
                artist = "Miles Davis",
                album = "Kind of Blue",
            ),
            song(
                id = 2L,
                title = "Blue Train",
                artist = "John Coltrane",
                album = "Blue Train",
            ),
        )

        val result = searchSongsForPicker(songs, "miles blue")

        assertEquals(listOf(1L), result.map(Song::id))
    }

    @Test
    fun searchSongsForPicker_preservesInputOrderWhenBlank() {
        val songs = listOf(
            song(id = 5L, title = "B Song"),
            song(id = 3L, title = "A Song"),
        )

        val result = searchSongsForPicker(songs, "")

        assertEquals(listOf(5L, 3L), result.map(Song::id))
    }

    @Test
    fun searchAlbumsForPicker_usesSharedNormalization() {
        val albums = listOf(
            album(id = 1L, title = "Héroes del Silencio", artist = "Bunbury"),
            album(id = 2L, title = "Random Access Memories", artist = "Daft Punk"),
        )

        val result = searchAlbumsForPicker(albums, "heroes")

        assertEquals(listOf(1L), result.map(Album::id))
    }

    @Test
    fun searchPlaylists_matchesNormalizedNames() {
        val playlists = listOf(
            Playlist(id = 1L, name = "Beyoncé / Favorites"),
            Playlist(id = 2L, name = "Late Night"),
        )

        val result = searchPlaylists(playlists, "beyonce favorites")

        assertEquals(listOf(1L), result.map(Playlist::id))
    }

    private fun song(
        id: Long,
        title: String,
        artist: String = "Artist",
        album: String = "Album",
    ): Song {
        return Song(
            id = id,
            title = title,
            isExplicit = false,
            artist = artist,
            album = album,
            releaseYear = null,
            genre = "",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "$id.mp3",
            albumId = id,
            durationMs = 180_000L,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = id,
            uri = TestUri(),
            artUri = null,
        )
    }

    private fun album(
        id: Long,
        title: String,
        artist: String,
    ): Album {
        return Album(
            id = id,
            title = title,
            artist = artist,
            artUri = null,
            songCount = 1,
            durationMs = 180_000L,
            songs = listOf(song(id = id, title = title, artist = artist, album = title)),
        )
    }
}
