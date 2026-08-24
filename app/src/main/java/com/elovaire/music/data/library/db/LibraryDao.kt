package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
@Suppress("TooManyFunctions")
internal interface LibraryDao {
    @Query("SELECT MAX(generationId) FROM scan_generations")
    suspend fun latestGenerationId(): Long?

    @Query(
        "SELECT * FROM media_mutations " +
            "WHERE status NOT IN ('Completed', 'Cancelled', 'Failed', 'NeedsRepair') " +
            "ORDER BY updatedAtMs ASC, mutationId ASC",
    )
    suspend fun recoverableMutations(): List<LibraryMutationEntity>

    @Query("SELECT * FROM media_mutations WHERE mutationId = :mutationId")
    suspend fun mutation(mutationId: String): LibraryMutationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanGeneration(generation: LibraryScanGenerationEntity)

    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Upsert
    suspend fun upsertAlbums(albums: List<AlbumEntity>)

    @Upsert
    suspend fun upsertMediaFiles(files: List<MediaFileEntity>)

    @Upsert
    suspend fun upsertMutation(mutation: LibraryMutationEntity)

    @Query("SELECT * FROM network_inventory WHERE sourceId = :sourceId ORDER BY relativePath ASC")
    suspend fun networkInventory(sourceId: String): List<NetworkInventoryEntity>

    @Query("SELECT * FROM network_inventory_sources WHERE sourceId = :sourceId")
    suspend fun networkInventorySource(sourceId: String): NetworkInventorySourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNetworkInventory(entries: List<NetworkInventoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNetworkInventorySource(source: NetworkInventorySourceEntity)

    @Query("UPDATE network_inventory_sources SET committedAtMs = :committedAtMs, availability = :availability, locationFingerprint = :locationFingerprint WHERE sourceId = :sourceId")
    suspend fun refreshNetworkInventorySource(
        sourceId: String,
        committedAtMs: Long,
        availability: String,
        locationFingerprint: String,
    )

    @Query("DELETE FROM network_inventory WHERE sourceId = :sourceId AND lastSeenGeneration != :generation")
    suspend fun deleteUnseenNetworkInventory(sourceId: String, generation: Long)

    @Query("DELETE FROM network_inventory WHERE sourceId = :sourceId")
    suspend fun deleteNetworkInventory(sourceId: String)

    @Query("DELETE FROM network_inventory_sources WHERE sourceId = :sourceId")
    suspend fun deleteNetworkInventorySource(sourceId: String)

    @Transaction
    suspend fun replaceNetworkInventory(
        source: NetworkInventorySourceEntity,
        entries: List<NetworkInventoryEntity>,
    ) {
        if (entries.isNotEmpty()) upsertNetworkInventory(entries)
        deleteUnseenNetworkInventory(source.sourceId, source.generation)
        upsertNetworkInventorySource(source)
    }

    @Query("UPDATE songs SET removedAtMs = :removedAtMs WHERE lastSeenGenerationId != :generationId AND removedAtMs IS NULL")
    suspend fun markSongsMissingFromGeneration(generationId: Long, removedAtMs: Long)

    @Query("UPDATE albums SET removedAtMs = :removedAtMs WHERE lastSeenGenerationId != :generationId AND removedAtMs IS NULL")
    suspend fun markAlbumsMissingFromGeneration(generationId: Long, removedAtMs: Long)

    @Query("UPDATE songs SET removedAtMs = :removedAtMs WHERE songId IN (:songIds)")
    suspend fun markSongsRemoved(songIds: Set<Long>, removedAtMs: Long)

    @Query("UPDATE albums SET removedAtMs = :removedAtMs WHERE albumId IN (:albumIds)")
    suspend fun markAlbumsRemoved(albumIds: Set<Long>, removedAtMs: Long)

    @Query("DELETE FROM media_files WHERE stableFileKey IN (:stableKeys) OR uri IN (:uris)")
    suspend fun deleteMediaFiles(stableKeys: Set<String>, uris: Set<String>)

    @Transaction
    suspend fun applyIncrementalUpdate(
        songs: List<SongEntity>,
        albums: List<AlbumEntity>,
        files: List<MediaFileEntity>,
        replacedFileKeys: Set<String> = emptySet(),
        replacedUris: Set<String> = emptySet(),
    ) {
        if (replacedFileKeys.isNotEmpty() || replacedUris.isNotEmpty()) {
            deleteMediaFiles(replacedFileKeys, replacedUris)
        }
        if (songs.isNotEmpty()) upsertSongs(songs)
        if (albums.isNotEmpty()) upsertAlbums(albums)
        if (files.isNotEmpty()) upsertMediaFiles(files)
    }

    @Transaction
    suspend fun applyIncrementalRemoval(
        songIds: Set<Long>,
        albumIds: Set<Long>,
        removedAtMs: Long,
    ) {
        if (songIds.isNotEmpty()) markSongsRemoved(songIds, removedAtMs)
        if (albumIds.isNotEmpty()) markAlbumsRemoved(albumIds, removedAtMs)
    }

    @Transaction
    suspend fun applyIncrementalChange(
        songs: List<SongEntity>,
        albums: List<AlbumEntity>,
        files: List<MediaFileEntity>,
        removedSongIds: Set<Long>,
        removedAlbumIds: Set<Long>,
        removedFileKeys: Set<String>,
        removedUris: Set<String>,
        removedAtMs: Long,
    ) {
        if (removedFileKeys.isNotEmpty() || removedUris.isNotEmpty()) {
            deleteMediaFiles(removedFileKeys, removedUris)
        }
        if (songs.isNotEmpty()) upsertSongs(songs)
        if (albums.isNotEmpty()) upsertAlbums(albums)
        if (files.isNotEmpty()) upsertMediaFiles(files)
        if (removedSongIds.isNotEmpty()) markSongsRemoved(removedSongIds, removedAtMs)
        if (removedAlbumIds.isNotEmpty()) markAlbumsRemoved(removedAlbumIds, removedAtMs)
    }

    @Transaction
    suspend fun replaceGeneration(
        generation: LibraryScanGenerationEntity,
        songs: List<SongEntity>,
        albums: List<AlbumEntity>,
        files: List<MediaFileEntity>,
        removedAtMs: Long,
    ) {
        insertScanGeneration(generation)
        upsertSongs(songs)
        upsertAlbums(albums)
        upsertMediaFiles(files)
        markSongsMissingFromGeneration(generation.generationId, removedAtMs)
        markAlbumsMissingFromGeneration(generation.generationId, removedAtMs)
    }
}
