package elovaire.music.droidbeauty.app.data.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateDownloadProgressThrottlerTest {
    @Test
    fun emitsMeaningfulProgressAndCompletion() {
        val throttler = UpdateDownloadProgressThrottler()
        assertTrue(throttler.shouldEmit(0f, 0L))
        assertFalse(throttler.shouldEmit(0.001f, 10L))
        assertTrue(throttler.shouldEmit(0.02f, 20L))
        assertTrue(throttler.shouldEmit(1f, 30L))
    }
}
