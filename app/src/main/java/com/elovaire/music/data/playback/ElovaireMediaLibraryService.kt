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
        val player = (application as ElovaireApp).container.playbackPlayer
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
                    foregroundRequests.invalidate(intent.getLongExtra(EXTRA_REQUEST_GENERATION, 0L))
                    if (!foregroundRequests.hasActiveRequest()) stopSelf(startId)
                } else if (!foregroundRequests.isCurrentStart(
                        notificationId,
                        intent.getLongExtra(EXTRA_REQUEST_GENERATION, 0L),
                    )
                ) {
                    if (!foregroundRequests.hasActiveRequest()) stopSelf(startId)
                } else {
                    runCatching {
                        ServiceCompat.startForeground(
                            this,
                            notificationId,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
                        )
                    }.onSuccess {
                        foregroundRequests.completeStart(
                            notificationId,
                            intent.getLongExtra(EXTRA_REQUEST_GENERATION, 0L),
                        )
                    }.onFailure { throwable ->
                        logForegroundFailure("Unable to promote media library service", throwable)
                        foregroundRequests.invalidate(
                            intent.getLongExtra(EXTRA_REQUEST_GENERATION, 0L),
                        )
                        if (!foregroundRequests.hasActiveRequest()) stopSelf(startId)
                    }
                }
            }

            ACTION_DEMOTE -> {
                if (foregroundRequests.isCurrentDemotion(intent.getLongExtra(EXTRA_REQUEST_GENERATION, 0L))) {
                    removeForegroundNotificationOnDestroy = false
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf(startId)
                }
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
        foregroundRequests.serviceDestroyed()
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
            .playbackRuntime
            .mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        val playbackManager = (application as ElovaireApp).container
            .also { it.startPlayback() }
            .playbackRuntime
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

        private val foregroundRequests = PlaybackForegroundRequests()

        fun start(
            context: Context,
            notificationId: Int,
            notification: Notification,
        ) {
            val generation = foregroundRequests.requestStart(notificationId) ?: return
            val intent = Intent(context, ElovaireMediaLibraryService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(EXTRA_NOTIFICATION, notification)
                putExtra(EXTRA_REQUEST_GENERATION, generation)
            }
            runCatching {
                ContextCompat.startForegroundService(context, intent)
            }.onFailure { throwable ->
                foregroundRequests.startDispatchFailed(generation)
                logForegroundFailure("Unable to start media library foreground service", throwable)
            }
        }

        fun stop(context: Context) {
            foregroundRequests.cancel()
            context.stopService(Intent(context, ElovaireMediaLibraryService::class.java))
        }

        fun demote(context: Context) {
            val generation = foregroundRequests.requestDemotion() ?: return
            val intent = Intent(context, ElovaireMediaLibraryService::class.java).apply {
                action = ACTION_DEMOTE
                putExtra(EXTRA_REQUEST_GENERATION, generation)
            }
            runCatching { context.startService(intent) }
                .onFailure { throwable ->
                    logForegroundFailure("Unable to demote media library foreground service", throwable)
                }
        }

        private fun logForegroundFailure(message: String, throwable: Throwable) {
            if (BuildConfig.DEBUG) Log.w(TAG, message, throwable)
        }

        private const val TAG = "MediaLibraryService"
    }
}

internal class PlaybackForegroundRequests {
    private var generation = 0L
    private var pendingId: Int? = null
    private var runningId: Int? = null

    @Synchronized
    fun requestStart(notificationId: Int): Long? {
        if (runningId == notificationId || pendingId == notificationId) return null
        generation += 1L
        pendingId = notificationId
        return generation
    }

    @Synchronized
    fun requestDemotion(): Long? {
        if (runningId == null && pendingId == null) return null
        generation += 1L
        pendingId = null
        runningId = null
        return generation
    }

    @Synchronized
    fun isCurrentStart(notificationId: Int, requestGeneration: Long): Boolean =
        requestGeneration == generation && (pendingId == notificationId || runningId == notificationId)

    @Synchronized
    fun isCurrentDemotion(requestGeneration: Long): Boolean =
        requestGeneration != 0L && requestGeneration == generation && pendingId == null && runningId == null

    @Synchronized
    fun completeStart(notificationId: Int, requestGeneration: Long) {
        if (generation == requestGeneration && pendingId == notificationId) {
            pendingId = null
            runningId = notificationId
        }
    }

    @Synchronized
    fun startDispatchFailed(requestGeneration: Long) {
        if (generation == requestGeneration) pendingId = null
    }

    @Synchronized
    fun invalidate(requestGeneration: Long) {
        if (generation == requestGeneration && pendingId != null) {
            pendingId = null
            runningId = null
        }
    }

    @Synchronized
    fun hasActiveRequest(): Boolean = pendingId != null || runningId != null

    @Synchronized
    fun cancel() {
        generation += 1L
        pendingId = null
        runningId = null
    }

    @Synchronized
    fun serviceDestroyed() {
        runningId = null
    }
}
