package elovaire.music.droidbeauty.app.data.library.db

import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.data.library.LibraryChangeSet
import elovaire.music.droidbeauty.app.data.library.MediaIdentityResolver
import elovaire.music.droidbeauty.app.data.library.libraryIndexContentRevision

internal class LibraryIndexStore(
    private val dao: LibraryDao,
    private val clock: AppClock = AndroidAppClock,
) {
    private var lastIndexedRevision: String? = null

    suspend fun indexSnapshot(
        snapshot: LibrarySnapshot,
        filterFingerprint: String,
        source: String,
    ) {
        val revision = libraryIndexContentRevision(snapshot, filterFingerprint, source)
        if (revision == lastIndexedRevision) return
        val now = clock.wallTimeMs()
        val generationId = nextLibraryGenerationId(now, dao.latestGenerationId())
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
        lastIndexedRevision = revision
    }

    suspend fun applyChangedSongs(
        songs: List<Song>,
        albums: List<Album>,
        replacedFileKeys: Set<String> = emptySet(),
        replacedUris: Set<String> = emptySet(),
    ) {
        if (songs.isEmpty() && albums.isEmpty() && replacedFileKeys.isEmpty() && replacedUris.isEmpty()) return
        val now = clock.wallTimeMs()
        dao.applyIncrementalUpdate(
            songs = songs.map { LibraryDatabaseMapper.songEntity(it, now) },
            albums = albums.map { LibraryDatabaseMapper.albumEntity(it, now) },
            files = songs.map { LibraryDatabaseMapper.mediaFileEntity(it, now, now) },
            replacedFileKeys = replacedFileKeys,
            replacedUris = replacedUris,
        )
        lastIndexedRevision = null
    }

    suspend fun applyChangeSet(
        changeSet: LibraryChangeSet,
        snapshot: LibrarySnapshot,
        fullRebuild: Boolean = false,
    ) {
        if (changeSet.isEmpty) return
        if (fullRebuild) {
            indexSnapshot(snapshot, filterFingerprint = "", source = "reconciliation")
            return
        }
        val changedSongs = buildList {
            addAll(changeSet.added)
            addAll(changeSet.updated.map { it.after })
            addAll(changeSet.relocated.map { it.after })
        }
        val affectedAlbums = snapshot.albums.filter { it.id in changeSet.affectedAlbumIds }
        val removedSongIds = buildSet {
            addAll(changeSet.removed.map(Song::id))
            changeSet.relocated.forEach { relocation ->
                if (relocation.before.id != relocation.after.id) add(relocation.before.id)
            }
        }
        val removedAlbumIds = changeSet.affectedAlbumIds - affectedAlbums.mapTo(hashSetOf(), Album::id)
        val removedFileKeys = buildSet {
            addAll(changeSet.removed.map(MediaIdentityResolver::stableKey))
            addAll(changeSet.relocated.map { MediaIdentityResolver.stableKey(it.before) })
        }
        val removedUris = buildSet {
            addAll(changeSet.removed.map { it.uri.toString() })
            addAll(changeSet.relocated.map { it.before.uri.toString() })
        }
        val now = clock.wallTimeMs()
        dao.applyIncrementalChange(
            songs = changedSongs.map { LibraryDatabaseMapper.songEntity(it, now) },
            albums = affectedAlbums.map { LibraryDatabaseMapper.albumEntity(it, now) },
            files = changedSongs.map { LibraryDatabaseMapper.mediaFileEntity(it, now, now) },
            removedSongIds = removedSongIds,
            removedAlbumIds = removedAlbumIds,
            removedFileKeys = removedFileKeys,
            removedUris = removedUris,
            removedAtMs = now,
        )
        lastIndexedRevision = null
    }

    suspend fun markRemoved(
        songIds: Set<Long>,
        albumIds: Set<Long>,
    ) {
        if (songIds.isEmpty() && albumIds.isEmpty()) return
        dao.applyIncrementalRemoval(songIds, albumIds, clock.wallTimeMs())
        lastIndexedRevision = null
    }
}

internal fun nextLibraryGenerationId(wallTimeMs: Long, latestGenerationId: Long?): Long {
    val nextPersisted = latestGenerationId
        ?.takeIf { it < Long.MAX_VALUE }
        ?.plus(1L)
        ?: 1L
    return maxOf(wallTimeMs.coerceAtLeast(1L), nextPersisted)
}
