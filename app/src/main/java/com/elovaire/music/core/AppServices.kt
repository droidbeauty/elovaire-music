package elovaire.music.droidbeauty.app.core

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.LibraryScanCoordinator
import elovaire.music.droidbeauty.app.data.library.MediaStoreScanner
import elovaire.music.droidbeauty.app.data.library.SafTreeLibraryScanner
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibraryScanner
import elovaire.music.droidbeauty.app.data.library.network.NetworkCredentialStore
import elovaire.music.droidbeauty.app.data.library.network.NetworkFileSystemRegistry
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySourceStore
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibraryProtocol
import elovaire.music.droidbeauty.app.data.library.network.NetworkCredentials
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySource
import elovaire.music.droidbeauty.app.data.library.network.NetworkProbeResult
import elovaire.music.droidbeauty.app.data.library.network.NetworkInventoryStore
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceCoordinator
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceMutationRuntime
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceMutationJournal
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceMutationMarker
import elovaire.music.droidbeauty.app.data.library.network.recover
import elovaire.music.droidbeauty.app.data.library.network.SmbNetworkFileSystem
import elovaire.music.droidbeauty.app.data.library.network.WebDavNetworkFileSystem
import elovaire.music.droidbeauty.app.core.hasLocalNetworkPermission
import elovaire.music.droidbeauty.app.data.playback.NetworkDataSourceFactory
import androidx.media3.datasource.DefaultDataSource
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.library.db.LibraryIndexStore
import elovaire.music.droidbeauty.app.data.artist.ArtistImageRepository
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationRecoveryResult
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffectsController
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackSessionStore
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaLibrarySessionCallback
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaTree
import elovaire.music.droidbeauty.app.data.playback.library.MediaLibraryReadExecutor
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.data.settings.PreferenceStorage
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.data.settings.PortableSettingsBackup
import elovaire.music.droidbeauty.app.data.settings.RoomUserDataStore
import elovaire.music.droidbeauty.app.data.settings.UserDataRecoverySnapshot
import elovaire.music.droidbeauty.app.data.settings.UpdatePreferencesStoreImpl
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.update.UpdateController
import elovaire.music.droidbeauty.app.data.update.createUpdateController
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicBoolean
import java.security.GeneralSecurityException

