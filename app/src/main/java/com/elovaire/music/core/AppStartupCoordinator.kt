package elovaire.music.droidbeauty.app.core

import android.content.Context
import android.util.Log
import elovaire.music.droidbeauty.app.data.library.LibraryStartupController
import elovaire.music.droidbeauty.app.data.update.UpdateController
import elovaire.music.droidbeauty.app.core.backend.BackendFailure
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticsRuntime
import elovaire.music.droidbeauty.app.core.backend.classifyBackendFailure
import com.google.common.util.concurrent.SettableFuture
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class DurableStartupPhase {
    NotStarted,
    Recovering,
    Ready,
    Degraded,
    Failed,
    Released,
}

internal enum class DurableStartupComponent {
    MediaMutationRecovery,
    NetworkSourceMutationRecovery,
}

internal data class DurableStartupState(
    val phase: DurableStartupPhase,
    val retryableComponents: Set<DurableStartupComponent> = emptySet(),
    val failure: BackendFailure? = null,
)

internal fun DurableStartupState.allowsMediaButton(): Boolean = when (phase) {
    DurableStartupPhase.Ready,
    DurableStartupPhase.Degraded,
    -> true
    DurableStartupPhase.NotStarted,
    DurableStartupPhase.Recovering,
    DurableStartupPhase.Failed,
    DurableStartupPhase.Released,
    -> false
}

internal fun startupStateAfterRecovery(
    mediaMutationRecoverySucceeded: Boolean,
    networkRecoverySucceeded: Boolean,
    blockedSourceIds: Set<String>,
): DurableStartupState {
    val retryableComponents = buildSet {
        if (!mediaMutationRecoverySucceeded) add(DurableStartupComponent.MediaMutationRecovery)
        if (!networkRecoverySucceeded || blockedSourceIds.isNotEmpty()) {
            add(DurableStartupComponent.NetworkSourceMutationRecovery)
        }
    }
    return if (retryableComponents.isEmpty()) {
        DurableStartupState(DurableStartupPhase.Ready)
    } else {
        DurableStartupState(
            phase = DurableStartupPhase.Degraded,
            retryableComponents = retryableComponents,
        )
    }
}

/** Owns durable recovery and optional startup work after the object graph is built. */
internal class AppStartupCoordinator(
    private val applicationContext: Context,
    private val appScope: CoroutineScope,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
    private val libraryRepository: LibraryStartupController,
    private val durableRecoveryRuntime: DurableRecoveryRuntime,
    private val optionalStartupRuntime: OptionalStartupRuntime,
    private val updateControllerProvider: () -> UpdateController,
    private val backendDiagnostics: BackendDiagnosticsRuntime? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    internal val durableStartupReady = SettableFuture.create<Unit>()
    private val _durableStartupState = MutableStateFlow(
        DurableStartupState(DurableStartupPhase.NotStarted),
    )
    internal val durableStartupState: StateFlow<DurableStartupState> = _durableStartupState.asStateFlow()

    private val exitDiagnosticsDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AppExitDiagnostics(applicationContext, diagnostics = backendDiagnostics)
    }
    private val released = AtomicBoolean(false)
    private var recoveryJob: kotlinx.coroutines.Job? = null
    private val criticalRecoveryScope = CoroutineScope(
        ownedChildScope(appScope, "startup-critical-recovery", backendDiagnostics ?: BackendDiagnostics)
            .coroutineContext + ioDispatcher,
    )
    @Suppress("TooGenericExceptionCaught")
    fun start() {
        if (released.get()) return
        try {
            updateControllerProvider().start()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: RuntimeException) {
            backgroundWorkPolicy.setOptionalStartupSuppressed(true)
            Log.w(TAG, "Optional update service could not start", failure)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun startPlayback() {
        if (released.get() || durableStartupReady.isDone || recoveryJob?.isActive == true) return
        transitionStartupState(DurableStartupState(DurableStartupPhase.Recovering))
        recoveryJob = criticalRecoveryScope.launch {
            try {
                val exitDiagnostics = exitDiagnosticsDelegate.value
                val exitSnapshot = withContext(ioDispatcher) {
                    exitDiagnostics.inspect().also {
                        exitDiagnostics.checkpointRuntime("durable-recovery", force = true)
                    }
                }
                val recovery = durableRecoveryRuntime.recover()
                val mediaMutationRecoverySucceeded = recovery.mediaMutationRecoverySucceeded
                val networkRecoverySucceeded = recovery.networkRecoverySucceeded
                val blockedSourceIds = recovery.blockedSourceIds
                if (blockedSourceIds.isNotEmpty()) {
                    libraryRepository.blockNetworkSources(blockedSourceIds)
                }
                libraryRepository.start()
                libraryRepository.onPermissionChanged(applicationContext.hasAudioReadPermission())
                val startupState = startupStateAfterRecovery(
                    mediaMutationRecoverySucceeded = mediaMutationRecoverySucceeded,
                    networkRecoverySucceeded = networkRecoverySucceeded,
                    blockedSourceIds = blockedSourceIds,
                )
                transitionStartupState(startupState)
                exitDiagnostics.checkpointRuntime(
                    phase = "ready",
                    outcome = startupState.phase.name,
                    force = true,
                )
                optionalStartupRuntime.start(mediaMutationRecoverySucceeded, exitSnapshot)
            } catch (cancelled: CancellationException) {
                transitionStartupState(DurableStartupState(DurableStartupPhase.Released))
                throw cancelled
            } catch (failure: Exception) {
                Log.e(TAG, "Durable playback startup failed", failure)
                val backendFailure = classifyBackendFailure(failure)
                transitionStartupState(DurableStartupState(
                    phase = DurableStartupPhase.Failed,
                    failure = backendFailure,
                ))
                exitDiagnosticsDelegate.value.checkpointRuntime(
                    phase = "failed",
                    outcome = backendFailure.disposition.name,
                    force = true,
                )
            } finally {
                if (recoveryJob === kotlinx.coroutines.currentCoroutineContext()[kotlinx.coroutines.Job]) {
                    recoveryJob = null
                }
            }
        }
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        transitionStartupState(DurableStartupState(DurableStartupPhase.Released))
        optionalStartupRuntime.release()
        if (exitDiagnosticsDelegate.isInitialized()) exitDiagnosticsDelegate.value.release()
        criticalRecoveryScope.cancel()
    }

    private fun transitionStartupState(next: DurableStartupState) {
        if (released.get() && next.phase != DurableStartupPhase.Released) return
        _durableStartupState.value = next
        when (next.phase) {
            DurableStartupPhase.Ready,
            DurableStartupPhase.Degraded,
            -> durableStartupReady.set(Unit)
            DurableStartupPhase.Failed -> durableStartupReady.setException(
                next.failure?.cause ?: IllegalStateException("Durable playback startup failed"),
            )
            DurableStartupPhase.Released -> durableStartupReady.cancel(false)
            DurableStartupPhase.NotStarted,
            DurableStartupPhase.Recovering,
            -> Unit
        }
    }

}

private const val TAG = "ElovaireStartup"
