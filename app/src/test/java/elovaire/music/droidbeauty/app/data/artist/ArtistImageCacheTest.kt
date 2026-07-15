package elovaire.music.droidbeauty.app.data.artist

import org.junit.Assert.assertEquals
import org.junit.Test

class ArtistImageCacheTest {
    @Test
    fun cacheTrimmingEvictsOldestEntriesAndPreservesIncomingKey() {
        val entries = linkedMapOf(
            "old" to 1L,
            "new" to 3L,
            "current" to 2L,
        )

        assertEquals(
            listOf("old"),
            artistCacheKeysToTrim(entries, incomingKey = "current", maxEntries = 2),
        )
        assertEquals(
            listOf("old", "current"),
            artistCacheKeysToTrim(entries, incomingKey = "incoming", maxEntries = 2),
        )
    }
}
