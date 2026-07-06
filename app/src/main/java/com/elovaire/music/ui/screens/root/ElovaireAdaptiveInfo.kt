package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ElovaireWindowMode {
    CompactPhone,
    Medium,
    Expanded,
}

enum class ElovairePostureMode {
    Normal,
    Book,
    Tabletop,
}

@Immutable
data class ElovaireAdaptiveInfo(
    val windowMode: ElovaireWindowMode,
    val postureMode: ElovairePostureMode,
    val useNavigationRail: Boolean,
    val useListDetailPanes: Boolean,
    val useSupportingPane: Boolean,
)

fun elovaireAdaptiveInfo(
    width: Dp,
    postureMode: ElovairePostureMode = ElovairePostureMode.Normal,
): ElovaireAdaptiveInfo {
    val windowMode = when {
        width < 600.dp -> ElovaireWindowMode.CompactPhone
        width < 840.dp -> ElovaireWindowMode.Medium
        else -> ElovaireWindowMode.Expanded
    }
    return ElovaireAdaptiveInfo(
        windowMode = windowMode,
        postureMode = postureMode,
        useNavigationRail = windowMode != ElovaireWindowMode.CompactPhone,
        useListDetailPanes = windowMode == ElovaireWindowMode.Expanded || postureMode == ElovairePostureMode.Book,
        useSupportingPane = windowMode == ElovaireWindowMode.Expanded || postureMode != ElovairePostureMode.Normal,
    )
}
