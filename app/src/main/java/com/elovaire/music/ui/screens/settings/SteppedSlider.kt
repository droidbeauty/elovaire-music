package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.InkText
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun SteppedSlider(
    selectedIndex: Int,
    stepCount: Int,
    onSelectedIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionSpecs = rememberMotionSpecs()
    val currentOnSelectedIndexChanged by rememberUpdatedState(onSelectedIndexChanged)
    val currentSelectedIndex = rememberUpdatedState(selectedIndex)
    val safeStepCount = stepCount.coerceAtLeast(2)
    val maxIndex = safeStepCount - 1
    val safeSelectedIndex = selectedIndex.coerceIn(0, maxIndex)
    val knobSize = 20.dp
    val dotColor = MaterialTheme.colorScheme.onSurface
    val lineColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.2f)
    }
    val knobColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText
    } else {
        Color.White
    }
    var isDragging by remember { mutableStateOf(false) }
    var dragCenterPx by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .horizontalGestureSafe(),
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val knobSizePx = with(density) { knobSize.toPx() }
        val selectedCenterPx = maxWidthPx * safeSelectedIndex / maxIndex.toFloat()
        val stepCenters = remember(maxWidthPx, maxIndex) {
            List(safeStepCount) { index ->
                maxWidthPx * index / maxIndex.toFloat()
            }
        }
        LaunchedEffect(selectedCenterPx, maxWidthPx) {
            if (!isDragging) {
                dragCenterPx = selectedCenterPx
            }
        }
        val knobOffset by animateDpAsState(
            targetValue = with(density) {
                ((if (isDragging) dragCenterPx else selectedCenterPx) - knobSizePx / 2f).toDp()
            },
            animationSpec = if (isDragging) {
                snap()
            } else {
                motionSpecs.spring(
                    dampingRatio = 0.82f,
                    stiffness = 480f,
                )
            },
            label = "stepped_slider_knob_offset",
        )
        val updateFromPosition: (Float) -> Unit = { xPosition ->
            val clampedX = xPosition.coerceIn(0f, maxWidthPx)
            dragCenterPx = clampedX
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(maxWidthPx, maxIndex) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var lastReportedIndex = currentSelectedIndex.value
                        fun updateFromPointer(xPosition: Float) {
                            if (maxWidthPx <= 0f) return
                            val clampedX = xPosition.coerceIn(0f, maxWidthPx)
                            updateFromPosition(clampedX)
                            val targetIndex = stepCenters
                                .withIndex()
                                .minByOrNull { (_, center) -> abs(center - clampedX) }
                                ?.index
                                ?: lastReportedIndex
                            if (targetIndex != lastReportedIndex) {
                                lastReportedIndex = targetIndex
                                currentOnSelectedIndexChanged(targetIndex)
                            }
                        }
                        updateFromPointer(down.position.x)
                        isDragging = true
                        try {
                            drag(down.id) { change ->
                                change.consume()
                                updateFromPointer(change.position.x)
                            }
                        } finally {
                            isDragging = false
                        }
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .height(2.dp)
                    .background(lineColor, RoundedCornerShape(ElovaireRadii.pill)),
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                val selectedDotRadius = 3.5.dp.toPx()
                val defaultDotRadius = 2.5.dp.toPx()
                val centerY = size.height / 2f
                repeat(safeStepCount) { index ->
                    drawCircle(
                        color = dotColor,
                        radius = if (index == safeSelectedIndex) selectedDotRadius else defaultDotRadius,
                        center = Offset(size.width * index / maxIndex.toFloat(), centerY),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = knobOffset.roundToPx(), y = 0) }
                    .size(knobSize)
                    .background(knobColor, CircleShape)
                    .align(Alignment.CenterStart),
            )
        }
    }
}
