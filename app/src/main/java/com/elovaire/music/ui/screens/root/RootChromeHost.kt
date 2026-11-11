package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.EnterTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.NowPlayingBarStyle
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.commonUiCopy
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.droidbeauty.app.ui.motion.MotionTransitions

@Composable
internal fun BoxScope.RootChromeHost(
    sharedTopBarSpec: SharedTopBarSpec?,
    showSharedTopBarBackdrop: Boolean,
    sharedTopBarHeight: Dp,
    canHostCompactNowPlaying: Boolean,
    playbackState: PlaybackUiState,
    nowPlayingBarStyle: NowPlayingBarStyle,
    nowPlayingViewModel: NowPlayingViewModel,
    showGlobalNowPlaying: Boolean,
    reenteringFromPlayer: Boolean,
    showBottomNavigation: Boolean,
    bottomNavHeight: Dp,
    navigationRailWidth: Dp,
    contentPaneStart: Dp,
    contentPaneWidth: Dp,
    showNavigationRail: Boolean,
    activeBottomRoute: String,
    currentRoute: String?,
    navigationState: RootNavigationState,
    topLevelDestinations: List<TopLevelDestination>,
    motionTransitions: MotionTransitions,
    onOpenPlayer: (NowPlayingTransitionSnapshot?) -> Unit,
) {
    val showCompactBottomNavigation = showBottomNavigation
    if (showNavigationRail) {
        TabletNavigationRail(
            currentRoute = activeBottomRoute,
            destinations = topLevelDestinations,
            onNavigate = { route ->
                navigationState.navigateBottomTab(
                    route = route,
                    activeBottomRoute = activeBottomRoute,
                    currentRoute = currentRoute,
                )
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(navigationRailWidth)
                .fillMaxHeight()
                .zIndex(RootLayerZ.BottomNavigation),
        )
    }
    if (showSharedTopBarBackdrop && sharedTopBarSpec != null) {
        FrostedTopBarBackground(
            darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = contentPaneStart)
                .width(contentPaneWidth)
                .height(sharedTopBarHeight)
                .zIndex(RootLayerZ.ChromeBackdrop),
        )
    }
    if (sharedTopBarSpec != null) {
        CompositionLocalProvider(LocalRenderSharedTopBarContent provides true) {
            SharedTopBarOverlay(
                spec = sharedTopBarSpec,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = contentPaneStart)
                    .width(contentPaneWidth)
                    .zIndex(RootLayerZ.SharedTopBar),
            )
        }
    }
    if (canHostCompactNowPlaying) {
        playbackState.currentSong?.let { song: Song ->
            val dockModifier = if (nowPlayingBarStyle == NowPlayingBarStyle.Compact) {
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = contentPaneStart)
                    .width(contentPaneWidth)
                    .padding(bottom = if (showCompactBottomNavigation) bottomNavHeight else navigationBarInsetDp())
            } else {
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = contentPaneStart)
                    .width(contentPaneWidth)
                    .fillMaxHeight()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (showCompactBottomNavigation) bottomNavHeight + 8.dp else navigationBarInsetDp() + 10.dp,
                    )
            }
            Box(
                modifier = dockModifier.zIndex(RootLayerZ.CompactPlayer),
                contentAlignment = Alignment.BottomCenter,
            ) {
                CompactNowPlayingDockHost(
                    viewModel = nowPlayingViewModel,
                    song = song,
                    style = nowPlayingBarStyle,
                    transportShowsPause = playbackState.transportShowsPause,
                    visible = showGlobalNowPlaying,
                    suppressEnterAnimation = reenteringFromPlayer,
                    onOpenPlayer = onOpenPlayer,
                    modifier = if (nowPlayingBarStyle == NowPlayingBarStyle.Compact) {
                        Modifier
                            .width(contentPaneWidth)
                            .fillMaxWidth()
                            .height(60.dp)
                    } else {
                        Modifier.width(contentPaneWidth).fillMaxWidth()
                    },
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
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = contentPaneStart)
                .width(contentPaneWidth),
        )
    }
}

@Composable
private fun BoxScope.TabletNavigationRail(
    currentRoute: String,
    destinations: List<TopLevelDestination>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    val copy = androidx.compose.runtime.remember(language) { commonUiCopy(language) }
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        destinations.forEach { destination ->
            val label = when (destination.route) {
                HOME_ROUTE -> copy.home
                ALBUMS_ROUTE -> copy.library
                PLAYLISTS_ROUTE -> copy.playlists
                SEARCH_ROUTE -> copy.search
                else -> destination.contentDescription
            }
            NavigationRailItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                icon = {
                    androidx.compose.material3.Icon(
                        painter = androidx.compose.ui.res.painterResource(destination.iconResId),
                        contentDescription = label,
                    )
                },
            )
        }
    }
}
