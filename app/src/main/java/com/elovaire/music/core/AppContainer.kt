package elovaire.music.droidbeauty.app.core

import android.annotation.SuppressLint
import android.app.Application
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
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@SuppressLint("UnsafeOptInUsageError")
class AppContainer(
    appContext: Context,
) {
    private val applicationContext = appContext.applicationContext
    private val appForegroundTracker = AppForegroundTracker(applicationContext as Application)
    private val appRuntimeScope = AppRuntimeScope()
    private val appScope = appRuntimeScope.scope

    val preferenceStore = PreferenceStore(applicationContext)
    val appUpdateManager = AppUpdateManager(
        context = applicationContext,
        scope = appScope,
        preferenceStore = preferenceStore,
        appForegroundState = appForegroundTracker.isForeground,
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
    private val notificationControllerHolder = NotificationControllerHolder {
        PlaybackNotificationController.ensureNotificationChannel(applicationContext)
        PlaybackNotificationController(
            context = applicationContext,
            playbackManager = playbackManager,
            scope = appScope,
        )
    }
    val libraryRepository = LibraryRepository(
        appContext = applicationContext,
        scanner = MediaStoreScanner(applicationContext),
        scope = appScope,
        appForegroundState = appForegroundTracker.isForeground,
    ).also { repository ->
        repository.setPreferredLibraryFolderPath(preferenceStore.libraryFolderPath.value)
    }
    private val openNowPlayingChannel = Channel<Unit>(capacity = Channel.CONFLATED)
    private val coldStartHomeResetConsumed = AtomicBoolean(false)
    private val playbackSettingsBridge = PlaybackSettingsBridge(
        scope = appScope,
        preferenceStore = preferenceStore,
        playbackManager = playbackManager,
        playbackEffectsController = playbackEffectsController,
    )
    private val playbackHistoryBridge = PlaybackHistoryBridge(
        scope = appScope,
        preferenceStore = preferenceStore,
        playbackManager = playbackManager,
    )
    private val libraryPlaybackBridge = LibraryPlaybackBridge(
        scope = appScope,
        libraryRepository = libraryRepository,
        playbackManager = playbackManager,
    )
    private val librarySettingsBridge = LibrarySettingsBridge(
        scope = appScope,
        preferenceStore = preferenceStore,
        libraryRepository = libraryRepository,
    )
    private val startupCoordinator = StartupCoordinator(appUpdateManager)
    val openNowPlayingCommands: Flow<Unit> = openNowPlayingChannel.receiveAsFlow()

    init {
        playbackSettingsBridge.start()
        playbackHistoryBridge.start()
        libraryPlaybackBridge.start()
        librarySettingsBridge.start()
        startupCoordinator.start()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        if (!enabled) {
            notificationControllerHolder.release()
            return
        }
        notificationController().setNotificationsEnabled(enabled)
    }

    fun requestOpenNowPlaying() {
        openNowPlayingChannel.trySend(Unit)
    }

    fun consumeColdStartHomeReset(): Boolean {
        return coldStartHomeResetConsumed.compareAndSet(false, true)
    }

    fun scheduleDeferredStartupWork() {
        startupCoordinator.scheduleDeferredStartupWork()
    }

    fun release() {
        openNowPlayingChannel.close()
        notificationControllerHolder.release()
        appUpdateManager.release()
        lyricsService.release()
        libraryRepository.release()
        playbackManager.release()
        preferenceStore.release()
        appRuntimeScope.close()
    }

    private fun notificationController(): PlaybackNotificationController {
        return notificationControllerHolder.get()
    }
}
