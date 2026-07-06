package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.EnterTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.droidbeauty.app.ui.motion.MotionTransitions

@Composable
internal fun BoxScope.RootChromeHost(
    sharedTopBarSpec: SharedTopBarSpec?,
    showSharedTopBarBackdrop: Boolean,
    sharedTopBarHeight: Dp,
    canHostCompactNowPlaying: Boolean,
    playbackState: PlaybackUiState,
    nowPlayingViewModel: NowPlayingViewModel,
    showGlobalNowPlaying: Boolean,
    reenteringFromPlayer: Boolean,
    showBottomNavigation: Boolean,
    adaptiveInfo: ElovaireAdaptiveInfo,
    bottomNavHeight: Dp,
    activeBottomRoute: String,
    currentRoute: String?,
    navigationState: RootNavigationState,
    topLevelDestinations: List<TopLevelDestination>,
    motionTransitions: MotionTransitions,
    onOpenPlayer: (NowPlayingTransitionSnapshot?) -> Unit,
) {
    val showNavigationRail = showBottomNavigation && adaptiveInfo.useNavigationRail
    val showCompactBottomNavigation = showBottomNavigation && !adaptiveInfo.useNavigationRail
    if (showSharedTopBarBackdrop && sharedTopBarSpec != null) {
        FrostedTopBarBackground(
            darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(sharedTopBarHeight)
                .zIndex(RootLayerZ.ChromeBackdrop),
        )
    }
    if (sharedTopBarSpec != null) {
        CompositionLocalProvider(LocalRenderSharedTopBarContent provides true) {
            SharedTopBarOverlay(
                spec = sharedTopBarSpec,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .zIndex(RootLayerZ.SharedTopBar),
            )
        }
    }
    if (canHostCompactNowPlaying) {
        playbackState.currentSong?.let { song: Song ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(RootLayerZ.CompactPlayer)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (showCompactBottomNavigation) bottomNavHeight + 8.dp else navigationBarInsetDp() + 10.dp,
                    ),
                contentAlignment = Alignment.BottomCenter,
            ) {
                CompactNowPlayingDockHost(
                    viewModel = nowPlayingViewModel,
                    song = song,
                    transportShowsPause = playbackState.transportShowsPause,
                    visible = showGlobalNowPlaying,
                    suppressEnterAnimation = reenteringFromPlayer,
                    onOpenPlayer = onOpenPlayer,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
    ElovaireAnimatedVisibility(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .zIndex(RootLayerZ.BottomNavigation)
            .fillMaxWidth(),
        visible = showCompactBottomNavigation,
        enter = if (reenteringFromPlayer) {
            EnterTransition.None
        } else {
            motionTransitions.bottomBarEnter()
        },
        exit = motionTransitions.bottomBarExit(),
        label = "BottomNavigationVisibility",
    ) {
        BottomNavigationBar(
            currentRoute = activeBottomRoute,
            suppressEnterAnimation = reenteringFromPlayer,
            destinations = topLevelDestinations,
            onNavigate = { route ->
                navigationState.navigateBottomTab(
                    route = route,
                    activeBottomRoute = activeBottomRoute,
                    currentRoute = currentRoute,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (showNavigationRail) {
        NavigationRail(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .zIndex(RootLayerZ.BottomNavigation)
                .fillMaxHeight()
                .width(88.dp),
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
        ) {
            topLevelDestinations.forEach { destination ->
                NavigationRailItem(
                    selected = activeBottomRoute == destination.route,
                    onClick = {
                        navigationState.navigateBottomTab(
                            route = destination.route,
                            activeBottomRoute = activeBottomRoute,
                            currentRoute = currentRoute,
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = destination.iconResId),
                            contentDescription = destination.contentDescription,
                        )
                    },
                    alwaysShowLabel = false,
                )
            }
        }
    }
}
