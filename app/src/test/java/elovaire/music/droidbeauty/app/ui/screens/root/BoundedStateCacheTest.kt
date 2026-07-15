package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BoundedStateCacheTest {
    @Test
    fun cacheBoundsDynamicScreenKeysAndRetainsRecentlyReadValues() {
        val cache = BoundedStateCache<Int>(maxEntries = 2)
        cache["first"] = 1
        cache["second"] = 2
        assertEquals(1, cache["first"])

        cache["third"] = 3

        assertEquals(2, cache.size())
        assertNull(cache["second"])
        assertEquals(1, cache["first"])
        assertEquals(3, cache["third"])
    }

    @Test
    fun removeIfClearsOnlyMatchingRoutes() {
        val cache = BoundedStateCache<Int>(maxEntries = 3)
        cache["album_detail|1"] = 1
        cache["search_screen"] = 2

        cache.removeIf { it.startsWith("album_detail") }

        assertNull(cache["album_detail|1"])
        assertEquals(2, cache["search_screen"])
    }
}
