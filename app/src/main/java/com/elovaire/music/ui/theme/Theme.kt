package elovaire.music.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import elovaire.music.app.domain.model.TextSizePreset
import elovaire.music.app.domain.model.ThemeMode

// Light mode palette used across the whole app.
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

// Dark mode palette used across the whole app.
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
    // Resolves whether the app should render in dark mode for the current theme setting.
    val darkTheme = resolveDarkTheme(themeMode = themeMode, systemDark = isSystemInDarkTheme())

    // Picks the full Material color scheme for the resolved mode above.
    val colorScheme = resolvedColorScheme(darkTheme)

    CompositionLocalProvider(LocalTextScale provides textSizePreset.scaleFactor) {
        MaterialTheme(
            colorScheme = colorScheme,
            // Global typography scaling for the text-size setting.
            typography = elovaireTypography(textSizePreset.scaleFactor),
            // Shared shape system for cards, pills, and buttons.
            shapes = elovaireShapes(),
            content = content,
        )
    }
}

fun resolveDarkTheme(
    themeMode: ThemeMode,
    systemDark: Boolean,
): Boolean {
    // Controls how the System / Light / Dark picker maps to real theme output.
    return when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
}

fun themeBackgroundForMode(
    themeMode: ThemeMode,
    systemDark: Boolean,
): Color {
    // Convenience helper used when UI needs the resolved screen background color directly.
    return resolvedColorScheme(resolveDarkTheme(themeMode, systemDark)).background
}

private fun resolvedColorScheme(darkTheme: Boolean): ColorScheme {
    // Single switch point between the light and dark palettes above.
    return if (darkTheme) DarkColors else LightColors
}
