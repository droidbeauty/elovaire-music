package elovaire.music.droidbeauty.app.core

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.MediaStoreScanner
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
import elovaire.music.droidbeauty.app.data.settings.RoomUserDataStore
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.update.AppUpdateManager
import elovaire.music.droidbeauty.app.data.update.UpdateController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(UnstableApi::class)
internal class AppServices(
    val applicationContext: Context,
    private val appScope: CoroutineScope,
    backgroundWorkPolicy: AppBackgroundWorkPolicy,
) {
    private val released = AtomicBoolean(false)
    val exitDiagnostics = AppExitDiagnostics(applicationContext)
    private val database = ElovaireDatabase.create(applicationContext)
    private val mediaMutationJournal = MediaMutationJournal(database.libraryDao())
    private val portableSettingsBackup = PortableSettingsBackup(applicationContext).also { it.restoreAndStart() }
    private val userDataStore = RoomUserDataStore(
        context = applicationContext,
        dao = database.userDataDao(),
        scope = appScope,
    )
    val preferenceStore = PreferenceStore(applicationContext, userDataStore)
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
    val playbackSessionStore = PlaybackSessionStore(applicationContext)
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

    fun onMemoryPressure(pressure: MemoryPressure) {
        lyricsService.onMemoryPressure(pressure)
        artistImageRepository.onMemoryPressure(pressure)
        mediaTree.onMemoryPressure(pressure)
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        appUpdateManager.release()
        lyricsService.release()
        playbackManager.release()
        libraryRepository.release()
        preferenceStore.release()
        portableSettingsBackup.release()
        database.close()
    }
}
