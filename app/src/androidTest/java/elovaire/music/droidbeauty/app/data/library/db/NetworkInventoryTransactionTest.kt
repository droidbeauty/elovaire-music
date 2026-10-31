package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkInventoryTransactionTest {
    @Test
    fun failedSourceDeletionRollsBackItsInventoryDeletion() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ElovaireDatabase::class.java,
        ).build()
        try {
            val dao = database.networkInventoryDao()
            val source = NetworkInventorySourceEntity("source", 1, 100, "Available", "location")
            val entry = NetworkInventoryEntity(
                sourceId = "source", relativePath = "song.mp3", sizeBytes = 1, modifiedAtMs = null,
                etag = null, contentType = null, sourceEntryId = null, songId = 1, albumId = 1,
                title = "song", artist = "artist", album = "album", albumArtist = null,
                releaseYear = null, genre = "genre", audioFormat = "MP3", audioQuality = null,
                durationMs = 1, trackNumber = 1, discNumber = 1, dateAddedSeconds = 1,
                dateModifiedSeconds = null, metadataResolved = false, artUri = null, lastSeenGeneration = 1,
            )
            dao.replaceNetworkInventory(source, listOf(entry))
            database.openHelper.writableDatabase.execSQL(
                "CREATE TRIGGER reject_source_deletion BEFORE DELETE ON network_inventory_sources " +
                    "BEGIN SELECT RAISE(ABORT, 'test failure'); END",
            )
            assertTrue(runCatching { dao.removeNetworkInventory("source") }.isFailure)
            assertEquals(listOf(entry), dao.networkInventory("source"))
            assertNotNull(dao.networkInventorySource("source"))
        } finally {
            database.close()
        }
    }
}
