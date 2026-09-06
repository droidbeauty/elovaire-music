package elovaire.music.droidbeauty.app.data.library.network

import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkScanWorkInstrumentedTest {
    @Test
    fun boundedWorkersReduceMaterializationForIdenticalOrderedWork() = runBlocking(Dispatchers.Default) {
        for (size in listOf(1_000, 10_000, 50_000)) {
            val input = List(size) { it }
            repeat(2) {
                queuedMap(input)
                mapNetworkScanWork(input, 2) { it + 1 }
            }
            val baselineTimes = mutableListOf<Double>()
            val candidateTimes = mutableListOf<Double>()
            val baselineBytes = mutableListOf<Long>()
            val candidateBytes = mutableListOf<Long>()
            repeat(5) { sample ->
                // Alternate the order to avoid always giving one path the warmed-up runtime.
                for (baseline in if (sample % 2 == 0) listOf(true, false) else listOf(false, true)) {
                    val beforeBytes = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
                    val beforeTime = System.nanoTime()
                    val result = if (baseline) queuedMap(input) else mapNetworkScanWork(input, 2) { it + 1 }
                    val elapsedMs = (System.nanoTime() - beforeTime) / 1_000_000.0
                    val allocated = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong() - beforeBytes
                    (if (baseline) baselineTimes else candidateTimes).add(elapsedMs)
                    (if (baseline) baselineBytes else candidateBytes).add(allocated)
                    assertEquals(size, result.size)
                    result.forEachIndexed { index, value -> assertEquals(index + 1, value) }
                }
            }
            Log.i("NetworkScanWork", "entries=$size baselineMs=$baselineTimes candidateMs=$candidateTimes")
            Log.i("NetworkScanWork", "entries=$size baselineBytes=$baselineBytes candidateBytes=$candidateBytes")
        }
    }

    private suspend fun queuedMap(input: List<Int>): List<Int> = supervisorScope {
        val permits = Semaphore(2)
        input.map { value -> async { permits.withPermit { value + 1 } } }.awaitAll()
    }
}
