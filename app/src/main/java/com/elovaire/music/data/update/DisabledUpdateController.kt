package elovaire.music.droidbeauty.app.data.update

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class DisabledUpdateController : UpdateController {
    private val disabledState = MutableStateFlow(AppUpdateUiState())
    override val uiState: StateFlow<AppUpdateUiState> = disabledState.asStateFlow()

    override fun checkForUpdates(force: Boolean) = Unit
    override fun dismissAvailableUpdate() = Unit
    override fun startUpdate() = Unit
    override fun clearInstallState() = Unit
    override fun clearTransientStatus() = Unit
    override fun scheduleStartupMaintenance() = Unit
    override fun release() = Unit
}
