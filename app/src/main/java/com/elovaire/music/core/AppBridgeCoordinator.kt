package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import android.content.Context
import elovaire.music.droidbeauty.app.data.library.db.PersistenceMaintenanceWorker
import elovaire.music.droidbeauty.app.data.library.LibraryReader
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffects
import elovaire.music.droidbeauty.app.data.playback.PlaybackIntegrationPort
import elovaire.music.droidbeauty.app.data.playback.PlaybackSessionPersistence
import elovaire.music.droidbeauty.app.data.settings.PlaybackHistoryStore
import elovaire.music.droidbeauty.app.data.settings.PlaybackIntegrationSettings
import elovaire.music.droidbeauty.app.data.settings.LibrarySettingsWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.isActive
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticRecorder
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics

@SuppressLint("UnsafeOptInUsageError")
@Suppress("TooGenericExceptionCaught")
internal class AppBridgeCoordinator(
    scope: CoroutineScope,
    private val applicationContext: Context,
    private val playbackSettings: PlaybackIntegrationSettings,
    private val librarySettings: LibrarySettingsWriter,
    private val history: PlaybackHistoryStore,
    private val library: LibraryReader,
    private val setLibraryFolders: (List<LibraryFolderSelection>) -> Unit,
    private val playback: PlaybackIntegrationPort,
    private val effects: PlaybackEffects,
    private val sessionStore: PlaybackSessionPersistence,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val diagnostics: BackendDiagnosticRecorder = BackendDiagnostics,
) {
    private val bridgeScope = ownedChildScope(scope, "app-bridge", diagnostics)
    private val playbackIntegration = PlaybackIntegrationCoordinator(
        scope = bridgeScope,
        preferences = playbackSettings,
        history = history,
        library = library,
        playback = playback,
        effects = effects,
        sessionStore = sessionStore,
        diagnostics = diagnostics,
        ioDispatcher = ioDispatcher,
    )
    private val lifecycleLock = Any()
    private var deferredStartupJob: Job? = null
    private var libraryFoldersJob: Job? = null
    private var releaseDrain: Deferred<PlaybackSessionDrainResult>? = null

    fun startPlayback() {
        if (!bridgeScope.isActive) return
        playbackIntegration.start()
    }

    fun start() {
        if (!bridgeScope.isActive) return
        if (libraryFoldersJob == null) {
            libraryFoldersJob = bridgeScope.launch {
                librarySettings.libraryFolders.collect(setLibraryFolders)
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

    fun release(): Deferred<PlaybackSessionDrainResult> {
        synchronized(lifecycleLock) {
            releaseDrain?.let { return it }
            runCatching {
                releaseBestEffort(
                    {
                        libraryFoldersJob?.cancel()
                        libraryFoldersJob = null
                    },
                    {
                        deferredStartupJob?.cancel()
                        deferredStartupJob = null
                    },
                )
            }.onFailure { failure -> diagnostics.recordWorkerFailure("app-bridge-release", failure) }
            val drain = runCatching { playbackIntegration.release() }.getOrElse { failure ->
                diagnostics.recordWorkerFailure("app-bridge-release", failure)
                CompletableDeferred<PlaybackSessionDrainResult>().apply {
                    complete(PlaybackSessionDrainResult.Failed(failure))
                }
            }
            releaseDrain = drain
            bridgeScope.launch {
                runCatching { drain.await() }
                bridgeScope.cancel()
            }
            return drain
        }
    }
}
