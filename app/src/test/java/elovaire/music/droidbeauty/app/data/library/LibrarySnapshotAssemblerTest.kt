package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySnapshotAssemblerTest {
    @Test
    fun libraryUiStateEqualityIncludesEveryFieldAndFloatSemantics() {
        val track = song(1L, "content://media/external/audio/media/1")
        val snapshot = LibrarySnapshotAssembler.assemble(listOf(track))
        val books = AudiobookCatalog.build(listOf(track.copy(mediaKind = elovaire.music.droidbeauty.app.domain.model.AudioMediaKind.Audiobook)))
        assertTrue(books.isNotEmpty())
        val state = LibraryUiState(songs = snapshot.songs, albums = snapshot.albums, contentRevision = snapshot.contentRevision)
        val equal = state.copy(songs = state.songs.toList(), albums = state.albums.toList())
        assertEquals(state, equal)
        assertEquals(state.hashCode(), equal.hashCode())
        listOf(
            state.copy(permissionGranted = true), state.copy(isLoading = true), state.copy(scanProgress = 0.5f),
            state.copy(isAuthoritative = true), state.copy(songs = emptyList()), state.copy(albums = emptyList()),
            state.copy(audiobooks = books), state.copy(contentRevision = "different"),
            state.copy(removingSongIds = setOf(1L)), state.copy(removingAlbumIds = setOf(1L)),
            state.copy(errorMessage = "error"),
        ).forEach { assertNotEquals(state, it) }
        assertNotEquals(state.copy(scanProgress = 0f), state.copy(scanProgress = -0f))
        assertEquals(state.copy(scanProgress = Float.NaN), state.copy(scanProgress = Float.NaN))
    }

    @Test
    fun libraryUiStateChecksRevisionAndMarkersBeforeSongs() {
        fun unvisitedSongs() = object : AbstractList<Song>() {
            override val size: Int get() = error("Song comparison is unnecessary")
            override fun get(index: Int): Song = error("Song comparison is unnecessary")
        }
        val current = LibraryUiState(songs = unvisitedSongs(), contentRevision = "before")
        assertNotEquals(current, LibraryUiState(songs = unvisitedSongs(), contentRevision = "after"))
        assertNotEquals(current, LibraryUiState(songs = unvisitedSongs(), contentRevision = "before", removingSongIds = setOf(1L)))
    }

    @Test
    fun deletionFailuresKeepRequestOrderAndFirstSongForDuplicateIds() {
        val first = song(1L, "content://media/external/audio/media/1").copy(albumId = 7L)
        val duplicate = first.copy(albumId = 8L)
        val second = song(2L, "content://media/external/audio/media/2").copy(albumId = 9L)
        val failures = deletionFailures(listOf(first, duplicate, second), linkedSetOf(2L, 99L, 1L))

        assertEquals(listOf(2L, 99L, 1L), failures.map { it.songId })
        assertEquals(listOf(9L, null, 7L), failures.map { it.albumId })
        assertEquals(7L, deletionFailures(listOf(first, duplicate), setOf(1L)).single().albumId)
        assertTrue(deletionFailures(listOf(first), emptySet()).isEmpty())
    }

    @Test
    fun fullyDeletedAlbumsPreserveRequestOrderAndFirstMatchingAlbum() {
        val first = song(1L, "content://media/external/audio/media/1")
        val second = song(2L, "content://media/external/audio/media/2")
        val album = buildAlbumsFromSongs(listOf(first, second)).single()
        val other = album.copy(id = 3L, songs = listOf(second))
        val duplicate = album.copy(songs = emptyList())
        val requested = linkedSetOf(3L, album.id, 999L)

        assertEquals(listOf(3L, album.id), fullyDeletedAlbumIds(listOf(album, other), requested, setOf(1L, 2L)).toList())
        assertEquals(listOf(3L), fullyDeletedAlbumIds(listOf(album, duplicate, other), requested, setOf(2L)).toList())
        assertEquals(setOf(album.id), fullyDeletedAlbumIds(listOf(album), setOf(album.id), setOf(1L, 2L)))
        assertTrue(fullyDeletedAlbumIds(listOf(album), setOf(album.id), setOf(1L)).isEmpty())
        assertTrue(fullyDeletedAlbumIds(listOf(album), emptySet(), setOf(1L, 2L)).isEmpty())
        assertEquals(listOf(3L, 999L), missingAlbumIds(requested, listOf(album)).toList())
        assertEquals(requested.toList(), missingAlbumIds(requested, emptyList()).toList())
        assertTrue(missingAlbumIds(setOf(album.id), listOf(album)).isEmpty())
        assertEquals(setOf(999L), missingAlbumIds(setOf(999L), listOf(album)))
    }

    @Test
    fun libraryContentStateEqualityChecksMarkersBeforeTraversingSongs() {
        fun unvisitedSongs() = object : AbstractList<Song>() {
            override val size: Int get() = error("Song comparison is unnecessary")
            override fun get(index: Int): Song = error("Song comparison is unnecessary")
        }
        val current = LibraryContentState(songs = unvisitedSongs(), contentRevision = "same")
        val next = LibraryContentState(songs = unvisitedSongs(), contentRevision = "same", removingSongIds = setOf(1L))

        assertNotEquals(current, next)
    }

    @Test
    fun unchangedSnapshotReusesPortableIdentityRevision() {
        val original = song(1L, "content://media/external/audio/media/1")
        val snapshot = LibrarySnapshotAssembler.assemble(listOf(original))
        val revision = MediaIdentityResolver.portableIdentityRevision(snapshot.songs)
        val current = LibraryContentState(
            songs = snapshot.songs,
            albums = snapshot.albums,
            contentRevision = snapshot.contentRevision,
            portableMediaIdentityRevision = revision,
        )
        val publisher = LibrarySnapshotPublisher({}, { current })
        val unvisitedSongs = object : AbstractList<Song>() {
            override val size: Int get() = error("Unchanged identity should not be recalculated")
            override fun get(index: Int): Song = error("Unchanged identity should not be recalculated")
        }
        val unchanged = publisher.stateForSnapshot(snapshot.copy(songs = unvisitedSongs), emptySet(), emptySet())
        assertEquals(revision, unchanged.portableMediaIdentityRevision)

        val changed = LibrarySnapshotAssembler.assemble(listOf(original.copy(title = "Changed")))
        val changedState = publisher.stateForSnapshot(changed, emptySet(), emptySet())
        assertEquals(MediaIdentityResolver.portableIdentityRevision(changed.songs), changedState.portableMediaIdentityRevision)
        assertNotEquals(revision, changedState.portableMediaIdentityRevision)
    }

    @Test
    fun libraryContentStateEqualityIncludesAllFields() {
        val first = song(1L, "content://media/external/audio/media/1")
        val snapshot = LibrarySnapshotAssembler.assemble(listOf(first))
        val state = LibraryContentState(snapshot.songs, snapshot.albums, contentRevision = snapshot.contentRevision)
        val equal = state.copy(songs = state.songs.toList(), albums = state.albums.toList())
        val books = AudiobookCatalog.build(listOf(first.copy(mediaKind = elovaire.music.droidbeauty.app.domain.model.AudioMediaKind.Audiobook)))
        assertTrue(books.isNotEmpty())
        assertEquals(state, equal)
        assertEquals(state.hashCode(), equal.hashCode())
        listOf(
            state.copy(songs = emptyList()),
            state.copy(albums = emptyList()),
            state.copy(audiobooks = books),
            state.copy(removingSongIds = setOf(1L)),
            state.copy(removingAlbumIds = setOf(1L)),
            state.copy(contentRevision = "different"),
            state.copy(portableMediaIdentityRevision = "different"),
        ).forEach { assertNotEquals(state, it) }
    }

    @Test
    fun publishStateSkipsSongComparisonWhenRevisionsDiffer() {
        fun unvisitedSongs() = object : AbstractList<Song>() {
            override val size: Int get() = error("Song comparison is unnecessary")
            override fun get(index: Int): Song = error("Song comparison is unnecessary")
        }
        val current = LibraryContentState(songs = unvisitedSongs(), contentRevision = "before")
        val next = LibraryContentState(songs = unvisitedSongs(), contentRevision = "after")
        var published: LibraryContentState? = null
        LibrarySnapshotPublisher({ published = it }, { current }).publishState(next)
        assertSame(next, published)
    }

    @Test
    fun unchangedMetadataPatchPreservesContentAndStillPublishesRemovalMarkers() {
        val original = song(1L, "content://media/external/audio/media/1")
        val snapshot = LibrarySnapshotAssembler.assemble(listOf(original))
        val current = LibraryContentState(snapshot.songs, snapshot.albums, contentRevision = snapshot.contentRevision)
        var published: LibraryContentState? = null
        val publisher = LibrarySnapshotPublisher({ published = it }, { current })

        val unchanged = publisher.patchSongs(listOf(original.copy()), emptySet(), emptySet())
        assertSame(current.songs, unchanged.state.songs)
        assertSame(current.albums, unchanged.state.albums)
        assertEquals(current.contentRevision, unchanged.state.contentRevision)
        assertEquals(LibraryChangeSet.Empty, unchanged.changeSet)
        assertEquals(null, published)

        val marked = publisher.patchSongs(listOf(original), setOf(original.id), emptySet())
        assertEquals(setOf(original.id), published?.removingSongIds)
        assertSame(current.songs, marked.state.songs)

        published = null
        publisher.patchSongs(listOf(original), setOf(original.id), emptySet(), publishResult = false)
        assertEquals(null, published)
    }
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
    fun assembleDeduplicatedSongsSkipsTheAlreadyCompletedDuplicatePass() {
        val songs = listOf(
            song(1L, "content://media/external/audio/media/1"),
            song(2L, "content://media/external/audio/media/2").copy(
                fileName = "second.mp3",
                libraryPath = "/music/second.mp3",
            ),
        )
        val prepared = LibrarySnapshotAssembler.assembleDeduplicatedSongs(songs)

        assertSame(songs, prepared.songs)
        assertEquals(LibrarySnapshotAssembler.assemble(songs), prepared)
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
    fun albumCollisionLeavesOtherGroupsUnchanged() {
        val primary = song(1L, "content://media/external_primary/audio/media/1").copy(albumId = 42L)
        val removable = song(2L, "content://media/1234-5678/audio/media/2").copy(albumId = 42L)
        val unrelated = song(3L, "content://media/external_primary/audio/media/3").copy(albumId = 100L)

        val result = LibrarySnapshotAssembler.canonicalizeAlbumIds(listOf(removable, unrelated, primary))

        assertSame(unrelated, result[1])
        assertEquals(42L, result[2].albumId)
        assertEquals(3, result.map(Song::albumId).distinct().size)
        assertEquals(result, LibrarySnapshotAssembler.canonicalizeAlbumIds(listOf(removable, unrelated, primary)))
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

        val patch = publisher.patchSongs(listOf(first.copy(title = "Edited")), emptySet(), emptySet())
        val next = patch.state

        assertEquals("Edited", next.songs.first { it.id == first.id }.title)
        assertEquals(false, patch.changeSet.isEmpty)
        assertSame(
            initial.albums.first { it.id == second.albumId },
            next.albums.first { it.id == second.albumId },
        )
        assertEquals(next, published)
    }

    @Test
    fun patchSongs_fallsBackToStableIdentityOnlyWhenIdIsMissing() {
        val original = song(1L, "content://media/external/audio/media/1")
        val initial = LibrarySnapshotAssembler.assemble(listOf(original))
        val current = LibraryContentState(initial.songs, initial.albums, contentRevision = initial.contentRevision)
        val publisher = LibrarySnapshotPublisher({}, { current })

        val result = publisher.patchSongs(
            editedSongs = listOf(original.copy(id = 99L, title = "Edited")),
            removingSongIds = emptySet(),
            removingAlbumIds = emptySet(),
        )

        assertEquals("Edited", result.state.songs.single().title)
        assertEquals(99L, result.state.songs.single().id)
    }

    @Test
    fun patchSongsUsesVolumeScopedIdentityWhenMediaStoreIdsCollide() {
        val primary = song(1L, "content://media/external_primary/audio/media/1")
        val removable = song(1L, "content://media/1234-5678/audio/media/1")
            .copy(fileName = "removable.mp3", libraryPath = "/storage/1234-5678/Music/removable.mp3")
        val initial = LibrarySnapshotAssembler.assemble(listOf(primary, removable))
        val publisher = LibrarySnapshotPublisher({}, { LibraryContentState(initial.songs, initial.albums) })

        val result = publisher.patchSongs(
            editedSongs = listOf(removable.copy(title = "Edited")),
            removingSongIds = emptySet(),
            removingAlbumIds = emptySet(),
        )

        assertEquals("Track", result.state.songs.first { it.uri == primary.uri }.title)
        assertEquals("Edited", result.state.songs.first { it.uri == removable.uri }.title)
    }

    @Test
    fun patchSongs_reusesAudiobookCatalogForMusicOnlyEdits() {
        val music = song(1L, "content://media/external/audio/media/1")
        val audiobook = song(2L, "file:///books/book/part-1.m4b").copy(
            mediaKind = elovaire.music.droidbeauty.app.domain.model.AudioMediaKind.Audiobook,
            album = "Book",
            fileName = "part-1.m4b",
            albumId = 0L,
            libraryPath = "/books/book/part-1.m4b",
        )
        val initial = LibrarySnapshotAssembler.assemble(listOf(music, audiobook))
        val current = LibraryContentState(
            songs = initial.songs,
            albums = initial.albums,
            audiobooks = initial.audiobooks,
            contentRevision = initial.contentRevision,
        )
        val publisher = LibrarySnapshotPublisher({}, { current })

        val patch = publisher.patchSongs(
            editedSongs = listOf(music.copy(title = "Edited")),
            removingSongIds = emptySet(),
            removingAlbumIds = emptySet(),
        )

        assertSame(initial.audiobooks, patch.state.audiobooks)
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

        val patch = publisher.patchSongs(
            editedSongs = listOf(moved.copy(albumId = 3L, album = "Destination")),
            removingSongIds = emptySet(),
            removingAlbumIds = emptySet(),
        )
        assertEquals(listOf(1L, 2L), patch.state.albums.single { it.id == 3L }.songs.map(Song::id).sorted())
        assertEquals(null, patch.state.albums.firstOrNull { it.id == moved.albumId })
    }

    @Test
    fun patchSongsRebuildsAlbumsWhenCanonicalizationMovesUneditedSongs() {
        val removable = song(2L, "content://media/1234-5678/audio/media/2").copy(
            albumId = 42L,
            libraryPath = "/storage/1234-5678/Music/removable.mp3",
        )
        val primary = song(1L, "content://media/external_primary/audio/media/1").copy(
            albumId = 100L,
            fileName = "primary.mp3",
            libraryPath = "/storage/emulated/0/Music/primary.mp3",
        )
        val unrelated = song(3L, "content://media/external_primary/audio/media/3").copy(
            albumId = 300L,
            fileName = "unrelated.mp3",
            libraryPath = "/storage/emulated/0/Music/unrelated.mp3",
        )
        val initial = LibrarySnapshotAssembler.assemble(listOf(removable, primary, unrelated))
        val current = LibraryContentState(initial.songs, initial.albums, contentRevision = initial.contentRevision)
        val publisher = LibrarySnapshotPublisher({}, { current })

        val patch = publisher.patchSongs(listOf(primary.copy(albumId = 42L)), emptySet(), emptySet())
        val expected = LibrarySnapshotAssembler.assembleDeduplicatedSongs(
            listOf(removable, primary.copy(albumId = 42L), unrelated),
        )

        assertEquals(expected.songs, patch.state.songs)
        assertEquals(expected.albums.associateBy { it.id }, patch.state.albums.associateBy { it.id })
        assertEquals(setOf(1L, 2L), patch.changeSet.updated.map { it.after.id }.toSet())
        assertTrue(expected.songs.first { it.id == 2L }.albumId in patch.changeSet.affectedAlbumIds)
        assertSame(initial.albums.first { it.id == 300L }, patch.state.albums.first { it.id == 300L })
        val changes = initial.songs.zip(expected.songs)
            .filter { (before, after) -> before != after }
            .map { (before, after) -> LibrarySongPatch(before, after) }
        assertEquals(libraryPatchedContentRevision(initial.contentRevision, changes), patch.state.contentRevision)
    }

    @Test
    fun patchSongs_skipsGlobalAlbumCanonicalizationWhenGroupingInputsDoNotChange() {
        val first = song(1L, "content://media/external/audio/media/1")
        val second = song(2L, "content://media/external/audio/media/2")
        val currentSnapshot = LibrarySnapshotAssembler.assemble(listOf(first, second))
        val updated = currentSnapshot.songs.map { it.copy(title = "Edited ${it.id}") }

        assertSame(
            updated,
            LibrarySnapshotAssembler.canonicalizeAlbumIdsAfterPatch(
                previousSongs = currentSnapshot.songs,
                updatedSongs = updated,
                changedPositions = updated.indices.toList(),
            ),
        )
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
