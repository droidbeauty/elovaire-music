package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.domain.kernel.normalizePlaybackQueue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackQueueInvariantsTest {
    @Test
    fun emptyQueueHasNoCurrentIndexOrPlaylistSource() {
        val normalized = normalizePlaybackQueue(
            queueSize = 0,
            currentIndex = 4,
            sourcePlaylistId = 9L,
        )

        assertEquals(-1, normalized.currentIndex)
        assertNull(normalized.sourcePlaylistId)
    }

    @Test
    fun nonEmptyQueueAlwaysHasValidCurrentIndex() {
        assertEquals(0, normalizePlaybackQueue(3, -1, null).currentIndex)
        assertEquals(0, normalizePlaybackQueue(3, 9, null).currentIndex)
        assertEquals(2, normalizePlaybackQueue(3, 2, null).currentIndex)
    }
}
