package elovaire.music.droidbeauty.app.data.settings

import android.os.SystemClock
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Serializes settings writes and keeps bursty controls bounded by key. */
internal class SettingsWriteSequencer(
    ownerScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val persist: suspend (String, suspend () -> Unit) -> Unit,
    private val nowMs: () -> Long = SystemClock::elapsedRealtime,
) {
    private data class PendingWrite(
        val key: String,
        val readyAtMs: Long,
        val write: suspend () -> Unit,
    )

    private val lock = Any()
    private val ordered = ArrayDeque<PendingWrite>()
    private val latest = LinkedHashMap<String, PendingWrite>()
    private var wake = CompletableDeferred<Unit>()
    private var idleWaiter: CompletableDeferred<Unit>? = null
    private var writing = false
    private var closed = false
    private val worker: Job = CoroutineScope(
        ownerScope.coroutineContext + dispatcher + CoroutineName("settings-persistence"),
    ).launch(start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) { runWorker() }

    fun enqueue(key: String, write: suspend () -> Unit) {
        synchronized(lock) {
            if (closed) return
            check(ordered.size < MAX_ORDERED_WRITES) { "Settings write queue is full." }
            ordered += PendingWrite(key, nowMs(), write)
            signalLocked()
        }
    }

    fun replaceLatest(
        key: String,
        debounceMs: Long = 0L,
        write: suspend () -> Unit,
    ) {
        synchronized(lock) {
            if (closed) return
            latest[key] = PendingWrite(key, nowMs() + debounceMs.coerceAtLeast(0L), write)
            signalLocked()
        }
    }

    fun hasPendingWork(): Boolean = synchronized(lock) {
        ordered.isNotEmpty() || latest.isNotEmpty() || writing
    }

    suspend fun flush() {
        while (true) {
            val waiter = synchronized(lock) {
                if (ordered.isEmpty() && latest.isEmpty() && !writing) {
                    null
                } else {
                    idleWaiter ?: CompletableDeferred<Unit>().also { idleWaiter = it }
                }
            } ?: return
            signal()
            waiter.await()
        }
    }

    fun close() {
        synchronized(lock) {
            if (closed) return
            closed = true
            ordered.clear()
            latest.clear()
            signalLocked()
            idleWaiter?.complete(Unit)
            idleWaiter = null
        }
        worker.cancel()
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun runWorker() {
        while (true) {
            val next = takeReadyWrite() ?: awaitWork()
            if (next == null) return
            try {
                persist(next.key, next.write)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                BackendDiagnostics.recordWorkerFailure("settings-persistence", failure)
            }
            markIdleIfNeeded()
        }
    }

    private suspend fun awaitWork(): PendingWrite? {
        while (true) {
            val waitMs: Long?
            val signal: CompletableDeferred<Unit>
            synchronized(lock) {
                if (closed) return null
                val nextReadyAt = latest.values.minOfOrNull(PendingWrite::readyAtMs)
                waitMs = nextReadyAt?.let { (it - nowMs()).coerceAtLeast(1L) }
                signal = wake
            }
            if (waitMs == null) {
                signal.await()
            } else {
                withTimeoutOrNull(waitMs) { signal.await() }
            }
            takeReadyWrite()?.let { return it }
        }
    }

    private fun takeReadyWrite(): PendingWrite? = synchronized(lock) {
        if (closed) return@synchronized null
        val orderedWrite = ordered.firstOrNull()
        if (orderedWrite != null) {
            ordered.removeFirst()
            writing = true
            return@synchronized orderedWrite
        }
        val now = nowMs()
        val entry = latest.entries.minByOrNull { it.value.readyAtMs } ?: return@synchronized null
        if (entry.value.readyAtMs > now) return@synchronized null
        latest.remove(entry.key)
        writing = true
        entry.value
    }

    private fun markIdleIfNeeded() {
        synchronized(lock) {
            writing = false
            if (ordered.isEmpty() && latest.isEmpty()) {
                idleWaiter?.complete(Unit)
                idleWaiter = null
            }
        }
    }

    private fun signal() {
        synchronized(lock) { signalLocked() }
    }

    private fun signalLocked() {
        val previous = wake
        wake = CompletableDeferred()
        previous.complete(Unit)
    }

    private companion object {
        const val MAX_ORDERED_WRITES = 64
    }
}
