package elovaire.music.droidbeauty.app.core.performance

import elovaire.music.droidbeauty.app.BuildConfig

internal data class MotionDiagnosticEvent(
    val sequence: Long,
    val interaction: String?,
    val surfaceId: String,
    val phase: String,
    val visible: Boolean?,
    val overlappingMotionCount: Int,
)

/** Bounded transition-level diagnostics. It records no animation values or user content. */
internal object MotionDiagnosticRecorder {
    private const val MAX_EVENTS = 256
    private const val MAX_SURFACE_ID_LENGTH = 64
    private val lock = Any()
    private val events = ArrayDeque<MotionDiagnosticEvent>()
    private val activeSurfaces = mutableSetOf<String>()
    private var sequence = 0L
    private var currentInteraction: String? = null

    fun recordInteractionState(value: String?) {
        if (!BuildConfig.DEBUG) return
        val next = value?.takeIf { it in INTERACTION_IDS }
        synchronized(lock) {
            if (next == currentInteraction) return
            currentInteraction?.let { append(it, "root", "interaction_end", null) }
            currentInteraction = next
            next?.let { append(it, "root", "interaction_begin", null) }
        }
    }

    fun recordMotion(surfaceId: String, phase: String, visible: Boolean?) {
        if (!BuildConfig.DEBUG) return
        val safeSurfaceId = surfaceId
            .take(MAX_SURFACE_ID_LENGTH)
            .takeIf { it.isNotBlank() && it.all { char -> char.isLetterOrDigit() || char in "_.-" } }
            ?: "unknown"
        synchronized(lock) {
            when (phase) {
                "motion_enter", "motion_exit", "motion_reversed" -> activeSurfaces.add(safeSurfaceId)
                "motion_settled" -> activeSurfaces.remove(safeSurfaceId)
            }
            append(currentInteraction, safeSurfaceId, phase, visible)
        }
    }

    fun sequenceMarker(): Long = synchronized(lock) { sequence }

    fun eventsSince(marker: Long): Pair<Long, List<MotionDiagnosticEvent>> = synchronized(lock) {
        sequence to events.filter { it.sequence > marker }
    }

    private fun append(
        interaction: String?,
        surfaceId: String,
        phase: String,
        visible: Boolean?,
    ) {
        if (events.size == MAX_EVENTS) events.removeFirst()
        events.addLast(
            MotionDiagnosticEvent(
                sequence = ++sequence,
                interaction = interaction,
                surfaceId = surfaceId,
                phase = phase,
                visible = visible,
                overlappingMotionCount = activeSurfaces.size,
            ),
        )
    }

    private val INTERACTION_IDS = setOf("back", "bottom_nav", "navigation")
}
