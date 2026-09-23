package elovaire.music.droidbeauty.app.core.backend

import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import elovaire.music.droidbeauty.app.core.UuidOperationIdGenerator
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicIntegerArray
import kotlinx.coroutines.CancellationException

internal data class BackendEventSnapshot(
    val name: String,
    val fields: Map<String, String>,
    val sequence: Long = 0L,
)

internal data class BackendDiagnosticContext(
    val eventName: String,
    val subsystem: String?,
    val phase: String?,
)

internal enum class BackendResourceKind(val key: String) {
    ActiveScan("active_library_scans"),
    ActiveNetworkScan("active_network_scans"),
    ActiveNetworkRead("active_network_reads"),
    ActiveNetworkPlaybackRead("active_network_playback_reads"),
    ActiveNetworkMetadataRead("active_network_metadata_reads"),
    ActiveNetworkArtworkRead("active_network_artwork_reads"),
    ActiveNetworkListing("active_network_listings"),
    WaitingNetworkBackgroundRead("waiting_network_background_reads"),
    WaitingNetworkPlaybackRead("waiting_network_playback_reads"),
    ActiveSmbSession("active_smb_sessions"),
    ActiveWebDavRequest("active_webdav_requests"),
    ActiveMetadataRead("active_metadata_reads"),
    ActiveArtworkDecode("active_artwork_decodes"),
    ActiveHttpRequest("active_http_requests"),
    ActiveRegisteredCallback("active_registered_callbacks"),
    ActiveRetriever("active_retrievers"),
    DatabaseInstance("database_instances"),
    PendingRoomOperation("pending_room_operations"),
    ActivePlayer("active_players"),
    ActiveMediaSession("active_media_sessions"),
    ActiveObserver("active_observers"),
    ActiveMutation("active_mutations"),
}

internal interface BackendDiagnosticRecorder {
    fun record(event: BackendEvent)
    fun snapshot(): List<BackendEventSnapshot>
    fun clear()
    fun lastContext(): BackendDiagnosticContext?
    fun installBreadcrumbCheckpoint(checkpoint: (BackendDiagnosticContext) -> Unit)
    fun clearBreadcrumbCheckpoint()
    fun recordWorkerFailure(owner: String, failure: Throwable)
}

/** Bounded, process-local diagnostics; it never stores user content or exception messages. */
internal object BackendDiagnostics : BackendDiagnosticRecorder by BackendDiagnosticsRuntime()

internal class RecordingBackendEventSink(
    private val maxEvents: Int = 256,
) : BackendEventSink {
    private val lock = Any()
    private val events = ArrayDeque<BackendEventSnapshot>()
    private var sequence = 0L

    override fun emit(event: BackendEvent) {
        synchronized(lock) {
            val snapshot = BackendEventSnapshot(event.name, sanitizeFields(event.fields), ++sequence)
            if (events.size == maxEvents) events.removeFirst()
            events.addLast(snapshot)
        }
    }

    fun snapshot(): List<BackendEventSnapshot> = synchronized(lock) { events.toList() }
}

internal interface BackendResourceTracker {
    fun acquire(kind: BackendResourceKind): Closeable
    fun set(kind: BackendResourceKind, count: Int)
    fun adjust(kind: BackendResourceKind, delta: Int)
    fun snapshot(): Map<String, Int>
    fun clear()
}

internal object NoOpBackendResourceTracker : BackendResourceTracker {
    override fun acquire(kind: BackendResourceKind): Closeable = Closeable {}
    override fun set(kind: BackendResourceKind, count: Int) = Unit
    override fun adjust(kind: BackendResourceKind, delta: Int) = Unit
    override fun snapshot(): Map<String, Int> = emptyMap()
    override fun clear() = Unit
}

internal class BackendResourceRuntime : BackendResourceTracker {
    private val counts = AtomicIntegerArray(BackendResourceKind.values().size)

    override fun acquire(kind: BackendResourceKind): Closeable {
        counts.incrementAndGet(kind.ordinal)
        return ResourceLease(kind)
    }

