package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import java.io.IOException
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

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

    fun invalidateAll() = Unit

    fun release() = Unit
}

internal class NetworkFileSystemRegistry(
    private val sourceStore: NetworkLibrarySourceStore,
    private val credentialStore: NetworkCredentialStore,
    private val fileSystems: Map<NetworkLibraryProtocol, NetworkFileSystem>,
    private val localNetworkAccessAllowed: () -> Boolean = { true },
    applicationContext: Context? = null,
) {
    private val operationAdmission = NetworkOperationAdmission()
    private val permissionAllowed = AtomicBoolean(localNetworkAccessAllowed())
    private val networkGeneration = AtomicLong(0L)
    private val connectivityObserver = applicationContext?.let { context ->
        NetworkConnectivityObserver(
            context = context,
            localNetworkAccessAllowed = localNetworkAccessAllowed,
            onStateChanged = { state ->
                permissionAllowed.set(state.localNetworkAccessAllowed)
                networkGeneration.set(state.generation)
                invalidateAll()
            },
        )
    }

    fun source(sourceId: String): NetworkLibrarySource? = sourceStore.sources.value.firstOrNull { it.id == sourceId }

    fun sourceGeneration(sourceId: String): Long = sourceStore.generation(sourceId)

    fun networkGeneration(): Long = networkGeneration.get()

    fun isCurrent(source: NetworkLibrarySource, generation: Long): Boolean =
        sourceStore.isCurrent(source, generation)

    fun credentials(source: NetworkLibrarySource): NetworkCredentials? =
        credentialStore.get(source.id, source.credentialKey)

    fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials): NetworkProbeResult {
        if (!checkLocalNetworkAccess(throwOnDenied = false)) {
            return NetworkProbeResult(NetworkAvailability.LocalNetworkPermissionRequired)
        }
        return operationAdmission.withPermit(NetworkReadPurpose.Listing) {
            val resource = BackendResourceRegistry.acquire(BackendResourceKind.ActiveNetworkListing)
            try {
                fileSystems[source.protocol]?.probeBlocking(source, credentials)
                    ?: NetworkProbeResult(NetworkAvailability.Misconfigured, "Protocol is unavailable")
            } finally {
                resource.close()
            }
        }
    }

    fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials): NetworkListingResult {
        checkLocalNetworkAccess()
        return operationAdmission.withPermit(NetworkReadPurpose.Listing) {
            val resource = BackendResourceRegistry.acquire(BackendResourceKind.ActiveNetworkListing)
            try {
                fileSystems[source.protocol]?.listBlocking(source, credentials)
                    ?: throw IOException("Network protocol is unavailable")
            } finally {
                resource.close()
            }
        }
    }

    fun openBlocking(
        sourceId: String,
        path: String,
        position: Long,
        length: Long,
        purpose: NetworkReadPurpose,
    ): NetworkReadHandle {
        checkLocalNetworkAccess()
        val sourceRecord = source(sourceId) ?: throw NetworkRemoteIoException(
            kind = RemoteIoFailureKind.SourceRemoved,
            message = "Network library source is unavailable",
        )
        val credentialRecord = credentials(sourceRecord) ?: throw NetworkRemoteIoException(
            kind = RemoteIoFailureKind.Authentication,
            message = "Network library credentials are unavailable",
        )
        val sourceGeneration = sourceGeneration(sourceId)
        val permit = operationAdmission.acquire(purpose)
        var handedOff = false
        return try {
            if (
                source(sourceId) == null ||
                    !isCurrent(sourceRecord, sourceGeneration) ||
                    this.credentials(sourceRecord) != credentialRecord
            ) {
                throw NetworkRemoteIoException(
                    kind = RemoteIoFailureKind.SourceRemoved,
                    message = "Network library source changed while opening",
                )
            }
            val handle = fileSystems[sourceRecord.protocol]?.openBlocking(sourceRecord, credentialRecord, path, position, length)
                ?: throw IOException("Network protocol is unavailable")
            val released = AtomicBoolean(false)
            val resource = BackendResourceRegistry.acquire(purpose.resourceKind())
            NetworkReadHandle(
                input = handle.input,
                length = handle.length,
                closeHandle = {
                    if (released.compareAndSet(false, true)) {
                        try {
                            handle.closeHandle()
                        } finally {
                            permit.release()
                            resource.close()
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

    fun invalidateAll() {
        fileSystems.values.forEach(NetworkFileSystem::invalidateAll)
    }

    fun start() {
        connectivityObserver?.start()
    }

    fun release() {
        connectivityObserver?.close()
        fileSystems.values.forEach(NetworkFileSystem::release)
    }

    private fun checkLocalNetworkAccess() {
        if (!checkLocalNetworkAccess(throwOnDenied = true)) {
            throw NetworkLocalNetworkPermissionException()
        }
    }

    private fun checkLocalNetworkAccess(throwOnDenied: Boolean): Boolean {
        connectivityObserver?.syncPermission()
        if (connectivityObserver == null) {
            val allowed = localNetworkAccessAllowed()
            if (permissionAllowed.getAndSet(allowed) != allowed) {
                networkGeneration.incrementAndGet()
                invalidateAll()
            }
        }
        val allowed = permissionAllowed.get()
        if (!allowed && throwOnDenied) throw NetworkLocalNetworkPermissionException()
        return allowed
    }

    private fun NetworkReadPurpose.resourceKind(): BackendResourceKind = when (this) {
        NetworkReadPurpose.Playback -> BackendResourceKind.ActiveNetworkPlaybackRead
        NetworkReadPurpose.Metadata -> BackendResourceKind.ActiveNetworkMetadataRead
        NetworkReadPurpose.Artwork -> BackendResourceKind.ActiveNetworkArtworkRead
        NetworkReadPurpose.Listing -> BackendResourceKind.ActiveNetworkRead
    }
}

/** Reserves one slot for range reads so a directory crawl cannot consume all network capacity. */
internal class NetworkOperationAdmission(
    backgroundCapacity: Int = 3,
    playbackCapacity: Int = 2,
    private val maxWaitMs: Long = MAX_WAIT_MS,
) {
    init {
        require(backgroundCapacity > 0)
        require(playbackCapacity > 0)
        require(maxWaitMs >= 0L)
    }

    private val background = Semaphore(backgroundCapacity, true)
    // Crossfade/prebuffer can keep the outgoing and incoming player reading at once.
    private val playback = Semaphore(playbackCapacity, true)
    private val activeBackground = AtomicInteger()
    private val activePlayback = AtomicInteger()
    private val waitingBackground = AtomicInteger()
    private val waitingPlayback = AtomicInteger()

    fun <T> withPermit(purpose: NetworkReadPurpose, block: () -> T): T {
        acquire(purpose).use {
            return block()
        }
    }

    fun acquire(purpose: NetworkReadPurpose): Permit {
        val semaphore = if (purpose == NetworkReadPurpose.Playback) playback else background
        val active = if (purpose == NetworkReadPurpose.Playback) activePlayback else activeBackground
        val waiting = if (purpose == NetworkReadPurpose.Playback) waitingPlayback else waitingBackground
        waiting.incrementAndGet()
        try {
            acquire(semaphore)
            active.incrementAndGet()
            return Permit(semaphore, active)
        } finally {
            waiting.decrementAndGet()
        }
    }

    private fun acquire(semaphore: Semaphore) {
        try {
            if (!semaphore.tryAcquire(maxWaitMs, TimeUnit.MILLISECONDS)) {
                throw IOException("Network operation capacity is temporarily exhausted")
            }
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IOException("Network operation admission interrupted", interrupted)
        }
    }

    internal data class Snapshot(
        val activeBackground: Int,
        val activePlayback: Int,
        val waitingBackground: Int,
        val waitingPlayback: Int,
    )

    fun snapshot(): Snapshot = Snapshot(
        activeBackground = activeBackground.get(),
        activePlayback = activePlayback.get(),
        waitingBackground = waitingBackground.get(),
        waitingPlayback = waitingPlayback.get(),
    )

    class Permit(
        private val semaphore: Semaphore,
        private val active: AtomicInteger?,
    ) : AutoCloseable {
        private val released = AtomicBoolean(false)

        fun release() {
            if (released.compareAndSet(false, true)) {
                active?.decrementAndGet()
                semaphore.release()
            }
        }

        override fun close() = release()
    }

    private companion object {
        const val MAX_WAIT_MS = 10_000L
    }
}
