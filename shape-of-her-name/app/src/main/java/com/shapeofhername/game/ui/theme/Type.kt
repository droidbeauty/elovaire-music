package com.shapeofhername.game.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.shapeofhername.game.R

private val Garamond = FontFamily(
    Font(R.font.eb_garamond_regular, weight = FontWeight.Normal),
    Font(R.font.eb_garamond_regular, weight = FontWeight.Medium),
    Font(R.font.eb_garamond_regular, weight = FontWeight.SemiBold),
    Font(R.font.eb_garamond_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
)

val ShapeOfHerNameTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Garamond,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Garamond,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Garamond,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Garamond,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 31.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Garamond,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 27.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Garamond,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    ),
)
