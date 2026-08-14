package elovaire.music.droidbeauty.app.data.mutation

import android.net.Uri
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/** Serializes destructive media replacement per target while allowing independent files to proceed. */
internal object MediaMutationCoordinator {
    private val targetLocks = ConcurrentHashMap<String, TargetLock>()

    suspend fun <T> withTarget(
        uri: Uri,
        block: suspend () -> T,
    ): T = withTargets(listOf(uri), block)

    suspend fun <T> withTargets(
        uris: Collection<Uri>,
        block: suspend () -> T,
    ): T {
        val keys = uris.asSequence()
            .map(Uri::toString)
            .distinct()
            .sorted()
            .toList()
        return withTargetLocks(keys, 0, block)
    }

    private suspend fun <T> withTargetLocks(
        keys: List<String>,
        index: Int,
        block: suspend () -> T,
    ): T {
        if (index == keys.size) return block()
        val key = keys[index]
        val targetLock = targetLocks.compute(key) { _, existing ->
            (existing ?: TargetLock()).also { it.references.incrementAndGet() }
        } ?: error("Unable to allocate media mutation lock")
        return try {
            targetLock.mutex.withLock {
                withTargetLocks(keys, index + 1, block)
            }
        } finally {
            if (targetLock.references.decrementAndGet() == 0) {
                targetLocks.remove(key, targetLock)
            }
        }
    }

    private class TargetLock {
        val mutex = Mutex()
        val references = AtomicInteger()
    }
}
