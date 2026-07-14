package elovaire.music.droidbeauty.app.data.library.db

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibraryDatabaseMapperTest {
    @Test
    fun indexedSnapshotPreservesSongAndAlbumIdentity() {
        val song = song(id = 42L, albumId = 7L)
        val album = Album(
            id = 7L,
            title = "Album",
            artist = "Artist",
            artUri = TestUri("content://art/7"),
            songCount = 1,
            durationMs = 123_000L,
            songs = listOf(song),
        )

        val indexed = LibraryDatabaseMapper.indexedSnapshot(
            snapshot = LibrarySnapshot(songs = listOf(song), albums = listOf(album)),
            generationId = 99L,
            scannedAtMs = 100L,
        )

        assertEquals(42L, indexed.songs.single().songId)
        assertEquals(7L, indexed.albums.single().albumId)
        assertEquals("content://song/42", indexed.mediaFiles.single().uri)
        assertEquals(99L, indexed.songs.single().lastSeenGenerationId)
    }

    @Test
    fun mediaFileFallsBackToUriWhenPathIsMissing() {
        val entity = LibraryDatabaseMapper.mediaFileEntity(
            song = song(id = 5L, albumId = 9L, libraryPath = null),
            generationId = 1L,
            scannedAtMs = 2L,
        )

        assertEquals("uri:content://song/5", entity.stableFileKey)
        assertNull(entity.filePath)
    }

    private fun song(
        id: Long,
        albumId: Long,
        libraryPath: String? = "/storage/emulated/0/Music/song.mp3",
    ) = Song(
        id = id,
        title = "Track",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = 2025,
        genre = "Pop",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "song.mp3",
        albumId = albumId,
        durationMs = 123_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 10L,
        dateModifiedSeconds = 11L,
        libraryPath = libraryPath,
        uri = TestUri("content://song/$id"),
        artUri = TestUri("content://art/$albumId"),
        metadataResolved = true,
        albumArtist = "Artist",
        volumeNormalization = null,
    )
}
