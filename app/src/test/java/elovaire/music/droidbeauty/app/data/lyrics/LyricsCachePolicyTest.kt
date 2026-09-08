package elovaire.music.droidbeauty.app.data.lyrics

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsCachePolicyTest {
    @Test
    fun trimRemovesLeastRecentlyUsedEntry() {
        val entries = LinkedHashMap<String, LyricsCacheEntry>(16, 0.75f, true)
        entries["first"] = LyricsCacheEntry(LyricsResult.NotFound, 1L)
        entries["second"] = LyricsCacheEntry(LyricsResult.NotFound, 1L)
        entries["first"]

        trimLyricsCacheEntries(entries, maxEntries = 1)

        assertTrue(entries.containsKey("first"))
        assertFalse(entries.containsKey("second"))
    }

    @Test
    fun futureDeadlineFromClockRollbackIsStale() {
        val day = 24L * 60L * 60L * 1_000L
        val entry = LyricsCacheEntry(LyricsResult.NotFound, expiresAtMillis = 31L * day, online = true)

        assertFalse(entry.isExpired(30L * day))
        assertTrue(entry.isExpired(29L * day))
    }
}
