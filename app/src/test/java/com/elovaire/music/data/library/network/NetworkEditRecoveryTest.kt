package elovaire.music.droidbeauty.app.data.library.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkEditRecoveryTest {
    @Test
    fun retriesAfterConnectivityChangeEvenWhenPreviousAttemptFailed() = runTest {
        val generations = MutableStateFlow(0L)
        var attempts = 0
        var failures = 0
        val job = launch {
            recoverNetworkEditsOnChanges(generations, {
                attempts++
                if (attempts == 1) throw IllegalStateException("temporarily unavailable")
            }) { failures++ }
        }
        runCurrent()
        assertEquals(1, attempts)
        assertEquals(1, failures)

        generations.value = 2L
        runCurrent()
        assertEquals(2, attempts)
        assertEquals(1, failures)
        job.cancelAndJoin()
    }

    @Test
    fun reconnectDuringRecoveryDoesNotCancelAnInFlightWrite() = runTest {
        val generations = MutableStateFlow(0L)
        val release = CompletableDeferred<Unit>()
        var attempts = 0
        var completed = 0
        val job = launch {
            recoverNetworkEditsOnChanges(generations, {
                attempts++
                if (attempts == 1) release.await()
                completed++
            }) { throw it }
        }
        runCurrent()
        generations.value = 1L
        runCurrent()
        assertEquals(1, attempts)
        assertEquals(0, completed)
        release.complete(Unit)
        runCurrent()
        assertEquals(2, attempts)
        assertEquals(2, completed)
        job.cancelAndJoin()
    }
}
