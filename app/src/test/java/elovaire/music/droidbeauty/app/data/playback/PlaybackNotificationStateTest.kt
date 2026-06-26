package elovaire.music.droidbeauty.app.data.playback

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class PlaybackNotificationStateTest {
    @Test
    fun notificationRenderStateOf_usesOnlyNotificationRelevantFields() {
        val artUri = TestUri("content://art/1")
        val song = testSong(
            title = "Title",
            artist = "Artist",
            album = "Album",
            artUri = artUri,
        )
        val state = notificationRenderStateOf(song, isPlaying = true)

        assertEquals(1L, state.songId)
        assertEquals("Title", state.title)
        assertEquals("Artist", state.artist)
        assertEquals("Album", state.album)
        assertSame(artUri, state.artUri)
        assertEquals(true, state.isPlaying)
    }

    @Test
    fun notificationRenderStateOf_handlesMissingSong() {
        val state = notificationRenderStateOf(song = null, isPlaying = false)

        assertNull(state.songId)
        assertNull(state.artUri)
        assertEquals(false, state.isPlaying)
    }

    private fun testSong(
        title: String,
        artist: String,
        album: String,
        artUri: TestUri,
    ): Song {
        return Song(
            id = 1L,
            title = title,
            isExplicit = false,
            artist = artist,
            album = album,
            releaseYear = null,
            genre = "",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "song.mp3",
            albumId = 2L,
            durationMs = 1_000L,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = 0L,
            uri = TestUri("content://song/1"),
            artUri = artUri,
        )
    }
}
