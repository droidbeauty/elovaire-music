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
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRepository
import elovaire.music.droidbeauty.app.ui.motion.MotionTransitions
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions

internal data class RootComposition(
    val container: AppContainer,
    val context: Context,
    val navController: NavHostController,
    val motionTransitions: MotionTransitions,
    val viewModelFactory: ElovaireViewModelFactory,
    val appState: RootAppState,
    val derivedState: RootLibraryDerivedState,
    val permissionController: RootPermissionController,
    val deleteController: RootDeleteController,
    val albumCollectionLayoutMode: AlbumLayoutMode,
    val changelogReleases: List<ChangelogRelease>,
    val searchViewModel: SearchViewModel,
    val nowPlayingViewModel: NowPlayingViewModel,
)

@Composable
internal fun rememberRootComposition(container: AppContainer): RootComposition {
    val context = LocalContext.current
    val navController = rememberNavController()
    val motionTransitions = rememberMotionTransitions()
    val viewModelFactory = remember(container) { ElovaireViewModelFactory(container.viewModelDependencies) }
    val rootViewModel: RootViewModel = viewModel(factory = viewModelFactory)
    val appState by rootViewModel.appState.collectAsStateWithLifecycle()
    val derivedState = rememberRootLibraryDerivedState(
        library = appState.library,
        playback = appState.playback,
        playlists = appState.playlists,
        songPlayCounts = appState.songPlayCounts,
    )
    val permissionController = rememberRootPermissionController(
        container = container,
        libraryState = appState.library,
    )
    val deleteController = rememberRootDeleteController(container)
    val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
    val nowPlayingViewModel: NowPlayingViewModel = viewModel(factory = viewModelFactory)
    return RootComposition(
        container = container,
        context = context,
        navController = navController,
        motionTransitions = motionTransitions,
        viewModelFactory = viewModelFactory,
        appState = appState,
        derivedState = derivedState,
        permissionController = permissionController,
        deleteController = deleteController,
        albumCollectionLayoutMode = appState.albumCollectionLayoutModeName.toAlbumLayoutMode(),
        changelogReleases = rememberChangelogReleases(context),
        searchViewModel = searchViewModel,
        nowPlayingViewModel = nowPlayingViewModel,
    )
}

@Composable
internal fun rememberChangelogReleases(context: Context): List<ChangelogRelease> {
    return remember(context) { ChangelogRepository(context).loadReleases() }
}
