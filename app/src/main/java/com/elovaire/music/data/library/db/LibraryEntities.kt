package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["albumId"]),
        Index(value = ["mediaStoreId"]),
        Index(value = ["uri"], unique = true),
    ],
)
internal data class SongEntity(
    @PrimaryKey val songId: Long,
    val mediaStoreId: Long?,
    val uri: String,
    val filePath: String?,
    val fileName: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String?,
    val albumId: Long,
    val durationMs: Long,
    val trackNumber: Int,
    val discNumber: Int,
    val dateAddedSeconds: Long,
    val dateModifiedSeconds: Long,
    val releaseYear: Int?,
    val genre: String,
    val audioFormat: String,
    val audioQuality: String?,
    val metadataResolved: Boolean,
    val artUri: String?,
    val volumeNormalization: Float?,
    val lastSeenGenerationId: Long,
    val removedAtMs: Long?,
)

@Entity(tableName = "albums")
internal data class AlbumEntity(
    @PrimaryKey val albumId: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val durationMs: Long,
    val releaseYear: Int?,
    val genre: String?,
    val artUri: String?,
    val lastSeenGenerationId: Long,
    val removedAtMs: Long?,
)

@Entity(
    tableName = "media_files",
    indices = [
        Index(value = ["mediaStoreId"]),
        Index(value = ["uri"], unique = true),
        Index(value = ["stableFileKey"], unique = true),
    ],
)
internal data class MediaFileEntity(
    @PrimaryKey val stableFileKey: String,
    val mediaStoreId: Long?,
    val uri: String,
    val filePath: String?,
    val displayName: String,
    val extension: String,
    val mimeType: String?,
    val container: String,
    val codecMimeType: String?,
    val decoderAvailable: Boolean?,
    val hasAudioTrack: Boolean,
    val hasVideoTrack: Boolean,
    val fileSizeBytes: Long?,
    val dateModifiedSeconds: Long,
    val lastScannedAtMs: Long,
    val lastSeenGenerationId: Long,
)

@Entity(tableName = "scan_generations")
internal data class LibraryScanGenerationEntity(
    @PrimaryKey val generationId: Long,
    val startedAtMs: Long,
    val finishedAtMs: Long?,
    val source: String,
    val filterFingerprint: String,
    val status: String,
    val error: String?,
)

@Entity(tableName = "metadata_enrichment_state")
internal data class MetadataEnrichmentEntity(
    @PrimaryKey val songId: Long,
    val fileSignature: String,
    val enrichedAtMs: Long,
    val status: String,
    val error: String?,
)

@Entity(
    tableName = "media_mutations",
    indices = [
        Index(value = ["songId"]),
        Index(value = ["status"]),
        Index(value = ["updatedAtMs"]),
    ],
)
internal data class LibraryMutationEntity(
    @PrimaryKey val mutationId: String,
    val type: String,
    val status: String,
    val songId: Long?,
    val albumId: Long?,
    val uri: String?,
    val displayName: String?,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val attemptCount: Int,
    val error: String?,
)
