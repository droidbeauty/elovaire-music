package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import elovaire.music.droidbeauty.app.data.library.db.PersistenceMaintenanceWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticRecorder
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics
@SuppressLint("UnsafeOptInUsageError")
@Suppress("TooGenericExceptionCaught")
internal class AppBridgeCoordinator(
    scope: CoroutineScope,
    private val services: AppServices,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val diagnostics: BackendDiagnosticRecorder = BackendDiagnostics,
) {
    private val bridgeScope = ownedChildScope(scope, "app-bridge", diagnostics)
    private val playbackIntegration = PlaybackIntegrationCoordinator(
        scope = bridgeScope,
        preferences = services.preferenceStore,
        history = services.userDataStore,
        library = services.libraryRepository,
        playback = services.playbackManager,
        effects = services.playbackEffectsController,
        sessionStore = services.playbackSessionStore,
        diagnostics = diagnostics,
        ioDispatcher = ioDispatcher,
    )
    private val preferences = services.preferenceStore
    private val library = services.libraryRepository
    private val applicationContext = services.applicationContext
    private val lifecycleLock = Any()
    private var deferredStartupJob: Job? = null
    private var libraryFoldersJob: Job? = null

    fun startPlayback() {
        if (!bridgeScope.isActive) return
        playbackIntegration.start()
    }

    fun start() {
        if (!bridgeScope.isActive) return
        if (libraryFoldersJob == null) {
            libraryFoldersJob = bridgeScope.launch {
                preferences.libraryFolders.collect(library::setLibraryFolders)
            }
        }
    }

    fun scheduleDeferredStartupWork() {
        synchronized(lifecycleLock) {
            if (!bridgeScope.isActive || deferredStartupJob?.isActive == true) return
            lateinit var job: Job
            job = bridgeScope.launch(ioDispatcher) {
                try {
                    PersistenceMaintenanceWorker.enqueue(applicationContext)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: RuntimeException) {
                    android.util.Log.w(
                        "AppBridgeCoordinator",
                        "Deferred persistence maintenance was not scheduled; retrying later.",
                        failure,
                    )
                } finally {
                    synchronized(lifecycleLock) {
                        if (deferredStartupJob === job) deferredStartupJob = null
                    }
                }
            }
            deferredStartupJob = job
        }
    }

    fun release() {
        synchronized(lifecycleLock) {
            releaseBestEffort(
                {
                    libraryFoldersJob?.cancel()
                    libraryFoldersJob = null
                },
                {
                    deferredStartupJob?.cancel()
                    deferredStartupJob = null
                },
                { playbackIntegration.release() },
                { bridgeScope.cancel() },
            )
        }
    }
}
