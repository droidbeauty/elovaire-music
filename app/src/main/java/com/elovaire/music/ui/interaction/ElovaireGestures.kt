package elovaire.music.droidbeauty.app.ui.interaction

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.abs

@Immutable
data class CompactBarGestureActions(
    val onTap: () -> Unit,
    val onSwipePrevious: () -> Unit,
    val onSwipeNext: () -> Unit,
    val onDragDelta: (Float) -> Unit = {},
    val onGestureFinished: () -> Unit = {},
)

fun Modifier.compactBarGestures(
    enabled: Boolean,
    actions: CompactBarGestureActions,
): Modifier = pointerInput(enabled, actions) {
    if (!enabled) return@pointerInput

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var totalDx = 0f
        var isDragging = false
        val slop = viewConfiguration.touchSlop

        do {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            totalDx += change.positionChange().x

            if (!isDragging && abs(totalDx) > slop) {
                isDragging = true
            }

            if (isDragging) {
                actions.onDragDelta(change.positionChange().x)
                change.consume()
            }
        } while (event.changes.any { it.pressed })

        if (isDragging) {
            when {
                totalDx > slop * 3f -> actions.onSwipePrevious()
                totalDx < -slop * 3f -> actions.onSwipeNext()
            }
            actions.onGestureFinished()
        } else {
            actions.onTap()
        }
    }
}
