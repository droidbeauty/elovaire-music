package elovaire.music.droidbeauty.app.data.library

internal data class LibraryRefreshRequest(
    val forceMediaIndex: Boolean = false,
    val enrichMetadata: Boolean = false,
    val targetedPaths: List<String> = emptyList(),
) {
    fun mergedWith(other: LibraryRefreshRequest): LibraryRefreshRequest {
        val force = forceMediaIndex || other.forceMediaIndex
        return LibraryRefreshRequest(
            forceMediaIndex = force,
            enrichMetadata = enrichMetadata || other.enrichMetadata,
            targetedPaths = if (force) {
                emptyList()
            } else {
                (targetedPaths + other.targetedPaths)
                    .asSequence()
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .distinct()
                    .toList()
            },
        )
    }

    fun normalized(): LibraryRefreshRequest {
        return copy(
            targetedPaths = if (forceMediaIndex) {
                emptyList()
            } else {
                targetedPaths
                    .asSequence()
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .distinct()
                    .toList()
            },
        )
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
