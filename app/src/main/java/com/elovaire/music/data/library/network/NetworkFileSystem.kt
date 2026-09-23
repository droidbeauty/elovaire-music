package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.core.backend.BackendResourceTracker
import java.io.IOException
import java.util.Collections
import java.util.IdentityHashMap
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.ConcurrentHashMap

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
    private val resourceTracker: BackendResourceTracker = BackendResourceRegistry,
) {
    private val operationAdmission = NetworkOperationAdmission(resourceTracker = resourceTracker)
    private val released = AtomicBoolean(false)
    private val activeHandlesLock = Any()
    private val activeHandles = Collections.newSetFromMap(IdentityHashMap<NetworkReadHandle, Boolean>())
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

    fun credentials(source: NetworkLibrarySource): NetworkCredentials? {
        if (released.get()) return null
        return credentialStore.get(source.id, source.credentialKey)
    }

    fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials): NetworkProbeResult {
        checkNotReleased()
        if (!checkLocalNetworkAccess(throwOnDenied = false)) {
            return NetworkProbeResult(NetworkAvailability.LocalNetworkPermissionRequired)
        }
        return operationAdmission.withPermit(NetworkReadPurpose.Listing) {
            val resource = resourceTracker.acquire(BackendResourceKind.ActiveNetworkListing)
            try {
                checkNotReleased()
                fileSystems[source.protocol]?.probeBlocking(source, credentials)
                    ?: NetworkProbeResult(NetworkAvailability.Misconfigured, "Protocol is unavailable")
            } finally {
                resource.close()
            }
        }
    }

    fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials): NetworkListingResult {
        checkNotReleased()
        checkLocalNetworkAccess()
        return operationAdmission.withPermit(NetworkReadPurpose.Listing) {
            val resource = resourceTracker.acquire(BackendResourceKind.ActiveNetworkListing)
            try {
                checkNotReleased()
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
        checkNotReleased()
        checkLocalNetworkAccess()
        val sourceRecord = source(sourceId) ?: throw NetworkRemoteIoException(
            kind = RemoteIoFailureKind.SourceRemoved,
            message = "Network library source is unavailable",
        )
        val credentialGeneration = credentialStore.generation(sourceRecord.credentialKey)
        val credentialRecord = credentials(sourceRecord) ?: throw NetworkRemoteIoException(
            kind = RemoteIoFailureKind.Authentication,
            message = "Network library credentials are unavailable",
        )
        val sourceGeneration = sourceGeneration(sourceId)
        val permit = operationAdmission.acquire(purpose)
        var handedOff = false
        return try {
            checkNotReleased()
            if (
                !isCurrent(sourceRecord, sourceGeneration) ||
                    credentialStore.generation(sourceRecord.credentialKey) != credentialGeneration
            ) {
                throw NetworkRemoteIoException(
                    kind = RemoteIoFailureKind.SourceRemoved,
                    message = "Network library source changed while opening",
                )
            }
            val handle = fileSystems[sourceRecord.protocol]?.openBlocking(sourceRecord, credentialRecord, path, position, length)
                ?: throw IOException("Network protocol is unavailable")
            val released = AtomicBoolean(false)
            val resource = resourceTracker.acquire(purpose.resourceKind())
            val handleReference = java.util.concurrent.atomic.AtomicReference<NetworkReadHandle?>()
            val wrapped = NetworkReadHandle(
                input = handle.input,
                length = handle.length,
                closeHandle = {
                    if (released.compareAndSet(false, true)) {
                        try {
                            handle.closeHandle()
                        } finally {
                            permit.release()
                            resource.close()
                            synchronized(activeHandlesLock) {
                                handleReference.get()?.let(activeHandles::remove)
                            }
                        }
                    }
                },
            )
            handleReference.set(wrapped)
            synchronized(activeHandlesLock) {
                if (this@NetworkFileSystemRegistry.released.get()) {
                    wrapped.close()
                    throw NetworkRemoteIoException(
                        kind = RemoteIoFailureKind.SourceRemoved,
                        message = "Network registry is released",
                    )
                }
                activeHandles += wrapped
            }
            wrapped.also { handedOff = true }
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
        if (!released.compareAndSet(false, true)) return
        connectivityObserver?.close()
        operationAdmission.close()
        val handles = synchronized(activeHandlesLock) {
            activeHandles.toList().also { activeHandles.clear() }
        }
        handles.forEach { handle -> runCatching { handle.close() } }
        fileSystems.values.forEach(NetworkFileSystem::release)
    }

    private fun checkNotReleased() {
        check(!released.get()) { "Network file-system registry is released" }
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
    private val resourceTracker: BackendResourceTracker = BackendResourceRegistry,
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
    private val closed = AtomicBoolean(false)
    private val waitingThreads = ConcurrentHashMap.newKeySet<Thread>()

    fun <T> withPermit(purpose: NetworkReadPurpose, block: () -> T): T {
        acquire(purpose).use {
            return block()
        }
    }

    fun acquire(purpose: NetworkReadPurpose): Permit {
        if (closed.get()) throw IOException("Network operation admission is closed")
        val semaphore = if (purpose == NetworkReadPurpose.Playback) playback else background
        val active = if (purpose == NetworkReadPurpose.Playback) activePlayback else activeBackground
        val waiting = if (purpose == NetworkReadPurpose.Playback) waitingPlayback else waitingBackground
        waiting.incrementAndGet()
        resourceTracker.adjust(purpose.waitingResourceKind(), 1)
        try {
            acquire(semaphore)
            active.incrementAndGet()
            return Permit(semaphore, active)
        } finally {
            waiting.decrementAndGet()
            resourceTracker.adjust(purpose.waitingResourceKind(), -1)
        }
    }

    private fun acquire(semaphore: Semaphore) {
        val thread = Thread.currentThread()
        waitingThreads += thread
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(maxWaitMs)
        try {
            if (closed.get()) throw IOException("Network operation admission is closed")
            val remainingNs = deadline - System.nanoTime()
            if (remainingNs < 0L || !semaphore.tryAcquire(remainingNs, TimeUnit.NANOSECONDS)) {
                throw IOException("Network operation capacity is temporarily exhausted")
            }
            if (closed.get()) {
                semaphore.release()
                throw IOException("Network operation admission is closed")
            }
            return
        } catch (interrupted: InterruptedException) {
            if (closed.get()) throw IOException("Network operation admission is closed", interrupted)
            Thread.currentThread().interrupt()
            throw IOException("Network operation admission interrupted", interrupted)
        } finally {
            waitingThreads.remove(thread)
        }
    }

    fun close() {
        if (!closed.compareAndSet(false, true)) return
        waitingThreads.toList().forEach(Thread::interrupt)
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

private fun NetworkReadPurpose.waitingResourceKind(): BackendResourceKind = when (this) {
    NetworkReadPurpose.Playback -> BackendResourceKind.WaitingNetworkPlaybackRead
    NetworkReadPurpose.Metadata,
    NetworkReadPurpose.Artwork,
    NetworkReadPurpose.Listing,
    -> BackendResourceKind.WaitingNetworkBackgroundRead
}
