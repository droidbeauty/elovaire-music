package elovaire.music.droidbeauty.app.core

import kotlinx.coroutines.CancellationException

@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> runSuspendCatching(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Exception) {
        Result.failure(failure)
    }
}
