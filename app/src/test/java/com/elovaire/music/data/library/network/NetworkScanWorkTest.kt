package elovaire.music.droidbeauty.app.data.library.network

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkScanWorkTest {
    @Test
    fun workersStayBoundedAndPreserveOrderAtLibraryScale() = runTest {
        for (size in listOf(1_000, 10_000, 50_000)) {
            val input = List(size) { it }
            val release = CompletableDeferred<Unit>()
            var active = 0
            var peak = 0
            val result = async {
                mapNetworkScanWork(input, parallelism = 2) {
                    active++
                    peak = maxOf(peak, active)
                    try {
                        release.await()
                        if (it % 2 == 0) delay(1)
                        it
                    } finally {
                        active--
                    }
                }
            }
            testScheduler.runCurrent()
            val tasks = result.children.single().children.count()
            assertEquals(2, tasks)
            release.complete(Unit)
            assertEquals(input, result.await())
            assertEquals(2, peak)
            assertEquals(0, active)
            assertTrue(result.children.none())
            println("NetworkScanWork candidate entries=$size outstandingTasks=$tasks peakReads=$peak")
        }
    }

    @Test
    fun cancellationStopsWorkersWithoutStartingRemainingEntries() = runTest {
        var started = 0
        var released = 0
        val result = async {
            mapNetworkScanWork(List(50_000) { it }, 2) {
                started++
                try {
                    awaitCancellation()
                } finally {
                    released++
                }
            }
        }
        testScheduler.runCurrent()
        result.cancelAndJoin()
        assertEquals(2, started)
        assertEquals(2, released)
        assertTrue(result.children.none())
    }

    @Test
    fun childCancellationIsNotReturnedAsPartialResults() = runTest {
        val result = async {
            mapNetworkScanWork(listOf(1, 2, 3), 2) {
                if (it == 1) throw CancellationException("metadata cancelled")
                awaitCancellation()
            }
        }
        try {
            result.await()
            fail("Cancellation must propagate")
        } catch (cancelled: CancellationException) {
            assertEquals("metadata cancelled", cancelled.message)
        }
        assertTrue(result.children.none())
    }

    @Test
    fun failureStopsSiblingsAndCanBeIsolatedBySource() = runTest {
        supervisorScope {
            var released = false
            val started = CompletableDeferred<Unit>()
            val result = async {
                mapNetworkScanWork(listOf(1, 2, 3), 2) {
                    if (it == 1) {
                        started.await()
                        error("source superseded")
                    }
                    try {
                        started.complete(Unit)
                        awaitCancellation()
                    } finally {
                        released = true
                    }
                }
            }
            try {
                result.await()
                fail("Failure must propagate")
            } catch (failure: IllegalStateException) {
                assertEquals("source superseded", failure.message)
            }
            assertTrue(released)
            assertTrue(result.children.none())
            assertEquals(listOf(4, 5), mapNetworkScanWork(listOf(4, 5), 2) { it })
        }
    }

    @Test
    fun serialEmptyAndNullableResultsArePreserved() = runTest {
        assertEquals(emptyList<Int>(), mapNetworkScanWork(emptyList<Int>(), 2) { it })
        assertEquals(listOf(1), mapNetworkScanWork(listOf(1), 2) { it })
        assertEquals(listOf(1, 2, 3), mapNetworkScanWork(listOf(1, 2, 3), 1) { it })
        assertEquals(listOf(null, null), mapNetworkScanWork(listOf(1, 2), 2) { null })
        try {
            mapNetworkScanWork(emptyList<Int>(), 0) { it }
            fail("Zero parallelism must be rejected")
        } catch (_: IllegalArgumentException) {
            // Invalid admission cannot hang the scan.
        }
    }

    @Test
    fun queuedTaskBaselineAtLibraryScale() = runTest {
        for (size in listOf(1_000, 10_000, 50_000)) {
            val input = List(size) { it }
            val release = CompletableDeferred<Unit>()
            var active = 0
            val result = async {
                queuedMap(input, parallelism = 2) {
                    active++
                    release.await()
                    it
                }
            }
            testScheduler.runCurrent()
            val tasks = result.children.single().children.count()
            assertEquals(2, active)
            assertEquals(size, tasks)
            println("NetworkScanWork baseline entries=$size outstandingTasks=$tasks activeReads=$active")
            release.complete(Unit)
            assertEquals(input, result.await())
        }
    }

    private suspend fun <T, R> queuedMap(
        items: List<T>,
        parallelism: Int,
        transform: suspend (T) -> R,
    ): List<R> = supervisorScope {
        val permits = Semaphore(parallelism)
        items.map { item -> async { permits.withPermit { transform(item) } } }.awaitAll()
    }
}
