package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class LibrarySnapshotAssemblerTest {
    @Test
    fun assemble_removesDuplicateBeforeBuildingAlbumAggregates() {
        val mediaStoreSong = song(1L, "content://media/external/audio/media/1")
        val safDuplicate = song(-1L, "content://documents/document/track")

        val snapshot = LibrarySnapshotAssembler.assemble(listOf(mediaStoreSong, safDuplicate))

        assertEquals(listOf(1L), snapshot.songs.map(Song::id))
        assertEquals(1, snapshot.albums.single().songCount)
        assertEquals(1_000L, snapshot.albums.single().durationMs)
    }

    private fun song(id: Long, uri: String) = Song(
        id = id,
        title = "Track",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "Genre",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "track.mp3",
        albumId = 2L,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        dateModifiedSeconds = 1L,
        libraryPath = "/music/track.mp3",
        uri = TestUri(uri),
        artUri = null,
    )
}
