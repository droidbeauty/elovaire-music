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
import elovaire.music.droidbeauty.app.core.backend.emitLazy
import elovaire.music.droidbeauty.app.domain.kernel.MediaMutationStatus
import elovaire.music.droidbeauty.app.domain.kernel.isTerminal
import elovaire.music.droidbeauty.app.domain.kernel.isValidMutationTransition
import elovaire.music.droidbeauty.app.domain.kernel.recoveryStatusFor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

internal enum class MediaMutationType {
    TagEdit,
    EmbeddedLyricsWrite,
    ArtworkWrite,
    Delete,
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
    private val transitionMutex = Mutex()

    suspend fun create(operation: MediaMutationOperation): String = transitionMutex.withLock {
        val now = clock.wallTimeMs()
        val mutationId = operation.mutationId?.takeIf { it.isNotBlank() } ?: operationIdGenerator.nextId()
        if (operation.mutationId != null && dao.mutation(mutationId) != null) {
            activeMutationIds += mutationId
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
    ): Unit = transitionMutex.withLock {
        val current = dao.mutation(mutationId) ?: return@withLock
        val currentStatus = current.status.toMediaMutationStatusOrNull() ?: return@withLock
        if (!isValidMutationTransition(currentStatus, status)) return@withLock
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
        if (status.isTerminal()) activeMutationIds -= mutationId
    }

    suspend fun recoverIncomplete(): MediaMutationRecoveryResult {
        val operation = BackendOperationContext(
            operationIdGenerator.nextId(), BackendSubsystem.MediaMutation, clock.elapsedTimeMs(),
        )
        val recovery = runCatching {
            var recoveredCount = 0
            dao.recoverableMutations().filterNot { it.mutationId in activeMutationIds }.forEach { mutation ->
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
        val recoveredCount = recovery.getOrThrow()
        backendEventSink.emitLazy {
            BackendEvent.MediaMutationCompleted(
                operation.fields(
                    phase = "startup_recovery",
                    elapsedTimeMs = clock.elapsedTimeMs(),
                    extra = mapOf("recovered" to recoveredCount.toString()),
                ),
            )
        }
        return MediaMutationRecoveryResult.Success(recoveredCount)
    }

    private companion object {
        val activeMutationIds: MutableSet<String> = ConcurrentHashMap.newKeySet()
    }
}

internal sealed interface MediaMutationRecoveryResult {
    data class Success(val recoveredCount: Int) : MediaMutationRecoveryResult
    data class Failure(val cause: Throwable) : MediaMutationRecoveryResult
}

private fun String.toMediaMutationStatusOrNull(): MediaMutationStatus? {
    return enumValues<MediaMutationStatus>().firstOrNull { it.name == this }
}