@OptIn(UnstableApi::class)
internal class AppServices(
    val applicationContext: Context,
    private val appScope: CoroutineScope,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
    private val portableSettingsBackup: PortableSettingsBackup,
) {
    private val playbackStarted = AtomicBoolean(false)
    private val started = AtomicBoolean(false)
    private val released = AtomicBoolean(false)
    private val playbackScope = CoroutineScope(
        appScope.coroutineContext + SupervisorJob(appScope.coroutineContext[Job]),
    )
    private val libraryScope = CoroutineScope(
        appScope.coroutineContext + SupervisorJob(appScope.coroutineContext[Job]),
    )
    private val optionalScope = CoroutineScope(
        appScope.coroutineContext + SupervisorJob(appScope.coroutineContext[Job]),
    )
    private val mediaLibraryReadExecutor = MediaLibraryReadExecutor.bounded()
    private val durableStartupStarted = AtomicBoolean(false)
    private val durableStartupReady = SettableFuture.create<Unit>()
    val exitDiagnostics = AppExitDiagnostics(applicationContext)
    private val database = ElovaireDatabase.create(applicationContext)
    private val mediaMutationJournal = MediaMutationJournal(database.libraryDao())
    private val userDataStore = RoomUserDataStore(
        context = applicationContext,
        dao = database.userDataDao(),
        recoverySnapshot = UserDataRecoverySnapshot(applicationContext),
    )
    val preferenceStore = PreferenceStore(applicationContext, userDataStore)
    private val networkSourceStore = NetworkLibrarySourceStore(applicationContext)
    private val networkSourceMutationJournal = NetworkSourceMutationJournal(applicationContext)
    private val _networkProbeResults = MutableStateFlow<Map<String, NetworkProbeResult>>(emptyMap())
    private val networkInventoryStore = NetworkInventoryStore(applicationContext, database.libraryDao())
    private val networkCredentialStoreDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        NetworkCredentialStore(applicationContext)
    }
    private val networkFileSystemRegistryDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        NetworkFileSystemRegistry(
            sourceStore = networkSourceStore,
            credentialStore = networkCredentialStoreDelegate.value,
            fileSystems = mapOf(
                NetworkLibraryProtocol.Smb to SmbNetworkFileSystem(optionalScope),
                NetworkLibraryProtocol.WebDav to WebDavNetworkFileSystem(),
            ),
            localNetworkAccessAllowed = { applicationContext.hasLocalNetworkPermission() },
        )
    }
    private val networkScannerDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        NetworkLibraryScanner(
            context = applicationContext,
            registry = networkFileSystemRegistryDelegate.value,
            inventory = networkInventoryStore,
            onAvailabilityChanged = { sourceId, result ->
                _networkProbeResults.update { it + (sourceId to result) }
            },
        )
    }
    private val networkDataSourceFactory = NetworkDataSourceFactory(
        defaultFactory = DefaultDataSource.Factory(applicationContext),
        registryProvider = { networkFileSystemRegistryDelegate.value },
    )
    private val networkSourceCoordinator = NetworkSourceCoordinator(
        sourceStore = networkSourceStore,
        credentialStoreProvider = { networkCredentialStoreDelegate.value },
        registryProvider = { networkFileSystemRegistryDelegate.value },
        inventoryStore = networkInventoryStore,
        mutationJournal = networkSourceMutationJournal,
    )
    private val updateControllerDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val updatePreferences = allowStrictModeDiskReads {
            UpdatePreferencesStoreImpl(PreferenceStorage(applicationContext).preferences)
        }
        createUpdateController(
            context = applicationContext,
            scope = optionalScope,
            preferences = updatePreferences,
            backgroundWorkPolicy = backgroundWorkPolicy,
        )
    }
    val updateController: UpdateController get() = updateControllerDelegate.value
    private val artistImageRepositoryDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ArtistImageRepository(
            appContext = applicationContext,
            scope = optionalScope,
        )
    }
    val artistImageRepository get() = artistImageRepositoryDelegate.value
    val albumTagEditorService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AlbumTagEditorService(
            applicationContext,
            mediaMutationJournal = mediaMutationJournal,
        )
    }
    val playbackEffectsController = PlaybackEffectsController()
    val playbackManager = PlaybackManager(
        context = applicationContext,
        scope = playbackScope,
        audioProcessorsProvider = playbackEffectsController::audioProcessors,
        hasSignalAlteringEffects = playbackEffectsController::hasSignalAlteringEffects,
        initialRecentSongIds = preferenceStore.recentSongIds.value,
        initialRecentAlbumIds = preferenceStore.recentAlbumIds.value,
        initialLastPlayedCollectionKind = preferenceStore.lastPlayedCollectionKind.value,
        initialLastPlayedCollectionId = preferenceStore.lastPlayedCollectionId.value,
        onRecentPlaybackChanged = preferenceStore::setRecentPlaybackIds,
        playbackDataSourceFactory = networkDataSourceFactory,
    )
    val playbackSessionStore = PlaybackSessionStore(applicationContext)
    val libraryRepository = LibraryRepository(
        appContext = applicationContext,
        scanner = LibraryScanCoordinator(
            localScanner = MediaStoreScanner(
                context = applicationContext,
            ),
            safScanner = SafTreeLibraryScanner(applicationContext),
            networkScannerProvider = { networkScannerDelegate.value },
        ).also { scanner ->
            scanner.setNetworkSources(networkSourceStore.sources.value)
        },
        scope = libraryScope,
        backgroundWorkPolicy = backgroundWorkPolicy,
        libraryIndexStore = LibraryIndexStore(database.libraryDao()),
        onSongRelocations = { replacements ->
            when (val result = userDataStore.relocateSongReferences(replacements).await()) {
                is PlaylistMutationResult.Success -> true
                else -> false
            }
        },
    ).also {
        it.setLibraryFolders(preferenceStore.libraryFolders.value)
    }
    private val networkMutationRuntime = NetworkSourceMutationRuntime(
        scope = optionalScope,
        coordinator = networkSourceCoordinator,
        onProbeResult = { sourceId, result ->
            _networkProbeResults.update { it + (sourceId to result) }
        },
        onSourceRemoved = { sourceId ->
            _networkProbeResults.update { it - sourceId }
        },
        onSourcesChanged = { sourceId, refreshRequired ->
            libraryRepository.unblockNetworkSource(sourceId)
            libraryRepository.setNetworkSources(
                networkSourceStore.sources.value,
                enrichMetadata = false,
                showLoadingIndicator = true,
                forceRefreshSourceIds = if (refreshRequired) setOf(sourceId) else emptySet(),
            )
        },
    )
    private val lyricsServiceDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LyricsService(
            context = applicationContext,
            mediaMutationJournal = mediaMutationJournal,
            onlineLyricsEnabled = { preferenceStore.onlineLyricsEnabled.value },
        )
    }
    val lyricsService get() = lyricsServiceDelegate.value
    val networkSources get() = networkSourceStore.sources
    val networkProbeResults: StateFlow<Map<String, NetworkProbeResult>> =
        _networkProbeResults.asStateFlow()

    fun saveNetworkSource(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ) {
        networkMutationRuntime.save(source, credentials)
    }

    fun removeNetworkSource(source: NetworkLibrarySource) {
        networkMutationRuntime.remove(source)
    }

    private val mediaTree = ElovaireMediaTree(libraryRepository, preferenceStore)

    init {
        playbackManager.setMediaLibrarySessionCallback(
            ElovaireMediaLibrarySessionCallback(
                browser = mediaTree,
                commandResolver = mediaTree,
                playbackManager = playbackManager,
                readExecutor = mediaLibraryReadExecutor,
                startupReady = durableStartupReady,
            ),
        )
    }

    fun start() {
        if (released.get() || !started.compareAndSet(false, true)) return
        startPlayback()
        updateController.start()
    }

    @Suppress("TooGenericExceptionCaught")
    fun startPlayback() {
        if (released.get() || !playbackStarted.compareAndSet(false, true)) return
        if (!durableStartupStarted.compareAndSet(false, true)) return
        optionalScope.launch(Dispatchers.IO) {
            val mediaMutationRecoverySucceeded = recoverCriticalMediaMutations()
            val pendingSourceIds = networkSourceMutationJournal.pending()
                .mapTo(linkedSetOf(), NetworkSourceMutationMarker::sourceId)
            val blockedSourceIds = try {
                withTimeout(DURABLE_RECOVERY_TIMEOUT_MS) {
                    networkSourceMutationJournal.recover(
                        sourceStore = networkSourceStore,
                        credentialStore = networkCredentialStoreDelegate.value,
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
            } catch (failure: GeneralSecurityException) {
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
            val exitSnapshot = exitDiagnostics.inspect()
            backgroundWorkPolicy.setOptionalStartupSuppressed(
                exitSnapshot.suppressOptionalStartup || !mediaMutationRecoverySucceeded,
            )
            portableSettingsBackup.start()
            updateController.scheduleStartupMaintenance()
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

    fun onMemoryPressure(pressure: MemoryPressure) {
        if (lyricsServiceDelegate.isInitialized()) lyricsService.onMemoryPressure(pressure)
        if (artistImageRepositoryDelegate.isInitialized()) artistImageRepository.onMemoryPressure(pressure)
        libraryRepository.onMemoryPressure(pressure)
        mediaTree.onMemoryPressure(pressure)
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        started.set(false)
        playbackStarted.set(false)
        networkMutationRuntime.release()
        optionalScope.cancel()
        playbackManager.release()
        playbackScope.cancel()
        if (updateControllerDelegate.isInitialized()) updateController.release()
        if (artistImageRepositoryDelegate.isInitialized()) artistImageRepository.release()
        libraryRepository.release()
        libraryScope.cancel()
        if (networkFileSystemRegistryDelegate.isInitialized()) {
            networkFileSystemRegistryDelegate.value.release()
        }
        preferenceStore.release(database::close)
        portableSettingsBackup.release()
        mediaLibraryReadExecutor.close()
        durableStartupReady.cancel(false)
    }
}

private const val DURABLE_RECOVERY_TIMEOUT_MS = 15_000L
private const val TAG = "ElovaireAppServices"
