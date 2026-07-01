package elovaire.music.droidbeauty.app.ui.performance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import elovaire.music.droidbeauty.app.core.performance.ElovairePerformance

@Composable
internal fun PerformanceState(
    key: String,
    value: String?,
) {
    DisposableEffect(key, value) {
        if (value == null) {
            ElovairePerformance.removeState(key)
        } else {
            ElovairePerformance.putState(key, value)
        }
        onDispose {
            ElovairePerformance.removeState(key)
        }
    }
}

@Composable
internal fun PerformanceScreenState(screen: String) {
    PerformanceState(key = "screen", value = screen)
}
