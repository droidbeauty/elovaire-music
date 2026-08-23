package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.provider.DocumentsContract
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.data.library.network.NetworkPathPolicy
import elovaire.music.droidbeauty.app.data.library.network.NetworkResourceUri
import java.util.Locale

internal sealed interface MediaSourceIdentity {
    val stableKey: String

    data class MediaStoreItem(
        val volumeName: String,
        val mediaId: Long,
    ) : MediaSourceIdentity {
        override val stableKey: String = "mediastore:$volumeName:$mediaId"
    }

    data class SafDocument(
        val authority: String,
        val documentId: String,
        val treeId: String?,
    ) : MediaSourceIdentity {
        override val stableKey: String = "saf:$authority:$documentId"
    }

    data class DirectFile(
        val canonicalPath: String,
    ) : MediaSourceIdentity {
        override val stableKey: String = "file:$canonicalPath"
    }

    data class NetworkFile(
        val sourceId: String,
        val relativePath: String,
    ) : MediaSourceIdentity {
        override val stableKey: String = "network:$sourceId:${NetworkPathPolicy.normalizeRelativePath(relativePath)}"
    }
}

internal data class MediaRevision(
    val modifiedAtMs: Long?,
    val sizeBytes: Long?,
    val providerGeneration: Long?,
    val metadataRevision: Long,
) {
    val stableKey: String = listOf(
        modifiedAtMs.orEmptyRevisionPart(),
        sizeBytes.orEmptyRevisionPart(),
        providerGeneration.orEmptyRevisionPart(),
        metadataRevision.toString(),
    ).joinToString(":")
}

@JvmInline
internal value class LogicalTrackId(val value: Long)

internal enum class MediaSourceAvailability {
    Available,
    Unavailable,
    Unknown,
}

internal data class MediaSource(
    val identity: MediaSourceIdentity,
    val uri: Uri,
    val revision: MediaRevision,
    val availability: MediaSourceAvailability = MediaSourceAvailability.Unknown,
)

/** A logical track keeps user-facing identity stable while its physical locator can change. */
internal data class LogicalTrack(
    val id: LogicalTrackId,
    val canonicalSong: Song,
    val preferredSource: MediaSource,
    val sources: List<MediaSource>,
)

internal object MediaIdentityResolver {
    fun mediaStore(volumeName: String?, mediaId: Long?): MediaSourceIdentity.MediaStoreItem? {
        val volume = volumeName.normalizedIdentityPart() ?: return null
        val id = mediaId?.takeIf { it >= 0L } ?: return null
        return MediaSourceIdentity.MediaStoreItem(volume, id)
    }

    fun safDocument(
        authority: String?,
        documentId: String?,
        treeId: String? = null,
    ): MediaSourceIdentity.SafDocument? {
        val normalizedAuthority = authority.normalizedIdentityPart() ?: return null
        val normalizedDocumentId = documentId.normalizedIdentityPart() ?: return null
        return MediaSourceIdentity.SafDocument(
            authority = normalizedAuthority,
            documentId = normalizedDocumentId,
            treeId = treeId.normalizedIdentityPart(),
        )
    }

    fun directFile(path: String?): MediaSourceIdentity.DirectFile? {
        val normalizedPath = LibrarySongDuplicateResolver.normalizedRealPath(path) ?: return null
        return MediaSourceIdentity.DirectFile(normalizedPath)
    }

    fun resolve(song: Song): MediaSourceIdentity? {
        if (NetworkResourceUri.isNetworkUri(song.uri)) {
            val sourceId = NetworkResourceUri.sourceId(song.uri)
            val path = NetworkResourceUri.path(song.uri)
            if (sourceId != null && path != null) return MediaSourceIdentity.NetworkFile(sourceId, path)
        }
        resolveContentUri(song.uri)?.let { return it }
        if (song.uri.scheme.equals("content", ignoreCase = true)) return null
        return directFile(song.libraryPath ?: song.uri.path)
    }

    fun logicalTrackId(song: Song): LogicalTrackId? {
        return song.id.takeIf { it != 0L }?.let(::LogicalTrackId)
    }

    fun source(
        song: Song,
        sizeBytes: Long? = null,
        providerGeneration: Long? = null,
        availability: MediaSourceAvailability = MediaSourceAvailability.Unknown,
    ): MediaSource? {
        return resolve(song)?.let { identity ->
            MediaSource(
                identity = identity,
                uri = song.uri,
                revision = revision(song, sizeBytes, providerGeneration),
                availability = availability,
            )
        }
    }

    fun stableKey(song: Song): String {
        val source = resolve(song)
        return source?.stableKey
            ?: song.uri.toString()
                .takeIf { song.uri.scheme.equals("content", ignoreCase = true) }
                ?.trim()
                ?.lowercase(Locale.ROOT)
                ?.let { "uri:$it" }
            ?: LibrarySongDuplicateResolver.normalizedRealPath(song.libraryPath)?.let { "file:$it" }
            ?: song.uri.toString().trim().lowercase(Locale.ROOT).let { "uri:$it" }
    }

    /** Source-local key used by scanner caches; unlike [stableKey], it never follows a path. */
    fun sourceKey(song: Song): String {
        return resolve(song)?.stableKey
            ?: song.uri.toString().trim().lowercase(Locale.ROOT).let { "uri:$it" }
    }

    fun revision(song: Song, sizeBytes: Long? = null, providerGeneration: Long? = null): MediaRevision {
        val modifiedAtMs = song.dateModifiedSeconds
            ?.takeIf { it in 0L..Long.MAX_VALUE / 1_000L }
            ?.times(1_000L)
        return MediaRevision(
            modifiedAtMs = modifiedAtMs,
            sizeBytes = sizeBytes?.takeIf { it >= 0L },
            providerGeneration = providerGeneration?.takeIf { it >= 0L },
            metadataRevision = if (song.metadataResolved) 1L else 0L,
        )
    }

    fun sourceRevisionKey(
        modifiedAtMs: Long?,
        sizeBytes: Long?,
        providerGeneration: Long? = null,
    ): String {
        return MediaRevision(
            modifiedAtMs = modifiedAtMs?.takeIf { it >= 0L },
            sizeBytes = sizeBytes?.takeIf { it >= 0L },
            providerGeneration = providerGeneration?.takeIf { it >= 0L },
            metadataRevision = 0L,
        ).stableKey
    }

    private fun resolveContentUri(uri: Uri): MediaSourceIdentity? {
        if (uri.scheme.equals("file", ignoreCase = true)) return directFile(uri.path)
        if (!uri.scheme.equals("content", ignoreCase = true)) return null
        if (uri.authority.equals("media", ignoreCase = true)) {
            val segments = uri.pathSegments
            val mediaId = segments.lastOrNull()?.toLongOrNull()
            val volume = segments.firstOrNull()
            mediaStore(volume, mediaId)?.let { return it }
        }
        val isDocumentUri = uri.pathSegments.any { segment ->
            segment.equals("document", ignoreCase = true) || segment.equals("tree", ignoreCase = true)
        }
        if (!isDocumentUri) return null
        val documentId = runCatching { DocumentsContract.getDocumentId(uri) }.getOrNull()
        val treeId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
        return safDocument(uri.authority, documentId, treeId)
    }
}

private fun Long?.orEmptyRevisionPart(): String = this?.toString() ?: "-"

private fun String?.normalizedIdentityPart(): String? {
    return this?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotBlank() }
}
