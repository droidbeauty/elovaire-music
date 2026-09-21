package elovaire.music.droidbeauty.app.quality

import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.ui.screens.RootRouteRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RapidUiSurfaceCatalogTest {
    @Test
    fun routeRegistryAndCatalogStayInLockstep() {
        val registered = RootRouteRegistry.registeredPatterns
        val cataloged = RapidUiSurfaceCatalog.routePatterns

        assertEquals("Root route registry contains duplicates", registered.size, registered.toSet().size)
        assertEquals("Route catalog contains duplicates", cataloged.size, RapidUiSurfaceCatalog.routes.size)
        assertEquals(
            "Every registered route must have rapid interaction coverage",
            registered.toSet(),
            cataloged,
        )
    }

    @Test
    fun catalogHasNoDuplicateSurfaceIdsAndCoversSystemBoundaries() {
        val all = RapidUiSurfaceCatalog.all
        assertEquals("Surface IDs must be unique", all.size, RapidUiSurfaceCatalog.ids.size)
        assertTrue(all.all { it.id.isNotBlank() && it.entryJourney.isNotBlank() && it.exitJourney.isNotBlank() })
        assertTrue(all.any { it.involvesSystemUi && it.id == "root.permission_gate" })
        assertTrue(all.any { it.involvesSystemUi && it.id == "manage_playlists.create_document" })
        assertTrue(all.any { it.involvesSystemUi && it.id == "manage_playlists.open_document" })
        assertTrue(all.any { it.involvesSystemUi && it.id == "tag.artwork_picker" })
        assertTrue(all.any { it.kind == RapidUiSurfaceKind.FullScreenLayer && it.id == "root.full_player" })
        assertTrue(all.any { it.kind == RapidUiSurfaceKind.Chrome && it.id == "root.compact_player" })
    }
}
