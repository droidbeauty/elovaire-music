package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertNotSame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class RootNavigationTransitionsTest {
    @Test
    fun interactionState_returnsToIdleWhenTheObservedRouteCommits() {
        RootInteractionState.begin("bottom_nav")
        assertEquals("bottom_nav", rootPerformanceInteractionLabel())

        RootInteractionState.finish()

        assertEquals("idle", rootPerformanceInteractionLabel())
    }

    @Test
    fun committedTopLevelRoute_doesNotAllowAnOlderDestinationToOverwritePendingIntent() {
        assertNull(committedTopLevelRoute(ALBUMS_ROUTE, SEARCH_ROUTE))
        assertEquals(SEARCH_ROUTE, committedTopLevelRoute(SEARCH_ROUTE, SEARCH_ROUTE))
    }

    @Test
    fun committedTopLevelRoute_acceptsObservedDestinationWithoutPendingIntent() {
        assertEquals(HOME_ROUTE, committedTopLevelRoute(HOME_ROUTE, null))
        assertNull(committedTopLevelRoute(SETTINGS_ROUTE, null))
    }

    @Test
    fun resolveActiveBottomRoute_prefersLatestPendingTabOverStaleDetailOwner() {
        assertEquals(
            SEARCH_ROUTE,
            resolveActiveBottomRoute(
                pendingTopLevelRoute = SEARCH_ROUTE,
                routeOwner = ALBUMS_ROUTE,
                currentRoute = "$ALBUM_ROUTE/{albumId}",
                browsingOriginRoute = ALBUMS_ROUTE,
                selectedBottomRoute = ALBUMS_ROUTE,
            ),
        )
    }

    @Test
    fun resolveActiveBottomRoute_usesDetailOwnerWhenNoPendingTabExists() {
        assertEquals(
            SEARCH_ROUTE,
            resolveActiveBottomRoute(
                pendingTopLevelRoute = null,
                routeOwner = SEARCH_ROUTE,
                currentRoute = "$ALBUM_ROUTE/{albumId}",
                browsingOriginRoute = ALBUMS_ROUTE,
                selectedBottomRoute = ALBUMS_ROUTE,
            ),
        )
    }

    @Test
    fun resolver_reusesResolutionForSameNavigationPair() {
        val resolver = NavigationMotionResolver()
        val key = NavigationMotionKey(
            initialRoute = HOME_ROUTE,
            targetRoute = ALBUMS_ROUTE,
            initialFallbackTopLevelRoute = HOME_ROUTE,
            targetFallbackTopLevelRoute = ALBUMS_ROUTE,
            detailMode = DetailRouteTransitionMode.Standard,
        )

        assertSame(resolver.resolve(key), resolver.resolve(key))
    }

    @Test
    fun resolver_invalidatesWhenDetailModeChanges() {
        val resolver = NavigationMotionResolver()
        val key = NavigationMotionKey(
            initialRoute = ALBUMS_ROUTE,
            targetRoute = "$ALBUM_ROUTE/{albumId}",
            initialFallbackTopLevelRoute = ALBUMS_ROUTE,
            targetFallbackTopLevelRoute = ALBUMS_ROUTE,
            detailMode = DetailRouteTransitionMode.Standard,
        )

        val standard = resolver.resolve(key)
        val tileExpand = resolver.resolve(key.copy(detailMode = DetailRouteTransitionMode.TileExpand))

        assertNotSame(standard, tileExpand)
    }
}
