package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomQueryPlanQualificationTest {
    private lateinit var database: ElovaireDatabase
    private lateinit var dao: UserDataDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ElovaireDatabase::class.java,
        ).build()
        dao = database.userDataDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun hotLookupPlansUseOrderingIndexesWithoutTemporarySorts() = runBlocking {
        dao.insertPlaylist(UserPlaylistEntity(1L, "Qualification", false))
        dao.replacePlaylistEntries(1L, (1L..256L).toList())

        assertIndexed(
            "SELECT songId FROM user_playlist_entries WHERE playlistId = 1 ORDER BY position",
        )
        assertIndexed(
            "SELECT songId FROM user_playlist_entries WHERE playlistId = 1 ORDER BY position LIMIT 20",
        )
    }

    @Test
    fun networkInventorySourceGenerationLookupHasCompositeIndex() {
        assertIndexed(
            "SELECT relativePath FROM network_inventory " +
                "WHERE sourceId = 'qualification' AND lastSeenGeneration = 1",
        )
    }

    private fun assertIndexed(sql: String) {
        val details = database.query(SimpleSQLiteQuery("EXPLAIN QUERY PLAN $sql")).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(3))
            }
        }
        val description = details.joinToString()
        assertTrue(description, details.any { it.contains("USING", ignoreCase = true) && it.contains("INDEX", ignoreCase = true) })
        assertTrue(description, details.none { it.contains("USE TEMP B-TREE", ignoreCase = true) })
    }
}
