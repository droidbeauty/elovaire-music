package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Owns the ready-state shell after the root permission gate has granted media access. */
@Composable
internal fun ElovaireRootReadyHost(
    composition: RootComposition,
    resetHomeScrollOnColdStart: Boolean,
) {
    val container = composition.container
    val appState by composition.rootViewModel.appState.collectAsStateWithLifecycle()
    val derivedState = rememberRootLibraryDerivedState(
        library = appState.library,
        playback = appState.playback,
        playlists = appState.playlists,
        songPlayCounts = appState.songPlayCounts,
    )
    val albumCollectionLayoutMode = appState.albumCollectionLayoutModeName.toAlbumLayoutMode()
    val libraryState = appState.library
    val playbackState = appState.playback
    val isPlaybackActuallyPlaying = playbackState.isPlaying && playbackState.currentSong != null
    val uiRuntime = rememberRootUiRuntime(
        navController = composition.navController,
        routeState = appState,
        libraryState = libraryState,
        playbackState = playbackState,
        albumsById = derivedState.albumsById,
        isPlaybackActuallyPlaying = isPlaybackActuallyPlaying,
    )
    val navigationState = uiRuntime.navigationState
    val currentRoute = uiRuntime.routeObservation.route
    val playerLayerController = uiRuntime.playerLayerController
    RootEffectsHost(
        composition = composition,
        uiRuntime = uiRuntime,
        derivedState = derivedState,
    )

    val actionRuntime = rememberRootActionRuntime(
        context = composition.context,
        container = container,
        navController = composition.navController,
        appState = appState,
        derivedState = derivedState,
        albumCollectionLayoutMode = albumCollectionLayoutMode,
        resetHomeScrollOnColdStart = resetHomeScrollOnColdStart,
        permissionController = composition.permissionController,
        deleteController = composition.deleteController,
        uiRuntime = uiRuntime,
    )

    RootShellHost(
        overscrollFactory = uiRuntime.shellInputs.overscrollFactory,
        songMenuActions = actionRuntime.songMenuActions,
        chromeHazeState = uiRuntime.shellInputs.chromeHazeState,
        sharedBackIconPainter = uiRuntime.shellInputs.sharedBackIconPainter,
        sharedTopMenuIconPainter = uiRuntime.shellInputs.sharedTopMenuIconPainter,
        appLanguage = appState.appLanguage,
        chromeVisibility = uiRuntime.chromeVisibility,
        sharedTopBarController = uiRuntime.shellInputs.sharedTopBarController,
        navHostBlur = uiRuntime.shellInputs.navHostBlur,
        navHostScrimAlpha = uiRuntime.shellInputs.navHostScrimAlpha,
        routeHost = { routePadding, modifier ->
            RootRouteGraph(
                navState = navigationState,
                motionTransitions = composition.motionTransitions,
                routeState = actionRuntime.routeState,
                routeActions = actionRuntime.routeActions,
                padding = routePadding,
                searchViewModel = composition.searchViewModel,
                viewModelFactory = composition.viewModelFactory,
                artistImageRepository = container.artistImageRepository,
                audiobookChapterReader = container.audiobookChapterReader,
                modifier = modifier,
            )
        },
        chromeHost = { layout ->
            RootChromeSlot(
                layout = layout,
                sharedTopBarSpec = uiRuntime.sharedTopBarSpec,
                chromeVisibility = uiRuntime.chromeVisibility,
                playbackState = playbackState,
                nowPlayingBarStyle = appState.nowPlayingBarStyle,
                nowPlayingViewModel = composition.nowPlayingViewModel,
                activeBottomRoute = uiRuntime.routeObservation.activeBottomRoute,
                currentRoute = currentRoute,
                navigationState = navigationState,
                topLevelDestinations = DefaultTopLevelDestinations,
                motionTransitions = composition.motionTransitions,
                onOpenPlayer = playerLayerController::requestOpen,
            )
        },
        overlayHost = {
            RootOverlaySlot(
                overlayState = uiRuntime.overlayState,
                topBarMenuActions = uiRuntime.topBarMenuActions,
                playlistActions = actionRuntime.playlistActions,
                permissionController = composition.permissionController,
                updateController = container.updateController,
                motionTransitions = composition.motionTransitions,
            )
        },
        playerLayerHost = {
            RootPlayerLayerSlot(
                playback = container.playbackManager,
                chromeVisibility = uiRuntime.chromeVisibility,
                playerLayerState = playerLayerController.state,
                playerLayerController = playerLayerController,
                nowPlayingViewModel = composition.nowPlayingViewModel,
                songsById = derivedState.songsById,
                playbackState = playbackState,
                audiobookSettings = appState.audiobookSettings,
                appState = appState,
                playlistActions = actionRuntime.playlistActions,
                openCurrentPlayingAlbum = uiRuntime.openCurrentPlayingAlbum,
                navigationState = navigationState,
            )
        },
    )
}
