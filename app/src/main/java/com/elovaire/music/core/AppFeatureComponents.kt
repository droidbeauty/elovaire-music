package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
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

internal class SettingsComponent(context: Context) {
    val preferenceStore = PreferenceStore(context)

    fun release() {
        preferenceStore.release()
    }
}

internal class UpdateComponent(
    context: Context,
    scope: CoroutineScope,
    preferenceStore: PreferenceStore,
    backgroundWorkPolicy: AppBackgroundWorkPolicy,
) {
    val appUpdateManager = AppUpdateManager(
        context = context,
        scope = scope,
        preferenceStore = preferenceStore,
        backgroundWorkPolicy = backgroundWorkPolicy,
    )

    fun release() {
        appUpdateManager.release()
    }
}

internal class LyricsComponent(
    context: Context,
    preferenceStore: PreferenceStore,
    backgroundWorkPolicy: AppBackgroundWorkPolicy,
) {
    val lyricsService = LyricsService(
        context = context,
        onlineLookupEnabled = preferenceStore.onlineLyricsLookupEnabled,
        backgroundWorkPolicy = backgroundWorkPolicy,
    )

    fun release() {
        lyricsService.release()
    }
}

internal class TagEditingComponent(context: Context) {
    val albumTagEditorService = AlbumTagEditorService(context)
}

@OptIn(UnstableApi::class)
internal class PlaybackComponent(
    context: Context,
    scope: CoroutineScope,
    preferenceStore: PreferenceStore,
) {
    val playbackEffectsController = PlaybackEffectsController()
    val playbackManager = PlaybackManager(
        context = context,
        scope = scope,
        audioProcessorsProvider = playbackEffectsController::audioProcessors,
        hasSignalAlteringEffects = playbackEffectsController::hasSignalAlteringEffects,
        initialRecentSongIds = preferenceStore.recentSongIds.value,
        initialRecentAlbumIds = preferenceStore.recentAlbumIds.value,
        initialLastPlayedCollectionKind = preferenceStore.lastPlayedCollectionKind.value,
        initialLastPlayedCollectionId = preferenceStore.lastPlayedCollectionId.value,
        onRecentPlaybackChanged = preferenceStore::setRecentPlaybackIds,
    )

    fun release() {
        playbackManager.release()
    }
}

internal class LibraryComponent(
    context: Context,
    scope: CoroutineScope,
    preferenceStore: PreferenceStore,
    backgroundWorkPolicy: AppBackgroundWorkPolicy,
) {
    val libraryRepository = LibraryRepository(
        appContext = context,
        scanner = MediaStoreScanner(context),
        scope = scope,
        backgroundWorkPolicy = backgroundWorkPolicy,
    ).also { repository ->
        repository.setLibraryFolders(preferenceStore.libraryFolders.value)
    }

    fun release() {
        libraryRepository.release()
    }
}

@SuppressLint("UnsafeOptInUsageError")
internal class MediaLibraryComponent(
    libraryRepository: LibraryRepository,
    preferenceStore: PreferenceStore,
    playbackManager: PlaybackManager,
) {
    private val mediaTree = ElovaireMediaTree(libraryRepository, preferenceStore)
    private val mediaLibraryCallback = ElovaireMediaLibrarySessionCallback(
        mediaTree = mediaTree,
        playbackManager = playbackManager,
    )

    init {
        playbackManager.setMediaLibrarySessionCallback(mediaLibraryCallback)
    }
}
