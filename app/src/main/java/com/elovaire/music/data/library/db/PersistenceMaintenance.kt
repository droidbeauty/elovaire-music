package elovaire.music.droidbeauty.app.data.library.db

import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationRecoveryResult
import androidx.room.Dao
import androidx.room.Query

@Dao
internal interface PersistenceMaintenanceDao {
    @Query("DELETE FROM media_mutations WHERE status IN ('Completed', 'Cancelled', 'Failed') AND updatedAtMs < :cutoffMs")
    suspend fun deleteTerminalMutationsBefore(cutoffMs: Long): Int
}

internal class PersistenceMaintenance(
    private val dao: PersistenceMaintenanceDao,
    private val mutationJournal: MediaMutationJournal,
    private val clock: AppClock = AndroidAppClock,
) {
    suspend fun recoverAndPrune() {
        if (mutationJournal.recoverIncomplete() !is MediaMutationRecoveryResult.Success) return
        dao.deleteTerminalMutationsBefore(terminalMutationCutoff(clock.wallTimeMs()))
    }
}

internal fun terminalMutationCutoff(nowMs: Long): Long {
    return (nowMs - TERMINAL_MUTATION_RETENTION_MS).coerceAtLeast(0L)
}

private const val TERMINAL_MUTATION_RETENTION_MS = 30L * 24L * 60L * 60L * 1_000L
