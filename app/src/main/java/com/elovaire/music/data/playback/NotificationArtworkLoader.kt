package elovaire.music.droidbeauty.app.data.playback

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import elovaire.music.droidbeauty.app.data.artwork.ArtworkPurpose
import elovaire.music.droidbeauty.app.data.artwork.ArtworkRequestKey
import elovaire.music.droidbeauty.app.data.artwork.artworkRequestKey
import elovaire.music.droidbeauty.app.data.artwork.loadArtworkBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@UnstableApi
internal class NotificationArtworkLoader(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val pendingLoads = linkedMapOf<String, Job>()
    private var currentKey: ArtworkRequestKey? = null

    init {
        NotificationArtworkCache.ensureRegistered(context.applicationContext)
    }

    fun setCurrentArtUri(uri: Uri?) {
        currentKey = uri?.let(::notificationArtworkLoadKey)
        trimPendingLoads(currentKey)
    }

    fun cachedBitmap(uri: Uri?): Bitmap? {
        val key = uri?.let(::notificationArtworkLoadKey) ?: return null
        return NotificationArtworkCache[key.cacheKey]
    }

    fun loadAsync(
        uri: Uri,
        isStillCurrent: (ArtworkRequestKey) -> Boolean,
        callback: PlayerNotificationManager.BitmapCallback,
    ) {
        val key = notificationArtworkLoadKey(uri)
        val cacheKey = key.cacheKey
        if (pendingLoads[cacheKey]?.isActive == true) return
        pendingLoads[cacheKey] = scope.launch(Dispatchers.IO) {
            try {
                val bitmap = loadBitmap(context, key)
                withContext(Dispatchers.Main.immediate) {
                    if (bitmap != null && isStillCurrent(key)) {
                        callback.onBitmap(bitmap)
                    }
                }
            } finally {
                withContext(NonCancellable + Dispatchers.Main.immediate) {
                    pendingLoads.remove(cacheKey)
                }
            }
        }
    }

    fun clear() {
        currentKey = null
        pendingLoads.values.forEach(Job::cancel)
        pendingLoads.clear()
    }

    fun isCurrent(key: ArtworkRequestKey): Boolean = currentKey == key

    private fun trimPendingLoads(activeKey: ArtworkRequestKey?) {
        val activeCacheKey = activeKey?.cacheKey
        val iterator = pendingLoads.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key == activeCacheKey) continue
            entry.value.cancel()
            iterator.remove()
        }
    }
}

internal fun notificationArtworkLoadKey(uri: Uri): ArtworkRequestKey {
    return requireNotNull(
        artworkRequestKey(
            uri = uri,
            targetPx = NOTIFICATION_ARTWORK_SIZE_PX,
            purpose = ArtworkPurpose.Notification,
        ),
    )
}

private fun loadBitmap(
    context: Context,
    key: ArtworkRequestKey,
): Bitmap? {
    NotificationArtworkCache[key.cacheKey]?.let { return it }
    val bitmap = loadArtworkBitmap(context, key)?.scaledDownForNotification()
    return bitmap?.also { cachedBitmap ->
        NotificationArtworkCache.put(key.cacheKey, cachedBitmap)
    }
}

private const val NOTIFICATION_ARTWORK_SIZE_PX = 96

private fun Bitmap.scaledDownForNotification(): Bitmap {
    val largestDimension = maxOf(width, height)
    if (largestDimension <= NOTIFICATION_ARTWORK_SIZE_PX) return this
    val scale = NOTIFICATION_ARTWORK_SIZE_PX.toFloat() / largestDimension.toFloat()
    val scaledWidth = (width * scale).roundToInt().coerceAtLeast(1)
    val scaledHeight = (height * scale).roundToInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(this, scaledWidth, scaledHeight, true)
}

private object NotificationArtworkCache {
    private val maxCacheBytes = (Runtime.getRuntime().maxMemory() / 16L)
        .coerceAtMost(256L * 256L * 2L * 12L)
        .coerceAtLeast(2L * 1024L * 1024L)
        .toInt()
    private var callbacksRegistered = false

    private val cache = object : LruCache<String, Bitmap>(maxCacheBytes) {
        override fun sizeOf(
            key: String,
            value: Bitmap,
        ): Int {
            return value.allocationByteCount
        }
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

    operator fun get(key: String): Bitmap? = cache.get(key)

    fun put(
        key: String,
        bitmap: Bitmap,
    ) {
        cache.put(key, bitmap)
    }

    fun removeAllMatchingUris(uris: Collection<String>) {
        if (uris.isEmpty()) return
        val keysToRemove = cache.snapshot().keys.filter { key ->
            uris.any { uri -> key == uri || key.startsWith("$uri|") }
        }
        keysToRemove.forEach(cache::remove)
    }

    @Synchronized
    private fun trim(level: Int) {
        when {
            level == TRIM_MEMORY_RUNNING_CRITICAL_LEVEL ||
                level >= TRIM_MEMORY_COMPLETE_LEVEL -> cache.evictAll()
            level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> cache.trimToSize((maxCacheBytes / 2).coerceAtLeast(1))
        }
    }

    private const val TRIM_MEMORY_RUNNING_CRITICAL_LEVEL = 15
    private const val TRIM_MEMORY_COMPLETE_LEVEL = 80
}

internal fun removeNotificationArtworkForUris(uris: Collection<String>) {
    NotificationArtworkCache.removeAllMatchingUris(uris)
}
