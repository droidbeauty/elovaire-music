package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.elovaireListDropdownReveal(
    key: Any?,
    index: Int,
    enabled: Boolean = true,
): Modifier {
    if (!enabled) return this
    val density = LocalDensity.current
    var revealed by remember(key) { mutableStateOf(false) }

    LaunchedEffect(key) {
        withFrameNanos { }
        revealed = true
    }

    val delayMillis = remember(index) { ElovaireMotion.listRevealDelay(index) }
    val alpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = ElovaireMotion.listRevealSpec(delayMillis),
        label = "list_item_reveal_alpha",
    )
    val offsetY by animateDpAsState(
        targetValue = if (revealed) 0.dp else (-8).dp,
        animationSpec = ElovaireMotion.listRevealSpec(delayMillis),
        label = "list_item_reveal_offset",
    )

    return graphicsLayer {
        this.alpha = alpha
        translationY = with(density) { offsetY.toPx() }
    }
}
