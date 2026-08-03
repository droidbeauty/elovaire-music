package elovaire.music.droidbeauty.app.ui.motion

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

@Immutable
data class MotionRuntime(
    val durationScale: Float,
) {
    val reduceMotion: Boolean
        get() = durationScale <= 0f

    fun duration(milliseconds: Int): Int = when {
        milliseconds <= 0 -> 0
        reduceMotion -> 0
        else -> (milliseconds * durationScale).toInt().coerceAtLeast(1)
    }

    fun delay(milliseconds: Int): Int = when {
        milliseconds <= 0 -> 0
        reduceMotion -> 0
        else -> (milliseconds * durationScale).toInt().coerceAtLeast(1)
    }

    fun duration(milliseconds: Long): Long = when {
        milliseconds <= 0L -> 0L
        reduceMotion -> 0L
        else -> (milliseconds * durationScale).toLong().coerceAtLeast(1L)
    }
}

val LocalMotionRuntime = staticCompositionLocalOf { MotionRuntime(durationScale = 1f) }

@Composable
fun rememberMotionRuntime(): MotionRuntime {
    val context = LocalContext.current
    return remember(context) { MotionRuntime(durationScale = systemAnimationScale(context)) }
}

private fun systemAnimationScale(context: Context): Float {
    val resolver = context.contentResolver
    val animatorScale = Settings.Global.getFloat(
        resolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    )
    val transitionScale = Settings.Global.getFloat(
        resolver,
        Settings.Global.TRANSITION_ANIMATION_SCALE,
        1f,
    )
    val windowScale = Settings.Global.getFloat(
        resolver,
        Settings.Global.WINDOW_ANIMATION_SCALE,
        1f,
    )
    return minOf(animatorScale, transitionScale, windowScale).coerceAtLeast(0f)
}

@Composable
fun MotionRuntimeProvider(
    runtime: MotionRuntime = rememberMotionRuntime(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalMotionRuntime provides runtime, content = content)
}
