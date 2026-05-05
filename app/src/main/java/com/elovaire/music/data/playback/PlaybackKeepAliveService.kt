package elovaire.music.app.data.playback

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

class PlaybackKeepAliveService : Service() {
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_NOTIFICATION, Notification::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_NOTIFICATION)
                }
                val notificationId = intent.getIntExtra(
                    EXTRA_NOTIFICATION_ID,
                    PlaybackNotificationController.NOTIFICATION_ID,
                )
                if (notification != null) {
                    PlaybackNotificationController.ensureNotificationChannel(this)
                    startForeground(notificationId, notification)
                } else {
                    stopSelf()
                }
            }

            ACTION_STOP -> {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        super.onDestroy()
    }

    companion object {
        private const val ACTION_START = "elovaire.music.app.action.PLAYBACK_SERVICE_START"
        private const val ACTION_STOP = "elovaire.music.app.action.PLAYBACK_SERVICE_STOP"
        private const val EXTRA_NOTIFICATION = "elovaire.music.app.extra.PLAYBACK_NOTIFICATION"
        private const val EXTRA_NOTIFICATION_ID = "elovaire.music.app.extra.PLAYBACK_NOTIFICATION_ID"

        fun start(
            context: Context,
            notificationId: Int,
            notification: Notification,
        ) {
            val intent = Intent(context, PlaybackKeepAliveService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(EXTRA_NOTIFICATION, notification)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PlaybackKeepAliveService::class.java))
        }
    }
}
