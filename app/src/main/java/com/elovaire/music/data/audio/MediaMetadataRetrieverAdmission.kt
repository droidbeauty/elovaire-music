package elovaire.music.droidbeauty.app.data.audio

import kotlinx.coroutines.sync.Semaphore

/** Bounds native retriever ownership for one app-scoped scan/runtime boundary. */
internal class MediaMetadataRetrieverAdmission(
    maxConcurrent: Int = 3,
) {
    private val permits = Semaphore(maxConcurrent.coerceAtLeast(1))

    suspend fun <T> withPermit(block: suspend () -> T): T {
        permits.acquire()
        return try {
            block()
        } finally {
            permits.release()
        }
    }

    fun <T> tryWithPermit(block: () -> T): T? {
        if (!permits.tryAcquire()) return null
        return try {
            block()
        } finally {
            permits.release()
        }
    }
}
