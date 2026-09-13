package elovaire.music.droidbeauty.app.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import elovaire.music.droidbeauty.app.core.AppBackgroundWorkPolicy
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.core.AppShortcutCommand
import elovaire.music.droidbeauty.app.core.LibraryActionDependencies
import elovaire.music.droidbeauty.app.core.SettingsActionDependencies
import elovaire.music.droidbeauty.app.data.artist.ArtistImageRepository
import elovaire.music.droidbeauty.app.data.library.AudiobookDescriptionReader
import elovaire.music.droidbeauty.app.data.playback.AudiobookChapterReader
import elovaire.music.droidbeauty.app.data.playback.NowPlayingPlayback
import elovaire.music.droidbeauty.app.data.settings.FavoritesStore
import elovaire.music.droidbeauty.app.data.settings.PlaylistStore
import elovaire.music.droidbeauty.app.data.update.UpdateController
import elovaire.music.droidbeauty.app.ui.motion.MotionTransitions
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions
import kotlinx.coroutines.flow.Flow

internal class RootComposition(
    val context: Context,
    val navController: NavHostController,
    val motionTransitions: MotionTransitions,
    val viewModelFactory: ElovaireViewModelFactory,
    val rootViewModel: RootViewModel,
    val permissionController: RootPermissionController,
    val deleteController: RootDeleteController,
    val searchViewModel: SearchViewModel,
    val nowPlayingViewModel: NowPlayingViewModel,
    val interactionWorkPolicy: AppBackgroundWorkPolicy,
    val openNowPlayingCommands: Flow<Unit>,
    val appShortcutCommands: Flow<AppShortcutCommand>,
    val playback: NowPlayingPlayback,
    val playlistStore: PlaylistStore,
    val favoritesStore: FavoritesStore,
    val libraryActionDependencies: LibraryActionDependencies,
    val settingsActionDependencies: SettingsActionDependencies,
    val updateController: UpdateController,
    val artistImageRepository: ArtistImageRepository,
    val audiobookChapterReader: AudiobookChapterReader,
    val audiobookDescriptionReader: AudiobookDescriptionReader,
)

@Composable
internal fun rememberRootComposition(container: AppContainer): RootComposition {
    val context = LocalContext.current
    val navController = rememberNavController()
    val motionTransitions = rememberMotionTransitions()
    val viewModelFactory = remember(container) { ElovaireViewModelFactory(container.viewModelDependencies) }
    val rootViewModel: RootViewModel = viewModel(factory = viewModelFactory)
    val libraryState by rootViewModel.libraryState.collectAsStateWithLifecycle()
    val permissionController = rememberRootPermissionController(
        libraryActionDependencies = container.libraryActionDependencies,
        libraryState = libraryState,
    )
    val deleteController = rememberRootDeleteController(container.rootDeleteDependencies.deleteHandler)
    val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
    val nowPlayingViewModel: NowPlayingViewModel = viewModel(factory = viewModelFactory)
    return RootComposition(
        context = context,
        navController = navController,
        motionTransitions = motionTransitions,
        viewModelFactory = viewModelFactory,
        rootViewModel = rootViewModel,
        permissionController = permissionController,
        deleteController = deleteController,
        searchViewModel = searchViewModel,
        nowPlayingViewModel = nowPlayingViewModel,
        interactionWorkPolicy = container.interactionWorkPolicy,
        openNowPlayingCommands = container.openNowPlayingCommands,
        appShortcutCommands = container.appShortcutCommands,
        playback = container.playbackManager,
        playlistStore = container.playlistStore,
        favoritesStore = container.favoritesStore,
        libraryActionDependencies = container.libraryActionDependencies,
        settingsActionDependencies = container.settingsActionDependencies,
        updateController = container.updateController,
        artistImageRepository = container.artistImageRepository,
        audiobookChapterReader = container.audiobookChapterReader,
        audiobookDescriptionReader = container.audiobookDescriptionReader,
    )
}
