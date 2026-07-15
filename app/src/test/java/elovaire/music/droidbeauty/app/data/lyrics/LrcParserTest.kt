package elovaire.music.droidbeauty.app.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

class LrcParserTest {
    @Test
    fun parsesMultipleTimestampPrecisionsAndAppliesGlobalOffset() {
        val payload = requireNotNull(
            parseLrcOrPlain(
                raw = """
                    [00:01.50][00:02.500]First line
                    [offset:250]
                    [00:03]Second line
                """.trimIndent(),
                providerName = "test",
                confidence = 90,
            ),
        )

        assertTrue(payload.isSynced)
        assertEquals(listOf(1_750L, 2_750L, 3_250L), payload.lines.map { it.startTimeMs })
        assertEquals(listOf(0, 1, 2), payload.lines.map { it.index })
    }

    @Test
    fun plainLyricsRemainVisibleWithoutTiming() {
        val payload = requireNotNull(
            parseLrcOrPlain(
                raw = "First line\nSecond line",
                providerName = "test",
                confidence = 60,
            ),
        )

        assertFalse(payload.isSynced)
        assertEquals(listOf("First line", "Second line"), payload.lines.map { it.text })
        assertEquals(listOf(null, null), payload.lines.map { it.startTimeMs })
    }

    @Test
    fun blankTimedLinesAndMetadataDoNotCreateVisibleRows() {
        val payload = requireNotNull(
            parseLrcOrPlain(
                raw = """
                    [ar:Artist]
                    [ti:Title]
                    [00:01.00]
                    [00:02.00]Visible
                """.trimIndent(),
                providerName = "test",
                confidence = 90,
            ),
        )

        assertEquals(listOf("Visible"), payload.lines.map { it.text })
    }

    @Test
    fun duplicateTimestampsUseNextDistinctLineForEndTime() {
        val payload = requireNotNull(
            parseLrcOrPlain(
                raw = """
                    [00:10.00]First
                    [00:10.00]Echo
                    [00:12.00]Second
                """.trimIndent(),
                providerName = "test",
                confidence = 90,
            ),
        )

        assertTrue(payload.isSynced)
        assertEquals(listOf(10_000L, 10_000L, 12_000L), payload.lines.map { it.startTimeMs })
        assertEquals(listOf(11_999L, 11_999L, null), payload.lines.map { it.endTimeMs })
    }

    @Test
    fun embeddedTextKeepsSyncedSourceInsteadOfFlatteningTiming() {
        val raw = "[00:01.00]First line\r\n[00:02.00]Second line\n\n\n"
        val payload = requireNotNull(
            parseLrcOrPlain(
                raw = raw,
                providerName = "test",
                confidence = 90,
            ),
        )

        assertTrue(payload.isSynced)
        assertEquals("[00:01.00]First line\n[00:02.00]Second line", payload.toEmbeddedLyricsText())
    }

    @Test
    fun displayPayloadStripsTimestampMarkersButKeepsEmbeddingSource() {
        val payload = requireNotNull(
            parseLrcOrPlain(
                raw = "[00:01.00]First line\n[00:02.00]Second line",
                providerName = "test",
                confidence = 90,
            ),
        ).copy(
            lines = listOf(
                LyricsLine(text = "[00:01.00]First line", startTimeMs = 1_000L, index = 0),
                LyricsLine(text = "[00:02.00]Second line", startTimeMs = 2_000L, index = 1),
            ),
        )

        val displayPayload = payload.toDisplayPayload()

        assertEquals(listOf("First line", "Second line"), displayPayload.lines.map { it.text })
        assertEquals("[00:01.00]First line\n[00:02.00]Second line", displayPayload.toEmbeddedLyricsText())
    }

    @Test
    fun rejectsLyricsBeyondCharacterAndLineLimits() {
        assertNull(parseLrcOrPlain("x".repeat(MAX_LYRICS_CHARACTERS + 1), null, 0))
        assertNull(parseLrcOrPlain("x\n".repeat(MAX_LYRICS_LINES + 1), null, 0))
        assertNull(parseLrcOrPlain("x".repeat(16 * 1024 + 1), null, 0))
    }

    @Test
    fun rejectsTimestampFanOutBeyondPerLineLimit() {
        val timestamps = (0..64).joinToString("") { "[00:01.00]" }
        assertNull(parseLrcOrPlain("${timestamps}line", null, 0))
    }
}
