package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomQueryPlanQualificationTest {
    private lateinit var database: ElovaireDatabase
    private lateinit var dao: UserDataDao
    private lateinit var libraryDao: LibraryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ElovaireDatabase::class.java,
        ).build()
        dao = database.userDataDao()
        libraryDao = database.libraryDao()
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

    @Test
    fun fullGenerationRetiresDerivedMediaFilesAtomically() = runBlocking {
        val first = LibraryDatabaseMapper.mediaFileEntity(song(1L), generationId = 1L, scannedAtMs = 1L)
        val second = LibraryDatabaseMapper.mediaFileEntity(song(2L), generationId = 1L, scannedAtMs = 1L)
        libraryDao.replaceGeneration(
            generation = generation(1L),
            songs = emptyList(),
            albums = emptyList(),
            files = listOf(first, second),
            removedAtMs = 1L,
        )

        libraryDao.replaceGeneration(
            generation = generation(2L),
            songs = emptyList(),
            albums = emptyList(),
            files = listOf(first.copy(lastSeenGenerationId = 2L)),
            removedAtMs = 2L,
        )

        database.query(SimpleSQLiteQuery("SELECT stableFileKey FROM media_files ORDER BY stableFileKey")).use { cursor ->
            val keys = buildList {
                while (cursor.moveToNext()) add(cursor.getString(0))
            }
            assertEquals(listOf(first.stableFileKey), keys)
        }
    }

    private fun generation(id: Long) = LibraryScanGenerationEntity(
        generationId = id,
        startedAtMs = id,
        finishedAtMs = id,
        source = "test",
        filterFingerprint = "test",
        status = "Completed",
        error = null,
    )

    private fun song(id: Long) = Song(
        id = id,
        title = "Song $id",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "",
        audioFormat = "WAV",
        audioQuality = null,
        fileName = "song$id.wav",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        uri = Uri.parse("content://media/$id"),
        artUri = null,
    )

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
