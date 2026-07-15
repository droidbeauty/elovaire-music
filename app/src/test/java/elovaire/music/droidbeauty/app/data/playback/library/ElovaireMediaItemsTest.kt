package elovaire.music.droidbeauty.app.data.playback.library

import android.net.TestUri
import androidx.media3.common.MediaMetadata
import elovaire.music.droidbeauty.app.data.playback.toPlaybackMediaItem
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

class ElovaireMediaItemsTest {
    @Test
    fun parse_acceptsPlaybackMediaItemIdsForControllerInterop() {
        assertEquals(ElovaireMediaId.Song(42L), ElovaireMediaIds.parse("42"))
        assertEquals(ElovaireMediaId.Song(42L), ElovaireMediaIds.parse(ElovaireMediaIds.song(42L)))
    }

    @Test
    fun mediaIdsRoundTripWithoutCrossingDomains() {
        val ids = listOf(
            ElovaireMediaId.Song(42L),
            ElovaireMediaId.Album(42L),
            ElovaireMediaId.Playlist(42L),
            ElovaireMediaId.Root,
            ElovaireMediaId.Favorites,
        )

        ids.forEach { id -> assertEquals(id, ElovaireMediaIds.parse(id.value)) }
    }

    @Test
    fun parse_rejectsMalformedAndUnboundedExternalIds() {
        assertNull(ElovaireMediaIds.parse("0"))
        assertNull(ElovaireMediaIds.parse("elovaire:artist:"))
        assertNull(ElovaireMediaIds.parse("elovaire:bucket:unknown:A"))
        assertNull(ElovaireMediaIds.parse("elovaire:artist:${"x".repeat(1_100)}"))
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

    @Test
    fun playbackMediaItem_usesPolicyMimeForSupportedFormats() {
        val cases = mapOf(
            "song.mp3" to "audio/mpeg",
            "song.flac" to "audio/flac",
            "song.m4a" to "audio/mp4",
            "song.m4b" to "audio/mp4",
            "song.ogg" to "audio/ogg",
            "song.opus" to "audio/opus",
            "song.wav" to "audio/wav",
            "song.aac" to "audio/aac",
        )

        cases.forEach { (fileName, expectedMime) ->
            assertEquals(expectedMime, testSong(fileName = fileName).toPlaybackMediaItem().localConfiguration?.mimeType)
        }
    }

    private fun testSong(fileName: String = "song.mp3"): Song {
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
            fileName = fileName,
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
