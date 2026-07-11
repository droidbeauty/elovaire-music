package elovaire.music.droidbeauty.app.platform

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import java.io.File

internal sealed interface MediaWriteTarget {
    val uri: Uri

    data class MediaStoreItem(override val uri: Uri) : MediaWriteTarget
    data class SafDocument(override val uri: Uri) : MediaWriteTarget
    data class FileUri(override val uri: Uri) : MediaWriteTarget
    data class Unsupported(
        override val uri: Uri,
        val reason: String,
    ) : MediaWriteTarget
}

internal enum class MediaWriteTargetKind {
    MediaStoreItem,
    SafDocument,
    FileUri,
    Unsupported,
}

internal object MediaWriteTargetClassifier {
    fun classify(
        context: Context,
        uri: Uri,
    ): MediaWriteTarget {
        val direct = classify(uri)
        if (direct !is MediaWriteTarget.Unsupported || uri.scheme != ContentResolverScheme) return direct
        if (DocumentsContract.isDocumentUri(context, uri)) {
            return MediaWriteTarget.SafDocument(uri)
        }
        return direct
    }

    fun classify(uri: Uri): MediaWriteTarget {
        return when (classifyParts(uri.scheme, uri.authority, uri.pathSegments)) {
            MediaWriteTargetKind.MediaStoreItem -> MediaWriteTarget.MediaStoreItem(uri)
            MediaWriteTargetKind.SafDocument -> MediaWriteTarget.SafDocument(uri)
            MediaWriteTargetKind.FileUri -> MediaWriteTarget.FileUri(uri)
            MediaWriteTargetKind.Unsupported -> MediaWriteTarget.Unsupported(uri, "Unsupported URI.")
        }
    }

    fun classifyParts(
        scheme: String?,
        authority: String?,
        pathSegments: List<String>,
    ): MediaWriteTargetKind {
        return when (scheme) {
            FileScheme -> MediaWriteTargetKind.FileUri
            ContentResolverScheme -> when {
                isSafDocumentUri(authority, pathSegments) -> MediaWriteTargetKind.SafDocument
                authority == MediaStore.AUTHORITY && pathSegments.lastOrNull()?.toLongOrNull() != null ->
                    MediaWriteTargetKind.MediaStoreItem
                else -> MediaWriteTargetKind.Unsupported
            }
            else -> MediaWriteTargetKind.Unsupported
        }
    }

    fun mediaStoreItems(
        context: Context,
        uris: Collection<Uri>,
    ): List<Uri> {
        return uris.map { classify(context, it) }
            .filterIsInstance<MediaWriteTarget.MediaStoreItem>()
            .map(MediaWriteTarget.MediaStoreItem::uri)
    }

    fun isWritableWithoutMediaStoreApproval(
        context: Context,
        uri: Uri,
    ): Boolean {
        return when (classify(context, uri)) {
            is MediaWriteTarget.FileUri -> uri.path?.let { File(it).canWrite() } == true
            is MediaWriteTarget.SafDocument -> runCatching {
                context.contentResolver.openFileDescriptor(uri, "rw")?.use { true } == true
            }.getOrDefault(false)
            is MediaWriteTarget.MediaStoreItem -> true
            is MediaWriteTarget.Unsupported -> false
        }
    }

    private fun isSafDocumentUri(
        authority: String?,
        pathSegments: List<String>,
    ): Boolean {
        return authority?.endsWith(".documents") == true &&
            pathSegments.any { it == "tree" || it == "document" }
    }

    private const val ContentResolverScheme = "content"
    private const val FileScheme = "file"
}

internal fun mediaStoreWriteRequest(
    context: Context,
    uris: Collection<Uri>,
): IntentSenderRequest? {
    val requestUris = MediaWriteTargetClassifier.mediaStoreItems(context, uris)
    if (requestUris.isEmpty()) return null
    return IntentSenderRequest.Builder(
        MediaStore.createWriteRequest(context.contentResolver, requestUris).intentSender,
    ).build()
}

internal fun mediaStoreDeleteRequest(
    context: Context,
    uris: Collection<Uri>,
): IntentSenderRequest? {
    val requestUris = MediaWriteTargetClassifier.mediaStoreItems(context, uris)
    if (requestUris.isEmpty()) return null
    return IntentSenderRequest.Builder(
        MediaStore.createDeleteRequest(context.contentResolver, requestUris).intentSender,
    ).build()
}

internal fun Uri.isContentUri(): Boolean {
    return scheme == "content"
}
