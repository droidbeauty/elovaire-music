package elovaire.music.droidbeauty.app.data.library.db

import org.junit.Assert.assertEquals
import org.junit.Test
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationRecoveryResult
import kotlinx.coroutines.runBlocking

class PersistenceMaintenanceTest {
    @Test
    fun criticalRecoveryContinuesAcrossAllSuccessfulBatches() = runBlocking {
        val batches = ArrayDeque(
            listOf(
                MediaMutationRecoveryResult.Success(128, 385),
                MediaMutationRecoveryResult.Success(128, 257),
                MediaMutationRecoveryResult.Success(128, 129),
                MediaMutationRecoveryResult.Success(128, 1),
                MediaMutationRecoveryResult.Success(1, 0),
            ),
        )
        var calls = 0

        val complete = recoverMutationBatches { calls += 1; batches.removeFirst() }

        assertEquals(true, complete)
        assertEquals(5, calls)
    }

    @Test
    fun criticalRecoveryStopsWhenABatchMakesNoProgress() = runBlocking {
        var calls = 0

        val complete = recoverMutationBatches {
            calls += 1
            MediaMutationRecoveryResult.Success(0, 1)
        }

        assertEquals(false, complete)
        assertEquals(1, calls)
    }

    @Test
    fun retentionCutoffNeverPredatesEpoch() {
        assertEquals(0L, terminalMutationCutoff(1L))
    }

    @Test
    fun retentionCutoffKeepsThirtyDays() {
        val thirtyOneDays = 31L * 24L * 60L * 60L * 1_000L
        val oneDay = 24L * 60L * 60L * 1_000L
        assertEquals(oneDay, terminalMutationCutoff(thirtyOneDays))
    }

    @Test
    fun healthCheckCadenceRunsOnFirstUseClockRollbackAndAfterSixHours() {
        assertEquals(true, persistenceHealthCheckDue(1_000L, 0L))
        assertEquals(true, persistenceHealthCheckDue(1_000L, 2_000L))
        assertEquals(false, persistenceHealthCheckDue(1_000L, 1_000L))
        assertEquals(true, persistenceHealthCheckDue(6L * 60L * 60L * 1_000L, 0L))
        assertEquals(
            true,
            persistenceHealthCheckDue(6L * 60L * 60L * 1_000L + 1L, 1L),
        )
    }
}
