package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
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

    @Test
    fun assemble_separatesSameMediaStoreAlbumIdAcrossVolumes() {
        val primary = song(1L, "content://media/external_primary/audio/media/1")
            .copy(albumId = 42L, libraryPath = "/storage/emulated/0/Music/primary.mp3")
        val removable = song(2L, "content://media/1234-5678/audio/media/2")
            .copy(
                albumId = 42L,
                fileName = "removable.mp3",
                libraryPath = "/storage/1234-5678/Music/removable.mp3",
            )

        val snapshot = LibrarySnapshotAssembler.assemble(listOf(primary, removable))

        assertEquals(2, snapshot.albums.size)
        assertEquals(2, snapshot.songs.map(Song::albumId).distinct().size)
        assertEquals(42L, snapshot.songs.first { it.id == primary.id }.albumId)
    }

    @Test
    fun assemble_separatesSameAlbumIdAcrossIndependentProviders() {
        val first = song(1L, "content://provider.one/tree/root/document/track-one")
            .copy(id = -1L, albumId = -9L, libraryPath = null)
        val second = song(2L, "content://provider.two/tree/root/document/track-two")
            .copy(id = -2L, albumId = -9L, fileName = "second.mp3", libraryPath = null)

        val snapshot = LibrarySnapshotAssembler.assemble(listOf(first, second))

        assertEquals(2, snapshot.albums.size)
        assertEquals(2, snapshot.songs.map(Song::albumId).distinct().size)
    }

    @Test
    fun patchSongs_rebuildsOnlyAffectedAlbum() {
        val first = song(1L, "content://media/external/audio/media/1")
        val second = song(2L, "content://media/external/audio/media/2")
            .copy(albumId = 3L, album = "Other", fileName = "other.mp3", libraryPath = "/music/other.mp3")
        val initial = LibrarySnapshotAssembler.assemble(listOf(first, second))
        val current = LibraryContentState(initial.songs, initial.albums)
        var published: LibraryContentState? = null
        val publisher = LibrarySnapshotPublisher({ published = it }, { current })

        val next = publisher.patchSongs(listOf(first.copy(title = "Edited")), emptySet(), emptySet())

        assertEquals("Edited", next.songs.first { it.id == first.id }.title)
        assertSame(
            initial.albums.first { it.id == second.albumId },
            next.albums.first { it.id == second.albumId },
        )
        assertEquals(next, published)
    }

    @Test
    fun patchSongs_movingSongRetainsSongsAlreadyInDestinationAlbum() {
        val moved = song(1L, "content://media/external/audio/media/1")
        val destination = song(2L, "content://media/external/audio/media/2")
            .copy(
                albumId = 3L,
                album = "Destination",
                fileName = "destination.mp3",
                libraryPath = "/music/destination.mp3",
            )
        val initial = LibrarySnapshotAssembler.assemble(listOf(moved, destination))
        val current = LibraryContentState(initial.songs, initial.albums)
        val publisher = LibrarySnapshotPublisher({}, { current })

        val next = publisher.patchSongs(
            editedSongs = listOf(moved.copy(albumId = 3L, album = "Destination")),
            removingSongIds = emptySet(),
            removingAlbumIds = emptySet(),
        )
        assertEquals(listOf(1L, 2L), next.albums.single { it.id == 3L }.songs.map(Song::id).sorted())
        assertEquals(null, next.albums.firstOrNull { it.id == moved.albumId })
    }

    @Test
    fun stateForSnapshotRestoresSemanticRevisionForCachedSnapshots() {
        val song = song(1L, "content://media/external/audio/media/1")
        val snapshot = LibrarySnapshotAssembler.assemble(listOf(song)).copy(contentRevision = "")
        val publisher = LibrarySnapshotPublisher({}, { LibraryContentState() })

        val state = publisher.stateForSnapshot(snapshot, emptySet(), emptySet())

        assertEquals(librarySongsContentRevision(listOf(song)), state.contentRevision)
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
