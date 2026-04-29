package elovaire.music.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
object ElovaireSpacing {
    // Main top bar height on home/library/playlists/search.
    val topBarContentHeight: Dp = 56.dp

    // Top bar height on detail screens like album, playlist, artist, and changelog.
    val detailTopBarContentHeight: Dp = 58.dp

    // Default vertical gap between a top bar and the first content block on main screens.
    val topBarToFirstContentGap: Dp = 22.dp

    // Space above large hero artwork/headers on drill-in screens.
    val albumHeaderTopGap: Dp = 32.dp

    // Total reserved height for the compact now playing bar area.
    val miniPlayerReservedHeight: Dp = 86.dp

    // Visible height of the bottom navigation dock itself.
    val bottomNavigationBodyHeight: Dp = 78.dp

    // Extra bottom lift for the dock when it is not fully edge-to-edge.
    val bottomNavigationOuterPadding: Dp = 8.dp

    // Shared extra scroll room below the last list item.
    val scrollTailPadding: Dp = 20.dp

    // Default top gap before the first list/grid block on detail screens.
    val detailListTopGap: Dp = 18.dp

    // Tighter top gap for compact control rows and chips under detail headers.
    val detailCompactTopGap: Dp = 14.dp

    // Larger top gap before full grouped sections/modules on detail screens.
    val detailSectionTopGap: Dp = 24.dp

    // Standard left/right screen padding used across most screens.
    val screenHorizontalPadding: Dp = 20.dp

    // Standard gap between major stacked sections/modules.
    val sectionVerticalGap: Dp = 20.dp

    // Top gap between now playing artwork and the title/artist row.
    val nowPlayingTitleTopGap: Dp = 5.dp

    // Bottom gap between the title/artist row and the progress bar in now playing.
    val nowPlayingTitleBottomGap: Dp = 0.dp
}
