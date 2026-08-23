package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibraryScanner
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySource
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.util.Locale

/** Composes independent source scanners without making any one source the merge authority. */
internal class LibraryScanCoordinator(
    internal val localScanner: MediaStoreScanner,
    private val safScanner: SafTreeLibraryScanner,
    private val networkScannerProvider: () -> NetworkLibraryScanner,
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

    fun setLibraryFolders(selections: List<LibraryFolderSelection>): Boolean =
        localScanner.setLibraryFolders(selections)

    fun currentFilterFingerprint(): String {
        val remote = networkSources.joinToString("|") { source ->
            listOf(source.id, source.protocol.name, source.server, source.shareOrPath, source.username)
                .joinToString("@")
                .lowercase(Locale.ROOT)
        }
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
        return networkScannerProvider().needsRefresh(networkSources, System.currentTimeMillis())
    }

    suspend fun scan(
        refreshMediaIndex: Boolean = false,
        refreshMediaPaths: List<String> = emptyList(),
        enrichMetadata: Boolean = true,
        onProgress: ((current: Int, total: Int) -> Unit)? = null,
    ): LibrarySnapshot {
        val local = localScanner.scan(
            refreshMediaIndex = refreshMediaIndex,
            refreshMediaPaths = refreshMediaPaths,
            enrichMetadata = enrichMetadata,
            onProgress = onProgress,
        )
        val safSongs = safScanner.scan(localScanner.safTreeSelections())
        val localSongs = LibrarySongDuplicateResolver.mergeMediaStoreAndSafSongs(
            mediaStoreSongs = local.songs,
            safSongs = safSongs,
        )
        if (networkSources.none(NetworkLibrarySource::enabled)) {
            return LibrarySnapshotAssembler.assemble(
                localSongs.sortedWith(
                    compareByDescending<Song> { it.dateAddedSeconds }
                        .thenBy { MediaIdentityResolver.stableKey(it) },
                ),
            )
        }

        val networkSongs = networkScannerProvider().scan(
            sources = networkSources,
            forceRefresh = refreshMediaIndex,
        )
        return LibrarySnapshotAssembler.assemble(
            (localSongs + networkSongs).sortedWith(
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
