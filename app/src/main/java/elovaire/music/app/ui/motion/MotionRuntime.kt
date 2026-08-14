package elovaire.music.droidbeauty.app.ui.motion

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.awaitCancellation

@Immutable
data class MotionRuntime(
    val durationScale: Float,
) {
    val reduceMotion: Boolean
        get() = durationScale <= 0f

    /** Compose applies MotionDurationScale to animation clocks; avoid scaling twice here. */
    fun duration(milliseconds: Int): Int = if (reduceMotion) 0 else milliseconds.coerceAtLeast(0)

    fun delay(milliseconds: Int): Int = if (reduceMotion) 0 else milliseconds.coerceAtLeast(0)

    fun duration(milliseconds: Long): Long = if (reduceMotion) 0L else milliseconds.coerceAtLeast(0L)
}

val LocalMotionRuntime = staticCompositionLocalOf { MotionRuntime(durationScale = 1f) }

@Composable
fun rememberMotionRuntime(): MotionRuntime {
    val context = LocalContext.current.applicationContext
    val initialDurationScale = remember(context) { systemAnimationScale(context) }
    val durationScale by produceState(
        initialValue = initialDurationScale,
        context,
    ) {
        val resolver = context.contentResolver
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                value = systemAnimationScale(context)
            }
        }
        val settingUris = listOf(
            Settings.Global.ANIMATOR_DURATION_SCALE,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            Settings.Global.WINDOW_ANIMATION_SCALE,
        ).map(Settings.Global::getUriFor)
        try {
            settingUris.forEach { uri ->
                resolver.registerContentObserver(uri, false, observer)
            }
            awaitCancellation()
        } finally {
            runCatching { resolver.unregisterContentObserver(observer) }
        }
    }
    return MotionRuntime(durationScale = durationScale)
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
