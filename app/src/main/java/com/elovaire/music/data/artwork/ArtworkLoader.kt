package elovaire.music.droidbeauty.app.data.artwork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Size

internal data class ArtworkRequestKey(
    val uri: String,
    val targetPx: Int,
    val purpose: ArtworkPurpose,
) {
    val cacheKey: String
        get() = "$uri|$targetPx|${purpose.name}"
}

internal enum class ArtworkPurpose {
    UiGrid,
    UiLarge,
    Notification,
    PlaylistPreview,
    TagEditorPreview,
}

internal fun artworkRequestKey(
    uri: Uri?,
    targetPx: Int,
    purpose: ArtworkPurpose,
): ArtworkRequestKey? {
    val uriKey = uri?.toString()?.takeIf { it.isNotBlank() } ?: return null
    return ArtworkRequestKey(
        uri = uriKey,
        targetPx = normalizeArtworkRequestSize(targetPx),
        purpose = purpose,
    )
}

internal fun normalizeArtworkRequestSize(size: Int): Int {
    val requested = size.coerceAtLeast(1)
    return when {
        requested <= 96 -> 96
        requested <= 160 -> 160
        requested <= 256 -> 256
        requested <= 384 -> 384
        requested <= 512 -> 512
        requested <= 768 -> 768
        else -> 1024
    }
}

internal fun loadArtworkBitmap(
    context: Context,
    uri: Uri?,
    targetPx: Int,
): Bitmap? {
    val requestUri = uri ?: return null
    val size = normalizeArtworkRequestSize(targetPx)

    runCatching {
        context.contentResolver.loadThumbnail(requestUri, Size(size, size), null)
    }.getOrNull()?.let { return it }

    decodeBitmapStream(context, requestUri, size)?.let { return it }

    return decodeEmbeddedArtwork(context, requestUri, size)
}

internal fun loadArtworkBitmap(
    context: Context,
    key: ArtworkRequestKey,
): Bitmap? {
    return loadArtworkBitmap(context, Uri.parse(key.uri), key.targetPx)
}

internal fun decodeArtworkBytes(
    bytes: ByteArray,
    targetPx: Int,
): Bitmap? {
    if (bytes.isEmpty()) return null
    val size = normalizeArtworkRequestSize(targetPx)
    return runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val sampledOptions = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
            inSampleSize = calculateInSampleSize(
                outWidth = bounds.outWidth,
                outHeight = bounds.outHeight,
                targetSize = size,
            )
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, sampledOptions)
    }.getOrNull()
}

private fun decodeBitmapStream(
    context: Context,
    uri: Uri,
    targetSize: Int,
): Bitmap? {
    return runCatching {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }
        val sampledOptions = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
            inSampleSize = calculateInSampleSize(
                outWidth = options.outWidth,
                outHeight = options.outHeight,
                targetSize = targetSize,
            )
        }
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, sampledOptions)
        }
    }.getOrNull()
}

private fun decodeEmbeddedArtwork(
    context: Context,
    uri: Uri,
    targetSize: Int,
): Bitmap? {
    return runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val bytes = retriever.embeddedPicture ?: return null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            val sampledOptions = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
                inSampleSize = calculateInSampleSize(
                    outWidth = bounds.outWidth,
                    outHeight = bounds.outHeight,
                    targetSize = targetSize,
                )
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, sampledOptions)
        } finally {
            runCatching { retriever.release() }
        }
    }.getOrNull()
}

private fun calculateInSampleSize(
    outWidth: Int,
    outHeight: Int,
    targetSize: Int,
): Int {
    if (outWidth <= 0 || outHeight <= 0 || targetSize <= 0) return 1
    var sampleSize = 1
    val halfWidth = outWidth / 2
    val halfHeight = outHeight / 2
    while (halfWidth / sampleSize >= targetSize && halfHeight / sampleSize >= targetSize) {
        sampleSize *= 2
    }
    return sampleSize.coerceAtLeast(1)
}
