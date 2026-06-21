package elovaire.music.droidbeauty.app.data.playback

import android.app.PendingIntent
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import android.util.Size
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import elovaire.music.droidbeauty.app.MainActivity
import elovaire.music.droidbeauty.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

const val EXTRA_OPEN_PLAYER_FROM_NOTIFICATION = "elovaire.music.droidbeauty.app.extra.OPEN_PLAYER_FROM_NOTIFICATION"

internal fun invalidateNotificationArtworkCache(uris: Collection<Uri?>) {
    val keys = uris
        .filterNotNull()
        .map(Uri::toString)
        .filter(String::isNotBlank)
    if (keys.isEmpty()) return
    NotificationArtworkCache.removeAll(keys)
}

@UnstableApi
class PlaybackNotificationController(
    private val context: Context,
    private val playbackManager: PlaybackManager,
    private val scope: CoroutineScope,
) {
    private val pendingArtworkLoads = linkedMapOf<String, Job>()

    init {
        NotificationArtworkCache.ensureRegistered(context.applicationContext)
    }

    private val notificationManager = PlayerNotificationManager.Builder(
        context,
        NOTIFICATION_ID,
        NOTIFICATION_CHANNEL_ID,
    )
        .setMediaDescriptionAdapter(NotificationDescriptionAdapter())
        .setCustomActionReceiver(ShuffleActionReceiver())
        .setNotificationListener(
            object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: android.app.Notification,
                    ongoing: Boolean,
                ) {
                    if (ongoing || playbackManager.state.value.currentSong != null) {
                        PlaybackKeepAliveService.start(context, notificationId, notification)
                    } else {
                        PlaybackKeepAliveService.stop(context)
                    }
                }

                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean,
                ) {
                    if (dismissedByUser) {
                        val currentState = playbackManager.state.value
                        if (currentState.currentSong != null && !currentState.isPlaying) {
                            notificationDismissedWhilePaused = true
                            pauseHideJob?.cancel()
                            updateNotificationPlayer(null)
                        }
                    }
                    PlaybackKeepAliveService.stop(context)
                }
            },
        )
        .build()
        .apply {
            setSmallIcon(R.drawable.ic_lucide_disc_3)
            setMediaSessionToken(playbackManager.platformMediaSessionToken)
            setUseFastForwardAction(false)
            setUseRewindAction(false)
            setUsePreviousAction(true)
            setUseNextAction(true)
            setUseChronometer(false)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setPlayer(null)
        }

    private var notificationsEnabled = false
    private var pauseHideJob: Job? = null
    private var notificationDismissedWhilePaused = false
    private var lastManualPlaybackStartVersion = 0L
    private var attachedPlayer: Player? = null

    init {
        scope.launch {
            playbackManager.manualPlaybackStartVersion.collect { version ->
                if (version == lastManualPlaybackStartVersion) return@collect
                lastManualPlaybackStartVersion = version
                notificationDismissedWhilePaused = false
                val currentState = playbackManager.state.value
                if (notificationsEnabled && shouldShowNotification(currentState)) {
                    updateNotificationPlayer(playbackManager.playerInstance)
                }
            }
        }
        scope.launch {
            playbackManager.playerInstanceVersion.collect {
                if (!notificationsEnabled) return@collect
                val currentState = playbackManager.state.value
                if (shouldShowNotification(currentState)) {
                    updateNotificationPlayer(playbackManager.playerInstance)
                }
            }
        }
        scope.launch {
            combine(
                playbackManager.nowPlayingState,
                playbackManager.transportState,
            ) { nowPlaying, transport ->
                    NotificationVisibilityState(
                        songId = nowPlaying.currentSong?.id,
                        isPlaying = transport.isPlaying,
                        metadataSignature = nowPlaying.currentSong?.let { song ->
                            "${song.title}\u0000${song.artist}\u0000${song.album}\u0000${song.artUri}"
                        },
                    )
                }
                .distinctUntilChanged()
                .collectLatest {
                    val state = playbackManager.state.value
                if (!notificationsEnabled) return@collectLatest
                notificationManager.invalidate()
                when {
                    state.currentSong == null -> {
                        pauseHideJob?.cancel()
                        notificationDismissedWhilePaused = false
                        updateNotificationPlayer(null)
                    }
                    state.isPlaying -> {
                        pauseHideJob?.cancel()
                        if (notificationDismissedWhilePaused) {
                            updateNotificationPlayer(null)
                            return@collectLatest
                        }
                        updateNotificationPlayer(playbackManager.playerInstance)
                    }
                    else -> {
                        pauseHideJob?.cancel()
                        if (notificationDismissedWhilePaused) {
                            updateNotificationPlayer(null)
                            return@collectLatest
                        }
                        updateNotificationPlayer(playbackManager.playerInstance)
                        pauseHideJob = launch {
                            delay(PAUSE_NOTIFICATION_TIMEOUT_MS)
                            val latestState = playbackManager.state.value
                            if (
                                notificationsEnabled &&
                                latestState.currentSong != null &&
                                !latestState.isPlaying &&
                                !notificationDismissedWhilePaused
                            ) {
                                updateNotificationPlayer(null)
                            }
                        }
                    }
                }
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled = enabled
        if (!enabled) {
            pauseHideJob?.cancel()
            updateNotificationPlayer(null)
            return
        }
        val currentState = playbackManager.state.value
        if (shouldShowNotification(currentState)) {
            updateNotificationPlayer(playbackManager.playerInstance)
        } else {
            updateNotificationPlayer(null)
        }
    }

    private fun shouldShowNotification(currentState: PlaybackUiState): Boolean {
        if (currentState.currentSong == null) return false
        if (notificationDismissedWhilePaused) return false
        return true
    }

    private fun updateNotificationPlayer(player: Player?) {
        if (attachedPlayer === player) return
        attachedPlayer = player
        notificationManager.setPlayer(player)
    }

    private inner class NotificationDescriptionAdapter : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return playbackManager.state.value.currentSong?.title.orEmpty()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION, true)
            }
            return PendingIntent.getActivity(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        override fun getCurrentContentText(player: Player): CharSequence {
            return playbackManager.state.value.currentSong?.artist.orEmpty()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback,
        ): Bitmap? {
            val artworkUri = playbackManager.state.value.currentSong?.artUri ?: return null
            NotificationArtworkCache[artworkUri.toString()]?.let { return it }
            loadArtworkAsync(artworkUri, callback)
            return null
        }
    }

    private inner class ShuffleActionReceiver : PlayerNotificationManager.CustomActionReceiver {
        override fun createCustomActions(
            context: Context,
            instanceId: Int,
        ): MutableMap<String, NotificationCompat.Action> {
            val shuffleIntent = Intent(ACTION_SHUFFLE).setPackage(context.packageName)
            return mutableMapOf(
                ACTION_SHUFFLE to NotificationCompat.Action(
                    R.drawable.ic_lucide_shuffle,
                    "Shuffle",
                    PendingIntent.getBroadcast(
                        context,
                        instanceId,
                        shuffleIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    ),
                ),
            )
        }

        override fun getCustomActions(player: Player): MutableList<String> {
            return mutableListOf(ACTION_SHUFFLE)
        }

        override fun onCustomAction(
            player: Player,
            action: String,
            intent: Intent,
        ) {
            if (action == ACTION_SHUFFLE) {
                playbackManager.toggleShuffle()
            }
        }
    }

    private fun loadArtworkAsync(
        uri: Uri,
        callback: PlayerNotificationManager.BitmapCallback,
    ) {
        val cacheKey = uri.toString()
        if (pendingArtworkLoads[cacheKey]?.isActive == true) return
        pendingArtworkLoads[cacheKey] = scope.launch(Dispatchers.IO) {
            val bitmap = loadBitmap(context, uri)
            withContext(Dispatchers.Main.immediate) {
                pendingArtworkLoads.remove(cacheKey)
                if (bitmap != null && playbackManager.state.value.currentSong?.artUri == uri) {
                    callback.onBitmap(bitmap)
                }
            }
        }
    }

    companion object {
        internal const val NOTIFICATION_CHANNEL_ID = "elovaire_playback"
        internal const val NOTIFICATION_ID = 1001
        private const val ACTION_SHUFFLE = "elovaire.music.droidbeauty.app.action.SHUFFLE"
        private const val PAUSE_NOTIFICATION_TIMEOUT_MS = 180_000L

        fun ensureNotificationChannel(context: Context) {
            NotificationUtil.createNotificationChannel(
                context,
                NOTIFICATION_CHANNEL_ID,
                R.string.app_name,
                R.string.app_name,
                NotificationUtil.IMPORTANCE_LOW,
            )
        }

        private fun loadBitmap(context: Context, uri: Uri): Bitmap? {
            NotificationArtworkCache[uri.toString()]?.let { return it }
            val bitmap = runCatching {
                context.contentResolver.loadThumbnail(uri, Size(NOTIFICATION_ARTWORK_SIZE_PX, NOTIFICATION_ARTWORK_SIZE_PX), null)
            }.getOrNull() ?: runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, bounds)
                }
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.RGB_565
                    inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, NOTIFICATION_ARTWORK_SIZE_PX)
                }
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, options)
                }
            }.getOrNull()
            return bitmap?.also { cachedBitmap ->
                NotificationArtworkCache.put(uri.toString(), cachedBitmap)
            }
        }

        private fun calculateInSampleSize(
            width: Int,
            height: Int,
            targetSize: Int,
        ): Int {
            if (width <= 0 || height <= 0 || targetSize <= 0) return 1
            var sampleSize = 1
            var halfWidth = width / 2
            var halfHeight = height / 2
            while (halfWidth / sampleSize >= targetSize && halfHeight / sampleSize >= targetSize) {
                sampleSize *= 2
            }
            return sampleSize.coerceAtLeast(1)
        }

        private const val NOTIFICATION_ARTWORK_SIZE_PX = 256
    }

    private data class NotificationVisibilityState(
        val songId: Long?,
        val isPlaying: Boolean,
        val metadataSignature: String?,
    )

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
            @Suppress("DEPRECATION")
            override fun onLowMemory() {
                trim(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
            }

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

    fun removeAll(keys: Collection<String>) {
        keys.forEach(cache::remove)
    }

    @Suppress("DEPRECATION")
    @Synchronized
    private fun trim(level: Int) {
        when {
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL ||
                level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> cache.evictAll()
            level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> cache.trimToSize((maxCacheBytes / 2).coerceAtLeast(1))
        }
    }
}
