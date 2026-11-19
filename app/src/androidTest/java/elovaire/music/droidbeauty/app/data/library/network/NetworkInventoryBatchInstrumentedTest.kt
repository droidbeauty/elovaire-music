package elovaire.music.droidbeauty.app.data.library.network

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.util.Log
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class NetworkInventoryBatchInstrumentedTest {
    @Test
    fun batchAvoidsRepeatedInventoryTransactions() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "network-batch-${UUID.randomUUID()}.db"
        val database = Room.databaseBuilder(context, ElovaireDatabase::class.java, name).build()
        val source = NetworkLibrarySource("batch", "Test", NetworkLibraryProtocol.WebDav,
            "https://example.test", "Music", "user", "credential")
        val dao = database.networkInventoryDao()
        val inventory = NetworkInventoryStore(context, dao)
        val songs = (1L..128L).map { id ->
            val path = "Music/$id.mp3"
            Song(id = id, title = "Track $id", isExplicit = false, artist = "Artist", album = "Album",
                releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null, fileName = "$id.mp3",
                albumId = 1L, durationMs = 1_000L, trackNumber = id.toInt(), discNumber = 1,
                dateAddedSeconds = 0L, uri = NetworkResourceUri.create(source.id, path), artUri = null)
        }
        try {
            inventory.replace(source, songs.map { NetworkInventoryEntry(NetworkFileEntry("Music/${it.id}.mp3", false), it) },
                NetworkAvailability.Available, 1L)
            val fingerprint = NetworkSourceIdentity.locationFingerprint(source)
            val timings = Array(2) { mutableListOf<Double>() }
            repeat(5) { sample ->
                for (batch in if (sample % 2 == 0) listOf(false, true) else listOf(true, false)) {
                    val start = System.nanoTime()
                    if (batch) {
                        inventory.applyVerifiedTags(source, songs)
                    } else {
                        songs.forEach { song ->
                            dao.updateVerifiedNetworkTags(source.id, "Music/${song.id}.mp3", song.id, fingerprint,
                                song.title, song.artist, song.album, song.albumArtist, song.releaseYear, song.genre,
                                song.trackNumber, song.discNumber, song.description)
                        }
                    }
                    timings[if (batch) 1 else 0] += (System.nanoTime() - start) / 1_000_000.0
                }
            }
            assertEquals(songs.map(Song::id).toSet(), inventory.load(source).map { it.song.id }.toSet())
            Log.i("NetworkInventoryBatch", "individualMs=${timings[0]} batchMs=${timings[1]}")
            assertTrue("Batch did not reduce median update time: ${timings.toList()}",
                timings[1].sorted()[2] < timings[0].sorted()[2])
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun batchKeepsIdentityGuardsAndRollsBackOnFailure() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
        val source = NetworkLibrarySource("batch", "Test", NetworkLibraryProtocol.WebDav,
            "https://example.test", "Music", "user", "credential")
        val inventory = NetworkInventoryStore(context, database.networkInventoryDao())
        val songs = (1L..3L).map { id ->
            val path = "Music/$id.mp3"
            Song(id = id, title = "Track $id", isExplicit = false, artist = "Artist", album = "Album",
                releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null, fileName = "$id.mp3",
                albumId = 1L, durationMs = 1_000L, trackNumber = id.toInt(), discNumber = 1,
                dateAddedSeconds = 0L, uri = NetworkResourceUri.create(source.id, path), artUri = null)
        }
        try {
            inventory.replace(source, songs.map { NetworkInventoryEntry(NetworkFileEntry("Music/${it.id}.mp3", false), it) },
                NetworkAvailability.Available, 1L)
            val edited = songs.map { it.copy(title = "Edited ${it.id}") }
            inventory.applyVerifiedTags(source, listOf(edited[0], edited[1].copy(id = 999L), edited[2]))
            assertEquals(listOf("Edited 1", "Track 2", "Edited 3"), inventory.load(source).map { it.song.title })
            inventory.applyVerifiedTags(source.copy(server = "https://other.test"), edited)
            assertEquals(listOf("Edited 1", "Track 2", "Edited 3"), inventory.load(source).map { it.song.title })

            database.openHelper.writableDatabase.execSQL(
                "CREATE TRIGGER reject_test_update BEFORE UPDATE ON network_inventory " +
                    "WHEN NEW.title = 'Reject' BEGIN SELECT RAISE(ABORT, 'test failure'); END",
            )
            val failure = runCatching {
                inventory.applyVerifiedTags(source, listOf(edited[0].copy(title = "Changed"), edited[1].copy(title = "Reject")))
            }.exceptionOrNull()
            assertTrue(failure != null)
            assertEquals(listOf("Edited 1", "Track 2", "Edited 3"), inventory.load(source).map { it.song.title })

            val visited = AtomicInteger()
            val job = launch(Dispatchers.Default) {
                val self = requireNotNull(currentCoroutineContext()[Job])
                val manySongs = object : AbstractList<Song>() {
                    override val size = 512
                    override fun get(index: Int): Song {
                        visited.incrementAndGet()
                        if (index == 32) self.cancel()
                        return songs[0]
                    }
                }
                inventory.applyVerifiedTags(source, manySongs)
            }
            job.join()
            assertTrue(job.isCancelled)
            assertTrue("Cancellation traversed the whole catalog", visited.get() < 512)
            assertEquals(listOf("Edited 1", "Track 2", "Edited 3"), inventory.load(source).map { it.song.title })
        } finally {
            database.close()
        }
    }
}
