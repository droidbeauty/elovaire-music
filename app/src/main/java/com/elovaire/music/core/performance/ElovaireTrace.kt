package elovaire.music.droidbeauty.app.core.performance

import elovaire.music.droidbeauty.app.BuildConfig
import androidx.tracing.Trace
import java.util.concurrent.atomic.AtomicInteger

internal object ElovaireTrace {
    @Suppress("TooGenericExceptionCaught")
    inline fun <T> section(
        name: String,
        block: () -> T,
    ): T {
        record(name)
        val traceName = name.take(MAX_TRACE_NAME_LENGTH)
        try {
            Trace.beginSection(traceName)
        } catch (failure: RuntimeException) {
            if (failure.message?.contains("not mocked", ignoreCase = true) == true) return block()
            throw failure
        }
        return try {
            block()
        } finally {
            runCatching { Trace.endSection() }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    suspend fun <T> suspendSection(
        name: String,
        block: suspend () -> T,
    ): T {
        val traceName = name.take(MAX_TRACE_NAME_LENGTH)
        record(traceName)
        val cookie = nextAsyncCookie.getAndUpdate { current ->
            if (current == Int.MAX_VALUE) 1 else current + 1
        }
        val tracingStarted = try {
            Trace.beginAsyncSection(traceName, cookie)
            true
        } catch (failure: RuntimeException) {
            if (failure.message?.contains("not mocked", ignoreCase = true) == true) {
                false
            } else {
                throw failure
            }
        }
        return try {
            block()
        } finally {
            if (tracingStarted) {
                Trace.endAsyncSection(traceName, cookie)
            }
        }
    }

    private val nextAsyncCookie = AtomicInteger(1)
    private val recordedSections = ArrayDeque<String>()

    @Synchronized
    fun recordedSectionNames(): List<String> = recordedSections.toList()

    @Synchronized
    fun clearRecordedSections() {
        recordedSections.clear()
    }

    @Synchronized
    private fun record(name: String) {
        if (!BuildConfig.DEBUG) return
        if (recordedSections.size == MAX_RECORDED_SECTIONS) recordedSections.removeFirst()
        recordedSections.addLast(name.take(MAX_TRACE_NAME_LENGTH))
    }

    private const val MAX_RECORDED_SECTIONS = 128
    private const val MAX_TRACE_NAME_LENGTH = 120
}
