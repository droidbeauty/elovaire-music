package elovaire.music.droidbeauty.app.data.mutation

import elovaire.music.droidbeauty.app.domain.kernel.MediaMutationStatus
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File

internal sealed interface VerifiedMediaFileMutationResult<out T> {
    data class Success<T>(val value: T, val mutationId: String?) : VerifiedMediaFileMutationResult<T>

    data class Failure(
        val cause: Exception,
        val mutationId: String?,
        val stage: MediaMutationStage,
        val rollbackFailed: Boolean,
    ) : VerifiedMediaFileMutationResult<Nothing>
}

internal enum class MediaMutationStage {
    SourceRead,
    TempWrite,
    WorkingMutation,
    WorkingVerification,
    OriginalOverwrite,
    PersistedVerification,
}

/** Owns the common backup, verified replacement, readback, rollback, and cleanup protocol. */
internal class VerifiedMediaFileMutationTransaction(
    private val mutationRunner: MediaFileMutationRunner,
    private val mediaMutationJournal: MediaMutationJournal?,
    private val faultInjector: MediaMutationFaultInjector,
) {
    @Suppress("TooGenericExceptionCaught")
    suspend fun <T> execute(
        song: Song,
        operation: MediaMutationOperation,
        mutationId: String? = null,
        mutateWorkingCopy: (File) -> T,
        verifyWorkingCopy: (File) -> Unit,
        verifyPersistedCopy: (File) -> Unit,
    ): VerifiedMediaFileMutationResult<T> {
        val effectiveMutationId = mutationId ?: run {
            faultInjector.checkpoint(MediaMutationTransactionPhase.BeforeJournal)
            mediaMutationJournal?.create(operation)
        }
        var backupFile: File? = null
        var workingFile: File? = null
        var persistedFile: File? = null
        var stage = MediaMutationStage.SourceRead
        var rollbackFailed = false
        var originalOverwriteStarted = false
        var rollbackAttempted = false

        fun rollbackIfNeeded() {
            val backup = backupFile ?: return
            if (!originalOverwriteStarted || rollbackAttempted) return
            rollbackAttempted = true
            rollbackFailed = try {
                faultInjector.checkpoint(MediaMutationTransactionPhase.RollbackStarted)
                mutationRunner.overwriteOriginal(song.uri, backup)
                mutationRunner.verifyOriginalBytes(song.uri, backup)
                false
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: RuntimeException) {
                true
            }
        }

        return try {
            faultInjector.checkpoint(MediaMutationTransactionPhase.AfterJournal)
            mutationRunner.preflight(song)
            effectiveMutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.PreflightPassed) }
            backupFile = mutationRunner.copySongToTemp(song, "backup")
            stage = MediaMutationStage.TempWrite
            workingFile = mutationRunner.createTempFile(song, "working").also { file ->
                mutationRunner.copyFileDurably(backupFile, file)
            }
            stage = MediaMutationStage.WorkingMutation
            faultInjector.checkpoint(MediaMutationTransactionPhase.WorkingMutationStarted)
            val value = mutateWorkingCopy(workingFile)
            effectiveMutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.TempWritten) }
            stage = MediaMutationStage.WorkingVerification
            verifyWorkingCopy(workingFile)
            faultInjector.checkpoint(MediaMutationTransactionPhase.WorkingVerified)
            effectiveMutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.TempVerified) }

            stage = MediaMutationStage.OriginalOverwrite
            originalOverwriteStarted = true
            mutationRunner.overwriteOriginal(song.uri, workingFile)
            effectiveMutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.Committed) }

            stage = MediaMutationStage.PersistedVerification
            persistedFile = mutationRunner.copySongToTemp(song, "verify")
            verifyPersistedCopy(persistedFile)
            effectiveMutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.PersistedVerified) }
            effectiveMutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.Completed) }
            VerifiedMediaFileMutationResult.Success(value, effectiveMutationId)
        } catch (cancelled: CancellationException) {
            withContext(NonCancellable) {
                rollbackIfNeeded()
                effectiveMutationId?.let {
                    mediaMutationJournal?.mark(
                        it,
                        if (rollbackFailed) MediaMutationStatus.NeedsRepair else MediaMutationStatus.Cancelled,
                    )
                }
            }
            throw cancelled
        } catch (failure: Exception) {
            rollbackIfNeeded()
            if (failure !is SecurityException) {
                effectiveMutationId?.let {
                    mediaMutationJournal?.mark(
                        it,
                        if (rollbackFailed) MediaMutationStatus.NeedsRepair else MediaMutationStatus.Failed,
                        "${stage.name}:${failure.javaClass.simpleName}",
                    )
                }
            }
            VerifiedMediaFileMutationResult.Failure(failure, effectiveMutationId, stage, rollbackFailed)
        } finally {
            checkpointCleanup()
            if (!rollbackFailed) runCatching { backupFile?.delete() }
            runCatching { workingFile?.delete() }
            runCatching { persistedFile?.delete() }
        }
    }

    private fun checkpointCleanup() {
        try {
            faultInjector.checkpoint(MediaMutationTransactionPhase.CleanupStarted)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: RuntimeException) {
            // Cleanup faults must not hide the durable mutation result.
        }
    }
}
