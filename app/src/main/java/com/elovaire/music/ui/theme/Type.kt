package elovaire.music.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import elovaire.music.app.R

private val GeistFamily = FontFamily(
    Font(R.font.geist_light, FontWeight.Light),
    Font(R.font.geist_regular, FontWeight.Normal),
    Font(R.font.geist_medium, FontWeight.Medium),
    Font(R.font.geist_semibold, FontWeight.SemiBold),
    Font(R.font.geist_bold, FontWeight.Bold),
)

private val BaseTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.7).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 25.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 24.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp,
    ),
)

val LocalTextScale = staticCompositionLocalOf { 1f }

fun elovaireTypography(scaleFactor: Float): Typography {
    return BaseTypography.copy(
        displayLarge = BaseTypography.displayLarge.scaled(scaleFactor),
        headlineMedium = BaseTypography.headlineMedium.scaled(scaleFactor),
        titleLarge = BaseTypography.titleLarge.scaled(scaleFactor),
        bodyLarge = BaseTypography.bodyLarge.scaled(scaleFactor),
        labelLarge = BaseTypography.labelLarge.scaled(scaleFactor),
    )
}

@Composable
fun elovaireScaledSp(baseSize: Float): TextUnit {
    return (baseSize * LocalTextScale.current).sp
}

private fun TextStyle.scaled(scaleFactor: Float): TextStyle {
    return copy(
        fontSize = fontSize.scaled(scaleFactor),
        lineHeight = lineHeight.scaled(scaleFactor),
        letterSpacing = letterSpacing.scaled(scaleFactor),
    )
}

private fun TextUnit.scaled(scaleFactor: Float): TextUnit {
    return if (isUnspecified) this else (value * scaleFactor).sp
}
