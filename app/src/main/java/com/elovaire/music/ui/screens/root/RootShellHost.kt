package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.OverscrollFactory
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeState
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.motion.MotionTransitions

@Composable
internal fun RootShellHost(
    overscrollFactory: OverscrollFactory,
    songMenuActions: SongMenuActions,
    chromeHazeState: HazeState,
    sharedBackIconPainter: Painter,
    sharedTopMenuIconPainter: Painter,
    appLanguage: AppLanguage,
    adaptiveInfo: ElovaireAdaptiveInfo,
    chromeVisibility: RootChromeVisibility,
    sharedTopBarController: SharedTopBarController,
    navHostBlur: Dp,
    navHostScrimAlpha: Float,
    routeHost: @Composable (RootRoutePadding, Modifier) -> Unit,
    chromeHost: @Composable BoxScope.(RootChromeLayout) -> Unit,
    overlayHost: @Composable BoxScope.(RootChromeLayout) -> Unit,
    playerLayerHost: @Composable BoxScope.() -> Unit,
) {
    ElovaireRootShell(
        overscrollFactory = overscrollFactory,
        songMenuActions = songMenuActions,
        chromeHazeState = chromeHazeState,
        sharedBackIconPainter = sharedBackIconPainter,
        sharedTopMenuIconPainter = sharedTopMenuIconPainter,
        appLanguage = appLanguage,
        adaptiveInfo = adaptiveInfo,
        chromeVisibility = chromeVisibility,
        sharedTopBarController = sharedTopBarController,
        navHostBlur = navHostBlur,
        navHostScrimAlpha = navHostScrimAlpha,
        routeHost = routeHost,
        chromeHost = chromeHost,
        overlayHost = overlayHost,
        playerLayerHost = playerLayerHost,
    )
}

@Composable
internal fun BoxScope.RootChromeSlot(
    layout: RootChromeLayout,
    sharedTopBarSpec: SharedTopBarSpec?,
    chromeVisibility: RootChromeVisibility,
    playbackState: PlaybackUiState,
    nowPlayingViewModel: NowPlayingViewModel,
    adaptiveInfo: ElovaireAdaptiveInfo,
    activeBottomRoute: String,
    currentRoute: String?,
    navigationState: RootNavigationState,
    topLevelDestinations: List<TopLevelDestination>,
    motionTransitions: MotionTransitions,
    onOpenPlayer: (NowPlayingTransitionSnapshot?) -> Unit,
) {
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
        motionTransitions = motionTransitions,
        onOpenPlayer = onOpenPlayer,
    )
}

@Composable
internal fun BoxScope.RootOverlaySlot(
    layout: RootChromeLayout,
    overlayState: RootOverlayStateHolder,
    topBarMenuActions: RootTopBarMenuActions,
    changelogReleases: List<ChangelogRelease>,
    playlistActions: RootPlaylistActions,
    chromeVisibility: RootChromeVisibility,
    currentRoute: String?,
    appState: RootAppState,
    container: AppContainer,
    permissionController: RootPermissionController,
    motionTransitions: MotionTransitions,
) {
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
        motionTransitions = motionTransitions,
    )
}

@Composable
internal fun BoxScope.RootPlayerLayerSlot(
    container: AppContainer,
    chromeVisibility: RootChromeVisibility,
    playerLayerState: PlayerLayerState,
    playerLayerController: RootPlayerLayerController,
    nowPlayingViewModel: NowPlayingViewModel,
    songsById: Map<Long, Song>,
    playbackState: PlaybackUiState,
    appState: RootAppState,
    playlistActions: RootPlaylistActions,
    openCurrentPlayingAlbum: (Long) -> Unit,
    navController: NavHostController,
) {
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
}
