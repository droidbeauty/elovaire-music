package elovaire.music.droidbeauty.app.data.library.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.security.GeneralSecurityException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

/** Owns the lifetime and stale-result handling for asynchronous source mutations. */
@Suppress("TooGenericExceptionCaught")
internal class NetworkSourceMutationRuntime(
    private val scope: CoroutineScope,
    private val coordinator: NetworkSourceCoordinator,
    private val onProbeResult: (sourceId: String, result: NetworkProbeResult) -> Unit,
    private val onSourceRemoved: (sourceId: String) -> Unit,
    private val onSourcesChanged: (sourceId: String, refreshRequired: Boolean) -> Unit,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val released = AtomicBoolean(false)
    private val stateLock = Any()
    private val active = mutableMapOf<String, MutationToken>()

    fun save(source: NetworkLibrarySource, credentials: NetworkCredentials) {
        if (released.get()) return
        val accepted = launch(source.id) { token ->
            try {
                if (!runIfCurrent(source.id, token) {
                        onProbeResult(source.id, NetworkProbeResult(NetworkAvailability.Checking))
                    }) return@launch
                val outcome = coordinator.save(source, credentials)
                if (!runIfCurrent(source.id, token) {
                        onProbeResult(source.id, outcome.probeResult)
                    }) return@launch
                if (!runIfCurrent(source.id, token) {
                        onSourcesChanged(source.id, outcome.refreshRequired)
                    }) return@launch
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: GeneralSecurityException) {
                recordFailure(source.id, token, failure)
            } catch (failure: SecurityException) {
                recordFailure(source.id, token, failure)
            } catch (failure: IllegalArgumentException) {
                recordFailure(source.id, token, failure)
            } catch (failure: IllegalStateException) {
                recordFailure(source.id, token, failure)
            } catch (failure: Exception) {
                recordFailure(source.id, token, failure)
            } finally {
                clearToken(source.id, token)
            }
        }
        if (!accepted) return
    }

    fun remove(source: NetworkLibrarySource) {
        launch(source.id) { token ->
            try {
                coordinator.remove(source)
                if (!runIfCurrent(source.id, token) {
                        onSourceRemoved(source.id)
                    }) return@launch
                if (!runIfCurrent(source.id, token) {
                        onSourcesChanged(source.id, true)
                    }) return@launch
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: GeneralSecurityException) {
                recordFailure(source.id, token, failure)
            } catch (failure: SecurityException) {
                recordFailure(source.id, token, failure)
            } catch (failure: IllegalArgumentException) {
                recordFailure(source.id, token, failure)
            } catch (failure: IllegalStateException) {
                recordFailure(source.id, token, failure)
            } catch (failure: Exception) {
                recordFailure(source.id, token, failure)
            } finally {
                clearToken(source.id, token)
            }
        }
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        val (jobs, tokens) = synchronized(stateLock) {
            val pending = active.values.mapNotNull { it.job }
            val tokens = active.values.toList()
            active.clear()
            pending to tokens
        }
        jobs.forEach(Job::cancel)
        tokens.forEach { token ->
            token.callbackLock.lock()
            token.callbackLock.unlock()
        }
    }

    private fun launch(
        sourceId: String,
        operation: suspend (MutationToken) -> Unit,
    ): Boolean {
        val token = MutationToken()
        val job = scope.launch(ioDispatcher, start = CoroutineStart.LAZY) {
            operation(token)
        }
        val accepted: Boolean
        val previous: MutationToken?
        synchronized(stateLock) {
            if (released.get()) {
                accepted = false
                previous = null
            } else {
                accepted = true
                previous = active.put(sourceId, token)
                token.job = job
            }
        }
        if (!accepted) {
            job.cancel()
            return false
        }
        previous?.job?.cancel()
        previous?.callbackLock?.lock()
        previous?.callbackLock?.unlock()
        job.start()
        return true
    }

    private inline fun runIfCurrent(
        sourceId: String,
        token: MutationToken,
        action: () -> Unit,
    ): Boolean {
        token.callbackLock.lock()
        return try {
            val current = synchronized(stateLock) {
                !released.get() && active[sourceId] === token
            }
            if (!current) {
                false
            } else {
                action()
                true
            }
        } finally {
            token.callbackLock.unlock()
        }
    }

    private fun recordFailure(sourceId: String, token: MutationToken, failure: Throwable) {
        runIfCurrent(sourceId, token) {
            onProbeResult(
                sourceId,
                NetworkProbeResult(
                    availability = NetworkAvailability.Unavailable,
                    message = failure::class.simpleName,
                ),
            )
        }
    }

    private fun clearToken(sourceId: String, token: MutationToken) {
        synchronized(stateLock) {
            if (active[sourceId] === token) active.remove(sourceId)
        }
    }

    private class MutationToken {
        var job: Job? = null
        val callbackLock = ReentrantLock()
    }
}
