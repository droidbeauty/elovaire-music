package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.artwork.ArtworkPurpose
import elovaire.music.droidbeauty.app.data.artwork.ArtworkRequestKey
import elovaire.music.droidbeauty.app.data.artwork.ArtworkBitmapCache
import elovaire.music.droidbeauty.app.data.artwork.artworkRequestKey
import elovaire.music.droidbeauty.app.data.artwork.encodeArtworkForMediaSession
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

    fun setCurrentArtwork(
        mediaUri: Uri?,
        fallbackArtUri: Uri?,
    ) {
        currentKey = artworkSources(mediaUri, fallbackArtUri)
            .firstOrNull()
            ?.let(::notificationArtworkLoadKey)
        trimPendingLoads(currentKey)
    }

    fun cachedBitmap(
        mediaUri: Uri?,
        fallbackArtUri: Uri?,
    ): Bitmap? {
        return artworkSources(mediaUri, fallbackArtUri).firstNotNullOfOrNull { uri ->
            ArtworkBitmapCache[notificationArtworkLoadKey(uri).cacheKey]
        }
    }

    fun loadAsync(
        mediaUri: Uri?,
        fallbackArtUri: Uri?,
        isStillCurrent: (ArtworkRequestKey) -> Boolean,
        callback: PlayerNotificationManager.BitmapCallback,
        onMediaSessionArtworkLoaded: ((ByteArray) -> Unit)? = null,
    ) {
        val sources = artworkSources(mediaUri, fallbackArtUri)
        val primaryUri = sources.firstOrNull() ?: return
        val key = notificationArtworkLoadKey(primaryUri)
        val cacheKey = key.cacheKey
        if (pendingLoads[cacheKey]?.isActive == true) return
        pendingLoads[cacheKey] = scope.launch(Dispatchers.IO) {
            try {
                val bitmap = loadBitmap(context, sources)
                val mediaSessionArtwork = bitmap?.let(::encodeArtworkForMediaSession)
                if (BuildConfig.DEBUG && bitmap != null) {
                    Log.d(
                        TAG,
                        "decoded width=${bitmap.width} height=${bitmap.height} " +
                            "config=${bitmap.config} media_session_bytes=${mediaSessionArtwork?.size ?: 0} " +
                            "source_count=${sources.size}",
                    )
                }
                withContext(Dispatchers.Main.immediate) {
                    if (bitmap != null && isStillCurrent(key)) {
                        mediaSessionArtwork?.let { onMediaSessionArtworkLoaded?.invoke(it) }
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

private fun artworkSources(
    mediaUri: Uri?,
    fallbackArtUri: Uri?,
): List<Uri> {
    return listOfNotNull(fallbackArtUri, mediaUri)
        .filter { it.toString().isNotBlank() }
        .distinct()
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
    sources: List<Uri>,
): Bitmap? {
    return sources.firstNotNullOfOrNull { source ->
        loadArtworkBitmap(context, notificationArtworkLoadKey(source))
    }
}

private const val NOTIFICATION_ARTWORK_SIZE_PX = 1024
private const val TAG = "NotificationArtwork"

internal fun removeNotificationArtworkForUris(uris: Collection<String>) {
    invalidateArtworkBitmapCache(uris)
}
