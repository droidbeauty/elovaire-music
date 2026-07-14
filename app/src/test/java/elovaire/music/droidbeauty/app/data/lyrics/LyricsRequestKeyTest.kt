package elovaire.music.droidbeauty.app.data.lyrics

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LyricsRequestKeyTest {
    @Test
    fun sourceIdentityIncludesUriAndRevision() {
        val original = song(uri = "content://media/song/1", revision = 10L)
        val moved = song(uri = "content://media/song/2", revision = 10L)
        val edited = song(uri = "content://media/song/1", revision = 11L)

        assertNotEquals(
            original.toLyricsRequestKey(LyricsLookupMode.Full).sourceIdentity,
            moved.toLyricsRequestKey(LyricsLookupMode.Full).sourceIdentity,
        )
        assertNotEquals(
            original.toLyricsRequestKey(LyricsLookupMode.Full).sourceIdentity,
            edited.toLyricsRequestKey(LyricsLookupMode.Full).sourceIdentity,
        )
    }

    @Test
    fun lookupModesShareSourceIdentityButNotRequestKey() {
        val song = song(uri = "content://media/song/1", revision = 10L)
        val full = song.toLyricsRequestKey(LyricsLookupMode.Full)
        val fast = song.toLyricsRequestKey(LyricsLookupMode.FastPresenceCheck)

        assertNotEquals(full, fast)
        assertEquals(full.sourceIdentity, fast.sourceIdentity)
    }

    private fun song(uri: String, revision: Long): Song {
        return Song(
            id = 1L,
            title = "Title",
            isExplicit = false,
            artist = "Artist",
            album = "Album",
            releaseYear = null,
            genre = "",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "track.mp3",
            albumId = 1L,
            durationMs = 180_000L,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = 1L,
            dateModifiedSeconds = revision,
            uri = TestUri(uri),
            artUri = null,
        )
    }
}
