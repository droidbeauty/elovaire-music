package elovaire.music.droidbeauty.app.data.mutation

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.library.db.LibraryMutationEntity
import elovaire.music.droidbeauty.app.domain.kernel.MediaMutationStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MediaMutationRecoveryBatchTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "media_mutation_recovery_batch_test.db"
    private lateinit var database: ElovaireDatabase

    @Before
    fun setUp() {
        context.deleteDatabase(databaseName)
        database = Room.databaseBuilder(context, ElovaireDatabase::class.java, databaseName).build()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun recoveryBatchReturnsAtMostTheRequestedRecordsAndTheTotalCount() = runBlocking {
        repeat(3) { index ->
            database.mediaMutationDao().upsertMutation(
                LibraryMutationEntity(
                    mutationId = "mutation-$index",
                    type = "TagEdit",
                    status = MediaMutationStatus.Created.name,
                    songId = index.toLong(),
                    albumId = null,
                    uri = null,
                    displayName = null,
                    createdAtMs = index.toLong(),
                    updatedAtMs = index.toLong(),
                    attemptCount = 0,
                    error = null,
                ),
            )
        }

        val batch = database.mediaMutationDao().recoverableMutationBatch(limit = 1)

        assertEquals(3, batch.totalCount)
        assertEquals(listOf("mutation-0"), batch.mutations.map(LibraryMutationEntity::mutationId))
    }
}
