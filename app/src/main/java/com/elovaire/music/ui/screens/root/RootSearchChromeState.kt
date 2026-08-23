package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

internal class RootSearchChromeState(
    isActive: Boolean,
    private val setActive: (Boolean) -> Unit,
) {
    val isActive: Boolean = isActive

    fun onActiveChanged(active: Boolean) = setActive(active)
}

@Composable
internal fun rememberRootSearchChromeState(): RootSearchChromeState {
    var isActive by remember { mutableStateOf(false) }
    return RootSearchChromeState(
        isActive = isActive,
        setActive = { isActive = it },
    )
}
