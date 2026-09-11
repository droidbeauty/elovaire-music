package elovaire.music.droidbeauty.app.core

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import elovaire.music.droidbeauty.app.data.library.LibraryStartupController
import elovaire.music.droidbeauty.app.data.library.network.NetworkCredentialStore
import elovaire.music.droidbeauty.app.data.library.network.NetworkInventoryStore
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySourceStore
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceMutationJournal
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceMutationMarker
import elovaire.music.droidbeauty.app.data.library.network.recover
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationRecoveryResult
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.data.settings.PortableSettingsBackup
import elovaire.music.droidbeauty.app.data.settings.PortableUserDataBackup
import elovaire.music.droidbeauty.app.data.settings.decodePortableUserData
import elovaire.music.droidbeauty.app.data.settings.RoomUserDataStore
import elovaire.music.droidbeauty.app.data.settings.UserDataReadiness
import elovaire.music.droidbeauty.app.data.settings.UserDataSnapshot
import elovaire.music.droidbeauty.app.data.update.UpdateController
import elovaire.music.droidbeauty.app.core.backend.BackendFailure
import elovaire.music.droidbeauty.app.core.backend.classifyBackendFailure
import com.google.common.util.concurrent.SettableFuture
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.FlowPreview

internal enum class DurableStartupPhase {
    NotStarted,
    Recovering,
    Ready,
    Degraded,
    Failed,
    Released,
}

internal enum class DurableStartupComponent {
    MediaMutationRecovery,
    NetworkSourceMutationRecovery,
}

internal data class DurableStartupState(
    val phase: DurableStartupPhase,
    val retryableComponents: Set<DurableStartupComponent> = emptySet(),
    val failure: BackendFailure? = null,
)

internal fun startupStateAfterRecovery(
    mediaMutationRecoverySucceeded: Boolean,
    networkRecoverySucceeded: Boolean,
    blockedSourceIds: Set<String>,
): DurableStartupState {
    val retryableComponents = buildSet {
        if (!mediaMutationRecoverySucceeded) add(DurableStartupComponent.MediaMutationRecovery)
        if (!networkRecoverySucceeded || blockedSourceIds.isNotEmpty()) {
            add(DurableStartupComponent.NetworkSourceMutationRecovery)
        }
    }
    return if (retryableComponents.isEmpty()) {
        DurableStartupState(DurableStartupPhase.Ready)
    } else {
        DurableStartupState(
            phase = DurableStartupPhase.Degraded,
            retryableComponents = retryableComponents,
        )
    }
}

