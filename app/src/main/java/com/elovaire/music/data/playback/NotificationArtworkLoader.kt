package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import elovaire.music.droidbeauty.app.data.artwork.ArtworkPurpose
import elovaire.music.droidbeauty.app.data.artwork.ArtworkRequestKey
import elovaire.music.droidbeauty.app.data.artwork.ArtworkBitmapCache
import elovaire.music.droidbeauty.app.data.artwork.artworkRequestKey
import elovaire.music.droidbeauty.app.data.artwork.invalidateArtworkBitmapCache
import elovaire.music.droidbeauty.app.data.artwork.loadArtworkBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
internal class NotificationArtworkLoader(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val pendingLoads = linkedMapOf<String, Job>()
    private var currentKey: ArtworkRequestKey? = null

    init {
        ArtworkBitmapCache.ensureRegistered(context.applicationContext)
    }

    fun setCurrentArtUri(uri: Uri?) {
        currentKey = uri?.let(::notificationArtworkLoadKey)
        trimPendingLoads(currentKey)
    }

    fun cachedBitmap(uri: Uri?): Bitmap? {
        val key = uri?.let(::notificationArtworkLoadKey) ?: return null
        return ArtworkBitmapCache[key.cacheKey]
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
    return loadArtworkBitmap(context, key)
}

private const val NOTIFICATION_ARTWORK_SIZE_PX = 1024

internal fun removeNotificationArtworkForUris(uris: Collection<String>) {
    invalidateArtworkBitmapCache(uris)
}
