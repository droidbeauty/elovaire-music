package elovaire.music.droidbeauty.app.data.library.db

import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song

internal class LibraryIndexStore(
    private val dao: LibraryDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private var lastIndexedInput: LibraryIndexInput? = null

    suspend fun indexSnapshot(
        snapshot: LibrarySnapshot,
        filterFingerprint: String,
        source: String,
    ) {
        val input = LibraryIndexInput(snapshot, filterFingerprint, source)
        if (input == lastIndexedInput) return
        val now = clock()
        val generationId = now
        val indexed = LibraryDatabaseMapper.indexedSnapshot(snapshot, generationId, now)
        dao.replaceGeneration(
            generation = LibraryScanGenerationEntity(
                generationId = generationId,
                startedAtMs = now,
                finishedAtMs = now,
                source = source,
                filterFingerprint = filterFingerprint,
                status = "Completed",
                error = null,
            ),
            songs = indexed.songs,
            albums = indexed.albums,
            files = indexed.mediaFiles,
            removedAtMs = now,
        )
        lastIndexedInput = input
    }

    suspend fun applyChangedSongs(
        songs: List<Song>,
        albums: List<Album>,
    ) {
        if (songs.isEmpty() && albums.isEmpty()) return
        val now = clock()
        dao.applyIncrementalUpdate(
            songs = songs.map { LibraryDatabaseMapper.songEntity(it, now) },
            albums = albums.map { LibraryDatabaseMapper.albumEntity(it, now) },
            files = songs.map { LibraryDatabaseMapper.mediaFileEntity(it, now, now) },
        )
        lastIndexedInput = null
    }

    suspend fun markRemoved(
        songIds: Set<Long>,
        albumIds: Set<Long>,
    ) {
        if (songIds.isEmpty() && albumIds.isEmpty()) return
        dao.applyIncrementalRemoval(songIds, albumIds, clock())
        lastIndexedInput = null
    }
}

private data class LibraryIndexInput(
    val snapshot: LibrarySnapshot,
    val filterFingerprint: String,
    val source: String,
)
