package elovaire.music.app.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `parse synced lyrics falls back cleanly for malformed lines`() {
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
    fun `parse synced lyrics keeps long display lines intact`() {
        val parsed = parseSyncedLyrics(
            """
            [00:01.00]I'll fix these broken things, repair your broken wings
            [00:05.00]And make sure everything's alright
            """.trimIndent(),
        )

        assertNotNull(parsed)
        assertEquals(2, parsed!!.size)
        assertEquals("I'll fix these broken things, repair your broken wings", parsed[0].text)
        assertEquals("And make sure everything's alright", parsed[1].text)
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
    fun `parse plain lyrics keeps long lines intact`() {
        val parsed = parsePlainLyrics(
            "Into every inch of you because I know that's what you want me to do",
        )

        assertNotNull(parsed)
        assertEquals(1, parsed!!.size)
        assertEquals("Into every inch of you because I know that's what you want me to do", parsed[0].text)
    }

    @Test
    fun `flat metadata only plain lyrics are rejected`() {
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
    fun `sanitize lyric line filters known remote garbage`() {
        assertNull(sanitizeLyricLine("TranslationsFrançaisTürkçePortuguês"))
        assertNull(sanitizeLyricLine("You might also like"))
        assertEquals("Actual lyric", sanitizeLyricLine("  Actual lyric  "))
    }

    @Test
    fun `current line index follows corrected playback position without early jump`() {
        val payload = LyricsPayload(
            lines = listOf(
                LyricsLine(text = "Line one", startTimeMs = 1_000L, endTimeMs = 4_000L),
                LyricsLine(text = "Line two", startTimeMs = 4_000L, endTimeMs = 7_000L),
                LyricsLine(text = "Line three", startTimeMs = 7_000L, endTimeMs = null),
            ),
            isSynced = true,
        )

        assertNull(payload.currentLineIndexAt(positionMs = 800L, switchGraceMs = 90L))
        assertEquals(0, payload.currentLineIndexAt(positionMs = 1_140L, switchGraceMs = 90L))
        assertEquals(1, payload.currentLineIndexAt(positionMs = 4_120L, switchGraceMs = 90L))
        assertEquals(2, payload.currentLineIndexAt(positionMs = 8_000L, switchGraceMs = 90L))
    }

    @Test
    fun `current line index respects display timing offset`() {
        val payload = LyricsPayload(
            lines = listOf(
                LyricsLine(text = "Late line", startTimeMs = 2_000L, endTimeMs = 5_000L),
            ),
            isSynced = true,
            displayTimingOffsetMs = 600L,
        )

        assertNull(payload.currentLineIndexAt(positionMs = 2_500L, switchGraceMs = 0L))
        assertEquals(0, payload.currentLineIndexAt(positionMs = 2_650L, switchGraceMs = 0L))
    }

    @Test
    fun `parse synced lyrics handles empty instrumental timed line safely`() {
        val parsed = parseSyncedLyrics(
            """
            [00:01.00]
            [00:03.00]First vocal line
            """.trimIndent(),
        )

        assertNotNull(parsed)
        assertEquals(1, parsed!!.size)
        assertEquals("First vocal line", parsed[0].text)
    }

    @Test
    fun `normalize for match removes diacritics`() {
        assertEquals("francais turkce portugues", "Français Türkçe Português".normalizeForMatch())
    }
}
