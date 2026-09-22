package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
internal interface MediaMutationDao {
    @Query(
        "SELECT * FROM media_mutations " +
            "WHERE status NOT IN ('Completed', 'Cancelled', 'Failed', 'NeedsRepair') " +
            "ORDER BY updatedAtMs ASC, mutationId ASC",
    )
    suspend fun recoverableMutations(): List<LibraryMutationEntity>

    @Query("SELECT * FROM media_mutations WHERE mutationId = :mutationId")
    suspend fun mutation(mutationId: String): LibraryMutationEntity?

    @Upsert
    suspend fun upsertMutation(mutation: LibraryMutationEntity)
}

@Dao
internal interface NetworkInventoryDao {
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
}
