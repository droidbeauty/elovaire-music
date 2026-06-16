package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import android.content.Context
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.MediaStoreScanner
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffectsController
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackNotificationController
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.data.update.AppUpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@SuppressLint("UnsafeOptInUsageError")
class AppContainer(
    appContext: Context,
) {
    private val applicationContext = appContext.applicationContext
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val preferenceStore = PreferenceStore(applicationContext)
    val appUpdateManager = AppUpdateManager(
        context = applicationContext,
        scope = appScope,
        preferenceStore = preferenceStore,
    )
    val lyricsService = LyricsService(applicationContext)
    private val playbackEffectsController = PlaybackEffectsController()
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
    private var playbackNotificationController: PlaybackNotificationController? = null
    val libraryRepository = LibraryRepository(
        appContext = applicationContext,
        scanner = MediaStoreScanner(applicationContext),
        scope = appScope,
    )
    private val _openPlayerRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val openPlayerRequests: SharedFlow<Unit> = _openPlayerRequests.asSharedFlow()

    init {
        PlaybackNotificationController.ensureNotificationChannel(applicationContext)
        appScope.launch {
            preferenceStore.eqSettings.collect { settings ->
                playbackEffectsController.updateSettings(settings)
                playbackManager.reevaluateAudioOutputPath()
            }
        }
        appScope.launch {
            playbackManager.nowPlayingState
                .map { it.currentSong?.id to it.currentSong?.albumId }
                .distinctUntilChanged()
                .collect { (songId, albumId) ->
                    if (songId != null) {
                        preferenceStore.incrementSongPlayCount(songId)
                    }
                    if (albumId != null) {
                        preferenceStore.incrementAlbumPlayCount(albumId)
                    }
                }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        if (!enabled && playbackNotificationController == null) return
        notificationController().setNotificationsEnabled(enabled)
    }

    fun requestOpenPlayer() {
        _openPlayerRequests.tryEmit(Unit)
    }

    private fun notificationController(): PlaybackNotificationController {
        return playbackNotificationController ?: PlaybackNotificationController(
            context = applicationContext,
            playbackManager = playbackManager,
            scope = appScope,
        ).also { controller ->
            playbackNotificationController = controller
        }
    }
}
