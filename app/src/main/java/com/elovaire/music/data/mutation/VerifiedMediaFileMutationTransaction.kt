package elovaire.music.droidbeauty.app.data.mutation

import elovaire.music.droidbeauty.app.domain.kernel.MediaMutationStatus
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.data.library.network.NetworkResourceUri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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
        var originalSafe = true
        var ownsMutation = mutationId == null
        val networkTarget = NetworkResourceUri.isNetworkUri(song.uri)
        var networkFingerprint: String? = null
        var networkTemporary: String? = null
        var retainWorkingFile = false

        fun networkContext(expected: File, id: String, fingerprint: String): NetworkMutationContext =
            NetworkMutationContext(
                expectedOriginal = expected,
                operationId = id,
                sourceFingerprint = fingerprint,
                recordTemporary = { path ->
                    runBlocking { checkNotNull(mediaMutationJournal).recordNetworkTemporary(checkNotNull(effectiveMutationId), path) }
                    networkTemporary = path
                },
                resolveTemporary = {
                    val path = checkNotNull(networkTemporary)
                    runBlocking { checkNotNull(mediaMutationJournal).resolveNetworkTemporary(checkNotNull(effectiveMutationId), path) }
                    networkTemporary = null
                },
            )

        suspend fun rollbackIfNeeded() {
            val backup = backupFile ?: return
            if (originalSafe || rollbackAttempted) return
            rollbackAttempted = true
            rollbackFailed = runMediaMutationRollback(
                onAbort = { rollbackFailed = true },
            ) {
                if (networkTarget) {
                    val fingerprint = checkNotNull(networkFingerprint)
                    check(mutationRunner.networkSourceFingerprint(song.uri) == fingerprint) {
                        "The network source changed before rollback."
                    }
                    if (!mutationRunner.originalMatchesBackup(song.uri, backup)) {
                        val replacement = checkNotNull(workingFile)
                        if (networkTemporary != null || !mutationRunner.originalMatchesBackup(song.uri, replacement)) {
                            throw IOException("The network file needs recovery before it can be restored.")
                        }
                        val id = checkNotNull(effectiveMutationId)
                        mediaMutationJournal?.prepareRollback(id)
                        faultInjector.checkpoint(MediaMutationTransactionPhase.RollbackStarted)
                        mutationRunner.overwriteOriginal(
                            song.uri,
                            backup,
                            networkContext(replacement, "rollback-$id", fingerprint),
                        )
                        mutationRunner.verifyOriginalBytes(song.uri, backup)
                    }
                } else {
                    // Opening a provider in a truncating mode can change it before the
                    // write callback runs. Verify that boundary before discarding recovery bytes.
                    if (originalOverwriteStarted || !mutationRunner.originalMatchesBackup(song.uri, backup)) {
                        effectiveMutationId?.let { mediaMutationJournal?.prepareRollback(it) }
                        faultInjector.checkpoint(MediaMutationTransactionPhase.RollbackStarted)
                        mutationRunner.overwriteOriginal(song.uri, backup)
                        mutationRunner.verifyOriginalBytes(song.uri, backup)
                    }
                }
            }
            if (!rollbackFailed) originalSafe = true
        }

        return try {
            faultInjector.checkpoint(MediaMutationTransactionPhase.AfterJournal)
            mediaMutationJournal?.recoverTarget(song.uri, effectiveMutationId)
            ownsMutation = true
            if (networkTarget) {
                checkNotNull(mediaMutationJournal) { "Network editing requires a durable mutation journal." }
                checkNotNull(effectiveMutationId) { "Network editing requires a mutation identifier." }
                networkFingerprint = mutationRunner.networkSourceFingerprint(song.uri)
            }
            mutationRunner.preflight(song)
            effectiveMutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.PreflightPassed) }
            backupFile = mutationRunner.copySongToTemp(song, "backup")
            stage = MediaMutationStage.TempWrite
            workingFile = mutationRunner.createTempFile(song, if (networkTarget) "network-working" else "working").also { file ->
                mutationRunner.copyFileDurably(backupFile, file)
            }
            stage = MediaMutationStage.WorkingMutation
            faultInjector.checkpoint(MediaMutationTransactionPhase.WorkingMutationStarted)
            val value = mutateWorkingCopy(workingFile)
            effectiveMutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.TempWritten) }
            stage = MediaMutationStage.WorkingVerification
            verifyWorkingCopy(workingFile)
            if (networkTarget) FileOutputStream(workingFile, true).use { it.fd.sync() }
            faultInjector.checkpoint(MediaMutationTransactionPhase.WorkingVerified)
            effectiveMutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.TempVerified) }
            effectiveMutationId?.let {
                mediaMutationJournal?.retainBackup(
                    it,
                    backupFile,
                    networkReplacement = workingFile.takeIf { networkTarget },
                    networkSourceFingerprint = networkFingerprint,
                )
                retainWorkingFile = networkTarget
            }

            stage = MediaMutationStage.OriginalOverwrite
            originalSafe = false
            val replacementContext = if (networkTarget) {
                networkContext(backupFile, checkNotNull(effectiveMutationId), checkNotNull(networkFingerprint))
            } else null
            mutationRunner.overwriteOriginal(song.uri, workingFile, replacementContext) { originalOverwriteStarted = true }
            effectiveMutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.Committed) }

            stage = MediaMutationStage.PersistedVerification
            persistedFile = mutationRunner.copySongToTemp(song, "verify")
            verifyPersistedCopy(persistedFile)
            effectiveMutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.PersistedVerified) }
            effectiveMutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.Completed) }
            originalSafe = true
            VerifiedMediaFileMutationResult.Success(value, effectiveMutationId)
        } catch (cancelled: CancellationException) {
            withContext(NonCancellable) {
                rollbackIfNeeded()
                effectiveMutationId?.takeIf { ownsMutation }?.let {
                    mediaMutationJournal?.mark(
                        it,
                        if (rollbackFailed) MediaMutationStatus.NeedsRepair else MediaMutationStatus.Cancelled,
                    )
                }
            }
            throw cancelled
        } catch (failure: Exception) {
            rollbackIfNeeded()
            if (ownsMutation && failure !is SecurityException) {
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
            try {
                if (originalSafe && !rollbackFailed && networkTemporary == null) {
                    backupFile?.let { backup ->
                        withContext(NonCancellable) {
                            effectiveMutationId?.let { mediaMutationJournal?.clearBackup(it, backup) }
                        }
                        backup.delete()
                        retainWorkingFile = false
                    }
                }
            } finally {
                if (!retainWorkingFile) workingFile?.delete()
                persistedFile?.delete()
                checkpointCleanup()
            }
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

internal inline fun runMediaMutationRollback(
    onAbort: () -> Unit,
    rollback: () -> Unit,
): Boolean = try {
    rollback()
    false
} catch (cancelled: CancellationException) {
    onAbort()
    throw cancelled
} catch (_: Exception) {
    true
} catch (failure: Throwable) {
    onAbort()
    throw failure
}
