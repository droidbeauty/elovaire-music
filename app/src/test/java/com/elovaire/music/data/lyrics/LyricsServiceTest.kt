package elovaire.music.app.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LyricsServiceTest {

    @Test
    fun `parse synced lyrics handles metadata offsets and multiple timestamps`() {
        val parsed = parseSyncedLyrics(
            """
            [ar:Artist]
            [ti:Song]
            [offset:250]
            [00:01.00][00:02.50]First line
            [00:03.000]Second line
            """.trimIndent(),
        )

        assertNotNull(parsed)
        assertEquals(3, parsed!!.size)
        assertEquals(1250L, parsed[0].startTimeMs)
        assertEquals(2750L, parsed[1].startTimeMs)
        assertEquals(3250L, parsed[2].startTimeMs)
        assertEquals("First line", parsed[0].text)
    }

    @Test
    fun `parse synced lyrics falls back to plain lyrics when timestamps are missing`() {
        val parsed = parseSyncedLyrics(
            """
            [ar:Artist]
            This is plain
            Also plain
            """.trimIndent(),
        )

        assertNotNull(parsed)
        assertEquals(2, parsed!!.size)
        assertNull(parsed[0].startTimeMs)
        assertEquals("This is plain", parsed[0].text)
    }

    @Test
    fun `parse synced lyrics merges continuation lines that were split mid sentence`() {
        val parsed = parseSyncedLyrics(
            """
            [00:01.00]And her heart is breaking in front
            [00:02.40]of me
            [00:05.00]I have no choice
            """.trimIndent(),
        )

        assertNotNull(parsed)
        assertEquals(2, parsed!!.size)
        assertEquals("And her heart is breaking in front of me", parsed[0].text)
        assertEquals(1000L, parsed[0].startTimeMs)
        assertEquals(5000L, parsed[0].endTimeMs)
    }

    @Test
    fun `parse plain lyrics removes bom and garbage`() {
        val parsed = parsePlainLyrics(
            "\uFEFFTranslationsFrançais\nYou might also like\nLine one\nLine two",
        )

        assertNotNull(parsed)
        assertEquals(listOf("Line one", "Line two"), parsed!!.map { it.text })
    }

    @Test
    fun `metadata only plain lyrics are rejected`() {
        val parsed = parsePlainLyrics(
            """
            [ar:Artist]
            [ti:Song]
            [al:Album]
            """.trimIndent(),
        )

        assertNull(parsed)
    }

    @Test
    fun `normalize track title removes common lookup noise`() {
        assertEquals(
            "purpose",
            normalizeTrackTitle("Purpose (Deluxe Edition) feat. Chance the Rapper"),
        )
        assertEquals(
            "you know i m no good",
            normalizeTrackTitle("You Know I'm No Good - Remastered"),
        )
    }

    @Test
    fun `payload current line index follows exact synced timestamps`() {
        val payload = LyricsPayload(
            lines = listOf(
                LyricsLine(text = "Line one", startTimeMs = 1_000L, endTimeMs = 4_000L, index = 0),
                LyricsLine(text = "Line two", startTimeMs = 4_000L, endTimeMs = 7_000L, index = 1),
                LyricsLine(text = "Line three", startTimeMs = 7_000L, endTimeMs = null, index = 2),
            ),
            isSynced = true,
        )

        assertNull(payload.currentLineIndexAt(positionMs = 900L))
        assertEquals(0, payload.currentLineIndexAt(positionMs = 1_000L))
        assertEquals(1, payload.currentLineIndexAt(positionMs = 4_010L))
        assertEquals(2, payload.currentLineIndexAt(positionMs = 9_500L))
    }

    @Test
    fun `unsynced payload never returns a highlighted line`() {
        val payload = LyricsPayload(
            lines = listOf(
                LyricsLine(text = "Plain line", startTimeMs = null, index = 0),
            ),
            isSynced = false,
        )

        assertNull(payload.currentLineIndexAt(positionMs = 10_000L))
    }
}
