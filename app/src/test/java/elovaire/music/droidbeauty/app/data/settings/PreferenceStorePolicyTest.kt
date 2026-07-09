package elovaire.music.droidbeauty.app.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class PreferenceStorePolicyTest {
    @Test
    fun incrementPlayCount_neverOverflowsOrPersistsNegativeCounts() {
        assertEquals(1, incrementPlayCount(null))
        assertEquals(1, incrementPlayCount(-4))
        assertEquals(4, incrementPlayCount(3))
        assertEquals(Int.MAX_VALUE, incrementPlayCount(Int.MAX_VALUE))
    }

    @Test
    fun normalizeFavoriteSongIds_dropsInvalidIdsAndKeepsOrder() {
        assertEquals(
            listOf(8L, 3L),
            normalizeFavoriteSongIds(listOf(0L, -1L, 8L, 3L, 8L, 0L)),
        )
    }
}
