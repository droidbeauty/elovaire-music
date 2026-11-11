package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.OverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage

internal data class RootChromeLayout(
    val topBarHeight: Dp,
    val sharedTopBarHeight: Dp,
    val bottomNavHeight: Dp,
    val navigationRailWidth: Dp,
    val showNavigationRail: Boolean,
    val showBottomNavigation: Boolean,
    val contentPaneStart: Dp,
    val contentPaneWidth: Dp,
    val routePadding: RootRoutePadding,
)

@Composable
internal fun ElovaireRootShell(
    overscrollFactory: OverscrollFactory,
    songMenuActions: SongMenuActions,
    chromeHazeState: HazeState,
    sharedBackIconPainter: Painter,
    sharedTopMenuIconPainter: Painter,
    appLanguage: AppLanguage,
    chromeVisibility: RootChromeVisibility,
    sharedTopBarController: SharedTopBarController,
    navHostBlur: Dp,
    navHostScrimAlpha: Float,
    routeHost: @Composable (RootRoutePadding, Modifier) -> Unit,
    chromeHost: @Composable BoxScope.(RootChromeLayout) -> Unit,
    overlayHost: @Composable BoxScope.(RootChromeLayout) -> Unit,
    playerLayerHost: @Composable BoxScope.() -> Unit,
) {
    CompositionLocalProvider(
        LocalOverscrollFactory provides overscrollFactory,
        LocalSongMenuActions provides songMenuActions,
        LocalChromeHazeState provides chromeHazeState,
        LocalSharedBackIconPainter provides sharedBackIconPainter,
        LocalSharedTopMenuIconPainter provides sharedTopMenuIconPainter,
        LocalAppLanguage provides appLanguage,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
        ) {
            val windowSizeClass = rootWindowSizeClass(maxWidth, maxHeight)
            CompositionLocalProvider(LocalRootWindowSizeClass provides windowSizeClass) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
            ) { innerPadding ->
                val layout = rootChromeLayout(
                    chromeVisibility = chromeVisibility,
                    innerTopPadding = innerPadding.calculateTopPadding(),
                    windowSizeClass = windowSizeClass,
                    windowWidth = maxWidth,
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.TopStart)
                            .width(layout.contentPaneWidth)
                            .fillMaxHeight()
                            .offset(x = layout.contentPaneStart),
                    ) {
                    CompositionLocalProvider(
                        LocalUseSharedTopBarBackdrop provides chromeVisibility.showSharedTopBarBackdrop,
                        LocalSharedTopBarController provides sharedTopBarController,
                    ) {
                        routeHost(
                            layout.routePadding,
                            Modifier
                                .fillMaxSize()
                                .blur(navHostBlur)
                                .hazeSource(chromeHazeState, zIndex = -1f)
                                .background(MaterialTheme.colorScheme.background),
                        )
                        if (navHostScrimAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background.copy(alpha = navHostScrimAlpha)),
                            )
                        }
                    }
                    }
                    chromeHost(layout)
                    overlayHost(layout)
                }
            }
            playerLayerHost()
            }
        }
    }
}

@Composable
internal fun rootChromeLayout(
    chromeVisibility: RootChromeVisibility,
    innerTopPadding: Dp,
    windowWidth: Dp = 400.dp,
    windowSizeClass: RootWindowSizeClass = RootWindowSizeClass(
        RootWindowWidthClass.Compact,
        RootWindowHeightClass.Expanded,
    ),
): RootChromeLayout {
    val topBarHeight = topBarOccupiedHeight()
    val showNavigationRail = chromeVisibility.showBottomNavigation &&
        windowSizeClass.width >= RootWindowWidthClass.Medium
    val showBottomNavigation = chromeVisibility.showBottomNavigation && !showNavigationRail
    val bottomNavHeight = if (showBottomNavigation) bottomNavigationOccupiedHeight() else 0.dp
    val navigationRailWidth = if (showNavigationRail) 80.dp else 0.dp
    val availableContentWidth = (windowWidth - navigationRailWidth).coerceAtLeast(0.dp)
    val contentPaneWidth = availableContentWidth.coerceAtMost(1200.dp)
    val contentPaneStart = navigationRailWidth + (availableContentWidth - contentPaneWidth) / 2
    return RootChromeLayout(
        topBarHeight = topBarHeight,
        sharedTopBarHeight = sharedTopBarOccupiedHeight(),
        bottomNavHeight = bottomNavHeight,
        navigationRailWidth = navigationRailWidth,
        showNavigationRail = showNavigationRail,
        showBottomNavigation = showBottomNavigation,
        contentPaneStart = contentPaneStart,
        contentPaneWidth = contentPaneWidth,
        routePadding = rootScaffoldPadding(
            showTopLevelChrome = chromeVisibility.showTopLevelChrome,
            showBottomNavigation = showBottomNavigation,
            reserveCompactNowPlayingSpace = chromeVisibility.reserveCompactNowPlayingSpace,
            topBarHeight = topBarHeight,
            innerTopPadding = innerTopPadding,
            bottomNavHeight = bottomNavHeight,
        ),
    )
}
