package elovaire.music.droidbeauty.app.data.library.db

import android.content.Context
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteTableLockedException
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

class PersistenceMaintenanceWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        var database: ElovaireDatabase? = null
        return try {
            database = ElovaireDatabase.create(applicationContext)
            val maintenance = PersistenceMaintenance(
                database.persistenceMaintenanceDao(),
                MediaMutationJournal(database.libraryDao()),
                userDataDao = database.userDataDao(),
            )
            if (!maintenance.recoverCritical()) {
                return Result.failure()
            }
            if (!healthCheckDue()) {
                return Result.success()
            }
            val health = maintenance.checkAndPrune()
            if (!health.isMaintenanceSuccessful()) {
                Result.failure()
            } else {
                val checkpointSaved = applicationContext
                    .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(KEY_LAST_HEALTH_CHECK_MS, AndroidAppClock.wallTimeMs())
                    .commit()
                when {
                    checkpointSaved -> Result.success()
                    runAttemptCount < MAX_RETRY_COUNT -> Result.retry()
                    else -> Result.failure()
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: SQLiteException) {
            if (failure.isTransientMaintenanceFailure() && runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                Result.failure()
            }
        } finally {
            database?.close()
        }
    }

    companion object {
        internal const val UNIQUE_WORK_NAME = "persistence-maintenance"
        private const val PREFERENCES = "persistence-maintenance"
        private const val KEY_LAST_HEALTH_CHECK_MS = "last-successful-health-check-ms"
        private const val MAX_RETRY_COUNT = 3

        fun enqueue(context: Context) {
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request(),
            )
        }

        internal fun request(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<PersistenceMaintenanceWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiresStorageNotLow(true)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30L, TimeUnit.SECONDS)
                .build()
        }
    }

    private fun healthCheckDue(): Boolean {
        val lastHealthCheck = applicationContext
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_HEALTH_CHECK_MS, 0L)
        return persistenceHealthCheckDue(AndroidAppClock.wallTimeMs(), lastHealthCheck)
    }
}

internal fun persistenceHealthCheckDue(nowMs: Long, lastSuccessfulHealthCheckMs: Long): Boolean {
    return lastSuccessfulHealthCheckMs <= 0L || nowMs < lastSuccessfulHealthCheckMs ||
        nowMs - lastSuccessfulHealthCheckMs >= 6L * 60L * 60L * 1_000L
}

internal fun DatabaseHealth.isMaintenanceSuccessful(): Boolean {
    return physicalIntegrityValid && foreignKeysValid && orphanCount == 0 && !recoveryRequired && userDataConsistent
}

private fun Throwable.isTransientMaintenanceFailure(): Boolean {
    return this is SQLiteDatabaseLockedException || this is SQLiteTableLockedException
}
