package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.provider.DocumentsContract
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale

/** Resolves duplicate scan results while preserving MediaStore as the preferred source. */
internal object LibrarySongDuplicateResolver {
    internal enum class DuplicateConfidence {
        SameSource,
        ProvenSameContent,
        ProbableDuplicate,
        Unrelated,
    }

    internal data class DuplicateEvidence(
        val confidence: DuplicateConfidence,
        val logicalTrackId: LogicalTrackId?,
    )

    fun mergeMediaStoreAndSafSongs(
        mediaStoreSongs: List<Song>,
        safSongs: List<Song>,
    ): List<Song> {
        val acceptedMediaStoreSongs = dedupeByStrongIdentity(mediaStoreSongs)
        if (safSongs.isEmpty()) return acceptedMediaStoreSongs

        val acceptedSafSongs = dedupeByStrongIdentity(safSongs)
        if (acceptedMediaStoreSongs.isEmpty()) return acceptedSafSongs

        val accepted = ArrayList<Song>(acceptedMediaStoreSongs.size + acceptedSafSongs.size)
        accepted += acceptedMediaStoreSongs

        acceptedSafSongs.forEach { safSong ->
            val duplicate = acceptedMediaStoreSongs.any { mediaSong ->
                duplicateEvidence(mediaSong, safSong).confidence in setOf(
                    DuplicateConfidence.SameSource,
                    DuplicateConfidence.ProvenSameContent,
                )
            }
            if (!duplicate) {
                accepted += safSong
            }
        }
        return accepted
    }

    internal fun duplicateEvidence(left: Song, right: Song): DuplicateEvidence {
        val leftSource = MediaIdentityResolver.resolve(left)
        val rightSource = MediaIdentityResolver.resolve(right)
        val sameSource = leftSource?.stableKey != null && leftSource.stableKey == rightSource?.stableKey
        val sameUri = left.uri.toString().trim().lowercase(Locale.ROOT).let { leftUri ->
            leftUri.isNotBlank() && leftUri == right.uri.toString().trim().lowercase(Locale.ROOT)
        }
        if (sameSource || sameUri) {
            return DuplicateEvidence(DuplicateConfidence.SameSource, MediaIdentityResolver.logicalTrackId(left))
        }
        val leftPath = normalizedRealPath(left.libraryPath)
        val rightPath = normalizedRealPath(right.libraryPath)
        if (leftPath != null && leftPath == rightPath) {
            return DuplicateEvidence(DuplicateConfidence.ProvenSameContent, MediaIdentityResolver.logicalTrackId(left))
        }
        val probable = left.durationMs > 0L &&
            left.durationMs == right.durationMs &&
            left.audioFormat.equals(right.audioFormat, ignoreCase = true) &&
            left.fileName.equals(right.fileName, ignoreCase = true)
        return DuplicateEvidence(
            confidence = if (probable) DuplicateConfidence.ProbableDuplicate else DuplicateConfidence.Unrelated,
            logicalTrackId = null,
        )
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
