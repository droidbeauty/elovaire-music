package elovaire.music.droidbeauty.app.core.backend

import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteFullException
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteTableLockedException
import android.os.DeadObjectException
import android.os.RemoteException
import java.io.FileNotFoundException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.coroutines.CancellationException

internal enum class BackendOperationKind {
    Generic,
    Database,
    Provider,
    Network,
    Input,
}

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
    val operationKind: BackendOperationKind = BackendOperationKind.Generic,
)

internal fun classifyBackendFailure(
    failure: Throwable,
    operationKind: BackendOperationKind = BackendOperationKind.Generic,
): BackendFailure {
    val chain = boundedCauseChain(failure)
    chain.firstOrNull { it is CancellationException }?.let { throw it }
    val disposition = chain.mapNotNull { candidate ->
        candidate.backendDisposition(operationKind)?.let { candidate to it }
    }.maxByOrNull { (candidate, _) -> candidate.classificationSpecificity() }
        ?.second
        ?: BackendFailureDisposition.InvariantViolation
    return BackendFailure(disposition, failure, operationKind)
}

private fun Throwable.classificationSpecificity(): Int = when (this) {
    is SQLiteDatabaseCorruptException,
    is SQLiteDatabaseLockedException,
    is SQLiteTableLockedException,
    is SQLiteFullException,
    is SQLiteConstraintException,
    is SecurityException,
    is FileNotFoundException,
    is DeadObjectException,
    is RemoteException,
    is SocketTimeoutException,
    is ConnectException,
    is UnknownHostException,
    is SSLException,
    -> 3
    is SQLiteException -> 1
    is IOException,
    is IllegalArgumentException,
    -> 2
    else -> 0
}

private fun Throwable.backendDisposition(
    operationKind: BackendOperationKind,
): BackendFailureDisposition? = when (this) {
    is SQLiteDatabaseCorruptException -> BackendFailureDisposition.IntegrityFailure
    is SQLiteDatabaseLockedException,
    is SQLiteTableLockedException,
    is SQLiteFullException,
    -> BackendFailureDisposition.RetryableTransient
    is SQLiteConstraintException -> BackendFailureDisposition.PermanentConflict
    is SQLiteException -> when (operationKind) {
        BackendOperationKind.Database -> BackendFailureDisposition.IntegrityFailure
        else -> BackendFailureDisposition.InvariantViolation
    }
    is SecurityException -> BackendFailureDisposition.PermissionRequired
    is FileNotFoundException -> BackendFailureDisposition.SourceUnavailable
    is DeadObjectException,
    is RemoteException,
    -> BackendFailureDisposition.RetryableTransient
    is SocketTimeoutException,
    is ConnectException,
    is UnknownHostException,
    is SSLException,
    -> BackendFailureDisposition.RetryableTransient
    is IllegalArgumentException -> BackendFailureDisposition.MalformedInput
    is IOException -> BackendFailureDisposition.RetryableTransient
    else -> null
}

private fun boundedCauseChain(failure: Throwable): Sequence<Throwable> = sequence {
    var current: Throwable? = failure
    var depth = 0
    val visited = HashSet<Throwable>()
    while (current != null && depth++ < MAX_CAUSE_DEPTH && visited.add(current)) {
        yield(current)
        current = current.cause
    }
}

private const val MAX_CAUSE_DEPTH = 8

internal fun BackendFailure.shouldRetry(attempt: Int, maxAttempts: Int): Boolean {
    return disposition == BackendFailureDisposition.RetryableTransient &&
        attempt in 0 until maxAttempts
}
