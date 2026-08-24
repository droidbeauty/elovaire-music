package elovaire.music.droidbeauty.app.core

import android.os.strictmode.Violation

internal data class StrictModeViolationSnapshot(
    val violationType: String,
    val appStackFrame: String,
    val count: Int,
)

internal object StrictModeViolationRecorder {
    private const val MAX_VIOLATIONS = 32
    private val counts = LinkedHashMap<String, StrictModeViolationSnapshot>()

    @Synchronized
    fun record(violation: Violation) {
        val frame = violation.stackTrace
            .firstOrNull { it.className.startsWith("elovaire.music") }
            ?.let { "${it.className}.${it.methodName}:${it.lineNumber}" }
            ?: "unknown"
        val key = "${violation.javaClass.simpleName}|$frame"
        val current = counts[key]
        if (current != null) {
            counts[key] = current.copy(count = current.count + 1)
        } else if (counts.size < MAX_VIOLATIONS) {
            counts[key] = StrictModeViolationSnapshot(
                violationType = violation.javaClass.simpleName,
                appStackFrame = frame,
                count = 1,
            )
        }
    }

    @Synchronized
    fun snapshot(): List<StrictModeViolationSnapshot> = counts.values.toList()

    @Synchronized
    fun clear() {
        counts.clear()
    }
}
