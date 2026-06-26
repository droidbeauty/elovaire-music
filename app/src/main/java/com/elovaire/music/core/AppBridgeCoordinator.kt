package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import kotlinx.coroutines.CoroutineScope

@SuppressLint("UnsafeOptInUsageError")
internal class AppBridgeCoordinator(
    scope: CoroutineScope,
    services: AppServices,
) {
    private val playbackSettingsBridge = PlaybackSettingsBridge(
        scope = scope,
        preferenceStore = services.preferenceStore,
        playbackManager = services.playbackManager,
        playbackEffectsController = services.playbackEffectsController,
    )
    private val playbackHistoryBridge = PlaybackHistoryBridge(
        scope = scope,
        preferenceStore = services.preferenceStore,
        playbackManager = services.playbackManager,
    )
    private val libraryPlaybackBridge = LibraryPlaybackBridge(
        scope = scope,
        libraryRepository = services.libraryRepository,
        playbackManager = services.playbackManager,
    )
    private val librarySettingsBridge = LibrarySettingsBridge(
        scope = scope,
        preferenceStore = services.preferenceStore,
        libraryRepository = services.libraryRepository,
    )
    private val startupCoordinator = StartupCoordinator(services.appUpdateManager)
    private var started = false

    fun start() {
        if (started) return
        started = true
        playbackSettingsBridge.start()
        playbackHistoryBridge.start()
        libraryPlaybackBridge.start()
        librarySettingsBridge.start()
        startupCoordinator.start()
    }

    fun scheduleDeferredStartupWork() {
        startupCoordinator.scheduleDeferredStartupWork()
    }
}
