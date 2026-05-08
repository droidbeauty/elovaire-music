package elovaire.music.app.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LyricsLineResolverTest {

    private val syncedLines = listOf(
        LyricsLine(text = "One", startTimeMs = 1_000L, endTimeMs = 2_000L, index = 0),
        LyricsLine(text = "Two", startTimeMs = 2_000L, endTimeMs = 3_000L, index = 1),
        LyricsLine(text = "Three", startTimeMs = 3_000L, endTimeMs = null, index = 2),
    )

    @Test
    fun `position before first line returns null`() {
        assertNull(resolveActiveLyricLineIndex(syncedLines, positionMs = 900L))
    }

    @Test
    fun `position between lines keeps current line active`() {
        assertEquals(0, resolveActiveLyricLineIndex(syncedLines, positionMs = 1_500L))
        assertEquals(1, resolveActiveLyricLineIndex(syncedLines, positionMs = 2_100L))
    }

    @Test
    fun `position after last line keeps last line active`() {
        assertEquals(2, resolveActiveLyricLineIndex(syncedLines, positionMs = 12_000L))
    }

    @Test
    fun `timing offset is applied after synced timestamps`() {
        assertNull(resolveActiveLyricLineIndex(syncedLines, positionMs = 1_400L, timingOffsetMs = 500L))
        assertEquals(0, resolveActiveLyricLineIndex(syncedLines, positionMs = 1_600L, timingOffsetMs = 500L))
    }
}
