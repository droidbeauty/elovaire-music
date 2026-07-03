package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RootSystemBarEffectTest {
    @Test
    fun useLightSystemBarIcons_followsThemeWhenPlayerOverlayHidden() {
        assertTrue(
            useLightSystemBarIcons(
                darkTheme = false,
                showPlayerOverlay = false,
                playerContentColor = Color.Black,
            ),
        )
        assertFalse(
            useLightSystemBarIcons(
                darkTheme = true,
                showPlayerOverlay = false,
                playerContentColor = Color.White,
            ),
        )
    }

    @Test
    fun useLightSystemBarIcons_followsPlayerContentContrastWhenOverlayShown() {
        assertTrue(
            useLightSystemBarIcons(
                darkTheme = true,
                showPlayerOverlay = true,
                playerContentColor = Color.Black,
            ),
        )
        assertFalse(
            useLightSystemBarIcons(
                darkTheme = false,
                showPlayerOverlay = true,
                playerContentColor = Color.White,
            ),
        )
    }
}
