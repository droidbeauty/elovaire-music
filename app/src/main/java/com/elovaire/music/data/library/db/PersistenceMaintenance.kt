package elovaire.music.droidbeauty.app.data.library.db

import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationRecoveryResult
import androidx.room.Dao
import androidx.room.RawQuery
import androidx.room.Query
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
internal interface PersistenceMaintenanceDao {
    @Query("SELECT COUNT(*) FROM pragma_foreign_key_check")
    suspend fun foreignKeyViolationCount(): Int

    @RawQuery
    suspend fun quickCheck(query: SupportSQLiteQuery): List<String>

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

    @Query(
        "SELECT COUNT(*) FROM user_playlist_entries AS entry " +
            "LEFT JOIN user_playlists AS playlist ON playlist.playlistId = entry.playlistId " +
            "WHERE playlist.playlistId IS NULL OR entry.position < 0 OR entry.position >= " +
            "(SELECT COUNT(*) FROM user_playlist_entries AS sibling WHERE sibling.playlistId = entry.playlistId)",
    )
    suspend fun invalidPlaylistEntryCount(): Int

    @Query(
        "SELECT COUNT(*) FROM favorite_songs AS favorite " +
            "WHERE favorite.position < 0 OR favorite.position >= (SELECT COUNT(*) FROM favorite_songs)",
    )
    suspend fun invalidFavoritePositionCount(): Int

    @Query("SELECT COUNT(*) FROM song_play_counts WHERE playCount < 0")
    suspend fun invalidSongPlayCountCount(): Int

    @Query("SELECT COUNT(*) FROM album_play_counts WHERE playCount < 0")
    suspend fun invalidAlbumPlayCountCount(): Int

    @Query(
        "SELECT COUNT(*) FROM recent_playback AS recent " +
            "WHERE recent.position < 0 OR recent.position >= " +
            "(SELECT COUNT(*) FROM recent_playback AS sibling WHERE sibling.kind = recent.kind)",
    )
    suspend fun invalidRecentPositionCount(): Int

    @Query("SELECT COUNT(*) FROM user_smart_playlists WHERE trim(payload) = ''")
    suspend fun invalidSmartPlaylistCount(): Int

}

internal enum class PersistenceHealthStatus {
    Healthy,
    RebuildableDerivedState,
    RepairableUserState,
    AmbiguousUserState,
    FatalStorageFailure,
}

internal data class DatabaseHealth(
    val foreignKeysValid: Boolean,
    val orphanCount: Int,
    val recoveryRequired: Boolean,
    val userDataConsistent: Boolean = true,
    val status: PersistenceHealthStatus = PersistenceHealthStatus.Healthy,
    val physicalIntegrityValid: Boolean = true,
)

internal class PersistenceMaintenance(
    private val dao: PersistenceMaintenanceDao,
    private val mutationJournal: MediaMutationJournal,
    private val clock: AppClock = AndroidAppClock,
    private val userDataDao: UserDataDao? = null,
) {
    suspend fun recoverCritical(): Boolean {
        return mutationJournal.recoverIncomplete() is MediaMutationRecoveryResult.Success
    }

    suspend fun checkAndPrune(): DatabaseHealth {
        val physicalIntegrityValid = dao.quickCheck(SimpleSQLiteQuery("PRAGMA quick_check")) == listOf("ok")
        if (!physicalIntegrityValid) {
            return DatabaseHealth(
                foreignKeysValid = false,
                orphanCount = -1,
                recoveryRequired = true,
                status = PersistenceHealthStatus.FatalStorageFailure,
                physicalIntegrityValid = false,
            )
        }
        val foreignKeyViolationCount = dao.foreignKeyViolationCount()
        val orphanCount = dao.activeOrphanSongCount()
        val repairRequired = dao.repairRequiredMutationCount() > 0
        val invalidPlaylistEntries = dao.invalidPlaylistEntryCount()
        val invalidFavorites = dao.invalidFavoritePositionCount()
        val invalidSongPlayCounts = dao.invalidSongPlayCountCount()
        val invalidAlbumPlayCounts = dao.invalidAlbumPlayCountCount()
        val invalidRecentPlayback = dao.invalidRecentPositionCount()
        val invalidSmartPlaylists = dao.invalidSmartPlaylistCount()
        val repairPlan = UserDataRepairer.plan(
            invalidPlaylistEntries = invalidPlaylistEntries > 0,
            invalidFavorites = invalidFavorites > 0,
            invalidRecentPlayback = invalidRecentPlayback > 0,
            invalidSongPlayCounts = invalidSongPlayCounts > 0,
            invalidAlbumPlayCounts = invalidAlbumPlayCounts > 0,
        )
        if (foreignKeyViolationCount == 0 && !repairRequired && userDataDao != null && !repairPlan.isEmpty) {
            userDataDao.repairDeterministicUserData(repairPlan)
        }
        val userDataConsistent = dao.invalidPlaylistEntryCount() == 0 &&
            dao.invalidFavoritePositionCount() == 0 &&
            dao.invalidSongPlayCountCount() == 0 &&
            dao.invalidAlbumPlayCountCount() == 0 &&
            dao.invalidRecentPositionCount() == 0 &&
            dao.invalidSmartPlaylistCount() == 0
        dao.deleteTerminalMutationsBefore(terminalMutationCutoff(clock.wallTimeMs()))
        dao.pruneScanGenerations(SCAN_GENERATION_RETENTION_COUNT)
        return DatabaseHealth(
            foreignKeysValid = foreignKeyViolationCount == 0,
            orphanCount = orphanCount,
            recoveryRequired = repairRequired || !userDataConsistent,
            userDataConsistent = userDataConsistent,
            physicalIntegrityValid = physicalIntegrityValid,
            status = when {
                foreignKeyViolationCount > 0 -> PersistenceHealthStatus.FatalStorageFailure
                repairRequired -> PersistenceHealthStatus.AmbiguousUserState
                invalidSmartPlaylists > 0 -> PersistenceHealthStatus.AmbiguousUserState
                !userDataConsistent -> PersistenceHealthStatus.RepairableUserState
                orphanCount > 0 -> PersistenceHealthStatus.RebuildableDerivedState
                else -> PersistenceHealthStatus.Healthy
            },
        )
    }

    suspend fun recoverAndPrune(): DatabaseHealth {
        if (!recoverCritical()) {
            return DatabaseHealth(
                foreignKeysValid = false,
                orphanCount = -1,
                recoveryRequired = true,
                status = PersistenceHealthStatus.FatalStorageFailure,
                physicalIntegrityValid = false,
            )
        }
        return checkAndPrune()
    }
}

internal fun terminalMutationCutoff(nowMs: Long): Long {
    return (nowMs - TERMINAL_MUTATION_RETENTION_MS).coerceAtLeast(0L)
}

private const val TERMINAL_MUTATION_RETENTION_MS = 30L * 24L * 60L * 60L * 1_000L
private const val SCAN_GENERATION_RETENTION_COUNT = 64
