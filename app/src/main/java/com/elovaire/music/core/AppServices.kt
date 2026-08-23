package elovaire.music.droidbeauty.app.core

import android.content.Context
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
import elovaire.music.droidbeauty.app.data.library.network.NetworkAvailability
import elovaire.music.droidbeauty.app.data.library.network.NetworkCredentials
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySource
import elovaire.music.droidbeauty.app.data.library.network.NetworkProbeResult
import elovaire.music.droidbeauty.app.data.library.network.NetworkInventoryStore
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceCoordinator
import elovaire.music.droidbeauty.app.data.library.network.SmbNetworkFileSystem
import elovaire.music.droidbeauty.app.data.library.network.WebDavNetworkFileSystem
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
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaLibrarySessionCallback
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaTree
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.data.settings.PortableSettingsBackup
import elovaire.music.droidbeauty.app.data.settings.PreferenceStorage
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.data.settings.RoomUserDataStore
import elovaire.music.droidbeauty.app.data.settings.UpdatePreferencesStoreImpl
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.update.UpdateController
import elovaire.music.droidbeauty.app.data.update.createUpdateController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.GeneralSecurityException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@OptIn(UnstableApi::class)
internal class AppServices(
    val applicationContext: Context,
    private val appScope: CoroutineScope,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
) {
    private val playbackStarted = AtomicBoolean(false)
    private val started = AtomicBoolean(false)
    private val released = AtomicBoolean(false)
    val exitDiagnostics = AppExitDiagnostics(applicationContext)
    private val database = ElovaireDatabase.create(applicationContext)
    private val mediaMutationJournal = MediaMutationJournal(database.libraryDao())
    private val portableSettingsBackup = PortableSettingsBackup(applicationContext)
    private val userDataStore = RoomUserDataStore(
        context = applicationContext,
        dao = database.userDataDao(),
    )
    // Restore portable settings before PreferenceStore snapshots its initial StateFlows.
    val preferenceStore = createPreferenceStore()
    private val networkSourceStore = NetworkLibrarySourceStore(applicationContext)
    private val _networkProbeResults = MutableStateFlow<Map<String, NetworkProbeResult>>(emptyMap())
    private val networkMutationGenerations = ConcurrentHashMap<String, AtomicLong>()
    private val networkMutationJobs = ConcurrentHashMap<String, Job>()
    private val networkInventoryStore = NetworkInventoryStore(applicationContext, database.libraryDao())
    private val networkCredentialStoreDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        NetworkCredentialStore(applicationContext)
    }
    private val networkFileSystemRegistryDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        NetworkFileSystemRegistry(
            sourceStore = networkSourceStore,
            credentialStore = networkCredentialStoreDelegate.value,
            fileSystems = mapOf(
                NetworkLibraryProtocol.Smb to SmbNetworkFileSystem(),
                NetworkLibraryProtocol.WebDav to WebDavNetworkFileSystem(),
            ),
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
    )
    private val updateControllerDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val updatePreferences = allowStrictModeDiskReads {
            UpdatePreferencesStoreImpl(PreferenceStorage(applicationContext).preferences)
        }
        createUpdateController(
            context = applicationContext,
            scope = appScope,
            preferences = updatePreferences,
            backgroundWorkPolicy = backgroundWorkPolicy,
        )
    }
    val updateController: UpdateController get() = updateControllerDelegate.value
    private val artistImageRepositoryDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ArtistImageRepository()
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
        scope = appScope,
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
        scope = appScope,
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

    private fun createPreferenceStore(): PreferenceStore {
        portableSettingsBackup.restore()
        return PreferenceStore(applicationContext, userDataStore)
    }

    fun saveNetworkSource(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ) {
        val generation = nextNetworkMutationGeneration(source.id)
        networkMutationJobs[source.id]?.cancel()
        _networkProbeResults.update { it + (source.id to NetworkProbeResult(NetworkAvailability.Checking)) }
        val job = appScope.launch(Dispatchers.IO) {
            try {
                val result = networkSourceCoordinator.save(source, credentials)
                if (!isCurrentNetworkMutation(source.id, generation)) return@launch
                _networkProbeResults.update { it + (source.id to result) }
                libraryRepository.setNetworkSources(
                    networkSourceStore.sources.value,
                    enrichMetadata = false,
                    showLoadingIndicator = true,
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: GeneralSecurityException) {
                recordNetworkMutationFailure(source.id, generation, failure)
            } catch (failure: SecurityException) {
                recordNetworkMutationFailure(source.id, generation, failure)
            } catch (failure: IllegalArgumentException) {
                recordNetworkMutationFailure(source.id, generation, failure)
            } catch (failure: IllegalStateException) {
                recordNetworkMutationFailure(source.id, generation, failure)
            } finally {
                networkMutationJobs.remove(source.id, coroutineContext[Job])
            }
        }
        networkMutationJobs[source.id] = job
    }

    fun removeNetworkSource(source: NetworkLibrarySource) {
        val generation = nextNetworkMutationGeneration(source.id)
        networkMutationJobs[source.id]?.cancel()
        val job = appScope.launch(Dispatchers.IO) {
            try {
                networkSourceCoordinator.remove(source)
                if (!isCurrentNetworkMutation(source.id, generation)) return@launch
                _networkProbeResults.update { it - source.id }
                libraryRepository.setNetworkSources(
                    networkSourceStore.sources.value,
                    enrichMetadata = false,
                    showLoadingIndicator = true,
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: GeneralSecurityException) {
                recordNetworkMutationFailure(source.id, generation, failure)
            } catch (failure: SecurityException) {
                recordNetworkMutationFailure(source.id, generation, failure)
            } catch (failure: IllegalArgumentException) {
                recordNetworkMutationFailure(source.id, generation, failure)
            } catch (failure: IllegalStateException) {
                recordNetworkMutationFailure(source.id, generation, failure)
            } finally {
                networkMutationJobs.remove(source.id, coroutineContext[Job])
            }
        }
        networkMutationJobs[source.id] = job
    }

    private val mediaTree = ElovaireMediaTree(libraryRepository, preferenceStore)

    init {
        playbackManager.setMediaLibrarySessionCallback(
            ElovaireMediaLibrarySessionCallback(
                browser = mediaTree,
                commandResolver = mediaTree,
                playbackManager = playbackManager,
            ),
        )
    }

    fun start() {
        if (released.get() || !started.compareAndSet(false, true)) return
        startPlayback()
        updateController.start()
        appScope.launch(Dispatchers.IO) {
            val exitSnapshot = exitDiagnostics.inspect()
            backgroundWorkPolicy.setOptionalStartupSuppressed(exitSnapshot.suppressOptionalStartup)
            portableSettingsBackup.start()
            updateController.scheduleStartupMaintenance()
        }
    }

    fun startPlayback() {
        if (released.get() || !playbackStarted.compareAndSet(false, true)) return
        libraryRepository.start()
        libraryRepository.onPermissionChanged(applicationContext.hasAudioReadPermission())
    }

    fun onMemoryPressure(pressure: MemoryPressure) {
        if (lyricsServiceDelegate.isInitialized()) lyricsService.onMemoryPressure(pressure)
        libraryRepository.onMemoryPressure(pressure)
        mediaTree.onMemoryPressure(pressure)
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        started.set(false)
        playbackStarted.set(false)
        networkMutationJobs.values.forEach(Job::cancel)
        networkMutationJobs.clear()
        playbackManager.release()
        if (updateControllerDelegate.isInitialized()) updateController.release()
        libraryRepository.release()
        preferenceStore.release(database::close)
        portableSettingsBackup.release()
    }

    private fun nextNetworkMutationGeneration(sourceId: String): Long =
        networkMutationGenerations
            .computeIfAbsent(sourceId) { AtomicLong() }
            .incrementAndGet()

    private fun isCurrentNetworkMutation(sourceId: String, generation: Long): Boolean =
        !released.get() && networkMutationGenerations[sourceId]?.get() == generation

    private fun recordNetworkMutationFailure(
        sourceId: String,
        generation: Long,
        failure: Throwable,
    ) {
        if (!isCurrentNetworkMutation(sourceId, generation)) return
        _networkProbeResults.update {
            it + (sourceId to NetworkProbeResult(
                availability = NetworkAvailability.Unavailable,
                message = failure::class.simpleName,
            ))
        }
    }
}
