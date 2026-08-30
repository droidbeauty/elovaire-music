package elovaire.music.droidbeauty.app.data.library

internal data class LibraryMediaStoreVolumeSyncState(
    val volumeName: String,
    val version: String,
    val generation: Long,
)

internal data class LibraryMediaStoreSyncState(
    val filterFingerprint: String,
    val volumes: List<LibraryMediaStoreVolumeSyncState>,
)

internal sealed interface LibrarySyncDecision {
    data object ReuseCached : LibrarySyncDecision
    data object IncrementalScan : LibrarySyncDecision
    data object FullScan : LibrarySyncDecision
}

internal fun decideLibrarySync(
    cached: LibraryMediaStoreSyncState?,
    current: LibraryMediaStoreSyncState?,
    cachedSongCount: Int? = null,
): LibrarySyncDecision {
    if (cached == null || current == null) return LibrarySyncDecision.FullScan
    if (cachedSongCount == 0) return LibrarySyncDecision.FullScan
    if (cached.filterFingerprint != current.filterFingerprint) return LibrarySyncDecision.FullScan
    if (cached.volumes.size != current.volumes.size) return LibrarySyncDecision.FullScan

    val cachedByVolume = cached.volumes.associateBy { it.volumeName }
    current.volumes.forEach { currentVolume ->
        val cachedVolume = cachedByVolume[currentVolume.volumeName] ?: return LibrarySyncDecision.FullScan
        if (cachedVolume.version != currentVolume.version) return LibrarySyncDecision.FullScan
        if (currentVolume.generation < cachedVolume.generation) return LibrarySyncDecision.FullScan
        if (currentVolume.generation > cachedVolume.generation) return LibrarySyncDecision.IncrementalScan
    }
    return LibrarySyncDecision.ReuseCached
}

internal fun decideLibrarySyncAtStartup(
    cached: LibraryMediaStoreSyncState?,
    current: LibraryMediaStoreSyncState?,
    cachedSongCount: Int? = null,
    hasSafSelections: Boolean,
): LibrarySyncDecision {
    return if (hasSafSelections) {
        LibrarySyncDecision.FullScan
    } else {
        decideLibrarySync(cached, current, cachedSongCount)
    }
}

internal fun decideForegroundReconcile(
    cached: LibraryMediaStoreSyncState?,
    current: LibraryMediaStoreSyncState?,
    cachedSongCount: Int,
    hasSafSelections: Boolean,
    staleNetworkSourceIds: Set<String>,
): LibraryRefreshRequest? {
    if (hasSafSelections) return LibraryRefreshRequest()
    val mediaStoreDecision = decideLibrarySync(cached, current, cachedSongCount)
    return when (mediaStoreDecision) {
        LibrarySyncDecision.ReuseCached -> null
        LibrarySyncDecision.FullScan -> LibraryRefreshRequest()
        LibrarySyncDecision.IncrementalScan -> {
            val generationFloor = cached
                ?.volumes
                ?.map(LibraryMediaStoreVolumeSyncState::generation)
                ?.distinct()
                ?.singleOrNull()
            if (generationFloor == null) {
                LibraryRefreshRequest()
            } else {
                LibraryRefreshRequest(mediaStoreGenerationFloor = generationFloor)
            }
        }
    }.let { mediaStoreRequest ->
        if (staleNetworkSourceIds.isEmpty()) {
            mediaStoreRequest
        } else if (mediaStoreDecision == LibrarySyncDecision.ReuseCached) {
            LibraryRefreshRequest(targetedNetworkSourceIds = staleNetworkSourceIds)
        } else {
            // A targeted network request tells the coordinator to reuse local
            // state, so it must not mask a required MediaStore reconciliation.
            LibraryRefreshRequest()
        }
    }
}
