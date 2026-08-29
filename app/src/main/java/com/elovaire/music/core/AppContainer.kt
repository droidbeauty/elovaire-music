package elovaire.music.droidbeauty.app.core

import android.app.Application
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.data.playback.PlaybackNotificationController
import elovaire.music.droidbeauty.app.data.settings.PortableSettingsBackup
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

enum class AppShortcutCommand {
    LastPlayed,
    Albums,
    Playlists,
    Search,
}

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
    private val portableSettingsBackup = ElovaireTrace.section("portable_settings_restore") {
        PortableSettingsBackup(applicationContext, ownerScope = appScope).also { it.restore() }
    }

    private val services = ElovaireTrace.section("app_services_init") {
        AppServices(
            applicationContext = applicationContext,
            appScope = appScope,
            backgroundWorkPolicy = backgroundWorkPolicy,
            portableSettingsBackup = portableSettingsBackup,
        )
    }
    private val bridgeCoordinator = AppBridgeCoordinator(appScope, services)
    private val dependencies = AppDependencies(services, backgroundWorkPolicy)
    val preferenceStore get() = services.preferenceStore
    internal val updateController get() = services.updateController
    internal val artistImageRepository get() = services.artistImageRepository
    internal val lyricsService get() = services.lyricsService
    internal val albumTagEditorService get() = services.albumTagEditorService
    val playbackManager get() = services.playbackManager
    val libraryRepository get() = services.libraryRepository
    internal val interactionWorkPolicy get() = backgroundWorkPolicy
    internal val rootReadDependencies get() = dependencies.rootReadDependencies
    internal val playbackActionDependencies get() = dependencies.playbackActionDependencies
    internal val libraryActionDependencies get() = dependencies.libraryActionDependencies
    internal val settingsActionDependencies get() = dependencies.settingsActionDependencies
    internal val playlistActionDependencies get() = dependencies.playlistActionDependencies
    internal val viewModelDependencies get() = dependencies.viewModelDependencies
    internal fun exportPortableUserData(): ByteArray = services.exportPortableUserData()
    internal fun importPortableUserData(bytes: ByteArray): Deferred<PlaylistMutationResult> =
        services.importPortableUserData(bytes)
    private val notificationControllerHolder = NotificationControllerHolder {
        PlaybackNotificationController.ensureNotificationChannel(applicationContext)
        PlaybackNotificationController(
            context = applicationContext,
            playbackManager = playbackManager,
            scope = appScope,
        )
    }
    private val openNowPlayingChannel = Channel<Unit>(capacity = Channel.CONFLATED)
    private val appShortcutChannel = Channel<AppShortcutCommand>(capacity = Channel.BUFFERED)
    private val coldStartHomeResetConsumed = AtomicBoolean(false)
    private val runtimeCoordinator = AppRuntimeCoordinator(
        startPlaybackAction = {
            services.startPlayback()
            bridgeCoordinator.startPlayback()
            notificationController().setNotificationsEnabled(true)
        },
        startAction = {
            services.start()
            bridgeCoordinator.start()
            notificationController().setNotificationsEnabled(true)
        },
        memoryPressureAction = services::onMemoryPressure,
        releaseAction = {
            openNowPlayingChannel.close()
            appShortcutChannel.close()
            bridgeCoordinator.release()
            notificationControllerHolder.release()
            services.release()
            appRuntimeScope.close()
            appForegroundTracker.close()
        },
    )
    val openNowPlayingCommands: Flow<Unit> = openNowPlayingChannel.receiveAsFlow()
    val appShortcutCommands: Flow<AppShortcutCommand> = appShortcutChannel.receiveAsFlow()

    fun start() {
        runtimeCoordinator.start()
    }

    internal fun startPlayback() {
        runtimeCoordinator.startPlayback()
    }

    fun requestOpenNowPlaying() {
        openNowPlayingChannel.trySend(Unit)
    }

    fun requestAppShortcut(command: AppShortcutCommand) {
        appShortcutChannel.trySend(command)
    }

    fun consumeColdStartHomeReset(): Boolean {
        return coldStartHomeResetConsumed.compareAndSet(false, true)
    }

    fun scheduleDeferredStartupWork() {
        bridgeCoordinator.scheduleDeferredStartupWork()
    }

    internal fun onMemoryPressure(pressure: MemoryPressure) {
        runtimeCoordinator.onMemoryPressure(pressure)
    }

    fun release() {
        runtimeCoordinator.release()
    }

    private fun notificationController(): PlaybackNotificationController {
        return notificationControllerHolder.get()
    }
}
