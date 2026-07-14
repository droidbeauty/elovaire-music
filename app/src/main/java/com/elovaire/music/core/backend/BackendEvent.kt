package elovaire.music.droidbeauty.app.core.backend

import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig

internal enum class BackendSubsystem {
    Library,
    MediaMutation,
    Playback,
    Update,
}

internal data class BackendOperationContext(
    val id: String,
    val subsystem: BackendSubsystem,
    val startedAtElapsedMs: Long,
) {
    fun fields(
        phase: String,
        elapsedTimeMs: Long,
        extra: Map<String, String> = emptyMap(),
    ): Map<String, String> = buildMap(extra.size + 4) {
        put("operation_id", id)
        put("subsystem", subsystem.name)
        put("phase", phase)
        put("elapsed_ms", (elapsedTimeMs - startedAtElapsedMs).coerceAtLeast(0L).toString())
        putAll(extra)
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

internal object NoOpBackendEventSink : BackendEventSink {
    override fun emit(event: BackendEvent) = Unit
}

internal object LogcatBackendEventSink : BackendEventSink {
    private const val TAG = "ElovaireBackend"

    override fun emit(event: BackendEvent) {
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
