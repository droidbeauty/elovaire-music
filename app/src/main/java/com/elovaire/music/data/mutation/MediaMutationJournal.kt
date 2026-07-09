package elovaire.music.droidbeauty.app.data.mutation

import android.net.Uri
import elovaire.music.droidbeauty.app.data.library.db.LibraryDao
import elovaire.music.droidbeauty.app.data.library.db.LibraryMutationEntity
import java.util.UUID

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
    val mutationId: String = UUID.randomUUID().toString(),
    val type: MediaMutationType,
    val songId: Long? = null,
    val albumId: Long? = null,
    val uri: Uri? = null,
    val displayName: String? = null,
)

internal class MediaMutationJournal(
    private val dao: LibraryDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend fun create(operation: MediaMutationOperation): String {
        val now = clock()
        dao.upsertMutation(
            LibraryMutationEntity(
                mutationId = operation.mutationId,
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
        return operation.mutationId
    }

    suspend fun mark(
        mutationId: String,
        status: MediaMutationStatus,
        error: String? = null,
    ) {
        val current = dao.mutation(mutationId) ?: return
        dao.upsertMutation(
            current.copy(
                status = status.name,
                updatedAtMs = clock(),
                attemptCount = if (status == MediaMutationStatus.Failed || status == MediaMutationStatus.NeedsRepair) {
                    current.attemptCount + 1
                } else {
                    current.attemptCount
                },
                error = error,
            ),
        )
    }
}
