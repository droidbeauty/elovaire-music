package elovaire.music.app.core

import android.annotation.SuppressLint
import android.content.Context
import elovaire.music.app.data.library.LibraryRepository
import elovaire.music.app.data.library.MediaStoreScanner
import elovaire.music.app.data.playback.PlaybackEffectsController
import elovaire.music.app.data.playback.PlaybackManager
import elovaire.music.app.data.playback.PlaybackNotificationController
import elovaire.music.app.data.settings.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
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
    val playbackManager = PlaybackManager(
        context = applicationContext,
        scope = appScope,
    )
    private val playbackEffectsController = PlaybackEffectsController()
    private val playbackNotificationController = PlaybackNotificationController(
        context = applicationContext,
        playbackManager = playbackManager,
        scope = appScope,
    )
    val libraryRepository = LibraryRepository(
        appContext = applicationContext,
        scanner = MediaStoreScanner(applicationContext),
        scope = appScope,
    )
    val openPlayerRequestVersion = MutableStateFlow(0L)

    init {
        PlaybackNotificationController.ensureNotificationChannel(applicationContext)
        appScope.launch {
            playbackManager.state
                .map { it.audioSessionId }
                .distinctUntilChanged()
                .collect(playbackEffectsController::updateAudioSessionId)
        }
        appScope.launch {
            preferenceStore.eqSettings.collect(playbackEffectsController::updateSettings)
        }
        appScope.launch {
            playbackManager.state
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
        playbackNotificationController.setNotificationsEnabled(enabled)
    }

    fun requestOpenPlayer() {
        openPlayerRequestVersion.value += 1L
    }
}