/** Owns durable recovery and optional startup work after the object graph is built. */
@OptIn(FlowPreview::class)
internal class AppStartupCoordinator(
    private val applicationContext: Context,
    private val appScope: CoroutineScope,
    private val optionalScope: CoroutineScope,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
    private val portableSettingsBackup: PortableSettingsBackup,
    private val portableUserDataBackup: PortableUserDataBackup,
    private val userDataStore: RoomUserDataStore,
    private val libraryRepository: LibraryStartupController,
    private val networkSourceMutationJournal: NetworkSourceMutationJournal,
    private val networkSourceStore: NetworkLibrarySourceStore,
    private val networkCredentialStoreProvider: () -> NetworkCredentialStore,
    private val invalidateNetworkSourceRuntime: ((sourceId: String) -> Unit)? = null,
    private val networkInventoryStore: NetworkInventoryStore,
    private val mediaMutationJournal: MediaMutationJournal,
    private val updateControllerProvider: () -> UpdateController,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    internal val durableStartupReady = SettableFuture.create<Unit>()
    private val _durableStartupState = MutableStateFlow(
        DurableStartupState(DurableStartupPhase.NotStarted),
    )
    internal val durableStartupState: StateFlow<DurableStartupState> = _durableStartupState.asStateFlow()

    private val exitDiagnosticsDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AppExitDiagnostics(applicationContext)
    }
    private val durableStartupStarted = AtomicBoolean(false)
    private val started = AtomicBoolean(false)
    private val playbackStarted = AtomicBoolean(false)
    private val released = AtomicBoolean(false)
    private val criticalRecoveryScope = CoroutineScope(
        appScope.coroutineContext + SupervisorJob(appScope.coroutineContext[Job]) + ioDispatcher,
    )
    private var portableUserDataBackupJob: Job? = null

    @Suppress("TooGenericExceptionCaught")
    fun start() {
        if (released.get() || !started.compareAndSet(false, true)) return
        startPlayback()
        try {
            updateControllerProvider().start()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: RuntimeException) {
            backgroundWorkPolicy.setOptionalStartupSuppressed(true)
            Log.w(TAG, "Optional update service could not start", failure)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun startPlayback() {
        if (released.get() || !playbackStarted.compareAndSet(false, true)) return
        if (!durableStartupStarted.compareAndSet(false, true)) return
        _durableStartupState.value = DurableStartupState(DurableStartupPhase.Recovering)
        startPortableUserDataBackup()
        criticalRecoveryScope.launch {
            try {
                val mediaMutationRecoverySucceeded = recoverCriticalMediaMutations()
                val pendingSourceIds = networkSourceMutationJournal.pending()
                    .mapTo(linkedSetOf(), NetworkSourceMutationMarker::sourceId)
                var networkRecoverySucceeded = true
                val blockedSourceIds = try {
                    withTimeout(DURABLE_RECOVERY_TIMEOUT_MS) {
                        networkSourceMutationJournal.recover(
                            sourceStore = networkSourceStore,
                            credentialStore = networkCredentialStoreProvider(),
                            inventoryStore = networkInventoryStore,
                            invalidateRuntime = invalidateNetworkSourceRuntime,
                        )
                    }
                    emptySet()
                } catch (_: TimeoutCancellationException) {
                    networkRecoverySucceeded = false
                    pendingSourceIds
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: SQLiteException) {
                    networkRecoverySucceeded = false
                    Log.w(TAG, "Network source mutation recovery deferred", failure)
                    pendingSourceIds
                } catch (failure: IllegalStateException) {
                    networkRecoverySucceeded = false
                    Log.w(TAG, "Network source mutation recovery deferred", failure)
                    pendingSourceIds
                } catch (failure: SecurityException) {
                    networkRecoverySucceeded = false
                    Log.w(TAG, "Network source mutation recovery deferred", failure)
                    pendingSourceIds
                } catch (failure: java.security.GeneralSecurityException) {
                    networkRecoverySucceeded = false
                    Log.w(TAG, "Network source credential recovery deferred", failure)
                    pendingSourceIds
                } catch (failure: RuntimeException) {
                    networkRecoverySucceeded = false
                    Log.e(TAG, "Network source mutation recovery failed", failure)
                    pendingSourceIds
                }
                if (blockedSourceIds.isNotEmpty()) {
                    libraryRepository.blockNetworkSources(blockedSourceIds)
                }
                libraryRepository.start()
                libraryRepository.onPermissionChanged(applicationContext.hasAudioReadPermission())
                _durableStartupState.value = startupStateAfterRecovery(
                    mediaMutationRecoverySucceeded = mediaMutationRecoverySucceeded,
                    networkRecoverySucceeded = networkRecoverySucceeded,
                    blockedSourceIds = blockedSourceIds,
                )
                durableStartupReady.set(Unit)
                optionalScope.launch(ioDispatcher) {
                    runOptionalStartup(mediaMutationRecoverySucceeded)
                }
            } catch (cancelled: CancellationException) {
                durableStartupReady.cancel(false)
                throw cancelled
            } catch (failure: Exception) {
                Log.e(TAG, "Durable playback startup failed", failure)
                _durableStartupState.value = DurableStartupState(
                    phase = DurableStartupPhase.Failed,
                    failure = classifyBackendFailure(failure),
                )
                durableStartupReady.setException(failure)
            }
        }
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        _durableStartupState.value = DurableStartupState(DurableStartupPhase.Released)
        portableUserDataBackupJob?.cancel()
        portableUserDataBackupJob = null
        criticalRecoveryScope.cancel()
        durableStartupReady.cancel(false)
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun runOptionalStartup(mediaMutationRecoverySucceeded: Boolean) {
        try {
            val exitSnapshot = exitDiagnosticsDelegate.value.inspect()
            backgroundWorkPolicy.setOptionalStartupSuppressed(
                exitSnapshot.suppressOptionalStartup || !mediaMutationRecoverySucceeded,
            )
            portableSettingsBackup.start()
            updateControllerProvider().scheduleStartupMaintenance()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: RuntimeException) {
            Log.w(TAG, "Optional startup work deferred after playback startup", failure)
            backgroundWorkPolicy.setOptionalStartupSuppressed(true)
        }
    }

    private fun startPortableUserDataBackup() {
        if (portableUserDataBackupJob?.isActive == true) return
        portableUserDataBackupJob = appScope.launch {
            var restoreChecked = false
            val contentSongs = libraryRepository.contentState
                .map { content -> content.contentRevision to content.songs }
                .distinctUntilChanged()
            val scanReadiness = libraryRepository.scanState
                .map { scan -> scan.permissionGranted to scan.isAuthoritative }
                .distinctUntilChanged()
            kotlinx.coroutines.flow.combine(
                userDataStore.userDataSnapshot,
                userDataStore.userDataReadiness,
                contentSongs,
                scanReadiness,
            ) { snapshot, readiness, content, scan ->
                PortableUserDataBackupState(
                    snapshot = snapshot,
                    readiness = readiness,
                    songs = content.second,
                    permissionGranted = scan.first,
                    isAuthoritative = scan.second,
                )
            }.map { state -> state.copy(userDataRevision = userDataStore.currentUserDataRevision) }
                .distinctUntilChanged()
                .debounce(PORTABLE_USER_DATA_BACKUP_COALESCE_DELAY_MS)
                .collect { state ->
                if (
                    state.readiness != UserDataReadiness.Ready ||
                    !state.isAuthoritative ||
                    !state.permissionGranted
                ) return@collect
                if (!restoreChecked) {
                    val encoded = withContext(ioDispatcher) { portableUserDataBackup.readBytes() }
                    val portable = encoded?.let { withContext(ioDispatcher) { decodePortableUserData(it) } }
                    val backupContainsSongReferences = portable?.hasSongReferences() == true
                    if (encoded != null && state.songs.isEmpty() && backupContainsSongReferences) {
                        return@collect
                    }
                    if (
                        encoded != null &&
                        shouldRestorePortableUserData(
                            localRevision = state.userDataRevision,
                            currentHasPortableData = state.snapshot.hasPortableUserData(),
                            backupRevision = portable?.userDataRevision,
                        )
                    ) {
                        when (userDataStore.restorePortableUserData(encoded, state.songs).await()) {
                            is PlaylistMutationResult.Success -> {
                                restoreChecked = true
                                return@collect
                            }
                            else -> return@collect
                        }
                    }
                    restoreChecked = true
                }
                if (state.songs.isEmpty() && state.snapshot.hasPortableSongReferences()) return@collect
                try {
                    withContext(ioDispatcher) {
                        portableUserDataBackup.write(
                            snapshot = state.snapshot,
                            songs = state.songs,
                            userDataRevision = state.userDataRevision,
                        )
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: java.io.IOException) {
                    Log.w(TAG, "Portable user-data backup deferred", failure)
                } catch (failure: SecurityException) {
                    Log.w(TAG, "Portable user-data backup deferred", failure)
                } catch (failure: IllegalArgumentException) {
                    Log.w(TAG, "Portable user-data backup deferred", failure)
                } catch (failure: IllegalStateException) {
                    Log.w(TAG, "Portable user-data backup deferred", failure)
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun recoverCriticalMediaMutations(): Boolean {
        return try {
            withTimeout(DURABLE_RECOVERY_TIMEOUT_MS) {
                mediaMutationJournal.recoverIncomplete() is MediaMutationRecoveryResult.Success
            }
        } catch (failure: TimeoutCancellationException) {
            Log.w(TAG, "Media mutation recovery timed out", failure)
            false
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: SQLiteException) {
            Log.w(TAG, "Media mutation recovery deferred", failure)
            false
        } catch (failure: IllegalStateException) {
            Log.w(TAG, "Media mutation recovery deferred", failure)
            false
        } catch (failure: RuntimeException) {
            Log.e(TAG, "Media mutation recovery failed", failure)
            false
        }
    }
}

private const val DURABLE_RECOVERY_TIMEOUT_MS = 15_000L
private const val PORTABLE_USER_DATA_BACKUP_COALESCE_DELAY_MS = 500L
private const val TAG = "ElovaireStartup"

private data class PortableUserDataBackupState(
    val snapshot: UserDataSnapshot,
    val readiness: UserDataReadiness,
    val songs: List<elovaire.music.droidbeauty.app.domain.model.Song>,
    val permissionGranted: Boolean,
    val isAuthoritative: Boolean,
    val userDataRevision: Long = 0L,
)

internal fun shouldRestorePortableUserData(
    localRevision: Long,
    currentHasPortableData: Boolean,
    backupRevision: Long?,
): Boolean = localRevision == 0L && !currentHasPortableData && backupRevision != null && backupRevision >= 0L

private fun UserDataSnapshot.hasPortableUserData(): Boolean {
    return playlists.isNotEmpty() || smartPlaylists.isNotEmpty() || hasPortableSongReferences()
}

private fun UserDataSnapshot.hasPortableSongReferences(): Boolean {
    return playlists.any { it.songIds.isNotEmpty() } ||
        favoriteSongIds.isNotEmpty() ||
        songPlayCounts.isNotEmpty() ||
        recentSongIds.isNotEmpty()
}

private fun elovaire.music.droidbeauty.app.data.settings.PortableUserData.hasSongReferences(): Boolean =
    playlists.any { it.songs.isNotEmpty() } ||
        favoriteSongs.isNotEmpty() ||
        songPlayCounts.isNotEmpty() ||
        recentSongs.isNotEmpty()
