package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import android.net.Uri
import elovaire.music.droidbeauty.app.data.library.db.LibraryDao
import elovaire.music.droidbeauty.app.data.library.db.NetworkInventoryEntity
import elovaire.music.droidbeauty.app.data.library.db.NetworkInventorySourceEntity
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import org.json.JSONObject

/** Durable, source-scoped listing state. A committed generation is authoritative only after listing succeeds. */
internal class NetworkInventoryStore(
    context: Context,
    private val dao: LibraryDao,
) {
    private val legacyCacheFile = context.applicationContext.filesDir.resolve("network_library_cache_v1.json")
    private var legacyRootLoaded = false
    private var legacyRoot: JSONObject? = null

    suspend fun load(source: NetworkLibrarySource): List<NetworkInventoryEntry> {
        migrateLegacyCache(source)
        return dao.networkInventory(source.id).map { it.toInventoryEntry(source) }
    }

    suspend fun hasFreshListing(sourceId: String, nowMs: Long): Boolean {
        val state = dao.networkInventorySource(sourceId) ?: return false
        return state.committedAtMs > 0L && nowMs >= state.committedAtMs &&
            nowMs - state.committedAtMs <= FRESHNESS_WINDOW_MS
    }

    suspend fun replace(
        source: NetworkLibrarySource,
        entries: List<NetworkInventoryEntry>,
        availability: NetworkAvailability,
        nowMs: Long,
    ) {
        val previous = dao.networkInventorySource(source.id)
        val generation = (previous?.generation ?: 0L).coerceAtMost(Long.MAX_VALUE - 1L) + 1L
        dao.replaceNetworkInventory(
            source = NetworkInventorySourceEntity(
                sourceId = source.id,
                generation = generation,
                committedAtMs = nowMs,
                availability = availability.name,
            ),
            entries = entries.map { it.toEntity(source.id, generation) },
        )
    }

    suspend fun refresh(sourceId: String, availability: NetworkAvailability, nowMs: Long) {
        dao.refreshNetworkInventorySource(sourceId, nowMs, availability.name)
    }

    suspend fun remove(sourceId: String) {
        dao.deleteNetworkInventory(sourceId)
        dao.deleteNetworkInventorySource(sourceId)
    }

    private suspend fun migrateLegacyCache(source: NetworkLibrarySource) {
        if (dao.networkInventorySource(source.id) != null) return
        val entries = legacyEntries(source)
        if (entries.isEmpty()) return
        val committedAtMs = legacyRoot
            ?.optJSONObject("lastSuccessfulScan")
            ?.optLong(source.id, 0L)
            ?.takeIf { it > 0L }
            ?: 0L
        dao.replaceNetworkInventory(
            source = NetworkInventorySourceEntity(
                sourceId = source.id,
                generation = 1L,
                committedAtMs = committedAtMs,
                availability = NetworkAvailability.Available.name,
            ),
            entries = entries.map { it.toEntity(source.id, 1L) },
        )
    }

    private fun legacyEntries(source: NetworkLibrarySource): List<NetworkInventoryEntry> {
        if (!legacyRootLoaded) {
            legacyRootLoaded = true
            legacyRoot = runCatching {
                JSONObject(legacyCacheFile.takeIf(File::isFile)?.readText() ?: return@runCatching null)
            }.getOrNull()
        }
        val songs = legacyRoot?.optJSONObject("sources")?.optJSONArray(source.id) ?: return emptyList()
        return buildList {
            repeat(songs.length()) { index ->
                val item = songs.optJSONObject(index) ?: return@repeat
                val uri = runCatching { Uri.parse(item.optString("uri")) }.getOrNull() ?: return@repeat
                val path = NetworkResourceUri.path(uri)
                    ?: item.optString("libraryPath")
                        .removePrefix("network/${source.id}/")
                        .takeIf(String::isNotBlank)
                    ?: return@repeat
                if (!NetworkResourceUri.isNetworkUri(uri)) return@repeat
                val normalizedPath = NetworkPathPolicy.normalizeRelativePath(path)
                val song = Song(
                    id = item.optLong("id"),
                    title = item.optString("title"),
                    isExplicit = false,
                    artist = item.optString("artist"),
                    album = item.optString("album"),
                    releaseYear = null,
                    genre = item.optString("genre", "Unknown Genre"),
                    audioFormat = item.optString("audioFormat"),
                    audioQuality = null,
                    fileName = item.optString("fileName").ifBlank { normalizedPath.substringAfterLast('/') },
                    albumId = item.optLong("albumId"),
                    durationMs = item.optLong("durationMs"),
                    trackNumber = item.optInt("trackNumber"),
                    discNumber = item.optInt("discNumber", 1),
                    dateAddedSeconds = item.optLong("dateAddedSeconds"),
                    dateModifiedSeconds = item.optLong("dateModifiedSeconds").takeIf { it > 0L },
                    libraryPath = "network/${source.id}/$normalizedPath",
                    uri = NetworkResourceUri.create(source.id, normalizedPath),
                    artUri = item.optString("artUri").takeIf(String::isNotBlank)?.let(Uri::parse),
                    metadataResolved = item.optBoolean("metadataResolved", true),
                    albumArtist = item.optString("albumArtist").takeIf(String::isNotBlank),
                    volumeNormalization = null,
                )
                if (song.id != 0L) {
                    add(
                        NetworkInventoryEntry(
                            entry = NetworkFileEntry(path = normalizedPath, isDirectory = false),
                            song = song,
                        ),
                    )
                }
            }
        }
    }

    private companion object {
        const val FRESHNESS_WINDOW_MS = 15 * 60 * 1_000L
    }
}

