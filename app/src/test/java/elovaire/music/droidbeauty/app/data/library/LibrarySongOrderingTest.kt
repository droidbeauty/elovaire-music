package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.testing.testSong
import org.junit.Assert.assertEquals
import org.junit.Test

class LibrarySongOrderingTest {
    @Test
    fun equalDateAddedSongsUseStableIdentityAsTieBreaker() {
        val first = testSong(id = 1L)
        val second = testSong(id = 2L)
        val original = listOf(second, first)

        assertEquals(listOf(first, second), sortLibrarySongs(original))
        assertEquals(listOf(second, first), original)
    }
}
