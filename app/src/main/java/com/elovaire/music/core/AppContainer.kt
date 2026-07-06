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
    val preferenceStore get() = services.preferenceStore
    internal val appUpdateManager get() = services.appUpdateManager
    val lyricsService get() = services.lyricsService
    internal val albumTagEditorService get() = services.albumTagEditorService
    val playbackManager get() = services.playbackManager
    val libraryRepository get() = services.libraryRepository
    internal val rootReadDependencies: RootReadDependencies = object : RootReadDependencies {
        override val libraryReader get() = services.libraryRepository
        override val rootSettingsReader get() = services.preferenceStore
        override val playbackReader get() = services.playbackManager
        override val updateReader get() = services.appUpdateManager
    }
    internal val playbackActionDependencies: PlaybackActionDependencies = object : PlaybackActionDependencies {
        override val playbackController get() = services.playbackManager
    }
    internal val libraryActionDependencies: LibraryActionDependencies = object : LibraryActionDependencies {
        override val libraryRepository get() = services.libraryRepository
    }
    internal val settingsActionDependencies: SettingsActionDependencies = object : SettingsActionDependencies {
        override val appearanceSettings get() = services.preferenceStore
        override val librarySettings get() = services.preferenceStore
        override val playbackSettings get() = services.preferenceStore
        override val setOnlineLyricsLookupEnabled = services.preferenceStore::setOnlineLyricsLookupEnabled
    }
    internal val playlistActionDependencies: PlaylistActionDependencies = object : PlaylistActionDependencies {
        override val playlistStore get() = services.preferenceStore
        override val favoritesStore get() = services.preferenceStore
    }
    internal val viewModelDependencies: ElovaireViewModelDependencies = object : ElovaireViewModelDependencies {
        override val libraryReader get() = services.libraryRepository
        override val libraryRepository get() = services.libraryRepository
        override val rootSettingsReader get() = services.preferenceStore
        override val preferenceStore get() = services.preferenceStore
        override val playbackReader get() = services.playbackManager
        override val playbackManager get() = services.playbackManager
        override val updateReader get() = services.appUpdateManager
        override val lyricsService get() = services.lyricsService
        override val albumTagEditorService get() = services.albumTagEditorService
        override val appUpdateManager get() = services.appUpdateManager
        override val backgroundWorkPolicy get() = this@AppContainer.backgroundWorkPolicy
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
        appForegroundTracker.close()
        appRuntimeScope.close()
    }

    private fun notificationController(): PlaybackNotificationController {
        return notificationControllerHolder.get()
    }
}
