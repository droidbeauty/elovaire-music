package elovaire.music.droidbeauty.app.core.backend

import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig

internal enum class BackendSubsystem {
    Library,
    MediaStore,
    Saf,
    NetworkLibrary,
    Persistence,
    UserData,
    MediaMutation,
    Lyrics,
    Artwork,
    Update,
    BackgroundWork,
    Playback,
}

internal data class BackendOperationMetrics(
    val itemsInput: Int? = null,
    val itemsOutput: Int? = null,
    val rowsRead: Int? = null,
    val rowsChanged: Int? = null,
    val bytesRead: Long? = null,
    val bytesWritten: Long? = null,
    val cacheHits: Int? = null,
    val cacheMisses: Int? = null,
    val cache: String? = null,
    val fallback: Boolean? = null,
)

internal data class BackendOperationContext(
    val id: String,
    val subsystem: BackendSubsystem,
    val startedAtElapsedMs: Long,
) {
    fun fields(
        phase: String,
        elapsedTimeMs: Long,
        extra: Map<String, String> = emptyMap(),
        metrics: BackendOperationMetrics = BackendOperationMetrics(),
    ): Map<String, String> = buildMap(extra.size + 4) {
        put("operation_id", id.take(MAX_OPERATION_ID_LENGTH))
        put("subsystem", subsystem.name)
        put("phase", phase.take(MAX_FIELD_VALUE_LENGTH))
        put("elapsed_ms", (elapsedTimeMs - startedAtElapsedMs).coerceAtLeast(0L).toString())
        metrics.itemsInput?.nonNegative()?.let { put("items_input", it.toString()) }
        metrics.itemsOutput?.nonNegative()?.let { put("items_output", it.toString()) }
        metrics.rowsRead?.nonNegative()?.let { put("rows_read", it.toString()) }
        metrics.rowsChanged?.nonNegative()?.let { put("rows_changed", it.toString()) }
        metrics.bytesRead?.nonNegative()?.let { put("bytes_read", it.toString()) }
        metrics.bytesWritten?.nonNegative()?.let { put("bytes_written", it.toString()) }
        metrics.cacheHits?.nonNegative()?.let { put("cache_hits", it.toString()) }
        metrics.cacheMisses?.nonNegative()?.let { put("cache_misses", it.toString()) }
        metrics.cache?.takeIf { it == "hit" || it == "miss" || it == "disabled" }
            ?.let { put("cache", it) }
        metrics.fallback?.let { put("fallback", it.toString()) }
        extra.asSequence()
            .filter { (key, _) -> key in SAFE_EXTRA_FIELDS }
            .take(MAX_EXTRA_FIELDS)
            .forEach { (key, value) -> put(key, value.safeBackendValue()) }
    }
}

internal sealed interface BackendEvent {
    val name: String
    val fields: Map<String, String>

    data class LibraryScanStarted(override val fields: Map<String, String>) : BackendEvent {
        override val name = "LibraryScanStarted"
    }

    data class LibraryScanCompleted(override val fields: Map<String, String>) : BackendEvent {
        override val name = "LibraryScanCompleted"
    }

    data class LibraryScanFailed(override val fields: Map<String, String>) : BackendEvent {
        override val name = "LibraryScanFailed"
    }

    data class LibraryRefreshCoalesced(override val fields: Map<String, String>) : BackendEvent {
        override val name = "LibraryRefreshCoalesced"
    }

    data class OperationStarted(override val fields: Map<String, String>) : BackendEvent {
        override val name = "OperationStarted"
    }

    data class OperationCompleted(override val fields: Map<String, String>) : BackendEvent {
        override val name = "OperationCompleted"
    }

    data class OperationFailed(override val fields: Map<String, String>) : BackendEvent {
        override val name = "OperationFailed"
    }

    data class OperationCancelled(override val fields: Map<String, String>) : BackendEvent {
        override val name = "OperationCancelled"
    }

    data class MediaMutationStarted(override val fields: Map<String, String>) : BackendEvent {
        override val name = "MediaMutationStarted"
    }

    data class MediaMutationCompleted(override val fields: Map<String, String>) : BackendEvent {
        override val name = "MediaMutationCompleted"
    }

    data class MediaMutationFailed(override val fields: Map<String, String>) : BackendEvent {
        override val name = "MediaMutationFailed"
    }

    data class PlaybackUnsupportedFormat(override val fields: Map<String, String>) : BackendEvent {
        override val name = "PlaybackUnsupportedFormat"
    }
}

internal interface BackendEventSink {
    fun emit(event: BackendEvent)
}

internal inline fun BackendEventSink.emitLazy(event: () -> BackendEvent) {
    if (this === NoOpBackendEventSink || (this === LogcatBackendEventSink && !BuildConfig.DEBUG)) return
    emit(event())
}

internal object NoOpBackendEventSink : BackendEventSink {
    override fun emit(event: BackendEvent) = Unit
}

internal object LogcatBackendEventSink : BackendEventSink {
    private const val TAG = "ElovaireBackend"

    override fun emit(event: BackendEvent) {
        BackendDiagnostics.record(event)
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, buildString {
            append(event.name)
            event.fields.entries
                .sortedBy { it.key }
                .forEach { (key, value) ->
                    append(' ')
                    append(key)
                    append('=')
                    append(value)
                }
        })
    }
}

private const val MAX_OPERATION_ID_LENGTH = 64
private const val MAX_FIELD_VALUE_LENGTH = 128
private const val MAX_EXTRA_FIELDS = 24
private val SAFE_EXTRA_FIELDS = setOf(
    "albums",
    "enrich_metadata",
    "error_type",
    "force_index",
    "recovered",
    "retry",
    "songs",
    "targeted_network_sources",
    "targeted_paths",
    "type",
)

private fun Int.nonNegative(): Int? = takeIf { it >= 0 }

private fun Long.nonNegative(): Long? = takeIf { it >= 0L }

private fun String.safeBackendValue(): String {
    return filter { it in '\u0020'..'\u007e' }.take(MAX_FIELD_VALUE_LENGTH)
}