internal data class NetworkInventoryEntry(
    val entry: NetworkFileEntry,
    val song: Song,
) {
    fun hasSameRevision(other: NetworkFileEntry): Boolean {
        val hasStrongRevision = entry.sizeBytes != null || entry.modifiedAtMs != null || entry.etag != null
        return hasStrongRevision &&
            entry.sizeBytes == other.sizeBytes &&
            entry.modifiedAtMs == other.modifiedAtMs &&
            entry.etag == other.etag
    }

    internal fun toEntity(sourceId: String, generation: Long): NetworkInventoryEntity {
        return NetworkInventoryEntity(
            sourceId = sourceId,
            relativePath = entry.path,
            sizeBytes = entry.sizeBytes,
            modifiedAtMs = entry.modifiedAtMs,
            etag = entry.etag,
            contentType = entry.contentType,
            sourceEntryId = entry.sourceEntryId,
            songId = song.id,
            albumId = song.albumId,
            title = song.title,
            artist = song.artist,
            album = song.album,
            albumArtist = song.albumArtist,
            releaseYear = song.releaseYear,
            genre = song.genre,
            audioFormat = song.audioFormat,
            audioQuality = song.audioQuality,
            durationMs = song.durationMs,
            trackNumber = song.trackNumber,
            discNumber = song.discNumber,
            dateAddedSeconds = song.dateAddedSeconds,
            dateModifiedSeconds = song.dateModifiedSeconds,
            metadataResolved = song.metadataResolved,
            artUri = song.artUri?.toString(),
            lastSeenGeneration = generation,
        )
    }
}

private fun NetworkInventoryEntity.toInventoryEntry(source: NetworkLibrarySource): NetworkInventoryEntry {
    val normalizedPath = NetworkPathPolicy.normalizeRelativePath(relativePath)
    return NetworkInventoryEntry(
        entry = NetworkFileEntry(
            path = normalizedPath,
            isDirectory = false,
            sizeBytes = sizeBytes,
            modifiedAtMs = modifiedAtMs,
            contentType = contentType,
            etag = etag,
            sourceEntryId = sourceEntryId,
        ),
        song = Song(
            id = songId,
            title = title,
            isExplicit = false,
            artist = artist,
            album = album,
            releaseYear = releaseYear,
            genre = genre,
            audioFormat = audioFormat,
            audioQuality = audioQuality,
            fileName = normalizedPath.substringAfterLast('/').ifBlank { "Unknown" },
            albumId = albumId,
            durationMs = durationMs,
            trackNumber = trackNumber,
            discNumber = discNumber,
            dateAddedSeconds = dateAddedSeconds,
            dateModifiedSeconds = dateModifiedSeconds,
            libraryPath = "network/${source.id}/$normalizedPath",
            uri = NetworkResourceUri.create(source.id, normalizedPath),
            artUri = artUri?.takeIf(String::isNotBlank)?.let(Uri::parse),
            metadataResolved = metadataResolved,
            albumArtist = albumArtist,
            volumeNormalization = null,
        ),
    )
}
