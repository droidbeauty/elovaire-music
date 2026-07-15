package elovaire.music.droidbeauty.app.core

import kotlinx.coroutines.CancellationException

internal suspend fun <T> runSuspendCatching(block: suspend () -> T): Result<T> {
    val result = runCatching { block() }
    val failure = result.exceptionOrNull()
    if (failure is CancellationException) throw failure
    return result
}
