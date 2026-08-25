package elovaire.music.droidbeauty.app.data.library.network

import java.io.IOException
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

internal sealed interface NetworkListingResult {
    val entries: List<NetworkFileEntry>

    data class Complete(override val entries: List<NetworkFileEntry>) : NetworkListingResult

    data class Incomplete(
        override val entries: List<NetworkFileEntry>,
        val reason: String,
    ) : NetworkListingResult
}

internal interface NetworkFileSystem {
    fun probeBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): NetworkProbeResult

    fun listBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        maxEntries: Int = 100_000,
        maxDepth: Int = 32,
    ): NetworkListingResult

    fun openBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        path: String,
        position: Long,
        length: Long,
    ): NetworkReadHandle

    fun invalidate(sourceId: String) = Unit

    fun release() = Unit
}

internal class NetworkFileSystemRegistry(
    private val sourceStore: NetworkLibrarySourceStore,
    private val credentialStore: NetworkCredentialStore,
    private val fileSystems: Map<NetworkLibraryProtocol, NetworkFileSystem>,
    private val localNetworkAccessAllowed: () -> Boolean = { true },
) {
    private val operationAdmission = NetworkOperationAdmission()

    fun source(sourceId: String): NetworkLibrarySource? = sourceStore.sources.value.firstOrNull { it.id == sourceId }

    fun sourceGeneration(sourceId: String): Long = sourceStore.generation(sourceId)

    fun isCurrent(source: NetworkLibrarySource, generation: Long): Boolean =
        sourceStore.isCurrent(source, generation)

    fun credentials(source: NetworkLibrarySource): NetworkCredentials? = credentialStore.get(source.credentialKey)

    fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials): NetworkProbeResult {
        if (!localNetworkAccessAllowed()) {
            return NetworkProbeResult(NetworkAvailability.LocalNetworkPermissionRequired)
        }
        return operationAdmission.withBackground {
            fileSystems[source.protocol]?.probeBlocking(source, credentials)
                ?: NetworkProbeResult(NetworkAvailability.Misconfigured, "Protocol is unavailable")
        }
    }

    fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials): NetworkListingResult {
        checkLocalNetworkAccess()
        return operationAdmission.withBackground {
            fileSystems[source.protocol]?.listBlocking(source, credentials)
                ?: throw IOException("Network protocol is unavailable")
        }
    }

    fun openBlocking(
        sourceId: String,
        path: String,
        position: Long,
        length: Long,
    ): NetworkReadHandle {
        checkLocalNetworkAccess()
        val source = source(sourceId) ?: throw IOException("Network library source is unavailable")
        val credentials = credentials(source) ?: throw IOException("Network library credentials are unavailable")
        val permit = operationAdmission.acquirePlayback()
        var handedOff = false
        return try {
            val handle = fileSystems[source.protocol]?.openBlocking(source, credentials, path, position, length)
                ?: throw IOException("Network protocol is unavailable")
            val released = AtomicBoolean(false)
            NetworkReadHandle(
                input = handle.input,
                length = handle.length,
                closeHandle = {
                    if (released.compareAndSet(false, true)) {
                        try {
                            handle.closeHandle()
                        } finally {
                            permit.release()
                        }
                    }
                },
            ).also { handedOff = true }
        } finally {
            if (!handedOff) permit.release()
        }
    }

    fun invalidate(sourceId: String) {
        fileSystems.values.forEach { it.invalidate(sourceId) }
    }

    fun release() {
        fileSystems.values.forEach(NetworkFileSystem::release)
    }

    private fun checkLocalNetworkAccess() {
        if (!localNetworkAccessAllowed()) throw NetworkLocalNetworkPermissionException()
    }
}

/** Reserves one slot for range reads so a directory crawl cannot consume all network capacity. */
private class NetworkOperationAdmission(
    backgroundCapacity: Int = 3,
) {
    private val background = Semaphore(backgroundCapacity, true)
    private val playback = Semaphore(1, true)

    fun <T> withBackground(block: () -> T): T {
        acquire(background).use {
            return block()
        }
    }

    fun acquirePlayback(): Permit {
        return acquire(playback)
    }

    private fun acquire(semaphore: Semaphore): Permit {
        try {
            if (!semaphore.tryAcquire(MAX_WAIT_MS, TimeUnit.MILLISECONDS)) {
                throw IOException("Network operation capacity is temporarily exhausted")
            }
            return Permit(semaphore)
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IOException("Network operation admission interrupted", interrupted)
        }
    }

    class Permit(private val semaphore: Semaphore) : AutoCloseable {
        private val released = AtomicBoolean(false)

        fun release() {
            if (released.compareAndSet(false, true)) semaphore.release()
        }

        override fun close() = release()
    }

    private companion object {
        const val MAX_WAIT_MS = 10_000L
    }
}
