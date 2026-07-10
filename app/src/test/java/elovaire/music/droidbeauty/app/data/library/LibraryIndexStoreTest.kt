package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.data.library.db.AlbumEntity
import elovaire.music.droidbeauty.app.data.library.db.LibraryDao
import elovaire.music.droidbeauty.app.data.library.db.LibraryIndexStore
import elovaire.music.droidbeauty.app.data.library.db.LibraryMutationEntity
import elovaire.music.droidbeauty.app.data.library.db.LibraryScanGenerationEntity
import elovaire.music.droidbeauty.app.data.library.db.MediaFileEntity
import elovaire.music.droidbeauty.app.data.library.db.SongEntity
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryIndexStoreTest {
    @Test
    fun indexSnapshot_skipsIdenticalCompletedGeneration() = runBlocking {
        val dao = RecordingLibraryDao()
        val store = LibraryIndexStore(dao, clock = { 42L })
        val snapshot = LibrarySnapshot(emptyList(), emptyList())

        store.indexSnapshot(snapshot, filterFingerprint = "folders", source = "MediaStore")
        store.indexSnapshot(snapshot, filterFingerprint = "folders", source = "MediaStore")

        assertEquals(1, dao.replaceCount)
    }

    @Test
    fun indexSnapshot_replacesGenerationWhenFilterChanges() = runBlocking {
        val dao = RecordingLibraryDao()
        val store = LibraryIndexStore(dao, clock = { 42L })
        val snapshot = LibrarySnapshot(emptyList(), emptyList())

        store.indexSnapshot(snapshot, filterFingerprint = "music", source = "MediaStore")
        store.indexSnapshot(snapshot, filterFingerprint = "downloads", source = "MediaStore")

        assertEquals(2, dao.replaceCount)
    }
}

private class RecordingLibraryDao : LibraryDao {
    var replaceCount = 0

    override fun songs(): Flow<List<SongEntity>> = emptyFlow()
    override fun albums(): Flow<List<AlbumEntity>> = emptyFlow()
    override fun activeMutations(): Flow<List<LibraryMutationEntity>> = emptyFlow()
    override suspend fun mutation(mutationId: String): LibraryMutationEntity? = null
    override suspend fun insertScanGeneration(generation: LibraryScanGenerationEntity) = Unit
    override suspend fun upsertSongs(songs: List<SongEntity>) = Unit
    override suspend fun upsertAlbums(albums: List<AlbumEntity>) = Unit
    override suspend fun upsertMediaFiles(files: List<MediaFileEntity>) = Unit
    override suspend fun upsertMutation(mutation: LibraryMutationEntity) = Unit
    override suspend fun markSongsMissingFromGeneration(generationId: Long, removedAtMs: Long) = Unit
    override suspend fun markAlbumsMissingFromGeneration(generationId: Long, removedAtMs: Long) = Unit

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
