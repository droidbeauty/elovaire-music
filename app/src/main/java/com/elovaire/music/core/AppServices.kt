package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import android.content.Context
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.MediaStoreScanner
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffectsController
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaLibrarySessionCallback
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaTree
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.update.AppUpdateManager
import kotlinx.coroutines.CoroutineScope

@SuppressLint("UnsafeOptInUsageError")
internal class AppServices(
    applicationContext: Context,
    appScope: CoroutineScope,
    backgroundWorkPolicy: AppBackgroundWorkPolicy,
) {
    val preferenceStore = PreferenceStore(applicationContext)
    val appUpdateManager = AppUpdateManager(
        context = applicationContext,
        scope = appScope,
        preferenceStore = preferenceStore,
        backgroundWorkPolicy = backgroundWorkPolicy,
    )
    val lyricsService = LyricsService(
        context = applicationContext,
        onlineLookupEnabled = preferenceStore.onlineLyricsLookupEnabled,
        backgroundWorkPolicy = backgroundWorkPolicy,
    )
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
        backgroundWorkPolicy = backgroundWorkPolicy,
    ).also { repository ->
        repository.setLibraryFolders(preferenceStore.libraryFolders.value)
    }
    private val mediaTree = ElovaireMediaTree(libraryRepository, preferenceStore)
    private val mediaLibraryCallback = ElovaireMediaLibrarySessionCallback(
        mediaTree = mediaTree,
        playbackManager = playbackManager,
    )

    init {
        playbackManager.setMediaLibrarySessionCallback(mediaLibraryCallback)
    }

    fun release() {
        appUpdateManager.release()
        lyricsService.release()
        libraryRepository.release()
        playbackManager.release()
        preferenceStore.release()
    }
}
