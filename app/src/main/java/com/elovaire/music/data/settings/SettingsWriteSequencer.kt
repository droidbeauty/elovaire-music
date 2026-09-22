package elovaire.music.droidbeauty.app.data.settings

import android.os.SystemClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal enum class SettingsWriteAdmission {
    Accepted,
    RejectedClosed,
}

internal sealed interface SettingsDrainResult {
    data object Drained : SettingsDrainResult
    data class Failed(val cause: Throwable?) : SettingsDrainResult
    data class TimedOut(val cause: Throwable?) : SettingsDrainResult
}

/** Serializes settings writes and keeps bursty controls bounded by key. */
internal class SettingsWriteSequencer(
    ownerScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val persist: suspend (String, suspend () -> Unit) -> Unit,
    private val onWriteFailure: (String, Throwable) -> Unit = { _, _ -> },
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
    private var lastFailure: Throwable? = null
    private var closeWatchdog: Job? = null
    private val closeResult = CompletableDeferred<SettingsDrainResult>()
    private val workerScope = CoroutineScope(
        ownerScope.coroutineContext.minusKey(Job) +
            SupervisorJob() +
            dispatcher +
            CoroutineName("settings-persistence"),
    )
    private val worker: Job = workerScope.launch(start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) {
        runWorker()
    }

    init {
        worker.invokeOnCompletion { cause ->
            closeWatchdog?.cancel()
            if (closed) {
                val result = when {
                    cause is CancellationException -> SettingsDrainResult.TimedOut(cause)
                    cause != null -> SettingsDrainResult.Failed(cause)
                    else -> synchronized(lock) {
                        lastFailure?.let(SettingsDrainResult::Failed)
                            ?: SettingsDrainResult.Drained
                    }
                }
                closeResult.complete(result)
            }
            workerScope.cancel()
        }
    }

    fun enqueue(key: String, write: suspend () -> Unit): SettingsWriteAdmission {
        synchronized(lock) {
            if (closed) return SettingsWriteAdmission.RejectedClosed
            check(ordered.size < MAX_ORDERED_WRITES) { "Settings write queue is full." }
            ordered += PendingWrite(key, nowMs(), write)
            signalLocked()
            return SettingsWriteAdmission.Accepted
        }
    }

    fun replaceLatest(
        key: String,
        debounceMs: Long = 0L,
        write: suspend () -> Unit,
    ): SettingsWriteAdmission {
        synchronized(lock) {
            if (closed) return SettingsWriteAdmission.RejectedClosed
            latest[key] = PendingWrite(key, nowMs() + debounceMs.coerceAtLeast(0L), write)
            signalLocked()
            return SettingsWriteAdmission.Accepted
        }
    }

    fun hasPendingWork(): Boolean = synchronized(lock) {
        ordered.isNotEmpty() || latest.isNotEmpty() || writing
    }

    /** Returns false when at least one accepted write failed while draining. */
    suspend fun flush(): Boolean {
        var failure: Throwable?
        while (true) {
            val waiter = synchronized(lock) {
                if (ordered.isEmpty() && latest.isEmpty() && !writing) {
                    null
                } else {
                    idleWaiter ?: CompletableDeferred<Unit>().also { idleWaiter = it }
                }
            } ?: break
            signal()
            waiter.await()
        }
        failure = synchronized(lock) { lastFailure.also { lastFailure = null } }
        return failure == null
    }

    /** Requests shutdown and returns the asynchronous durability outcome. */
    fun close(): Deferred<SettingsDrainResult> {
        synchronized(lock) {
            if (closed) return closeResult
            closed = true
            signalLocked()
            closeWatchdog = workerScope.launch {
                delay(CLOSE_DRAIN_TIMEOUT_MS)
                if (worker.isActive) worker.cancel(CloseDrainTimeoutException())
            }
        }
        return closeResult
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
                synchronized(lock) { lastFailure = failure }
                try {
                    onWriteFailure(next.key, failure)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: RuntimeException) {
                    // Diagnostics must not prevent later independent writes from draining.
                }
            }
            markIdleIfNeeded()
        }
    }

    private suspend fun awaitWork(): PendingWrite? {
        while (true) {
            val waitMs: Long?
            val signal: CompletableDeferred<Unit>
            synchronized(lock) {
                if (closed && ordered.isEmpty() && latest.isEmpty() && !writing) return null
                val nextReadyAt = latest.values.minOfOrNull(PendingWrite::readyAtMs)
                waitMs = if (closed) {
                    0L
                } else {
                    nextReadyAt?.let { (it - nowMs()).coerceAtLeast(1L) }
                }
                signal = wake
            }
            if (waitMs == null) {
                signal.await()
            } else if (waitMs > 0L) {
                withTimeoutOrNull(waitMs) { signal.await() }
            } else {
                takeReadyWrite()?.let { return it }
            }
            takeReadyWrite()?.let { return it }
        }
    }

    private fun takeReadyWrite(): PendingWrite? = synchronized(lock) {
        val orderedWrite = ordered.firstOrNull()
        if (orderedWrite != null) {
            ordered.removeFirst()
            writing = true
            return@synchronized orderedWrite
        }
        val now = nowMs()
        val entry = latest.entries.minByOrNull { it.value.readyAtMs } ?: return@synchronized null
        if (!closed && entry.value.readyAtMs > now) return@synchronized null
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
        const val CLOSE_DRAIN_TIMEOUT_MS = 15_000L
    }

    private class CloseDrainTimeoutException : CancellationException("Settings write drain timed out.")
}
