package elovaire.music.droidbeauty.app.ui.screens

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

internal fun useLightSystemBarIcons(
    darkTheme: Boolean,
    showPlayerOverlay: Boolean,
    playerContentColor: Color,
): Boolean {
    return if (showPlayerOverlay) {
        playerContentColor.luminance() < 0.56f
    } else {
        !darkTheme
    }
}

@Composable
internal fun RootSystemBarEffect(
    darkTheme: Boolean,
    showPlayerOverlay: Boolean,
    playerContentColor: Color,
) {
    val rootView = LocalView.current
    SideEffect {
        val window = (rootView.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, rootView)
        val usesLightSystemBarIcons = useLightSystemBarIcons(
            darkTheme = darkTheme,
            showPlayerOverlay = showPlayerOverlay,
            playerContentColor = playerContentColor,
        )
        controller.isAppearanceLightStatusBars = usesLightSystemBarIcons
        controller.isAppearanceLightNavigationBars = usesLightSystemBarIcons
    }
}
