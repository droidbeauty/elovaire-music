package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import elovaire.music.droidbeauty.app.data.settings.PlaybackIntegrationSettings
import elovaire.music.droidbeauty.app.data.library.db.PersistenceMaintenanceWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@SuppressLint("UnsafeOptInUsageError")
internal class AppBridgeCoordinator(
    scope: CoroutineScope,
    services: AppServices,
) {
    private val bridgeJob = SupervisorJob(scope.coroutineContext[Job])
    private val bridgeScope = CoroutineScope(scope.coroutineContext + bridgeJob)
    private val playbackSettings = object : PlaybackIntegrationSettings {
        override val eqSettings get() = services.preferenceStore.eqSettings
        override val gaplessPlaybackEnabled get() = services.preferenceStore.gaplessPlaybackEnabled
        override val volumeNormalizationEnabled get() = services.preferenceStore.volumeNormalizationEnabled

        override fun recordPlaybackTransition(songId: Long?, albumId: Long?) {
            services.preferenceStore.recordPlaybackTransition(songId, albumId)
        }
    }
    private val playbackIntegration = PlaybackIntegrationCoordinator(
        scope = bridgeScope,
        preferences = playbackSettings,
        library = services.libraryRepository,
        playback = services.playbackManager,
        effects = services.playbackEffectsController,
        sessionStore = services.playbackSessionStore,
    )
    private val preferences = services.preferenceStore
    private val library = services.libraryRepository
    private val appUpdateManager = services.appUpdateManager
    private val exitDiagnostics = services.exitDiagnostics
    private val applicationContext = services.applicationContext
    private var started = false
    private var released = false
    private var deferredStartupScheduled = false

    fun start() {
        if (started || released) return
        started = true
        appUpdateManager.start()
        playbackIntegration.start()
        bridgeScope.launch {
            preferences.libraryFolders.collect(library::setLibraryFolders)
        }
    }

    fun scheduleDeferredStartupWork() {
        if (!started || released || deferredStartupScheduled) return
        deferredStartupScheduled = true
        PersistenceMaintenanceWorker.enqueue(applicationContext)
        bridgeScope.launch(Dispatchers.IO) {
            if (!exitDiagnostics.inspect().suppressOptionalStartup) {
                appUpdateManager.scheduleStartupMaintenance()
            }
        }
    }

    fun release() {
        if (released) return
        released = true
        started = false
        playbackIntegration.release()
        bridgeScope.cancel()
    }
}
