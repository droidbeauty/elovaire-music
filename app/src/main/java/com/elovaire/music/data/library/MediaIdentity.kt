package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.provider.DocumentsContract
import elovaire.music.droidbeauty.app.domain.model.Song
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
        resolveContentUri(song.uri)?.let { return it }
        return directFile(song.libraryPath)
    }

    fun stableKey(song: Song): String {
        return resolve(song)?.stableKey
            ?: song.uri.toString().trim().lowercase(Locale.ROOT).let { "uri:$it" }
    }

    fun revision(song: Song, sizeBytes: Long? = null, providerGeneration: Long? = null): MediaRevision {
        return MediaRevision(
            modifiedAtMs = song.dateModifiedSeconds?.coerceAtLeast(0L)?.times(1_000L),
            sizeBytes = sizeBytes?.takeIf { it >= 0L },
            providerGeneration = providerGeneration?.takeIf { it >= 0L },
            metadataRevision = if (song.metadataResolved) 1L else 0L,
        )
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
        val documentId = runCatching { DocumentsContract.getDocumentId(uri) }.getOrNull()
        val treeId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
        return safDocument(uri.authority, documentId, treeId)
    }
}

private fun Long?.orEmptyRevisionPart(): String = this?.toString() ?: "-"

private fun String?.normalizedIdentityPart(): String? {
    return this?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotBlank() }
}
