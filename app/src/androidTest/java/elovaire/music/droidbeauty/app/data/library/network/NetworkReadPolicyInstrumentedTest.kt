package elovaire.music.droidbeauty.app.data.library.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.ByteArrayInputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkReadPolicyInstrumentedTest {
    @Test
    fun networkPathsPreserveWhitespaceThatBelongsToFileNames() {
        assertEquals(" Music / /song.mp3 ", NetworkPathPolicy.normalizeRelativePath(" Music / /song.mp3 "))
        assertEquals("Music/ ", NetworkPathPolicy.join("Music", " "))
        assertEquals("https://nas.example/base/song.mp3%20",
            NetworkPathPolicy.webDavResourceUrl("https://nas.example/base", "song.mp3 ").toString())
        val uri = NetworkResourceUri.create("fixture", "Music/song.mp3 ")
        assertEquals("Music/song.mp3 ", NetworkResourceUri.path(uri))
    }

    @Test
    fun boundedReadsKeepTheirLimitAcrossMarkResetAndAvailable() {
        limitWebDavRead(ByteArrayInputStream(byteArrayOf(1, 2, 3, 4)), 2L).use { input ->
            assertTrue(input.markSupported())
            input.mark(4)
            assertEquals(1, input.read())
            input.reset()
            assertArrayEquals(byteArrayOf(1, 2), input.readBytes())
            assertEquals(0, input.available())
            assertEquals(-1, input.read())
        }
    }
}
