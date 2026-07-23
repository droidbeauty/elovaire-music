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
                title = "Northern Signal",
                artist = "Aurora Vale",
                album = "Glass Horizons",
            ),
            song(
                id = 2L,
                title = "Another Song",
                artist = "Someone Else",
                album = "Elsewhere",
            ),
        )

        val result = searchSongsForPicker(songs, "auróra signal")

        assertEquals(listOf(1L), result.map(Song::id))
    }

    @Test
    fun searchSongsForPicker_matchesArtistAndAlbumTokens() {
        val songs = listOf(
            song(
                id = 1L,
                title = "Blue Meridian",
                artist = "Miles North",
                album = "Shades of Blue",
            ),
            song(
                id = 2L,
                title = "Night Train",
                artist = "Jonas Reed",
                album = "Night Lines",
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
            album(id = 1L, title = "Héroes del Viento", artist = "Luz Norte"),
            album(id = 2L, title = "Memory Atlas", artist = "Signal Pair"),
        )

        val result = searchAlbumsForPicker(albums, "heroes")

        assertEquals(listOf(1L), result.map(Album::id))
    }

    @Test
    fun searchPlaylists_matchesNormalizedNames() {
        val playlists = listOf(
            Playlist(id = 1L, name = "Auróra / Favorites"),
            Playlist(id = 2L, name = "Late Night"),
        )

        val result = searchPlaylists(playlists, "aurora favorites")

        assertEquals(listOf(1L), result.map(Playlist::id))
    }

    @Test
    fun searchArtistsForPicker_matchesArtistAndAlbumTokens() {
        val artists = listOf(
            "Miles North" to listOf(song(id = 1L, title = "Blue Meridian", artist = "Miles North", album = "Shades of Blue")),
            "Jonas Reed" to listOf(song(id = 2L, title = "Night Train", artist = "Jonas Reed", album = "Night Lines")),
        )

        val result = searchArtistsForPicker(
            artists = artists,
            query = NormalizedSearchQuery.from("miles blue"),
            name = { it.first },
            songs = { it.second },
            songCount = { it.second.size },
        )

        assertEquals(listOf("Miles North"), result.map { it.first })
    }

    @Test
    fun playbackSourceLabel_stopsAtFirstDistinctAlbum() {
        val sameAlbum = listOf(
            song(id = 1L, title = "One", album = "Album"),
            song(id = 2L, title = "Two", album = "Album"),
        )
        val mixedAlbums = sameAlbum + song(id = 3L, title = "Three", album = "Other")

        assertEquals("Album", sameAlbum.playbackSourceLabel("Fallback"))
        assertEquals("Search", mixedAlbums.playbackSourceLabel("Fallback"))
        assertEquals("Fallback", emptyList<Song>().playbackSourceLabel("Fallback"))
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
