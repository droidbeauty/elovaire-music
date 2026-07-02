package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationMetadata
import org.json.JSONArray
import org.json.JSONObject

internal data class LibrarySignature(
    val songCount: Int,
    val newestDateAddedSeconds: Long,
    val idChecksum: Long,
    val filterFingerprint: String = "",
)

internal data class CachedLibrarySnapshot(
    val snapshot: LibrarySnapshot,
    val signature: LibrarySignature,
    val syncState: LibraryMediaStoreSyncState?,
)

internal class LibrarySnapshotStore(
    appContext: Context,
) {
    private val snapshotFile = appContext.filesDir.resolve(SNAPSHOT_FILE_NAME)
    @Volatile
    private var lastSerializedSnapshot: String? = null

    fun load(): CachedLibrarySnapshot? {
        if (!snapshotFile.exists()) return null

        return runCatching {
            val serialized = snapshotFile.readText()
            lastSerializedSnapshot = serialized
            val root = JSONObject(serialized)
            if (root.optInt("version", 0) != SNAPSHOT_VERSION) return null

            val signature = LibrarySignature(
                songCount = root.optInt("songCount", 0),
                newestDateAddedSeconds = root.optLong("newestDateAddedSeconds", 0L),
                idChecksum = root.optLong("idChecksum", 0L),
                filterFingerprint = root.optString("filterFingerprint"),
            )
            val syncState = root.optJSONObject("mediaStoreSyncState")?.toLibraryMediaStoreSyncState()
            val songs = buildList {
                val songsArray = root.optJSONArray("songs") ?: JSONArray()
                repeat(songsArray.length()) { index ->
                    val songJson = songsArray.optJSONObject(index) ?: return@repeat
                    add(
                        Song(
                            id = songJson.optLong("id"),
                            title = songJson.optString("title"),
                            isExplicit = songJson.optBoolean("isExplicit"),
                            artist = songJson.optString("artist"),
                            albumArtist = songJson.optString("albumArtist").takeIf { it.isNotBlank() },
                            album = songJson.optString("album"),
                            releaseYear = songJson.optInt("releaseYear").takeIf { it > 0 },
                            genre = songJson.optString("genre"),
                            audioFormat = songJson.optString("audioFormat"),
                            audioQuality = songJson.optString("audioQuality").takeIf { it.isNotBlank() },
                            fileName = songJson.optString("fileName"),
                            albumId = songJson.optLong("albumId"),
                            durationMs = songJson.optLong("durationMs"),
                            trackNumber = songJson.optInt("trackNumber"),
                            discNumber = songJson.optInt("discNumber", 1).coerceAtLeast(1),
                            dateAddedSeconds = songJson.optLong("dateAddedSeconds"),
                            dateModifiedSeconds = songJson.optLong("dateModifiedSeconds")
                                .takeIf { it > 0L },
                            libraryPath = songJson.optString("libraryPath").takeIf { it.isNotBlank() },
                            uri = Uri.parse(songJson.optString("uri")),
                            artUri = songJson.optString("artUri").takeIf { it.isNotBlank() }?.let(Uri::parse),
                            metadataResolved = songJson.optBoolean("metadataResolved", false),
                            volumeNormalization = songJson.optJSONObject("volumeNormalization")?.toVolumeNormalizationMetadata(),
                        ),
                    )
                }
            }.filter(::isSupportedLibrarySong)
            val filteredSignature = signatureFromSongs(
                songs = songs,
                filterFingerprint = signature.filterFingerprint,
            )

            CachedLibrarySnapshot(
                snapshot = LibrarySnapshot(
                    songs = songs,
                    albums = buildAlbumsFromSongs(songs),
                ),
                signature = if (songs.size == signature.songCount) signature else filteredSignature,
                syncState = syncState,
            )
        }.getOrNull()
    }

    fun save(
        snapshot: LibrarySnapshot,
        filterFingerprint: String,
        syncState: LibraryMediaStoreSyncState? = null,
    ) {
        runCatching {
            val songs = snapshot.songs.filter(::isSupportedLibrarySong)
            val signature = signatureFromSongs(
                songs = songs,
                filterFingerprint = filterFingerprint,
            )
            val serializedSnapshot = JSONObject().apply {
                put("version", SNAPSHOT_VERSION)
                put("songCount", signature.songCount)
                put("newestDateAddedSeconds", signature.newestDateAddedSeconds)
                put("idChecksum", signature.idChecksum)
                put("filterFingerprint", signature.filterFingerprint)
                syncState?.let { put("mediaStoreSyncState", it.toJson()) }
                put(
                    "songs",
                    JSONArray().apply {
                        songs.forEach { song ->
                            put(
                                JSONObject().apply {
                                    put("id", song.id)
                                    put("title", song.title)
                                    put("isExplicit", song.isExplicit)
                                    put("artist", song.artist)
                                    put("albumArtist", song.albumArtist.orEmpty())
                                    put("album", song.album)
                                    put("releaseYear", song.releaseYear ?: 0)
                                    put("genre", song.genre)
                                    put("audioFormat", song.audioFormat)
                                    put("audioQuality", song.audioQuality.orEmpty())
                                    put("fileName", song.fileName)
                                    put("albumId", song.albumId)
                                    put("durationMs", song.durationMs)
                                    put("trackNumber", song.trackNumber)
                                    put("discNumber", song.discNumber)
                                    put("dateAddedSeconds", song.dateAddedSeconds)
                                    put("dateModifiedSeconds", song.dateModifiedSeconds ?: 0L)
                                    put("libraryPath", song.libraryPath.orEmpty())
                                    put("uri", song.uri.toString())
                                    put("artUri", song.artUri?.toString().orEmpty())
                                    put("metadataResolved", song.metadataResolved)
                                    song.volumeNormalization
                                        ?.toJson()
                                        ?.takeIf { it.length() > 0 }
                                        ?.let { put("volumeNormalization", it) }
                                },
                            )
                        }
                    },
                )
            }.toString()
            if (lastSerializedSnapshot == serializedSnapshot) return

            val tempFile = snapshotFile.resolveSibling("${snapshotFile.name}.tmp")
            tempFile.writeText(serializedSnapshot)
            if (!tempFile.renameTo(snapshotFile)) {
                snapshotFile.writeText(serializedSnapshot)
                tempFile.delete()
            }
            lastSerializedSnapshot = serializedSnapshot
        }
    }

    private companion object {
        const val SNAPSHOT_FILE_NAME = "library_snapshot_v7.json"
        const val SNAPSHOT_VERSION = 7
    }
}

