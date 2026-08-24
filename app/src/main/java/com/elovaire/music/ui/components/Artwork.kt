package elovaire.music.droidbeauty.app.ui.components

import android.content.Context
import android.graphics.Bitmap
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
import androidx.palette.graphics.Palette
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.artwork.ArtworkPurpose
import elovaire.music.droidbeauty.app.data.artwork.ArtworkBitmapCache
import elovaire.music.droidbeauty.app.data.artwork.artworkRequestKey
import elovaire.music.droidbeauty.app.data.artwork.loadArtworkBitmap
import elovaire.music.droidbeauty.app.data.artwork.invalidateArtworkBitmapCache
import elovaire.music.droidbeauty.app.data.artwork.normalizeArtworkRequestSize
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
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
    requestedSizePx: Int = 384,
    placeholderIconSize: Dp = 30.dp,
    showArtworkGlow: Boolean = false,
    overlay: (@Composable BoxScope.() -> Unit)? = null,
) {
    val image = rememberArtworkBitmap(uri = uri, size = requestedSizePx)
    val artworkBitmap = image.value
    val gradient = if (showArtworkGlow && artworkBitmap == null) {
        rememberArtworkGradient(uri).value
    } else {
        null
    }
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier,
    ) {
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
                val fallbackGradient = gradient ?: defaultArtworkGradient(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.background,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.74f)
                        .fillMaxHeight(0.26f)
                        .clip(shape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    fallbackGradient.first().copy(alpha = 0f),
                                    fallbackGradient.first().copy(alpha = 0.1f),
                                    fallbackGradient.last().copy(alpha = 0.16f),
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
                        modifier = Modifier.size(placeholderIconSize),
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
    val normalizedSize = normalizeArtworkRequestSize(size)
    val requestKey = artworkRequestKey(
        uri = uri,
        targetPx = normalizedSize,
        purpose = artworkPurposeForSize(normalizedSize),
    )
    val cachedImage = requestKey?.let { ArtworkBitmapCache[it.cacheKey]?.asImageBitmap() }
    val fallbackImage = requestKey?.let {
        ArtworkBitmapCache.bestForUri(it.uri, it.targetPx, it.purpose)?.asImageBitmap()
    }
    return produceState<ImageBitmap?>(initialValue = cachedImage ?: fallbackImage, uri, normalizedSize) {
        val cached = requestKey?.let { ArtworkBitmapCache[it.cacheKey] }
        if (cached != null) {
            value = cached.asImageBitmap()
            return@produceState
        }
        value = value ?: fallbackImage
        val loaded = withContext(Dispatchers.IO) {
            requestKey?.let { loadArtworkBitmap(context, it) }?.also { bitmap ->
                bitmap.prepareToDraw()
            }?.asImageBitmap()
        }
        if (loaded != null || value == null) value = loaded
    }
}

@Composable
fun rememberArtworkGradient(uri: Uri?): State<List<Color>> {
    val context = LocalContext.current
    val fallbackColor = MaterialTheme.colorScheme.primary
    val foundation = MaterialTheme.colorScheme.background
    val cacheKey = rememberGradientCacheKey(uri, 512)
    return produceState(
        initialValue = ArtworkGradientCache.gradient(cacheKey) ?: defaultArtworkGradient(fallbackColor, foundation),
        key1 = uri,
    ) {
        val cached = ArtworkGradientCache.gradient(cacheKey)
        if (cached != null) {
            value = cached
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            val bitmap = loadArtworkBitmap(context, uri, 512)
            (bitmap?.let { paletteFromBitmap(it, foundation) } ?: defaultArtworkGradient(fallbackColor, foundation)).also { gradient ->
                ArtworkGradientCache.putGradient(cacheKey, gradient)
            }
        }
    }
}

@Composable
fun rememberArtworkPaletteAccent(
    uri: Uri?,
    size: Int = 512,
): State<Color?> {
    val context = LocalContext.current
    val normalizedSize = normalizeArtworkRequestSize(size)
    return produceState<Color?>(initialValue = null, key1 = uri, key2 = normalizedSize) {
        val bitmap = withContext(Dispatchers.IO) {
            loadArtworkBitmap(context, uri, normalizedSize)
        }
        value = bitmap?.let {
            withContext(Dispatchers.Default) {
                val palette = Palette.from(it).generate()
                val swatch = palette.darkMutedSwatch
                    ?: palette.mutedSwatch
                    ?: palette.dominantSwatch
                    ?: palette.darkVibrantSwatch
                    ?: palette.vibrantSwatch
                swatch?.rgb?.let(::mutedPaletteColor)
            }
        }
    }
}

private fun mutedPaletteColor(rgb: Int): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(rgb, hsv)
    hsv[1] = (hsv[1] * 0.58f).coerceAtMost(0.62f)
    hsv[2] = hsv[2].coerceAtMost(0.78f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

internal fun invalidateArtworkCaches(uris: Collection<Uri?>) {
    val keys = uris
        .filterNotNull()
        .map(Uri::toString)
        .filter(String::isNotBlank)
        .toSet()
    if (keys.isEmpty()) return
    invalidateArtworkBitmapCache(keys)
    ArtworkGradientCache.removeMatching(keys)
}

private fun rememberGradientCacheKey(
    uri: Uri?,
    size: Int,
): String {
    return artworkRequestKey(
        uri = uri,
        targetPx = size,
        purpose = ArtworkPurpose.UiLarge,
    )?.let { "${it.cacheKey}|gradient" }.orEmpty()
}

private fun artworkPurposeForSize(size: Int): ArtworkPurpose {
    return if (size <= 256) ArtworkPurpose.UiGrid else ArtworkPurpose.UiLarge
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

private object ArtworkGradientCache {
    private const val MAX_GRADIENTS = 160
    private val gradients = object : LinkedHashMap<String, List<Color>>(MAX_GRADIENTS, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<Color>>?): Boolean {
            return size > MAX_GRADIENTS
        }
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

    @Synchronized
    fun removeMatching(uriKeys: Set<String>) {
        if (uriKeys.isEmpty()) return
        val iterator = gradients.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (uriKeys.any { uriKey -> entry.key.startsWith("$uriKey|") }) {
                iterator.remove()
            }
        }
    }
}
