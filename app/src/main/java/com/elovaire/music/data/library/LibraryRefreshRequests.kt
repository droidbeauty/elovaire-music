package elovaire.music.droidbeauty.app.data.library

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

    fun takeForImmediateScan(request: LibraryRefreshRequest): LibraryRefreshRequest {
        val merged = pending?.let(request::mergedWith) ?: request
        pending = null
        return merged.normalized()
    }

    fun takePendingAfterScan(): LibraryRefreshRequest? {
        val next = pending?.normalized()
        pending = null
        return next
    }

    fun clearIndexRefresh() {
        pending = pending?.copy(
            forceMediaIndex = false,
            targetedPaths = emptyList(),
        )?.takeIf { it.enrichMetadata }
    }

    fun clear() {
        pending = null
    }
}
