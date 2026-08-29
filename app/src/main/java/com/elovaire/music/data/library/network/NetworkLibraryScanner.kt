package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import android.net.Uri
import com.hierynomus.smbj.common.SMBRuntimeException
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.data.library.isSupportedAudioExtension
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParserException

internal class NetworkLibraryScanner(
    context: Context,
    private val registry: NetworkFileSystemRegistry,
    private val inventory: NetworkInventoryStore,
    private val onAvailabilityChanged: (String, NetworkProbeResult) -> Unit = { _, _ -> },
    private val clock: AppClock = AndroidAppClock,
) {
    private val artworkCache = NetworkArtworkCache(context)
    private val metadataReader = NetworkMetadataReader(registry)

    suspend fun scan(
        sources: List<NetworkLibrarySource>,
        forceRefresh: Boolean = false,
        enrichMetadata: Boolean = true,
    ): List<Song> = scanWithStatus(sources, forceRefresh, enrichMetadata).songs

    suspend fun scanWithStatus(
        sources: List<NetworkLibrarySource>,
        forceRefresh: Boolean = false,
        enrichMetadata: Boolean = true,
    ): NetworkLibraryScanResult = withContext(Dispatchers.IO) {
        val networkScan = BackendResourceRegistry.acquire(BackendResourceKind.ActiveNetworkScan)
        val networkRead = BackendResourceRegistry.acquire(BackendResourceKind.ActiveNetworkRead)
        try {
            val enabledSources = sources.filter(NetworkLibrarySource::enabled)
            val sourceSemaphore = Semaphore(MAX_CONCURRENT_SOURCE_SCANS)
            val metadataSemaphore = Semaphore(MAX_CONCURRENT_METADATA_READS)
            val sourceResults = supervisorScope {
                enabledSources.map { source ->
                    async {
                        sourceSemaphore.withPermit {
                            currentCoroutineContext().ensureActive()
                            scanSourceSafely(
                                source = source,
                                forceRefresh = forceRefresh,
                                enrichMetadata = enrichMetadata,
                                metadataSemaphore = metadataSemaphore,
                            )
                        }
                    }
                }.awaitAll()
            }
            NetworkLibraryScanResult(
                songs = sourceResults.flatMap(NetworkLibrarySourceScanResult::songs),
                isComplete = sourceResults.all(NetworkLibrarySourceScanResult::isComplete),
            )
        } finally {
            networkRead.close()
            networkScan.close()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun scanSourceSafely(
        source: NetworkLibrarySource,
        forceRefresh: Boolean,
        enrichMetadata: Boolean,
        metadataSemaphore: Semaphore,
    ): NetworkLibrarySourceScanResult {
        return try {
            scanSource(source, forceRefresh, enrichMetadata, metadataSemaphore)
        } catch (_: NetworkScanSupersededException) {
            NetworkLibrarySourceScanResult(
                songs = inventory.load(source).map(NetworkInventoryEntry::song),
                isComplete = false,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: IOException) {
            publishSourceFailure(source, failure)
        } catch (failure: SMBRuntimeException) {
            publishSourceFailure(source, failure)
        } catch (failure: SecurityException) {
            publishSourceFailure(source, failure)
        } catch (failure: IllegalArgumentException) {
            publishSourceFailure(source, failure)
        } catch (failure: IllegalStateException) {
            publishSourceFailure(source, failure)
        } catch (failure: XmlPullParserException) {
            publishSourceFailure(source, failure)
        } catch (failure: Exception) {
            publishSourceFailure(source, failure)
        }
    }

    private suspend fun publishSourceFailure(
        source: NetworkLibrarySource,
        failure: Throwable,
    ): NetworkLibrarySourceScanResult {
        val generation = registry.sourceGeneration(source.id)
        val credentials = registry.credentials(source)
        publishAvailability(source, generation, credentials, failure.toProbeResult())
        val songs = runCatching { inventory.load(source) }
            .getOrDefault(emptyList())
            .map(NetworkInventoryEntry::song)
        return NetworkLibrarySourceScanResult(songs, isComplete = false)
    }

    suspend fun needsRefresh(sources: List<NetworkLibrarySource>, nowMs: Long): Boolean {
        return sources.any { it.enabled && !inventory.hasFreshListing(it, nowMs) }
    }

    private suspend fun scanSource(
        source: NetworkLibrarySource,
        forceRefresh: Boolean,
        enrichMetadata: Boolean,
        metadataSemaphore: Semaphore,
    ): NetworkLibrarySourceScanResult {
        val cached = inventory.load(source)
        val sourceGeneration = registry.sourceGeneration(source.id)
        val transportGeneration = registry.networkGeneration()
        if (!isCurrent(source, sourceGeneration, registry.credentials(source), transportGeneration)) {
            return NetworkLibrarySourceScanResult(emptyList(), isComplete = false)
        }
        val hasUnresolvedCachedMetadata = cached.any { !it.song.metadataResolved }
        if (!forceRefresh && inventory.hasFreshListing(source, clock.wallTimeMs())) {
            if (!enrichMetadata || !hasUnresolvedCachedMetadata) {
                return NetworkLibrarySourceScanResult(cached.map(NetworkInventoryEntry::song), isComplete = true)
            }
            return enrichCommittedInventory(source, sourceGeneration, transportGeneration, cached, metadataSemaphore)
        }
        val credentials = registry.credentials(source)
        if (credentials == null) {
            publishAvailability(
                source = source,
                sourceGeneration = sourceGeneration,
                credentials = null,
                result = NetworkProbeResult(NetworkAvailability.AuthenticationRequired),
            )
            return NetworkLibrarySourceScanResult(cached.map(NetworkInventoryEntry::song), isComplete = false)
        }
        val listing = listSourceOrNull(source, sourceGeneration, credentials)
            ?: return NetworkLibrarySourceScanResult(
                cached.map(NetworkInventoryEntry::song),
                isComplete = false,
            )
        val entries = listing.entries
        val incompleteReason = (listing as? NetworkListingResult.Incomplete)?.reason
        // A source edit/removal may have completed while the blocking listing was in flight.
        // Never commit or republish the result for an obsolete configuration.
        if (!isCurrent(source, sourceGeneration, credentials, transportGeneration)) {
            return NetworkLibrarySourceScanResult(emptyList(), isComplete = false)
        }
        publishAvailability(
            source,
            sourceGeneration,
            credentials,
            NetworkProbeResult(
                availability = NetworkAvailability.Available,
                message = incompleteReason?.let { "scan-incomplete:$it" },
            ),
        )
        val audioEntries = entries
            .asSequence()
            .filterNot(NetworkFileEntry::isDirectory)
            .filter { entry -> isSupportedAudioExtension(entry.path.substringAfterLast('.', "")) }
            .map { entry -> entry.copy(path = NetworkPathPolicy.normalizeRelativePath(entry.path)) }
            .distinctBy(NetworkFileEntry::path)
            .toList()
        val cachedByPath = cached.associateBy { it.entry.path }
        val cachedByEntryId = cached
            .mapNotNull { item -> item.entry.sourceEntryId?.let { it to item } }
            .groupBy({ it.first }, { it.second })
        val revisionIndex = buildNetworkRevisionIndex(cached)
        val artworkByDirectory = if (enrichMetadata) {
            entries
                .asSequence()
                .filterNot(NetworkFileEntry::isDirectory)
                .filter(NetworkFileEntry::isSupportedArtwork)
                .groupBy { it.path.substringBeforeLast('/', "") }
                .mapValues { (_, candidates) -> candidates.minBy(NetworkFileEntry::artworkPriority) }
                .entries
                .take(MAX_ARTWORKS_PER_SCAN)
                .associate { (directory, entry) ->
                    ensureCurrent(source, sourceGeneration, credentials, transportGeneration)
                    directory to artworkCache.uriFor(source, entry, registry)
                }
                .also { artworkCache.trim() }
        } else {
            emptyMap()
        }
        val inventoryEntries = buildInventoryEntries(
            source = source,
            audioEntries = audioEntries,
            cachedByPath = cachedByPath,
            cachedByEntryId = cachedByEntryId,
            revisionIndex = revisionIndex,
            artworkByDirectory = artworkByDirectory,
            enrichMetadata = enrichMetadata,
            forceRefresh = forceRefresh,
            metadataSemaphore = metadataSemaphore,
            isCurrent = {
                isCurrent(source, sourceGeneration, credentials, transportGeneration)
            },
        )
        val inventoryChanged = inventoryEntries.size != cached.size || inventoryEntries.any { current ->
            val previous = cachedByPath[current.entry.path]
            previous == null ||
                previous != current
        }
        val nowMs = clock.wallTimeMs()
        if (!isCurrent(source, sourceGeneration, credentials, transportGeneration)) {
            return NetworkLibrarySourceScanResult(emptyList(), isComplete = false)
        }
        if (incompleteReason != null) {
            // A bounded traversal proves only the entries it saw. Preserve the previous
            // inventory for unseen paths and leave its freshness unchanged so a later scan
            // retries the source instead of treating truncation as authoritative deletion.
            return NetworkLibrarySourceScanResult(
                mergePartialNetworkInventory(cached, inventoryEntries),
                isComplete = false,
            )
        }
        if (inventoryChanged) {
            inventory.replace(
                source = source,
                entries = inventoryEntries,
                availability = NetworkAvailability.Available,
                nowMs = nowMs,
            )
        } else {
            inventory.refresh(source, NetworkAvailability.Available, nowMs)
        }
        return NetworkLibrarySourceScanResult(
            inventoryEntries.map(NetworkInventoryEntry::song),
            isComplete = true,
        )
    }

    private suspend fun enrichCommittedInventory(
        source: NetworkLibrarySource,
        sourceGeneration: Long,
        transportGeneration: Long,
        cached: List<NetworkInventoryEntry>,
        metadataSemaphore: Semaphore,
    ): NetworkLibrarySourceScanResult {
        val credentials = registry.credentials(source)
        if (credentials == null) {
            publishAvailability(
                source = source,
                sourceGeneration = sourceGeneration,
                credentials = null,
                result = NetworkProbeResult(NetworkAvailability.AuthenticationRequired),
            )
            return NetworkLibrarySourceScanResult(cached.map(NetworkInventoryEntry::song), isComplete = false)
        }
        if (!isCurrent(source, sourceGeneration, credentials, transportGeneration)) {
            return NetworkLibrarySourceScanResult(emptyList(), isComplete = false)
        }
        val cachedByPath = cached.associateBy { it.entry.path }
        val cachedByEntryId = cached
            .mapNotNull { item -> item.entry.sourceEntryId?.let { it to item } }
            .groupBy({ it.first }, { it.second })
        val enriched = buildInventoryEntries(
            source = source,
            audioEntries = cached.map(NetworkInventoryEntry::entry),
            cachedByPath = cachedByPath,
            cachedByEntryId = cachedByEntryId,
            revisionIndex = emptyMap(),
            artworkByDirectory = emptyMap(),
            enrichMetadata = true,
            forceRefresh = false,
            metadataSemaphore = metadataSemaphore,
            isCurrent = {
                isCurrent(source, sourceGeneration, credentials, transportGeneration)
            },
        )
        if (!isCurrent(source, sourceGeneration, credentials, transportGeneration)) {
            return NetworkLibrarySourceScanResult(emptyList(), isComplete = false)
        }
        if (enriched != cached) {
            inventory.replace(
                source = source,
                entries = enriched,
                availability = NetworkAvailability.Available,
                nowMs = clock.wallTimeMs(),
            )
        }
        return NetworkLibrarySourceScanResult(enriched.map(NetworkInventoryEntry::song), isComplete = true)
    }

    private suspend fun buildInventoryEntries(
        source: NetworkLibrarySource,
        audioEntries: List<NetworkFileEntry>,
        cachedByPath: Map<String, NetworkInventoryEntry>,
        cachedByEntryId: Map<String, List<NetworkInventoryEntry>>,
        revisionIndex: Map<NetworkRevisionKey, NetworkInventoryEntry?>,
        artworkByDirectory: Map<String, Uri?>,
        enrichMetadata: Boolean,
        forceRefresh: Boolean,
        metadataSemaphore: Semaphore,
        isCurrent: () -> Boolean,
    ): List<NetworkInventoryEntry> {
        val buildEntry: suspend (NetworkFileEntry) -> NetworkInventoryEntry = { entry ->
            currentCoroutineContext().ensureActive()
            if (!isCurrent()) throw NetworkScanSupersededException()
            val previous = cachedByPath[entry.path]
                ?: entry.sourceEntryId
                    ?.let { cachedByEntryId[it].orEmpty().singleOrNull() }
                ?: entry.revisionCandidate(revisionIndex)
            if (
                previous != null &&
                    previous.hasSameRevision(entry) &&
                    (!enrichMetadata || previous.song.metadataResolved)
            ) {
                val relocatedSong = previous.song.copy(
                    fileName = entry.path.substringAfterLast('/').ifBlank { "Unknown" },
                    dateModifiedSeconds = entry.modifiedAtMs?.div(1_000L),
                    libraryPath = "network/${source.id}/${entry.path}",
                    uri = NetworkResourceUri.create(source.id, entry.path),
                )
                previous.copy(
                    entry = entry,
                    song = relocatedSong.copy(
                        artUri = artworkByDirectory[entry.path.substringBeforeLast('/', "")] ?: previous.song.artUri,
                    ),
                )
            } else {
                val metadata = if (enrichMetadata) {
                    ElovaireTrace.section("network_metadata_enrichment") {
                        metadataReader.read(source, entry, forceRefresh)
                    }
                } else {
                    null
                }
                if (!isCurrent()) throw NetworkScanSupersededException()
                val provisionalSong = if (metadata == null && previous != null) {
                    previous.song.copy(
                        fileName = entry.path.substringAfterLast('/').ifBlank { "Unknown" },
                        dateModifiedSeconds = entry.modifiedAtMs?.div(1_000L),
                        libraryPath = "network/${source.id}/${entry.path}",
                        uri = NetworkResourceUri.create(source.id, entry.path),
                        metadataResolved = false,
                        artUri = artworkByDirectory[entry.path.substringBeforeLast('/', "")] ?: previous.song.artUri,
                    )
                } else {
                    null
                }
                NetworkInventoryEntry(
                    entry = entry,
                    song = provisionalSong ?: entry.toSong(
                        source = source,
                        artUri = artworkByDirectory[entry.path.substringBeforeLast('/', "")]
                            ?: previous?.song?.artUri,
                        metadata = metadata,
                        preservedSongId = previous?.song?.id,
                    ),
                )
            }
        }
        return if (!enrichMetadata) {
            audioEntries.map { entry -> buildEntry(entry) }
        } else {
            supervisorScope {
                audioEntries.map { entry ->
                    async { metadataSemaphore.withPermit { buildEntry(entry) } }
                }.awaitAll()
            }
        }
    }

    private fun publishAvailability(
        source: NetworkLibrarySource,
        sourceGeneration: Long,
        credentials: NetworkCredentials?,
        result: NetworkProbeResult,
    ) {
        if (!isCurrent(source, sourceGeneration, credentials)) return
        onAvailabilityChanged(source.id, result)
    }

    private fun listSourceOrNull(
        source: NetworkLibrarySource,
        sourceGeneration: Long,
        credentials: NetworkCredentials,
    ): NetworkListingResult? {
        return try {
            ElovaireTrace.section("network_source_list") {
                registry.listBlocking(source, credentials)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: IOException) {
            publishAvailability(source, sourceGeneration, credentials, failure.toProbeResult())
            null
        } catch (failure: SMBRuntimeException) {
            publishAvailability(source, sourceGeneration, credentials, failure.toProbeResult())
            null
        } catch (failure: IllegalArgumentException) {
            publishAvailability(source, sourceGeneration, credentials, failure.toProbeResult())
            null
        } catch (failure: SecurityException) {
            publishAvailability(source, sourceGeneration, credentials, failure.toProbeResult())
            null
        } catch (failure: IllegalStateException) {
            publishAvailability(source, sourceGeneration, credentials, failure.toProbeResult())
            null
        } catch (failure: XmlPullParserException) {
            publishAvailability(source, sourceGeneration, credentials, failure.toProbeResult())
            null
        }
    }

    private fun isCurrentSource(
        source: NetworkLibrarySource,
        sourceGeneration: Long,
    ): Boolean = registry.isCurrent(source, sourceGeneration)

    private fun isCurrent(
        source: NetworkLibrarySource,
        sourceGeneration: Long,
        credentials: NetworkCredentials?,
    ): Boolean = isCurrentSource(source, sourceGeneration) && registry.credentials(source) == credentials

    private fun isCurrent(
        source: NetworkLibrarySource,
        sourceGeneration: Long,
        credentials: NetworkCredentials?,
        transportGeneration: Long,
    ): Boolean = isCurrent(source, sourceGeneration, credentials) &&
        registry.networkGeneration() == transportGeneration

    private fun ensureCurrent(
        source: NetworkLibrarySource,
        sourceGeneration: Long,
        credentials: NetworkCredentials?,
        transportGeneration: Long,
    ) {
        if (!isCurrent(source, sourceGeneration, credentials, transportGeneration)) {
            throw NetworkScanSupersededException()
        }
    }

    private fun NetworkFileEntry.toSong(
        source: NetworkLibrarySource,
        artUri: Uri?,
        metadata: NetworkMetadataReadResult?,
        preservedSongId: Long?,
    ): Song {
        val normalizedPath = NetworkPathPolicy.normalizeRelativePath(path)
        val fileName = normalizedPath.substringAfterLast('/').ifBlank { "Unknown" }
        val filenameTitle = fileName.substringBeforeLast('.').ifBlank { fileName }
        val parent = normalizedPath.substringBeforeLast('/', "")
        val album = parent.substringAfterLast('/').ifBlank { source.name }
        val artistAndTitle = filenameTitle.split(" - ", limit = 2)
        val artist = artistAndTitle.firstOrNull()
            ?.takeIf { artistAndTitle.size == 2 && it.isNotBlank() }
            ?: "Unknown Artist"
        val displayTitle = if (artistAndTitle.size == 2) artistAndTitle[1].trim().ifBlank { filenameTitle } else filenameTitle
        val extension = fileName.substringAfterLast('.', "").uppercase(Locale.ROOT)
        val songId = preservedSongId ?: NetworkSourceIdentity.songId(source.id, normalizedPath, sourceEntryId)
        val resolvedArtist = metadata?.artist?.trim().takeIf { !it.isNullOrBlank() } ?: artist
        val resolvedTitle = metadata?.title?.trim().takeIf { !it.isNullOrBlank() } ?: displayTitle
        val resolvedAlbum = metadata?.album?.trim().takeIf { !it.isNullOrBlank() } ?: album
        return Song(
            id = songId,
            title = resolvedTitle,
            isExplicit = false,
            artist = resolvedArtist,
            album = resolvedAlbum,
            releaseYear = metadata?.releaseYear,
            genre = metadata?.genre?.trim().takeIf { !it.isNullOrBlank() } ?: "Unknown Genre",
            audioFormat = extension,
            audioQuality = null,
            fileName = fileName,
            albumId = NetworkSourceIdentity.songId(source.id, "album:$parent"),
            durationMs = metadata?.durationMs ?: 0L,
            trackNumber = metadata?.trackNumber ?: parseTrackNumber(fileName),
            discNumber = metadata?.discNumber ?: 1,
            dateAddedSeconds = modifiedAtMs?.div(1_000L) ?: 0L,
            dateModifiedSeconds = modifiedAtMs?.div(1_000L),
            libraryPath = "network/${source.id}/$normalizedPath",
            uri = NetworkResourceUri.create(source.id, normalizedPath),
            artUri = artUri,
            metadataResolved = metadata?.succeeded == true,
            albumArtist = metadata?.albumArtist?.trim().takeIf { !it.isNullOrBlank() } ?: resolvedArtist,
            volumeNormalization = null,
        )
    }

    private fun parseTrackNumber(fileName: String): Int {
        return TRACK_PREFIX.find(fileName)?.groupValues?.getOrNull(1)?.toIntOrNull()?.takeIf { it > 0 } ?: 0
    }

    private fun NetworkFileEntry.revisionCandidate(
        index: Map<NetworkRevisionKey, NetworkInventoryEntry?>,
    ): NetworkInventoryEntry? {
        val key = networkRevisionKey() ?: return null
        return index[key]
    }

    private companion object {
        val TRACK_PREFIX = Regex("^(\\d{1,3})[ ._-]")
        const val MAX_ARTWORKS_PER_SCAN = 256
        const val MAX_CONCURRENT_SOURCE_SCANS = 2
        const val MAX_CONCURRENT_METADATA_READS = 2
    }

    private fun Throwable.toProbeResult(): NetworkProbeResult {
        if (this is NetworkLocalNetworkPermissionException) {
            return NetworkProbeResult(NetworkAvailability.LocalNetworkPermissionRequired, this::class.simpleName)
        }
        return NetworkProbeResult(
            availability = remoteIoFailureKind().toNetworkAvailability(),
            message = this::class.simpleName,
        )
    }
}

internal fun mergePartialNetworkInventory(
    cached: List<NetworkInventoryEntry>,
    discovered: List<NetworkInventoryEntry>,
): List<Song> {
    val merged = LinkedHashMap<String, NetworkInventoryEntry>(cached.size + discovered.size)
    cached.forEach { merged[it.entry.path] = it }
    discovered.forEach { merged[it.entry.path] = it }
    return merged.values.map(NetworkInventoryEntry::song)
}

private fun NetworkFileEntry.isSupportedArtwork(): Boolean {
    return path.substringAfterLast('/').lowercase(Locale.ROOT) in
        SUPPORTED_ARTWORK_NAMES
}

private val SUPPORTED_ARTWORK_NAMES = setOf(
    "cover.jpg",
    "cover.jpeg",
    "cover.png",
    "folder.jpg",
    "folder.jpeg",
    "folder.png",
    "front.jpg",
    "front.jpeg",
    "front.png",
)

private fun NetworkFileEntry.artworkPriority(): Int {
    return when (path.substringAfterLast('/').lowercase(Locale.ROOT)) {
        "cover.jpg", "cover.jpeg", "cover.png" -> 0
        "folder.jpg", "folder.jpeg", "folder.png" -> 1
        else -> 2
    }
}

private class NetworkArtworkCache(context: Context) {
    private val directory = context.applicationContext.filesDir.resolve("network_artwork_v1")

    fun uriFor(
        source: NetworkLibrarySource,
        entry: NetworkFileEntry,
        registry: NetworkFileSystemRegistry,
    ): Uri? {
        val extension = entry.path.substringAfterLast('.', "bin").lowercase(Locale.ROOT)
        val target = directory.resolve("${cacheKey(source.id, entry)}.$extension")
        if (target.isFile && target.length() in 1..MAX_ARTWORK_BYTES) return Uri.fromFile(target)
        return try {
            directory.mkdirs()
            val temporary = target.resolveSibling("${target.name}.tmp")
            if (!downloadArtwork(temporary, source, entry, registry)) return null
            ElovaireTrace.section("artwork_disk_commit") {
                if (!temporary.renameTo(target)) {
                    temporary.delete()
                    return null
                }
            }
            Uri.fromFile(target)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            null
        } catch (_: SMBRuntimeException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun trim() {
        val allFiles = directory.listFiles().orEmpty()
        allFiles.filter { file ->
            file.name.endsWith(".tmp") || !file.isFile || file.length() !in 1..MAX_ARTWORK_BYTES
        }.forEach(File::delete)
        val files = allFiles
            .filter { it.isFile && it.length() in 1..MAX_ARTWORK_BYTES }
            .sortedBy(File::lastModified)
        var totalBytes = files.sumOf(File::length)
        var remaining = files.size
        files.forEach { file ->
            if (remaining <= MAX_ARTWORK_FILES && totalBytes <= MAX_ARTWORK_CACHE_BYTES) return@forEach
            val size = file.length()
            if (file.delete()) {
                remaining -= 1
                totalBytes -= size
            }
        }
    }

    private fun downloadArtwork(
        temporary: File,
        source: NetworkLibrarySource,
        entry: NetworkFileEntry,
        registry: NetworkFileSystemRegistry,
    ): Boolean {
        var completed = false
        try {
            registry.openBlocking(
                sourceId = source.id,
                path = entry.path,
                position = 0L,
                length = MAX_ARTWORK_BYTES + 1L,
                purpose = NetworkReadPurpose.Artwork,
            ).use { handle ->
                if (handle.length != null && handle.length > MAX_ARTWORK_BYTES) return false
                FileOutputStream(temporary).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var total = 0L
                    while (true) {
                        val read = handle.input.read(buffer)
                        if (read < 0) break
                        if (read == 0) continue
                        total += read
                        if (total > MAX_ARTWORK_BYTES) return false
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            completed = true
            return true
        } finally {
            if (!completed) temporary.delete()
        }
    }

    private fun cacheKey(sourceId: String, entry: NetworkFileEntry): String {
        val identity = listOf(sourceId, entry.path, entry.sizeBytes, entry.modifiedAtMs, entry.etag).joinToString("|")
        return MessageDigest.getInstance("SHA-256")
            .digest(identity.toByteArray())
            .joinToString("") { byte -> "%02x".format(Locale.ROOT, byte) }
    }

    private companion object {
        const val MAX_ARTWORK_BYTES = 4L * 1024L * 1024L
        const val MAX_ARTWORK_FILES = 256
        const val MAX_ARTWORK_CACHE_BYTES = 128L * 1024L * 1024L
    }
}

internal data class NetworkLibraryScanResult(
    val songs: List<Song>,
    val isComplete: Boolean,
)

private class NetworkScanSupersededException : IOException("Network scan was superseded")

private data class NetworkLibrarySourceScanResult(
    val songs: List<Song>,
    val isComplete: Boolean,
)
