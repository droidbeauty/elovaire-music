package elovaire.music.droidbeauty.app.data.playback.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import com.google.common.util.concurrent.SettableFuture
import org.junit.Test

class ElovaireMediaLibrarySessionCallbackTest {
    @Test
    fun emptyMediaItemsWithStartPosition_returnsCleanUnavailableQueue() {
        val result = emptyMediaItemsWithStartPosition()

        assertTrue(result.mediaItems.isEmpty())
        assertEquals(0, result.startIndex)
        assertEquals(0L, result.startPositionMs)
    }

    @Test
    fun completedSuccessfully_doesNotTreatFailureOrCancellationAsReadiness() {
        val failed = SettableFuture.create<Unit>().apply {
            setException(IllegalStateException("startup failed"))
        }
        val cancelled = SettableFuture.create<Unit>().apply { cancel(false) }
        val ready = SettableFuture.create<Unit>().apply { set(Unit) }

        assertFalse(failed.completedSuccessfully())
        assertFalse(cancelled.completedSuccessfully())
        assertTrue(ready.completedSuccessfully())
    }
}
