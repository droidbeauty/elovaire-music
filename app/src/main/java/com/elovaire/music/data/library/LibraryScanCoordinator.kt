package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibraryScanner
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySource
import elovaire.music.droidbeauty.app.data.library.network.NetworkResourceUri
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceIdentity
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.util.Locale

/** Composes independent source scanners without making any one source the merge authority. */
internal class LibraryScanCoordinator(
    internal val localScanner: MediaStoreScanner,
    private val safScanner: SafTreeLibraryScanner,
    private val networkScannerProvider: () -> NetworkLibraryScanner,
    private val clock: AppClock = AndroidAppClock,
) {
    private var networkSources: List<NetworkLibrarySource> = emptyList()
    private var blockedNetworkSourceIds: Set<String> = emptySet()

    fun setNetworkSources(sources: List<NetworkLibrarySource>): Boolean {
        val normalized = sources
            .distinctBy(NetworkLibrarySource::id)
            .sortedBy { it.id }
        if (networkSources == normalized) return false
        networkSources = normalized
        return true
    }

    internal fun networkSourceIdsChanged(sources: List<NetworkLibrarySource>): Set<String> {
        val normalized = sources
            .distinctBy(NetworkLibrarySource::id)
            .sortedBy { it.id }
        val previousById = networkSources.associateBy(NetworkLibrarySource::id)
        return normalized
            .filter { previousById[it.id] != it }
            .mapTo(linkedSetOf(), NetworkLibrarySource::id)
    }

    fun setLibraryFolders(selections: List<LibraryFolderSelection>): Boolean =
        localScanner.setLibraryFolders(selections)

    internal fun libraryFolderSelections(): List<LibraryFolderSelection> = localScanner.libraryFolderSelections()

    fun requiresMediaIndexRepair(): Boolean = localScanner.requiresMediaIndexRepair()

    fun currentFilterFingerprint(): String {
        val remote = networkFilterFingerprint(networkSources)
        return "${localScanner.currentFilterFingerprint()}::network:$remote"
    }

    internal fun currentSyncState(): LibraryMediaStoreSyncState? = localScanner.currentSyncState()

    fun primeMetadataCache(songs: List<Song>) = localScanner.primeMetadataCache(songs)

    fun clearMetadataCache() = localScanner.clearMetadataCache()

    internal fun onMemoryPressure(pressure: MemoryPressure) = localScanner.onMemoryPressure(pressure)

    fun invalidateMetadataCacheForPaths(paths: Collection<String>) = localScanner.invalidateMetadataCacheForPaths(paths)

    fun invalidateMetadataCacheForSongIds(songIds: Collection<Long>) = localScanner.invalidateMetadataCacheForSongIds(songIds)

    internal suspend fun networkSourceNeedsRefresh(): Boolean {
        return staleNetworkSourceIds().isNotEmpty()
    }

    internal suspend fun staleNetworkSourceIds(): Set<String> {
        val sources = networkSources.filter { it.enabled && it.id !in blockedNetworkSourceIds }
        if (sources.isEmpty()) return emptySet()
        return networkScannerProvider().staleSourceIds(sources, clock.wallTimeMs())
    }

    fun blockNetworkSources(sourceIds: Set<String>) {
        blockedNetworkSourceIds = blockedNetworkSourceIds + sourceIds
    }

    fun unblockNetworkSource(sourceId: String) {
        blockedNetworkSourceIds = blockedNetworkSourceIds - sourceId
    }

    suspend fun scan(
        refreshMediaIndex: Boolean = false,
        refreshMediaPaths: List<String> = emptyList(),
        enrichMetadata: Boolean = true,
        mediaStoreGenerationFloor: Long? = null,
        targetedSafTreeIds: Set<String>? = null,
        targetedNetworkSourceIds: Set<String>? = null,
        baseSnapshot: LibrarySnapshot? = null,
        reuseLocalState: Boolean = false,
        onProgress: ((current: Int, total: Int) -> Unit)? = null,
    ): LibrarySnapshot = scanWithStatus(
        refreshMediaIndex = refreshMediaIndex,
        refreshMediaPaths = refreshMediaPaths,
        enrichMetadata = enrichMetadata,
        mediaStoreGenerationFloor = mediaStoreGenerationFloor,
        targetedSafTreeIds = targetedSafTreeIds,
        targetedNetworkSourceIds = targetedNetworkSourceIds,
        baseSnapshot = baseSnapshot,
        reuseLocalState = reuseLocalState,
        onProgress = onProgress,
    ).snapshot

    internal suspend fun scanWithStatus(
        refreshMediaIndex: Boolean = false,
        refreshMediaPaths: List<String> = emptyList(),
        enrichMetadata: Boolean = true,
        mediaStoreGenerationFloor: Long? = null,
        targetedSafTreeIds: Set<String>? = null,
        targetedNetworkSourceIds: Set<String>? = null,
        baseSnapshot: LibrarySnapshot? = null,
        reuseLocalState: Boolean = false,
        onProgress: ((current: Int, total: Int) -> Unit)? = null,
    ): CoordinatedLibraryScan {
        val baseSources = baseSnapshot?.songs
            ?.let(::partitionBaseSnapshot)
            ?: BaseSnapshotSources.Empty
        val canReuseLocalState = targetedSafTreeIds == null &&
            (targetedNetworkSourceIds != null || reuseLocalState) &&
            baseSnapshot != null &&
            !refreshMediaIndex &&
            refreshMediaPaths.isEmpty() &&
            !enrichMetadata
        val canReuseMediaStoreState = shouldReusePublishedMediaStoreState(
            targetedSafTreeIds = targetedSafTreeIds,
            hasBaseSnapshot = baseSnapshot != null,
            refreshMediaIndex = refreshMediaIndex,
            enrichMetadata = enrichMetadata,
        )
        val canReuseNetworkState = targetedNetworkSourceIds != null &&
            baseSnapshot != null &&
            !refreshMediaIndex &&
            !enrichMetadata
        var isComplete = true
        var incompleteMessage: String? = null
        var retryableSafTreeIds = emptySet<String>()
        val localSongs = if (canReuseLocalState) {
            baseSources.nonNetworkSongs
        } else {
            val localResult = scanLocalSources(
                refreshMediaIndex = refreshMediaIndex,
                refreshMediaPaths = refreshMediaPaths,
                enrichMetadata = enrichMetadata,
                mediaStoreGenerationFloor = mediaStoreGenerationFloor,
                onProgress = onProgress,
                baseSources = baseSources,
                targetedSafTreeIds = targetedSafTreeIds,
                reuseMediaStoreState = canReuseMediaStoreState,
            )
            isComplete = localResult.isComplete
            incompleteMessage = localResult.incompleteMessage
            retryableSafTreeIds = localResult.retryableSafTreeIds
            localResult.songs
        }
        if (networkSources.none(NetworkLibrarySource::enabled)) {
            return CoordinatedLibraryScan(
                snapshot = assembleFinalLibrarySnapshot(localSongs),
                isComplete = isComplete,
                incompleteMessage = incompleteMessage,
                retryableSafTreeIds = retryableSafTreeIds,
            )
        }

        val availableNetworkSources = networkSources.filter {
            it.enabled && it.id !in blockedNetworkSourceIds
        }
        val sourcesToScan = if (targetedNetworkSourceIds == null) {
            availableNetworkSources
        } else if (!canReuseNetworkState) {
            availableNetworkSources
        } else {
            availableNetworkSources.filter { it.id in targetedNetworkSourceIds }
        }
        val sourcesToScanIds = sourcesToScan.mapTo(hashSetOf(), NetworkLibrarySource::id)
        val preservedNetworkSourceIds = networkSources
            .filter { it.enabled && it.id !in sourcesToScanIds }
            .mapTo(hashSetOf(), NetworkLibrarySource::id)
        val existingNetworkSongs = baseSources.networkSongsBySource
            .filterKeys(preservedNetworkSourceIds::contains)
        val networkScan = networkScannerProvider().scanWithStatus(
            sources = sourcesToScan,
            forceRefresh = refreshMediaIndex,
            enrichMetadata = enrichMetadata,
        )
        isComplete = isComplete && networkScan.isComplete &&
            networkSources.none { it.enabled && it.id in blockedNetworkSourceIds }
        val networkSongsBySource = existingNetworkSongs.toMutableMap()
        sourcesToScan.forEach { source -> networkSongsBySource[source.id] = emptyList() }
        networkScan.songs
            .groupBy { song -> NetworkResourceUri.sourceId(song.uri) }
            .forEach { (sourceId, songs) ->
                if (sourceId != null) networkSongsBySource[sourceId] = songs
            }
        return CoordinatedLibraryScan(
            snapshot = assembleFinalLibrarySnapshot(localSongs + networkSongsBySource.values.flatten()),
            isComplete = isComplete,
            incompleteMessage = incompleteMessage,
            retryableSafTreeIds = retryableSafTreeIds,
        )
    }

    private suspend fun scanLocalSources(
        refreshMediaIndex: Boolean,
        refreshMediaPaths: List<String>,
        enrichMetadata: Boolean,
        mediaStoreGenerationFloor: Long?,
        onProgress: ((current: Int, total: Int) -> Unit)?,
        baseSources: BaseSnapshotSources,
        targetedSafTreeIds: Set<String>?,
        reuseMediaStoreState: Boolean,
    ): LocalSourceScanResult {
        val localResult = localScanner.scanSafely(
            refreshMediaIndex = refreshMediaIndex,
            refreshMediaPaths = refreshMediaPaths.takeUnless { targetedSafTreeIds != null }.orEmpty(),
            enrichMetadata = enrichMetadata,
            mediaStoreGenerationFloor = mediaStoreGenerationFloor,
            baseMediaStoreSongs = baseSources.mediaStoreSongs,
            reuseMediaStoreState = reuseMediaStoreState,
            onProgress = onProgress,
        )
        val local = when (localResult) {
            is LocalLibraryScanResult.Complete -> localResult.songs
            is LocalLibraryScanResult.Unavailable -> {
                ScannerDebugLogger.logSourceFailure(localResult.failure)
                baseSources.nonNetworkSongs
            }
        }
        val configuredSafTrees = localScanner.safTreeSelections()
        val configuredSafTreeIds = configuredSafTrees.mapNotNull { safTreeIdentity(it.uri) }.toSet()
        val safSelections = when {
            targetedSafTreeIds != null -> configuredSafTrees.filter { selection ->
                safTreeIdentity(selection.uri) in targetedSafTreeIds
            }
            refreshMediaPaths.isEmpty() -> configuredSafTrees
            else -> configuredSafTrees.filter { selection ->
                shouldScanSafTreeForPaths(selection, refreshMediaPaths)
            }
        }
        val safResults = safScanner.scanByTree(safSelections)
        val safIncomplete = safResults.any { it !is SafTreeScanResult.Complete }
        val retryableSafTreeIds = safResults
            .asSequence()
            .filter { result ->
                result is SafTreeScanResult.Incomplete &&
                    result.failure.reason == SAF_PROVIDER_LOADING_REASON
            }
            .mapNotNull { result -> safTreeIdentity(result.selection.uri) }
            .toSet()
        val currentMediaStoreVolumes = localScanner.currentSyncState()?.volumes
            ?.mapTo(hashSetOf(), LibraryMediaStoreVolumeSyncState::volumeName)
        val preservedDetachedMediaStoreSongs = if (currentMediaStoreVolumes == null) {
            emptyList()
        } else {
            baseSources.mediaStoreSongsByVolume
                .filterKeys { volume -> volume != MediaStore.VOLUME_EXTERNAL && volume !in currentMediaStoreVolumes }
                .values
                .flatten()
        }
        val safSongs = safResults.flatMap { result ->
            when (result) {
                is SafTreeScanResult.Complete -> result.songs
                is SafTreeScanResult.Incomplete -> result.songs
                is SafTreeScanResult.Unavailable -> emptyList()
            }
        }
        val scannedSafTreeIds = safResults
            .mapNotNull { safTreeIdentity(it.selection.uri) }
        val failedSafTreeIds = safResults
            .filter { it !is SafTreeScanResult.Complete }
            .mapNotNull { safTreeIdentity(it.selection.uri) }
            .toSet()
        // An incomplete provider result can contain only the rows available so far. Preserve
        // the last known rows and retry the provider instead of publishing a transient removal.
        val preservedSafTreeIds = failedSafTreeIds + (configuredSafTreeIds - scannedSafTreeIds)
        val preservedSafSongs = baseSources.safSongsByTree
            .filterKeys(preservedSafTreeIds::contains)
            .values
            .flatten()
        val localUnavailable = localResult is LocalLibraryScanResult.Unavailable
        val mergedSongs = LibrarySongDuplicateResolver.mergeMediaStoreAndSafSongs(
            mediaStoreSongs = local + preservedDetachedMediaStoreSongs,
            safSongs = safSongs + preservedSafSongs,
        )
        ScannerDebugLogger.recordSafSummary(
            treeCount = configuredSafTrees.size,
            validGrantCount = safResults.count { result ->
                result !is SafTreeScanResult.Unavailable ||
                    result.failure.operation != "validate-persisted-permission"
            },
            discoveredSongCount = safSongs.size,
            incompleteTreeCount = safResults.count { it !is SafTreeScanResult.Complete },
            providerLoadingTreeCount = safResults.count { result ->
                result is SafTreeScanResult.Incomplete && result.failure.reason == SAF_PROVIDER_LOADING_REASON
            },
            providerErrorTreeCount = safResults.count { result ->
                when (result) {
                    is SafTreeScanResult.Incomplete -> result.failure.reason == "provider cursor error"
                    is SafTreeScanResult.Unavailable -> result.failure.operation == "provider cursor error"
                    is SafTreeScanResult.Complete -> false
                }
            },
            mergedSongCount = mergedSongs.size,
        )
        return LocalSourceScanResult(
            songs = mergedSongs,
            isComplete = !localUnavailable && !safIncomplete && preservedDetachedMediaStoreSongs.isEmpty(),
            incompleteMessage = when {
                localUnavailable -> LibraryFailure.MediaStoreUnavailable.toUserMessage()
                preservedDetachedMediaStoreSongs.isNotEmpty() -> LibraryFailure.MediaStoreUnavailable.toUserMessage()
                safIncomplete -> LibraryFailure.SafProviderFailure(
                    authority = null,
                    operation = "scan-tree",
                    cause = null,
                ).toUserMessage()
                else -> null
            },
            retryableSafTreeIds = retryableSafTreeIds,
        )
    }

    fun scanRoots(): List<File> = localScanner.scanRoots()

    internal fun hasSafSelections(): Boolean = localScanner.hasSafSelections()

    suspend fun findExistingSongIds(songIds: Set<Long>): Set<Long> = localScanner.findExistingSongIds(songIds)

    fun musicDirectory(): File = localScanner.musicDirectory()

    fun refreshMediaIndex() = localScanner.refreshMediaIndex()

    fun refreshMediaIndex(paths: List<String>) = localScanner.refreshMediaIndex(paths)

    val targetExistenceProbe: MediaTargetExistenceProbe
        get() = localScanner.targetExistenceProbe
}

