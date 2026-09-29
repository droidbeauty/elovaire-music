package elovaire.music.droidbeauty.app.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalLyricsResolverTest {
    @Test
    fun sidecarCandidatesKeepUntrustedNamesWithinTheAudioDirectory() {
        val candidates = lyricsSidecarBaseNames(
            localFileName = "track",
            songFileName = "../private/track:live.mp3",
            title = "../private/title",
        )

        assertEquals(listOf("track", "..privatetrack:live", "..privatetitle"), candidates)
        assertTrue(candidates.none { '/' in it || '\\' in it })
        assertTrue(candidates.none { it == "." || it == ".." })
    }

    @Test
    fun temporaryCopyUsesOnlyAPlainFileExtension() {
        assertEquals("flac", lyricsTemporaryFileExtension("track.flac"))
        assertEquals("tmp", lyricsTemporaryFileExtension("track.mp3/../../private"))
        assertEquals("tmp", lyricsTemporaryFileExtension("track."))
    }
}
