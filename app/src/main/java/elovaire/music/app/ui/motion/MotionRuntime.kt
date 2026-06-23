package elovaire.music.droidbeauty.app.ui.motion

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import kotlin.math.roundToInt

@Immutable
data class MotionRuntime(
    val durationScale: Float,
) {
    val reduceMotion: Boolean
        get() = durationScale <= 0f

    fun duration(milliseconds: Int): Int = when {
        milliseconds <= 0 || reduceMotion -> 0
        else -> (milliseconds * durationScale).roundToInt().coerceAtLeast(1)
    }

    fun delay(milliseconds: Int): Int = when {
        milliseconds <= 0 || reduceMotion -> 0
        else -> (milliseconds * durationScale).roundToInt().coerceAtLeast(0)
    }

    fun duration(milliseconds: Long): Long = when {
        milliseconds <= 0L || reduceMotion -> 0L
        else -> (milliseconds * durationScale).roundToInt().toLong().coerceAtLeast(1L)
    }
}

val LocalMotionRuntime = staticCompositionLocalOf { MotionRuntime(durationScale = 1f) }

@Composable
fun rememberMotionRuntime(): MotionRuntime {
    val context = LocalContext.current
    val resolver = context.contentResolver
    var durationScale by remember(resolver) {
        mutableFloatStateOf(
            runCatching {
                Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
            }.getOrDefault(1f).coerceAtLeast(0f),
        )
    }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                durationScale = runCatching {
                    Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
                }.getOrDefault(1f).coerceAtLeast(0f)
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose { runCatching { resolver.unregisterContentObserver(observer) } }
    }
    return remember(durationScale) { MotionRuntime(durationScale) }
}

@Composable
fun MotionRuntimeProvider(
    runtime: MotionRuntime = rememberMotionRuntime(),
    content: @Composable () -> Unit,
) {
    SideEffect {
        ElovaireMotion.updateSystemDurationScale(runtime.durationScale)
    }
    CompositionLocalProvider(LocalMotionRuntime provides runtime, content = content)
}
