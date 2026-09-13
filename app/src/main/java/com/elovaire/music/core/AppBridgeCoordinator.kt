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
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("UnsafeOptInUsageError")
@Suppress("TooGenericExceptionCaught")
internal class AppBridgeCoordinator(
    scope: CoroutineScope,
    private val services: AppServices,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val bridgeScope = ownedChildScope(scope, "app-bridge")
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
    private var libraryFoldersJob: Job? = null

    fun startPlayback() {
        synchronized(lifecycleLock) {
            if (released.get() || !playbackStarted.compareAndSet(false, true)) return
            playbackIntegration.start()
        }
    }

    fun start() {
        synchronized(lifecycleLock) {
            if (released.get()) return
            startPlayback()
            appStarted.set(true)
            if (libraryFoldersJob == null) {
                libraryFoldersJob = bridgeScope.launch {
                    preferences.libraryFolders.collect(library::setLibraryFolders)
                }
            }
        }
    }

    fun scheduleDeferredStartupWork() {
        synchronized(lifecycleLock) {
            if (!appStarted.get() || released.get() || !deferredStartupScheduled.compareAndSet(false, true)) return
            bridgeScope.launch(ioDispatcher) {
                try {
                    PersistenceMaintenanceWorker.enqueue(applicationContext)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: RuntimeException) {
                    synchronized(lifecycleLock) {
                        if (!released.get()) deferredStartupScheduled.set(false)
                    }
                    android.util.Log.w(
                        "AppBridgeCoordinator",
                        "Deferred persistence maintenance was not scheduled; retrying later.",
                        failure,
                    )
                }
            }
        }
    }

    fun release() {
        synchronized(lifecycleLock) {
            if (!released.compareAndSet(false, true)) return
            appStarted.set(false)
            playbackStarted.set(false)
            releaseBestEffort(
                {
                    libraryFoldersJob?.cancel()
                    libraryFoldersJob = null
                },
                { playbackIntegration.release() },
                { bridgeScope.cancel() },
            )
        }
    }
}
