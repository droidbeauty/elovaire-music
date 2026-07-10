package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable

/** Owns the ready-state shell after the root permission gate has granted media access. */
@Composable
internal fun ElovaireRootReadyHost(
    composition: RootComposition,
    resetHomeScrollOnColdStart: Boolean,
    adaptiveInfo: ElovaireAdaptiveInfo,
) {
    val container = composition.container
    val appState = composition.appState
    val derivedState = composition.derivedState
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
        isPlaybackActuallyPlaying = isPlaybackActuallyPlaying,
    )

    val actionRuntime = rememberRootActionRuntime(
        context = composition.context,
        container = container,
        navController = composition.navController,
        appState = appState,
        derivedState = derivedState,
        albumCollectionLayoutMode = composition.albumCollectionLayoutMode,
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
                searchViewModel = composition.searchViewModel,
                viewModelFactory = composition.viewModelFactory,
                changelogReleases = composition.changelogReleases,
                modifier = modifier,
            )
        },
        chromeHost = { layout ->
            RootChromeSlot(
                layout = layout,
                sharedTopBarSpec = uiRuntime.sharedTopBarSpec,
                chromeVisibility = uiRuntime.chromeVisibility,
                playbackState = playbackState,
                nowPlayingViewModel = composition.nowPlayingViewModel,
                adaptiveInfo = adaptiveInfo,
                activeBottomRoute = uiRuntime.routeObservation.activeBottomRoute,
                currentRoute = currentRoute,
                navigationState = navigationState,
                topLevelDestinations = DefaultTopLevelDestinations,
                motionTransitions = composition.motionTransitions,
                onOpenPlayer = playerLayerController::requestOpen,
            )
        },
        overlayHost = { layout ->
            RootOverlaySlot(
                layout = layout,
                overlayState = uiRuntime.overlayState,
                topBarMenuActions = uiRuntime.topBarMenuActions,
                changelogReleases = composition.changelogReleases,
                playlistActions = actionRuntime.playlistActions,
                chromeVisibility = uiRuntime.chromeVisibility,
                currentRoute = currentRoute,
                appState = appState,
                container = container,
                permissionController = composition.permissionController,
                motionTransitions = composition.motionTransitions,
            )
        },
        playerLayerHost = {
            RootPlayerLayerSlot(
                container = container,
                chromeVisibility = uiRuntime.chromeVisibility,
                playerLayerState = playerLayerController.state,
                playerLayerController = playerLayerController,
                nowPlayingViewModel = composition.nowPlayingViewModel,
                songsById = derivedState.songsById,
                playbackState = playbackState,
                appState = appState,
                playlistActions = actionRuntime.playlistActions,
                openCurrentPlayingAlbum = uiRuntime.openCurrentPlayingAlbum,
                navController = composition.navController,
            )
        },
    )
}