    private inner class ResourceLease(
        private val kind: BackendResourceKind,
    ) : Closeable {
        private val released = AtomicBoolean(false)

        override fun close() {
            if (!released.compareAndSet(false, true)) return
            release(kind)
        }
    }

    override fun set(kind: BackendResourceKind, count: Int) {
        counts.set(kind.ordinal, count.coerceAtLeast(0))
    }

    override fun adjust(kind: BackendResourceKind, delta: Int) {
        require(delta != 0)
        val index = kind.ordinal
        while (true) {
            val current = counts.get(index)
            val next = (current + delta).coerceAtLeast(0)
            if (counts.compareAndSet(index, current, next)) return
        }
    }

    override fun snapshot(): Map<String, Int> = buildMap {
        BackendResourceKind.values().forEach { kind ->
            counts.get(kind.ordinal).takeIf { it > 0 }?.let { put(kind.key, it) }
        }
    }

    override fun clear() {
        BackendResourceKind.values().forEach { kind -> counts.set(kind.ordinal, 0) }
    }

    private fun release(kind: BackendResourceKind) {
        adjust(kind, -1)
    }
}

/** Compatibility facade for tests and legacy leaf objects not yet wired to an app runtime. */
@Deprecated("Use an app-scoped BackendResourceRuntime")
internal object BackendResourceRegistry : BackendResourceTracker by BackendResourceRuntime()

/** App-scoped bounded diagnostics and resource accounting. */
internal class BackendDiagnosticsRuntime(
    private val maxEvents: Int = 256,
) : BackendEventSink, BackendDiagnosticRecorder, Closeable {
    private val lock = Any()
    private val events = ArrayDeque<BackendEventSnapshot>()
    private val _resources = BackendResourceRuntime()
    private val released = AtomicBoolean(false)
    private var eventSequence = 0L
    @Volatile private var lastContext: BackendDiagnosticContext? = null
    @Volatile private var breadcrumbCheckpoint: ((BackendDiagnosticContext) -> Unit)? = null

    val resources: BackendResourceTracker = _resources

    override fun emit(event: BackendEvent) = record(event)

    override fun record(event: BackendEvent) {
        if (released.get()) return
        val snapshot = synchronized(lock) {
            val eventSnapshot = BackendEventSnapshot(
                name = event.name.take(MAX_EVENT_NAME_LENGTH),
                fields = sanitizeFields(event.fields),
                sequence = ++eventSequence,
            )
            if (events.size == maxEvents) events.removeFirst()
            events.addLast(eventSnapshot)
            eventSnapshot
        }
        val context = BackendDiagnosticContext(
            eventName = snapshot.name,
            subsystem = snapshot.fields["subsystem"],
            phase = snapshot.fields["phase"],
        )
        lastContext = context
        runCatching { breadcrumbCheckpoint?.invoke(context) }
    }

    override fun snapshot(): List<BackendEventSnapshot> = synchronized(lock) { events.toList() }
    fun sequenceMarker(): Long = synchronized(lock) { eventSequence }
    fun eventsAfter(sequence: Long): List<BackendEventSnapshot> = synchronized(lock) {
        events.filter { it.sequence > sequence }
    }
    fun eventsSince(sequence: Long): Pair<Long, List<BackendEventSnapshot>> = synchronized(lock) {
        eventSequence to events.filter { it.sequence > sequence }
    }
    override fun clear() {
        synchronized(lock) { events.clear() }
        lastContext = null
    }
    override fun lastContext(): BackendDiagnosticContext? = lastContext
    override fun installBreadcrumbCheckpoint(checkpoint: (BackendDiagnosticContext) -> Unit) {
        if (released.get()) return
        breadcrumbCheckpoint = checkpoint
    }
    override fun clearBreadcrumbCheckpoint() {
        breadcrumbCheckpoint = null
    }
    override fun recordWorkerFailure(owner: String, failure: Throwable) {
        if (failure is CancellationException) return
        emit(BackendEvent.WorkerFailed(mapOf("owner" to owner, "error_type" to (failure::class.simpleName ?: "Unknown"))))
    }

    override fun close() {
        if (!released.compareAndSet(false, true)) return
        clearBreadcrumbCheckpoint()
        clear()
    }

    private companion object {
        const val MAX_EVENT_NAME_LENGTH = 64
    }
}

