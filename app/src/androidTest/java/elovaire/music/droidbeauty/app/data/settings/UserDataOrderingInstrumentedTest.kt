package elovaire.music.droidbeauty.app.data.settings

import kotlinx.coroutines.channels.Channel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserDataOrderingInstrumentedTest {
    @Test
    fun deferredCheckpointCannotOverwriteANewerCheckpointOrFollowARekey() {
        val queue = Channel<String>(1)
        val pending = linkedMapOf<String, String>()
        assertTrue(offerRoomOperation(queue, pending, "filler", "filler", true) {})
        assertTrue(offerRoomOperation(queue, pending, "book", "old", true) {})
        assertEquals("filler", queue.tryReceive().getOrNull())
        assertTrue(offerRoomOperation(queue, pending, "book", "new", true) {})
        assertFalse(offerRoomOperation(queue, pending, "rekey", "rekey", false) {})
        assertEquals("new", queue.tryReceive().getOrNull())
        assertTrue(offerRoomOperation(queue, pending, "rekey", "rekey", false) {})
    }

    @Test
    fun failedRecentWriteCannotReplaceANewerSnapshot() {
        val buffer = PlaybackHistoryWriteBuffer()
        val old = RecentPlaybackWrite(listOf(1L), emptyList(), null, null)
        val latest = RecentPlaybackWrite(listOf(2L), emptyList(), null, null)
        assertTrue(buffer.setRecent(old))
        assertEquals(old, buffer.takeRecent())
        assertTrue(buffer.setRecent(latest))
        buffer.restoreRecent(old)
        assertEquals(latest, buffer.takeRecent())
    }
}
