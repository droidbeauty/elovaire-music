package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.provider.DocumentsContract
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

/** Composes independent source scanners without making any one source the merge authority. */
internal class LibraryScanCoordinator(
    internal val localScanner: MediaStoreScanner,
    private val safScanner: SafTreeLibraryScanner,
    private val networkScannerProvider: () -> NetworkLibraryScanner,
    private val clock: AppClock = AndroidAppClock,
) {
    private var networkSources: List<NetworkLibrarySource> = emptyList()

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
        if (networkSources.none(NetworkLibrarySource::enabled)) return false
        return networkScannerProvider().needsRefresh(networkSources, clock.wallTimeMs())
    }

    suspend fun scan(
        refreshMediaIndex: Boolean = false,
        refreshMediaPaths: List<String> = emptyList(),
        enrichMetadata: Boolean = true,
        targetedNetworkSourceIds: Set<String>? = null,
        baseSnapshot: LibrarySnapshot? = null,
        onProgress: ((current: Int, total: Int) -> Unit)? = null,
    ): LibrarySnapshot {
        val canReuseLocalState = targetedNetworkSourceIds != null &&
            baseSnapshot != null &&
            !refreshMediaIndex &&
            refreshMediaPaths.isEmpty() &&
            !enrichMetadata
        val localSongs = if (canReuseLocalState) {
            requireNotNull(baseSnapshot).songs.filterNot { NetworkResourceUri.isNetworkUri(it.uri) }
        } else {
            val local = localScanner.scan(
                refreshMediaIndex = refreshMediaIndex,
                refreshMediaPaths = refreshMediaPaths,
                enrichMetadata = enrichMetadata,
                onProgress = onProgress,
            )
            val safResults = safScanner.scanByTree(localScanner.safTreeSelections())
            val safSongs = safResults
                .filterIsInstance<SafTreeScanResult.Complete>()
                .flatMap(SafTreeScanResult.Complete::songs)
            val failedSafTreeIds = safResults
                .filter { it !is SafTreeScanResult.Complete }
                .mapNotNull { safTreeIdentity(it.selection.uri) }
                .toSet()
            val preservedSafSongs = baseSnapshot?.songs.orEmpty().filter { song ->
                safTreeIdentity(song.uri) in failedSafTreeIds
            }
            LibrarySongDuplicateResolver.mergeMediaStoreAndSafSongs(
                mediaStoreSongs = local.songs,
                safSongs = safSongs + preservedSafSongs,
            )
        }
        if (networkSources.none(NetworkLibrarySource::enabled)) {
            return LibrarySnapshotAssembler.assemble(
                localSongs.sortedWith(
                    compareByDescending<Song> { it.dateAddedSeconds }
                        .thenBy { MediaIdentityResolver.stableKey(it) },
                ),
            )
        }

        val activeNetworkSourceIds = networkSources
            .filter(NetworkLibrarySource::enabled)
            .mapTo(hashSetOf(), NetworkLibrarySource::id)
        val existingNetworkSongs = if (canReuseLocalState) {
            requireNotNull(baseSnapshot).songs
                .asSequence()
                .filter { song ->
                    NetworkResourceUri.sourceId(song.uri)?.let(activeNetworkSourceIds::contains) == true
                }
                .groupBy { song -> NetworkResourceUri.sourceId(song.uri)!! }
        } else {
            emptyMap()
        }
        val sourcesToScan = if (targetedNetworkSourceIds == null) {
            networkSources
        } else if (!canReuseLocalState) {
            networkSources
        } else {
            networkSources.filter { it.id in targetedNetworkSourceIds }
        }
        val scannedNetworkSongs = networkScannerProvider().scan(
            sources = sourcesToScan,
            forceRefresh = refreshMediaIndex,
            enrichMetadata = enrichMetadata,
        )
        val networkSongsBySource = existingNetworkSongs.toMutableMap()
        if (targetedNetworkSourceIds == null) networkSongsBySource.clear()
        sourcesToScan.forEach { source -> networkSongsBySource[source.id] = emptyList() }
        scannedNetworkSongs
            .groupBy { song -> NetworkResourceUri.sourceId(song.uri) }
            .forEach { (sourceId, songs) ->
                if (sourceId != null) networkSongsBySource[sourceId] = songs
            }
        return LibrarySnapshotAssembler.assemble(
            (localSongs + networkSongsBySource.values.flatten()).sortedWith(
                compareByDescending<Song> { it.dateAddedSeconds }
                    .thenBy { MediaIdentityResolver.stableKey(it) },
            ),
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

internal fun safTreeIdentity(uri: Uri?): String? {
    if (uri == null || uri.scheme != "content") return null
    return runCatching {
        "${uri.authority.orEmpty()}|${DocumentsContract.getTreeDocumentId(uri)}"
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
