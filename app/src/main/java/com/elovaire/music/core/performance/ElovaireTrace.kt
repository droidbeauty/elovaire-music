package elovaire.music.droidbeauty.app.core.performance

import androidx.tracing.Trace
import androidx.tracing.trace
import java.util.concurrent.atomic.AtomicInteger

internal object ElovaireTrace {
    inline fun <T> section(
        name: String,
        block: () -> T,
    ): T {
        return trace(name.take(MAX_TRACE_NAME_LENGTH)) {
            block()
        }
    }

    suspend fun <T> suspendSection(
        name: String,
        block: suspend () -> T,
    ): T {
        val traceName = name.take(MAX_TRACE_NAME_LENGTH)
        val cookie = nextAsyncCookie.getAndUpdate { current ->
            if (current == Int.MAX_VALUE) 1 else current + 1
        }
        Trace.beginAsyncSection(traceName, cookie)
        return try {
            block()
        } finally {
            Trace.endAsyncSection(traceName, cookie)
        }
    }

    private val nextAsyncCookie = AtomicInteger(1)
    private const val MAX_TRACE_NAME_LENGTH = 120
}
