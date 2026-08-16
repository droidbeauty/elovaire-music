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
    val revision: String,
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
            revision = libraryContentRevision(emptyList(), "", null),
        )
    }
}

internal data class LibrarySongUpdate(
    val before: Song,
    val after: Song,
) {
    val stableKey: String
        get() = MediaIdentityResolver.stableKey(after)
}

internal data class LibrarySongRelocation(
    val before: Song,
    val after: Song,
)

internal object LibraryChangeSetCalculator {
    fun between(
        previous: List<Song>,
        next: List<Song>,
    ): LibraryChangeSet {
        val previousByKey = previous.associateBy(MediaIdentityResolver::stableKey)
        val nextByKey = next.associateBy(MediaIdentityResolver::stableKey)
        val directRelocations = nextByKey.mapNotNull { (key, after) ->
            val before = previousByKey[key] ?: return@mapNotNull null
            if (locatorChanged(before, after) && sameLogicalContent(before, after)) {
                LibrarySongRelocation(before, after)
            } else {
                null
            }
        }
        val previousUnmatchedByPath = previousByKey
            .filterKeys { it !in nextByKey }
            .values
            .mapNotNull { song ->
                LibrarySongDuplicateResolver.normalizedRealPath(song.libraryPath)?.let { it to song }
            }
            .toMap()
        val reindexedRelocations = nextByKey
            .filterKeys { it !in previousByKey }
            .values
            .mapNotNull { after ->
                val path = LibrarySongDuplicateResolver.normalizedRealPath(after.libraryPath)
                    ?: return@mapNotNull null
                val before = previousUnmatchedByPath[path] ?: return@mapNotNull null
                if (sameLogicalContent(before, after)) {
                    LibrarySongRelocation(before, after)
                } else {
                    null
                }
            }
        val relocated = directRelocations + reindexedRelocations
        val relocatedBeforeKeys = relocated.mapTo(hashSetOf()) { MediaIdentityResolver.stableKey(it.before) }
        val relocatedAfterKeys = relocated.mapTo(hashSetOf()) { MediaIdentityResolver.stableKey(it.after) }
        val added = nextByKey
            .filterKeys { it !in previousByKey && it !in relocatedAfterKeys }
            .values
            .toList()
        val removed = previousByKey
            .filterKeys { it !in nextByKey && it !in relocatedBeforeKeys }
            .values
            .toList()
        val updated = nextByKey.mapNotNull { (key, after) ->
            val before = previousByKey[key] ?: return@mapNotNull null
            after.takeIf { !sameSong(before, it) && key !in relocatedBeforeKeys }
                ?.let { LibrarySongUpdate(before, it) }
        }
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
                if (relocation.before.artUri?.toString() != relocation.after.artUri?.toString()) {
                    relocation.before.artUri?.toString()?.takeIf(String::isNotBlank)?.let(::add)
                    relocation.after.artUri?.toString()?.takeIf(String::isNotBlank)?.let(::add)
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
            revision = libraryContentRevision(next, "", null),
        )
    }

    private fun sameSong(first: Song, second: Song): Boolean {
        return first.id == second.id &&
            first.title == second.title &&
            first.isExplicit == second.isExplicit &&
            first.artist == second.artist &&
            first.album == second.album &&
            first.releaseYear == second.releaseYear &&
            first.genre == second.genre &&
            first.audioFormat == second.audioFormat &&
            first.audioQuality == second.audioQuality &&
            first.fileName == second.fileName &&
            first.albumId == second.albumId &&
            first.durationMs == second.durationMs &&
            first.trackNumber == second.trackNumber &&
            first.discNumber == second.discNumber &&
            first.dateAddedSeconds == second.dateAddedSeconds &&
            first.dateModifiedSeconds == second.dateModifiedSeconds &&
            first.libraryPath == second.libraryPath &&
            first.uri.toString() == second.uri.toString() &&
            first.artUri?.toString() == second.artUri?.toString() &&
            first.metadataResolved == second.metadataResolved &&
            first.albumArtist == second.albumArtist &&
            first.volumeNormalization == second.volumeNormalization
    }

    private fun sameLogicalContent(first: Song, second: Song): Boolean {
        return sameSong(
            first.copy(
                id = second.id,
                fileName = second.fileName,
                dateAddedSeconds = second.dateAddedSeconds,
                libraryPath = second.libraryPath,
                uri = second.uri,
                artUri = second.artUri,
            ),
            second,
        )
    }

    private fun locatorChanged(first: Song, second: Song): Boolean {
        return first.fileName != second.fileName ||
            first.libraryPath != second.libraryPath ||
            first.uri.toString() != second.uri.toString() ||
            first.artUri?.toString() != second.artUri?.toString()
    }
}
