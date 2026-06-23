package elovaire.music.droidbeauty.app.core

import java.io.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

internal class AppRuntimeScope : Closeable {
    private val supervisorJob = SupervisorJob()

    val scope: CoroutineScope = CoroutineScope(
        supervisorJob + Dispatchers.Main.immediate,
    )

    override fun close() {
        scope.cancel()
    }
}
