package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
        ElovaireRootReadyContent(
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

@Composable
private fun ElovaireRootReadyContent(
    container: AppContainer,
    resetHomeScrollOnColdStart: Boolean,
    adaptiveInfo: ElovaireAdaptiveInfo,
    navController: androidx.navigation.NavHostController,
    rootMotionTransitions: elovaire.music.droidbeauty.app.ui.motion.MotionTransitions,
    context: android.content.Context,
    viewModelFactory: ElovaireViewModelFactory,
    appState: RootAppState,
    derivedState: RootLibraryDerivedState,
    permissionController: RootPermissionController,
    deleteController: RootDeleteController,
    albumCollectionLayoutMode: AlbumLayoutMode,
    changelogReleases: List<elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease>,
    searchViewModel: SearchViewModel,
    nowPlayingViewModel: NowPlayingViewModel,
) {
    val libraryState = appState.library
    val playbackState = appState.playback
    val songsById = derivedState.songsById
    val albumsById = derivedState.albumsById
    val isPlaybackActuallyPlaying = playbackState.isPlaying && playbackState.currentSong != null
    RootNotificationPermissionEffect(
        isPlaybackActuallyPlaying,
        permissionController.state,
        permissionController::requestNotificationPermission,
    )

    val uiRuntime = rememberRootUiRuntime(
        navController = navController,
        routeState = appState,
        libraryState = libraryState,
        playbackState = playbackState,
        albumsById = albumsById,
        isPlaybackActuallyPlaying = isPlaybackActuallyPlaying,
    )
    val navigationState = uiRuntime.navigationState
    val currentRoute = uiRuntime.routeObservation.route
    val playerLayerController = uiRuntime.playerLayerController
    val playerLayerState = playerLayerController.state
    val currentPlayerLayerController by rememberUpdatedState(playerLayerController)
    LaunchedEffect(container) {
        container.openNowPlayingCommands.collect {
            currentPlayerLayerController.requestOpen(null)
        }
    }
    RootSystemBarEffect(
        darkTheme = uiRuntime.shellInputs.darkTheme,
        showPlayerOverlay = uiRuntime.chromeVisibility.showPlayerOverlay,
        playerContentColor = uiRuntime.shellInputs.playerAdaptivePalette.contentColor,
    )

    val actionRuntime = rememberRootActionRuntime(
        context = context,
        container = container,
        navController = navController,
        appState = appState,
        derivedState = derivedState,
        albumCollectionLayoutMode = albumCollectionLayoutMode,
        resetHomeScrollOnColdStart = resetHomeScrollOnColdStart,
        permissionController = permissionController,
        deleteController = deleteController,
        uiRuntime = uiRuntime,
    )

    RootShellHost(
        overscrollFactory = uiRuntime.shellInputs.overscrollFactory,
        songMenuActions = actionRuntime.songMenuActions,
        chromeHazeState = uiRuntime.shellInputs.chromeHazeState,
        sharedBackIconPainter = uiRuntime.shellInputs.sharedBackIconPainter,
        sharedTopMenuIconPainter = uiRuntime.shellInputs.sharedTopMenuIconPainter,
        appLanguage = appState.appLanguage,
        adaptiveInfo = adaptiveInfo,
        chromeVisibility = uiRuntime.chromeVisibility,
        sharedTopBarController = uiRuntime.shellInputs.sharedTopBarController,
        navHostBlur = uiRuntime.shellInputs.navHostBlur,
        navHostScrimAlpha = uiRuntime.shellInputs.navHostScrimAlpha,
        routeHost = { routePadding, modifier ->
            RootRouteGraph(
                navState = navigationState,
                routeState = actionRuntime.routeState,
                routeActions = actionRuntime.routeActions,
                padding = routePadding,
                searchViewModel = searchViewModel,
                viewModelFactory = viewModelFactory,
                changelogReleases = changelogReleases,
                modifier = modifier,
            )
        },
        chromeHost = { layout ->
            RootChromeSlot(
                layout = layout,
                sharedTopBarSpec = uiRuntime.sharedTopBarSpec,
                chromeVisibility = uiRuntime.chromeVisibility,
                playbackState = playbackState,
                nowPlayingViewModel = nowPlayingViewModel,
                adaptiveInfo = adaptiveInfo,
                activeBottomRoute = uiRuntime.routeObservation.activeBottomRoute,
                currentRoute = currentRoute,
                navigationState = navigationState,
                topLevelDestinations = DefaultTopLevelDestinations,
                motionTransitions = rootMotionTransitions,
                onOpenPlayer = playerLayerController::requestOpen,
            )
        },
        overlayHost = { layout ->
            RootOverlaySlot(
                layout = layout,
                overlayState = uiRuntime.overlayState,
                topBarMenuActions = uiRuntime.topBarMenuActions,
                changelogReleases = changelogReleases,
                playlistActions = actionRuntime.playlistActions,
                chromeVisibility = uiRuntime.chromeVisibility,
                currentRoute = currentRoute,
                appState = appState,
                container = container,
                permissionController = permissionController,
                motionTransitions = rootMotionTransitions,
            )
        },
        playerLayerHost = {
            RootPlayerLayerSlot(
                container = container,
                chromeVisibility = uiRuntime.chromeVisibility,
                playerLayerState = playerLayerState,
                playerLayerController = playerLayerController,
                nowPlayingViewModel = nowPlayingViewModel,
                songsById = songsById,
                playbackState = playbackState,
                appState = appState,
                playlistActions = actionRuntime.playlistActions,
                openCurrentPlayingAlbum = uiRuntime.openCurrentPlayingAlbum,
                navController = navController,
            )
        },
    )
}
