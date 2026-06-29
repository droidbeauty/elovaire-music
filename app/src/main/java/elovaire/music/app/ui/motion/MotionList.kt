package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private const val MaxAnimatedRevealIndex = 11

@Stable
class MotionRevealRegistry {
    private val revealedKeys = mutableStateMapOf<Any, Boolean>()

    fun isRevealed(key: Any): Boolean = revealedKeys[key] == true

    fun markRevealed(key: Any) {
        revealedKeys[key] = true
    }

    fun retainKeys(keys: Set<Any>) {
        if (revealedKeys.isEmpty()) return
        revealedKeys.keys.retainAll(keys)
    }

    fun clear() {
        revealedKeys.clear()
    }
}

@Composable
fun rememberMotionRevealRegistry(): MotionRevealRegistry = remember { MotionRevealRegistry() }

@Composable
fun MotionRevealRegistry.RetainVisibleKeys(keys: Set<Any>) {
    LaunchedEffect(this, keys) {
        retainKeys(keys)
    }
}

fun Modifier.elovaireListReveal(
    itemKey: Any,
    index: Int,
    registry: MotionRevealRegistry,
    enabled: Boolean = true,
): Modifier = composed {
    val previouslyRevealed = remember(itemKey, registry) { registry.isRevealed(itemKey) }
    if (!enabled || previouslyRevealed || index > MaxAnimatedRevealIndex) return@composed this
    val specs = rememberMotionSpecs()
    val density = LocalDensity.current
    var started by remember(itemKey) { mutableStateOf(false) }
    LaunchedEffect(itemKey) {
        withFrameNanos { }
        started = true
        registry.markRevealed(itemKey)
    }
    val delay = specs.listRevealDelay(index)
    val transition = updateTransition(
        targetState = started,
        label = "elovaireListRevealTransition",
    )
    val alpha by transition.animateFloat(
        transitionSpec = {
            specs.tween(
                durationMillis = MotionDuration.ListReveal,
                delayMillis = delay,
                easing = MotionEasing.FadeIn,
            )
        },
        label = "elovaireListRevealAlpha",
    ) { visible -> if (visible) 1f else 0f }
    val offset by transition.animateDp(
        transitionSpec = {
            specs.listReveal(delayMillis = delay)
        },
        label = "elovaireListRevealOffsetY",
    ) { visible -> if (visible) 0.dp else (-8).dp }
    graphicsLayer {
        this.alpha = alpha
        translationY = with(density) { offset.toPx() }
    }
}
