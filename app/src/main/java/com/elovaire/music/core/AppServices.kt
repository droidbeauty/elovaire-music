package elovaire.music.droidbeauty.app.core

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.MediaStoreScanner
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.library.db.LibraryIndexStore
import elovaire.music.droidbeauty.app.data.artist.ArtistImageRepository
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffectsController
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackSessionStore
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaLibrarySessionCallback
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaTree
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.data.settings.PortableSettingsBackup
import elovaire.music.droidbeauty.app.data.settings.RoomUserDataStore
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(UnstableApi::class)
internal class AppServices(
    val applicationContext: Context,
    private val appScope: CoroutineScope,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
) {
    private val started = AtomicBoolean(false)
    private val released = AtomicBoolean(false)
    val exitDiagnostics = AppExitDiagnostics(applicationContext)
    private val database = ElovaireDatabase.create(applicationContext)
    private val mediaMutationJournal = MediaMutationJournal(database.libraryDao())
    private val portableSettingsBackup = PortableSettingsBackup(applicationContext)
    private val userDataStore = RoomUserDataStore(
        context = applicationContext,
        dao = database.userDataDao(),
    )
    val preferenceStore = PreferenceStore(applicationContext, userDataStore)
    private val artistImageRepositoryDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ArtistImageRepository(applicationContext, backgroundWorkPolicy)
    }
    val artistImageRepository get() = artistImageRepositoryDelegate.value
    val albumTagEditorService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AlbumTagEditorService(
            applicationContext,
            mediaMutationJournal = mediaMutationJournal,
        )
    }
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
    val playbackSessionStore = PlaybackSessionStore(applicationContext)
    val libraryRepository = LibraryRepository(
        appContext = applicationContext,
        scanner = MediaStoreScanner(applicationContext),
        scope = appScope,
        backgroundWorkPolicy = backgroundWorkPolicy,
        indexStore = LibraryIndexStore(database.libraryDao()),
    ).also { it.setLibraryFolders(preferenceStore.libraryFolders.value) }
    private val lyricsServiceDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LyricsService(
            context = applicationContext,
            onlineLookupEnabled = preferenceStore.onlineLyricsLookupEnabled,
            backgroundWorkPolicy = backgroundWorkPolicy,
            mediaMutationJournal = mediaMutationJournal,
            onEmbeddedLyricsChanged = callback@{ song ->
                if (released.get()) return@callback
                appScope.launch {
                    if (released.get()) return@launch
                    libraryRepository.refreshChangedFiles(
                        filePaths = listOfNotNull(song.libraryPath?.takeIf { it.isNotBlank() }),
                        songIds = listOf(song.id),
                        enrichMetadata = true,
                    )
                }
            },
        )
    }
    val lyricsService get() = lyricsServiceDelegate.value

    private val mediaTree = ElovaireMediaTree(libraryRepository, preferenceStore)

    init {
        playbackManager.setMediaLibrarySessionCallback(
            ElovaireMediaLibrarySessionCallback(
                browser = mediaTree,
                commandResolver = mediaTree,
                playbackManager = playbackManager,
            ),
        )
    }

    fun start() {
        if (released.get() || !started.compareAndSet(false, true)) return
        libraryRepository.start()
        libraryRepository.onPermissionChanged(applicationContext.hasAudioReadPermission())
        appScope.launch(Dispatchers.IO) {
            val exitSnapshot = exitDiagnostics.inspect()
            backgroundWorkPolicy.setOptionalStartupSuppressed(exitSnapshot.suppressOptionalStartup)
            portableSettingsBackup.start()
        }
    }

    fun onMemoryPressure(pressure: MemoryPressure) {
        if (lyricsServiceDelegate.isInitialized()) lyricsService.onMemoryPressure(pressure)
        if (artistImageRepositoryDelegate.isInitialized()) artistImageRepository.onMemoryPressure(pressure)
        mediaTree.onMemoryPressure(pressure)
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        started.set(false)
        if (lyricsServiceDelegate.isInitialized()) lyricsService.release()
        playbackManager.release()
        libraryRepository.release()
        portableSettingsBackup.release()
        preferenceStore.release(database::close)
    }
}
