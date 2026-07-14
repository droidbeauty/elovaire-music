package elovaire.music.droidbeauty.app.data.update

import kotlinx.coroutines.flow.StateFlow

internal interface UpdateReader {
    val uiState: StateFlow<AppUpdateUiState>
}

internal interface UpdateController : UpdateReader {
    fun start()
    fun checkForUpdates(force: Boolean = false)
    fun dismissAvailableUpdate()
    fun startUpdate()
    fun clearInstallState()
    fun clearTransientStatus()
    fun scheduleStartupMaintenance()
    fun release()
}
