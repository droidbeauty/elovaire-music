package elovaire.music.droidbeauty.app.core

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.LibraryScanState
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
import kotlinx.coroutines.flow.map

/** Owns durable recovery and optional startup work after the object graph is built. */
internal class AppStartupCoordinator(
    private val applicationContext: Context,
    private val appScope: CoroutineScope,
    private val optionalScope: CoroutineScope,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
    private val portableSettingsBackup: PortableSettingsBackup,
    private val portableUserDataBackup: PortableUserDataBackup,
    private val userDataStore: RoomUserDataStore,
    private val libraryRepository: LibraryRepository,
    private val networkSourceMutationJournal: NetworkSourceMutationJournal,
    private val networkSourceStore: NetworkLibrarySourceStore,
    private val networkCredentialStoreProvider: () -> NetworkCredentialStore,
    private val networkInventoryStore: NetworkInventoryStore,
    private val mediaMutationJournal: MediaMutationJournal,
    private val updateControllerProvider: () -> UpdateController,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    internal val durableStartupReady = SettableFuture.create<Unit>()

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
        startPortableUserDataBackup()
        criticalRecoveryScope.launch {
            try {
                val mediaMutationRecoverySucceeded = recoverCriticalMediaMutations()
                val pendingSourceIds = networkSourceMutationJournal.pending()
                    .mapTo(linkedSetOf(), NetworkSourceMutationMarker::sourceId)
                val blockedSourceIds = try {
                    withTimeout(DURABLE_RECOVERY_TIMEOUT_MS) {
                        networkSourceMutationJournal.recover(
                            sourceStore = networkSourceStore,
                            credentialStore = networkCredentialStoreProvider(),
                            inventoryStore = networkInventoryStore,
                        )
                    }
                    emptySet()
                } catch (_: TimeoutCancellationException) {
                    pendingSourceIds
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: SQLiteException) {
                    Log.w(TAG, "Network source mutation recovery deferred", failure)
                    pendingSourceIds
                } catch (failure: IllegalStateException) {
                    Log.w(TAG, "Network source mutation recovery deferred", failure)
                    pendingSourceIds
                } catch (failure: SecurityException) {
                    Log.w(TAG, "Network source mutation recovery deferred", failure)
                    pendingSourceIds
                } catch (failure: java.security.GeneralSecurityException) {
                    Log.w(TAG, "Network source credential recovery deferred", failure)
                    pendingSourceIds
                } catch (failure: RuntimeException) {
                    Log.e(TAG, "Network source mutation recovery failed", failure)
                    pendingSourceIds
                }
                if (blockedSourceIds.isNotEmpty()) {
                    libraryRepository.blockNetworkSources(blockedSourceIds)
                }
                libraryRepository.start()
                libraryRepository.onPermissionChanged(applicationContext.hasAudioReadPermission())
                durableStartupReady.set(Unit)
                optionalScope.launch(ioDispatcher) {
                    runOptionalStartup(mediaMutationRecoverySucceeded)
                }
            } catch (cancelled: CancellationException) {
                durableStartupReady.cancel(false)
                throw cancelled
            } catch (failure: Exception) {
                Log.e(TAG, "Durable playback startup failed", failure)
                durableStartupReady.setException(failure)
            }
        }
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
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
            kotlinx.coroutines.flow.combine(
                userDataStore.userDataSnapshot,
                userDataStore.userDataReadiness,
                libraryRepository.contentState,
                libraryRepository.scanState,
            ) { snapshot, readiness, content, scan ->
                PortableUserDataBackupState(snapshot, readiness, content.songs, scan)
            }.map { state -> state.copy(userDataRevision = userDataStore.currentUserDataRevision) }
                .distinctUntilChanged()
                .collect { state ->
                if (
                    state.readiness != UserDataReadiness.Ready ||
                    !state.scan.isAuthoritative ||
                    !state.scan.permissionGranted
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
private const val TAG = "ElovaireStartup"

private data class PortableUserDataBackupState(
    val snapshot: UserDataSnapshot,
    val readiness: UserDataReadiness,
    val songs: List<elovaire.music.droidbeauty.app.domain.model.Song>,
    val scan: LibraryScanState,
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
