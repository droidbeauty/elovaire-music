package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.library.LibraryRefreshRequest

internal sealed interface BackendWorkRequest {
    data class LibraryRefresh(val request: LibraryRefreshRequest) : BackendWorkRequest
    data class MetadataEnrichment(val songIds: Set<Long>) : BackendWorkRequest
    data class LyricsPrefetch(val songIds: List<Long>, val userInitiated: Boolean) : BackendWorkRequest
    data object AutomaticUpdateCheck : BackendWorkRequest
    data object StartupCleanup : BackendWorkRequest
}

internal class BackendWorkCoordinator {
    private var pendingLibraryRefresh: LibraryRefreshRequest? = null
    private var pendingMetadataSongIds = emptySet<Long>()
    private var pendingLyricsPrefetch: BackendWorkRequest.LyricsPrefetch? = null
    private var automaticUpdatePending = false
    private var startupCleanupPending = false
    private var released = false

    @Synchronized
    fun enqueue(request: BackendWorkRequest) {
        if (released) return
        when (request) {
            is BackendWorkRequest.LibraryRefresh -> {
                val normalized = request.request.normalized()
                pendingLibraryRefresh = pendingLibraryRefresh?.mergedWith(normalized) ?: normalized
            }
            is BackendWorkRequest.MetadataEnrichment -> {
                pendingMetadataSongIds = pendingMetadataSongIds + request.songIds.filter { it > 0L }
            }
            is BackendWorkRequest.LyricsPrefetch -> {
                pendingLyricsPrefetch = request.copy(songIds = request.songIds.filter { it > 0L }.distinct())
            }
            BackendWorkRequest.AutomaticUpdateCheck -> automaticUpdatePending = true
            BackendWorkRequest.StartupCleanup -> startupCleanupPending = true
        }
    }

    @Synchronized
    fun takeLibraryRefresh(immediate: LibraryRefreshRequest? = null): LibraryRefreshRequest? {
        val merged = when {
            immediate == null -> pendingLibraryRefresh
            pendingLibraryRefresh == null -> immediate
            else -> pendingLibraryRefresh?.let(immediate::mergedWith)
        }
        pendingLibraryRefresh = null
        return merged?.normalized()
    }

    @Synchronized
    fun clearLibraryIndexRefresh() {
        pendingLibraryRefresh = pendingLibraryRefresh?.copy(
            forceMediaIndex = false,
            targetedPaths = emptyList(),
        )?.takeIf { it.enrichMetadata }
    }

    @Synchronized
    fun takeLyricsPrefetch(): BackendWorkRequest.LyricsPrefetch? = pendingLyricsPrefetch.also {
        pendingLyricsPrefetch = null
    }

    @Synchronized
    fun clear() {
        pendingLibraryRefresh = null
        pendingMetadataSongIds = emptySet()
        pendingLyricsPrefetch = null
        automaticUpdatePending = false
        startupCleanupPending = false
    }

    @Synchronized
    fun release() {
        released = true
        clear()
    }
}
