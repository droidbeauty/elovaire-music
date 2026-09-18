package elovaire.music.droidbeauty.app.core

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

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
    private val lock = ReentrantLock()
    private val actionFinished: Condition = lock.newCondition()
    private var actionRunning = false
    private var actionOwner: Thread? = null
    private var releaseActionExecuted = false
    private var releasePending = false

    fun startPlayback() {
        runTransition(
            canStart = { it == AppRuntimePhase.Created },
            target = AppRuntimePhase.PlaybackStarted,
            action = startPlaybackAction,
        )
    }

    fun start() {
        runTransition(
            canStart = { it == AppRuntimePhase.Created || it == AppRuntimePhase.PlaybackStarted },
            target = AppRuntimePhase.Started,
            action = startAction,
        )
    }

    fun onMemoryPressure(pressure: MemoryPressure) {
        runExclusiveAction(action = { memoryPressureAction(pressure) })
    }

    fun scheduleDeferredStartupWork(action: () -> Unit) {
        runExclusiveAction(action = action, onlyWhen = AppRuntimePhase.Started)
    }

    @Suppress("TooGenericExceptionCaught")
    fun release() {
        val ownsTransition = beginRelease() ?: return
        val failure = runReleaseActionIfNeeded()
        if (ownsTransition) finishAction()
        failure?.let { throw it }
    }

    internal fun currentPhase(): AppRuntimePhase {
        lock.lock()
        return try {
            phase
        } finally {
            lock.unlock()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun runTransition(
        canStart: (AppRuntimePhase) -> Boolean,
        target: AppRuntimePhase,
        action: () -> Unit,
    ) {
        if (!beginTransition(canStart, target)) return
        try {
            action()
        } catch (failure: Throwable) {
            lock.lock()
            try {
                phase = AppRuntimePhase.Released
            } finally {
                lock.unlock()
            }
            val cleanupFailure = runReleaseActionIfNeeded()
            finishAction()
            if (cleanupFailure != null && cleanupFailure !== failure) {
                failure.addSuppressed(cleanupFailure)
            }
            throw failure
        }
        val cleanupFailure = finishAction()
        cleanupFailure?.let { throw it }
    }

    private fun beginTransition(
        canStart: (AppRuntimePhase) -> Boolean,
        target: AppRuntimePhase,
    ): Boolean {
        lock.lock()
        try {
            if (actionRunning && actionOwner === Thread.currentThread()) return false
            awaitActionLocked()
            if (!canStart(phase)) return false
            phase = target
            actionRunning = true
            actionOwner = Thread.currentThread()
            return true
        } finally {
            lock.unlock()
        }
    }

    private fun beginRelease(): Boolean? {
        lock.lock()
        try {
            if (actionRunning && actionOwner === Thread.currentThread()) {
                if (releaseActionExecuted) return null
                phase = AppRuntimePhase.Released
                releasePending = true
                return null
            }
            awaitActionLocked()
            if (phase == AppRuntimePhase.Released) return null
            phase = AppRuntimePhase.Released
            actionRunning = true
            actionOwner = Thread.currentThread()
            return true
        } finally {
            lock.unlock()
        }
    }

    private fun runExclusiveAction(
        action: () -> Unit,
        onlyWhen: AppRuntimePhase? = null,
    ) {
        val ownsTransition = beginExclusiveAction(onlyWhen) ?: return
        try {
            action()
        } finally {
            if (ownsTransition) finishAction()
        }
    }

    private fun beginExclusiveAction(onlyWhen: AppRuntimePhase?): Boolean? {
        lock.lock()
        try {
            if (actionRunning && actionOwner === Thread.currentThread()) {
                return if (onlyWhen == null || phase == onlyWhen) false else null
            }
            awaitActionLocked()
            if (phase == AppRuntimePhase.Released || onlyWhen != null && phase != onlyWhen) return null
            actionRunning = true
            actionOwner = Thread.currentThread()
            return true
        } finally {
            lock.unlock()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun runReleaseActionIfNeeded(): Throwable? {
        lock.lock()
        val shouldRun = try {
            if (releaseActionExecuted) {
                false
            } else {
                releaseActionExecuted = true
                true
            }
        } finally {
            lock.unlock()
        }
        if (!shouldRun) return null
        return try {
            releaseAction()
            null
        } catch (failure: Throwable) {
            failure
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun finishAction(): Throwable? {
        lock.lock()
        val runPendingRelease = try {
            val shouldRun = releasePending && !releaseActionExecuted
            releasePending = false
            if (shouldRun) releaseActionExecuted = true
            shouldRun
        } finally {
            lock.unlock()
        }
        if (runPendingRelease) {
            return try {
                releaseAction()
                finishAction()
                null
            } catch (failure: Throwable) {
                finishAction()
                failure
            }
        }
        lock.lock()
        try {
            actionRunning = false
            actionOwner = null
            actionFinished.signalAll()
        } finally {
            lock.unlock()
        }
        return null
    }

    private fun awaitActionLocked() {
        while (actionRunning) {
            try {
                actionFinished.await()
            } catch (interrupted: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IllegalStateException("Interrupted while waiting for app runtime transition.", interrupted)
            }
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
