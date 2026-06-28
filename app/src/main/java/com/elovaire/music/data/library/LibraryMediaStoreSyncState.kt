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
    data object FullScan : LibrarySyncDecision
}

internal fun decideLibrarySync(
    cached: LibraryMediaStoreSyncState?,
    current: LibraryMediaStoreSyncState?,
): LibrarySyncDecision {
    if (cached == null || current == null) return LibrarySyncDecision.FullScan
    if (cached.filterFingerprint != current.filterFingerprint) return LibrarySyncDecision.FullScan
    if (cached.volumes.size != current.volumes.size) return LibrarySyncDecision.FullScan

    val cachedByVolume = cached.volumes.associateBy { it.volumeName }
    current.volumes.forEach { currentVolume ->
        val cachedVolume = cachedByVolume[currentVolume.volumeName] ?: return LibrarySyncDecision.FullScan
        if (cachedVolume.version != currentVolume.version) return LibrarySyncDecision.FullScan
        if (cachedVolume.generation != currentVolume.generation) return LibrarySyncDecision.FullScan
    }
    return LibrarySyncDecision.ReuseCached
}
