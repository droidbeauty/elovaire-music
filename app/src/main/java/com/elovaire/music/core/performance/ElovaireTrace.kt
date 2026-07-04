package elovaire.music.droidbeauty.app.core.performance

import androidx.tracing.Trace
import androidx.tracing.trace

internal object ElovaireTrace {
    inline fun <T> section(
        name: String,
        block: () -> T,
    ): T {
        return trace(name.take(MAX_TRACE_NAME_LENGTH)) {
            block()
        }
    }

    suspend inline fun <T> suspendSection(
        name: String,
        crossinline block: suspend () -> T,
    ): T {
        Trace.beginSection(name.take(MAX_TRACE_NAME_LENGTH))
        return try {
            block()
        } finally {
            Trace.endSection()
        }
    }

    private const val MAX_TRACE_NAME_LENGTH = 120
}
