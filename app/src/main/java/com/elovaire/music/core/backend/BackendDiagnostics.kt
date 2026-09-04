package elovaire.music.droidbeauty.app.core.backend

import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import elovaire.music.droidbeauty.app.core.UuidOperationIdGenerator
import java.io.Closeable
import java.util.EnumMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException

internal data class BackendEventSnapshot(
    val name: String,
    val fields: Map<String, String>,
)

internal enum class BackendResourceKind(val key: String) {
    ActiveScan("active_library_scans"),
    ActiveNetworkScan("active_network_scans"),
    ActiveNetworkRead("active_network_reads"),
    ActiveNetworkPlaybackRead("active_network_playback_reads"),
    ActiveNetworkMetadataRead("active_network_metadata_reads"),
    ActiveNetworkArtworkRead("active_network_artwork_reads"),
    ActiveNetworkListing("active_network_listings"),
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

/** Bounded, process-local diagnostics; it never stores user content or exception messages. */
internal object BackendDiagnostics {
    private const val MAX_EVENTS = 256
    private val lock = Any()
    private val events = ArrayDeque<BackendEventSnapshot>()

    fun record(event: BackendEvent) {
        val snapshot = BackendEventSnapshot(
            name = event.name.take(MAX_EVENT_NAME_LENGTH),
            fields = sanitizeFields(event.fields),
        )
        synchronized(lock) {
            if (events.size == MAX_EVENTS) events.removeFirst()
            events.addLast(snapshot)
        }
    }

    fun snapshot(): List<BackendEventSnapshot> = synchronized(lock) { events.toList() }

    fun clear() = synchronized(lock) { events.clear() }

    private const val MAX_EVENT_NAME_LENGTH = 64
}

internal class RecordingBackendEventSink(
    private val maxEvents: Int = 256,
) : BackendEventSink {
    private val lock = Any()
    private val events = ArrayDeque<BackendEventSnapshot>()

    override fun emit(event: BackendEvent) {
        val snapshot = BackendEventSnapshot(event.name, sanitizeFields(event.fields))
        synchronized(lock) {
            if (events.size == maxEvents) events.removeFirst()
            events.addLast(snapshot)
        }
    }

    fun snapshot(): List<BackendEventSnapshot> = synchronized(lock) { events.toList() }
}

internal object BackendResourceRegistry {
    private val lock = Any()
    private val counts = EnumMap<BackendResourceKind, Int>(BackendResourceKind::class.java)

    fun acquire(kind: BackendResourceKind): Closeable {
        synchronized(lock) { counts[kind] = (counts[kind] ?: 0) + 1 }
        val released = AtomicBoolean(false)
        return Closeable {
            if (!released.compareAndSet(false, true)) return@Closeable
            synchronized(lock) {
                val count = counts[kind] ?: return@synchronized
                if (count <= 1) counts.remove(kind) else counts[kind] = count - 1
            }
        }
    }

    fun set(kind: BackendResourceKind, count: Int) {
        synchronized(lock) {
            if (count <= 0) counts.remove(kind) else counts[kind] = count
        }
    }

    fun snapshot(): Map<String, Int> = synchronized(lock) {
        counts.entries.associate { (kind, count) -> kind.key to count }
    }

    fun clear() = synchronized(lock) { counts.clear() }
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
