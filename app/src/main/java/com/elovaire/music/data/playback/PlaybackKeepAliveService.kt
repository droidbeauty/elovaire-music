package elovaire.music.droidbeauty.app.data.playback

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.getParcelableExtraCompat

@UnstableApi
class PlaybackKeepAliveService : Service() {
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notification = intent.getParcelableExtraCompat<Notification>(EXTRA_NOTIFICATION)
                val notificationId = intent.getIntExtra(
                    EXTRA_NOTIFICATION_ID,
                    PlaybackNotificationController.NOTIFICATION_ID,
                )
                if (notification != null) {
                    markRunning(notificationId)
                    PlaybackNotificationController.ensureNotificationChannel(this)
                    runCatching {
                        ServiceCompat.startForeground(
                            this,
                            notificationId,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
                        )
                    }.onFailure { throwable ->
                        logStartFailure("Unable to promote playback service to foreground", throwable)
                        clearRunningState()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && throwable is ForegroundServiceStartNotAllowedException) {
                            stopSelf()
                        } else {
                            stopSelf()
                        }
                    }
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
        clearRunningState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        private const val ACTION_START = "elovaire.music.droidbeauty.app.action.PLAYBACK_SERVICE_START"
        private const val ACTION_STOP = "elovaire.music.droidbeauty.app.action.PLAYBACK_SERVICE_STOP"
        private const val EXTRA_NOTIFICATION = "elovaire.music.droidbeauty.app.extra.PLAYBACK_NOTIFICATION"
        private const val EXTRA_NOTIFICATION_ID = "elovaire.music.droidbeauty.app.extra.PLAYBACK_NOTIFICATION_ID"

        fun start(
            context: Context,
            notificationId: Int,
            notification: Notification,
        ) {
            if (isRunningFor(notificationId)) return
            val intent = Intent(context, PlaybackKeepAliveService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(EXTRA_NOTIFICATION, notification)
            }
            runCatching {
                ContextCompat.startForegroundService(context, intent)
            }.onFailure { throwable ->
                logStartFailure("Unable to start playback foreground service", throwable)
            }
        }

        fun stop(context: Context) {
            clearRunningState()
            context.stopService(Intent(context, PlaybackKeepAliveService::class.java))
        }

        @Volatile
        private var runningNotificationId: Int? = null

        private fun isRunningFor(notificationId: Int): Boolean {
            return runningNotificationId == notificationId
        }

        private fun markRunning(notificationId: Int) {
            runningNotificationId = notificationId
        }

        private fun clearRunningState() {
            runningNotificationId = null
        }

        private fun logStartFailure(
            message: String,
            throwable: Throwable,
        ) {
            if (!BuildConfig.DEBUG) return
            Log.w(TAG, message, throwable)
        }

        private const val TAG = "PlaybackKeepAlive"
    }
}
