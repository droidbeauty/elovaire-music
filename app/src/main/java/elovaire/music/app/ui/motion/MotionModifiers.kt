package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale

fun Modifier.elovairePressScaleMotion(
    pressed: Boolean,
    pressedScale: Float,
    pressSpec: FiniteAnimationSpec<Float>,
    releaseSpec: FiniteAnimationSpec<Float>,
    label: String,
): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = if (pressed) pressSpec else releaseSpec,
        label = label,
    )
    scale(scale)
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
