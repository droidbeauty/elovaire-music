package elovaire.music.droidbeauty.app.data.playback.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaTreeSnapshotCacheTest {
    @Test
    fun unchangedStateFlowValuesReuseDerivedSnapshot() {
        val songs = emptyList<Song>()
        val albums = emptyList<Album>()
        val playlists = emptyList<Playlist>()
        val favorites = emptyList<Long>()
        val recent = emptyList<Long>()
        val cache = MediaTreeSnapshotCache()
        val first = cache.snapshot(
            permissionGranted = true,
            songs = songs,
            albums = albums,
            playlists = playlists,
            favoriteSongIds = favorites,
            recentSongIds = recent,
            lastPlayedCollectionKind = null,
            lastPlayedCollectionId = null,
        )
        val second = cache.snapshot(
            true, songs, albums, playlists, favorites, recent, null, null,
        )
        val changed = cache.snapshot(
            true, songs, albums, playlists, listOf(1L), recent, null, null,
        )

        assertSame(first, second)
        assertNotSame(second, changed)
    }

    @Test
    fun semanticLibraryRevisionReusesSnapshotAcrossEquivalentListInstances() {
        val songs = listOf(testSong(id = 1L))
        val albums = emptyList<Album>()
        val playlists = emptyList<Playlist>()
        val favorites = emptyList<Long>()
        val recent = emptyList<Long>()
        val cache = MediaTreeSnapshotCache()
        val first = cache.snapshot(
            true, songs, albums, playlists, favorites, recent, null, null, "library-1",
        )
        val second = cache.snapshot(
            true, songs.toList(), albums.toList(), playlists, favorites, recent, null, null, "library-1",
        )

        assertSame(first, second)
    }

    @Test
    fun clearDropsDerivedSnapshot() {
        val cache = MediaTreeSnapshotCache()
        val first = cache.snapshot(true, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), null, null)

        cache.clear()

        val second = cache.snapshot(true, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), null, null)
        assertNotSame(first, second)
    }

    @Test
    fun blankGenreUsesSameUnknownGenreIdentityForGroupingAndLookup() {
        val song = testSong(id = 1L, genre = "")
        val snapshot = MediaTreeSnapshotCache().snapshot(
            true, listOf(song), emptyList(), emptyList(), emptyList(), emptyList(), null, null,
        )

        assertEquals(listOf("Unknown Genre"), snapshot.genreNames())
        assertEquals(listOf(song), snapshot.songsForGenre("Unknown Genre"))
    }

    @Test
    fun equalTitlesUseStableSongIdTieBreakers() {
        val snapshot = MediaTreeSnapshotCache().snapshot(
            true,
            listOf(testSong(id = 2L), testSong(id = 1L)),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            null,
            null,
        )

        assertEquals(listOf(1L, 2L), snapshot.songsByTitle().map(Song::id))
    }

    @Test
    fun contextSongsAreSortedOnceAndReusedForArtistAndGenreBrowses() {
        val songs = listOf(
            testSong(id = 3L).copy(album = "B", trackNumber = 1),
            testSong(id = 1L).copy(album = "A", trackNumber = 2),
            testSong(id = 2L).copy(album = "A", trackNumber = 1),
        )
        val snapshot = MediaTreeSnapshotCache().snapshot(
            true,
            songs,
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            null,
            null,
        )

        val artistSongs = snapshot.songsForArtistInContext("Artist")
        val genreSongs = snapshot.songsForGenreInContext("Genre")

        assertEquals(listOf(2L, 1L, 3L), artistSongs.map(Song::id))
        assertEquals(listOf(2L, 1L, 3L), genreSongs.map(Song::id))
        assertSame(artistSongs, snapshot.songsForArtistInContext("Artist"))
        assertSame(genreSongs, snapshot.songsForGenreInContext("Genre"))
    }

    private fun testSong(
        id: Long,
        genre: String = "Genre",
    ) = Song(
        id = id,
        title = "Song",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = genre,
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "song.mp3",
        albumId = 2L,
        durationMs = 1L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        uri = TestUri("content://song/$id"),
        artUri = null,
    )
}
