package elovaire.music.droidbeauty.app.data.artwork

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import android.util.Size
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.core.memoryPressureForTrimLevel
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

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
    AboutLogo,
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

internal fun isLikelyAudioMediaUri(uri: Uri): Boolean {
    val path = uri.toString()
        .substringBefore('?')
        .substringBefore('#')
        .lowercase(Locale.ROOT)
    return path.contains("/audio/media/") || AUDIO_FILE_EXTENSIONS.any(path::endsWith)
}

internal fun shouldUseContentResolverThumbnail(
    uri: Uri,
    purpose: ArtworkPurpose,
): Boolean {
    return purpose != ArtworkPurpose.Notification && !isLikelyAudioMediaUri(uri)
}

internal fun loadArtworkBitmap(
    context: Context,
    uri: Uri?,
    targetPx: Int,
    purpose: ArtworkPurpose = if (targetPx <= 256) ArtworkPurpose.UiGrid else ArtworkPurpose.UiLarge,
): Bitmap? {
    val requestUri = uri ?: return null
    ArtworkBitmapCache.ensureRegistered(context.applicationContext)
    val size = normalizeArtworkRequestSize(targetPx)
    val key = artworkRequestKey(requestUri, size, purpose)
    val decode = {
        val targetSize = ImageTargetSize(size, size)
        val bitmap = if (shouldUseContentResolverThumbnail(requestUri, purpose)) {
            runCatching {
                context.contentResolver.loadThumbnail(requestUri, Size(size, size), null)
            }.getOrNull()
        } else {
            null
        }
        bitmap
            ?: if (isLikelyAudioMediaUri(requestUri)) {
                null
            } else {
                decodeBitmapStream(context, requestUri, targetSize, purpose)
            }
            ?: decodeEmbeddedArtwork(context, requestUri, targetSize, purpose)
    }
    return key?.let { ArtworkBitmapCache.getOrLoad(it.cacheKey, decode) } ?: decode()
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

internal fun encodeArtworkForMediaSession(bitmap: Bitmap): ByteArray? {
    fun encode(quality: Int): ByteArray? {
        val output = ByteArrayOutputStream(
            (bitmap.width * bitmap.height / 3).coerceAtLeast(16 * 1024),
        )
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)) return null
        return output.toByteArray().takeIf { it.size <= MAX_MEDIA_SESSION_ARTWORK_BYTES }
    }
    return encode(94) ?: encode(84)
}

private fun decodeBitmapStream(
    context: Context,
    uri: Uri,
    targetSize: ImageTargetSize,
    purpose: ArtworkPurpose,
): Bitmap? {
    return runCatching {
        if (uri.scheme == "https") {
            return@runCatching downloadRemoteArtwork(uri)?.let { bytes ->
                decodeArtworkBytes(bytes, targetSize.widthPx, purpose)
            }
        }
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openArtworkInputStream(context, uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }
        val sampledOptions = artworkDecodeOptions(options, targetSize, purpose)
            ?: return@runCatching null
        openArtworkInputStream(context, uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, sampledOptions)
        }
    }.getOrNull()
}

private fun downloadRemoteArtwork(uri: Uri): ByteArray? {
    val connection = (URL(uri.toString()).openConnection() as? HttpURLConnection)?.apply {
        requestMethod = "GET"
        connectTimeout = REMOTE_ARTWORK_CONNECT_TIMEOUT_MS
        readTimeout = REMOTE_ARTWORK_READ_TIMEOUT_MS
        instanceFollowRedirects = true
        setRequestProperty("Accept", "image/*")
    } ?: return null
    return try {
        connection.connect()
        if (connection.responseCode !in 200..299 || connection.url.protocol != "https") return null
        if (connection.contentLengthLong > MAX_REMOTE_ARTWORK_BYTES) return null
        connection.inputStream.use { input ->
            val output = ByteArrayOutputStream(minOf(MAX_REMOTE_ARTWORK_BYTES, 32 * 1024))
            val buffer = ByteArray(8 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (output.size() > MAX_REMOTE_ARTWORK_BYTES - count) return null
                output.write(buffer, 0, count)
            }
            output.toByteArray().takeIf { it.isNotEmpty() }
        }
    } finally {
        connection.disconnect()
    }
}

private fun openArtworkInputStream(context: Context, uri: Uri): InputStream? {
    return if (uri.scheme == "file") {
        uri.path?.let(::FileInputStream)
    } else {
        context.contentResolver.openInputStream(uri)
    }
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
private const val MAX_MEDIA_SESSION_ARTWORK_BYTES = 2 * 1024 * 1024
private const val MAX_ARTWORK_DIMENSION = 8_192
private const val MAX_ARTWORK_PIXELS = 40_000_000L
private const val MAX_REMOTE_ARTWORK_BYTES = 2 * 1024 * 1024
private const val REMOTE_ARTWORK_CONNECT_TIMEOUT_MS = 5_000
private const val REMOTE_ARTWORK_READ_TIMEOUT_MS = 5_000
private val AUDIO_FILE_EXTENSIONS = setOf(
    ".aac",
    ".flac",
    ".m4a",
    ".m4b",
    ".mp3",
    ".ogg",
    ".oga",
    ".opus",
    ".wav",
    ".webm",
)

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
        ArtworkPurpose.PlaylistPreview,
        -> Bitmap.Config.RGB_565

        ArtworkPurpose.UiLarge,
        ArtworkPurpose.Notification,
        ArtworkPurpose.TagEditorPreview,
        ArtworkPurpose.AboutLogo,
        -> Bitmap.Config.ARGB_8888
    }
}

