package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.Song

internal data class LibraryRefreshRequest(
    val forceMediaIndex: Boolean = false,
    val enrichMetadata: Boolean = false,
    val targetedPaths: List<String> = emptyList(),
    /** Null scans all configured SAF trees; a set scans only those tree identities. */
    val targetedSafTreeIds: Set<String>? = null,
    /** Null means reconcile every source; an empty set means only merge current source state. */
    val targetedNetworkSourceIds: Set<String>? = null,
    val mediaStoreGenerationFloor: Long? = null,
    val mediaStoreGenerationFloors: Map<String, Long> = emptyMap(),
    /** Reuse unaffected local sources while a targeted SAF source is being discovered. */
    val reuseLocalState: Boolean = false,
    /** Number of automatic retries after a provider reports that its result is still loading. */
    val safProviderRetryAttempt: Int = 0,
    val priority: LibraryRefreshPriority = LibraryRefreshPriority.Background,
    val removedPaths: List<String> = emptyList(),
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
        val mergedNetworkSourceIds = when {
            force || targetedNetworkSourceIds == null || other.targetedNetworkSourceIds == null -> null
            else -> targetedNetworkSourceIds + other.targetedNetworkSourceIds
        }
        val mergedSafTreeIds = when {
            force || targetedSafTreeIds == null || other.targetedSafTreeIds == null -> null
            else -> targetedSafTreeIds + other.targetedSafTreeIds
        }
        val reuseLocalState = reuseLocalState && other.reuseLocalState && !force
        val safProviderRetryAttempt = maxOf(safProviderRetryAttempt, other.safProviderRetryAttempt)
        val priority = maxOf(priority, other.priority)
        val mergedRemovedPaths = if (force) {
            emptyList()
        } else {
            (removedPaths + other.removedPaths)
                .asSequence()
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .toList()
        }
        val mergedGenerationFloor = if (
            force ||
                mergedPaths.isNotEmpty() ||
                mergedSafTreeIds != null ||
                targetedNetworkSourceIds != null ||
                other.targetedNetworkSourceIds != null
        ) {
            null
        } else {
            listOfNotNull(mediaStoreGenerationFloor, other.mediaStoreGenerationFloor).minOrNull()
            }
        val mergedGenerationFloors = if (
            force ||
                mergedPaths.isNotEmpty() ||
                mergedSafTreeIds != null ||
                targetedNetworkSourceIds != null ||
                other.targetedNetworkSourceIds != null
        ) {
            emptyMap()
        } else {
            (mediaStoreGenerationFloors.keys + other.mediaStoreGenerationFloors.keys)
                .associateWith { volume ->
                    listOfNotNull(
                        mediaStoreGenerationFloors[volume],
                        other.mediaStoreGenerationFloors[volume],
                    ).minOrNull() ?: error("Missing MediaStore generation floor")
                }
        }
        if (mergedPaths.size > MAX_TARGETED_REFRESH_PATHS) {
            // A large path burst is already a full MediaStore reconciliation. Escalating it to
            // a recursive MediaScannerConnection walk adds unbounded storage work and is not
            // required to query the authoritative provider state.
            return LibraryRefreshRequest(
                forceMediaIndex = false,
                enrichMetadata = enrichMetadata || other.enrichMetadata,
                targetedSafTreeIds = null,
                targetedNetworkSourceIds = mergedNetworkSourceIds,
                priority = priority,
                reuseLocalState = false,
                safProviderRetryAttempt = safProviderRetryAttempt,
            )
        }
        return LibraryRefreshRequest(
            forceMediaIndex = force,
            enrichMetadata = enrichMetadata || other.enrichMetadata,
            targetedPaths = mergedPaths,
            targetedSafTreeIds = mergedSafTreeIds,
            targetedNetworkSourceIds = mergedNetworkSourceIds,
            mediaStoreGenerationFloor = mergedGenerationFloor,
            mediaStoreGenerationFloors = mergedGenerationFloors,
            reuseLocalState = reuseLocalState,
            safProviderRetryAttempt = safProviderRetryAttempt,
            priority = priority,
            removedPaths = mergedRemovedPaths,
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
                forceMediaIndex = false,
                targetedPaths = emptyList(),
                targetedSafTreeIds = null,
            )
        }
        return copy(
            targetedPaths = normalizedPaths,
            targetedSafTreeIds = if (forceMediaIndex) {
                null
            } else {
                targetedSafTreeIds
                    ?.map(String::trim)
                    ?.filter(String::isNotBlank)
                    ?.toSet()
            },
            targetedNetworkSourceIds = targetedNetworkSourceIds
                ?.map(String::trim)
                ?.filter(String::isNotBlank)
                ?.toSet(),
            mediaStoreGenerationFloor = mediaStoreGenerationFloor
                ?.takeIf {
                    !forceMediaIndex &&
                        normalizedPaths.isEmpty() &&
                        targetedSafTreeIds.isNullOrEmpty() &&
                        targetedNetworkSourceIds.isNullOrEmpty()
                },
            mediaStoreGenerationFloors = mediaStoreGenerationFloors
                .filterValues { it >= 0L }
                .takeIf {
                    !forceMediaIndex &&
                        normalizedPaths.isEmpty() &&
                        targetedSafTreeIds.isNullOrEmpty() &&
                        targetedNetworkSourceIds.isNullOrEmpty()
                }
                .orEmpty(),
            reuseLocalState = reuseLocalState && !forceMediaIndex,
            safProviderRetryAttempt = safProviderRetryAttempt.coerceAtLeast(0),
            priority = priority,
            removedPaths = removedPaths
                .asSequence()
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .toList(),
        )
    }

    private companion object {
        const val MAX_TARGETED_REFRESH_PATHS = 64
    }
}

internal fun mergeBackgroundRefreshRequest(
    existing: LibraryRefreshRequest?,
    incoming: LibraryRefreshRequest,
): LibraryRefreshRequest = existing?.mergedWith(incoming) ?: incoming.normalized()

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
    fun hasPending(): Boolean = pending != null

    @Synchronized
    fun clearIndexRefresh() {
        pending = pending?.copy(
            forceMediaIndex = false,
            targetedPaths = emptyList(),
            targetedSafTreeIds = null,
            mediaStoreGenerationFloor = null,
            mediaStoreGenerationFloors = emptyMap(),
            removedPaths = emptyList(),
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
