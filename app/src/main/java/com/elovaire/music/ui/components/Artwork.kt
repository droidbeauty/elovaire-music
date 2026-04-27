package elovaire.music.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import android.net.Uri
import elovaire.music.app.R
import elovaire.music.app.ui.theme.ElovaireRadii
import elovaire.music.app.ui.theme.elovaireScaledSp
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.LinkedHashMap

@Composable
fun ArtworkImage(
    uri: Uri?,
    modifier: Modifier = Modifier,
    title: String = "",
    cornerRadius: Dp = ElovaireRadii.artwork,
    showArtworkGlow: Boolean = false,
    overlay: (@Composable BoxScope.() -> Unit)? = null,
) {
    val image = rememberArtworkBitmap(uri = uri, size = 768)
    val gradient = rememberArtworkGradient(uri).value
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier,
    ) {
        val artworkBitmap = image.value
        if (showArtworkGlow) {
            if (artworkBitmap != null) {
                Image(
                    bitmap = artworkBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.74f)
                        .fillMaxHeight(0.26f)
                        .clip(shape)
                        .blur(18.dp),
                    alpha = 0.34f,
                )
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.74f)
                        .fillMaxHeight(0.26f)
                        .clip(shape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    gradient.first().copy(alpha = 0f),
                                    gradient.first().copy(alpha = 0.1f),
                                    gradient.last().copy(alpha = 0.16f),
                                ),
                            ),
                        )
                        .blur(18.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                ),
        ) {
            if (artworkBitmap != null) {
                Image(
                    bitmap = artworkBitmap,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_music),
                        contentDescription = title.ifBlank { "Artwork placeholder" },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        modifier = Modifier.size(42.dp),
                    )
                }
            }

            overlay?.invoke(this)
        }
    }
}

@Composable
fun rememberArtworkBitmap(
    uri: Uri?,
    size: Int,
): State<ImageBitmap?> {
    val context = LocalContext.current
    val cacheKey = rememberCacheKey(uri, size)
    return produceState<ImageBitmap?>(initialValue = ArtworkMemoryCache.image(cacheKey), uri, size) {
        val cached = ArtworkMemoryCache.image(cacheKey)
        if (cached != null) {
            value = cached
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            loadBitmap(context, uri, size)?.asImageBitmap()?.also { bitmap ->
                ArtworkMemoryCache.putImage(cacheKey, bitmap)
            }
        }
    }
}

@Composable
fun rememberArtworkGradient(uri: Uri?): State<List<Color>> {
    val context = LocalContext.current
    val fallbackColor = MaterialTheme.colorScheme.primary
    val foundation = MaterialTheme.colorScheme.background
    val cacheKey = rememberCacheKey(uri, 512)
    return produceState(
        initialValue = ArtworkMemoryCache.gradient(cacheKey) ?: defaultArtworkGradient(fallbackColor, foundation),
        key1 = uri,
    ) {
        val cached = ArtworkMemoryCache.gradient(cacheKey)
        if (cached != null) {
            value = cached
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            val bitmap = loadBitmap(context, uri, 512)
            (bitmap?.let { paletteFromBitmap(it, foundation) } ?: defaultArtworkGradient(fallbackColor, foundation)).also { gradient ->
                ArtworkMemoryCache.putGradient(cacheKey, gradient)
            }
        }
    }
}

private fun rememberCacheKey(
    uri: Uri?,
    size: Int,
): String {
    return "${uri?.toString().orEmpty()}#$size"
}

private fun loadBitmap(
    context: Context,
    uri: Uri?,
    size: Int,
): Bitmap? {
    if (uri == null) return null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        runCatching {
            context.contentResolver.loadThumbnail(uri, Size(size, size), null)
        }.getOrNull()?.let { return it }
    }

    decodeBitmapStream(context, uri, size)?.let { return it }

    return decodeEmbeddedArtwork(context, uri)
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
                    targetSize = 768,
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
    var halfWidth = outWidth / 2
    var halfHeight = outHeight / 2
    while (halfWidth / sampleSize >= targetSize && halfHeight / sampleSize >= targetSize) {
        sampleSize *= 2
    }
    return sampleSize.coerceAtLeast(1)
}

private fun paletteFromBitmap(
    bitmap: Bitmap,
    foundation: Color,
): List<Color> {
    var red = 0L
    var green = 0L
    var blue = 0L
    var samples = 0L
    val stepX = (bitmap.width / 18).coerceAtLeast(1)
    val stepY = (bitmap.height / 18).coerceAtLeast(1)

    for (x in 0 until bitmap.width step stepX) {
        for (y in 0 until bitmap.height step stepY) {
            val color = bitmap.getPixel(x, y)
            red += android.graphics.Color.red(color)
            green += android.graphics.Color.green(color)
            blue += android.graphics.Color.blue(color)
            samples++
        }
    }

    if (samples == 0L) return defaultArtworkGradient(Color(0xFF6F5840), foundation)

    val base = Color(
        android.graphics.Color.argb(
            255,
            (red / samples).toInt(),
            (green / samples).toInt(),
            (blue / samples).toInt(),
        ),
    )
    return defaultArtworkGradient(base, foundation)
}

private fun defaultArtworkGradient(
    base: Color,
    foundation: Color,
): List<Color> {
    val softened = base.copy(alpha = 0.16f).compositeOver(foundation)
    val accent = base.copy(alpha = 0.08f).compositeOver(foundation)
    return listOf(softened, foundation, accent)
}

private object ArtworkMemoryCache {
    private const val MAX_IMAGES = 96
    private const val MAX_GRADIENTS = 160

    private val images = object : LinkedHashMap<String, ImageBitmap>(MAX_IMAGES, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean {
            return size > MAX_IMAGES
        }
    }
    private val gradients = object : LinkedHashMap<String, List<Color>>(MAX_GRADIENTS, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<Color>>?): Boolean {
            return size > MAX_GRADIENTS
        }
    }

    @Synchronized
    fun image(key: String): ImageBitmap? = images[key]

    @Synchronized
    fun putImage(
        key: String,
        image: ImageBitmap,
    ) {
        images[key] = image
    }

    @Synchronized
    fun gradient(key: String): List<Color>? = gradients[key]

    @Synchronized
    fun putGradient(
        key: String,
        gradient: List<Color>,
    ) {
        gradients[key] = gradient
    }
}
