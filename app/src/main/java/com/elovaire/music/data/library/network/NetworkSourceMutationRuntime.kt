package elovaire.music.droidbeauty.app.data.library.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import elovaire.music.droidbeauty.app.core.backend.BackendFailure
import elovaire.music.droidbeauty.app.core.backend.BackendFailureDisposition
import elovaire.music.droidbeauty.app.core.backend.classifyBackendFailure
import java.util.concurrent.atomic.AtomicBoolean

/** Owns the lifetime and stale-result handling for asynchronous source mutations. */
@Suppress("TooGenericExceptionCaught")
internal class NetworkSourceMutationRuntime(
    private val scope: CoroutineScope,
    private val coordinator: NetworkSourceMutationBackend,
    private val onResult: (NetworkSourceMutationResult) -> Unit,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val released = AtomicBoolean(false)
    private val stateLock = Any()
    private val active = mutableMapOf<String, MutationToken>()
    private val sourceSlots = mutableMapOf<String, SourceSlot>()

    fun save(source: NetworkLibrarySource, credentials: NetworkCredentials) {
        if (released.get()) return
        launch(source.id) { token ->
            try {
                if (!runIfCurrent(source.id, token) {
                    onResult(NetworkSourceMutationResult.Checking(source.id))
                }) return@launch
                val outcome = try {
                    token.slot.mutex.withLock { coordinator.save(source, credentials) }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (failure: Exception) {
                    recordFailure(source.id, token, failure)
                    return@launch
                }
                runIfCurrent(source.id, token) {
                    onResult(
                        NetworkSourceMutationResult.Saved(
                            sourceId = source.id,
                            probeResult = outcome.probeResult,
                            refreshRequired = outcome.refreshRequired,
                        ),
                    )
                }
            } finally {
                clearToken(source.id, token)
            }
        }
    }

    fun remove(source: NetworkLibrarySource) {
        launch(source.id) { token ->
            try {
                try {
                    token.slot.mutex.withLock { coordinator.remove(source) }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (failure: Exception) {
                    recordFailure(source.id, token, failure)
                    return@launch
                }
                runIfCurrent(source.id, token) {
                    onResult(NetworkSourceMutationResult.Removed(source.id))
                }
            } finally {
                clearToken(source.id, token)
            }
        }
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        val jobs = synchronized(stateLock) {
            val pending = active.values.mapNotNull { it.job }
            active.clear()
            sourceSlots.clear()
            pending
        }
        jobs.forEach(Job::cancel)
    }

    private fun launch(
        sourceId: String,
        operation: suspend (MutationToken) -> Unit,
    ): Boolean {
        lateinit var token: MutationToken
        lateinit var job: Job
        val accepted: Boolean
        val previous: MutationToken?
        synchronized(stateLock) {
            if (released.get()) {
                accepted = false
                previous = null
            } else {
                accepted = true
                val slot = sourceSlots.getOrPut(sourceId) { SourceSlot() }
                slot.users += 1
                token = MutationToken(slot)
                previous = active.put(sourceId, token)
                job = scope.launch(ioDispatcher, start = CoroutineStart.LAZY) {
                    operation(token)
                }
                token.job = job
            }
        }
        if (!accepted) {
            return false
        }
        previous?.job?.cancel()
        job.start()
        return true
    }

    private inline fun runIfCurrent(
        sourceId: String,
        token: MutationToken,
        action: () -> Unit,
    ): Boolean {
        synchronized(stateLock) {
            if (released.get() || active[sourceId] !== token) return false
            action()
            return true
        }
    }

    private fun recordFailure(sourceId: String, token: MutationToken, failure: Throwable) {
        val classification = if (failure is NetworkRemoteIoException) {
            BackendFailure(
                disposition = failure.kind.toBackendFailureDisposition(),
                cause = failure,
            )
        } else {
            classifyBackendFailure(failure)
        }
        runIfCurrent(sourceId, token) {
            onResult(
                NetworkSourceMutationResult.Failed(
                    sourceId = sourceId,
                    failureType = failure::class.simpleName,
                    disposition = classification.disposition,
                    failure = classification,
                ),
            )
        }
    }

    private fun clearToken(sourceId: String, token: MutationToken) {
        synchronized(stateLock) {
            if (active[sourceId] === token) active.remove(sourceId)
            token.slot.users -= 1
            if (token.slot.users == 0 && sourceSlots[sourceId] === token.slot) {
                sourceSlots.remove(sourceId)
            }
        }
    }

    private class SourceSlot {
        val mutex = Mutex()
        var users: Int = 0
    }

    private class MutationToken(
        val slot: SourceSlot,
    ) {
        var job: Job? = null
    }
}

internal sealed interface NetworkSourceMutationResult {
    val sourceId: String

    data class Checking(override val sourceId: String) : NetworkSourceMutationResult

    data class Saved(
        override val sourceId: String,
        val probeResult: NetworkProbeResult,
        val refreshRequired: Boolean,
    ) : NetworkSourceMutationResult

    data class Removed(override val sourceId: String) : NetworkSourceMutationResult

    data class Failed(
        override val sourceId: String,
        val failureType: String?,
        val disposition: BackendFailureDisposition = BackendFailureDisposition.InvariantViolation,
        val failure: BackendFailure? = null,
    ) : NetworkSourceMutationResult
}
