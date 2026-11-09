package elovaire.music.droidbeauty.app.ui.screens

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

internal fun useDarkSystemBarIcons(
    darkTheme: Boolean,
    showPlayerOverlay: Boolean,
    playerBackdropColor: Color,
): Boolean {
    return if (showPlayerOverlay) {
        Color.Black.contrastRatioAgainst(playerBackdropColor) >=
            Color.White.contrastRatioAgainst(playerBackdropColor)
    } else {
        !darkTheme
    }
}

@Composable
internal fun RootSystemBarEffect(
    darkTheme: Boolean,
    showPlayerOverlay: Boolean,
    playerBackdropColor: Color,
) {
    val rootView = LocalView.current
    SideEffect {
        val window = (rootView.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, rootView)
        val usesDarkSystemBarIcons = useDarkSystemBarIcons(
            darkTheme = darkTheme,
            showPlayerOverlay = showPlayerOverlay,
            playerBackdropColor = playerBackdropColor,
        )
        controller.isAppearanceLightStatusBars = usesDarkSystemBarIcons
        controller.isAppearanceLightNavigationBars = usesDarkSystemBarIcons
    }
}
