package elovaire.music.droidbeauty.app.data.settings

import kotlinx.coroutines.channels.Channel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomOperationOrderingTest {
    @Test
    fun newerCoalescedWriteCannotBeOvertakenByOlderDeferredWrite() {
        val queue = Channel<String>(1)
        val pending = linkedMapOf<String, String>()
        fun offer(name: String, value: String) = offerRoomOperation(queue, pending, name, value, true) {}
        assertTrue(offer("filler", "filler"))
        assertTrue(offer("book", "old"))
        assertEquals("filler", queue.tryReceive().getOrNull())
        assertTrue(offer("book", "new"))
        assertTrue(queue.tryReceive().isFailure)
        assertEquals(listOf("new"), pending.values.toList())
    }

    @Test
    fun durableOperationCannotOvertakePendingWrites() {
        val queue = Channel<String>(1)
        val pending = linkedMapOf<String, String>()
        assertTrue(offerRoomOperation(queue, pending, "filler", "filler", true) {})
        assertTrue(offerRoomOperation(queue, pending, "book", "checkpoint", true) {})
        assertEquals("filler", queue.tryReceive().getOrNull())
        assertFalse(offerRoomOperation(queue, pending, "rekey", "rekey", false) {})
        assertEquals("checkpoint", queue.tryReceive().getOrNull())
        assertTrue(offerRoomOperation(queue, pending, "rekey", "rekey", false) {})
        assertEquals("rekey", queue.tryReceive().getOrNull())
    }

    @Test
    fun replacingDeferredValueMovesItAfterOtherPendingKeys() {
        val queue = Channel<String>(1)
        val pending = linkedMapOf<String, String>()
        assertTrue(offerRoomOperation(queue, pending, "filler", "filler", true) {})
        assertTrue(offerRoomOperation(queue, pending, "a", "a1", true) {})
        assertTrue(offerRoomOperation(queue, pending, "b", "b1", true) {})
        assertTrue(offerRoomOperation(queue, pending, "a", "a2", true) {})
        assertEquals(listOf("b1", "a2"), pending.values.toList())
    }
}
