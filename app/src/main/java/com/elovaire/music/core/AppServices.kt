package elovaire.music.droidbeauty.app.core

import android.content.Context
import kotlinx.coroutines.CoroutineScope

internal class AppServices(
    applicationContext: Context,
    appScope: CoroutineScope,
    backgroundWorkPolicy: AppBackgroundWorkPolicy,
) {
    private val settingsComponent = SettingsComponent(applicationContext)
    private val updateComponent = UpdateComponent(
        context = applicationContext,
        scope = appScope,
        preferenceStore = preferenceStore,
        backgroundWorkPolicy = backgroundWorkPolicy,
    )
    private val lyricsComponent = LyricsComponent(
        context = applicationContext,
        preferenceStore = preferenceStore,
        backgroundWorkPolicy = backgroundWorkPolicy,
    )
    private val tagEditingComponent = TagEditingComponent(applicationContext)
    private val playbackComponent = PlaybackComponent(
        context = applicationContext,
        scope = appScope,
        preferenceStore = preferenceStore,
    )
    private val libraryComponent = LibraryComponent(
        context = applicationContext,
        scope = appScope,
        preferenceStore = preferenceStore,
        backgroundWorkPolicy = backgroundWorkPolicy,
    )
    private val mediaLibraryComponent = MediaLibraryComponent(
        libraryRepository = libraryRepository,
        preferenceStore = preferenceStore,
        playbackManager = playbackManager,
    )

    val preferenceStore get() = settingsComponent.preferenceStore
    val appUpdateManager get() = updateComponent.appUpdateManager
    val lyricsService get() = lyricsComponent.lyricsService
    val albumTagEditorService get() = tagEditingComponent.albumTagEditorService
    val playbackEffectsController get() = playbackComponent.playbackEffectsController
    val playbackManager get() = playbackComponent.playbackManager
    val libraryRepository get() = libraryComponent.libraryRepository

    fun release() {
        updateComponent.release()
        lyricsComponent.release()
        libraryComponent.release()
        playbackComponent.release()
        settingsComponent.release()
    }
}
