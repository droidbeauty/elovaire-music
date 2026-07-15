package elovaire.music.droidbeauty.app.data.tags.matching

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal class FingerprintExecutionGate(maxConcurrentJobs: Int = DEFAULT_CONCURRENCY) {
    private val semaphore = Semaphore(maxConcurrentJobs.coerceIn(1, MAX_CONCURRENCY))

    suspend fun <T> run(block: suspend () -> T): T = semaphore.withPermit { block() }

    private companion object {
        const val DEFAULT_CONCURRENCY = 2
        const val MAX_CONCURRENCY = 4
    }
}
