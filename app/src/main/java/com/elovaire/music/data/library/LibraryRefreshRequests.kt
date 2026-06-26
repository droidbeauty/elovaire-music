package elovaire.music.droidbeauty.app.data.library

internal data class LibraryRefreshRequest(
    val forceMediaIndex: Boolean,
    val enrichMetadata: Boolean,
    val targetedPaths: List<String>,
)

internal class LibraryRefreshRequests {
    var pendingRefresh = false
        private set

    private var pendingIndexRefresh = false
    private var pendingMetadataEnrichment = false
    private val pendingTargetedPaths = linkedSetOf<String>()

    fun markPending(
        forceMediaIndex: Boolean = false,
        enrichMetadata: Boolean = false,
        targetedPaths: Collection<String> = emptyList(),
    ) {
        pendingRefresh = true
        pendingIndexRefresh = pendingIndexRefresh || forceMediaIndex
        pendingMetadataEnrichment = pendingMetadataEnrichment || enrichMetadata
        if (forceMediaIndex) {
            pendingTargetedPaths.clear()
        } else {
            pendingTargetedPaths.addAll(targetedPaths)
        }
    }

    fun markIndexRefresh(
        forceMediaIndex: Boolean,
        targetedPath: String? = null,
    ) {
        pendingIndexRefresh = pendingIndexRefresh || forceMediaIndex
        if (forceMediaIndex) {
            pendingTargetedPaths.clear()
        } else {
            targetedPath?.let(pendingTargetedPaths::add)
        }
    }

    fun addTargetedPaths(
        paths: Collection<String>,
        enrichMetadata: Boolean,
    ) {
        pendingTargetedPaths.addAll(paths)
        pendingMetadataEnrichment = pendingMetadataEnrichment || enrichMetadata
    }

    fun prepareImmediate(request: LibraryRefreshRequest) {
        pendingIndexRefresh = pendingIndexRefresh || request.forceMediaIndex
        pendingMetadataEnrichment = pendingMetadataEnrichment || request.enrichMetadata
        if (request.forceMediaIndex) {
            pendingTargetedPaths.clear()
        } else {
            pendingTargetedPaths.addAll(request.targetedPaths)
        }
    }

    fun takeForScan(
        forceMediaIndex: Boolean,
        enrichMetadata: Boolean,
    ): LibraryRefreshRequest {
        val shouldRefreshIndex = forceMediaIndex || pendingIndexRefresh
        val targeted = if (shouldRefreshIndex) emptyList() else pendingTargetedPaths.toList()
        val shouldEnrich = enrichMetadata || pendingMetadataEnrichment
        pendingIndexRefresh = false
        pendingMetadataEnrichment = false
        pendingTargetedPaths.clear()
        return LibraryRefreshRequest(
            forceMediaIndex = shouldRefreshIndex,
            enrichMetadata = shouldEnrich,
            targetedPaths = targeted,
        )
    }

    fun takePendingAfterScan(): LibraryRefreshRequest? {
        if (!pendingRefresh) return null
        pendingRefresh = false
        val request = LibraryRefreshRequest(
            forceMediaIndex = pendingIndexRefresh,
            enrichMetadata = pendingMetadataEnrichment,
            targetedPaths = pendingTargetedPaths.toList(),
        )
        pendingIndexRefresh = false
        pendingMetadataEnrichment = false
        pendingTargetedPaths.clear()
        return request
    }

    fun clearIndexRefresh() {
        pendingIndexRefresh = false
        pendingTargetedPaths.clear()
    }

    fun clear() {
        pendingRefresh = false
        pendingIndexRefresh = false
        pendingMetadataEnrichment = false
        pendingTargetedPaths.clear()
    }
}
