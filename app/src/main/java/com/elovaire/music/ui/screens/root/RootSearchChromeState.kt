package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

internal class RootSearchChromeState(
    isQueryActive: Boolean,
    isFieldFocused: Boolean,
    private val setQueryActive: (Boolean) -> Unit,
    private val setFieldFocused: (Boolean) -> Unit,
) {
    val isQueryActive: Boolean = isQueryActive
    val isFieldFocused: Boolean = isFieldFocused

    fun onQueryActiveChanged(active: Boolean) = setQueryActive(active)
    fun onFieldFocusedChanged(focused: Boolean) = setFieldFocused(focused)
}

@Composable
internal fun rememberRootSearchChromeState(): RootSearchChromeState {
    var isQueryActive by rememberSaveable { mutableStateOf(false) }
    var isFieldFocused by rememberSaveable { mutableStateOf(false) }
    return RootSearchChromeState(
        isQueryActive = isQueryActive,
        isFieldFocused = isFieldFocused,
        setQueryActive = { isQueryActive = it },
        setFieldFocused = { isFieldFocused = it },
    )
}
