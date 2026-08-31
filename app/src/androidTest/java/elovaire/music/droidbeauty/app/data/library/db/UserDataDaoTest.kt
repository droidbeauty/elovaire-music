package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDataDaoTest {
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
    fun concurrentPlayCountIncrementsAreAtomic() = runBlocking {
        List(40) { async(Dispatchers.IO) { dao.incrementSongPlayCount(7L) } }.awaitAll()

        assertEquals(40, dao.songPlayCounts().single().playCount)
    }

    @Test
    fun removingSongsPreservesPlaylistAndFavoriteOrdering() = runBlocking {
        dao.insertPlaylist(UserPlaylistEntity(1L, "Test", false))
        dao.replacePlaylistEntries(1L, listOf(10L, 20L, 30L))
        listOf(10L, 20L, 30L).forEachIndexed { position, songId ->
            dao.insertFavorite(FavoriteSongEntity(songId, position))
        }

        dao.removeSongReferences(setOf(20L))

        assertEquals(
            listOf(10L to 0, 30L to 1),
            dao.playlistEntries().map { it.songId to it.position },
        )
        assertEquals(
            listOf(10L to 0, 30L to 2),
            dao.favorites().map { it.songId to it.position },
        )
    }

    @Test
    fun playlistBatchImportRollsBackWhenAnyPlaylistInsertFails() = runBlocking {
        val batch = listOf(
            UserPlaylistEntity(1L, "First", false),
            UserPlaylistEntity(1L, "Duplicate", false),
        )

        var failed = false
        try {
            dao.insertPlaylistsWithEntries(batch, emptyList())
        } catch (_: RuntimeException) {
            failed = true
        }

        assertTrue(failed)
        assertTrue(dao.playlists().isEmpty())
    }

    @Test
    fun criticalPlaylistLookupUsesOrderingIndex() = runBlocking {
        dao.insertPlaylist(UserPlaylistEntity(1L, "Test", false))
        dao.replacePlaylistEntries(1L, listOf(10L, 20L, 30L))

        val details = database.query(
            SimpleSQLiteQuery(
                "EXPLAIN QUERY PLAN SELECT songId FROM user_playlist_entries " +
                    "WHERE playlistId = 1 ORDER BY position",
            ),
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(3))
            }
        }

        assertTrue(details.joinToString(), details.any { it.contains("USING", ignoreCase = true) && it.contains("INDEX", ignoreCase = true) })
        assertTrue(details.joinToString(), details.none { it.contains("USE TEMP B-TREE", ignoreCase = true) })
    }

    @Test
    fun maintenanceForeignKeyCheckDetectsPersistedViolation() = runBlocking {
        val writableDatabase = database.openHelper.writableDatabase
        writableDatabase.execSQL("PRAGMA foreign_keys = OFF")
        writableDatabase.execSQL(
            "INSERT INTO user_playlist_entries(playlistId, songId, position) VALUES(99, 10, 0)",
        )
        writableDatabase.execSQL("PRAGMA foreign_keys = ON")

        assertEquals(1, database.persistenceMaintenanceDao().foreignKeyViolationCount())
    }

    @Test
    fun maintenanceDetectsOrderingGapsEvenWhenPositionsAreWithinBounds() = runBlocking {
        dao.insertPlaylist(UserPlaylistEntity(1L, "Gapped", false))
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO user_playlist_entries(playlistId, songId, position) VALUES(1, 10, 0), (1, 20, 2)",
        )
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO favorite_songs(songId, position) VALUES(10, 0), (20, 2)",
        )
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO recent_playback(kind, itemId, position) VALUES('song', 10, 0), ('song', 20, 2)",
        )

        val maintenance = database.persistenceMaintenanceDao()
        assertEquals(1, maintenance.invalidPlaylistEntryCount())
        assertEquals(1, maintenance.invalidFavoritePositionCount())
        assertEquals(1, maintenance.invalidRecentPositionCount())
    }

    @Test
    fun deterministicRepairNormalizesOrderingAndCountersInOneTransaction() = runBlocking {
        dao.insertPlaylist(UserPlaylistEntity(1L, "Repair", false))
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO user_playlist_entries(playlistId, songId, position) VALUES(1, 10, -1), (1, 20, 7)",
        )
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO favorite_songs(songId, position) VALUES(10, -3), (20, 8)",
        )
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO recent_playback(kind, itemId, position) VALUES('song', 10, -2), ('song', 20, 9)",
        )
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO song_play_counts(songId, playCount) VALUES(10, -4)",
        )
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO album_play_counts(albumId, playCount) VALUES(1, -5)",
        )

        dao.repairDeterministicUserData(
            UserDataRepairer.plan(
                invalidPlaylistEntries = true,
                invalidFavorites = true,
                invalidRecentPlayback = true,
                invalidSongPlayCounts = true,
                invalidAlbumPlayCounts = true,
            ),
        )

        assertEquals(listOf(10L to 0, 20L to 1), dao.playlistEntries().map { it.songId to it.position })
        assertEquals(listOf(10L to 0, 20L to 1), dao.favorites().map { it.songId to it.position })
        assertEquals(listOf(10L to 0, 20L to 1), dao.recentPlayback().map { it.itemId to it.position })
        assertEquals(0, dao.songPlayCounts().single().playCount)
        assertEquals(0, dao.albumPlayCounts().single().playCount)
    }
}
