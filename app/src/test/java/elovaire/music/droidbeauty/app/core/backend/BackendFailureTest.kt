package elovaire.music.droidbeauty.app.core.backend

import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteException
import java.io.FileNotFoundException
import java.io.IOException
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
            BackendFailureDisposition.IntegrityFailure,
            classifyBackendFailure(SQLiteException("broken")).disposition,
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
    fun cancellationIsNeverConvertedToARecoverableFailure() {
        val cancellation = CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            classifyBackendFailure(cancellation)
        }
    }
}
