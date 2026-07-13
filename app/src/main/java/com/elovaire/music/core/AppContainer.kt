package elovaire.music.droidbeauty.app.core

import android.app.Application
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.data.playback.PlaybackNotificationController
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@OptIn(UnstableApi::class)
class AppContainer(
    appContext: Context,
) {
    private val applicationContext = appContext.applicationContext
    private val appForegroundTracker = ElovaireTrace.section("app_foreground_tracker_init") {
        AppForegroundTracker(applicationContext as Application)
    }
    private val backgroundWorkPolicy = AppBackgroundWorkPolicy(appForegroundTracker.isForeground)
    private val appRuntimeScope = AppRuntimeScope()
    private val appScope = appRuntimeScope.scope

    private val services = ElovaireTrace.section("app_services_init") {
        AppServices(
            applicationContext = applicationContext,
            appScope = appScope,
            backgroundWorkPolicy = backgroundWorkPolicy,
        )
    }
    private val bridgeCoordinator = AppBridgeCoordinator(appScope, services)
    private val dependencies = AppDependencies(services, backgroundWorkPolicy)
    val preferenceStore get() = services.preferenceStore
    internal val appUpdateManager get() = services.appUpdateManager
    val lyricsService get() = services.lyricsService
    internal val albumTagEditorService get() = services.albumTagEditorService
    val playbackManager get() = services.playbackManager
    val libraryRepository get() = services.libraryRepository
    internal val rootReadDependencies get() = dependencies.rootReadDependencies
    internal val playbackActionDependencies get() = dependencies.playbackActionDependencies
    internal val libraryActionDependencies get() = dependencies.libraryActionDependencies
    internal val settingsActionDependencies get() = dependencies.settingsActionDependencies
    internal val playlistActionDependencies get() = dependencies.playlistActionDependencies
    internal val viewModelDependencies get() = dependencies.viewModelDependencies
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
    private val started = AtomicBoolean(false)
    private val released = AtomicBoolean(false)
    val openNowPlayingCommands: Flow<Unit> = openNowPlayingChannel.receiveAsFlow()

    fun start() {
        if (released.get() || !started.compareAndSet(false, true)) return
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
        if (!released.compareAndSet(false, true)) return
        started.set(false)
        openNowPlayingChannel.close()
        bridgeCoordinator.release()
        notificationControllerHolder.release()
        services.release()
        appRuntimeScope.close()
        appForegroundTracker.close()
    }

    private fun notificationController(): PlaybackNotificationController {
        return notificationControllerHolder.get()
    }
}
