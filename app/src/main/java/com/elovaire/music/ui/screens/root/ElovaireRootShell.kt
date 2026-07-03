package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.OverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
            ) { innerPadding ->
                val layout = rootChromeLayout(
                    chromeVisibility = chromeVisibility,
                    innerTopPadding = innerPadding.calculateTopPadding(),
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(
                        LocalUseSharedTopBarBackdrop provides chromeVisibility.showSharedTopBarBackdrop,
                        LocalSharedTopBarController provides sharedTopBarController,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .hazeSource(chromeHazeState),
                        ) {
                            routeHost(
                                layout.routePadding,
                                Modifier
                                    .fillMaxSize()
                                    .blur(navHostBlur),
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

@Composable
internal fun rootChromeLayout(
    chromeVisibility: RootChromeVisibility,
    innerTopPadding: Dp,
): RootChromeLayout {
    val topBarHeight = topBarOccupiedHeight()
    val bottomNavHeight = if (chromeVisibility.showBottomNavigation) bottomNavigationOccupiedHeight() else 0.dp
    return RootChromeLayout(
        topBarHeight = topBarHeight,
        sharedTopBarHeight = sharedTopBarOccupiedHeight(),
        bottomNavHeight = bottomNavHeight,
        routePadding = rootScaffoldPadding(
            showTopLevelChrome = chromeVisibility.showTopLevelChrome,
            showBottomNavigation = chromeVisibility.showBottomNavigation,
            reserveCompactNowPlayingSpace = chromeVisibility.reserveCompactNowPlayingSpace,
            topBarHeight = topBarHeight,
            innerTopPadding = innerTopPadding,
            bottomNavHeight = bottomNavHeight,
        ),
    )
}
