package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal enum class RootWindowWidthClass {
    Compact,
    Medium,
    Expanded,
    Large,
    ExtraLarge,
}

internal enum class RootWindowHeightClass {
    Compact,
    Medium,
    Expanded,
}

internal data class RootWindowSizeClass(
    val width: RootWindowWidthClass,
    val height: RootWindowHeightClass,
    val isLandscape: Boolean = false,
)

internal val LocalRootWindowSizeClass = compositionLocalOf {
    RootWindowSizeClass(RootWindowWidthClass.Compact, RootWindowHeightClass.Expanded)
}

internal fun rootWindowSizeClass(width: Dp, height: Dp): RootWindowSizeClass = RootWindowSizeClass(
    width = when {
        width < 600.dp -> RootWindowWidthClass.Compact
        width < 840.dp -> RootWindowWidthClass.Medium
        width < 1200.dp -> RootWindowWidthClass.Expanded
        width < 1600.dp -> RootWindowWidthClass.Large
        else -> RootWindowWidthClass.ExtraLarge
    },
    height = when {
        height < 480.dp -> RootWindowHeightClass.Compact
        height < 900.dp -> RootWindowHeightClass.Medium
        else -> RootWindowHeightClass.Expanded
    },
    isLandscape = width > height * 1.15f,
)

internal fun adaptiveGridColumnCount(
    phoneColumns: Int,
    widthClass: RootWindowWidthClass,
): Int = phoneColumns + when (widthClass) {
    RootWindowWidthClass.Compact -> 0
    RootWindowWidthClass.Medium -> 1
    RootWindowWidthClass.Expanded -> 2
    RootWindowWidthClass.Large -> 3
    RootWindowWidthClass.ExtraLarge -> 4
}
