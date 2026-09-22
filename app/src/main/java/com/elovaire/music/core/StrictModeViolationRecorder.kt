package elovaire.music.droidbeauty.app.core

import android.os.strictmode.Violation

internal data class StrictModeViolationSnapshot(
    val violationType: String,
    val appStackFrame: String,
    val count: Int,
    val sequence: Long = 0L,
)

internal object StrictModeViolationRecorder {
    private const val MAX_VIOLATIONS = 32
    private val counts = LinkedHashMap<String, StrictModeViolationSnapshot>()
    private var sequence = 0L

    @Synchronized
    fun record(violation: Violation) {
        sequence += 1L
        val frame = violation.stackTrace
            .firstOrNull { it.className.startsWith("elovaire.music") }
            ?.let { "${it.className}.${it.methodName}:${it.lineNumber}" }
            ?: "unknown"
        val key = "${violation.javaClass.simpleName}|$frame"
        val current = counts[key]
        if (current != null) {
            counts[key] = current.copy(count = current.count + 1, sequence = sequence)
        } else {
            if (counts.size == MAX_VIOLATIONS) counts.remove(counts.keys.first())
            counts[key] = StrictModeViolationSnapshot(
                violationType = violation.javaClass.simpleName,
                appStackFrame = frame,
                count = 1,
                sequence = sequence,
            )
        }
    }

    @Synchronized
    fun snapshot(): List<StrictModeViolationSnapshot> = counts.values.toList()

    @Synchronized
    fun snapshotWithMarker(): Pair<Long, List<StrictModeViolationSnapshot>> = sequence to counts.values.toList()

    @Synchronized
    fun sequenceMarker(): Long = sequence

    @Synchronized
    fun snapshotsAfter(marker: Long): List<StrictModeViolationSnapshot> =
        counts.values.filter { it.sequence > marker }

    @Synchronized
    fun clear() {
        counts.clear()
    }
}
