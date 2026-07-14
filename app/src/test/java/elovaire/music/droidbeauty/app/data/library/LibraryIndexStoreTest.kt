package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.core.FakeAppClock
import elovaire.music.droidbeauty.app.data.library.db.AlbumEntity
import elovaire.music.droidbeauty.app.data.library.db.LibraryDao
import elovaire.music.droidbeauty.app.data.library.db.LibraryIndexStore
import elovaire.music.droidbeauty.app.data.library.db.LibraryMutationEntity
import elovaire.music.droidbeauty.app.data.library.db.LibraryScanGenerationEntity
import elovaire.music.droidbeauty.app.data.library.db.MediaFileEntity
import elovaire.music.droidbeauty.app.data.library.db.SongEntity
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryIndexStoreTest {
    @Test
    fun indexSnapshot_skipsIdenticalCompletedGeneration() = runBlocking {
        val dao = RecordingLibraryDao()
        val store = LibraryIndexStore(dao, clock = FakeAppClock(wallTime = 42L))
        val snapshot = LibrarySnapshot(emptyList(), emptyList())

        store.indexSnapshot(snapshot, filterFingerprint = "folders", source = "MediaStore")
        store.indexSnapshot(snapshot, filterFingerprint = "folders", source = "MediaStore")

        assertEquals(1, dao.replaceCount)
    }

    @Test
    fun indexSnapshot_replacesGenerationWhenFilterChanges() = runBlocking {
        val dao = RecordingLibraryDao()
        val store = LibraryIndexStore(dao, clock = FakeAppClock(wallTime = 42L))
        val snapshot = LibrarySnapshot(emptyList(), emptyList())

        store.indexSnapshot(snapshot, filterFingerprint = "music", source = "MediaStore")
        store.indexSnapshot(snapshot, filterFingerprint = "downloads", source = "MediaStore")

        assertEquals(2, dao.replaceCount)
    }

    @Test
    fun applyChangedSongsOnlyUpsertsAffectedRows() = runBlocking {
        val dao = RecordingLibraryDao()
        val store = LibraryIndexStore(dao, clock = FakeAppClock(wallTime = 50L))
        val song = song(1L, 2L)
        val album = Album(2L, "Album", "Artist", null, 1, 1_000L, listOf(song))

        store.applyChangedSongs(listOf(song), listOf(album))

        assertEquals(listOf(1L), dao.changedSongIds)
        assertEquals(listOf(2L), dao.changedAlbumIds)
    }

    @Test
    fun markRemovedOnlyTouchesRequestedRows() = runBlocking {
        val dao = RecordingLibraryDao()
        val store = LibraryIndexStore(dao, clock = FakeAppClock(wallTime = 50L))

        store.markRemoved(setOf(1L), setOf(2L))

        assertEquals(setOf(1L), dao.removedSongIds)
        assertEquals(setOf(2L), dao.removedAlbumIds)
    }

    private fun song(id: Long, albumId: Long) = Song(
        id = id,
        title = "Song",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = 2026,
        genre = "Genre",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "song.mp3",
        albumId = albumId,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        uri = TestUri("content://media/$id"),
        artUri = null,
    )
}

private class RecordingLibraryDao : LibraryDao {
    var replaceCount = 0
    var changedSongIds = emptyList<Long>()
    var changedAlbumIds = emptyList<Long>()
    var removedSongIds = emptySet<Long>()
    var removedAlbumIds = emptySet<Long>()

    override suspend fun recoverableMutations(): List<LibraryMutationEntity> = emptyList()
    override suspend fun mutation(mutationId: String): LibraryMutationEntity? = null
    override suspend fun songById(songId: Long): SongEntity? = null
    override suspend fun songByUri(uri: String): SongEntity? = null
    override suspend fun mediaFileByStableKey(stableFileKey: String): MediaFileEntity? = null
    override suspend fun activeSongsForAlbum(albumId: Long): List<SongEntity> = emptyList()
    override suspend fun insertScanGeneration(generation: LibraryScanGenerationEntity) = Unit
    override suspend fun upsertSongs(songs: List<SongEntity>) {
        changedSongIds = songs.map(SongEntity::songId)
    }
    override suspend fun upsertAlbums(albums: List<AlbumEntity>) {
        changedAlbumIds = albums.map(AlbumEntity::albumId)
    }
    override suspend fun upsertMediaFiles(files: List<MediaFileEntity>) = Unit
    override suspend fun upsertMutation(mutation: LibraryMutationEntity) = Unit
    override suspend fun markSongsMissingFromGeneration(generationId: Long, removedAtMs: Long) = Unit
    override suspend fun markAlbumsMissingFromGeneration(generationId: Long, removedAtMs: Long) = Unit
    override suspend fun markSongsRemoved(songIds: Set<Long>, removedAtMs: Long) {
        removedSongIds = songIds
    }
    override suspend fun markAlbumsRemoved(albumIds: Set<Long>, removedAtMs: Long) {
        removedAlbumIds = albumIds
    }

    override suspend fun replaceGeneration(
        generation: LibraryScanGenerationEntity,
        songs: List<SongEntity>,
        albums: List<AlbumEntity>,
        files: List<MediaFileEntity>,
        removedAtMs: Long,
    ) {
        replaceCount += 1
    }
}