internal class BackendOperationMonitor(
    private val sink: BackendEventSink,
    private val clock: AppClock = AndroidAppClock,
    private val operationIdGenerator: OperationIdGenerator = UuidOperationIdGenerator,
) {
    fun begin(subsystem: BackendSubsystem): BackendOperationHandle {
        val context = BackendOperationContext(
            id = operationIdGenerator.nextId(),
            subsystem = subsystem,
            startedAtElapsedMs = clock.elapsedTimeMs(),
        )
        sink.emit(
            BackendEvent.OperationStarted(
                context.fields("started", clock.elapsedTimeMs()),
            ),
        )
        return BackendOperationHandle(context, sink, clock)
    }

    @Suppress("TooGenericExceptionCaught")
    suspend fun <T> run(
        subsystem: BackendSubsystem,
        block: suspend () -> T,
    ): T {
        val operation = begin(subsystem)
        return try {
            block().also { operation.complete() }
        } catch (cancelled: CancellationException) {
            operation.cancel()
            throw cancelled
        } catch (failure: Throwable) {
            operation.fail(failure)
            throw failure
        }
    }
}

internal class BackendOperationHandle internal constructor(
    private val context: BackendOperationContext,
    private val sink: BackendEventSink,
    private val clock: AppClock,
) {
    private val terminal = AtomicBoolean(false)

    fun complete(
        extra: Map<String, String> = emptyMap(),
        metrics: BackendOperationMetrics = BackendOperationMetrics(),
    ) {
        if (!terminal.compareAndSet(false, true)) return
        sink.emit(
            BackendEvent.OperationCompleted(
                context.fields("completed", clock.elapsedTimeMs(), extra, metrics),
            ),
        )
    }

    fun fail(failure: Throwable) {
        if (!terminal.compareAndSet(false, true)) return
        sink.emit(
            BackendEvent.OperationFailed(
                context.fields(
                    phase = "failed",
                    elapsedTimeMs = clock.elapsedTimeMs(),
                    extra = mapOf("error_type" to (failure::class.simpleName ?: "Unknown")),
                ),
            ),
        )
    }

    fun cancel() {
        if (!terminal.compareAndSet(false, true)) return
        sink.emit(
            BackendEvent.OperationCancelled(
                context.fields("cancelled", clock.elapsedTimeMs()),
            ),
        )
    }
}

private fun sanitizeFields(fields: Map<String, String>): Map<String, String> {
    return fields.entries
        .filter { (key, _) -> key in SAFE_DIAGNOSTIC_FIELDS }
        .take(MAX_DIAGNOSTIC_FIELDS)
        .associate { (key, value) -> key to value.safeDiagnosticValue() }
}

private val SAFE_DIAGNOSTIC_FIELDS = setOf(
    "operation_id",
    "owner",
    "subsystem",
    "phase",
    "elapsed_ms",
    "items_input",
    "items_output",
    "rows_read",
    "rows_changed",
    "bytes_read",
    "bytes_written",
    "cache_hits",
    "cache_misses",
    "cache",
    "fallback",
    "error_type",
    "albums",
    "enrich_metadata",
    "force_index",
    "recovered",
    "retry",
    "songs",
    "targeted_network_sources",
    "targeted_paths",
    "type",
    "result",
    "part_count",
    "chapter_count",
    "source_kind",
    "duration_known",
    "resume_found",
)

private const val MAX_DIAGNOSTIC_FIELDS = 24
private const val MAX_DIAGNOSTIC_VALUE_LENGTH = 128

private fun String.safeDiagnosticValue(): String {
    return filter { it in '\u0020'..'\u007e' }.take(MAX_DIAGNOSTIC_VALUE_LENGTH)
}
