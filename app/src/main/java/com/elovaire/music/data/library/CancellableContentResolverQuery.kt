package elovaire.music.droidbeauty.app.data.library

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.OperationCanceledException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job

/**
 * Connects coroutine cancellation to providers which may block while opening a cursor.
 * Providers are allowed to ignore the signal, so the returned cursor is also closed when
 * cancellation is observed after the query returns.
 */
internal suspend fun ContentResolver.queryCancellable(
    uri: Uri,
    projection: Array<String>?,
    selection: String?,
    selectionArgs: Array<String>?,
    sortOrder: String?,
): Cursor? {
    currentCoroutineContext().ensureActive()
    val signal = CancellationSignal()
    val cancellationHandle = currentCoroutineContext().job.invokeOnCompletion {
        signal.cancel()
    }
    return try {
        val cursor = try {
            query(uri, projection, selection, selectionArgs, sortOrder, signal)
        } catch (failure: OperationCanceledException) {
            currentCoroutineContext().ensureActive()
            throw CancellationException("Content provider query was cancelled").also {
                it.initCause(failure)
            }
        }
        try {
            currentCoroutineContext().ensureActive()
        } catch (cancellation: CancellationException) {
            cursor?.close()
            throw cancellation
        }
        cursor
    } finally {
        cancellationHandle.dispose()
    }
}
