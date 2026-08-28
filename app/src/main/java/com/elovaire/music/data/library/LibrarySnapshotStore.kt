package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.net.Uri
import android.util.AtomicFile
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationMetadata
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale
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
    private val snapshotFile = allowStrictModeDiskReads {
        // Snapshot loading is explicit and off-main; construction only resolves the app-private file.
        appContext.filesDir.resolve(SNAPSHOT_FILE_NAME)
    }
    private val atomicFile = AtomicFile(snapshotFile)
    private val snapshotLock = Any()
    private var lastSavedContentRevision: String? = null
    private var lastSavedFilterFingerprint: String? = null
    private var lastSavedSyncState: LibraryMediaStoreSyncState? = null

    fun load(): CachedLibrarySnapshot? = synchronized(snapshotLock) {
        try {
            val serialized = atomicFile.openRead().use { input ->
                input.readBytes().toString(StandardCharsets.UTF_8)
            }
            val root = JSONObject(serialized)
            if (root.optInt("version", 0) != SNAPSHOT_VERSION) {
                discardSnapshot()
                return@synchronized null
            }

            val signature = LibrarySignature(
                songCount = root.optInt("songCount", 0),
                newestDateAddedSeconds = root.optLong("newestDateAddedSeconds", 0L),
                idChecksum = root.optLong("idChecksum", 0L),
                filterFingerprint = root.optString("filterFingerprint"),
            )
            val syncState = root.optJSONObject("mediaStoreSyncState")?.toLibraryMediaStoreSyncState()
            val decodedSongs = buildList {
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
            }.filter(::isValidSnapshotSong)
            if (!isLibrarySignatureValid(signature, decodedSongs)) {
                discardSnapshot()
                return@synchronized null
            }
            val loadedSongs = decodedSongs
            val songs = LibrarySongDuplicateResolver.dedupeLoadedSnapshotSongs(loadedSongs)
            val filteredSignature = signatureFromSongs(
                songs = songs,
                filterFingerprint = signature.filterFingerprint,
            )

            val cachedSnapshot = CachedLibrarySnapshot(
                snapshot = LibrarySnapshotAssembler.assemble(songs),
                signature = if (songs.size == signature.songCount) signature else filteredSignature,
                syncState = syncState,
            )
            if (songs.size != loadedSongs.size) {
                save(
                    snapshot = cachedSnapshot.snapshot,
                    filterFingerprint = cachedSnapshot.signature.filterFingerprint,
                    syncState = cachedSnapshot.syncState,
                )
            }
            lastSavedContentRevision = librarySnapshotContentRevision(
                snapshot = cachedSnapshot.snapshot,
                filterFingerprint = cachedSnapshot.signature.filterFingerprint,
                syncState = cachedSnapshot.syncState,
            )
            lastSavedFilterFingerprint = cachedSnapshot.signature.filterFingerprint
            lastSavedSyncState = cachedSnapshot.syncState
            cachedSnapshot
        } catch (_: Exception) {
            discardSnapshot()
            null
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun save(
        snapshot: LibrarySnapshot,
        filterFingerprint: String,
        syncState: LibraryMediaStoreSyncState? = null,
    ) = synchronized(snapshotLock) {
        // Scans already apply this predicate. Keep the defensive filter for untrusted callers,
        // but avoid allocating a second full song list on the normal save path.
        val songs = if (snapshot.songs.all(::isSupportedLibrarySong)) {
            snapshot.songs
        } else {
            snapshot.songs.filter(::isSupportedLibrarySong)
        }
        val contentRevision = librarySnapshotContentRevision(
            snapshot = if (songs.size == snapshot.songs.size) {
                snapshot.copy(songs = songs)
            } else {
                LibrarySnapshotAssembler.assemble(songs)
            },
            filterFingerprint = filterFingerprint,
            syncState = syncState,
        )
        if (
            lastSavedContentRevision == contentRevision &&
            lastSavedFilterFingerprint == filterFingerprint &&
            lastSavedSyncState == syncState
        ) {
            return@synchronized
        }
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
        val output = atomicFile.startWrite()
        try {
            output.write(serializedSnapshot.toByteArray(StandardCharsets.UTF_8))
            output.flush()
            atomicFile.finishWrite(output)
            lastSavedContentRevision = contentRevision
            lastSavedFilterFingerprint = filterFingerprint
            lastSavedSyncState = syncState
        } catch (failure: Throwable) {
            atomicFile.failWrite(output)
            throw failure
        }
    }

    private companion object {
        const val SNAPSHOT_FILE_NAME = "library_snapshot_v7.json"
        const val SNAPSHOT_VERSION = 7
    }

    private fun discardSnapshot() {
        lastSavedContentRevision = null
        lastSavedFilterFingerprint = null
        lastSavedSyncState = null
        try {
            atomicFile.delete()
        } catch (_: Exception) {
            // A corrupt snapshot is already unusable; a later scan can replace it.
        }
    }
}

internal fun libraryContentRevision(
    songs: List<Song>,
    filterFingerprint: String,
    syncState: LibraryMediaStoreSyncState?,
): String {
    val digest = MessageDigest.getInstance("SHA-256")

    digest.appendRevisionValue(filterFingerprint)
    digest.appendRevisionValue(syncState?.filterFingerprint)
    syncState?.volumes?.forEach { volume ->
        digest.appendRevisionValue(volume.volumeName)
        digest.appendRevisionValue(volume.version)
        digest.appendRevisionValue(volume.generation)
    }
    songs.forEach { song -> digest.appendSongRevision(song) }

    return digest.digest().toHexString()
}

internal fun librarySnapshotContentRevision(
    snapshot: LibrarySnapshot,
    filterFingerprint: String,
    syncState: LibraryMediaStoreSyncState?,
): String {
    if (snapshot.contentRevision.isBlank()) {
        return libraryContentRevision(snapshot.songs, filterFingerprint, syncState)
    }
    val digest = MessageDigest.getInstance("SHA-256")
    digest.appendRevisionValue(filterFingerprint)
    digest.appendRevisionValue(syncState?.filterFingerprint)
    syncState?.volumes?.forEach { volume ->
        digest.appendRevisionValue(volume.volumeName)
        digest.appendRevisionValue(volume.version)
        digest.appendRevisionValue(volume.generation)
    }
    digest.appendRevisionValue(snapshot.contentRevision)
    return digest.digest().toHexString()
}

internal fun librarySongsContentRevision(songs: List<Song>): String {
    val digest = MessageDigest.getInstance("SHA-256")
    songs.forEach { song -> digest.appendSongRevision(song) }
    return digest.digest().toHexString()
}

internal fun libraryPatchedContentRevision(
    previousRevision: String,
    patches: List<LibrarySongPatch>,
): String {
    if (patches.isEmpty()) return previousRevision
    val digest = MessageDigest.getInstance("SHA-256")
    digest.appendRevisionValue(previousRevision)
    patches
        .sortedBy { MediaIdentityResolver.stableKey(it.after) }
        .forEach { patch ->
            digest.appendRevisionValue(MediaIdentityResolver.stableKey(patch.before))
            digest.appendSongRevision(patch.after)
        }
    return digest.digest().toHexString()
}

internal fun libraryIndexContentRevision(
    snapshot: LibrarySnapshot,
    filterFingerprint: String,
    source: String,
): String {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.appendRevisionValue(filterFingerprint)
    digest.appendRevisionValue(source)
    if (snapshot.contentRevision.isNotBlank()) {
        digest.appendRevisionValue(snapshot.contentRevision)
        return digest.digest().toHexString()
    }
    snapshot.songs.forEach { song -> digest.appendSongRevision(song) }
    snapshot.albums.forEach { album ->
        digest.appendRevisionValue(album.id)
        digest.appendRevisionValue(album.title)
        digest.appendRevisionValue(album.artist)
        digest.appendRevisionValue(album.artUri?.toString().orEmpty())
        digest.appendRevisionValue(album.songCount)
        digest.appendRevisionValue(album.durationMs)
        album.songs.forEach { song -> digest.appendRevisionValue(song.id) }
    }
    return digest.digest().toHexString()
}

private fun MessageDigest.appendSongRevision(song: Song) {
    appendRevisionValue(song.id)
    appendRevisionValue(song.title)
    appendRevisionValue(song.isExplicit)
    appendRevisionValue(song.artist)
    appendRevisionValue(song.albumArtist.orEmpty())
    appendRevisionValue(song.album)
    appendRevisionValue(song.releaseYear ?: 0)
    appendRevisionValue(song.genre)
    appendRevisionValue(song.audioFormat)
    appendRevisionValue(song.audioQuality.orEmpty())
    appendRevisionValue(song.fileName)
    appendRevisionValue(song.albumId)
    appendRevisionValue(song.durationMs)
    appendRevisionValue(song.trackNumber)
    appendRevisionValue(song.discNumber)
    appendRevisionValue(song.dateAddedSeconds)
    appendRevisionValue(song.dateModifiedSeconds ?: 0L)
    appendRevisionValue(song.libraryPath.orEmpty())
    appendRevisionValue(song.uri)
    appendRevisionValue(song.artUri?.toString().orEmpty())
    appendRevisionValue(song.metadataResolved)
    appendRevisionValue(song.volumeNormalization?.trackGainDb)
    appendRevisionValue(song.volumeNormalization?.albumGainDb)
    appendRevisionValue(song.volumeNormalization?.trackPeak)
    appendRevisionValue(song.volumeNormalization?.albumPeak)
}

private fun MessageDigest.appendRevisionValue(value: Any?) {
    update(value?.toString()?.toByteArray(StandardCharsets.UTF_8) ?: NULL_VALUE)
    update(VALUE_SEPARATOR)
}

private fun ByteArray.toHexString(): String {
    val digits = "0123456789abcdef"
    return buildString(size * 2) {
        this@toHexString.forEach { byte ->
            val value = byte.toInt() and 0xFF
            append(digits[value ushr 4])
            append(digits[value and 0x0F])
        }
    }
}

private val VALUE_SEPARATOR = byteArrayOf(0)
private val NULL_VALUE = byteArrayOf(1)

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

internal fun isLibrarySignatureValid(
    signature: LibrarySignature,
    songs: List<Song>,
): Boolean {
    return signature == signatureFromSongs(songs, signature.filterFingerprint)
}

internal fun isValidSnapshotSong(song: Song): Boolean {
    val uriScheme = song.uri.scheme
    return song.id != 0L &&
        (song.durationMs > 0L || uriScheme.equals("elovaire-network", ignoreCase = true)) &&
        (uriScheme == "content" || uriScheme == "file" || uriScheme.equals("elovaire-network", ignoreCase = true)) &&
        isSupportedLibrarySong(song)
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
                { it.artist.lowercase(Locale.ROOT) },
                { it.title.lowercase(Locale.ROOT) },
            ),
        )
}
