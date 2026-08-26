package elovaire.music.droidbeauty.app.core

import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    val interactionCritical: Boolean = false,
)

internal enum class WorkDecision {
    Admit,
    Defer,
    Reject,
}

private val OPTIONAL_AUTOMATIC_WORK = setOf(
    AppWorkKind.ForegroundOnlyUiWork,
    AppWorkKind.ForegroundOnlyMaintenance,
)

internal fun decideWorkAdmission(
    kind: AppWorkKind,
    userInitiated: Boolean,
    environment: WorkEnvironment,
): WorkDecision {
    if (kind == AppWorkKind.PersistentScheduledWork) return WorkDecision.Reject
    if (kind == AppWorkKind.MediaPlaybackRuntime) return WorkDecision.Admit
    if (
        environment.interactionCritical &&
        !userInitiated &&
        kind in OPTIONAL_AUTOMATIC_WORK
    ) {
        return WorkDecision.Defer
    }
    if (!environment.foreground) return WorkDecision.Defer
    if (userInitiated) return WorkDecision.Admit
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
    private val optionalStartupSuppressed = AtomicBoolean(false)
    private val interactionOwners = AtomicInteger(0)
    private val interactionLock = Any()
    private val _interactionCritical = MutableStateFlow(false)
    val interactionCritical: StateFlow<Boolean> = _interactionCritical.asStateFlow()

    fun canStart(
        kind: AppWorkKind,
        userInitiated: Boolean = false,
    ): Boolean {
        if (
            optionalStartupSuppressed.get() &&
            !userInitiated &&
            kind in OPTIONAL_AUTOMATIC_WORK
        ) {
            return false
        }
        return decideWorkAdmission(
            kind = kind,
            userInitiated = userInitiated,
            environment = WorkEnvironment(
                foreground = isForeground.value,
                interactionCritical = interactionCritical.value,
            ),
        ) == WorkDecision.Admit
    }

    fun shouldKeepMediaStoreObserver(permissionGranted: Boolean): Boolean {
        return permissionGranted && isForeground.value
    }

    fun shouldKeepRecursiveLibraryObservers(permissionGranted: Boolean): Boolean {
        return permissionGranted && isForeground.value
    }

    fun shouldDeferLibraryRefresh(): Boolean =
        !isForeground.value || interactionCritical.value

    fun acquireInteractionCritical(): Closeable {
        synchronized(interactionLock) {
            interactionOwners.incrementAndGet()
            _interactionCritical.value = true
        }
        val released = AtomicBoolean(false)
        return Closeable {
            if (!released.compareAndSet(false, true)) return@Closeable
            synchronized(interactionLock) {
                if (interactionOwners.get() <= 0) return@Closeable
                _interactionCritical.value = interactionOwners.decrementAndGet() > 0
            }
        }
    }

    fun shouldStartLyricsPrefetch(): Boolean {
        return canStart(AppWorkKind.ForegroundOnlyUiWork)
    }

    fun setOptionalStartupSuppressed(suppressed: Boolean) {
        optionalStartupSuppressed.set(suppressed)
    }

}
