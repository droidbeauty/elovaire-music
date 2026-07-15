package elovaire.music.droidbeauty.app.data.tags.matching

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FingerprintExecutionGateTest {
    @Test
    fun boundsConcurrentNativeWork() = runBlocking {
        val gate = FingerprintExecutionGate(maxConcurrentJobs = 2)
        val active = AtomicInteger()
        val maximum = AtomicInteger()
        val admitted = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()

        List(4) {
            async {
                gate.run {
                    val running = active.incrementAndGet()
                    maximum.updateAndGet { current -> maxOf(current, running) }
                    if (running == 2) admitted.complete(Unit)
                    release.await()
                    active.decrementAndGet()
                }
            }
        }.also { jobs ->
            admitted.await()
            assertEquals(2, maximum.get())
            release.complete(Unit)
            jobs.awaitAll()
        }
        Unit
    }
}
