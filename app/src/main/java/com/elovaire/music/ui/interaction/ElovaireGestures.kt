package elovaire.music.droidbeauty.app.ui.interaction

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
): Modifier = composed {
    val latestActions by rememberUpdatedState(actions)
    pointerInput(enabled) {
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
                    latestActions.onDragDelta(change.positionChange().x)
                    change.consume()
                }
            } while (event.changes.any { it.pressed })

            if (isDragging) {
                when {
                    totalDx > slop * 3f -> latestActions.onSwipePrevious()
                    totalDx < -slop * 3f -> latestActions.onSwipeNext()
                }
                latestActions.onGestureFinished()
            } else {
                latestActions.onTap()
            }
        }
    }
}
