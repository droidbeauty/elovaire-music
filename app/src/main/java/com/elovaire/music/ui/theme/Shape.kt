package elovaire.music.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
object ElovaireRadii {
    val topBar: Dp = 0.dp
    val dock: Dp = 20.dp
    val module: Dp = 14.dp
    val card: Dp = 14.dp
    val tile: Dp = 14.dp
    val button: Dp = 16.dp
    val artwork: Dp = 10.dp
    val artworkSmall: Dp = 4.dp
    val input: Dp = 16.dp
    val dialog: Dp = 16.dp
    val pill: Dp = 999.dp
}

fun elovaireShapes(): Shapes {
    return Shapes(
        extraSmall = RoundedCornerShape(ElovaireRadii.tile),
        small = RoundedCornerShape(ElovaireRadii.tile),
        medium = RoundedCornerShape(ElovaireRadii.card),
        large = RoundedCornerShape(ElovaireRadii.module),
        extraLarge = RoundedCornerShape(ElovaireRadii.dock),
    )
}
