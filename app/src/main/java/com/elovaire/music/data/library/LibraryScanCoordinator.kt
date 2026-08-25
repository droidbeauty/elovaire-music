package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
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
        val sources = networkSources.filter { it.enabled && it.id !in blockedNetworkSourceIds }
        if (sources.isEmpty()) return false
        return networkScannerProvider().needsRefresh(sources, clock.wallTimeMs())
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
        targetedNetworkSourceIds: Set<String>? = null,
        baseSnapshot: LibrarySnapshot? = null,
        onProgress: ((current: Int, total: Int) -> Unit)? = null,
    ): LibrarySnapshot = scanWithStatus(
        refreshMediaIndex = refreshMediaIndex,
        refreshMediaPaths = refreshMediaPaths,
        enrichMetadata = enrichMetadata,
        targetedNetworkSourceIds = targetedNetworkSourceIds,
        baseSnapshot = baseSnapshot,
        onProgress = onProgress,
    ).snapshot

    internal suspend fun scanWithStatus(
        refreshMediaIndex: Boolean = false,
        refreshMediaPaths: List<String> = emptyList(),
        enrichMetadata: Boolean = true,
        targetedNetworkSourceIds: Set<String>? = null,
        baseSnapshot: LibrarySnapshot? = null,
        onProgress: ((current: Int, total: Int) -> Unit)? = null,
    ): CoordinatedLibraryScan {
        val canReuseLocalState = targetedNetworkSourceIds != null &&
            baseSnapshot != null &&
            !refreshMediaIndex &&
            refreshMediaPaths.isEmpty() &&
            !enrichMetadata
        var isComplete = true
        var incompleteMessage: String? = null
        val localSongs = if (canReuseLocalState) {
            requireNotNull(baseSnapshot).songs.filterNot { NetworkResourceUri.isNetworkUri(it.uri) }
        } else {
            val localResult = scanLocalSources(
                refreshMediaIndex = refreshMediaIndex,
                refreshMediaPaths = refreshMediaPaths,
                enrichMetadata = enrichMetadata,
                onProgress = onProgress,
                baseSnapshot = baseSnapshot,
            )
            isComplete = localResult.isComplete
            incompleteMessage = localResult.incompleteMessage
            localResult.songs
        }
        if (networkSources.none(NetworkLibrarySource::enabled)) {
            return CoordinatedLibraryScan(
                snapshot = LibrarySnapshotAssembler.assemble(
                    localSongs.sortedWith(
                        compareByDescending<Song> { it.dateAddedSeconds }
                            .thenBy { MediaIdentityResolver.stableKey(it) },
                    ),
                ),
                isComplete = isComplete,
                incompleteMessage = incompleteMessage,
            )
        }

        val availableNetworkSources = networkSources.filter { it.id !in blockedNetworkSourceIds }
        val sourcesToScan = if (targetedNetworkSourceIds == null) {
            availableNetworkSources
        } else if (!canReuseLocalState) {
            availableNetworkSources
        } else {
            availableNetworkSources.filter { it.id in targetedNetworkSourceIds }
        }
        val sourcesToScanIds = sourcesToScan.mapTo(hashSetOf(), NetworkLibrarySource::id)
        val preservedNetworkSourceIds = networkSources
            .filter { it.enabled && it.id !in sourcesToScanIds }
            .mapTo(hashSetOf(), NetworkLibrarySource::id)
        val existingNetworkSongs = baseSnapshot?.songs
            .orEmpty()
            .asSequence()
            .filter { song ->
                NetworkResourceUri.sourceId(song.uri)?.let(preservedNetworkSourceIds::contains) == true
            }
            .groupBy { song -> NetworkResourceUri.sourceId(song.uri)!! }
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
            snapshot = LibrarySnapshotAssembler.assemble(
                (localSongs + networkSongsBySource.values.flatten()).sortedWith(
                    compareByDescending<Song> { it.dateAddedSeconds }
                        .thenBy { MediaIdentityResolver.stableKey(it) },
                ),
            ),
            isComplete = isComplete,
            incompleteMessage = incompleteMessage,
        )
    }

    private suspend fun scanLocalSources(
        refreshMediaIndex: Boolean,
        refreshMediaPaths: List<String>,
        enrichMetadata: Boolean,
        onProgress: ((current: Int, total: Int) -> Unit)?,
        baseSnapshot: LibrarySnapshot?,
    ): LocalSourceScanResult {
        val localResult = localScanner.scanSafely(
            refreshMediaIndex = refreshMediaIndex,
            refreshMediaPaths = refreshMediaPaths,
            enrichMetadata = enrichMetadata,
            onProgress = onProgress,
        )
        val local = when (localResult) {
            is LocalLibraryScanResult.Complete -> localResult.snapshot
            is LocalLibraryScanResult.Unavailable -> {
                ScannerDebugLogger.logSourceFailure(localResult.failure)
                baseSnapshot?.let { snapshot ->
                    LibrarySnapshotAssembler.assemble(
                        snapshot.songs.filterNot { NetworkResourceUri.isNetworkUri(it.uri) },
                    )
                } ?: LibrarySnapshot(emptyList(), emptyList())
            }
        }
        val configuredSafTrees = localScanner.safTreeSelections()
        val configuredSafTreeIds = configuredSafTrees.mapNotNull { safTreeIdentity(it.uri) }.toSet()
        val safSelections = if (refreshMediaPaths.isEmpty()) {
            configuredSafTrees
        } else {
            configuredSafTrees.filter { selection ->
                shouldScanSafTreeForPaths(selection, refreshMediaPaths)
            }
        }
        val safResults = safScanner.scanByTree(safSelections)
        val safIncomplete = safResults.any { it !is SafTreeScanResult.Complete }
        val currentMediaStoreVolumes = localScanner.currentSyncState()?.volumes
            ?.mapTo(hashSetOf(), LibraryMediaStoreVolumeSyncState::volumeName)
        val preservedDetachedMediaStoreSongs = baseSnapshot?.songs.orEmpty().filter { song ->
            val source = MediaIdentityResolver.resolve(song)
            source is MediaSourceIdentity.MediaStoreItem &&
                source.volumeName != MediaStore.VOLUME_EXTERNAL &&
                currentMediaStoreVolumes != null &&
                source.volumeName !in currentMediaStoreVolumes
        }
        val safSongs = safResults
            .filterIsInstance<SafTreeScanResult.Complete>()
            .flatMap(SafTreeScanResult.Complete::songs)
        val scannedSafTreeIds = safResults
            .mapNotNull { safTreeIdentity(it.selection.uri) }
        val failedSafTreeIds = safResults
            .filter { it !is SafTreeScanResult.Complete }
            .mapNotNull { safTreeIdentity(it.selection.uri) }
            .toSet()
        val preservedSafSongs = baseSnapshot?.songs.orEmpty().filter { song ->
            val treeId = safTreeIdentity(song.uri)
            treeId in failedSafTreeIds || treeId != null &&
                treeId in configuredSafTreeIds && treeId !in scannedSafTreeIds
        }
        val localUnavailable = localResult is LocalLibraryScanResult.Unavailable
        return LocalSourceScanResult(
            songs = LibrarySongDuplicateResolver.mergeMediaStoreAndSafSongs(
                mediaStoreSongs = local.songs + preservedDetachedMediaStoreSongs,
                safSongs = safSongs + preservedSafSongs,
            ),
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
        )
    }

    fun scanRoots(): List<File> = localScanner.scanRoots()

    internal fun hasSafSelections(): Boolean = localScanner.hasSafSelections()

    fun findExistingSongIds(songIds: Set<Long>): Set<Long> = localScanner.findExistingSongIds(songIds)

    fun musicDirectory(): File = localScanner.musicDirectory()

    fun refreshMediaIndex() = localScanner.refreshMediaIndex()

    fun refreshMediaIndex(paths: List<String>) = localScanner.refreshMediaIndex(paths)

    val targetExistenceProbe: MediaTargetExistenceProbe
        get() = localScanner.targetExistenceProbe
}

internal data class CoordinatedLibraryScan(
    val snapshot: LibrarySnapshot,
    val isComplete: Boolean,
    val incompleteMessage: String? = null,
)

private data class LocalSourceScanResult(
    val songs: List<Song>,
    val isComplete: Boolean,
    val incompleteMessage: String?,
)

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
    return runCatching {
        "${uri.authority.orEmpty().lowercase(Locale.ROOT)}|${DocumentsContract.getTreeDocumentId(uri)}"
    }.getOrNull()
}

internal fun networkFilterFingerprint(sources: List<NetworkLibrarySource>): String {
    return sources
        .distinctBy(NetworkLibrarySource::id)
        .sortedBy(NetworkLibrarySource::id)
        .joinToString("|") { source ->
            "${source.id}:${NetworkSourceIdentity.locationFingerprint(source)}:${source.enabled}"
        }
}
