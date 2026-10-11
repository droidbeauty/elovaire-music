package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.Song

/** The smallest useful description of a library transition for incremental consumers. */
internal data class LibraryChangeSet(
    val added: List<Song>,
    val updated: List<LibrarySongUpdate>,
    val relocated: List<LibrarySongRelocation>,
    val removed: List<Song>,
    val affectedAlbumIds: Set<Long>,
    val artworkInvalidatedUris: Set<String>,
) {
    val isEmpty: Boolean
        get() = added.isEmpty() && updated.isEmpty() && relocated.isEmpty() && removed.isEmpty()

    companion object {
        val Empty = LibraryChangeSet(
            added = emptyList(),
            updated = emptyList(),
            relocated = emptyList(),
            removed = emptyList(),
            affectedAlbumIds = emptySet(),
            artworkInvalidatedUris = emptySet(),
        )
    }
}

internal data class LibrarySongUpdate(
    val before: Song,
    val after: Song,
    val stableKey: String = MediaIdentityResolver.stableKey(after),
)

internal data class LibrarySongRelocation(
    val before: Song,
    val after: Song,
)

internal data class LibrarySongPatch(
    val before: Song,
    val after: Song,
)

internal object LibraryChangeSetCalculator {
    fun fromPatches(patches: List<LibrarySongPatch>): LibraryChangeSet {
        if (patches.isEmpty()) return LibraryChangeSet.Empty
        val indexedPatches = patches.map { patch ->
            val before = index(patch.before)
            val after = index(patch.after)
            IndexedLibrarySongPatch(
                before = before,
                after = after,
                isRelocation = locatorChanged(before.song, after.song) &&
                    sameLogicalContent(before.song, after.song),
            )
        }
        val relocated = indexedPatches.mapNotNull { patch ->
            if (patch.isRelocation) {
                LibrarySongRelocation(patch.before.song, patch.after.song)
            } else {
                null
            }
        }
        val relocatedBeforeKeys = indexedPatches.asSequence()
            .filter(IndexedLibrarySongPatch::isRelocation)
            .mapTo(hashSetOf()) { it.before.stableKey }
        val updated = indexedPatches.mapNotNull { patch ->
            if (sameSong(patch.before.song, patch.after.song) ||
                patch.before.stableKey in relocatedBeforeKeys
            ) {
                null
            } else {
                LibrarySongUpdate(patch.before.song, patch.after.song, patch.after.stableKey)
            }
        }
        return buildChangeSet(
            added = emptyList(),
            updated = updated,
            relocated = relocated,
            removed = emptyList(),
        )
    }

    fun between(
        previous: List<Song>,
        next: List<Song>,
    ): LibraryChangeSet {
        val previousByKey = indexByStableKey(previous)
        val nextByKey = indexByStableKey(next)
        val directRelocations = ArrayList<IndexedLibrarySongRelocation>()
        nextByKey.forEach { (key, after) ->
            val before = previousByKey[key] ?: return@forEach
            if (locatorChanged(before.song, after.song) && sameLogicalContent(before.song, after.song)) {
                directRelocations += IndexedLibrarySongRelocation(before, after)
            }
        }
        val previousUnmatchedByPath = HashMap<String, IndexedLibrarySong>()
        previousByKey.forEach { (key, song) ->
            if (key !in nextByKey) song.normalizedRealPath?.let { previousUnmatchedByPath[it] = song }
        }
        val reindexedRelocations = ArrayList<IndexedLibrarySongRelocation>()
        nextByKey.forEach { (key, after) ->
            if (key in previousByKey) return@forEach
            val path = after.normalizedRealPath ?: return@forEach
            val before = previousUnmatchedByPath[path] ?: return@forEach
            if (sameLogicalContent(before.song, after.song)) {
                reindexedRelocations += IndexedLibrarySongRelocation(before, after)
            }
        }
        val relocatedCandidates = directRelocations + reindexedRelocations
        val relocated = relocatedCandidates.map { LibrarySongRelocation(it.before.song, it.after.song) }
        val relocatedBeforeKeys = relocatedCandidates.mapTo(hashSetOf()) { it.before.stableKey }
        val relocatedAfterKeys = relocatedCandidates.mapTo(hashSetOf()) { it.after.stableKey }
        val added = ArrayList<Song>()
        nextByKey.forEach { (key, song) ->
            if (key !in previousByKey && key !in relocatedAfterKeys) added += song.song
        }
        val removed = ArrayList<Song>()
        previousByKey.forEach { (key, song) ->
            if (key !in nextByKey && key !in relocatedBeforeKeys) removed += song.song
        }
        val updated = ArrayList<LibrarySongUpdate>()
        nextByKey.forEach { (key, after) ->
            val before = previousByKey[key] ?: return@forEach
            if (!sameSong(before.song, after.song) && key !in relocatedBeforeKeys) {
                updated += LibrarySongUpdate(before.song, after.song, after.stableKey)
            }
        }
        return buildChangeSet(added, updated, relocated, removed)
    }

