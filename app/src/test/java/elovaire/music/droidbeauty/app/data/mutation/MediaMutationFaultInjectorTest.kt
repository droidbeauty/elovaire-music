package elovaire.music.droidbeauty.app.data.mutation

import java.io.IOException
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaMutationFaultInjectorTest {
    @Test
    fun everyDurabilityBoundaryCanBeInjectedDeterministically() {
        val observed = mutableListOf<MediaMutationTransactionPhase>()
        val injector = MediaMutationFaultInjector { observed += it }

        MediaMutationTransactionPhase.entries.forEach(injector::checkpoint)

        assertEquals(MediaMutationTransactionPhase.entries.toList(), observed)
    }

    @Test
    fun aSelectedBoundaryFailsEveryTimeItIsReached() {
        MediaMutationTransactionPhase.entries.forEach { selectedPhase ->
            val injector = MediaMutationFaultInjector { phase ->
                if (phase == selectedPhase) throw IllegalStateException("injected:$phase")
            }

            assertThrows(IllegalStateException::class.java) {
                injector.checkpoint(selectedPhase)
            }
        }
    }

    @Test
    fun rollbackIoFailureIsReportedAsUnrecovered() {
        assertTrue(
            runMediaMutationRollback(onAbort = {}, rollback = { throw IOException("rollback failed") }),
        )
    }

    @Test
    fun rollbackCancellationPreservesBackupAndPropagates() {
        var preserveBackup = false

        assertThrows(CancellationException::class.java) {
            runMediaMutationRollback(onAbort = { preserveBackup = true }) {
                throw CancellationException("cancelled")
            }
        }

        assertTrue(preserveBackup)
    }

    @Test
    fun fatalRollbackFailurePreservesBackupAndPropagates() {
        var preserveBackup = false

        assertThrows(AssertionError::class.java) {
            runMediaMutationRollback(onAbort = { preserveBackup = true }) {
                throw AssertionError("fatal rollback failure")
            }
        }

        assertTrue(preserveBackup)
    }
}
