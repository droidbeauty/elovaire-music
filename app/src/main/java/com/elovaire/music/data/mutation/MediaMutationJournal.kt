package elovaire.music.droidbeauty.app.data.mutation

import android.net.Uri
import elovaire.music.droidbeauty.app.data.library.db.LibraryDao
import elovaire.music.droidbeauty.app.data.library.db.LibraryMutationEntity
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import elovaire.music.droidbeauty.app.core.UuidOperationIdGenerator
import elovaire.music.droidbeauty.app.core.backend.BackendEvent
import elovaire.music.droidbeauty.app.core.backend.BackendEventSink
import elovaire.music.droidbeauty.app.core.backend.BackendOperationContext
import elovaire.music.droidbeauty.app.core.backend.BackendSubsystem
import elovaire.music.droidbeauty.app.core.backend.LogcatBackendEventSink
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

internal enum class MediaMutationType {
    TagEdit,
    EmbeddedLyricsWrite,
    ArtworkWrite,
    Delete,
}

internal enum class MediaMutationStatus {
    Created,
    PreflightPassed,
    NeedsPermission,
    PermissionGranted,
    TempWritten,
    TempVerified,
    Committed,
    PersistedVerified,
    Published,
    Completed,
    Failed,
    NeedsRepair,
    Cancelled,
}

internal data class MediaMutationOperation(
    val mutationId: String? = null,
    val type: MediaMutationType,
    val songId: Long? = null,
    val albumId: Long? = null,
    val uri: Uri? = null,
    val displayName: String? = null,
)

internal class MediaMutationJournal(
    private val dao: LibraryDao,
    private val clock: AppClock = AndroidAppClock,
    private val operationIdGenerator: OperationIdGenerator = UuidOperationIdGenerator,
    private val backendEventSink: BackendEventSink = LogcatBackendEventSink,
) {
    suspend fun create(operation: MediaMutationOperation): String {
        val now = clock.wallTimeMs()
        val mutationId = operation.mutationId?.takeIf { it.isNotBlank() } ?: operationIdGenerator.nextId()
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
        backendEventSink.emit(
            BackendEvent.MediaMutationStarted(
                BackendOperationContext(mutationId, BackendSubsystem.MediaMutation, clock.elapsedTimeMs()).fields(
                    phase = MediaMutationStatus.Created.name,
                    elapsedTimeMs = clock.elapsedTimeMs(),
                    extra = mapOf("type" to operation.type.name),
                ),
            ),
        )
        return mutationId
    }

    suspend fun mark(
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
    }

    suspend fun recoverIncomplete(): MediaMutationRecoveryResult {
        val operation = BackendOperationContext(
            operationIdGenerator.nextId(), BackendSubsystem.MediaMutation, clock.elapsedTimeMs(),
        )
        val recovery = runCatching {
            var recoveredCount = 0
            dao.activeMutations().first().forEach { mutation ->
                val current = mutation.status.toMediaMutationStatusOrNull() ?: return@forEach
                recoveryStatusFor(current)?.let { recoveredStatus ->
                    mark(mutation.mutationId, recoveredStatus, mutation.error)
                    recoveredCount += 1
                }
            }
            recoveredCount
        }
        val failure = recovery.exceptionOrNull()
        if (failure is CancellationException) throw failure
        if (failure != null) {
            backendEventSink.emit(
                BackendEvent.MediaMutationFailed(
                    operation.fields(
                        phase = "startup_recovery",
                        elapsedTimeMs = clock.elapsedTimeMs(),
                        extra = mapOf("error_type" to (failure::class.simpleName ?: "Unknown")),
                    ),
                ),
            )
            return MediaMutationRecoveryResult.Failure(failure)
        }
        val recoveredCount = recovery.getOrThrow()
        backendEventSink.emit(
            BackendEvent.MediaMutationCompleted(
                operation.fields(
                    phase = "startup_recovery",
                    elapsedTimeMs = clock.elapsedTimeMs(),
                    extra = mapOf("recovered" to recoveredCount.toString()),
                ),
            ),
        )
        return MediaMutationRecoveryResult.Success(recoveredCount)
    }
}

internal sealed interface MediaMutationRecoveryResult {
    data class Success(val recoveredCount: Int) : MediaMutationRecoveryResult
    data class Failure(val cause: Throwable) : MediaMutationRecoveryResult
}

internal fun recoveryStatusFor(status: MediaMutationStatus): MediaMutationStatus? {
    return when (status) {
        MediaMutationStatus.Created,
        MediaMutationStatus.PreflightPassed,
        MediaMutationStatus.NeedsPermission,
        MediaMutationStatus.PermissionGranted,
        MediaMutationStatus.TempWritten,
        MediaMutationStatus.TempVerified,
        -> MediaMutationStatus.Cancelled
        MediaMutationStatus.Committed -> MediaMutationStatus.NeedsRepair
        MediaMutationStatus.PersistedVerified,
        MediaMutationStatus.Published,
        -> MediaMutationStatus.Completed
        MediaMutationStatus.Failed,
        MediaMutationStatus.NeedsRepair,
        MediaMutationStatus.Completed,
        MediaMutationStatus.Cancelled,
        -> null
    }
}

internal fun isValidMutationTransition(
    current: MediaMutationStatus,
    next: MediaMutationStatus,
): Boolean {
    if (current == next) return false
    return next in when (current) {
        MediaMutationStatus.Created -> setOf(
            MediaMutationStatus.PreflightPassed,
            MediaMutationStatus.Failed,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.PreflightPassed -> setOf(
            MediaMutationStatus.NeedsPermission,
            MediaMutationStatus.TempWritten,
            MediaMutationStatus.Failed,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.NeedsPermission -> setOf(
            MediaMutationStatus.PermissionGranted,
            MediaMutationStatus.Failed,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.PermissionGranted -> setOf(
            MediaMutationStatus.TempWritten,
            MediaMutationStatus.Failed,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.TempWritten -> setOf(
            MediaMutationStatus.TempVerified,
            MediaMutationStatus.Failed,
            MediaMutationStatus.NeedsRepair,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.TempVerified -> setOf(
            MediaMutationStatus.Committed,
            MediaMutationStatus.Failed,
            MediaMutationStatus.NeedsRepair,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.Committed -> setOf(
            MediaMutationStatus.PersistedVerified,
            MediaMutationStatus.Failed,
            MediaMutationStatus.NeedsRepair,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.PersistedVerified -> setOf(
            MediaMutationStatus.Published,
            MediaMutationStatus.Completed,
            MediaMutationStatus.Failed,
            MediaMutationStatus.NeedsRepair,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.Published -> setOf(
            MediaMutationStatus.Completed,
            MediaMutationStatus.Failed,
            MediaMutationStatus.NeedsRepair,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.Failed -> setOf(MediaMutationStatus.NeedsRepair)
        MediaMutationStatus.NeedsRepair,
        MediaMutationStatus.Completed,
        MediaMutationStatus.Cancelled,
        -> emptySet()
    }
}

private fun String.toMediaMutationStatusOrNull(): MediaMutationStatus? {
    return enumValues<MediaMutationStatus>().firstOrNull { it.name == this }
}
