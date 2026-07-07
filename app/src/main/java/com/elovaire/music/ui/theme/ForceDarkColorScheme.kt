package elovaire.music.droidbeauty.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
internal fun ForceDarkColorScheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = elovaireResolvedColorScheme(darkTheme = true),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}
