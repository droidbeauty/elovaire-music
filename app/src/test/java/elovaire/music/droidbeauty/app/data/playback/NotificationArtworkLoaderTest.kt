package elovaire.music.droidbeauty.app.data.playback

import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationArtworkLoaderTest {
    @Test
    fun completionOfCancelledLoadDoesNotRemoveItsReplacement() {
        val oldJob = Job()
        val replacement = Job()
        val pending = mutableMapOf<String, Job>("cover" to replacement)

        removePendingArtworkLoadIfCurrent(pending, "cover", oldJob)
        assertTrue(pending["cover"] === replacement)

        removePendingArtworkLoadIfCurrent(pending, "cover", replacement)
        assertFalse("cover" in pending)
    }

    @Test
    fun concurrentRequestsForOneArtworkKeepEveryCallback() {
        val pending = mutableMapOf<String, MutableList<String>>()

        appendPendingArtworkCallback(pending, "cover", "first")
        appendPendingArtworkCallback(pending, "cover", "second")

        assertEquals(listOf("first", "second"), pending["cover"])
    }
}
