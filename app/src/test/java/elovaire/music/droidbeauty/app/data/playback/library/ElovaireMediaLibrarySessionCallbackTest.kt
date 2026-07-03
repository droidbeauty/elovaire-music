package elovaire.music.droidbeauty.app.data.playback.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ElovaireMediaLibrarySessionCallbackTest {
    @Test
    fun emptyMediaItemsWithStartPosition_returnsCleanUnavailableQueue() {
        val result = emptyMediaItemsWithStartPosition()

        assertTrue(result.mediaItems.isEmpty())
        assertEquals(0, result.startIndex)
        assertEquals(0L, result.startPositionMs)
    }
}
