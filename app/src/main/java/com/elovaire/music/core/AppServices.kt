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
import elovaire.music.droidbeauty.app.data.library.MediaFailureRegistry
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
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceMutationJournal
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibraryBackend
import elovaire.music.droidbeauty.app.data.library.network.SmbNetworkFileSystem
import elovaire.music.droidbeauty.app.data.library.network.WebDavNetworkFileSystem
import elovaire.music.droidbeauty.app.data.audio.MediaMetadataRetrieverAdmission
import elovaire.music.droidbeauty.app.core.hasLocalNetworkPermission
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticsRuntime
import elovaire.music.droidbeauty.app.core.backend.BackendEventSink
import elovaire.music.droidbeauty.app.data.playback.NetworkDataSourceFactory
import androidx.media3.datasource.DefaultDataSource
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.artist.ArtistImageRepository
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationRuntime
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffectsController
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackSessionStore
import elovaire.music.droidbeauty.app.data.playback.AudiobookChapterReader
import elovaire.music.droidbeauty.app.data.playback.Media3AudiobookChapterReader
import elovaire.music.droidbeauty.app.data.library.AudiobookDescriptionReader
import elovaire.music.droidbeauty.app.data.library.GoogleBooksAudiobookDescriptionReader
import elovaire.music.droidbeauty.app.data.playback.DefaultPlaybackResumptionGateway
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaLibrarySessionCallback
import elovaire.music.droidbeauty.app.data.playback.library.PlaybackManagerMediaPlaybackPort
import elovaire.music.droidbeauty.app.data.playback.library.StartupReadinessPort
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaTree
import elovaire.music.droidbeauty.app.data.playback.library.MediaLibraryInvalidationCoordinator
import elovaire.music.droidbeauty.app.data.playback.library.MediaLibraryReadExecutor
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.data.settings.PortableSettingsBackup
import elovaire.music.droidbeauty.app.data.settings.PortableUserDataBackup
import elovaire.music.droidbeauty.app.data.settings.RoomUserDataStore
import elovaire.music.droidbeauty.app.data.settings.SettingsDrainResult
import elovaire.music.droidbeauty.app.data.settings.UserDataReadiness
import elovaire.music.droidbeauty.app.data.settings.UserDataRecoverySnapshot
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.tags.AudiobookTagEditorService
import elovaire.music.droidbeauty.app.data.update.UpdateController
import elovaire.music.droidbeauty.app.data.update.createUpdateController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(UnstableApi::class)
@Suppress("TooGenericExceptionCaught")
internal class AppServices(
    val applicationContext: Context,
    private val appScope: CoroutineScope,
    private val appDispatchers: AppDispatchers,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
    private val portableSettingsBackup: PortableSettingsBackup,
    internal val backendDiagnostics: BackendDiagnosticsRuntime,
) {
    private val releaseStarted = AtomicBoolean(false)
    private val releaseCompletion = CompletableDeferred<AppServicesShutdownResult>()
    private val serviceScopes = AppServiceScopes(appScope, backendDiagnostics)
    private val playbackScope = serviceScopes.playback
    private val libraryScope = serviceScopes.library
    private val optionalScope = serviceScopes.optional
    private val backendEventSink: BackendEventSink = backendDiagnostics
    private val mediaLibraryReadExecutor = MediaLibraryReadExecutor.bounded()
    private val database = ElovaireDatabase.create(applicationContext)
    private val databaseResource = backendDiagnostics.resources.acquire(BackendResourceKind.DatabaseInstance)
    private val libraryMediaFailureRegistry = MediaFailureRegistry()
    private val mediaMetadataRetrieverAdmission = MediaMetadataRetrieverAdmission()
    private val mediaMutationJournal = MediaMutationJournal(
        dao = database.mediaMutationDao(),
        backendEventSink = backendEventSink,
        resourceTracker = backendDiagnostics.resources,
    )
    private val mediaMutationRuntime = MediaMutationRuntime()
    internal val userDataStore = RoomUserDataStore(
        context = applicationContext,
        dao = database.userDataDao(),
        database = database,
        recoverySnapshot = UserDataRecoverySnapshot(applicationContext),
        ioDispatcher = appDispatchers.io,
        ownerScope = appScope,
        resourceTracker = backendDiagnostics.resources,
    )
    private val portableUserDataBackup = PortableUserDataBackup(applicationContext)
    val preferenceStore = PreferenceStore(
        context = applicationContext,
        ioDispatcher = appDispatchers.io,
        ownerScope = appScope,
        initialSettings = portableSettingsBackup.bootSettingsSnapshot(),
        portableSettingsBackup = portableSettingsBackup,
        diagnostics = backendDiagnostics,
    )
    private val networkSourceStore = NetworkLibrarySourceStore(applicationContext)
    private val networkSourceMutationJournal = NetworkSourceMutationJournal(applicationContext)
    private val networkInventoryStore = NetworkInventoryStore(
        applicationContext,
        database.networkInventoryDao(),
        networkSourceStore,
    )
    private val networkCredentialStoreDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        NetworkCredentialStore(applicationContext)
    }
    private val networkFileSystemRegistryDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        NetworkFileSystemRegistry(
            sourceStore = networkSourceStore,
            credentialStore = networkCredentialStoreDelegate.value,
            fileSystems = mapOf(
                NetworkLibraryProtocol.Smb to SmbNetworkFileSystem(
                    scope = optionalScope,
                    resourceTracker = backendDiagnostics.resources,
                ),
                NetworkLibraryProtocol.WebDav to WebDavNetworkFileSystem(backendDiagnostics.resources),
            ),
            localNetworkAccessAllowed = { applicationContext.hasLocalNetworkPermission() },
            applicationContext = applicationContext,
            resourceTracker = backendDiagnostics.resources,
        )
    }
    private var networkLibraryBackend: NetworkLibraryBackend? = null
    private val networkScannerDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        NetworkLibraryScanner(
            context = applicationContext,
            registry = networkFileSystemRegistryDelegate.value,
            inventory = networkInventoryStore,
            ioDispatcher = appDispatchers.io,
            failureRegistry = libraryMediaFailureRegistry,
            retrieverAdmission = mediaMetadataRetrieverAdmission,
            resourceTracker = backendDiagnostics.resources,
            onAvailabilityChanged = { sourceId, result ->
                networkLibraryBackend?.recordProbe(sourceId, result)
            },
        )
    }
    private val networkDataSourceFactory = NetworkDataSourceFactory(
        defaultFactory = DefaultDataSource.Factory(applicationContext),
        registryProvider = { networkFileSystemRegistryDelegate.value },
    )
    val audiobookChapterReader: AudiobookChapterReader = Media3AudiobookChapterReader(
        dataSourceFactory = networkDataSourceFactory,
        backendEventSink = backendEventSink,
    )
    val audiobookDescriptionReader: AudiobookDescriptionReader = GoogleBooksAudiobookDescriptionReader(
        transport = elovaire.music.droidbeauty.app.data.network.BoundedHttpTransport(
            ioDispatcher = appDispatchers.io,
            resourceTracker = backendDiagnostics.resources,
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
            resourceTracker = backendDiagnostics.resources,
        )
    }
    val updateController: UpdateController get() = updateControllerDelegate.value
    private val artistImageRepositoryDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ArtistImageRepository(
            resourceTracker = backendDiagnostics.resources,
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
            mediaMutationRuntime = mediaMutationRuntime,
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
        audiobookProgressRepository = userDataStore,
        audiobookPlaybackSpeedPreferences = preferenceStore.audiobookPlaybackSpeed,
        onAudiobookPlaybackSpeedChanged = preferenceStore::setAudiobookPlaybackSpeed,
        audioProcessorsProvider = playbackEffectsController::audioProcessors,
        hasSignalAlteringEffects = playbackEffectsController::hasSignalAlteringEffects,
        initialRecentSongIds = userDataStore.recentSongIds.value,
        initialRecentAlbumIds = userDataStore.recentAlbumIds.value,
        initialLastPlayedCollectionKind = userDataStore.lastPlayedCollectionKind.value,
        initialLastPlayedCollectionId = userDataStore.lastPlayedCollectionId.value,
        onRecentPlaybackChanged = userDataStore::setRecentPlaybackIds,
        playbackDataSourceFactory = networkDataSourceFactory,
        resourceTracker = backendDiagnostics.resources,
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
                failureRegistry = libraryMediaFailureRegistry,
                retrieverAdmission = mediaMetadataRetrieverAdmission,
                resourceTracker = backendDiagnostics.resources,
            ),
            safScanner = SafTreeLibraryScanner(
                context = applicationContext,
                failureRegistry = libraryMediaFailureRegistry,
                retrieverAdmission = mediaMetadataRetrieverAdmission,
                resourceTracker = backendDiagnostics.resources,
            ),
            networkScannerProvider = { networkScannerDelegate.value },
        ).also { scanner ->
            scanner.setNetworkSources(networkSourceStore.sources.value)
        },
        scope = libraryScope,
        backgroundWorkPolicy = backgroundWorkPolicy,
        ioDispatcher = appDispatchers.io,
        defaultDispatcher = appDispatchers.default,
        backendEventSink = backendEventSink,
        resourceTracker = backendDiagnostics.resources,
        onSongRelocations = { commitId, replacements ->
            when (val result = userDataStore.relocateSongReferences(commitId, replacements).await()) {
                is PlaylistMutationResult.Success -> {
                    SongRelocationOutcome.Applied
                }
                is PlaylistMutationResult.Failure -> SongRelocationOutcome.RetryableFailure
                else -> SongRelocationOutcome.UnrecoverableConflict
            }
        },
    ).also {
        it.setLibraryFolders(preferenceStore.libraryFolders.value)
    }
    private val networkBackend = NetworkLibraryBackend(
        optionalScope = optionalScope,
        sourceStore = networkSourceStore,
        registryProvider = { networkFileSystemRegistryDelegate.value },
        coordinator = networkSourceCoordinator,
        library = libraryRepository,
        ioDispatcher = appDispatchers.io,
    ).also { networkLibraryBackend = it }
    private val durableRecoveryRuntime = DurableRecoveryRuntime(
        mediaMutationJournal = mediaMutationJournal,
        networkSourceMutationJournal = networkSourceMutationJournal,
        networkSourceStore = networkSourceStore,
        networkCredentialStoreProvider = { networkCredentialStoreDelegate.value },
        networkInventoryStore = networkInventoryStore,
        invalidateNetworkSourceRuntime = { sourceId ->
            networkFileSystemRegistryDelegate.value.invalidate(sourceId)
        },
    )
    private val optionalStartupRuntime = OptionalStartupRuntime(
        scope = optionalScope,
        backgroundWorkPolicy = backgroundWorkPolicy,
        portableSettingsBackup = portableSettingsBackup,
        updateControllerProvider = { updateController },
        ioDispatcher = appDispatchers.io,
    )
    private val portableUserDataBackupRuntime = PortableUserDataBackupRuntime(
        appScope = appScope,
        portableUserDataBackup = portableUserDataBackup,
        userDataStore = userDataStore,
        libraryRepository = libraryRepository,
        ioDispatcher = appDispatchers.io,
    )
    private val lyricsServiceDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LyricsService(
            context = applicationContext,
            mediaMutationJournal = mediaMutationJournal,
            mediaMutationRuntime = mediaMutationRuntime,
            resourceTracker = backendDiagnostics.resources,
            onlineLyricsEnabled = { preferenceStore.onlineLyricsEnabled.value },
        )
    }
    val lyricsService get() = lyricsServiceDelegate.value
    val networkSources get() = networkBackend.sources
    val networkProbeResults: StateFlow<Map<String, NetworkProbeResult>> = networkBackend.probeResults

    fun saveNetworkSource(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ) {
        networkBackend.save(source, credentials)
    }

    fun removeNetworkSource(source: NetworkLibrarySource) {
        networkBackend.remove(source)
    }

    private val mediaTree = ElovaireMediaTree(libraryRepository, userDataStore)
    private val startupCoordinator = AppStartupCoordinator(
        applicationContext = applicationContext,
        appScope = appScope,
        backgroundWorkPolicy = backgroundWorkPolicy,
        libraryRepository = libraryRepository,
        durableRecoveryRuntime = durableRecoveryRuntime,
        optionalStartupRuntime = optionalStartupRuntime,
        updateControllerProvider = { updateController },
        backendDiagnostics = backendDiagnostics,
        ioDispatcher = appDispatchers.io,
    )
    private val mediaLibraryInvalidationCoordinator = MediaLibraryInvalidationCoordinator(
        session = playbackManager.mediaLibrarySession,
        libraryRepository = libraryRepository,
        settings = userDataStore,
        scope = libraryScope,
        dispatcher = appDispatchers.default,
    )

    init {
        playbackManager.setMediaLibrarySessionCallback(
            ElovaireMediaLibrarySessionCallback(
                playback = PlaybackManagerMediaPlaybackPort(playbackManager),
                readExecutor = mediaLibraryReadExecutor,
                catalog = mediaTree,
                readiness = StartupReadinessPort(
                    ready = startupCoordinator.durableStartupReady,
                    operational = { startupCoordinator.durableStartupState.value.allowsMediaButton() },
                ),
            ),
        )
    }

    fun start() {
        startPlayback()
        startupCoordinator.start()
    }

    fun startPlayback() {
        networkBackend.start()
        mediaLibraryInvalidationCoordinator.start()
        startupCoordinator.startPlayback()
        portableUserDataBackupRuntime.start()
    }

    internal fun exportPortableUserData(): ByteArray {
        check(userDataStore.userDataReadiness.value == UserDataReadiness.Ready) {
            "User data is not ready for export."
        }
        check(libraryRepository.scanState.value.isAuthoritative) {
            "The library is not ready for portable user-data export."
        }
        val content = libraryRepository.contentState.value
        val revisionedUserData = userDataStore.revisionedUserDataSnapshot.value
        return portableUserDataBackup.encode(
            snapshot = revisionedUserData.snapshot,
            songs = content.songs,
            createdAtMs = AndroidAppClock.wallTimeMs(),
            appVersion = BuildConfig.VERSION_NAME,
            userDataRevision = revisionedUserData.revision,
            contentRevision = content.portableMediaIdentityRevision,
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

    fun release(): Deferred<AppServicesShutdownResult> {
        if (!releaseStarted.compareAndSet(false, true)) return releaseCompletion
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
        val failures = mutableListOf<Throwable>()
        try {
            releaseBestEffort(
                { startupCoordinator.release() },
                { portableUserDataBackupRuntime.release() },
                { networkBackend.close() },
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
                { mediaMutationRuntime.close() },
                { mediaMutationJournal.close() },
                { portableSettingsBackup.release() },
                { mediaLibraryReadExecutor.close() },
            )
        } catch (failure: Throwable) {
            failures += failure
        }

        var settingsDrain: Deferred<SettingsDrainResult>? = null
        var userDataDrain: Deferred<Unit>? = null
        try {
            settingsDrain = preferenceStore.release()
        } catch (failure: Throwable) {
            failures += failure
        }
        try {
            userDataDrain = userDataStore.release(closeDatabase)
        } catch (failure: Throwable) {
            failures += failure
            runCatching { closeDatabase() }.onFailure(failures::add)
        }
        appScope.launch {
            settingsDrain?.let { drain ->
                runCatching { drain.await() }.onFailure(failures::add)
            }
            userDataDrain?.let { drain ->
                runCatching { drain.await() }.onFailure(failures::add)
            }
            failures.forEach { failure -> backendDiagnostics.recordWorkerFailure("app-shutdown", failure) }
            releaseCompletion.complete(AppServicesShutdownResult(failures.toList()))
        }
        return releaseCompletion
    }
}

internal data class AppServicesShutdownResult(
    val failures: List<Throwable>,
)
