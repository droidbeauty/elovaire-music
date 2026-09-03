package elovaire.music.droidbeauty.app.data.playback

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import elovaire.music.droidbeauty.app.ElovaireApp
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.getParcelableExtraCompat

@OptIn(UnstableApi::class)
class ElovaireMediaLibraryService : MediaLibraryService() {
    private val mainHandler by lazy(LazyThreadSafetyMode.NONE) { Handler(mainLooper) }
    private var removeForegroundNotificationOnDestroy = true
    private val mediaButtonStateCheck = Runnable {
        val player = (application as ElovaireApp).container.playbackManager.playerInstance
        if (!player.playWhenReady && !isPlaybackOngoing) {
            pauseAllPlayersAndStopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> {
                removeForegroundNotificationOnDestroy = true
                val notification = intent.getParcelableExtraCompat<Notification>(EXTRA_NOTIFICATION)
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, DEFAULT_NOTIFICATION_ID)
                if (notification == null) {
                    invalidateForegroundRequest(intent.getLongExtra(EXTRA_REQUEST_GENERATION, 0L))
                    stopSelf()
                } else if (!isCurrentForegroundRequest(
                        notificationId,
                        intent.getLongExtra(EXTRA_REQUEST_GENERATION, 0L),
                    )
                ) {
                    stopSelf(startId)
                } else {
                    runCatching {
                        ServiceCompat.startForeground(
                            this,
                            notificationId,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
                        )
                    }.onSuccess {
                        markForegroundRequestComplete(
                            notificationId,
                            intent.getLongExtra(EXTRA_REQUEST_GENERATION, 0L),
                        )
                    }.onFailure { throwable ->
                        logForegroundFailure("Unable to promote media library service", throwable)
                        invalidateForegroundRequest(
                            intent.getLongExtra(EXTRA_REQUEST_GENERATION, 0L),
                        )
                        stopSelf()
                    }
                }
            }

            ACTION_DEMOTE -> {
                removeForegroundNotificationOnDestroy = false
                cancelForegroundRequest()
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
            }

            Intent.ACTION_MEDIA_BUTTON -> {
                mainHandler.removeCallbacks(mediaButtonStateCheck)
                mainHandler.post(mediaButtonStateCheck)
            }
        }
        return result
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        if (removeForegroundNotificationOnDestroy) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        cancelForegroundRequest()
        super.onDestroy()
    }

    /**
     * The app's custom notification controller is the only foreground owner. Keeping Media3's
     * default provider disabled prevents this session-only service from creating a second
     * notification or foreground-service lifecycle.
     */
    override fun onUpdateNotification(
        session: MediaSession,
        startInForegroundRequired: Boolean,
    ) = Unit

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo,
    ): MediaLibrarySession? {
        return (application as ElovaireApp).container
            .also { it.startPlayback() }
            .playbackManager
            .mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        val playbackManager = (application as ElovaireApp).container
            .also { it.startPlayback() }
            .playbackManager
        if (!playbackManager.state.value.transportShowsPause) {
            stopSelf()
            super.onTaskRemoved(rootIntent)
        }
    }

    companion object {
        private const val ACTION_START = "elovaire.music.droidbeauty.app.action.PLAYBACK_SERVICE_START"
        private const val ACTION_DEMOTE = "elovaire.music.droidbeauty.app.action.PLAYBACK_SERVICE_DEMOTE"
        private const val EXTRA_NOTIFICATION = "elovaire.music.droidbeauty.app.extra.PLAYBACK_NOTIFICATION"
        private const val EXTRA_NOTIFICATION_ID = "elovaire.music.droidbeauty.app.extra.PLAYBACK_NOTIFICATION_ID"
        private const val EXTRA_REQUEST_GENERATION = "elovaire.music.droidbeauty.app.extra.PLAYBACK_REQUEST_GENERATION"
        private const val DEFAULT_NOTIFICATION_ID = 4101

        private val foregroundRequestLock = Any()
        private var foregroundRequestGeneration = 0L
        private var pendingForegroundRequest: ForegroundRequest? = null

        @Volatile
        private var runningNotificationId: Int? = null

        private data class ForegroundRequest(
            val generation: Long,
            val notificationId: Int,
        )

        fun start(
            context: Context,
            notificationId: Int,
            notification: Notification,
        ) {
            val generation = synchronized(foregroundRequestLock) {
                if (runningNotificationId == notificationId ||
                    pendingForegroundRequest?.notificationId == notificationId
                ) {
                    return
                }
                foregroundRequestGeneration += 1L
                pendingForegroundRequest = ForegroundRequest(foregroundRequestGeneration, notificationId)
                foregroundRequestGeneration
            }
            val intent = Intent(context, ElovaireMediaLibraryService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(EXTRA_NOTIFICATION, notification)
                putExtra(EXTRA_REQUEST_GENERATION, generation)
            }
            runCatching {
                ContextCompat.startForegroundService(context, intent)
            }.onFailure { throwable ->
                synchronized(foregroundRequestLock) {
                    if (pendingForegroundRequest?.generation == generation) {
                        pendingForegroundRequest = null
                    }
                }
                logForegroundFailure("Unable to start media library foreground service", throwable)
            }
        }

        fun stop(context: Context) {
            cancelForegroundRequest()
            context.stopService(Intent(context, ElovaireMediaLibraryService::class.java))
        }

        fun demote(context: Context) {
            val shouldDemote = synchronized(foregroundRequestLock) {
                if (runningNotificationId == null && pendingForegroundRequest == null) {
                    false
                } else {
                    foregroundRequestGeneration += 1L
                    pendingForegroundRequest = null
                    runningNotificationId = null
                    true
                }
            }
            if (!shouldDemote) return
            val intent = Intent(context, ElovaireMediaLibraryService::class.java).apply {
                action = ACTION_DEMOTE
            }
            runCatching { context.startService(intent) }
                .onFailure { throwable ->
                    logForegroundFailure("Unable to demote media library foreground service", throwable)
                }
        }

        private fun isCurrentForegroundRequest(notificationId: Int, generation: Long): Boolean {
            return synchronized(foregroundRequestLock) {
                pendingForegroundRequest?.let {
                    it.notificationId == notificationId && it.generation == generation
                } ?: (runningNotificationId == notificationId)
            }
        }

        private fun markForegroundRequestComplete(notificationId: Int, generation: Long) {
            synchronized(foregroundRequestLock) {
                if (pendingForegroundRequest?.generation == generation) {
                    pendingForegroundRequest = null
                    runningNotificationId = notificationId
                }
            }
        }

        private fun invalidateForegroundRequest(generation: Long) {
            synchronized(foregroundRequestLock) {
                if (generation == 0L || pendingForegroundRequest?.generation == generation) {
                    pendingForegroundRequest = null
                    runningNotificationId = null
                }
            }
        }

        private fun cancelForegroundRequest() {
            synchronized(foregroundRequestLock) {
                foregroundRequestGeneration += 1L
                pendingForegroundRequest = null
                runningNotificationId = null
            }
        }

        private fun logForegroundFailure(message: String, throwable: Throwable) {
            if (BuildConfig.DEBUG) Log.w(TAG, message, throwable)
        }

        private const val TAG = "MediaLibraryService"
    }
}
