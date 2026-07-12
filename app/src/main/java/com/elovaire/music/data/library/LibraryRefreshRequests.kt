package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.core.BackendWorkCoordinator
import elovaire.music.droidbeauty.app.core.BackendWorkRequest

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
    private val coordinator = BackendWorkCoordinator()

    fun enqueue(request: LibraryRefreshRequest) {
        coordinator.enqueue(BackendWorkRequest.LibraryRefresh(request))
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
        return coordinator.takeLibraryRefresh(request) ?: request.normalized()
    }

    fun takePendingAfterScan(): LibraryRefreshRequest? {
        return coordinator.takeLibraryRefresh()
    }

    fun clearIndexRefresh() {
        coordinator.clearLibraryIndexRefresh()
    }

    fun clear() {
        coordinator.clear()
    }
}
