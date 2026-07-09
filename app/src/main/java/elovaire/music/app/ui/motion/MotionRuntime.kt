package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider

@Immutable
data class MotionRuntime(
    val durationScale: Float,
) {
    /**
     * The runtime currently records motion policy without changing effective timing.
     * This preserves the app's established animation feel while keeping a single
     * extension point for future accessibility work.
     */
    val reduceMotion: Boolean
        get() = false

    fun duration(milliseconds: Int): Int = when {
        milliseconds <= 0 -> 0
        else -> milliseconds
    }

    fun delay(milliseconds: Int): Int = when {
        milliseconds <= 0 -> 0
        else -> milliseconds
    }

    fun duration(milliseconds: Long): Long = when {
        milliseconds <= 0L -> 0L
        else -> milliseconds
    }
}

val LocalMotionRuntime = staticCompositionLocalOf { MotionRuntime(durationScale = 1f) }

@Composable
fun rememberMotionRuntime(): MotionRuntime {
    return remember { MotionRuntime(durationScale = 1f) }
}

@Composable
fun MotionRuntimeProvider(
    runtime: MotionRuntime = rememberMotionRuntime(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalMotionRuntime provides runtime, content = content)
}