/** One bounded decoded-bitmap cache shared by UI and notification artwork consumers. */
internal object ArtworkBitmapCache {
    private val maxCacheBytes = (Runtime.getRuntime().maxMemory() / 16L)
        .coerceAtMost(8L * 1024L * 1024L)
        .coerceAtLeast(2L * 1024L * 1024L)
        .toInt()
    private var callbacksRegistered = false
    private val cache = object : LruCache<String, Bitmap>(maxCacheBytes) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.allocationByteCount.coerceAtLeast(1)
        }
    }
    private val inFlight = mutableMapOf<String, CompletableFuture<Bitmap?>>()
    private const val MAX_IN_FLIGHT = 8

    @Synchronized
    fun ensureRegistered(appContext: Context) {
        if (callbacksRegistered) return
        appContext.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit

            @Deprecated("Deprecated Android callback")
            override fun onLowMemory() = Unit

            override fun onTrimMemory(level: Int) {
                trim(level)
            }
        })
        callbacksRegistered = true
    }

    @Synchronized
    operator fun get(key: String): Bitmap? = cache.get(key)

    @Synchronized
    fun bestForUri(
        uri: Uri,
        requestedSize: Int,
        purpose: ArtworkPurpose,
    ): Bitmap? {
        val prefix = "$uri|"
        var smallestSufficientSize = Int.MAX_VALUE
        var largestAvailableSize = Int.MIN_VALUE
        var smallestSufficient: Bitmap? = null
        var largestAvailable: Bitmap? = null
        cache.snapshot().forEach { (key, bitmap) ->
            if (!key.startsWith(prefix)) return@forEach
            val remainder = key.removePrefix(prefix)
            val separator = remainder.indexOf('|')
            if (separator <= 0 || remainder.substring(separator + 1) != purpose.name) return@forEach
            val size = remainder.substring(0, separator).toIntOrNull() ?: return@forEach
            if (size >= requestedSize && size < smallestSufficientSize) {
                smallestSufficientSize = size
                smallestSufficient = bitmap
            }
            if (size > largestAvailableSize) {
                largestAvailableSize = size
                largestAvailable = bitmap
            }
        }
        return smallestSufficient ?: largestAvailable
    }

    @Synchronized
    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    fun getOrLoad(
        key: String,
        decode: () -> Bitmap?,
    ): Bitmap? {
        var ownerFuture: CompletableFuture<Bitmap?>? = null
        while (ownerFuture == null) {
            val waitFor: CompletableFuture<Bitmap?>?
            synchronized(this) {
                cache.get(key)?.let { return it }
                val existing = inFlight[key]
                if (existing != null) {
                    waitFor = existing
                } else if (inFlight.size < MAX_IN_FLIGHT) {
                    ownerFuture = CompletableFuture()
                    inFlight[key] = ownerFuture
                    waitFor = null
                } else {
                    waitFor = inFlight.values.firstOrNull()
                }
            }
            if (ownerFuture == null) {
                val completed = waitFor ?: continue
                try {
                    completed.get()
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return null
                } catch (_: ExecutionException) {
                    // The owner completed with a failure; retry the requested key below.
                }
                synchronized(this) {
                    cache.get(key)?.let { return it }
                    if (inFlight[key] === completed) return null
                }
            }
        }

        val pending = requireNotNull(ownerFuture)
        val outcome = runCatching {
            decode()?.also { put(key, it) }
        }
        return try {
            outcome.fold(
                onSuccess = { loaded ->
                    pending.complete(loaded)
                    loaded
                },
                onFailure = { failure ->
                    pending.completeExceptionally(failure)
                    throw failure
                },
            )
        } finally {
            synchronized(this) {
                if (inFlight[key] === pending) inFlight.remove(key)
            }
        }
    }

    @Synchronized
    fun removeAllMatchingUris(uris: Collection<String>) {
        if (uris.isEmpty()) return
        cache.snapshot().keys
            .filter { key -> uris.any { uri -> key == uri || key.startsWith("$uri|") } }
            .forEach(cache::remove)
    }

    @Synchronized
    private fun trim(level: Int) {
        when (memoryPressureForTrimLevel(level)) {
            MemoryPressure.Critical -> cache.evictAll()
            MemoryPressure.Moderate -> cache.trimToSize((maxCacheBytes / 2).coerceAtLeast(1))
            MemoryPressure.Normal -> Unit
        }
    }
}

internal fun invalidateArtworkBitmapCache(uris: Collection<String>) {
    ArtworkBitmapCache.removeAllMatchingUris(uris)
}
