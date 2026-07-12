package elovaire.music.droidbeauty.app.core

import android.content.Context
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.atomic.AtomicBoolean

internal class AppServices(
    applicationContext: Context,
    appScope: CoroutineScope,
    backgroundWorkPolicy: AppBackgroundWorkPolicy,
) {
    private val released = AtomicBoolean(false)
    private val database = ElovaireDatabase.create(applicationContext)
    private val mediaMutationJournal = MediaMutationJournal(database.libraryDao())
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
        mediaMutationJournal = mediaMutationJournal,
    )
    private val tagEditingComponent = TagEditingComponent(applicationContext, mediaMutationJournal)
    private val playbackComponent = PlaybackComponent(
        context = applicationContext,
        scope = appScope,
        preferenceStore = preferenceStore,
    )
    private val libraryComponent = LibraryComponent(
        context = applicationContext,
        database = database,
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
        if (!released.compareAndSet(false, true)) return
        playbackComponent.release()
        libraryComponent.release()
        lyricsComponent.release()
        updateComponent.release()
        settingsComponent.release()
        database.close()
    }
}
