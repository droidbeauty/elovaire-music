package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions

@Composable
fun ElovaireRoot(
    container: AppContainer,
    resetHomeScrollOnColdStart: Boolean = false,
    adaptiveInfo: ElovaireAdaptiveInfo = elovaireAdaptiveInfo(width = 0.dp),
) {
    val navController = rememberNavController()
    val rootMotionTransitions = rememberMotionTransitions()
    val context = LocalContext.current
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
    RootUpdateTransientStatusEffect(
        enabled = true,
        transientStatus = appState.appUpdateState.transientStatus,
        clearTransientStatus = container.appUpdateManager::clearTransientStatus,
    )
    val albumCollectionLayoutMode = appState.albumCollectionLayoutModeName.toAlbumLayoutMode()
    val changelogReleases = rememberChangelogReleases(context)
    val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
    val nowPlayingViewModel: NowPlayingViewModel = viewModel(factory = viewModelFactory)

    RootPermissionGate(
        permissionState = permissionController.state,
        onRequestAudioPermission = permissionController::requestAudioPermission,
    ) {
        ElovaireRootReadyHost(
            container = container,
            resetHomeScrollOnColdStart = resetHomeScrollOnColdStart,
            adaptiveInfo = adaptiveInfo,
            navController = navController,
            rootMotionTransitions = rootMotionTransitions,
            context = context,
            viewModelFactory = viewModelFactory,
            appState = appState,
            derivedState = derivedState,
            permissionController = permissionController,
            deleteController = deleteController,
            albumCollectionLayoutMode = albumCollectionLayoutMode,
            changelogReleases = changelogReleases,
            searchViewModel = searchViewModel,
            nowPlayingViewModel = nowPlayingViewModel,
        )
    }
}
