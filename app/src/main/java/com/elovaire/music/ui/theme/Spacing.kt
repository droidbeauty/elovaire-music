package elovaire.music.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
object ElovaireSpacing {
    // Height of the main top bar content, excluding the Android status bar above it.
    val topBarContentHeight: Dp = 56.dp

    // Height of detail top bars like album, playlist, artist, and similar drill-in screens.
    val detailTopBarContentHeight: Dp = 58.dp

    // Gap between a top bar and the first main piece of content below it on top-level screens.
    val topBarToFirstContentGap: Dp = 22.dp

    // Extra space between a detail top bar and a large hero header like album or playlist artwork.
    val albumHeaderTopGap: Dp = 32.dp

    // Reserved stack height for the compact now playing bar.
    val miniPlayerReservedHeight: Dp = 86.dp

    // Visible body height of the bottom navigation bar.
    val bottomNavigationBodyHeight: Dp = 78.dp

    // Outer bottom inset used to lift the bottom navigation bar off the system edge.
    val bottomNavigationOuterPadding: Dp = 8.dp

    // Shared tail space at the bottom of scrollable screens.
    val scrollTailPadding: Dp = 20.dp

    // Top inset used before the first standard list/grid content block on detail screens.
    val detailListTopGap: Dp = 18.dp

    // Slightly tighter top inset used before compact chips or controls under detail top bars.
    val detailCompactTopGap: Dp = 14.dp

    // Larger top inset used before grouped modules on detail screens.
    val detailSectionTopGap: Dp = 24.dp

    // Default horizontal page padding used by most scrollable screens.
    val screenHorizontalPadding: Dp = 20.dp

    // Shared vertical gap between major sections stacked in a screen.
    val sectionVerticalGap: Dp = 20.dp

    // Gap between now playing artwork and the title/artist row.
    val nowPlayingTitleTopGap: Dp = 5.dp

    // Gap between the now playing title/artist row and the progress section below it.
    val nowPlayingTitleBottomGap: Dp = 0.dp
}
