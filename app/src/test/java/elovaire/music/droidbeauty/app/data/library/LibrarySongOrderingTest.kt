package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.testing.testSong
import org.junit.Assert.assertEquals
import org.junit.Test

class LibrarySongOrderingTest {
    @Test
    fun equalDateAddedSongsUseStableIdentityAsTieBreaker() {
        val first = testSong(id = 1L)
        val second = testSong(id = 2L)

        assertEquals(listOf(first, second), sortLibrarySongs(listOf(second, first)))
    }
}
