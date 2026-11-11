package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RootChromePolicyTest {
    @Test
    fun windowSizeClassesKeepPhoneGridsUnchangedAndExpandTabletGrids() {
        val phone = rootWindowSizeClass(412.dp, 915.dp)
        val tablet = rootWindowSizeClass(1280.dp, 800.dp)

        assertEquals(RootWindowWidthClass.Compact, phone.width)
        assertEquals(RootWindowHeightClass.Expanded, phone.height)
        assertEquals(RootWindowWidthClass.Large, tablet.width)
        assertEquals(RootWindowHeightClass.Medium, tablet.height)
        assertTrue(tablet.isLandscape)
        assertFalse(phone.isLandscape)
        assertEquals(2, adaptiveGridColumnCount(2, phone.width))
        assertEquals(5, adaptiveGridColumnCount(2, tablet.width))
        assertEquals(3, adaptiveGridColumnCount(3, phone.width))
        assertEquals(6, adaptiveGridColumnCount(3, tablet.width))
    }

    @Test
    fun libraryNavigationFallbackUsesTheCorrectScreenName() {
        assertEquals("Library", DefaultTopLevelDestinations.single { it.route == ALBUMS_ROUTE }.contentDescription)
    }
    @Test
    fun playlistPickerTabsUseTheSelectedLanguage() {
        assertEquals(listOf("Utwory", "Albumy", "Artyści"), PlaylistPickerTab.entries.map { it.label(AppLanguage.Polish) })
        AppLanguage.entries.forEach { language ->
            PlaylistPickerTab.entries.forEach { assertTrue(it.label(language).isNotBlank()) }
        }
    }
    @Test
    fun upscalerIsRegisteredAsASettingsDetailScreen() {
        assertTrue(UPSCALER_ROUTE in RootRouteRegistry.registeredPatterns)
        assertEquals(1, ElovaireNavigationTransitions.depthOf(UPSCALER_ROUTE))
        assertEquals("upscaler", rootPerformanceRouteLabel(UPSCALER_ROUTE))
        val visibility = rootChromeVisibility(UPSCALER_ROUTE, false, false, true, PlayerLayerState.Compact)
        assertFalse(visibility.showGlobalNowPlaying)
    }

    @Test
    fun flatOverlaySkipsRuntimeHazeWhileNormalSurfacesKeepIt() {
        assertFalse(shouldApplyDynamicBackdropHaze(31, flatOverlay = true, hazeStateAvailable = true))
        assertTrue(shouldApplyDynamicBackdropHaze(31, flatOverlay = false, hazeStateAvailable = true))
        assertFalse(shouldApplyDynamicBackdropHaze(30, flatOverlay = false, hazeStateAvailable = true))
        assertFalse(shouldApplyDynamicBackdropHaze(31, flatOverlay = false, hazeStateAvailable = false))
    }

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
    fun rootChromeVisibility_hidesCompactPlayerForArtistInfoOverlay() {
        val visibility = rootChromeVisibility(
            currentRoute = "$ARTIST_ROUTE/{artistName}",
            keyboardVisible = false,
            searchQueryActive = false,
            currentSongPresent = true,
            playerLayerState = PlayerLayerState.Compact,
            artistInfoOverlayVisible = true,
        )

        assertTrue(visibility.showBottomNavigation)
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

    @Test
    fun rootSharedTopBarSpec_exposesPlaylistCreateActionOnlyOnPlaylistsRoute() {
        val playlistsSpec = rootSharedTopBarSpec(
            currentRoute = PLAYLISTS_ROUTE,
            showTopLevelChrome = true,
            language = AppLanguage.English,
            onRequestCreatePlaylist = {},
            onOpenMenu = {},
        ) as SharedTopBarSpec.Unified
        val homeSpec = rootSharedTopBarSpec(
            currentRoute = HOME_ROUTE,
            showTopLevelChrome = true,
            language = AppLanguage.English,
            onRequestCreatePlaylist = {},
            onOpenMenu = {},
        ) as SharedTopBarSpec.Unified

        assertEquals("Create playlist", playlistsSpec.supplementalActionContentDescription)
        val polishSpec = rootSharedTopBarSpec(PLAYLISTS_ROUTE, true, AppLanguage.Polish, {}, {}) as SharedTopBarSpec.Unified
        assertEquals("Utwórz playlistę", polishSpec.supplementalActionContentDescription)
        AppLanguage.entries.forEach { language ->
            val spec = rootSharedTopBarSpec(PLAYLISTS_ROUTE, true, language, {}, {}) as SharedTopBarSpec.Unified
            assertTrue("Missing playlist action label for $language", !spec.supplementalActionContentDescription.isNullOrBlank())
            if (language != AppLanguage.English) {
                assertTrue("English playlist action label for $language", spec.supplementalActionContentDescription != "Create playlist")
            }
        }
        assertTrue(playlistsSpec.onSupplementalAction != null)
        assertEquals(null, homeSpec.supplementalActionContentDescription)
        assertEquals(null, homeSpec.onSupplementalAction)
        assertEquals(null, rootSharedTopBarSpec(HOME_ROUTE, showTopLevelChrome = false, AppLanguage.English, {}, {}))
    }

    @Test
    fun topBarMotionDirection_tracksSemanticDepth() {
        val unified = SharedTopBarSpec.Unified(
            title = "Home",
            showSettings = true,
            onOpenMenu = {},
        )
        val back = SharedTopBarSpec.Back(
            title = "Settings",
            onBack = {},
        )
        val detail = SharedTopBarSpec.Detail(
            title = "Album",
            subtitle = "Artist",
            onBack = {},
        )

        assertEquals(TopBarMotionDirection.Forward, topBarMotionDirection(unified, back))
        assertEquals(TopBarMotionDirection.Forward, topBarMotionDirection(back, detail))
        assertEquals(TopBarMotionDirection.Back, topBarMotionDirection(detail, back))
        assertEquals(TopBarMotionDirection.Back, topBarMotionDirection(back, unified))
        assertEquals(TopBarMotionDirection.Lateral, topBarMotionDirection(back, back.copy(title = "About")))
    }
}
