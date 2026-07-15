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

    @Query(
        "DELETE FROM scan_generations WHERE generationId NOT IN " +
            "(SELECT generationId FROM scan_generations " +
            "ORDER BY finishedAtMs DESC, generationId DESC LIMIT :retainCount)",
    )
    suspend fun pruneScanGenerations(retainCount: Int): Int

    @Query(
        "SELECT COUNT(*) FROM songs AS song LEFT JOIN albums AS album ON album.albumId = song.albumId " +
            "WHERE song.removedAtMs IS NULL AND (album.albumId IS NULL OR album.removedAtMs IS NOT NULL)",
    )
    suspend fun activeOrphanSongCount(): Int

    @Query("SELECT COUNT(*) FROM media_mutations WHERE status = 'NeedsRepair'")
    suspend fun repairRequiredMutationCount(): Int
}

internal data class DatabaseHealth(
    val foreignKeysValid: Boolean,
    val orphanCount: Int,
    val recoveryRequired: Boolean,
)

internal class PersistenceMaintenance(
    private val dao: PersistenceMaintenanceDao,
    private val mutationJournal: MediaMutationJournal,
    private val clock: AppClock = AndroidAppClock,
) {
    suspend fun recoverAndPrune(): DatabaseHealth {
        if (mutationJournal.recoverIncomplete() !is MediaMutationRecoveryResult.Success) {
            return DatabaseHealth(
                foreignKeysValid = false,
                orphanCount = -1,
                recoveryRequired = true,
            )
        }
        val orphanCount = dao.activeOrphanSongCount()
        val repairRequired = dao.repairRequiredMutationCount() > 0
        dao.deleteTerminalMutationsBefore(terminalMutationCutoff(clock.wallTimeMs()))
        dao.pruneScanGenerations(SCAN_GENERATION_RETENTION_COUNT)
        return DatabaseHealth(
            foreignKeysValid = orphanCount == 0,
            orphanCount = orphanCount,
            recoveryRequired = repairRequired,
        )
    }
}

internal fun terminalMutationCutoff(nowMs: Long): Long {
    return (nowMs - TERMINAL_MUTATION_RETENTION_MS).coerceAtLeast(0L)
}

private const val TERMINAL_MUTATION_RETENTION_MS = 30L * 24L * 60L * 60L * 1_000L
private const val SCAN_GENERATION_RETENTION_COUNT = 64
