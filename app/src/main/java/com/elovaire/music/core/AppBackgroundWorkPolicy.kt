package elovaire.music.droidbeauty.app.core

import kotlinx.coroutines.flow.StateFlow

internal enum class AppWorkKind {
    ForegroundOnlyUiWork,
    ForegroundOnlyMaintenance,
    UserInitiatedShortWork,
    UserInitiatedLongTransfer,
    MediaPlaybackRuntime,
    PersistentScheduledWork,
}

internal class AppBackgroundWorkPolicy(
    val isForeground: StateFlow<Boolean>,
) {
    fun canStart(
        kind: AppWorkKind,
        userInitiated: Boolean = false,
    ): Boolean {
        return when (kind) {
            AppWorkKind.ForegroundOnlyUiWork,
            AppWorkKind.ForegroundOnlyMaintenance,
            -> isForeground.value

            AppWorkKind.UserInitiatedShortWork,
            AppWorkKind.UserInitiatedLongTransfer,
            -> userInitiated && isForeground.value

            AppWorkKind.MediaPlaybackRuntime -> true
            AppWorkKind.PersistentScheduledWork -> false
        }
    }

    fun shouldKeepMediaStoreObserver(permissionGranted: Boolean): Boolean = permissionGranted

    fun shouldKeepRecursiveLibraryObservers(permissionGranted: Boolean): Boolean {
        return permissionGranted && isForeground.value
    }

    fun shouldDeferLibraryRefresh(): Boolean = !isForeground.value

    fun shouldStartAutomaticUpdateCheck(): Boolean {
        return canStart(AppWorkKind.ForegroundOnlyMaintenance)
    }

    fun shouldStartLyricsPrefetch(): Boolean {
        return canStart(AppWorkKind.ForegroundOnlyUiWork)
    }
}
