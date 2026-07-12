package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@SuppressLint("UnsafeOptInUsageError")
internal class AppBridgeCoordinator(
    scope: CoroutineScope,
    services: AppServices,
) {
    private val bridgeJob = SupervisorJob(scope.coroutineContext[Job])
    private val bridgeScope = CoroutineScope(scope.coroutineContext + bridgeJob)
    private val playbackIntegration = PlaybackIntegrationCoordinator(
        scope = bridgeScope,
        preferences = services.preferenceStore,
        library = services.libraryRepository,
        playback = services.playbackManager,
        effects = services.playbackEffectsController,
    )
    private val librarySettingsBridge = LibrarySettingsBridge(
        scope = bridgeScope,
        preferenceStore = services.preferenceStore,
        libraryRepository = services.libraryRepository,
    )
    private val startupCoordinator = StartupCoordinator(services.appUpdateManager)
    private var started = false
    private var released = false

    fun start() {
        if (started || released) return
        started = true
        playbackIntegration.start()
        librarySettingsBridge.start()
        startupCoordinator.start()
    }

    fun scheduleDeferredStartupWork() {
        if (!started) return
        startupCoordinator.scheduleDeferredStartupWork()
    }

    fun release() {
        if (released) return
        released = true
        started = false
        bridgeScope.cancel()
    }
}
