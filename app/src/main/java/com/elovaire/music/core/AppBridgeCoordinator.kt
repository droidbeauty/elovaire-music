package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import elovaire.music.droidbeauty.app.data.library.db.PersistenceMaintenanceWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("UnsafeOptInUsageError")
internal class AppBridgeCoordinator(
    scope: CoroutineScope,
    private val services: AppServices,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val bridgeJob = SupervisorJob(scope.coroutineContext[Job])
    private val bridgeScope = CoroutineScope(scope.coroutineContext + bridgeJob)
    private val playbackIntegration = PlaybackIntegrationCoordinator(
        scope = bridgeScope,
        preferences = services.preferenceStore,
        library = services.libraryRepository,
        playback = services.playbackManager,
        effects = services.playbackEffectsController,
        sessionStore = services.playbackSessionStore,
        ioDispatcher = ioDispatcher,
    )
    private val preferences = services.preferenceStore
    private val library = services.libraryRepository
    private val applicationContext = services.applicationContext
    private val lifecycleLock = Any()
    private val playbackStarted = AtomicBoolean(false)
    private val appStarted = AtomicBoolean(false)
    private val released = AtomicBoolean(false)
    private val deferredStartupScheduled = AtomicBoolean(false)

    fun startPlayback() {
        synchronized(lifecycleLock) {
            if (released.get() || !playbackStarted.compareAndSet(false, true)) return
            playbackIntegration.start()
            bridgeScope.launch {
                preferences.libraryFolders.collect(library::setLibraryFolders)
            }
        }
    }

    fun start() {
        synchronized(lifecycleLock) {
            if (released.get()) return
            startPlayback()
            appStarted.set(true)
        }
    }

    fun scheduleDeferredStartupWork() {
        synchronized(lifecycleLock) {
            if (!appStarted.get() || released.get() || !deferredStartupScheduled.compareAndSet(false, true)) return
            bridgeScope.launch(ioDispatcher) {
                PersistenceMaintenanceWorker.enqueue(applicationContext)
            }
        }
    }

    fun release() {
        synchronized(lifecycleLock) {
            if (!released.compareAndSet(false, true)) return
            appStarted.set(false)
            playbackStarted.set(false)
            playbackIntegration.release()
            bridgeScope.cancel()
        }
    }
}
