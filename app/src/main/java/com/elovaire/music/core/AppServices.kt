package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import android.content.Context
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.MediaStoreScanner
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffectsController
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.update.AppUpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("UnsafeOptInUsageError")
internal class AppServices(
    applicationContext: Context,
    appScope: CoroutineScope,
    appForegroundState: StateFlow<Boolean>,
) {
    val preferenceStore = PreferenceStore(applicationContext)
    val appUpdateManager = AppUpdateManager(
        context = applicationContext,
        scope = appScope,
        preferenceStore = preferenceStore,
        appForegroundState = appForegroundState,
    )
    val lyricsService = LyricsService(applicationContext)
    val albumTagEditorService = AlbumTagEditorService(applicationContext)
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
        appForegroundState = appForegroundState,
    ).also { repository ->
        repository.setPreferredLibraryFolderPath(preferenceStore.libraryFolderPath.value)
    }

    fun release() {
        appUpdateManager.release()
        lyricsService.release()
        libraryRepository.release()
        playbackManager.release()
        preferenceStore.release()
    }
}
