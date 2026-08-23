package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import android.net.Uri
import com.hierynomus.smbj.common.SMBRuntimeException
import elovaire.music.droidbeauty.app.data.library.isSupportedAudioExtension
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

internal class NetworkLibraryScanner(
    context: Context,
    private val registry: NetworkFileSystemRegistry,
    private val inventory: NetworkInventoryStore,
    private val onAvailabilityChanged: (String, NetworkProbeResult) -> Unit = { _, _ -> },
) {
    private val artworkCache = NetworkArtworkCache(context)
    private val metadataReader = NetworkMetadataReader(registry)

    suspend fun scan(
        sources: List<NetworkLibrarySource>,
        forceRefresh: Boolean = false,
    ): List<Song> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Song>()
        sources.filter(NetworkLibrarySource::enabled).forEach { source ->
            currentCoroutineContext().ensureActive()
            result += scanSource(source, forceRefresh)
        }
        result
    }

    suspend fun needsRefresh(sources: List<NetworkLibrarySource>, nowMs: Long): Boolean {
        return sources.any { it.enabled && !inventory.hasFreshListing(it.id, nowMs) }
    }

    private suspend fun scanSource(source: NetworkLibrarySource, forceRefresh: Boolean): List<Song> {
        val cached = inventory.load(source)
        if (!forceRefresh && inventory.hasFreshListing(source.id, System.currentTimeMillis())) {
            return cached.map(NetworkInventoryEntry::song)
        }
        val credentials = registry.credentials(source)
        if (credentials == null) {
            onAvailabilityChanged(
                source.id,
                NetworkProbeResult(NetworkAvailability.AuthenticationRequired),
            )
            return cached.map(NetworkInventoryEntry::song)
        }
        val entries = try {
            registry.listBlocking(source, credentials)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: IOException) {
            onAvailabilityChanged(source.id, failure.toProbeResult())
            return cached.map(NetworkInventoryEntry::song)
        } catch (failure: SMBRuntimeException) {
            onAvailabilityChanged(source.id, failure.toProbeResult())
            return cached.map(NetworkInventoryEntry::song)
        } catch (failure: IllegalArgumentException) {
            onAvailabilityChanged(source.id, failure.toProbeResult())
            return cached.map(NetworkInventoryEntry::song)
        }
        // A source edit/removal may have completed while the blocking listing was in flight.
        // Never commit or republish the result for an obsolete configuration.
        if (registry.source(source.id) != source || registry.credentials(source) != credentials) return emptyList()
        onAvailabilityChanged(source.id, NetworkProbeResult(NetworkAvailability.Available))
        val audioEntries = entries
            .asSequence()
            .filterNot(NetworkFileEntry::isDirectory)
            .filter { entry -> isSupportedAudioExtension(entry.path.substringAfterLast('.', "")) }
            .map { entry -> entry.copy(path = NetworkPathPolicy.normalizeRelativePath(entry.path)) }
            .distinctBy(NetworkFileEntry::path)
            .toList()
        val artworkByDirectory = entries
            .asSequence()
            .filterNot(NetworkFileEntry::isDirectory)
            .filter(NetworkFileEntry::isSupportedArtwork)
            .groupBy { it.path.substringBeforeLast('/', "") }
            .mapValues { (_, candidates) -> candidates.minBy(NetworkFileEntry::artworkPriority) }
            .entries
            .take(MAX_ARTWORKS_PER_SCAN)
            .associate { (directory, entry) ->
                directory to artworkCache.uriFor(source, entry, registry)
            }
        artworkCache.trim()
        val cachedByPath = cached.associateBy { it.entry.path }
        val cachedByEntryId = cached
            .mapNotNull { item -> item.entry.sourceEntryId?.let { it to item } }
            .groupBy({ it.first }, { it.second })
        val inventoryEntries = audioEntries.map { entry ->
            val previous = cachedByPath[entry.path]
                ?: entry.sourceEntryId
                    ?.let { cachedByEntryId[it].orEmpty().singleOrNull() }
                ?: entry.revisionCandidate(cached)
            if (previous != null && previous.hasSameRevision(entry)) {
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
                val metadata = metadataReader.read(source, entry)
                NetworkInventoryEntry(
                    entry = entry,
                    song = entry.toSong(
                        source = source,
                        artUri = artworkByDirectory[entry.path.substringBeforeLast('/', "")],
                        metadata = metadata,
                        preservedSongId = previous?.song?.id,
                    ),
                )
            }
        }
        val inventoryChanged = inventoryEntries.size != cached.size || inventoryEntries.any { current ->
            val previous = cachedByPath[current.entry.path]
            previous == null ||
                !previous.hasSameRevision(current.entry) ||
                previous.song.artUri != current.song.artUri
        }
        val nowMs = System.currentTimeMillis()
        if (inventoryChanged) {
            inventory.replace(
                source = source,
                entries = inventoryEntries,
                availability = NetworkAvailability.Available,
                nowMs = nowMs,
            )
        } else {
            inventory.refresh(source.id, NetworkAvailability.Available, nowMs)
        }
        return inventoryEntries.map(NetworkInventoryEntry::song)
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
        cached: List<NetworkInventoryEntry>,
    ): NetworkInventoryEntry? {
        if (sizeBytes == null || etag.isNullOrBlank()) return null
        return cached.filter { previous ->
            previous.entry.sizeBytes == sizeBytes && previous.entry.etag == etag
        }.singleOrNull()
    }

    private companion object {
        val TRACK_PREFIX = Regex("^(\\d{1,3})[ ._-]")
        const val MAX_ARTWORKS_PER_SCAN = 256
    }

    private fun Throwable.toProbeResult(): NetworkProbeResult {
        val message = message.orEmpty()
        if (message.contains("STATUS_LOGON_FAILURE") || message.contains("STATUS_ACCESS_DENIED")) {
            return NetworkProbeResult(NetworkAvailability.AuthenticationRequired, this::class.simpleName)
        }
        val offline = this is java.net.UnknownHostException ||
            this is java.net.ConnectException ||
            this is java.net.SocketTimeoutException
        return NetworkProbeResult(
            availability = if (offline) NetworkAvailability.Offline else NetworkAvailability.Unavailable,
            message = this::class.simpleName,
        )
    }
}

private fun NetworkFileEntry.isSupportedArtwork(): Boolean {
    return path.substringAfterLast('/').lowercase(Locale.ROOT) in setOf(
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
}

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
            if (!temporary.renameTo(target)) {
                temporary.delete()
                return null
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
        val files = directory.listFiles()
            ?.filter { it.isFile && it.length() in 1..MAX_ARTWORK_BYTES }
            ?.sortedBy(File::lastModified)
            .orEmpty()
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
            registry.openBlocking(source.id, entry.path, 0L, MAX_ARTWORK_BYTES + 1L).use { handle ->
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
