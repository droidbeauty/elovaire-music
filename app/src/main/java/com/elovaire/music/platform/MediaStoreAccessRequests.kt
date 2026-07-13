package elovaire.music.droidbeauty.app.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest

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
    val pendingIntent = mediaStoreWritePendingIntent(context, uris) ?: return null
    return IntentSenderRequest.Builder(pendingIntent.intentSender).build()
}

internal fun mediaStoreWritePendingIntent(
    context: Context,
    uris: Collection<Uri>,
): android.app.PendingIntent? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
    val requestUris = MediaWriteTargetClassifier.mediaStoreItems(context, uris)
    if (requestUris.isEmpty()) return null
    return MediaStore.createWriteRequest(context.contentResolver, requestUris)
}

internal fun mediaStoreDeleteRequest(
    context: Context,
    uris: Collection<Uri>,
): IntentSenderRequest? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
    val requestUris = MediaWriteTargetClassifier.mediaStoreItems(context, uris)
    if (requestUris.isEmpty()) return null
    return IntentSenderRequest.Builder(
        MediaStore.createDeleteRequest(context.contentResolver, requestUris).intentSender,
    ).build()
}

internal fun takePersistableTreePermission(
    context: Context,
    uri: Uri,
): Boolean {
    val resolver = context.contentResolver
    val read = Intent.FLAG_GRANT_READ_URI_PERMISSION
    return runCatching {
        resolver.takePersistableUriPermission(uri, read or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }.recoverCatching {
        resolver.takePersistableUriPermission(uri, read)
    }.isSuccess
}

internal fun releasePersistableTreePermission(
    context: Context,
    uri: Uri,
): Boolean {
    val resolver = context.contentResolver
    val permission = runCatching {
        resolver.persistedUriPermissions.firstOrNull { it.uri == uri }
    }.getOrNull() ?: return true
    var flags = 0
    if (permission.isReadPermission) flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
    if (permission.isWritePermission) flags = flags or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    if (flags == 0) return true
    return runCatching {
        resolver.releasePersistableUriPermission(uri, flags)
    }.isSuccess
}

internal fun Uri.isContentUri(): Boolean {
    return scheme == "content"
}
