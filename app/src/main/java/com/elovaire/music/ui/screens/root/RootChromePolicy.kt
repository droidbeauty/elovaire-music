package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing

internal data class RootChromeVisibility(
    val showTopLevelChrome: Boolean,
    val showBottomNavigation: Boolean,
    val hideCompactNowPlaying: Boolean,
    val reserveCompactNowPlayingSpace: Boolean,
    val canHostCompactNowPlaying: Boolean,
    val showPlayerOverlay: Boolean,
    val showGlobalNowPlaying: Boolean,
    val reenteringFromPlayer: Boolean,
    val showSharedTopBarBackdrop: Boolean,
)

internal fun rootChromeVisibility(
    currentRoute: String?,
    keyboardVisible: Boolean,
    searchQueryActive: Boolean,
    currentSongPresent: Boolean,
    playerLayerState: PlayerLayerState,
): RootChromeVisibility {
    val hideCompactNowPlaying = (keyboardVisible && currentRoute == PLAYLISTS_ROUTE) ||
        (currentRoute == SEARCH_ROUTE && searchQueryActive) ||
        currentRoute in CompactNowPlayingHiddenRoutes
    val canHostCompactNowPlaying = currentSongPresent
    val showPlayerOverlay = playerLayerState == PlayerLayerState.Expanded && currentSongPresent
    return RootChromeVisibility(
        showTopLevelChrome = currentRoute in TopLevelRoutes,
        showBottomNavigation = currentRoute in BottomNavigationRoutes,
        hideCompactNowPlaying = hideCompactNowPlaying,
        reserveCompactNowPlayingSpace = currentSongPresent && !hideCompactNowPlaying,
        canHostCompactNowPlaying = canHostCompactNowPlaying,
        showPlayerOverlay = showPlayerOverlay,
        showGlobalNowPlaying = canHostCompactNowPlaying && !hideCompactNowPlaying && playerLayerState != PlayerLayerState.Expanded,
        reenteringFromPlayer = playerLayerState == PlayerLayerState.ReturningToCompact,
        showSharedTopBarBackdrop = currentRoute != null && currentRoute != PLAYER_ROUTE,
    )
}

internal fun rootScaffoldPadding(
    showTopLevelChrome: Boolean,
    showBottomNavigation: Boolean,
    reserveCompactNowPlayingSpace: Boolean,
    topBarHeight: Dp,
    innerTopPadding: Dp,
    bottomNavHeight: Dp,
): RootRoutePadding {
    val bottomContentPadding =
        (if (showBottomNavigation) bottomNavHeight else 0.dp) +
            (if (reserveCompactNowPlayingSpace) ElovaireSpacing.miniPlayerReservedHeight else 0.dp) +
            ElovaireSpacing.scrollTailPadding
    return RootRoutePadding(
        topContent = if (showTopLevelChrome) {
            topBarHeight + ElovaireSpacing.topBarToFirstContentGap
        } else {
            innerTopPadding
        },
        bottomContent = bottomContentPadding,
        detailBottom = bottomContentPadding,
    )
}

private val CompactNowPlayingHiddenRoutes = setOf(
    SETTINGS_ROUTE,
    LIBRARY_FOLDERS_ROUTE,
    PRIVACY_SAFETY_ROUTE,
    CHANGELOG_ROUTE,
    ABOUT_ROUTE,
    EQUALIZER_ROUTE,
    "$ALBUM_TAG_EDITOR_ROUTE/{albumId}",
)
