package elovaire.music.droidbeauty.app.ui.screens

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class RootStateBindingsTest {
    @Test
    fun buildRootSongIndexes_preservesSourceOrderInBothIndexes() {
        val first = song(1L, 42L)
        val second = song(2L, 42L)
        val third = song(3L, 7L)

        val indexes = buildRootSongIndexes(listOf(second, first, third))

        assertEquals(listOf(2L, 1L, 3L), indexes.songsById.keys.toList())
        assertEquals(listOf(2L, 1L), indexes.songsByAlbumId.getValue(42L).map(Song::id))
        assertSame(second, indexes.songsByAlbumId.getValue(42L).first())
    }

    private fun song(id: Long, albumId: Long) = Song(
        id = id,
        title = "Song $id",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "$id.mp3",
        albumId = albumId,
        durationMs = 1L,
        trackNumber = id.toInt(),
        discNumber = 1,
        dateAddedSeconds = id,
        uri = TestUri("content://root/$id"),
        artUri = null,
    )
}
