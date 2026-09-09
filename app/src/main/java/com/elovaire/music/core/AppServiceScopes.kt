package elovaire.music.droidbeauty.app.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

internal class AppServiceScopes(parentScope: CoroutineScope) {
    val playback: CoroutineScope = childScope(parentScope)
    val library: CoroutineScope = childScope(parentScope)
    val optional: CoroutineScope = childScope(parentScope)

    fun cancelPlayback() = playback.cancel()

    fun cancelLibrary() = library.cancel()

    fun cancelOptional() = optional.cancel()

    private fun childScope(parentScope: CoroutineScope): CoroutineScope {
        return CoroutineScope(
            parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job]),
        )
    }
}
