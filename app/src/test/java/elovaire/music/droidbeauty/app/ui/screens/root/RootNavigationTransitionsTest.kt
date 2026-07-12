package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class RootNavigationTransitionsTest {
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
