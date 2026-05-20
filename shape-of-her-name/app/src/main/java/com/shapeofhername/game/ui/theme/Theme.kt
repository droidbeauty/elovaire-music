package com.shapeofhername.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DreamColorScheme = darkColorScheme(
    primary = SoftBlue,
    onPrimary = NightPaper,
    secondary = RoseAsh,
    onSecondary = NightPaper,
    background = NightPaper,
    onBackground = MistInk,
    surface = WarmShadow,
    onSurface = MistInk,
)

@Composable
fun ShapeOfHerNameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DreamColorScheme,
        typography = ShapeOfHerNameTypography,
        content = content,
    )
}
