package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RootSystemBarEffectTest {
    @Test
    fun useDarkSystemBarIcons_followsThemeWhenPlayerOverlayHidden() {
        assertTrue(
            useDarkSystemBarIcons(
                darkTheme = false,
                showPlayerOverlay = false,
                playerBackdropColor = Color.Black,
            ),
        )
        assertFalse(
            useDarkSystemBarIcons(
                darkTheme = true,
                showPlayerOverlay = false,
                playerBackdropColor = Color.White,
            ),
        )
    }

    @Test
    fun useDarkSystemBarIcons_followsPlayerBackdropContrastWhenOverlayShown() {
        assertTrue(
            useDarkSystemBarIcons(
                darkTheme = true,
                showPlayerOverlay = true,
                playerBackdropColor = Color.White,
            ),
        )
        assertFalse(
            useDarkSystemBarIcons(
                darkTheme = false,
                showPlayerOverlay = true,
                playerBackdropColor = Color.Black,
            ),
        )
    }
}
