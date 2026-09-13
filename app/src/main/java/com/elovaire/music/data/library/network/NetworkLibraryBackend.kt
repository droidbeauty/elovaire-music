package elovaire.music.droidbeauty.app.data.library.network

import android.util.Log
import elovaire.music.droidbeauty.app.data.library.LibraryNetworkController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal sealed interface NetworkLibraryChange {
    val sourceId: String

    data class Saved(
        override val sourceId: String,
        val refreshRequired: Boolean,
    ) : NetworkLibraryChange

    data class Removed(override val sourceId: String) : NetworkLibraryChange
}

/** Owns network mutation results, probe state, connectivity service lifetime, and release. */
internal class NetworkLibraryBackend(
    private val optionalScope: CoroutineScope,
    private val sourceStore: NetworkLibrarySourceStore,
    private val registryProvider: () -> NetworkFileSystemRegistry,
    coordinator: NetworkSourceMutationBackend,
    private val onLibraryChange: (NetworkLibraryChange) -> Unit,
    private val ioDispatcher: CoroutineDispatcher,
) : Closeable {
    private val released = AtomicBoolean(false)
    private val servicesLock = Any()
    private var servicesStarted = false
    private var servicesJob: Job? = null
    private val _probeResults = MutableStateFlow<Map<String, NetworkProbeResult>>(emptyMap())
    val sources: StateFlow<List<NetworkLibrarySource>> = sourceStore.sources
    val probeResults: StateFlow<Map<String, NetworkProbeResult>> = _probeResults.asStateFlow()
    private val mutationRuntime = NetworkSourceMutationRuntime(
        scope = optionalScope,
        coordinator = coordinator,
        onResult = ::handleMutationResult,
        ioDispatcher = ioDispatcher,
    )

    fun recordProbe(sourceId: String, result: NetworkProbeResult) {
        if (released.get()) return
        _probeResults.update { it + (sourceId to result) }
    }

    fun save(source: NetworkLibrarySource, credentials: NetworkCredentials) {
        if (released.get()) return
        start()
        mutationRuntime.save(source, credentials)
    }

    fun remove(source: NetworkLibrarySource) {
        if (released.get()) return
        mutationRuntime.remove(source)
    }

    @Suppress("TooGenericExceptionCaught")
    fun start() {
        if (released.get() || sources.value.none(NetworkLibrarySource::enabled)) return
        synchronized(servicesLock) {
            if (servicesStarted) return
            servicesStarted = true
            servicesJob = optionalScope.launch(ioDispatcher) {
                try {
                    registryProvider().start()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: SecurityException) {
                    resetAfterStartFailure(failure)
                } catch (failure: IllegalArgumentException) {
                    resetAfterStartFailure(failure)
                } catch (failure: IllegalStateException) {
                    resetAfterStartFailure(failure)
                } catch (failure: RuntimeException) {
                    resetAfterStartFailure(failure)
                }
            }
        }
    }

    override fun close() {
        if (!released.compareAndSet(false, true)) return
        mutationRuntime.release()
        synchronized(servicesLock) {
            servicesJob?.cancel()
            servicesJob = null
            servicesStarted = false
        }
    }

    private fun handleMutationResult(result: NetworkSourceMutationResult) {
        if (released.get()) return
        when (result) {
            is NetworkSourceMutationResult.Checking -> {
                recordProbe(result.sourceId, NetworkProbeResult(NetworkAvailability.Checking))
            }
            is NetworkSourceMutationResult.Saved -> {
                recordProbe(result.sourceId, result.probeResult)
                start()
                onLibraryChange(NetworkLibraryChange.Saved(result.sourceId, result.refreshRequired))
            }
            is NetworkSourceMutationResult.Removed -> {
                _probeResults.update { it - result.sourceId }
                start()
                onLibraryChange(NetworkLibraryChange.Removed(result.sourceId))
            }
            is NetworkSourceMutationResult.Failed -> {
                recordProbe(
                    result.sourceId,
                    NetworkProbeResult(NetworkAvailability.Unavailable, result.failureType),
                )
            }
        }
    }

    private fun resetAfterStartFailure(failure: RuntimeException) {
        synchronized(servicesLock) {
            servicesStarted = false
            servicesJob = null
        }
        Log.w(TAG, "Network services could not start; retrying later.", failure)
    }

    private companion object {
        const val TAG = "NetworkLibraryBackend"
    }
}

internal class NetworkLibraryIntegration(
    private val library: LibraryNetworkController,
    private val sourceStore: NetworkLibrarySourceStore,
) {
    fun apply(change: NetworkLibraryChange) {
        library.unblockNetworkSource(change.sourceId)
        library.setNetworkSources(
            sources = sourceStore.sources.value,
            enrichMetadata = false,
            showLoadingIndicator = true,
            forceRefreshSourceIds = when (change) {
                is NetworkLibraryChange.Saved -> if (change.refreshRequired) setOf(change.sourceId) else emptySet()
                is NetworkLibraryChange.Removed -> setOf(change.sourceId)
            },
        )
    }
}
