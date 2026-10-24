package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Reveals a replacement layout without keeping a second lazy layout attached to its scroll state. */
@Composable
fun Modifier.elovaireContentChangeMotion(contentKey: Any?): Modifier {
    val specs = rememberMotionSpecs()
    val runtime = LocalMotionRuntime.current
    val alpha = remember { Animatable(1f) }
    var previousKey by remember { mutableStateOf(contentKey) }
    LaunchedEffect(contentKey, specs) {
        if (previousKey != contentKey && !runtime.reduceMotion) {
            alpha.snapTo(minOf(alpha.value, 0.82f))
        }
        previousKey = contentKey
        alpha.animateTo(1f, specs.refinedEnter(MotionDuration.Component))
    }
    return graphicsLayer { this.alpha = alpha.value }
}

@Composable
fun Modifier.elovairePressScaleMotion(
    pressed: Boolean,
    pressedScale: Float,
    pressSpec: FiniteAnimationSpec<Float>,
    releaseSpec: FiniteAnimationSpec<Float>,
    label: String,
): Modifier {
    val scale = animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = if (pressed) pressSpec else releaseSpec,
        label = label,
    )
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

@Composable
fun Modifier.elovaireAnimateContentSize(): Modifier {
    val specs = rememberMotionSpecs()
    return animateContentSize(animationSpec = specs.contentSize())
}

@Composable
fun Modifier.elovaireChromePressScale(pressed: Boolean): Modifier {
    val specs = rememberMotionSpecs()
    return elovairePressScaleMotion(
        pressed = pressed,
        pressedScale = MotionScale.ChromePressed,
        pressSpec = specs.pressDown(),
        releaseSpec = specs.chromeRelease(),
        label = "elovaireChromePressScale",
    )
}

@Composable
fun Modifier.elovaireMediaPressScale(pressed: Boolean): Modifier {
    val specs = rememberMotionSpecs()
    return elovairePressScaleMotion(
        pressed = pressed,
        pressedScale = MotionScale.MediaPressed,
        pressSpec = specs.pressDown(),
        releaseSpec = specs.mediaRelease(),
        label = "elovaireMediaPressScale",
    )
}
