package elovaire.music.droidbeauty.app.core

import java.io.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.concurrent.atomic.AtomicBoolean

internal class AppRuntimeScope : Closeable {
    private val supervisorJob = SupervisorJob()
    private val closed = AtomicBoolean(false)

    val scope: CoroutineScope = CoroutineScope(
        supervisorJob + Dispatchers.Main.immediate,
    )

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        scope.cancel()
    }
}
