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
import org.json.JSONArray
import org.json.JSONObject

internal class NetworkLibraryScanner(
    context: Context,
    private val registry: NetworkFileSystemRegistry,
    private val onAvailabilityChanged: (String, NetworkProbeResult) -> Unit = { _, _ -> },
) {
    private val cache = NetworkLibraryCacheStore(context)
    private val artworkCache = NetworkArtworkCache(context)

    suspend fun scan(
        sources: List<NetworkLibrarySource>,
        forceRefresh: Boolean = false,
    ): List<Song> = withContext(Dispatchers.IO) {
        buildList {
            sources.filter(NetworkLibrarySource::enabled).forEach { source ->
                currentCoroutineContext().ensureActive()
                addAll(scanSource(source, forceRefresh))
            }
        }
    }

    fun needsRefresh(sources: List<NetworkLibrarySource>, nowMs: Long): Boolean {
        return sources.any { it.enabled && !cache.hasFreshListing(it.id, nowMs) }
    }

    private fun scanSource(source: NetworkLibrarySource, forceRefresh: Boolean): List<Song> {
        if (!forceRefresh && cache.hasFreshListing(source.id, System.currentTimeMillis())) {
            return cache.load(source.id)
        }
        val credentials = registry.credentials(source)
        if (credentials == null) {
            onAvailabilityChanged(
                source.id,
                NetworkProbeResult(NetworkAvailability.AuthenticationRequired),
            )
            return cache.load(source.id)
        }
        val entries = try {
            registry.listBlocking(source, credentials)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: IOException) {
            onAvailabilityChanged(source.id, failure.toProbeResult())
            return cache.load(source.id)
        } catch (failure: SMBRuntimeException) {
            onAvailabilityChanged(source.id, failure.toProbeResult())
            return cache.load(source.id)
        } catch (failure: IllegalArgumentException) {
            onAvailabilityChanged(source.id, failure.toProbeResult())
            return cache.load(source.id)
        }
        onAvailabilityChanged(source.id, NetworkProbeResult(NetworkAvailability.Available))
        val audioEntries = entries
            .asSequence()
            .filterNot(NetworkFileEntry::isDirectory)
            .filter { entry -> isSupportedAudioExtension(entry.path.substringAfterLast('.', "")) }
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
        val songs = audioEntries.map { entry ->
            entry.toSong(
                source = source,
                artUri = artworkByDirectory[entry.path.substringBeforeLast('/', "")],
            )
        }
        cache.save(source.id, songs)
        return songs
    }

    private fun NetworkFileEntry.toSong(source: NetworkLibrarySource, artUri: Uri?): Song {
        val normalizedPath = NetworkPathPolicy.normalizeRelativePath(path)
        val fileName = normalizedPath.substringAfterLast('/').ifBlank { "Unknown" }
        val title = fileName.substringBeforeLast('.').ifBlank { fileName }
        val parent = normalizedPath.substringBeforeLast('/', "")
        val album = parent.substringAfterLast('/').ifBlank { source.name }
        val artistAndTitle = title.split(" - ", limit = 2)
        val artist = artistAndTitle.firstOrNull()
            ?.takeIf { artistAndTitle.size == 2 && it.isNotBlank() }
            ?: "Unknown Artist"
        val displayTitle = if (artistAndTitle.size == 2) artistAndTitle[1].trim().ifBlank { title } else title
        val extension = fileName.substringAfterLast('.', "").uppercase(Locale.ROOT)
        val songId = NetworkSourceIdentity.songId(source.id, normalizedPath)
        return Song(
            id = songId,
            title = displayTitle,
            isExplicit = false,
            artist = artist,
            album = album,
            releaseYear = null,
            genre = "Unknown Genre",
            audioFormat = extension,
            audioQuality = null,
            fileName = fileName,
            albumId = NetworkSourceIdentity.songId(source.id, "album:$album"),
            durationMs = 0L,
            trackNumber = parseTrackNumber(fileName),
            discNumber = 1,
            dateAddedSeconds = modifiedAtMs?.div(1_000L) ?: 0L,
            dateModifiedSeconds = modifiedAtMs?.div(1_000L),
            libraryPath = "network/${source.id}/$normalizedPath",
            uri = NetworkResourceUri.create(source.id, normalizedPath),
            artUri = artUri,
            metadataResolved = true,
            albumArtist = artist,
            volumeNormalization = null,
        )
    }

    private fun parseTrackNumber(fileName: String): Int {
        return TRACK_PREFIX.find(fileName)?.groupValues?.getOrNull(1)?.toIntOrNull()?.takeIf { it > 0 } ?: 0
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

private class NetworkLibraryCacheStore(context: Context) {
    private val file = context.applicationContext.filesDir.resolve("network_library_cache_v1.json")

    @Synchronized
    fun load(sourceId: String): List<Song> {
        val root = runCatching { JSONObject(file.takeIf(File::isFile)?.readText() ?: return emptyList()) }.getOrNull()
            ?: return emptyList()
        if (root.optInt("version", 0) != CACHE_VERSION) return emptyList()
        val songs = root.optJSONObject("sources")?.optJSONArray(sourceId) ?: return emptyList()
        return buildList {
            repeat(songs.length()) { index ->
                val item = songs.optJSONObject(index) ?: return@repeat
                val uri = runCatching { Uri.parse(item.optString("uri")) }.getOrNull() ?: return@repeat
                if (!NetworkResourceUri.isNetworkUri(uri)) return@repeat
                add(
                    Song(
                        id = item.optLong("id"),
                        title = item.optString("title"),
                        isExplicit = false,
                        artist = item.optString("artist"),
                        album = item.optString("album"),
                        releaseYear = null,
                        genre = item.optString("genre", "Unknown Genre"),
                        audioFormat = item.optString("audioFormat"),
                        audioQuality = null,
                        fileName = item.optString("fileName"),
                        albumId = item.optLong("albumId"),
                        durationMs = item.optLong("durationMs"),
                        trackNumber = item.optInt("trackNumber"),
                        discNumber = item.optInt("discNumber", 1),
                        dateAddedSeconds = item.optLong("dateAddedSeconds"),
                        dateModifiedSeconds = item.optLong("dateModifiedSeconds").takeIf { it > 0L },
                        libraryPath = item.optString("libraryPath").takeIf(String::isNotBlank),
                        uri = uri,
                        artUri = item.optString("artUri").takeIf(String::isNotBlank)?.let(Uri::parse),
                        metadataResolved = true,
                        albumArtist = item.optString("albumArtist").takeIf(String::isNotBlank),
                        volumeNormalization = null,
                    ),
                )
            }
        }.filter { it.id != 0L && it.fileName.isNotBlank() }
    }

    @Synchronized
    fun hasFreshListing(sourceId: String, nowMs: Long): Boolean {
        val root = runCatching {
            JSONObject(file.takeIf(File::isFile)?.readText() ?: return false)
        }.getOrNull() ?: return false
        if (root.optInt("version", 0) != CACHE_VERSION) return false
        val lastSuccessfulScan = root.optJSONObject("lastSuccessfulScan")?.optLong(sourceId, 0L) ?: 0L
        return lastSuccessfulScan > 0L && nowMs - lastSuccessfulScan in 0..NETWORK_SCAN_CACHE_TTL_MS
    }

    @Synchronized
    fun save(sourceId: String, songs: List<Song>) {
        val root = runCatching { JSONObject(file.takeIf(File::isFile)?.readText() ?: "{}") }.getOrElse { JSONObject() }
        root.put("version", CACHE_VERSION)
        val sources = root.optJSONObject("sources") ?: JSONObject().also { root.put("sources", it) }
        val lastSuccessfulScan = root.optJSONObject("lastSuccessfulScan")
            ?: JSONObject().also { root.put("lastSuccessfulScan", it) }
        lastSuccessfulScan.put(sourceId, System.currentTimeMillis())
        val array = JSONArray()
        songs.forEach { song ->
            array.put(
                JSONObject()
                    .put("id", song.id)
                    .put("title", song.title)
                    .put("artist", song.artist)
                    .put("album", song.album)
                    .put("genre", song.genre)
                    .put("audioFormat", song.audioFormat)
                    .put("fileName", song.fileName)
                    .put("albumId", song.albumId)
                    .put("durationMs", song.durationMs)
                    .put("trackNumber", song.trackNumber)
                    .put("discNumber", song.discNumber)
                    .put("dateAddedSeconds", song.dateAddedSeconds)
                    .put("dateModifiedSeconds", song.dateModifiedSeconds ?: 0L)
                    .put("libraryPath", song.libraryPath.orEmpty())
                    .put("uri", song.uri.toString())
                    .put("artUri", song.artUri?.toString().orEmpty())
                    .put("albumArtist", song.albumArtist.orEmpty()),
            )
        }
        sources.put(sourceId, array)
        val temporary = file.resolveSibling("${file.name}.tmp")
        temporary.writeText(root.toString())
        if (!temporary.renameTo(file)) {
            temporary.delete()
            throw IllegalStateException("Unable to persist network library cache")
        }
    }

    private companion object {
        const val CACHE_VERSION = 1
        const val NETWORK_SCAN_CACHE_TTL_MS = 15 * 60 * 1_000L
    }
}
