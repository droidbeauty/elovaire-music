package elovaire.music.droidbeauty.app.data.artwork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Size

internal data class ArtworkRequestKey(
    val uri: Uri,
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

internal data class ImageTargetSize(
    val widthPx: Int,
    val heightPx: Int,
)

internal fun artworkRequestKey(
    uri: Uri?,
    targetPx: Int,
    purpose: ArtworkPurpose,
): ArtworkRequestKey? {
    val requestUri = uri?.takeIf { it.toString().isNotBlank() } ?: return null
    return ArtworkRequestKey(
        uri = requestUri,
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
    purpose: ArtworkPurpose = if (targetPx <= 256) ArtworkPurpose.UiGrid else ArtworkPurpose.UiLarge,
): Bitmap? {
    val requestUri = uri ?: return null
    val size = normalizeArtworkRequestSize(targetPx)
    val targetSize = ImageTargetSize(size, size)

    runCatching {
        context.contentResolver.loadThumbnail(requestUri, Size(size, size), null)
    }.getOrNull()?.let { return it }

    decodeBitmapStream(context, requestUri, targetSize, purpose)?.let { return it }

    return decodeEmbeddedArtwork(context, requestUri, targetSize, purpose)
}

internal fun loadArtworkBitmap(
    context: Context,
    key: ArtworkRequestKey,
): Bitmap? {
    return loadArtworkBitmap(context, key.uri, key.targetPx, key.purpose)
}

internal fun decodeArtworkBytes(
    bytes: ByteArray,
    targetPx: Int,
    purpose: ArtworkPurpose = ArtworkPurpose.TagEditorPreview,
): Bitmap? {
    if (bytes.isEmpty() || bytes.size > MAX_ENCODED_ARTWORK_BYTES) return null
    val size = normalizeArtworkRequestSize(targetPx)
    return runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val sampledOptions = artworkDecodeOptions(bounds, ImageTargetSize(size, size), purpose)
            ?: return@runCatching null
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, sampledOptions)
    }.getOrNull()
}

private fun decodeBitmapStream(
    context: Context,
    uri: Uri,
    targetSize: ImageTargetSize,
    purpose: ArtworkPurpose,
): Bitmap? {
    return runCatching {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }
        val sampledOptions = artworkDecodeOptions(options, targetSize, purpose)
            ?: return@runCatching null
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, sampledOptions)
        }
    }.getOrNull()
}

private fun decodeEmbeddedArtwork(
    context: Context,
    uri: Uri,
    targetSize: ImageTargetSize,
    purpose: ArtworkPurpose,
): Bitmap? {
    return runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val bytes = retriever.embeddedPicture ?: return null
            if (bytes.size > MAX_ENCODED_ARTWORK_BYTES) return null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            val sampledOptions = artworkDecodeOptions(bounds, targetSize, purpose)
                ?: return@runCatching null
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, sampledOptions)
        } finally {
            runCatching { retriever.release() }
        }
    }.getOrNull()
}

private const val MAX_ENCODED_ARTWORK_BYTES = 16 * 1024 * 1024
private const val MAX_ARTWORK_DIMENSION = 8_192
private const val MAX_ARTWORK_PIXELS = 40_000_000L

internal fun isArtworkBoundsSafe(width: Int, height: Int): Boolean {
    return width in 1..MAX_ARTWORK_DIMENSION &&
        height in 1..MAX_ARTWORK_DIMENSION &&
        width.toLong() * height <= MAX_ARTWORK_PIXELS
}

private fun artworkDecodeOptions(
    bounds: BitmapFactory.Options,
    targetSize: ImageTargetSize,
    purpose: ArtworkPurpose,
): BitmapFactory.Options? {
    if (!isArtworkBoundsSafe(bounds.outWidth, bounds.outHeight)) return null
    return BitmapFactory.Options().apply {
        inPreferredConfig = bitmapConfigForPurpose(purpose)
        inSampleSize = calculateInSampleSize(
            outWidth = bounds.outWidth,
            outHeight = bounds.outHeight,
            targetSize = targetSize,
        )
    }
}

private fun calculateInSampleSize(
    outWidth: Int,
    outHeight: Int,
    targetSize: ImageTargetSize,
): Int {
    if (outWidth <= 0 || outHeight <= 0 || targetSize.widthPx <= 0 || targetSize.heightPx <= 0) return 1
    var sampleSize = 1
    val halfWidth = outWidth / 2
    val halfHeight = outHeight / 2
    while (
        halfWidth / sampleSize >= targetSize.widthPx &&
        halfHeight / sampleSize >= targetSize.heightPx
    ) {
        sampleSize *= 2
    }
    return sampleSize.coerceAtLeast(1)
}

internal fun bitmapConfigForPurpose(purpose: ArtworkPurpose): Bitmap.Config {
    return when (purpose) {
        ArtworkPurpose.UiGrid,
        ArtworkPurpose.Notification,
        ArtworkPurpose.PlaylistPreview,
        -> Bitmap.Config.RGB_565

        ArtworkPurpose.UiLarge,
        ArtworkPurpose.TagEditorPreview,
        -> Bitmap.Config.ARGB_8888
    }
}
