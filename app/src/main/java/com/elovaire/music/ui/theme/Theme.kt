package elovaire.music.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import elovaire.music.app.domain.model.TextSizePreset
import elovaire.music.app.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = RoseAccent,
    onPrimary = Cloud,
    secondary = Frost,
    onSecondary = InkText,
    background = Cloud,
    onBackground = InkText,
    surface = Snow,
    onSurface = InkText,
    surfaceVariant = Frost,
    onSurfaceVariant = InkTextSecondary,
)

private val DarkColors = darkColorScheme(
    primary = RoseAccent,
    onPrimary = Cloud,
    secondary = Graphite,
    onSecondary = Cloud,
    background = Night,
    onBackground = Cloud,
    surface = Carbon,
    onSurface = Cloud,
    surfaceVariant = Graphite,
    onSurfaceVariant = Slate,
)

@Composable
fun ElovaireTheme(
    themeMode: ThemeMode,
    textSizePreset: TextSizePreset,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    CompositionLocalProvider(LocalTextScale provides textSizePreset.scaleFactor) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = elovaireTypography(textSizePreset.scaleFactor),
            shapes = elovaireShapes(),
            content = content,
        )
    }
}
