package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import android.content.Context
import elovaire.music.droidbeauty.app.data.library.db.PersistenceMaintenanceWorker
import elovaire.music.droidbeauty.app.data.library.LibraryReader
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffects
import elovaire.music.droidbeauty.app.data.playback.PlaybackIntegrationPort
import elovaire.music.droidbeauty.app.data.playback.PersistedPlaybackSession
import elovaire.music.droidbeauty.app.data.playback.PlaybackSessionPersistence
import elovaire.music.droidbeauty.app.data.settings.PlaybackHistoryStore
import elovaire.music.droidbeauty.app.data.settings.PlaybackIntegrationSettings
import elovaire.music.droidbeauty.app.data.settings.LibrarySettingsWriter
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

internal class AppBridgeDependencies(
    val applicationContext: Context,
    val playbackSettings: PlaybackIntegrationSettings,
    val librarySettings: LibrarySettingsWriter,
    val history: PlaybackHistoryStore,
    val library: LibraryReader,
    val setLibraryFolders: (List<LibraryFolderSelection>) -> Unit,
    val playback: PlaybackIntegrationPort,
    val effects: PlaybackEffects,
    val sessionStore: PlaybackSessionPersistence,
    val hydrateRecentPlayback: (
        List<Long>,
        List<Long>,
        elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind?,
        Long?,
    ) -> Unit,
    val currentPositionForPersistence: () -> Long,
    val checkpointAudiobookProgress: () -> Unit,
    val restoreSession: (List<elovaire.music.droidbeauty.app.domain.model.Song>, Int, PersistedPlaybackSession) -> Unit,
)

@SuppressLint("UnsafeOptInUsageError")
@Suppress("TooGenericExceptionCaught")
internal class AppBridgeCoordinator(
    scope: CoroutineScope,
    private val dependencies: AppBridgeDependencies,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val diagnostics: BackendDiagnosticRecorder = BackendDiagnostics,
) {
    private val bridgeScope = ownedChildScope(scope, "app-bridge", diagnostics)
    private val playbackIntegration = PlaybackIntegrationCoordinator(
        scope = bridgeScope,
        preferences = dependencies.playbackSettings,
        history = dependencies.history,
        library = dependencies.library,
        playback = dependencies.playback,
        effects = dependencies.effects,
        sessionStore = dependencies.sessionStore,
        hydrateRecentPlayback = dependencies.hydrateRecentPlayback,
        currentPositionForPersistence = dependencies.currentPositionForPersistence,
        checkpointAudiobookProgress = dependencies.checkpointAudiobookProgress,
        restoreSession = dependencies.restoreSession,
        diagnostics = diagnostics,
        ioDispatcher = ioDispatcher,
    )
    private val applicationContext = dependencies.applicationContext
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
                dependencies.librarySettings.libraryFolders.collect(dependencies.setLibraryFolders)
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
