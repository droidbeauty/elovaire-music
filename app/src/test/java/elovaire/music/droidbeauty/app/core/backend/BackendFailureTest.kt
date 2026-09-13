package elovaire.music.droidbeauty.app.core.backend

import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.os.RemoteException
import java.io.FileNotFoundException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackendFailureTest {
    @Test
    fun classifiesLockedDatabaseAsRetryable() {
        val failure = classifyBackendFailure(SQLiteDatabaseLockedException("locked"))

        assertEquals(BackendFailureDisposition.RetryableTransient, failure.disposition)
        assertEquals(true, failure.shouldRetry(attempt = 0, maxAttempts = 3))
        assertEquals(false, failure.shouldRetry(attempt = 3, maxAttempts = 3))
    }

    @Test
    fun classifiesBackendBoundariesByRecoveryAction() {
        assertEquals(
            BackendFailureDisposition.InvariantViolation,
            classifyBackendFailure(SQLiteException("broken")).disposition,
        )
        assertEquals(
            BackendFailureDisposition.IntegrityFailure,
            classifyBackendFailure(SQLiteException("broken"), BackendOperationKind.Database).disposition,
        )
        assertEquals(
            BackendFailureDisposition.PermanentConflict,
            classifyBackendFailure(SQLiteConstraintException("conflict")).disposition,
        )
        assertEquals(
            BackendFailureDisposition.PermissionRequired,
            classifyBackendFailure(SecurityException("denied")).disposition,
        )
        assertEquals(
            BackendFailureDisposition.SourceUnavailable,
            classifyBackendFailure(FileNotFoundException("missing")).disposition,
        )
        assertEquals(
            BackendFailureDisposition.RetryableTransient,
            classifyBackendFailure(IOException("temporary")).disposition,
        )
    }

    @Test
    fun followsBoundedCauseChainWithoutRetryingUnknownFailures() {
        assertEquals(
            BackendFailureDisposition.RetryableTransient,
            classifyBackendFailure(IllegalStateException("wrapper", SocketTimeoutException())).disposition,
        )
        assertEquals(
            BackendFailureDisposition.RetryableTransient,
            classifyBackendFailure(IllegalStateException("wrapper", RemoteException("binder"))).disposition,
        )
        val deep = generateSequence<Throwable>(IllegalStateException("root")) { IllegalStateException("next", it) }
            .take(20)
            .last()
        assertEquals(BackendFailureDisposition.InvariantViolation, classifyBackendFailure(deep).disposition)
    }

    @Test
    fun cancellationIsNeverConvertedToARecoverableFailure() {
        val cancellation = CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            classifyBackendFailure(cancellation)
        }
    }
}
