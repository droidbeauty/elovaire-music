package elovaire.music.droidbeauty.app.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Test

class EmbeddedLyricsMetadataTest {
    @Test
    fun plainTextUsesUnsyncedLyricsTarget() {
        assertEquals(
            EmbeddedLyricsTagKind.UnsyncedLyrics,
            classifyLyricsTagKind("First line\nSecond line"),
        )
    }

    @Test
    fun timestampedTextUsesSyncedLyricsTarget() {
        listOf("[00:12.34]Line", "[01:02]Line", "[1:02.345]Line").forEach { lyrics ->
            assertEquals(EmbeddedLyricsTagKind.SyncedLyrics, classifyLyricsTagKind(lyrics))
        }
    }

    @Test
    fun unrelatedBracketsDoNotUseSyncedLyricsTarget() {
        assertEquals(
            EmbeddedLyricsTagKind.UnsyncedLyrics,
            classifyLyricsTagKind("[Verse 1]\nFirst line"),
        )
    }
}
