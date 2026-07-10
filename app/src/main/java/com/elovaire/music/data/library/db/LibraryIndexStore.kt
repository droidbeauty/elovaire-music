package elovaire.music.droidbeauty.app.data.library.db

import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot

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
}

private data class LibraryIndexInput(
    val snapshot: LibrarySnapshot,
    val filterFingerprint: String,
    val source: String,
)
