package elovaire.music.droidbeauty.app.ui.components

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.LruCache
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
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.artwork.ArtworkPurpose
import elovaire.music.droidbeauty.app.data.artwork.artworkRequestKey
import elovaire.music.droidbeauty.app.data.artwork.loadArtworkBitmap
import elovaire.music.droidbeauty.app.data.artwork.normalizeArtworkRequestSize
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.core.memoryPressureForTrimLevel
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
    val normalizedSize = normalizeArtworkRequestSize(size)
    val requestKey = artworkRequestKey(
        uri = uri,
        targetPx = normalizedSize,
        purpose = artworkPurposeForSize(normalizedSize),
    )
    val cacheKey = requestKey?.cacheKey.orEmpty()
    val uriKey = uri?.toString().orEmpty()
    ArtworkMemoryCache.ensureRegistered(context.applicationContext)
    val cachedImage = ArtworkMemoryCache.image(cacheKey)
    val fallbackImage = if (cachedImage != null || uriKey.isBlank()) {
        cachedImage
    } else {
        ArtworkMemoryCache.bestImageForUri(uriKey, normalizedSize)
    }
    return produceState<ImageBitmap?>(initialValue = fallbackImage, uri, normalizedSize) {
        val cached = ArtworkMemoryCache.image(cacheKey)
        if (cached != null) {
            value = cached
            return@produceState
        }
        value = value ?: ArtworkMemoryCache.bestImageForUri(uriKey, normalizedSize)
        val loaded = withContext(Dispatchers.IO) {
            requestKey?.let { loadArtworkBitmap(context, it) }?.also { bitmap ->
                bitmap.prepareToDraw()
            }?.asImageBitmap()?.also { image ->
                ArtworkMemoryCache.putImage(
                    key = cacheKey,
                    uriKey = uriKey,
                    size = normalizedSize,
                    image = image,
                )
            }
        }
        if (loaded != null || value == null) {
            value = loaded
        }
    }
}

@Composable
fun rememberArtworkGradient(uri: Uri?): State<List<Color>> {
    val context = LocalContext.current
    val fallbackColor = MaterialTheme.colorScheme.primary
    val foundation = MaterialTheme.colorScheme.background
    val cacheKey = rememberGradientCacheKey(uri, 512)
    ArtworkMemoryCache.ensureRegistered(context.applicationContext)
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
            val bitmap = loadArtworkBitmap(context, uri, 512)
            (bitmap?.let { paletteFromBitmap(it, foundation) } ?: defaultArtworkGradient(fallbackColor, foundation)).also { gradient ->
                ArtworkMemoryCache.putGradient(cacheKey, gradient)
            }
        }
    }
}

internal fun invalidateArtworkCaches(uris: Collection<Uri?>) {
    val keys = uris
        .filterNotNull()
        .map(Uri::toString)
        .filter(String::isNotBlank)
        .toSet()
    if (keys.isEmpty()) return
    ArtworkMemoryCache.removeMatching(keys)
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

private object ArtworkMemoryCache {
    private val maxImageCacheBytes = (Runtime.getRuntime().maxMemory() / 8L)
        .coerceAtMost(24L * 1024L * 1024L)
        .coerceAtLeast(4L * 1024L * 1024L)
        .toInt()
    private const val MAX_GRADIENTS = 160
    private var callbacksRegistered = false

    private val images = object : LruCache<String, ImageBitmap>(maxImageCacheBytes) {
        override fun sizeOf(
            key: String,
            value: ImageBitmap,
        ): Int {
            return (value.width * value.height * 4).coerceAtLeast(1)
        }

        override fun entryRemoved(
            evicted: Boolean,
            key: String,
            oldValue: ImageBitmap,
            newValue: ImageBitmap?,
        ) {
            removeImageIndexKey(key)
        }
    }
    private val imageIndex = linkedMapOf<String, MutableMap<Int, LinkedHashSet<String>>>()
    private val imageKeyIndex = linkedMapOf<String, Pair<String, Int>>()
    private val gradients = object : LinkedHashMap<String, List<Color>>(MAX_GRADIENTS, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<Color>>?): Boolean {
            return size > MAX_GRADIENTS
        }
    }

    @Synchronized
    fun image(key: String): ImageBitmap? = images.get(key)

    @Synchronized
    fun putImage(
        key: String,
        uriKey: String,
        size: Int,
        image: ImageBitmap,
    ) {
        images.put(key, image)
        if (uriKey.isNotBlank()) {
            imageKeyIndex[key] = uriKey to size
            imageIndex
                .getOrPut(uriKey) { linkedMapOf() }
                .getOrPut(size) { linkedSetOf() }
                .add(key)
        }
    }

    @Synchronized
    fun bestImageForUri(
        uriKey: String,
        requestedSize: Int,
    ): ImageBitmap? {
        if (uriKey.isBlank()) return null
        val sizeIndex = imageIndex[uriKey] ?: return null
        var smallestSufficient = Int.MAX_VALUE
        var largestAvailable = Int.MIN_VALUE
        sizeIndex.keys.forEach { size ->
            if (size >= requestedSize && size < smallestSufficient) smallestSufficient = size
            if (size > largestAvailable) largestAvailable = size
        }
        val preferredSize = smallestSufficient.takeIf { it != Int.MAX_VALUE }
            ?: largestAvailable.takeIf { it != Int.MIN_VALUE }
            ?: return null
        val key = sizeIndex[preferredSize]?.lastOrNull() ?: return null
        return images.get(key)
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
    fun removeMatching(uriKeys: Set<String>) {
        if (uriKeys.isEmpty()) return
        val iterator = gradients.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (uriKeys.any { uriKey -> entry.key.startsWith("$uriKey|") }) {
                iterator.remove()
            }
        }
        images.snapshot().keys
            .filter { key -> uriKeys.any { uriKey -> key.startsWith("$uriKey|") } }
            .forEach(images::remove)
    }

    @Synchronized
    private fun removeImageIndexKey(key: String) {
        val (uriKey, size) = imageKeyIndex.remove(key) ?: return
        val sizes = imageIndex[uriKey] ?: return
        val keys = sizes[size] ?: return
        keys.remove(key)
        if (keys.isEmpty()) {
            sizes.remove(size)
        }
        if (sizes.isEmpty()) {
            imageIndex.remove(uriKey)
        }
    }

    @Synchronized
    private fun trim(level: Int) {
        when (memoryPressureForTrimLevel(level)) {
            MemoryPressure.Critical -> {
                images.evictAll()
                gradients.clear()
            }
            MemoryPressure.Moderate -> {
                images.trimToSize((maxImageCacheBytes / 2).coerceAtLeast(1))
                while (gradients.size > MAX_GRADIENTS / 2) {
                    gradients.entries.iterator().run {
                        if (hasNext()) {
                            next()
                            remove()
                        }
                    }
                }
            }
            MemoryPressure.Normal -> Unit
        }
    }
}
