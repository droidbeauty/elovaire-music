package elovaire.music.droidbeauty.app.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CoroutineFailuresTest {
    @Test
    fun cancellationIsNeverConvertedToResultFailure() {
        assertThrows(CancellationException::class.java) {
            runBlocking {
                runSuspendCatching<Unit> { throw CancellationException("cancelled") }
            }
        }
    }

    @Test
    fun operationalFailureRemainsAvailableToCaller() = runBlocking {
        val result = runSuspendCatching<Unit> { error("failed") }

        assertEquals("failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun fatalErrorsAreNotConvertedToOperationalFailures() {
        assertThrows(OutOfMemoryError::class.java) {
            runBlocking {
                runSuspendCatching<Unit> { throw OutOfMemoryError("fatal") }
            }
        }
    }
}
