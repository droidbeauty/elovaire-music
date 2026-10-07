package elovaire.music.droidbeauty.app.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsCachePolicyTest {
    @Test
    fun snapshotsLargerThanTheReadLimitAreNotPersisted() {
        assertTrue(isLyricsCacheSnapshotWithinLimit(1, 10))
        assertTrue(isLyricsCacheSnapshotWithinLimit(10, 10))
        assertFalse(isLyricsCacheSnapshotWithinLimit(11, 10))
        assertFalse(isLyricsCacheSnapshotWithinLimit(0, 10))
    }

    @Test
    fun snapshotBudgetKeepsNewestEntriesAndSkipsOversizedOnes() {
        val retained = retainRecentLyricsCacheEntriesWithinLimit(
            entriesNewestFirst = listOf("newest" to 4, "middle" to 4, "oldest" to 4),
            emptySnapshotBytes = 2,
            maxBytes = 14,
            serializedEntrySizeBytes = { it.second },
        )

        assertEquals(listOf("newest", "middle"), retained.map { it.first })
        assertEquals(
            listOf("old"),
            retainRecentLyricsCacheEntriesWithinLimit(
                entriesNewestFirst = listOf("too-large" to 20, "old" to 4),
                emptySnapshotBytes = 2,
                maxBytes = 8,
                serializedEntrySizeBytes = { it.second },
            ).map { it.first },
        )
    }

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
