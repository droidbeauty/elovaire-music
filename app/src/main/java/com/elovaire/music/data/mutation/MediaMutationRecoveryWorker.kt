package elovaire.music.droidbeauty.app.data.mutation

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import kotlinx.coroutines.flow.first

internal class MediaMutationRecoveryWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val database = ElovaireDatabase.create(applicationContext)
        return try {
            database.libraryDao().activeMutations().first()
            Result.success()
        } finally {
            database.close()
        }
    }
}

internal object MediaMutationRecoveryWork {
    private const val UNIQUE_WORK_NAME = "media_mutation_recovery"

    fun enqueue(context: Context) {
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<MediaMutationRecoveryWorker>().build(),
        )
    }
}
