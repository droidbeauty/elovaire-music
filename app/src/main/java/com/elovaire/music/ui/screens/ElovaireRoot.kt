package elovaire.music.droidbeauty.app.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.rememberHazeState
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkGradient
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions
import elovaire.music.droidbeauty.app.ui.performance.PerformanceState
import elovaire.music.droidbeauty.app.ui.theme.elovaireResolvedColorScheme
import elovaire.music.droidbeauty.app.ui.theme.rememberElovaireOverscrollFactory

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
    val libraryState = appState.library
    val playbackState = appState.playback
    val songsById = derivedState.songsById
    val songsByAlbumId = derivedState.songsByAlbumId
    val albumsById = derivedState.albumsById
    val playlistsById = derivedState.playlistsById
    val recentlyAddedAlbums = derivedState.recentlyAddedAlbums
    val recentAlbums = derivedState.recentAlbums
    val favoriteAlbums = derivedState.favoriteAlbums
    val lastPlayedAlbum = derivedState.lastPlayedAlbum
    val lastPlayedPlaylist = derivedState.lastPlayedPlaylist

    if (!permissionController.state.hasAudioPermission) {
        PerformanceState("screen", "permission")
        FirstLaunchPermissionLoadingScreen(
            showLoading = false,
            onRequestPermission = permissionController::requestAudioPermission,
        )
        return
    }

    val isPlaybackActuallyPlaying = playbackState.isPlaying && playbackState.currentSong != null
    LaunchedEffect(
        isPlaybackActuallyPlaying,
        permissionController.state.hasNotificationPermission,
    ) {
        if (
            isPlaybackActuallyPlaying &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !permissionController.state.hasNotificationPermission
        ) {
            permissionController.requestNotificationPermission()
        }
    }

    val topLevelDestinations = DefaultTopLevelDestinations

    val navigationState = rememberRootNavigationState(navController)
    val routeObservation = rememberRootRouteObservation(
        navController = navController,
        navigationState = navigationState,
        libraryState = libraryState,
        isPlaybackActuallyPlaying = isPlaybackActuallyPlaying,
    )
    val currentRoute = routeObservation.route
    val currentAlbumRouteId = routeObservation.currentAlbumRouteId
    val currentSongPresent = playbackState.currentSong != null
    val playerLayerController = rememberRootPlayerLayerController(
        currentSongId = playbackState.currentSong?.id,
        currentSongPresent = currentSongPresent,
    )
    val playerLayerState = playerLayerController.state
    val searchChromeState = rememberRootSearchChromeState()
    val openAlbum: (Album, ExpandOrigin, AlbumOpenSource) -> Unit = { album, origin, source ->
        navigationState.openAlbum(album, origin, source)
    }
    val activeBottomRoute = routeObservation.activeBottomRoute
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val chromeVisibility = rootChromeVisibility(
        currentRoute = currentRoute,
        keyboardVisible = keyboardVisible,
        searchQueryActive = searchChromeState.isQueryActive,
        currentSongPresent = currentSongPresent,
        playerLayerState = playerLayerState,
    )
    val overscrollFactory = rememberElovaireOverscrollFactory()
    val navHostBlur = 0.dp
    val navHostScrimAlpha = 0f
    val appBackground = MaterialTheme.colorScheme.background
    val darkTheme = appBackground.luminance() < 0.5f
    val chromeHazeState = rememberHazeState()
    val sharedTopBarController = remember { SharedTopBarController() }
    val sharedBackIconPainter = painterResource(id = R.drawable.ic_lucide_chevron_left)
    val sharedTopMenuIconPainter = painterResource(id = R.drawable.ic_lucide_menu)
    val overlayState = rememberRootOverlayStateHolder(currentRoute)
    val playerArtworkGradient = rememberArtworkGradient(playbackState.currentSong?.artUri).value
    val playerAdaptivePalette = remember(
        playbackState.currentSong?.id,
        playerArtworkGradient,
        darkTheme,
        appBackground,
    ) {
        buildPlayerAdaptivePalette(
            gradient = playerArtworkGradient,
            appBackground = appBackground,
            darkTheme = darkTheme,
        )
    }
    val openCurrentPlayingAlbum: (Long) -> Unit = { albumId ->
        val sameAlbumAlreadyVisible =
            currentRoute == "$ALBUM_ROUTE/{albumId}" && currentAlbumRouteId == albumId
        playerLayerController.hide(false)
        if (!sameAlbumAlreadyVisible) {
            albumsById[albumId]?.let { album ->
                openAlbum(album, ExpandOrigin(), AlbumOpenSource.Player)
            } ?: run {
                navigationState.detailExpandOrigin = ExpandOrigin()
                navigationState.detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
                navController.navigate(Routes.album(albumId))
            }
        }
    }
    val topBarMenuActions = rememberRootTopBarMenuActions(navController, overlayState)
    val sharedTopBarSpec = sharedTopBarController.registration?.spec
        ?: rootSharedTopBarSpec(
            currentRoute = currentRoute,
            showTopLevelChrome = chromeVisibility.showTopLevelChrome,
            language = appState.appLanguage,
            onRequestCreatePlaylist = overlayState::requestCreatePlaylist,
            onOpenMenu = overlayState::openTopBarMenu,
        )
    val currentPlayerLayerController by rememberUpdatedState(playerLayerController)
    LaunchedEffect(container) {
        container.openNowPlayingCommands.collect {
            currentPlayerLayerController.requestOpen(null)
        }
    }
    RootSystemBarEffect(
        darkTheme = darkTheme,
        showPlayerOverlay = chromeVisibility.showPlayerOverlay,
        playerContentColor = playerAdaptivePalette.contentColor,
    )

    val songMenuActions = rememberRootSongMenuActions(
        playlists = appState.playlists,
        songsById = songsById,
        albumsById = albumsById,
        playbackManager = container.playbackManager,
        preferenceStore = container.preferenceStore,
        onDeleteSongsFromDevice = deleteController::deleteSongsFromDevice,
        openAlbum = openAlbum,
        navigateToAlbumId = { albumId -> navController.navigate(Routes.album(albumId)) },
    )
    val playbackActions = rememberRootPlaybackActions(
        container = container,
        appLanguage = appState.appLanguage,
        songsByAlbumId = songsByAlbumId,
        albumsById = albumsById,
        openNowPlaying = playerLayerController::requestOpen,
    )
    val playlistActions = rememberRootPlaylistActions(container)
    val routeState = rootRouteStateOf(
        appState = appState,
        derivedState = derivedState,
        albumCollectionLayoutMode = albumCollectionLayoutMode,
        resetHomeScrollOnColdStart = resetHomeScrollOnColdStart,
        playFirstLaunchHomeReveal = permissionController.state.playFirstLaunchHomeReveal,
        searchFieldFocused = searchChromeState.isFieldFocused,
    )
    val routeActions = rememberRootRouteActions(
        context = context,
        container = container,
        navController = navController,
        navigationState = navigationState,
        playbackActions = playbackActions,
        playlistActions = playlistActions,
        deleteController = deleteController,
        onRequestCreatePlaylist = overlayState::requestCreatePlaylist,
        onInitialRevealFinished = permissionController::onInitialRevealFinished,
        onSearchFieldFocusedChange = searchChromeState::onFieldFocusedChanged,
        onSearchQueryActiveChanged = searchChromeState::onQueryActiveChanged,
        openAlbum = openAlbum,
    )

    ElovaireRootShell(
        overscrollFactory = overscrollFactory,
        songMenuActions = songMenuActions,
        chromeHazeState = chromeHazeState,
        sharedBackIconPainter = sharedBackIconPainter,
        sharedTopMenuIconPainter = sharedTopMenuIconPainter,
        appLanguage = appState.appLanguage,
        adaptiveInfo = adaptiveInfo,
        chromeVisibility = chromeVisibility,
        sharedTopBarController = sharedTopBarController,
        navHostBlur = navHostBlur,
        navHostScrimAlpha = navHostScrimAlpha,
        routeHost = { routePadding, modifier ->
            RootRouteGraph(
                navState = navigationState,
                routeState = routeState,
                routeActions = routeActions,
                padding = routePadding,
                searchViewModel = searchViewModel,
                viewModelFactory = viewModelFactory,
                changelogReleases = changelogReleases,
                modifier = modifier,
            )
        },
        chromeHost = { layout ->
            RootChromeHost(
                sharedTopBarSpec = sharedTopBarSpec,
                showSharedTopBarBackdrop = chromeVisibility.showSharedTopBarBackdrop,
                sharedTopBarHeight = layout.sharedTopBarHeight,
                canHostCompactNowPlaying = chromeVisibility.canHostCompactNowPlaying,
                playbackState = playbackState,
                nowPlayingViewModel = nowPlayingViewModel,
                showGlobalNowPlaying = chromeVisibility.showGlobalNowPlaying,
                reenteringFromPlayer = chromeVisibility.reenteringFromPlayer,
                showBottomNavigation = chromeVisibility.showBottomNavigation,
                adaptiveInfo = adaptiveInfo,
                bottomNavHeight = layout.bottomNavHeight,
                activeBottomRoute = activeBottomRoute,
                currentRoute = currentRoute,
                navigationState = navigationState,
                topLevelDestinations = topLevelDestinations,
                motionTransitions = rootMotionTransitions,
                onOpenPlayer = playerLayerController::requestOpen,
            )
        },
        overlayHost = { layout ->
            RootOverlayHost(
                showTopBarMenu = overlayState.showTopBarMenu,
                onDismissTopBarMenu = overlayState::dismissTopBarMenu,
                onOpenSettings = topBarMenuActions.openSettings,
                onOpenEqualizer = topBarMenuActions.openEqualizer,
                onOpenChangelog = topBarMenuActions.openChangelogSheet,
                onOpenAbout = topBarMenuActions.openAbout,
                showChangelogSheet = overlayState.showChangelogSheet,
                changelogReleases = changelogReleases,
                onDismissChangelogSheet = overlayState::dismissChangelogSheet,
                showPlaylistCreateDialog = overlayState.showPlaylistCreateDialog,
                onDismissPlaylistCreateDialog = overlayState::dismissPlaylistCreateDialog,
                onCreatePlaylist = playlistActions::createPlaylist,
                showTopLevelChrome = chromeVisibility.showTopLevelChrome,
                currentRoute = currentRoute,
                topBarHeight = layout.topBarHeight,
                appUpdateState = appState.appUpdateState,
                onDismissUpdate = container.appUpdateManager::dismissAvailableUpdate,
                onStartUpdate = container.appUpdateManager::startUpdate,
                permissionState = permissionController.state,
                onRequestAudioPermission = permissionController::requestAudioPermission,
                motionTransitions = rootMotionTransitions,
            )
        },
        playerLayerHost = {
            RootPlayerLayerHost(
                visible = chromeVisibility.showPlayerOverlay,
                playerLayerState = playerLayerState,
                onExitFinished = playerLayerController::clearTransitionSnapshot,
                onReturnToCompactFinished = playerLayerController::finishReturnToCompact,
                nowPlayingViewModel = nowPlayingViewModel,
                playbackManager = container.playbackManager,
                songsById = songsById,
                isCurrentSongFavorite = playbackState.currentSong?.id in appState.favoriteSongIds,
                playlists = appState.playlists.filterNot { it.isSystem },
                onBack = { playerLayerController.hide(true) },
                onOpenCurrentAlbum = openCurrentPlayingAlbum,
                onToggleFavorite = playlistActions::toggleFavorite,
                onAddCurrentSongToPlaylist = { playlistId, song ->
                    playlistActions.addSongsToPlaylist(playlistId, listOf(song.id))
                },
                onCreatePlaylist = playlistActions::createPlaylist,
                onOpenEqualizer = {
                    playerLayerController.hide(false)
                    navController.navigate(EQUALIZER_ROUTE)
                },
                transitionSnapshot = playerLayerController.transitionSnapshot,
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
}
@Composable
internal fun ForceDarkColorScheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = elovaireResolvedColorScheme(darkTheme = true),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}
