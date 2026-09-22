package elovaire.music.droidbeauty.app.quality

import android.content.Context
import elovaire.music.droidbeauty.app.ElovaireApp
import elovaire.music.droidbeauty.app.core.StrictModeViolationRecorder
import elovaire.music.droidbeauty.app.core.StrictModeViolationSnapshot
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticsRuntime
import elovaire.music.droidbeauty.app.core.backend.BackendEventSnapshot
import elovaire.music.droidbeauty.app.core.performance.ElovairePerformance
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.core.performance.JankWindowSnapshot
import elovaire.music.droidbeauty.app.core.performance.MotionDiagnosticEvent
import elovaire.music.droidbeauty.app.core.performance.MotionDiagnosticRecorder

internal fun appDiagnostics(context: Context): BackendDiagnosticsRuntime =
    (context.applicationContext as ElovaireApp).container.backendDiagnosticsRuntime

internal data class DiagnosticJourneyBaseline(
    val backendSequence: Long,
    val resourceCounts: Map<String, Int>,
    val strictModeSequence: Long,
    val strictModeCounts: Map<Pair<String, String>, Int>,
    val jankSequence: Long,
    val traceSequence: Long,
    val motionSequence: Long,
)

internal data class DiagnosticJourneyDelta(
    val backendEvents: List<BackendEventSnapshot>,
    val resourceDeltas: Map<String, Int>,
    val strictModeViolations: List<StrictModeViolationSnapshot>,
    val jankWindows: List<JankWindowSnapshot>,
    val traceSections: List<String>,
    val motionEvents: List<MotionDiagnosticEvent>,
    val workerFailures: List<BackendEventSnapshot>,
    val droppedBackendEvents: Int,
    val droppedMotionEvents: Int,
)

internal fun BackendDiagnosticsRuntime.captureJourneyBaseline(): DiagnosticJourneyBaseline {
    val (strictModeSequence, strictViolations) = StrictModeViolationRecorder.snapshotWithMarker()
    return DiagnosticJourneyBaseline(
        backendSequence = sequenceMarker(),
        resourceCounts = resources.snapshot(),
        strictModeSequence = strictModeSequence,
        strictModeCounts = strictViolations.associate { (it.violationType to it.appStackFrame) to it.count },
        jankSequence = ElovairePerformance.jankSequenceMarker(),
        traceSequence = ElovaireTrace.sequenceMarker(),
        motionSequence = MotionDiagnosticRecorder.sequenceMarker(),
    )
}

internal fun BackendDiagnosticsRuntime.captureJourneyDelta(
    baseline: DiagnosticJourneyBaseline,
): DiagnosticJourneyDelta {
    val (latestBackendSequence, events) = eventsSince(baseline.backendSequence)
    val expectedBackendEvents = (latestBackendSequence - baseline.backendSequence).coerceAtLeast(0L)
    val droppedEvents = (expectedBackendEvents - events.size).coerceAtLeast(0L)
        .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    val currentResources = resources.snapshot()
    val (latestMotionSequence, motionEvents) = MotionDiagnosticRecorder.eventsSince(baseline.motionSequence)
    val resourceDeltas = (baseline.resourceCounts.keys + currentResources.keys)
        .associateWith { (currentResources[it] ?: 0) - (baseline.resourceCounts[it] ?: 0) }
        .filterValues { it != 0 }
    val strictViolations = StrictModeViolationRecorder.snapshotsAfter(baseline.strictModeSequence)
        .mapNotNull { current ->
            val previousCount = baseline.strictModeCounts[current.violationType to current.appStackFrame] ?: 0
            val deltaCount = current.count - previousCount
            current.copy(count = deltaCount).takeIf { deltaCount > 0 }
        }
    return DiagnosticJourneyDelta(
        backendEvents = events,
        resourceDeltas = resourceDeltas,
        strictModeViolations = strictViolations,
        jankWindows = ElovairePerformance.jankWindowsAfter(baseline.jankSequence),
        traceSections = ElovaireTrace.sectionNamesAfter(baseline.traceSequence),
        motionEvents = motionEvents,
        workerFailures = events.filter { it.name == "WorkerFailed" },
        droppedBackendEvents = droppedEvents,
        droppedMotionEvents = (latestMotionSequence - baseline.motionSequence - motionEvents.size)
            .coerceAtLeast(0L)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt(),
    )
}
