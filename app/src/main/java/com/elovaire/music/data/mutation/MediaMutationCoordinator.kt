package elovaire.music.droidbeauty.app.data.mutation

import android.net.Uri
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Serializes destructive media replacement per target while allowing independent files to proceed. */
internal class MediaMutationRuntime : Closeable {
    private val registryLock = Any()
    private val targetLocks = mutableMapOf<String, TargetLock>()
    private val released = AtomicBoolean(false)

    suspend fun <T> withTarget(
        uri: Uri,
        block: suspend () -> T,
    ): T = withTargets(listOf(uri), block)

    suspend fun <T> withTargets(
        uris: Collection<Uri>,
        block: suspend () -> T,
    ): T {
        check(!released.get()) { "Media mutation runtime is released." }
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
        val targetLock = synchronized(registryLock) {
            targetLocks.getOrPut(key, ::TargetLock).also { it.references += 1 }
        }
        return try {
            targetLock.mutex.withLock {
                withTargetLocks(keys, index + 1, block)
            }
        } finally {
            synchronized(registryLock) {
                targetLock.references -= 1
                if (targetLock.references == 0) {
                    targetLocks.remove(key, targetLock)
                }
            }
        }
    }

    internal fun activeTargetLockCount(): Int = synchronized(registryLock) { targetLocks.size }

    override fun close() {
        if (!released.compareAndSet(false, true)) return
        synchronized(registryLock) {
            targetLocks.entries.removeIf { it.value.references == 0 }
        }
    }

    private class TargetLock {
        val mutex = Mutex()
        var references: Int = 0
    }
}

/** Compatibility facade for isolated callers that have not joined the app composition graph. */
@Deprecated("Use an app-scoped MediaMutationRuntime")
internal object MediaMutationCoordinator {
    private val runtime = MediaMutationRuntime()

    suspend fun <T> withTarget(uri: Uri, block: suspend () -> T): T = runtime.withTarget(uri, block)

    suspend fun <T> withTargets(uris: Collection<Uri>, block: suspend () -> T): T =
        runtime.withTargets(uris, block)

    internal fun activeTargetLockCount(): Int = runtime.activeTargetLockCount()
}
