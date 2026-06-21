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
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.update.AppUpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@SuppressLint("UnsafeOptInUsageError")
@OptIn(kotlinx.coroutines.FlowPreview::class)
class AppContainer(
    appContext: Context,
) {
    private val applicationContext = appContext.applicationContext
    private val appJob = SupervisorJob()
    private val appScope = CoroutineScope(appJob + Dispatchers.Main.immediate)

    val preferenceStore = PreferenceStore(applicationContext)
    val appUpdateManager = AppUpdateManager(
        context = applicationContext,
        scope = appScope,
        preferenceStore = preferenceStore,
    )
    val lyricsService = LyricsService(applicationContext)
    internal val albumTagEditorService = AlbumTagEditorService(applicationContext)
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
    ).also { repository ->
        repository.setPreferredLibraryFolderPath(preferenceStore.libraryFolderPath.value)
    }
    private val openNowPlayingChannel = Channel<Unit>(capacity = Channel.CONFLATED)
    private val coldStartHomeResetConsumed = AtomicBoolean(false)
    val openNowPlayingCommands: Flow<Unit> = openNowPlayingChannel.receiveAsFlow()

    init {
        appScope.launch {
            preferenceStore.eqSettings.debounce(40L).collect { settings ->
                playbackEffectsController.applyEffectSettings(settings)
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
        appScope.launch {
            libraryRepository.contentState
                .map { it.songs }
                .distinctUntilChanged()
                .collect(playbackManager::refreshLibraryMetadata)
        }
        appScope.launch {
            preferenceStore.gaplessPlaybackEnabled
                .map { it }
                .distinctUntilChanged()
                .collect(playbackManager::setGaplessPlaybackEnabled)
        }
        appScope.launch {
            preferenceStore.libraryFolderPath
                .collect(libraryRepository::setPreferredLibraryFolderPath)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        if (!enabled && playbackNotificationController == null) return
        notificationController().setNotificationsEnabled(enabled)
    }

    fun requestOpenNowPlaying() {
        openNowPlayingChannel.trySend(Unit)
    }

    fun consumeColdStartHomeReset(): Boolean {
        return coldStartHomeResetConsumed.compareAndSet(false, true)
    }

    fun scheduleDeferredStartupWork() {
        appUpdateManager.scheduleStartupMaintenance()
    }

    fun release() {
        openNowPlayingChannel.close()
        playbackNotificationController?.setNotificationsEnabled(false)
        playbackNotificationController = null
        appUpdateManager.release()
        lyricsService.release()
        libraryRepository.release()
        playbackManager.release()
        appJob.cancel()
    }

    private fun notificationController(): PlaybackNotificationController {
        PlaybackNotificationController.ensureNotificationChannel(applicationContext)
        return playbackNotificationController ?: PlaybackNotificationController(
            context = applicationContext,
            playbackManager = playbackManager,
            scope = appScope,
        ).also { controller ->
            playbackNotificationController = controller
        }
    }
}
