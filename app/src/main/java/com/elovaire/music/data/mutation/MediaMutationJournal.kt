package elovaire.music.droidbeauty.app.data.mutation

import android.net.Uri
import elovaire.music.droidbeauty.app.data.library.db.MediaMutationDao
import elovaire.music.droidbeauty.app.data.library.db.LibraryMutationEntity
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import elovaire.music.droidbeauty.app.core.UuidOperationIdGenerator
import elovaire.music.droidbeauty.app.core.runSuspendCatching
import elovaire.music.droidbeauty.app.core.backend.BackendEvent
import elovaire.music.droidbeauty.app.core.backend.BackendEventSink
import elovaire.music.droidbeauty.app.core.backend.BackendOperationContext
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.core.backend.BackendResourceTracker
import elovaire.music.droidbeauty.app.core.backend.BackendSubsystem
import elovaire.music.droidbeauty.app.core.backend.LogcatBackendEventSink
import elovaire.music.droidbeauty.app.core.backend.emitLazy
import elovaire.music.droidbeauty.app.domain.kernel.MediaMutationStatus
import elovaire.music.droidbeauty.app.domain.kernel.isTerminal
import elovaire.music.droidbeauty.app.domain.kernel.isValidMutationTransition
import elovaire.music.droidbeauty.app.domain.kernel.recoveryStatusFor
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.Closeable
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ConcurrentHashMap

internal enum class MediaMutationType {
    TagEdit,
    EmbeddedLyricsWrite,
    ArtworkWrite,
    Delete,
}

internal enum class MediaMutationDelivery {
    AtMostOnce,
    RetryableIdempotent,
    LastWriteWins,
}

internal enum class MediaMutationReconciliation {
    TargetedLibraryRefresh,
    LibraryAndPlaybackRefresh,
    None,
}

internal data class MediaMutationOperation(
    val mutationId: String? = null,
    val type: MediaMutationType,
    val songId: Long? = null,
    val albumId: Long? = null,
    val uri: Uri? = null,
    val displayName: String? = null,
    val delivery: MediaMutationDelivery = type.defaultDelivery(),
    val reconciliation: MediaMutationReconciliation = type.defaultReconciliation(),
)

private fun MediaMutationType.defaultDelivery(): MediaMutationDelivery {
    return when (this) {
        MediaMutationType.Delete -> MediaMutationDelivery.RetryableIdempotent
        MediaMutationType.TagEdit,
        MediaMutationType.EmbeddedLyricsWrite,
        MediaMutationType.ArtworkWrite,
        -> MediaMutationDelivery.LastWriteWins
    }
}

private fun MediaMutationType.defaultReconciliation(): MediaMutationReconciliation {
    return when (this) {
        MediaMutationType.Delete -> MediaMutationReconciliation.LibraryAndPlaybackRefresh
        MediaMutationType.TagEdit,
        MediaMutationType.EmbeddedLyricsWrite,
        MediaMutationType.ArtworkWrite,
        -> MediaMutationReconciliation.TargetedLibraryRefresh
    }
}

