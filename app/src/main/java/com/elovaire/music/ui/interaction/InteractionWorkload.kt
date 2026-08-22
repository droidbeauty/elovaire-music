package elovaire.music.droidbeauty.app.ui.interaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import elovaire.music.droidbeauty.app.core.AppBackgroundWorkPolicy

internal val LocalInteractionWorkload =
    staticCompositionLocalOf<AppBackgroundWorkPolicy?> { null }

@Composable
internal fun InteractionCriticalWindow(active: Boolean) {
    val workload = LocalInteractionWorkload.current
    DisposableEffect(workload, active) {
        val lease = if (active) workload?.acquireInteractionCritical() else null
        onDispose { lease?.close() }
    }
}
