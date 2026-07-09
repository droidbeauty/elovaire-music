package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LibraryDao {
    @Query("SELECT * FROM songs WHERE removedAtMs IS NULL ORDER BY dateAddedSeconds DESC")
    fun songs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM albums WHERE removedAtMs IS NULL ORDER BY title COLLATE NOCASE")
    fun albums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM media_mutations WHERE status NOT IN ('Completed', 'Cancelled') ORDER BY updatedAtMs ASC")
    fun activeMutations(): Flow<List<LibraryMutationEntity>>

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

    @Query("UPDATE songs SET removedAtMs = :removedAtMs WHERE lastSeenGenerationId != :generationId AND removedAtMs IS NULL")
    suspend fun markSongsMissingFromGeneration(generationId: Long, removedAtMs: Long)

    @Query("UPDATE albums SET removedAtMs = :removedAtMs WHERE lastSeenGenerationId != :generationId AND removedAtMs IS NULL")
    suspend fun markAlbumsMissingFromGeneration(generationId: Long, removedAtMs: Long)

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
