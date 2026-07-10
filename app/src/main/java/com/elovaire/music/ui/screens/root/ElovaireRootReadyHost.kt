package elovaire.music.droidbeauty.app.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation.NavHostController
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease
import elovaire.music.droidbeauty.app.ui.motion.MotionTransitions

/** Owns the ready-state shell after the root permission gate has granted media access. */
@Composable
internal fun ElovaireRootReadyHost(
    container: AppContainer,
    resetHomeScrollOnColdStart: Boolean,
    adaptiveInfo: ElovaireAdaptiveInfo,
    navController: NavHostController,
    rootMotionTransitions: MotionTransitions,
    context: Context,
    viewModelFactory: ElovaireViewModelFactory,
    appState: RootAppState,
    derivedState: RootLibraryDerivedState,
    permissionController: RootPermissionController,
    deleteController: RootDeleteController,
    albumCollectionLayoutMode: AlbumLayoutMode,
    changelogReleases: List<ChangelogRelease>,
    searchViewModel: SearchViewModel,
    nowPlayingViewModel: NowPlayingViewModel,
) {
    val libraryState = appState.library
    val playbackState = appState.playback
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
        albumsById = derivedState.albumsById,
        isPlaybackActuallyPlaying = isPlaybackActuallyPlaying,
    )
    val navigationState = uiRuntime.navigationState
    val currentRoute = uiRuntime.routeObservation.route
    val playerLayerController = uiRuntime.playerLayerController
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
                playerLayerState = playerLayerController.state,
                playerLayerController = playerLayerController,
                nowPlayingViewModel = nowPlayingViewModel,
                songsById = derivedState.songsById,
                playbackState = playbackState,
                appState = appState,
                playlistActions = actionRuntime.playlistActions,
                openCurrentPlayingAlbum = uiRuntime.openCurrentPlayingAlbum,
                navController = navController,
            )
        },
    )
}