    private fun indexByStableKey(songs: List<Song>): LinkedHashMap<String, IndexedLibrarySong> {
        val indexedSongs = LinkedHashMap<String, IndexedLibrarySong>(songs.size)
        songs.forEach { song ->
            val indexedSong = index(song)
            indexedSongs[indexedSong.stableKey] = indexedSong
        }
        return indexedSongs
    }

    private fun buildChangeSet(
        added: List<Song>,
        updated: List<LibrarySongUpdate>,
        relocated: List<LibrarySongRelocation>,
        removed: List<Song>,
    ): LibraryChangeSet {
        val affectedAlbumIds = buildSet {
            added.forEach { add(it.albumId) }
            removed.forEach { add(it.albumId) }
            updated.forEach {
                add(it.before.albumId)
                add(it.after.albumId)
            }
            relocated.forEach {
                add(it.before.albumId)
                add(it.after.albumId)
            }
        }
        val artworkInvalidatedUris = buildSet {
            updated.forEach { update ->
                update.before.artUri?.toString()?.takeIf(String::isNotBlank)?.let(::add)
                update.after.artUri?.toString()?.takeIf(String::isNotBlank)?.let(::add)
            }
            relocated.forEach { relocation ->
                val beforeArtUri = relocation.before.artUri?.toString()
                val afterArtUri = relocation.after.artUri?.toString()
                if (beforeArtUri != afterArtUri) {
                    beforeArtUri?.takeIf(String::isNotBlank)?.let(::add)
                    afterArtUri?.takeIf(String::isNotBlank)?.let(::add)
                }
            }
            removed.forEach { song ->
                song.artUri?.toString()?.takeIf(String::isNotBlank)?.let(::add)
            }
        }
        return LibraryChangeSet(
            added = added,
            updated = updated,
            relocated = relocated,
            removed = removed,
            affectedAlbumIds = affectedAlbumIds,
            artworkInvalidatedUris = artworkInvalidatedUris,
        )
    }

    private fun sameSong(first: Song, second: Song): Boolean {
        return sameLogicalContent(first, second) &&
            first.id == second.id &&
            first.fileName == second.fileName &&
            first.dateAddedSeconds == second.dateAddedSeconds &&
            first.libraryPath == second.libraryPath &&
            first.uri.toString() == second.uri.toString() &&
            first.artUri?.toString() == second.artUri?.toString()
    }

    private fun sameLogicalContent(first: Song, second: Song): Boolean {
        return first.title == second.title &&
            first.isExplicit == second.isExplicit &&
            first.artist == second.artist &&
            first.album == second.album &&
            first.description == second.description &&
            first.releaseYear == second.releaseYear &&
            first.genre == second.genre &&
            first.audioFormat == second.audioFormat &&
            first.audioQuality == second.audioQuality &&
            first.albumId == second.albumId &&
            first.durationMs == second.durationMs &&
            first.trackNumber == second.trackNumber &&
            first.discNumber == second.discNumber &&
            first.dateModifiedSeconds == second.dateModifiedSeconds &&
            first.metadataResolved == second.metadataResolved &&
            first.albumArtist == second.albumArtist &&
            first.volumeNormalization == second.volumeNormalization &&
            first.mediaKind == second.mediaKind &&
            first.bookmarkMs == second.bookmarkMs
    }

    private fun locatorChanged(first: Song, second: Song): Boolean {
        return first.fileName != second.fileName ||
            first.libraryPath != second.libraryPath ||
            first.uri.toString() != second.uri.toString()
    }

    private fun index(song: Song): IndexedLibrarySong = IndexedLibrarySong(
        song = song,
        stableKey = MediaIdentityResolver.stableKey(song),
        normalizedRealPath = LibrarySongDuplicateResolver.normalizedRealPath(song.libraryPath),
    )
}

private data class IndexedLibrarySong(
    val song: Song,
    val stableKey: String,
    val normalizedRealPath: String?,
)

private data class IndexedLibrarySongPatch(
    val before: IndexedLibrarySong,
    val after: IndexedLibrarySong,
    val isRelocation: Boolean,
)

private data class IndexedLibrarySongRelocation(
    val before: IndexedLibrarySong,
    val after: IndexedLibrarySong,
)
