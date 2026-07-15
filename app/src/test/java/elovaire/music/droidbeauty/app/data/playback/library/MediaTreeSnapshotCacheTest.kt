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
    fun clearDropsDerivedSnapshot() {
        val cache = MediaTreeSnapshotCache()
        val first = cache.snapshot(true, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), null, null)

        cache.clear()

        val second = cache.snapshot(true, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), null, null)
        assertNotSame(first, second)
    }

    @Test
    fun blankGenreUsesSameUnknownGenreIdentityForGroupingAndLookup() {
        val song = Song(
            id = 1L,
            title = "Song",
            isExplicit = false,
            artist = "Artist",
            album = "Album",
            releaseYear = null,
            genre = "",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "song.mp3",
            albumId = 2L,
            durationMs = 1L,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = 1L,
            uri = TestUri("content://song/1"),
            artUri = null,
        )
        val snapshot = MediaTreeSnapshotCache().snapshot(
            true, listOf(song), emptyList(), emptyList(), emptyList(), emptyList(), null, null,
        )

        assertEquals(listOf("Unknown Genre"), snapshot.genreNames())
        assertEquals(listOf(song), snapshot.songsForGenre("Unknown Genre"))
    }
}
