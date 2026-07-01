package elovaire.music.droidbeauty.app.core.performance

import android.os.Trace

internal object ElovaireTrace {
    inline fun <T> section(
        name: String,
        block: () -> T,
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
