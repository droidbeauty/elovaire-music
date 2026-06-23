package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Stable
class MotionRevealRegistry {
    private val revealedKeys = mutableStateMapOf<Any, Boolean>()

    fun isRevealed(key: Any): Boolean = revealedKeys[key] == true

    fun markRevealed(key: Any) {
        revealedKeys[key] = true
    }

    fun retainKeys(keys: Set<Any>) {
        revealedKeys.keys.retainAll(keys)
    }
}

@Composable
fun rememberMotionRevealRegistry(): MotionRevealRegistry = remember { MotionRevealRegistry() }

fun Modifier.elovaireListReveal(
    itemKey: Any,
    index: Int,
    registry: MotionRevealRegistry,
    enabled: Boolean = true,
): Modifier = composed {
    val previouslyRevealed = remember(itemKey, registry) { registry.isRevealed(itemKey) }
    if (!enabled || previouslyRevealed) return@composed this
    val specs = rememberMotionSpecs()
    val density = LocalDensity.current
    var started by remember(itemKey) { mutableStateOf(false) }
    LaunchedEffect(itemKey) {
        withFrameNanos { }
        started = true
        registry.markRevealed(itemKey)
    }
    val delay = (index.coerceAtLeast(0) * 14).coerceAtMost(90)
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = specs.tween(
            durationMillis = MotionDuration.ListReveal,
            delayMillis = delay,
            easing = MotionEasing.FadeIn,
        ),
        label = "elovaireListRevealAlpha",
    )
    val offset by animateDpAsState(
        targetValue = if (started) 0.dp else (-8).dp,
        animationSpec = specs.tween(
            durationMillis = MotionDuration.ListReveal,
            delayMillis = delay,
            easing = MotionEasing.RefinedDecelerate,
        ),
        label = "elovaireListRevealOffset",
    )
    graphicsLayer {
        this.alpha = alpha
        translationY = with(density) { offset.toPx() }
    }
}
