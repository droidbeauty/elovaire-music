package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import elovaire.music.droidbeauty.app.data.playback.PlaybackNotificationController
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@SuppressLint("UnsafeOptInUsageError")
class AppContainer(
    appContext: Context,
) {
    private val applicationContext = appContext.applicationContext
    private val appForegroundTracker = AppForegroundTracker(applicationContext as Application)
    private val appRuntimeScope = AppRuntimeScope()
    private val appScope = appRuntimeScope.scope

    private val services = AppServices(
        applicationContext = applicationContext,
        appScope = appScope,
        appForegroundState = appForegroundTracker.isForeground,
    )
    private val bridgeCoordinator = AppBridgeCoordinator(appScope, services)
    val preferenceStore get() = services.preferenceStore
    val appUpdateManager get() = services.appUpdateManager
    val lyricsService get() = services.lyricsService
    internal val albumTagEditorService get() = services.albumTagEditorService
    val playbackManager get() = services.playbackManager
    val libraryRepository get() = services.libraryRepository
    internal val viewModelDependencies: ElovaireViewModelDependencies = object : ElovaireViewModelDependencies {
        override val libraryRepository get() = services.libraryRepository
        override val preferenceStore get() = services.preferenceStore
        override val playbackManager get() = services.playbackManager
        override val lyricsService get() = services.lyricsService
        override val albumTagEditorService get() = services.albumTagEditorService
    }
    private val notificationControllerHolder = NotificationControllerHolder {
        PlaybackNotificationController.ensureNotificationChannel(applicationContext)
        PlaybackNotificationController(
            context = applicationContext,
            playbackManager = playbackManager,
            scope = appScope,
        )
    }
    private val openNowPlayingChannel = Channel<Unit>(capacity = Channel.CONFLATED)
    private val coldStartHomeResetConsumed = AtomicBoolean(false)
    val openNowPlayingCommands: Flow<Unit> = openNowPlayingChannel.receiveAsFlow()

    init {
        bridgeCoordinator.start()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        if (!enabled) {
            notificationControllerHolder.release()
            return
        }
        notificationController().setNotificationsEnabled(enabled)
    }

    fun requestOpenNowPlaying() {
        openNowPlayingChannel.trySend(Unit)
    }

    fun consumeColdStartHomeReset(): Boolean {
        return coldStartHomeResetConsumed.compareAndSet(false, true)
    }

    fun scheduleDeferredStartupWork() {
        bridgeCoordinator.scheduleDeferredStartupWork()
    }

    fun release() {
        openNowPlayingChannel.close()
        notificationControllerHolder.release()
        services.release()
        appRuntimeScope.close()
    }

    private fun notificationController(): PlaybackNotificationController {
        return notificationControllerHolder.get()
    }
}
