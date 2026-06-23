package elovaire.music.droidbeauty.app.ui.interaction

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion

@Immutable
data class ElovaireInteractionSpecs(
    val chromePressScale: Float = 0.965f,
    val mediaPressScale: Float = 0.94f,
)

object ElovaireInteraction {
    val specs = ElovaireInteractionSpecs()
}

@Composable
fun rememberElovaireInteractionSource(): MutableInteractionSource {
    return remember { MutableInteractionSource() }
}

fun Modifier.elovairePressScale(
    enabled: Boolean = true,
    pressedScale: Float = ElovaireInteraction.specs.chromePressScale,
    animationSpec: FiniteAnimationSpec<Float> = ElovaireMotion.chromeReleaseSpec(),
    interactionSource: MutableInteractionSource? = null,
    label: String = "elovairePressScale",
): Modifier = composed {
    if (!enabled) return@composed this
    val resolvedInteractionSource = interactionSource ?: rememberElovaireInteractionSource()
    val pressed by resolvedInteractionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = if (pressed) {
            ElovaireMotion.pressDownSpec()
        } else {
            animationSpec
        },
        label = label,
    )
    scale(scale)
}

fun Modifier.consumePointersWithoutSemantics(): Modifier {
    return clearAndSetSemantics {}
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Final)
                    event.changes.forEach { change ->
                        if (!change.isConsumed) {
                            change.consume()
                        }
                    }
                }
            }
        }
}
