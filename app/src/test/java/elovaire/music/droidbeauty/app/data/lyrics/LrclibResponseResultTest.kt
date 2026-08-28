package elovaire.music.droidbeauty.app.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Test

class LrclibResponseResultTest {
    @Test
    fun retryAfterIsPreservedForRateLimitedResponses() {
        assertEquals(
            LyricsResult.RateLimited(30_000L),
            lrclibResponseResult(statusCode = 429, retryAfterMs = 30_000L),
        )
    }

    @Test
    fun responseClassesRemainDistinct() {
        assertEquals(LyricsResult.NotFound, lrclibResponseResult(404, null))
        assertEquals(LyricsResult.Timeout, lrclibResponseResult(503, null))
        assertEquals(LyricsResult.Rejected(403), lrclibResponseResult(403, null))
        assertEquals(LyricsResult.Unavailable, lrclibResponseResult(302, null))
    }
}
