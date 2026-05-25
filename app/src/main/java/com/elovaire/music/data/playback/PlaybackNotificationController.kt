package elovaire.music.app.data.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import elovaire.music.app.MainActivity
import elovaire.music.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

const val EXTRA_OPEN_PLAYER_FROM_NOTIFICATION = "elovaire.music.app.extra.OPEN_PLAYER_FROM_NOTIFICATION"

@UnstableApi
class PlaybackNotificationController(
    private val context: Context,
    private val playbackManager: PlaybackManager,
    private val scope: CoroutineScope,
) {
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

    init {
        scope.launch {
            playbackManager.manualPlaybackStartVersion.collect { version ->
                if (version == lastManualPlaybackStartVersion) return@collect
                lastManualPlaybackStartVersion = version
                notificationDismissedWhilePaused = false
                val currentState = playbackManager.state.value
                if (notificationsEnabled && shouldShowNotification(currentState)) {
                    notificationManager.setPlayer(playbackManager.playerInstance)
                }
            }
        }
        scope.launch {
            playbackManager.playerInstanceVersion.collect {
                if (!notificationsEnabled) return@collect
                val currentState = playbackManager.state.value
                if (shouldShowNotification(currentState)) {
                    notificationManager.setPlayer(playbackManager.playerInstance)
                }
            }
        }
        scope.launch {
            playbackManager.state.collectLatest { state ->
                if (!notificationsEnabled) return@collectLatest
                when {
                    state.currentSong == null -> {
                        pauseHideJob?.cancel()
                        notificationDismissedWhilePaused = false
                        notificationManager.setPlayer(null)
                    }
                    state.isPlaying -> {
                        pauseHideJob?.cancel()
                        if (notificationDismissedWhilePaused) {
                            notificationManager.setPlayer(null)
                            return@collectLatest
                        }
                        notificationManager.setPlayer(playbackManager.playerInstance)
                    }
                    else -> {
                        pauseHideJob?.cancel()
                        if (notificationDismissedWhilePaused) {
                            notificationManager.setPlayer(null)
                            return@collectLatest
                        }
                        notificationManager.setPlayer(playbackManager.playerInstance)
                        pauseHideJob = launch {
                            delay(PAUSE_NOTIFICATION_TIMEOUT_MS)
                            val latestState = playbackManager.state.value
                            if (
                                notificationsEnabled &&
                                latestState.currentSong != null &&
                                !latestState.isPlaying &&
                                !notificationDismissedWhilePaused
                            ) {
                                notificationManager.setPlayer(null)
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
            notificationManager.setPlayer(null)
            return
        }
        val currentState = playbackManager.state.value
        if (shouldShowNotification(currentState)) {
            notificationManager.setPlayer(playbackManager.playerInstance)
        } else {
            notificationManager.setPlayer(null)
        }
    }

    private fun shouldShowNotification(currentState: PlaybackUiState): Boolean {
        if (currentState.currentSong == null) return false
        if (notificationDismissedWhilePaused) return false
        return currentState.isPlaying || currentState.currentSong != null
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
            return playbackManager.state.value.currentSong
                ?.artUri
                ?.let { loadBitmap(context, it) }
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

    companion object {
        internal const val NOTIFICATION_CHANNEL_ID = "elovaire_playback"
        internal const val NOTIFICATION_ID = 1001
        private const val ACTION_SHUFFLE = "elovaire.music.app.action.SHUFFLE"
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
            return runCatching {
                context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            }.getOrNull()
        }
    }
}
