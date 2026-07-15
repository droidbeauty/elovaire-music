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
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

class PersistenceMaintenanceWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val database = ElovaireDatabase.create(applicationContext)
        return try {
            val health = PersistenceMaintenance(
                database.persistenceMaintenanceDao(),
                MediaMutationJournal(database.libraryDao()),
            ).recoverAndPrune()
            if (health.recoveryRequired) Result.failure() else Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: SQLiteException) {
            if (failure.isTransientMaintenanceFailure() && runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                Result.failure()
            }
        } finally {
            database.close()
        }
    }

    companion object {
        internal const val UNIQUE_WORK_NAME = "persistence-maintenance"
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
                        .setRequiresStorageNotLow(true)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30L, TimeUnit.SECONDS)
                .build()
        }
    }
}

private fun Throwable.isTransientMaintenanceFailure(): Boolean {
    return this is SQLiteDatabaseLockedException || this is SQLiteTableLockedException
}
