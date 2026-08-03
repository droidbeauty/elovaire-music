package elovaire.music.droidbeauty.app.data.update

import kotlinx.coroutines.flow.StateFlow

internal data class AppReleaseInfo(
    val versionName: String,
    val tagName: String,
    val downloadUrl: String,
    val checksumUrl: String?,
    val checksumSha256: String?,
    val assetSizeBytes: Long?,
    val notes: String,
    val publishedAt: String,
    val assetFileName: String,
    val releasePageUrl: String,
)

internal data class AppUpdateUiState(
    val availableRelease: AppReleaseInfo? = null,
    val isChecking: Boolean = false,
    val isDownloading: Boolean = false,
    val isInstalling: Boolean = false,
    val installPermissionRequired: Boolean = false,
    val downloadProgress: Float? = null,
    val errorMessage: String? = null,
    val transientStatus: AppUpdateTransientStatus? = null,
)

internal enum class AppUpdateTransientStatus {
    UpToDate,
}

internal interface UpdateController {
    val isSupported: Boolean
    val uiState: StateFlow<AppUpdateUiState>

    fun start()
    fun checkForUpdates(force: Boolean = false)
    fun dismissAvailableUpdate()
    fun startUpdate()
    fun clearInstallState()
    fun clearTransientStatus()
    fun scheduleStartupMaintenance()
    fun release()
}
