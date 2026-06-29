package elovaire.music.droidbeauty.app.data.update

import kotlinx.coroutines.flow.StateFlow

interface UpdateReader {
    val uiState: StateFlow<AppUpdateUiState>
}