private fun assembleFinalLibrarySnapshot(songs: List<Song>): LibrarySnapshot {
    return ElovaireTrace.section("library_song_sort") {
        ElovaireTrace.section("library_album_build") {
            LibrarySnapshotAssembler.assembleSourceDeduplicated(sortLibrarySongs(songs))
        }
    }
}

internal fun sortLibrarySongs(songs: List<Song>): List<Song> {
    return songs.sortedWith(
        compareByDescending<Song> { it.dateAddedSeconds }
            .thenBy { MediaIdentityResolver.stableKey(it) },
    )
}

internal data class CoordinatedLibraryScan(
    val snapshot: LibrarySnapshot,
    val isComplete: Boolean,
    val incompleteMessage: String? = null,
    val retryableSafTreeIds: Set<String> = emptySet(),
)

internal fun shouldReusePublishedMediaStoreState(
    targetedSafTreeIds: Set<String>?,
    hasBaseSnapshot: Boolean,
    refreshMediaIndex: Boolean,
    enrichMetadata: Boolean,
): Boolean = targetedSafTreeIds != null &&
    hasBaseSnapshot &&
    !refreshMediaIndex &&
    !enrichMetadata

private data class LocalSourceScanResult(
    val songs: List<Song>,
    val isComplete: Boolean,
    val incompleteMessage: String?,
    val retryableSafTreeIds: Set<String> = emptySet(),
)

