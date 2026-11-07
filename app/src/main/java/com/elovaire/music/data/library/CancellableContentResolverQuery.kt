package elovaire.music.droidbeauty.app.data.library

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.OperationCanceledException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

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
    return cancellableCursorQuery { signal ->
        query(uri, projection, selection, selectionArgs, sortOrder, signal)
    }
}

internal suspend fun cancellableCursorQuery(query: (CancellationSignal) -> Cursor?): Cursor? =
    suspendCancellableCoroutine { continuation ->
        val signal = CancellationSignal()
        continuation.invokeOnCancellation { signal.cancel() }
        if (!continuation.isActive) return@suspendCancellableCoroutine
        try {
            val cursor = query(signal)
            continuation.resume(cursor) { _, rejectedCursor, _ -> rejectedCursor?.close() }
        } catch (failure: OperationCanceledException) {
            continuation.resumeWithException(CancellationException("Content provider query was cancelled").also {
                it.initCause(failure)
            })
        } catch (failure: Exception) {
            continuation.resumeWithException(failure)
        }
    }
