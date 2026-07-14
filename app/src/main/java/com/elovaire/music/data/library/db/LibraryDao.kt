package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
internal interface LibraryDao {
    @Query(
        "SELECT * FROM media_mutations " +
            "WHERE status IN ('Created', 'PreflightPassed', 'NeedsPermission', 'PermissionGranted', " +
            "'TempWritten', 'TempVerified', 'Committed', 'PersistedVerified', 'Published') " +
            "ORDER BY updatedAtMs ASC",
    )
    suspend fun recoverableMutations(): List<LibraryMutationEntity>

    @Query("SELECT * FROM media_mutations WHERE mutationId = :mutationId")
    suspend fun mutation(mutationId: String): LibraryMutationEntity?

    @Query("SELECT * FROM songs WHERE songId = :songId AND removedAtMs IS NULL")
    suspend fun songById(songId: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE uri = :uri AND removedAtMs IS NULL")
    suspend fun songByUri(uri: String): SongEntity?

    @Query("SELECT * FROM media_files WHERE stableFileKey = :stableFileKey")
    suspend fun mediaFileByStableKey(stableFileKey: String): MediaFileEntity?

    @Query("SELECT * FROM songs WHERE albumId = :albumId AND removedAtMs IS NULL ORDER BY discNumber, trackNumber, title")
    suspend fun activeSongsForAlbum(albumId: Long): List<SongEntity>

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

    @Query("UPDATE songs SET removedAtMs = :removedAtMs WHERE lastSeenGenerationId != :generationId AND removedAtMs IS NULL")
    suspend fun markSongsMissingFromGeneration(generationId: Long, removedAtMs: Long)

    @Query("UPDATE albums SET removedAtMs = :removedAtMs WHERE lastSeenGenerationId != :generationId AND removedAtMs IS NULL")
    suspend fun markAlbumsMissingFromGeneration(generationId: Long, removedAtMs: Long)

    @Query("UPDATE songs SET removedAtMs = :removedAtMs WHERE songId IN (:songIds)")
    suspend fun markSongsRemoved(songIds: Set<Long>, removedAtMs: Long)

    @Query("UPDATE albums SET removedAtMs = :removedAtMs WHERE albumId IN (:albumIds)")
    suspend fun markAlbumsRemoved(albumIds: Set<Long>, removedAtMs: Long)

    @Transaction
    suspend fun applyIncrementalUpdate(
        songs: List<SongEntity>,
        albums: List<AlbumEntity>,
        files: List<MediaFileEntity>,
    ) {
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
