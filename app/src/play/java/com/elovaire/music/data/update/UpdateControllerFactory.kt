package elovaire.music.droidbeauty.app.data.update

import android.content.Context
import elovaire.music.droidbeauty.app.core.AppBackgroundWorkPolicy
import elovaire.music.droidbeauty.app.data.settings.UpdatePreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal fun createUpdateController(
    context: Context,
    scope: CoroutineScope,
    preferences: UpdatePreferencesStore,
    backgroundWorkPolicy: AppBackgroundWorkPolicy,
): UpdateController = PlayUpdateController

private object PlayUpdateController : UpdateController {
    override val isSupported: Boolean = false
    override val uiState: StateFlow<AppUpdateUiState> = MutableStateFlow(AppUpdateUiState())
    override fun start() = Unit
    override fun checkForUpdates(force: Boolean) = Unit
    override fun dismissAvailableUpdate() = Unit
    override fun startUpdate() = Unit
    override fun clearInstallState() = Unit
    override fun clearTransientStatus() = Unit
    override fun scheduleStartupMaintenance() = Unit
    override fun release() = Unit
}
