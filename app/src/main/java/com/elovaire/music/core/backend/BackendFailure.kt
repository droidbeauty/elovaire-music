package elovaire.music.droidbeauty.app.core.backend

import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteTableLockedException
import java.io.FileNotFoundException
import java.io.IOException
import kotlinx.coroutines.CancellationException

/** The small set of actions a backend caller can safely take after a failure. */
internal enum class BackendFailureDisposition {
    RetryableTransient,
    PermissionRequired,
    SourceUnavailable,
    MalformedInput,
    IntegrityFailure,
    Superseded,
    PermanentConflict,
    InvariantViolation,
}

internal data class BackendFailure(
    val disposition: BackendFailureDisposition,
    val cause: Throwable,
)

internal fun classifyBackendFailure(failure: Throwable): BackendFailure {
    if (failure is CancellationException) throw failure
    val disposition = when (failure) {
        is SQLiteDatabaseCorruptException -> BackendFailureDisposition.IntegrityFailure
        is SQLiteDatabaseLockedException,
        is SQLiteTableLockedException,
        -> BackendFailureDisposition.RetryableTransient
        is SQLiteException -> BackendFailureDisposition.IntegrityFailure
        is SecurityException -> BackendFailureDisposition.PermissionRequired
        is FileNotFoundException -> BackendFailureDisposition.SourceUnavailable
        is IllegalArgumentException -> BackendFailureDisposition.MalformedInput
        is IOException -> BackendFailureDisposition.RetryableTransient
        else -> BackendFailureDisposition.InvariantViolation
    }
    return BackendFailure(disposition, failure)
}

internal fun BackendFailure.shouldRetry(attempt: Int, maxAttempts: Int): Boolean {
    return disposition == BackendFailureDisposition.RetryableTransient &&
        attempt in 0 until maxAttempts
}
