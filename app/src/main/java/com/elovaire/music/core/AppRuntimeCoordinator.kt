package elovaire.music.droidbeauty.app.core

import java.util.concurrent.atomic.AtomicReference

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
    private val phase = AtomicReference(AppRuntimePhase.Created)
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
            if (phase.get() != AppRuntimePhase.Released) {
                memoryPressureAction(pressure)
            }
        }
    }

    fun release() {
        synchronized(transitionLock) {
            if (phase.getAndSet(AppRuntimePhase.Released) != AppRuntimePhase.Released) {
                releaseAction()
            }
        }
    }

    internal fun currentPhase(): AppRuntimePhase = phase.get()

    @Suppress("TooGenericExceptionCaught")
    private fun transitionTo(
        target: AppRuntimePhase,
        allowedFrom: Set<AppRuntimePhase>,
        action: () -> Unit,
    ) {
        val current = phase.get()
        if (current == AppRuntimePhase.Released || current == target || current == AppRuntimePhase.Started) {
            return
        }
        if (current !in allowedFrom) return
        phase.set(target)
        try {
            action()
        } catch (failure: Throwable) {
            release()
            throw failure
        }
    }
}
