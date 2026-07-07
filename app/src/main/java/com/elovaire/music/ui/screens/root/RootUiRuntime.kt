package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.rememberHazeState
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkGradient
import elovaire.music.droidbeauty.app.ui.theme.rememberElovaireOverscrollFactory

internal data class RootUiRuntime(
    val navigationState: RootNavigationState,
    val routeObservation: RootRouteObservation,
    val playerLayerController: RootPlayerLayerController,
    val searchChromeState: RootSearchChromeState,
    val chromeVisibility: RootChromeVisibility,
    val shellInputs: RootShellInputs,
    val overlayState: RootOverlayStateHolder,
    val topBarMenuActions: RootTopBarMenuActions,
    val sharedTopBarSpec: SharedTopBarSpec?,
    val openAlbum: (Album, ExpandOrigin, AlbumOpenSource) -> Unit,
    val openCurrentPlayingAlbum: (Long) -> Unit,
)

internal data class RootShellInputs(
    val overscrollFactory: androidx.compose.foundation.OverscrollFactory,
    val chromeHazeState: dev.chrisbanes.haze.HazeState,
    val sharedBackIconPainter: androidx.compose.ui.graphics.painter.Painter,
    val sharedTopMenuIconPainter: androidx.compose.ui.graphics.painter.Painter,
    val sharedTopBarController: SharedTopBarController,
    val navHostBlur: androidx.compose.ui.unit.Dp,
    val navHostScrimAlpha: Float,
    val darkTheme: Boolean,
    val playerAdaptivePalette: PlayerAdaptivePalette,
)

@Composable
internal fun rememberRootUiRuntime(
    navController: NavHostController,
    routeState: RootAppState,
    libraryState: elovaire.music.droidbeauty.app.data.library.LibraryUiState,
    playbackState: elovaire.music.droidbeauty.app.data.playback.PlaybackUiState,
    albumsById: Map<Long, Album>,
    isPlaybackActuallyPlaying: Boolean,
): RootUiRuntime {
    val navigationState = rememberRootNavigationState(navController)
    val routeObservation = rememberRootRouteObservation(
        navController = navController,
        navigationState = navigationState,
        libraryState = libraryState,
        isPlaybackActuallyPlaying = isPlaybackActuallyPlaying,
    )
    val currentSongPresent = playbackState.currentSong != null
    val playerLayerController = rememberRootPlayerLayerController(
        currentSongId = playbackState.currentSong?.id,
        currentSongPresent = currentSongPresent,
    )
    val searchChromeState = rememberRootSearchChromeState()
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val chromeVisibility = rootChromeVisibility(
        currentRoute = routeObservation.route,
        keyboardVisible = keyboardVisible,
        searchQueryActive = searchChromeState.isQueryActive,
        currentSongPresent = currentSongPresent,
        playerLayerState = playerLayerController.state,
    )
    val sharedTopBarController = remember { SharedTopBarController() }
    val overlayState = rememberRootOverlayStateHolder(routeObservation.route)
    val openAlbum: (Album, ExpandOrigin, AlbumOpenSource) -> Unit = { album, origin, source ->
        navigationState.openAlbum(album, origin, source)
    }
    val shellInputs = rememberRootShellInputs(
        playbackState = playbackState,
        darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f,
        sharedTopBarController = sharedTopBarController,
    )
    val openCurrentPlayingAlbum = rememberOpenCurrentPlayingAlbum(
        navController = navController,
        navigationState = navigationState,
        currentRoute = routeObservation.route,
        currentAlbumRouteId = routeObservation.currentAlbumRouteId,
        albumsById = albumsById,
        playerLayerController = playerLayerController,
        openAlbum = openAlbum,
    )
    return RootUiRuntime(
        navigationState = navigationState,
        routeObservation = routeObservation,
        playerLayerController = playerLayerController,
        searchChromeState = searchChromeState,
        chromeVisibility = chromeVisibility,
        shellInputs = shellInputs,
        overlayState = overlayState,
        topBarMenuActions = rememberRootTopBarMenuActions(navController, overlayState),
        sharedTopBarSpec = sharedTopBarController.registration?.spec
            ?: rootSharedTopBarSpec(
                currentRoute = routeObservation.route,
                showTopLevelChrome = chromeVisibility.showTopLevelChrome,
                language = routeState.appLanguage,
                onRequestCreatePlaylist = overlayState::requestCreatePlaylist,
                onOpenMenu = overlayState::openTopBarMenu,
            ),
        openAlbum = openAlbum,
        openCurrentPlayingAlbum = openCurrentPlayingAlbum,
    )
}

@Composable
private fun rememberRootShellInputs(
    playbackState: elovaire.music.droidbeauty.app.data.playback.PlaybackUiState,
    darkTheme: Boolean,
    sharedTopBarController: SharedTopBarController,
): RootShellInputs {
    val appBackground = MaterialTheme.colorScheme.background
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
    return RootShellInputs(
        overscrollFactory = rememberElovaireOverscrollFactory(),
        chromeHazeState = rememberHazeState(),
        sharedBackIconPainter = painterResource(id = R.drawable.ic_lucide_chevron_left),
        sharedTopMenuIconPainter = painterResource(id = R.drawable.ic_lucide_menu),
        sharedTopBarController = sharedTopBarController,
        navHostBlur = 0.dp,
        navHostScrimAlpha = 0f,
        darkTheme = darkTheme,
        playerAdaptivePalette = playerAdaptivePalette,
    )
}
