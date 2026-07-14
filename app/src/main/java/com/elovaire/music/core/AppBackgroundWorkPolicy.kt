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

internal data class WorkEnvironment(
    val foreground: Boolean,
    val networkAvailable: Boolean = true,
    val metered: Boolean = false,
    val batterySaver: Boolean = false,
    val criticalUserOperationActive: Boolean = false,
)

internal enum class WorkDecision {
    Admit,
    Defer,
    Reject,
}

internal fun decideWorkAdmission(
    kind: AppWorkKind,
    userInitiated: Boolean,
    environment: WorkEnvironment,
): WorkDecision {
    if (kind == AppWorkKind.PersistentScheduledWork) return WorkDecision.Reject
    if (kind == AppWorkKind.MediaPlaybackRuntime) return WorkDecision.Admit
    if (!environment.foreground) return WorkDecision.Defer
    if (userInitiated) return WorkDecision.Admit
    if (environment.criticalUserOperationActive) return WorkDecision.Defer
    if (environment.batterySaver && kind == AppWorkKind.ForegroundOnlyMaintenance) return WorkDecision.Defer
    return when (kind) {
        AppWorkKind.ForegroundOnlyUiWork,
        AppWorkKind.ForegroundOnlyMaintenance,
        -> WorkDecision.Admit
        AppWorkKind.UserInitiatedShortWork,
        AppWorkKind.UserInitiatedLongTransfer,
        -> WorkDecision.Reject
        AppWorkKind.MediaPlaybackRuntime,
        AppWorkKind.PersistentScheduledWork,
        -> error("Handled above")
    }
}

internal class AppBackgroundWorkPolicy(
    val isForeground: StateFlow<Boolean>,
) {
    fun canStart(
        kind: AppWorkKind,
        userInitiated: Boolean = false,
    ): Boolean {
        return decideWorkAdmission(
            kind = kind,
            userInitiated = userInitiated,
            environment = WorkEnvironment(foreground = isForeground.value),
        ) == WorkDecision.Admit
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
