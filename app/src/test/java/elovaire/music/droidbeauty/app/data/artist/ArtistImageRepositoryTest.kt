package elovaire.music.droidbeauty.app.data.artist

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class ArtistImageRepositoryTest {
    private val repository = ArtistImageRepository()

    @Test
    fun backdropUsesBestLocalAlbumArtwork() = runBlocking {
        val art = TestUri("content://art/album")
        val state = repository.backdropState(
            artistName = "Artist",
            songs = listOf(song(artUri = TestUri("content://art/song"))),
            albums = listOf(album(artUri = art, songCount = 2)),
        ).first()

        assertSame(art, (state as ArtistBackdropState.Fallback).localArtworkUri)
        assertEquals("artist", state.artistKey)
    }

    @Test
    fun backdropFallsBackToLocalSongArtwork() = runBlocking {
        val art = TestUri("content://art/song")
        val state = repository.backdropState("Artist", listOf(song(artUri = art)), emptyList()).first()

        assertSame(art, (state as ArtistBackdropState.Fallback).localArtworkUri)
        assertEquals("artist", state.artistKey)
    }

    @Test
    fun backdropWithoutArtworkRemainsLocalFallback() = runBlocking {
        val state = repository.backdropState(" Artist ", listOf(song(artUri = null)), emptyList()).first()

        assertEquals("artist", (state as ArtistBackdropState.Fallback).artistKey)
        assertNull(state.localArtworkUri)
    }

    private fun album(artUri: android.net.Uri?, songCount: Int) = Album(
        id = 1L,
        title = "Album",
        artist = "Artist",
        artUri = artUri,
        songCount = songCount,
        durationMs = 1_000L,
        songs = emptyList(),
    )

    private fun song(artUri: android.net.Uri?) = Song(
        id = 1L,
        title = "Track",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "Genre",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "track.mp3",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        uri = TestUri("content://media/1"),
        artUri = artUri,
    )
}
