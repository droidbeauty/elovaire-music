package elovaire.music.droidbeauty.app.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Execution dependencies for work that is not tied to the main thread. */
internal data class AppDispatchers(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
) {
    companion object {
        fun production(): AppDispatchers = AppDispatchers(
            io = Dispatchers.IO,
            default = Dispatchers.Default,
        )
    }
}
