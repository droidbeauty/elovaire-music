package elovaire.music.app.ui.theme

import androidx.compose.foundation.OverscrollFactory
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberPlatformOverscrollFactory
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun rememberElovaireOverscrollFactory(): OverscrollFactory {
    return rememberPlatformOverscrollFactory(
        glowColor = Color.Transparent,
        glowDrawPadding = PaddingValues(),
    )
}
