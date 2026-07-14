package elovaire.music.droidbeauty.app.core

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.MediaStoreScanner
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.library.db.LibraryIndexStore
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
    appScope: CoroutineScope,
    backgroundWorkPolicy: AppBackgroundWorkPolicy,
) {
    private val released = AtomicBoolean(false)
    private val database = ElovaireDatabase.create(applicationContext)
    private val mediaMutationJournal = MediaMutationJournal(database.libraryDao())
    private val mutationRecoveryJob: Job = appScope.launch {
        mediaMutationJournal.recoverIncomplete()
    }
    val preferenceStore = PreferenceStore(applicationContext)
    val appUpdateManager: UpdateController = AppUpdateManager(
        context = applicationContext,
        scope = appScope,
        preferences = preferenceStore,
        backgroundWorkPolicy = backgroundWorkPolicy,
    )
    val albumTagEditorService = AlbumTagEditorService(
        applicationContext,
        mediaMutationJournal = mediaMutationJournal,
    )
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
        onEmbeddedLyricsChanged = { song ->
            appScope.launch {
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

    init {
        playbackManager.setMediaLibrarySessionCallback(
            ElovaireMediaLibrarySessionCallback(
                mediaTree = ElovaireMediaTree(libraryRepository, preferenceStore),
                playbackManager = playbackManager,
            ),
        )
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        appUpdateManager.release()
        lyricsService.release()
        playbackManager.release()
        libraryRepository.release()
        preferenceStore.release()
        mutationRecoveryJob.cancel()
        database.close()
    }
}
