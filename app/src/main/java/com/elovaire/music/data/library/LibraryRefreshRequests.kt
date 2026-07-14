package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.Song

internal data class LibraryRefreshRequest(
    val forceMediaIndex: Boolean = false,
    val enrichMetadata: Boolean = false,
    val targetedPaths: List<String> = emptyList(),
) {
    fun mergedWith(other: LibraryRefreshRequest): LibraryRefreshRequest {
        val force = forceMediaIndex || other.forceMediaIndex
        val mergedPaths = if (force) {
            emptyList()
        } else {
            (targetedPaths + other.targetedPaths)
                .asSequence()
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .toList()
        }
        if (mergedPaths.size > MAX_TARGETED_REFRESH_PATHS) {
            return LibraryRefreshRequest(
                forceMediaIndex = true,
                enrichMetadata = enrichMetadata || other.enrichMetadata,
            )
        }
        return LibraryRefreshRequest(
            forceMediaIndex = force,
            enrichMetadata = enrichMetadata || other.enrichMetadata,
            targetedPaths = mergedPaths,
        )
    }

    fun normalized(): LibraryRefreshRequest {
        val normalizedPaths = if (forceMediaIndex) {
            emptyList()
        } else {
            targetedPaths
                .asSequence()
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .toList()
        }
        if (normalizedPaths.size > MAX_TARGETED_REFRESH_PATHS) {
            return copy(
                forceMediaIndex = true,
                targetedPaths = emptyList(),
            )
        }
        return copy(
            targetedPaths = normalizedPaths,
        )
    }

    private companion object {
        const val MAX_TARGETED_REFRESH_PATHS = 64
    }
}

internal class LibraryRefreshRequests {
    private var pending: LibraryRefreshRequest? = null

    @Synchronized
    fun enqueue(request: LibraryRefreshRequest) {
        val normalized = request.normalized()
        pending = pending?.mergedWith(normalized) ?: normalized
    }

    fun enqueue(
        forceMediaIndex: Boolean = false,
        enrichMetadata: Boolean = false,
        targetedPaths: Collection<String> = emptyList(),
    ) {
        enqueue(
            LibraryRefreshRequest(
                forceMediaIndex = forceMediaIndex,
                enrichMetadata = enrichMetadata,
                targetedPaths = targetedPaths.toList(),
            ),
        )
    }

    @Synchronized
    fun takeForImmediateScan(request: LibraryRefreshRequest): LibraryRefreshRequest {
        val queued = pending
        pending = null
        return queued?.let(request::mergedWith) ?: request.normalized()
    }

    @Synchronized
    fun takePendingAfterScan(): LibraryRefreshRequest? {
        return pending.also { pending = null }
    }

    @Synchronized
    fun clearIndexRefresh() {
        pending = pending?.copy(
            forceMediaIndex = false,
            targetedPaths = emptyList(),
        )?.takeIf { it.enrichMetadata }
    }

    @Synchronized
    fun clear() {
        pending = null
    }
}

internal fun resolveTargetedRefreshPaths(
    requestedPaths: Collection<String>,
    songIds: Collection<Long>,
    currentSongs: List<Song>,
): List<String> {
    if (requestedPaths.isEmpty() && songIds.isEmpty()) return emptyList()
    val requestedSongIds = songIds.toHashSet()
    return buildList {
        addAll(requestedPaths)
        if (requestedSongIds.isNotEmpty()) {
            currentSongs.asSequence()
                .filter { it.id in requestedSongIds }
                .mapNotNull(Song::libraryPath)
                .forEach(::add)
        }
    }.asSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .distinct()
        .toList()
}
