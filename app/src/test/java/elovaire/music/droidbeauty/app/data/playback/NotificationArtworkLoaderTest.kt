package elovaire.music.droidbeauty.app.data.playback

import kotlinx.coroutines.Job
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
}
