package elovaire.music.droidbeauty.app.core

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.SongRelocationOutcome
import elovaire.music.droidbeauty.app.data.library.LibrarySnapshotStore
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
import elovaire.music.droidbeauty.app.data.library.network.NetworkAvailability
import elovaire.music.droidbeauty.app.data.library.network.NetworkProbeResult
import elovaire.music.droidbeauty.app.data.library.network.NetworkInventoryStore
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceCoordinator
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceMutationRuntime
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceMutationResult
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceMutationJournal
import elovaire.music.droidbeauty.app.data.library.network.SmbNetworkFileSystem
import elovaire.music.droidbeauty.app.data.library.network.WebDavNetworkFileSystem
import elovaire.music.droidbeauty.app.core.hasLocalNetworkPermission
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.data.playback.NetworkDataSourceFactory
import androidx.media3.datasource.DefaultDataSource
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.library.db.LibraryIndexStore
import elovaire.music.droidbeauty.app.data.artist.ArtistImageRepository
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffectsController
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackSessionStore
import elovaire.music.droidbeauty.app.data.playback.AudiobookChapterReader
import elovaire.music.droidbeauty.app.data.playback.Media3AudiobookChapterReader
import elovaire.music.droidbeauty.app.data.library.AudiobookDescriptionReader
import elovaire.music.droidbeauty.app.data.library.GoogleBooksAudiobookDescriptionReader
import elovaire.music.droidbeauty.app.data.playback.DefaultPlaybackResumptionGateway
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaLibrarySessionCallback
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaTree
import elovaire.music.droidbeauty.app.data.playback.library.MediaLibraryInvalidationCoordinator
import elovaire.music.droidbeauty.app.data.playback.library.MediaLibraryReadExecutor
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.data.settings.PortableSettingsBackup
import elovaire.music.droidbeauty.app.data.settings.PortableUserDataBackup
import elovaire.music.droidbeauty.app.data.settings.RoomUserDataStore
import elovaire.music.droidbeauty.app.data.settings.UserDataReadiness
import elovaire.music.droidbeauty.app.data.settings.UserDataRecoverySnapshot
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.tags.AudiobookTagEditorService
import elovaire.music.droidbeauty.app.data.update.UpdateController
import elovaire.music.droidbeauty.app.data.update.createUpdateController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(UnstableApi::class)
@Suppress("TooGenericExceptionCaught")
internal class AppServices(
    val applicationContext: Context,
    private val appScope: CoroutineScope,
    private val appDispatchers: AppDispatchers,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
    private val portableSettingsBackup: PortableSettingsBackup,
) {
    private val serviceScopes = AppServiceScopes(appScope)
    private val playbackScope = serviceScopes.playback
    private val libraryScope = serviceScopes.library
    private val optionalScope = serviceScopes.optional
    private val networkServicesStarted = AtomicBoolean(false)
    private val released = AtomicBoolean(false)
    private val mediaLibraryReadExecutor = MediaLibraryReadExecutor.bounded()
    private val database = ElovaireDatabase.create(applicationContext)
    private val databaseResource = BackendResourceRegistry.acquire(BackendResourceKind.DatabaseInstance)
    private val mediaMutationJournal = MediaMutationJournal(database.mediaMutationDao())
    private val userDataStore = RoomUserDataStore(
        context = applicationContext,
        dao = database.userDataDao(),
        database = database,
        recoverySnapshot = UserDataRecoverySnapshot(applicationContext),
        ioDispatcher = appDispatchers.io,
        ownerScope = appScope,
    )
    private val portableUserDataBackup = PortableUserDataBackup(applicationContext)
    val preferenceStore = PreferenceStore(
        context = applicationContext,
        userDataStore = userDataStore,
        ioDispatcher = appDispatchers.io,
        ownerScope = appScope,
        initialSettings = portableSettingsBackup.bootSettingsSnapshot(),
        portableSettingsBackup = portableSettingsBackup,
    )
    private val networkSourceStore = NetworkLibrarySourceStore(applicationContext)
    private val networkSourceMutationJournal = NetworkSourceMutationJournal(applicationContext)
    private val _networkProbeResults = MutableStateFlow<Map<String, NetworkProbeResult>>(emptyMap())
    private val networkInventoryStore = NetworkInventoryStore(applicationContext, database.networkInventoryDao())
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
            applicationContext = applicationContext,
        )
    }
    private val networkScannerDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        NetworkLibraryScanner(
            context = applicationContext,
            registry = networkFileSystemRegistryDelegate.value,
            inventory = networkInventoryStore,
            ioDispatcher = appDispatchers.io,
            onAvailabilityChanged = { sourceId, result ->
                _networkProbeResults.update { it + (sourceId to result) }
            },
        )
    }
    private val networkDataSourceFactory = NetworkDataSourceFactory(
        defaultFactory = DefaultDataSource.Factory(applicationContext),
        registryProvider = { networkFileSystemRegistryDelegate.value },
    )
    val audiobookChapterReader: AudiobookChapterReader = Media3AudiobookChapterReader(
        dataSourceFactory = networkDataSourceFactory,
    )
    val audiobookDescriptionReader: AudiobookDescriptionReader = GoogleBooksAudiobookDescriptionReader(
        transport = elovaire.music.droidbeauty.app.data.network.BoundedHttpTransport(
            ioDispatcher = appDispatchers.io,
        ),
    )
    private val networkSourceCoordinator = NetworkSourceCoordinator(
        sourceStore = networkSourceStore,
        credentialStoreProvider = { networkCredentialStoreDelegate.value },
        registryProvider = { networkFileSystemRegistryDelegate.value },
        inventoryStore = networkInventoryStore,
        mutationJournal = networkSourceMutationJournal,
    )
    private val updateControllerDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        createUpdateController(
            context = applicationContext,
            scope = optionalScope,
            preferences = preferenceStore,
            backgroundWorkPolicy = backgroundWorkPolicy,
        )
    }
    val updateController: UpdateController get() = updateControllerDelegate.value
    private val artistImageRepositoryDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ArtistImageRepository(
            appContext = applicationContext,
            scope = optionalScope,
            ioDispatcher = appDispatchers.io,
        )
    }
    val artistImageRepository get() = artistImageRepositoryDelegate.value
    val albumTagEditorService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AlbumTagEditorService(
            applicationContext,
            mediaMutationJournal = mediaMutationJournal,
            ioDispatcher = appDispatchers.io,
        )
    }

    val audiobookTagEditorService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AudiobookTagEditorService(albumTagEditorService)
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
    private val librarySnapshotStore = LibrarySnapshotStore(applicationContext)
    val playbackResumptionGateway = DefaultPlaybackResumptionGateway(
        hasAudioReadPermission = { applicationContext.hasAudioReadPermission() },
        persistedSessionReader = playbackSessionStore::load,
        librarySongsReader = { librarySnapshotStore.load()?.snapshot?.songs.orEmpty() },
        ioDispatcher = appDispatchers.io,
    )
    val libraryRepository = LibraryRepository(
        appContext = applicationContext,
        scanner = LibraryScanCoordinator(
            localScanner = MediaStoreScanner(
                context = applicationContext,
                ioDispatcher = appDispatchers.io,
            ),
            safScanner = SafTreeLibraryScanner(applicationContext),
            networkScannerProvider = { networkScannerDelegate.value },
        ).also { scanner ->
            scanner.setNetworkSources(networkSourceStore.sources.value)
        },
        scope = libraryScope,
        backgroundWorkPolicy = backgroundWorkPolicy,
        libraryIndexStore = LibraryIndexStore(database.libraryIndexDao()),
        ioDispatcher = appDispatchers.io,
        defaultDispatcher = appDispatchers.default,
        onSongRelocations = { replacements ->
            when (val result = userDataStore.relocateSongReferences(replacements).await()) {
                is PlaylistMutationResult.Success -> {
                    playbackManager.remapAudiobookProgress(replacements)
                    SongRelocationOutcome.Applied
                }
                is PlaylistMutationResult.Failure -> SongRelocationOutcome.RetryableFailure
                else -> SongRelocationOutcome.UnrecoverableConflict
            }
        },
    ).also {
        it.setLibraryFolders(preferenceStore.libraryFolders.value)
    }
    private val networkMutationRuntime = NetworkSourceMutationRuntime(
        scope = optionalScope,
        coordinator = networkSourceCoordinator,
        onResult = { result ->
            when (result) {
                is NetworkSourceMutationResult.Checking -> {
                    _networkProbeResults.update {
                        it + (result.sourceId to NetworkProbeResult(NetworkAvailability.Checking))
                    }
                }
                is NetworkSourceMutationResult.Saved -> {
                    _networkProbeResults.update { it + (result.sourceId to result.probeResult) }
                    startNetworkServices()
                    libraryRepository.unblockNetworkSource(result.sourceId)
                    libraryRepository.setNetworkSources(
                        networkSourceStore.sources.value,
                        enrichMetadata = false,
                        showLoadingIndicator = true,
                        forceRefreshSourceIds = if (result.refreshRequired) {
                            setOf(result.sourceId)
                        } else {
                            emptySet()
                        },
                    )
                }
                is NetworkSourceMutationResult.Removed -> {
                    _networkProbeResults.update { it - result.sourceId }
                    startNetworkServices()
                    libraryRepository.unblockNetworkSource(result.sourceId)
                    libraryRepository.setNetworkSources(
                        networkSourceStore.sources.value,
                        enrichMetadata = false,
                        showLoadingIndicator = true,
                        forceRefreshSourceIds = setOf(result.sourceId),
                    )
                }
                is NetworkSourceMutationResult.Failed -> {
                    _networkProbeResults.update {
                        it + (
                            result.sourceId to NetworkProbeResult(
                                availability = NetworkAvailability.Unavailable,
                                message = result.failureType,
                            )
                        )
                    }
                }
            }
        },
        ioDispatcher = appDispatchers.io,
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
        startNetworkServices()
        networkMutationRuntime.save(source, credentials)
    }

    fun removeNetworkSource(source: NetworkLibrarySource) {
        networkMutationRuntime.remove(source)
    }

    private val mediaTree = ElovaireMediaTree(libraryRepository, preferenceStore)
    private val startupCoordinator = AppStartupCoordinator(
        applicationContext = applicationContext,
        appScope = appScope,
        optionalScope = optionalScope,
        backgroundWorkPolicy = backgroundWorkPolicy,
        portableSettingsBackup = portableSettingsBackup,
        portableUserDataBackup = portableUserDataBackup,
        userDataStore = userDataStore,
        libraryRepository = libraryRepository,
        networkSourceMutationJournal = networkSourceMutationJournal,
        networkSourceStore = networkSourceStore,
        networkCredentialStoreProvider = { networkCredentialStoreDelegate.value },
        invalidateNetworkSourceRuntime = { sourceId ->
            networkFileSystemRegistryDelegate.value.invalidate(sourceId)
        },
        networkInventoryStore = networkInventoryStore,
        mediaMutationJournal = mediaMutationJournal,
        updateControllerProvider = { updateController },
        ioDispatcher = appDispatchers.io,
    )
    private val mediaLibraryInvalidationCoordinator = MediaLibraryInvalidationCoordinator(
        session = playbackManager.mediaLibrarySession,
        libraryRepository = libraryRepository,
        settings = preferenceStore,
        scope = libraryScope,
        dispatcher = appDispatchers.default,
    )

    init {
        playbackManager.setMediaLibrarySessionCallback(
            ElovaireMediaLibrarySessionCallback(
                browser = mediaTree,
                commandResolver = mediaTree,
                playbackManager = playbackManager,
                readExecutor = mediaLibraryReadExecutor,
                startupReady = startupCoordinator.durableStartupReady,
                startupOperational = { startupCoordinator.durableStartupState.value.allowsMediaButton() },
            ),
        )
    }

    fun start() {
        if (released.get()) return
        startNetworkServices()
        mediaLibraryInvalidationCoordinator.start()
        startupCoordinator.start()
    }

    fun startPlayback() {
        if (released.get()) return
        startNetworkServices()
        mediaLibraryInvalidationCoordinator.start()
        startupCoordinator.startPlayback()
    }

    private fun startNetworkServices() {
        if (networkSourceStore.sources.value.none(NetworkLibrarySource::enabled)) return
        if (!networkServicesStarted.compareAndSet(false, true)) return
        optionalScope.launch(appDispatchers.io) {
            try {
                ElovaireTrace.section("network_services_start") {
                    networkFileSystemRegistryDelegate.value.start()
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: SecurityException) {
                networkServicesStarted.set(false)
                android.util.Log.w("AppServices", "Network services could not start; retrying later.", failure)
            } catch (failure: IllegalArgumentException) {
                networkServicesStarted.set(false)
                android.util.Log.w("AppServices", "Network services could not start; retrying later.", failure)
            } catch (failure: IllegalStateException) {
                networkServicesStarted.set(false)
                android.util.Log.w("AppServices", "Network services could not start; retrying later.", failure)
            } catch (failure: RuntimeException) {
                networkServicesStarted.set(false)
                android.util.Log.w("AppServices", "Network services could not start; retrying later.", failure)
            }
        }
    }

    internal fun exportPortableUserData(): ByteArray {
        check(userDataStore.userDataReadiness.value == UserDataReadiness.Ready) {
            "User data is not ready for export."
        }
        check(libraryRepository.scanState.value.isAuthoritative) {
            "The library is not ready for portable user-data export."
        }
        return elovaire.music.droidbeauty.app.data.settings.encodePortableUserData(
            snapshot = userDataStore.userDataSnapshot.value,
            songs = libraryRepository.contentState.value.songs,
            createdAtMs = AndroidAppClock.wallTimeMs(),
            appVersion = BuildConfig.VERSION_NAME,
            userDataRevision = userDataStore.currentUserDataRevision,
        )
    }

    internal fun importPortableUserData(bytes: ByteArray): kotlinx.coroutines.Deferred<PlaylistMutationResult> {
        if (
            userDataStore.userDataReadiness.value != UserDataReadiness.Ready ||
            !libraryRepository.scanState.value.isAuthoritative
        ) {
            return kotlinx.coroutines.CompletableDeferred(
                PlaylistMutationResult.Failure("The library is not ready for portable user-data import."),
            )
        }
        return userDataStore.restorePortableUserData(bytes, libraryRepository.contentState.value.songs)
    }

    fun onMemoryPressure(pressure: MemoryPressure) {
        if (lyricsServiceDelegate.isInitialized()) lyricsService.onMemoryPressure(pressure)
        if (artistImageRepositoryDelegate.isInitialized()) artistImageRepository.onMemoryPressure(pressure)
        audiobookChapterReader.onMemoryPressure(pressure)
        libraryRepository.onMemoryPressure(pressure)
        mediaTree.onMemoryPressure(pressure)
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        val databaseClosed = AtomicBoolean(false)
        val closeDatabase = {
            if (databaseClosed.compareAndSet(false, true)) {
                try {
                    database.close()
                } finally {
                    databaseResource.close()
                }
            }
        }
        releaseBestEffort(
            { startupCoordinator.release() },
            { networkMutationRuntime.release() },
            { mediaLibraryInvalidationCoordinator.close() },
            { serviceScopes.cancelOptional() },
            { playbackManager.release() },
            { serviceScopes.cancelPlayback() },
            { if (updateControllerDelegate.isInitialized()) updateController.release() },
            { if (artistImageRepositoryDelegate.isInitialized()) artistImageRepository.release() },
            { libraryRepository.release() },
            { serviceScopes.cancelLibrary() },
            {
                if (networkFileSystemRegistryDelegate.isInitialized()) {
                    networkFileSystemRegistryDelegate.value.release()
                }
            },
            { mediaMutationJournal.close() },
            {
                try {
                    preferenceStore.release(closeDatabase)
                } catch (failure: Throwable) {
                    try {
                        closeDatabase()
                    } catch (cleanupFailure: Throwable) {
                        if (cleanupFailure !== failure) failure.addSuppressed(cleanupFailure)
                    }
                    throw failure
                }
            },
            { portableSettingsBackup.release() },
            { mediaLibraryReadExecutor.close() },
        )
    }
}
