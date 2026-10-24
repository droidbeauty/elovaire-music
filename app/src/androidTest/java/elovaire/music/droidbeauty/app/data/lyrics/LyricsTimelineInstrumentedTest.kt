package elovaire.music.droidbeauty.app.data.lyrics

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LyricsTimelineInstrumentedTest {
    @Test
    fun blankTimedCuesEndThePreviousLineDuringInstrumentalBreaks() {
        val payload = requireNotNull(parseLrcOrPlain("[00:01]First\n[00:02][Chorus]\n[00:05]\n[00:20]Next\n[00:25]"))
        assertEquals(listOf("First", "Next"), payload.lines.map(LyricsLine::text))
        assertEquals(listOf(4_999L, 24_999L), payload.lines.map(LyricsLine::endTimeMs))
        assertNull(payload.currentLineIndexAtFast(10_000L, switchGraceMs = 0L))
        assertNull(payload.currentLineIndexAtFast(30_000L, switchGraceMs = 0L))
        assertEquals(1, payload.currentLineIndexAtFast(21_000L, switchGraceMs = 0L))
        val plain = requireNotNull(parseLrcOrPlain("[00:01]\nPlain lyrics"))
        assertFalse(plain.isSynced)
        assertEquals(listOf("Plain lyrics"), plain.lines.map(LyricsLine::text))
    }

    @Test
    fun changedAlbumMetadataCannotReuseAnotherAlbumsLyricsCache() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val song = Song(
            id = 901L, title = "Shared title", isExplicit = false, artist = "Artist", album = "Studio",
            releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null,
            fileName = "fixture.mp3", albumId = 1L, durationMs = 180_000L, trackNumber = 1,
            discNumber = 1, dateAddedSeconds = 1L,
            uri = Uri.parse("content://lyrics-fixture/${System.nanoTime()}"), artUri = null,
        )
        val original = song.toLyricsIdentity()
        val edited = song.copy(album = "Live").toLyricsIdentity()
        val cache = LyricsCache(context)
        try {
            cache.put(original, LyricsCacheEntry(
                LyricsResult.Found(requireNotNull(parseLrcOrPlain("Studio lyrics"))),
                System.currentTimeMillis() + 60_000L, online = true,
            ))
            assertNotEquals(original.cacheKeys, edited.cacheKeys)
            assertNull(cache.get(edited, includeNotFound = true))
        } finally {
            cache.remove(original)
            cache.remove(edited)
        }
    }
}