private fun LibraryMediaStoreSyncState.toJson(): JSONObject {
    return JSONObject().apply {
        put("filterFingerprint", filterFingerprint)
        put(
            "volumes",
            JSONArray().apply {
                volumes.forEach { volume ->
                    put(
                        JSONObject().apply {
                            put("volumeName", volume.volumeName)
                            put("version", volume.version)
                            put("generation", volume.generation)
                        },
                    )
                }
            },
        )
    }
}

private fun VolumeNormalizationMetadata.toJson(): JSONObject {
    return JSONObject().apply {
        trackGainDb?.takeIf(Float::isFinite)?.let { put("trackGainDb", it.toDouble()) }
        albumGainDb?.takeIf(Float::isFinite)?.let { put("albumGainDb", it.toDouble()) }
        trackPeak?.takeIf(Float::isFinite)?.let { put("trackPeak", it.toDouble()) }
        albumPeak?.takeIf(Float::isFinite)?.let { put("albumPeak", it.toDouble()) }
    }
}

private fun JSONObject.toVolumeNormalizationMetadata(): VolumeNormalizationMetadata {
    fun optionalFloat(name: String): Float? {
        return takeIf { has(name) && !isNull(name) }
            ?.optDouble(name)
            ?.let(::finiteFloatOrNull)
    }
    return VolumeNormalizationMetadata(
        trackGainDb = optionalFloat("trackGainDb"),
        albumGainDb = optionalFloat("albumGainDb"),
        trackPeak = optionalFloat("trackPeak"),
        albumPeak = optionalFloat("albumPeak"),
    )
}

internal fun finiteFloatOrNull(value: Double): Float? {
    return value.takeIf(Double::isFinite)?.toFloat()
}

private fun JSONObject.toLibraryMediaStoreSyncState(): LibraryMediaStoreSyncState {
    val volumesArray = optJSONArray("volumes") ?: JSONArray()
    return LibraryMediaStoreSyncState(
        filterFingerprint = optString("filterFingerprint"),
        volumes = buildList {
            repeat(volumesArray.length()) { index ->
                val volume = volumesArray.optJSONObject(index) ?: return@repeat
                val volumeName = volume.optString("volumeName").takeIf { it.isNotBlank() } ?: return@repeat
                val version = volume.optString("version").takeIf { it.isNotBlank() } ?: return@repeat
                add(
                    LibraryMediaStoreVolumeSyncState(
                        volumeName = volumeName,
                        version = version,
                        generation = volume.optLong("generation", -1L),
                    ),
                )
            }
        },
    )
}

internal fun signatureFromSongs(
    songs: List<Song>,
    filterFingerprint: String = "",
): LibrarySignature {
    return LibrarySignature(
        songCount = songs.size,
        newestDateAddedSeconds = songs.maxOfOrNull(Song::dateAddedSeconds) ?: 0L,
        idChecksum = songs.fold(0L) { acc, song ->
            acc xor songSignatureChecksum(
                id = song.id,
                dateAddedSeconds = song.dateAddedSeconds,
                dateModifiedSeconds = song.dateModifiedSeconds,
            )
        },
        filterFingerprint = filterFingerprint,
    )
}

internal fun songSignatureChecksum(
    id: Long,
    dateAddedSeconds: Long,
    dateModifiedSeconds: Long?,
): Long {
    val modified = dateModifiedSeconds ?: 0L
    return (id shl 1) xor dateAddedSeconds xor (modified shl 7)
}

internal fun buildAlbumsFromSongs(
    songs: List<Song>,
): List<Album> {
    return songs
        .groupBy { it.albumId }
        .values
        .map { albumSongs ->
            val sortedSongs = sortAlbumSongs(albumSongs)
            val firstSong = sortedSongs.first()
            Album(
                id = firstSong.albumId,
                title = firstSong.album,
                artist = firstSong.albumArtist?.takeIf { it.isNotBlank() } ?: firstSong.artist,
                artUri = firstSong.artUri,
                songCount = sortedSongs.size,
                durationMs = sortedSongs.sumOf { it.durationMs },
                songs = sortedSongs,
            )
        }
        .sortedWith(
            compareBy(
                { it.artist.lowercase() },
                { it.title.lowercase() },
            ),
        )
}
