package elovaire.music.droidbeauty.app.data.library.network

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal suspend fun <T, R> mapNetworkScanWork(
    items: List<T>,
    parallelism: Int,
    transform: suspend (T) -> R,
): List<R> {
    require(parallelism > 0)
    currentCoroutineContext().ensureActive()
    if (parallelism == 1 || items.size <= 1) {
        return items.map { item ->
            currentCoroutineContext().ensureActive()
            transform(item)
        }
    }
    return coroutineScope {
        val nextIndex = AtomicInteger()
        val results = MutableList<R?>(items.size) { null }
        List(minOf(parallelism, items.size)) {
            async {
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val index = nextIndex.getAndIncrement()
                    if (index >= items.size) break
                    results[index] = transform(items[index])
                }
            }
        }.awaitAll()
        // Each slot is written once; awaiting every worker also propagates child cancellation.
        @Suppress("UNCHECKED_CAST")
        (results as List<R>)
    }
}
