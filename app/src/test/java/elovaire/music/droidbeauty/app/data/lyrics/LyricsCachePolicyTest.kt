package elovaire.music.droidbeauty.app.data.lyrics

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsCachePolicyTest {
    @Test
    fun futureDeadlineFromClockRollbackIsStale() {
        val day = 24L * 60L * 60L * 1_000L
        val entry = LyricsCacheEntry(LyricsResult.NotFound, expiresAtMillis = 31L * day, online = true)

        assertFalse(entry.isExpired(30L * day))
        assertTrue(entry.isExpired(29L * day))
    }
}