private data class BaseSnapshotSources(
    val nonNetworkSongs: List<Song>,
    val mediaStoreSongs: List<Song>,
    val mediaStoreSongsByVolume: Map<String, List<Song>>,
    val safSongsByTree: Map<String, List<Song>>,
    val networkSongsBySource: Map<String, List<Song>>,
) {
    companion object {
        val Empty = BaseSnapshotSources(
            nonNetworkSongs = emptyList(),
            mediaStoreSongs = emptyList(),
            mediaStoreSongsByVolume = emptyMap(),
            safSongsByTree = emptyMap(),
            networkSongsBySource = emptyMap(),
        )
    }
}

private fun partitionBaseSnapshot(songs: List<Song>): BaseSnapshotSources {
    val nonNetworkSongs = ArrayList<Song>(songs.size)
    val mediaStoreSongs = ArrayList<Song>()
    val mediaStoreSongsByVolume = linkedMapOf<String, MutableList<Song>>()
    val safSongsByTree = linkedMapOf<String, MutableList<Song>>()
    val networkSongsBySource = linkedMapOf<String, MutableList<Song>>()
    songs.forEach { song ->
        if (NetworkResourceUri.isNetworkUri(song.uri)) {
            NetworkResourceUri.sourceId(song.uri)?.let { sourceId ->
                networkSongsBySource.getOrPut(sourceId, ::mutableListOf).add(song)
            }
            return@forEach
        }
        nonNetworkSongs += song
        when (val source = MediaIdentityResolver.resolve(song)) {
            is MediaSourceIdentity.MediaStoreItem -> {
                mediaStoreSongs += song
                mediaStoreSongsByVolume.getOrPut(source.volumeName, ::mutableListOf).add(song)
            }
            else -> Unit
        }
        safTreeIdentity(song.uri)?.let { treeId ->
            safSongsByTree.getOrPut(treeId, ::mutableListOf).add(song)
        }
    }
    return BaseSnapshotSources(
        nonNetworkSongs = nonNetworkSongs,
        mediaStoreSongs = mediaStoreSongs,
        mediaStoreSongsByVolume = mediaStoreSongsByVolume,
        safSongsByTree = safSongsByTree,
        networkSongsBySource = networkSongsBySource,
    )
}

internal fun shouldScanSafTreeForPaths(
    selection: LibraryFolderSelection,
    requestedPaths: Collection<String>,
): Boolean {
    val root = selection.path
        .takeUnless(LibraryFolderSelectionResolver::isUriBackedPath)
        ?.takeIf(String::isNotBlank)
        ?: return false
    return requestedPaths.any { requestedPath ->
        val normalized = requestedPath.trim()
        normalized.isNotBlank() && LibraryFolderSelectionResolver.isSameOrChildPath(normalized, root)
    }
}

internal fun safTreeIdentity(uri: Uri?): String? {
    if (uri == null || !uri.scheme.equals("content", ignoreCase = true)) return null
    val authority = uri.authority.orEmpty().lowercase(Locale.ROOT)
    val documentId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
    return "$authority|${documentId ?: uri.toString()}"
}

internal fun networkFilterFingerprint(sources: List<NetworkLibrarySource>): String {
    return sources
        .distinctBy(NetworkLibrarySource::id)
        .sortedBy(NetworkLibrarySource::id)
        .joinToString("|") { source ->
            "${source.id}:${NetworkSourceIdentity.locationFingerprint(source)}:${source.enabled}"
        }
}
