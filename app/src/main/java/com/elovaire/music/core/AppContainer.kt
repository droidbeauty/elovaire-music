package elovaire.music.droidbeauty.app.core

import android.app.Application
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticsRuntime
import elovaire.music.droidbeauty.app.data.playback.PlaybackNotificationController
import elovaire.music.droidbeauty.app.data.settings.PortableSettingsBackup
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.data.artwork.invalidateArtworkCaches
import elovaire.music.droidbeauty.app.data.tags.AlbumTagArtworkInvalidator
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
    private val backendDiagnostics = BackendDiagnosticsRuntime()
    private val appForegroundTracker = ElovaireTrace.section("app_foreground_tracker_init") {
        AppForegroundTracker(
            application = applicationContext as Application,
            resourceTracker = backendDiagnostics.resources,
        )
    }
    private val backgroundWorkPolicy = AppBackgroundWorkPolicy(appForegroundTracker.isForeground)
    private val appDispatchers = AppDispatchers.production()
    private val appRuntimeScope = AppRuntimeScope(backendDiagnostics)
    private val appScope = appRuntimeScope.scope
    private val portableSettingsBackup = ElovaireTrace.section("portable_settings_init") {
        PortableSettingsBackup(
            context = applicationContext,
            ioDispatcher = appDispatchers.io,
            ownerScope = appScope,
        )
    }

    private val services = ElovaireTrace.section("app_services_init") {
        AppServices(
            applicationContext = applicationContext,
            appScope = appScope,
            appDispatchers = appDispatchers,
            backgroundWorkPolicy = backgroundWorkPolicy,
            portableSettingsBackup = portableSettingsBackup,
            backendDiagnostics = backendDiagnostics,
        )
    }
    private val bridgeCoordinator = AppBridgeCoordinator(
        scope = appScope,
        services = services,
        ioDispatcher = appDispatchers.io,
        diagnostics = backendDiagnostics,
    )
    private val dependencies = AppDependencies(
        applicationContext = applicationContext,
        services = services,
        appDispatchers = appDispatchers,
        artworkInvalidator = AlbumTagArtworkInvalidator { uris ->
            invalidateArtworkCaches(uris)
        },
    )
    internal val updateController get() = services.updateController
    internal val artistImageRepository get() = services.artistImageRepository
    internal val lyricsService get() = services.lyricsService
    internal val albumTagEditorService get() = services.albumTagEditorService
    val playbackManager get() = services.playbackManager
    internal val audiobookChapterReader get() = services.audiobookChapterReader
    internal val audiobookDescriptionReader get() = services.audiobookDescriptionReader
    internal val interactionWorkPolicy get() = backgroundWorkPolicy
    internal val dispatchers: AppDispatchers get() = appDispatchers
    internal val playbackResumptionGateway get() = services.playbackResumptionGateway
    internal val rootLibraryReader get() = services.libraryRepository
    internal val rootSettingsReader get() = services.preferenceStore
    internal val rootDeleteDependencies get() = dependencies.rootDeleteDependencies
    internal val libraryActionDependencies get() = dependencies.libraryActionDependencies
    internal val settingsActionDependencies get() = dependencies.settingsActionDependencies
    internal val playlistStore get() = services.userDataStore
    internal val favoritesStore get() = services.userDataStore
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
    // A shortcut is a navigation request, so only the newest request remains meaningful. Keeping
    // this conflated also prevents a burst of launcher taps from accumulating work behind UI.
    private val appShortcutChannel = Channel<AppShortcutCommand>(capacity = Channel.CONFLATED)
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
            releaseBestEffort(
                { openNowPlayingChannel.close() },
                { appShortcutChannel.close() },
                { bridgeCoordinator.release() },
                { notificationControllerHolder.release() },
                { services.release() },
                { appRuntimeScope.close() },
                { appForegroundTracker.close() },
                { backendDiagnostics.close() },
            )
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

    internal fun launchApplicationWork(block: suspend CoroutineScope.() -> Unit): Job {
        return appScope.launch(block = block)
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
        runtimeCoordinator.scheduleDeferredStartupWork(bridgeCoordinator::scheduleDeferredStartupWork)
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
