package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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

@Entity(
    tableName = "network_inventory",
    primaryKeys = ["sourceId", "relativePath"],
    indices = [
        Index(value = ["sourceId", "lastSeenGeneration"]),
        Index(value = ["sourceId", "songId"]),
    ],
)
internal data class NetworkInventoryEntity(
    val sourceId: String,
    val relativePath: String,
    val sizeBytes: Long?,
    val modifiedAtMs: Long?,
    val etag: String?,
    val contentType: String?,
    val sourceEntryId: String?,
    val songId: Long,
    val albumId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String?,
    val releaseYear: Int?,
    val genre: String,
    val audioFormat: String,
    val audioQuality: String?,
    val durationMs: Long,
    val trackNumber: Int,
    val discNumber: Int,
    val dateAddedSeconds: Long,
    val dateModifiedSeconds: Long?,
    val metadataResolved: Boolean,
    val artUri: String?,
    val mediaKind: String = "Music",
    val lastSeenGeneration: Long,
)

@Entity(tableName = "network_inventory_sources")
internal data class NetworkInventorySourceEntity(
    @PrimaryKey val sourceId: String,
    val generation: Long,
    val committedAtMs: Long,
    val availability: String,
    val locationFingerprint: String?,
)
