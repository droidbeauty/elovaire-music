package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.provider.DocumentsContract
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale

/** Resolves duplicate scan results while preserving MediaStore as the preferred source. */
internal object LibrarySongDuplicateResolver {
    fun mergeMediaStoreAndSafSongs(
        mediaStoreSongs: List<Song>,
        safSongs: List<Song>,
    ): List<Song> {
        val acceptedMediaStoreSongs = dedupeByStrongIdentity(mediaStoreSongs)
        if (safSongs.isEmpty()) return acceptedMediaStoreSongs

        val acceptedSafSongs = dedupeByStrongIdentity(safSongs)
        if (acceptedMediaStoreSongs.isEmpty()) return acceptedSafSongs

        val mediaStrongKeys = acceptedMediaStoreSongs.flatMapTo(linkedSetOf(), ::strongKeys)
        val mediaByFallbackKey = acceptedMediaStoreSongs
            .flatMap { mediaSong -> fallbackKeys(mediaSong).map { key -> key to mediaSong } }
            .groupBy({ it.first }, { it.second })
        val accepted = ArrayList<Song>(acceptedMediaStoreSongs.size + acceptedSafSongs.size)
        accepted += acceptedMediaStoreSongs

        acceptedSafSongs.forEach { safSong ->
            val strongDuplicate = strongKeys(safSong).any { it in mediaStrongKeys }
            val fallbackDuplicate = fallbackKeys(safSong).any { key ->
                mediaByFallbackKey[key].orEmpty().any { mediaSong ->
                    normalizedRealPath(safSong.libraryPath) == null ||
                        normalizedRealPath(mediaSong.libraryPath) == null
                }
            }
            if (!strongDuplicate && !fallbackDuplicate) {
                accepted += safSong
            }
        }
        return accepted
    }

    fun dedupeLoadedSnapshotSongs(songs: List<Song>): List<Song> {
        val mediaStoreSongs = songs.filter { it.id > 0L }
        val safSongs = songs.filterNot { it.id > 0L }
        return mergeMediaStoreAndSafSongs(mediaStoreSongs, safSongs)
    }

    internal fun strongKeys(song: Song): Set<String> = buildSet {
        add(MediaIdentityResolver.stableKey(song))
        normalizedRealPath(song.libraryPath)?.let { add("path:$it") }
        song.uri.toString()
            .trim()
            .lowercase(Locale.ROOT)
            .takeIf { it.isNotBlank() }
            ?.let { add("uri:$it") }
        if (song.id < 0L) {
            safDocumentIdentity(song.uri)?.let { add("saf-document:$it") }
        }
    }

    internal fun fallbackKeys(song: Song): Set<String> = buildSet {
        val fileName = song.fileName.normalizedIdentityPart() ?: return@buildSet
        val title = song.title.normalizedIdentityPart() ?: return@buildSet
        val artist = song.artist.normalizedIdentityPart() ?: return@buildSet
        val album = song.album.normalizedIdentityPart().orEmpty()
        if (song.durationMs > 0L) {
            add("metadata:$fileName|${song.durationMs}|$title|$artist|$album")
        }
    }

    internal fun normalizedRealPath(path: String?): String? {
        val value = path
            ?.trim()
            ?.replace('\\', '/')
            ?.trimEnd('/')
            ?.takeIf { it.isNotBlank() }
            ?: return null
        if (value.startsWith("saf/", ignoreCase = true) ||
            value.startsWith("content://", ignoreCase = true)
        ) {
            return null
        }
        return value.lowercase(Locale.ROOT)
    }

    internal fun safDocumentIdentity(uri: Uri): String? {
        if (uri.scheme != "content") return null
        val documentId = runCatching { DocumentsContract.getDocumentId(uri) }.getOrNull()
        return MediaIdentityResolver.safDocument(uri.authority, documentId)?.stableKey
    }

    internal fun documentIdentity(
        authority: String?,
        documentId: String?,
    ): String? {
        return MediaIdentityResolver.safDocument(authority, documentId)?.stableKey
    }

    private fun dedupeByStrongIdentity(songs: List<Song>): List<Song> {
        val accepted = ArrayList<Song>(songs.size)
        val keys = linkedSetOf<String>()
        songs.forEach { song ->
            val songKeys = strongKeys(song)
            if (songKeys.isEmpty() || songKeys.none { it in keys }) {
                accepted += song
                keys += songKeys
            }
        }
        return accepted
    }
}

private fun String?.normalizedIdentityPart(): String? {
    return this?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotBlank() }
}
