package elovaire.music.app.data.library

import android.content.Context
import android.net.Uri
import elovaire.music.app.domain.model.Album
import elovaire.music.app.domain.model.LibrarySnapshot
import elovaire.music.app.domain.model.Song
import org.json.JSONArray
import org.json.JSONObject

internal data class LibrarySignature(
    val songCount: Int,
    val newestDateAddedSeconds: Long,
    val idChecksum: Long,
)

internal data class CachedLibrarySnapshot(
    val snapshot: LibrarySnapshot,
    val signature: LibrarySignature,
)

internal class LibrarySnapshotStore(
    appContext: Context,
) {
    private val snapshotFile = appContext.filesDir.resolve(SNAPSHOT_FILE_NAME)

    fun load(): CachedLibrarySnapshot? {
        if (!snapshotFile.exists()) return null

        return runCatching {
            val root = JSONObject(snapshotFile.readText())
            if (root.optInt("version", 0) != SNAPSHOT_VERSION) return null

            val signature = LibrarySignature(
                songCount = root.optInt("songCount", 0),
                newestDateAddedSeconds = root.optLong("newestDateAddedSeconds", 0L),
                idChecksum = root.optLong("idChecksum", 0L),
            )
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
                            uri = Uri.parse(songJson.optString("uri")),
                            artUri = songJson.optString("artUri").takeIf { it.isNotBlank() }?.let(Uri::parse),
                        ),
                    )
                }
            }

            CachedLibrarySnapshot(
                snapshot = LibrarySnapshot(
                    songs = songs,
                    albums = buildAlbumsFromSongs(songs),
                ),
                signature = signature,
            )
        }.getOrNull()
    }

    fun save(snapshot: LibrarySnapshot) {
        runCatching {
            val signature = signatureFromSongs(snapshot.songs)
            val root = JSONObject().apply {
                put("version", SNAPSHOT_VERSION)
                put("songCount", signature.songCount)
                put("newestDateAddedSeconds", signature.newestDateAddedSeconds)
                put("idChecksum", signature.idChecksum)
                put(
                    "songs",
                    JSONArray().apply {
                        snapshot.songs.forEach { song ->
                            put(
                                JSONObject().apply {
                                    put("id", song.id)
                                    put("title", song.title)
                                    put("isExplicit", song.isExplicit)
                                    put("artist", song.artist)
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
                                    put("uri", song.uri.toString())
                                    put("artUri", song.artUri?.toString().orEmpty())
                                },
                            )
                        }
                    },
                )
            }
            snapshotFile.writeText(root.toString())
        }
    }

    fun clear() {
        if (snapshotFile.exists()) {
            snapshotFile.delete()
        }
    }

    private companion object {
        const val SNAPSHOT_FILE_NAME = "library_snapshot_v2.json"
        const val SNAPSHOT_VERSION = 2
    }
}

internal fun signatureFromSongs(songs: List<Song>): LibrarySignature {
    return LibrarySignature(
        songCount = songs.size,
        newestDateAddedSeconds = songs.maxOfOrNull(Song::dateAddedSeconds) ?: 0L,
        idChecksum = songs.fold(0L) { acc, song ->
            acc xor (song.id shl 1) xor song.dateAddedSeconds
        },
    )
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
                artist = firstSong.artist,
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

