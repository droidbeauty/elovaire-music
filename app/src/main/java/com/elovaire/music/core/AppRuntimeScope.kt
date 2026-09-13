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

internal class AppRuntimeScope : Closeable {
    private val supervisorJob = SupervisorJob()
    private val closed = AtomicBoolean(false)

    val scope: CoroutineScope = CoroutineScope(
        supervisorJob + Dispatchers.Main.immediate + CoroutineName("app-runtime") +
            diagnosticFailureHandler("app-runtime"),
    )

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        scope.cancel()
    }
}

internal fun ownedChildScope(
    parentScope: CoroutineScope,
    owner: String,
): CoroutineScope {
    return CoroutineScope(
        parentScope.coroutineContext +
            SupervisorJob(parentScope.coroutineContext[Job]) +
            CoroutineName(owner) +
            diagnosticFailureHandler(owner),
    )
}

private fun diagnosticFailureHandler(owner: String): CoroutineExceptionHandler = CoroutineExceptionHandler { _, failure ->
    if (failure !is CancellationException) BackendDiagnostics.recordWorkerFailure(owner, failure)
}