internal class MediaMutationJournal(
    private val dao: MediaMutationDao,
    private val clock: AppClock = AndroidAppClock,
    private val operationIdGenerator: OperationIdGenerator = UuidOperationIdGenerator,
    private val backendEventSink: BackendEventSink = LogcatBackendEventSink,
    private val resourceTracker: BackendResourceTracker = BackendResourceRegistry,
    private val recoveryRunner: MediaFileMutationRunner? = null,
) : Closeable {
    private val closed = AtomicBoolean(false)
    private val activeMutationIds = ConcurrentHashMap.newKeySet<String>()

    suspend fun create(operation: MediaMutationOperation): String = transitionMutex.withLock {
        check(!closed.get()) { "Media mutation journal is closed." }
        val now = clock.wallTimeMs()
        val mutationId = operation.mutationId?.takeIf { it.isNotBlank() } ?: operationIdGenerator.nextId()
        val existing = operation.mutationId
            ?.takeIf { it.isNotBlank() }
            ?.let { dao.mutation(mutationId) }
        if (existing != null) {
            check(existing.type == operation.type.name) { "Mutation ID $mutationId cannot be reused for ${operation.type.name}." }
            check(existing.songId == operation.songId) { "Mutation ID $mutationId cannot change song target." }
            check(existing.albumId == operation.albumId) { "Mutation ID $mutationId cannot change album target." }
            check(existing.uri == operation.uri?.toString()) { "Mutation ID $mutationId cannot change URI target." }
            val status = existing.status.toMediaMutationStatusOrNull()
            check(status != null) { "Mutation $mutationId has an unknown persisted status." }
            if (!status.isTerminal()) {
                activeMutationIds += mutationId
                activeOwners[mutationId] = this
                updateActiveMutationResource()
            }
            return@withLock mutationId
        }
        dao.upsertMutation(
            LibraryMutationEntity(
                mutationId = mutationId,
                type = operation.type.name,
                status = MediaMutationStatus.Created.name,
                songId = operation.songId,
                albumId = operation.albumId,
                uri = operation.uri?.toString(),
                displayName = operation.displayName,
                createdAtMs = now,
                updatedAtMs = now,
                attemptCount = 0,
                error = null,
            ),
        )
        activeMutationIds += mutationId
        activeOwners[mutationId] = this
        updateActiveMutationResource()
        backendEventSink.emitLazy {
            BackendEvent.MediaMutationStarted(
                BackendOperationContext(mutationId, BackendSubsystem.MediaMutation, clock.elapsedTimeMs()).fields(
                    phase = MediaMutationStatus.Created.name,
                    elapsedTimeMs = clock.elapsedTimeMs(),
                    extra = mapOf("type" to operation.type.name),
                ),
            )
        }
        mutationId
    }

    suspend fun mark(
        mutationId: String,
        status: MediaMutationStatus,
        error: String? = null,
    ): Unit = transitionMutex.withLock { markLocked(mutationId, status, error) }

    suspend fun retainBackup(mutationId: String, backup: File): Unit = transitionMutex.withLock {
        val current = checkNotNull(dao.mutation(mutationId))
        check(current.status == MediaMutationStatus.TempVerified.name && current.recoveryBackupPath == null) {
            "The media mutation is not ready to replace the original."
        }
        check(current.uri != null && dao.mutationAwaitingFileRecovery(current.uri) == null) {
            "The song has an unfinished file replacement that must be recovered first."
        }
        dao.upsertMutation(current.copy(recoveryBackupPath = backup.absolutePath))
    }

    suspend fun recoverTarget(uri: Uri, mutationId: String?): Unit = transitionMutex.withLock {
        val pendingId = dao.mutationAwaitingFileRecovery(uri.toString()) ?: return@withLock
        val pending = checkNotNull(dao.mutation(pendingId))
        val resumingPermission = pendingId == mutationId && pending.status == MediaMutationStatus.PermissionGranted.name
        check(!activeOwners.containsKey(pendingId) || pending.status == MediaMutationStatus.NeedsPermission.name || resumingPermission) {
            "The song is already being edited."
        }
        recoverBackupLocked(pending, resumingPermission)
    }

    suspend fun clearBackup(mutationId: String): Unit = transitionMutex.withLock {
        val current = dao.mutation(mutationId) ?: return@withLock
        if (current.recoveryBackupPath != null) {
            dao.upsertMutation(current.copy(recoveryBackupPath = null))
        }
    }

    suspend fun prepareRollback(mutationId: String): Unit = transitionMutex.withLock {
        val current = checkNotNull(dao.mutation(mutationId))
        check(current.recoveryBackupPath != null) { "The media rollback has no durable backup." }
        // A rollback can itself be interrupted. Persist an unverified state before it
        // touches the original, even if verification of the replacement had succeeded.
        dao.upsertMutation(current.copy(status = MediaMutationStatus.TempVerified.name))
        activeMutationIds += mutationId
        activeOwners[mutationId] = this
    }

    private suspend fun markLocked(
        mutationId: String,
        status: MediaMutationStatus,
        error: String? = null,
    ) {
        val current = dao.mutation(mutationId) ?: return
        val currentStatus = current.status.toMediaMutationStatusOrNull() ?: return
        if (!isValidMutationTransition(currentStatus, status)) return
        dao.upsertMutation(
            current.copy(
                status = status.name,
                updatedAtMs = clock.wallTimeMs(),
                attemptCount = if (status == MediaMutationStatus.Failed || status == MediaMutationStatus.NeedsRepair) {
                    current.attemptCount + 1
                } else {
                    current.attemptCount
                },
                error = error,
            ),
        )
        if (!status.isTerminal()) {
            activeMutationIds += mutationId
            activeOwners[mutationId] = this
        }
        if (status.isTerminal()) {
            activeMutationIds -= mutationId
            activeOwners.remove(mutationId, this)
            updateActiveMutationResource()
        }
    }

    suspend fun recoverIncomplete(maxRecords: Int = MAX_RECOVERY_RECORDS): MediaMutationRecoveryResult {
        require(maxRecords > 0) { "Recovery batch size must be positive." }
        val operation = BackendOperationContext(
            operationIdGenerator.nextId(), BackendSubsystem.MediaMutation, clock.elapsedTimeMs(),
        )
        val recovery = runSuspendCatching {
            transitionMutex.withLock {
                var recoveredCount = 0
                val batch = dao.recoverableMutationBatch(maxRecords)
                batch.mutations.forEach { mutation ->
                    currentCoroutineContext().ensureActive()
                    if (activeOwners.containsKey(mutation.mutationId)) return@forEach
                    if (mutation.recoveryBackupPath != null) {
                        recoverBackupLocked(mutation)
                        recoveredCount += 1
                        return@forEach
                    }
                    val current = mutation.status.toMediaMutationStatusOrNull()
                    if (current == null) {
                        dao.upsertMutation(
                            mutation.copy(
                                status = MediaMutationStatus.NeedsRepair.name,
                                updatedAtMs = clock.wallTimeMs(),
                                attemptCount = mutation.attemptCount + 1,
                                error = "Unknown persisted mutation status: ${mutation.status}",
                            ),
                        )
                        recoveredCount += 1
                        return@forEach
                    }
                    recoveryStatusFor(current)?.let { recoveredStatus ->
                        markLocked(mutation.mutationId, recoveredStatus, mutation.error)
                        recoveredCount += 1
                    }
                }
                recoveredCount to (batch.totalCount - recoveredCount).coerceAtLeast(0)
            }
        }
        val failure = recovery.exceptionOrNull()
        if (failure != null) {
            backendEventSink.emitLazy {
                BackendEvent.MediaMutationFailed(
                    operation.fields(
                        phase = "startup_recovery",
                        elapsedTimeMs = clock.elapsedTimeMs(),
                        extra = mapOf("error_type" to (failure::class.simpleName ?: "Unknown")),
                    ),
                )
            }
            return MediaMutationRecoveryResult.Failure(failure)
        }
        val (recoveredCount, remainingCount) = recovery.getOrThrow()
        backendEventSink.emitLazy {
            BackendEvent.MediaMutationCompleted(
                operation.fields(
                    phase = "startup_recovery",
                    elapsedTimeMs = clock.elapsedTimeMs(),
                    extra = mapOf(
                        "recovered" to recoveredCount.toString(),
                        "remaining" to remainingCount.toString(),
                    ),
                ),
            )
        }
        return MediaMutationRecoveryResult.Success(recoveredCount, remainingCount)
    }

    private suspend fun recoverBackupLocked(mutation: LibraryMutationEntity, resumingPermission: Boolean = false) {
        val backup = checkNotNull(recoveryRunner) { "Media file recovery is unavailable." }.recoverBackup(mutation)
        val verified = mutation.status in setOf(
            MediaMutationStatus.PersistedVerified.name,
            MediaMutationStatus.Published.name,
            MediaMutationStatus.Completed.name,
        )
        dao.upsertMutation(
            mutation.copy(
                status = when {
                    resumingPermission -> MediaMutationStatus.PermissionGranted.name
                    verified -> MediaMutationStatus.Completed.name
                    else -> MediaMutationStatus.Cancelled.name
                },
                recoveryBackupPath = null,
                updatedAtMs = clock.wallTimeMs(),
                error = null,
            ),
        )
        backup.delete()
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        activeMutationIds.forEach { activeOwners.remove(it, this) }
        activeMutationIds.clear()
        updateActiveMutationResource()
    }

    private fun updateActiveMutationResource() {
        resourceTracker.set(BackendResourceKind.ActiveMutation, activeMutationIds.size)
    }

    private companion object {
        // Maintenance opens its own Room handle. Serialize journal transitions and leave
        // live edits alone; only a departed owner's records are eligible for recovery.
        val transitionMutex = Mutex()
        val activeOwners = ConcurrentHashMap<String, MediaMutationJournal>()
    }
}

internal sealed interface MediaMutationRecoveryResult {
    data class Success(
        val recoveredCount: Int,
        val remainingCount: Int = 0,
    ) : MediaMutationRecoveryResult
    data class Failure(val cause: Throwable) : MediaMutationRecoveryResult
}

private const val MAX_RECOVERY_RECORDS = 128

private fun String.toMediaMutationStatusOrNull(): MediaMutationStatus? {
    return enumValues<MediaMutationStatus>().firstOrNull { it.name == this }
}
