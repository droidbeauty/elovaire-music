package elovaire.music.droidbeauty.app.core

internal enum class AppRuntimePhase {
    Created,
    PlaybackStarted,
    Started,
    Released,
}

/** Owns the app-level lifecycle transitions around the composition graph. */
internal class AppRuntimeCoordinator(
    private val startPlaybackAction: () -> Unit,
    private val startAction: () -> Unit,
    private val memoryPressureAction: (MemoryPressure) -> Unit,
    private val releaseAction: () -> Unit,
) {
    private var phase = AppRuntimePhase.Created
    private val transitionLock = Any()

    fun startPlayback() {
        synchronized(transitionLock) {
            transitionTo(
                target = AppRuntimePhase.PlaybackStarted,
                allowedFrom = setOf(AppRuntimePhase.Created),
                action = startPlaybackAction,
            )
        }
    }

    fun start() {
        synchronized(transitionLock) {
            transitionTo(
                target = AppRuntimePhase.Started,
                allowedFrom = setOf(AppRuntimePhase.Created, AppRuntimePhase.PlaybackStarted),
                action = startAction,
            )
        }
    }

    fun onMemoryPressure(pressure: MemoryPressure) {
        synchronized(transitionLock) {
            if (phase != AppRuntimePhase.Released) {
                memoryPressureAction(pressure)
            }
        }
    }

    fun scheduleDeferredStartupWork(action: () -> Unit) {
        synchronized(transitionLock) {
            if (phase == AppRuntimePhase.Started) action()
        }
    }

    fun release() {
        synchronized(transitionLock) {
            if (phase != AppRuntimePhase.Released) {
                phase = AppRuntimePhase.Released
                releaseAction()
            }
        }
    }

    internal fun currentPhase(): AppRuntimePhase = synchronized(transitionLock) { phase }

    @Suppress("TooGenericExceptionCaught")
    private fun transitionTo(
        target: AppRuntimePhase,
        allowedFrom: Set<AppRuntimePhase>,
        action: () -> Unit,
    ) {
        val current = phase
        if (current == AppRuntimePhase.Released || current == target || current == AppRuntimePhase.Started) {
            return
        }
        if (current !in allowedFrom) return
        phase = target
        try {
            action()
        } catch (failure: Throwable) {
            try {
                release()
            } catch (cleanupFailure: Throwable) {
                if (cleanupFailure !== failure) failure.addSuppressed(cleanupFailure)
            }
            throw failure
        }
    }
}

/** Attempts every owned release action while preserving the first failure for the caller. */
@Suppress("TooGenericExceptionCaught")
internal fun releaseBestEffort(vararg actions: () -> Unit) {
    var firstFailure: Throwable? = null
    actions.forEach { action ->
        try {
            action()
        } catch (failure: Throwable) {
            val recordedFailure = firstFailure
            if (recordedFailure == null) {
                firstFailure = failure
            } else if (failure !== recordedFailure) {
                recordedFailure.addSuppressed(failure)
            }
        }
    }
    firstFailure?.let { throw it }
}
