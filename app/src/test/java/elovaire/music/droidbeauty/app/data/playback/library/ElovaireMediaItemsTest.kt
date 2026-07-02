package elovaire.music.droidbeauty.app.data.playback.library

import android.net.TestUri
import androidx.media3.common.MediaMetadata
import elovaire.music.droidbeauty.app.data.playback.toPlaybackMediaItem
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ElovaireMediaItemsTest {
    @Test
    fun parse_acceptsPlaybackMediaItemIdsForControllerInterop() {
        assertEquals(ElovaireMediaId.Song(42L), ElovaireMediaIds.parse("42"))
        assertEquals(ElovaireMediaId.Song(42L), ElovaireMediaIds.parse(ElovaireMediaIds.song(42L)))
    }

    @Test
    fun playbackMediaItem_containsSystemFacingMusicMetadata() {
        val mediaItem = testSong().toPlaybackMediaItem()
        val metadata = mediaItem.mediaMetadata

        assertEquals("Title", metadata.title)
        assertEquals("Artist", metadata.artist)
        assertEquals("Album", metadata.albumTitle)
        assertEquals("Album Artist", metadata.albumArtist)
        assertEquals("Rock", metadata.genre)
        assertEquals(1999, metadata.releaseYear)
        assertEquals(7, metadata.trackNumber)
        assertEquals(2, metadata.discNumber)
        assertEquals(123_000L, metadata.durationMs)
        assertEquals(MediaMetadata.MEDIA_TYPE_MUSIC, metadata.mediaType)
        assertTrue(metadata.isPlayable == true)
        assertFalse(metadata.isBrowsable == true)
    }

    private fun testSong(): Song {
        return Song(
            id = 42L,
            title = "Title",
            isExplicit = false,
            artist = "Artist",
            album = "Album",
            releaseYear = 1999,
            genre = "Rock",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "song.mp3",
            albumId = 24L,
            durationMs = 123_000L,
            trackNumber = 7,
            discNumber = 2,
            dateAddedSeconds = 0L,
            uri = TestUri("content://song/42"),
            artUri = TestUri("content://art/24"),
            albumArtist = "Album Artist",
        )
    }
}
