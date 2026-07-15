package elovaire.music.droidbeauty.app.core

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.MediaStoreScanner
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.library.db.LibraryIndexStore
import elovaire.music.droidbeauty.app.data.library.db.PersistenceMaintenance
import elovaire.music.droidbeauty.app.data.artist.ArtistImageRepository
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffectsController
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaLibrarySessionCallback
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaTree
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.update.AppUpdateManager
import elovaire.music.droidbeauty.app.data.update.UpdateController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(UnstableApi::class)
internal class AppServices(
    applicationContext: Context,
    private val appScope: CoroutineScope,
    backgroundWorkPolicy: AppBackgroundWorkPolicy,
) {
    private val released = AtomicBoolean(false)
    private val database = ElovaireDatabase.create(applicationContext)
    private val mediaMutationJournal = MediaMutationJournal(database.libraryDao())
    private var persistenceMaintenanceJob: Job? = null
    val preferenceStore = PreferenceStore(applicationContext)
    val artistImageRepository = ArtistImageRepository(applicationContext, backgroundWorkPolicy)
    val appUpdateManager: UpdateController = AppUpdateManager(
        context = applicationContext,
        scope = appScope,
        preferences = preferenceStore,
        backgroundWorkPolicy = backgroundWorkPolicy,
    )
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
    )
    val libraryRepository = LibraryRepository(
        appContext = applicationContext,
        scanner = MediaStoreScanner(applicationContext),
        scope = appScope,
        backgroundWorkPolicy = backgroundWorkPolicy,
        indexStore = LibraryIndexStore(database.libraryDao()),
    ).also { it.setLibraryFolders(preferenceStore.libraryFolders.value) }
    val lyricsService = LyricsService(
        context = applicationContext,
        onlineLookupEnabled = preferenceStore.onlineLyricsLookupEnabled,
        backgroundWorkPolicy = backgroundWorkPolicy,
        mediaMutationJournal = mediaMutationJournal,
        onEmbeddedLyricsChanged = callback@{ song ->
            if (released.get()) return@callback
            appScope.launch {
                if (released.get()) return@launch
                song.libraryPath
                    ?.takeIf { it.isNotBlank() }
                    ?.let { path ->
                        libraryRepository.refreshChangedFiles(
                            filePaths = listOf(path),
                            songIds = listOf(song.id),
                            enrichMetadata = false,
                        )
                    }
            }
        },
    )

    private val mediaTree = ElovaireMediaTree(libraryRepository, preferenceStore)

    init {
        playbackManager.setMediaLibrarySessionCallback(
            ElovaireMediaLibrarySessionCallback(
                mediaTree = mediaTree,
                playbackManager = playbackManager,
            ),
        )
    }

    fun scheduleDeferredMaintenance() {
        if (released.get() || persistenceMaintenanceJob?.isActive == true) return
        persistenceMaintenanceJob = appScope.launch {
            PersistenceMaintenance(database.persistenceMaintenanceDao(), mediaMutationJournal).recoverAndPrune()
        }
    }

    fun onMemoryPressure(pressure: MemoryPressure) {
        lyricsService.onMemoryPressure(pressure)
        artistImageRepository.onMemoryPressure(pressure)
        mediaTree.onMemoryPressure(pressure)
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        persistenceMaintenanceJob?.cancel()
        persistenceMaintenanceJob = null
        appUpdateManager.release()
        lyricsService.release()
        playbackManager.release()
        libraryRepository.release()
        preferenceStore.release()
        database.close()
    }
}
