package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RootChromePolicyTest {
    @Test
    fun rootChromeVisibility_showsTopLevelChromeForTopLevelRoutes() {
        val visibility = rootChromeVisibility(
            currentRoute = HOME_ROUTE,
            keyboardVisible = false,
            searchQueryActive = false,
            currentSongPresent = true,
            playerLayerState = PlayerLayerState.Compact,
        )

        assertTrue(visibility.showTopLevelChrome)
        assertTrue(visibility.showBottomNavigation)
        assertTrue(visibility.canHostCompactNowPlaying)
        assertTrue(visibility.showGlobalNowPlaying)
        assertFalse(visibility.showPlayerOverlay)
    }

    @Test
    fun rootChromeVisibility_hidesCompactPlayerForSearchTyping() {
        val visibility = rootChromeVisibility(
            currentRoute = SEARCH_ROUTE,
            keyboardVisible = false,
            searchQueryActive = true,
            currentSongPresent = true,
            playerLayerState = PlayerLayerState.Compact,
        )

        assertTrue(visibility.hideCompactNowPlaying)
        assertFalse(visibility.reserveCompactNowPlayingSpace)
        assertFalse(visibility.showGlobalNowPlaying)
    }

    @Test
    fun rootChromeVisibility_tracksExpandedAndReturningPlayerStates() {
        val expanded = rootChromeVisibility(
            currentRoute = HOME_ROUTE,
            keyboardVisible = false,
            searchQueryActive = false,
            currentSongPresent = true,
            playerLayerState = PlayerLayerState.Expanded,
        )
        val returning = rootChromeVisibility(
            currentRoute = HOME_ROUTE,
            keyboardVisible = false,
            searchQueryActive = false,
            currentSongPresent = true,
            playerLayerState = PlayerLayerState.ReturningToCompact,
        )

        assertTrue(expanded.showPlayerOverlay)
        assertFalse(expanded.showGlobalNowPlaying)
        assertTrue(returning.reenteringFromPlayer)
        assertTrue(returning.showGlobalNowPlaying)
    }

    @Test
    fun rootScaffoldPadding_reservesChromeAndMiniPlayerSpace() {
        val padding = rootScaffoldPadding(
            showTopLevelChrome = true,
            showBottomNavigation = true,
            reserveCompactNowPlayingSpace = true,
            topBarHeight = 64.dp,
            innerTopPadding = 10.dp,
            bottomNavHeight = 72.dp,
        )

        assertEquals(64.dp + ElovaireSpacing.topBarToFirstContentGap, padding.topContent)
        assertEquals(
            72.dp + ElovaireSpacing.miniPlayerReservedHeight + ElovaireSpacing.scrollTailPadding,
            padding.bottomContent,
        )
        assertEquals(padding.bottomContent, padding.detailBottom)
    }
}
