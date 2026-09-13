package elovaire.music.droidbeauty.app.core

import java.io.Closeable
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CancellationException
import java.util.concurrent.atomic.AtomicBoolean
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticRecorder

internal class AppRuntimeScope(
    private val diagnostics: BackendDiagnosticRecorder = BackendDiagnostics,
) : Closeable {
    private val supervisorJob = SupervisorJob()
    private val closed = AtomicBoolean(false)

    val scope: CoroutineScope = CoroutineScope(
        supervisorJob + Dispatchers.Main.immediate + CoroutineName("app-runtime") +
            diagnosticFailureHandler("app-runtime", diagnostics),
    )

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        scope.cancel()
    }
}

internal fun ownedChildScope(
    parentScope: CoroutineScope,
    owner: String,
    diagnostics: BackendDiagnosticRecorder = BackendDiagnostics,
): CoroutineScope {
    return CoroutineScope(
        parentScope.coroutineContext +
            SupervisorJob(parentScope.coroutineContext[Job]) +
            CoroutineName(owner) +
            diagnosticFailureHandler(owner, diagnostics),
    )
}

private fun diagnosticFailureHandler(
    owner: String,
    diagnostics: BackendDiagnosticRecorder,
): CoroutineExceptionHandler = CoroutineExceptionHandler { _, failure ->
    if (failure !is CancellationException) diagnostics.recordWorkerFailure(owner, failure)
}
