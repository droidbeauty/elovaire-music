package elovaire.music.droidbeauty.app.data.settings

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomUserDataStoreTest {
    private val databaseName = "playlist_reliability_test.db"
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: ElovaireDatabase

    @Before
    fun setUp() {
        assertTrue(
            context.getSharedPreferences(PreferenceStorage.PREFERENCE_FILE_NAME, 0)
                .edit()
                .clear()
                .commit(),
        )
        context.deleteDatabase(databaseName)
        database = Room.databaseBuilder(context, ElovaireDatabase::class.java, databaseName).build()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(databaseName)
        context.getSharedPreferences(PreferenceStorage.PREFERENCE_FILE_NAME, 0)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun releaseDrainsMandatoryWritesAndRejectsLaterCreates() = runBlocking {
        val store = RoomUserDataStore(context, database.userDataDao(), FixedClock)
        val results = List(512) { index -> store.createPlaylist("Playlist $index").await() }
        val ids = results.map { (it as PlaylistMutationResult.Success).playlistId ?: error("missing playlist id") }
        val drained = CompletableDeferred<Unit>()

        store.release { drained.complete(Unit) }
        withTimeout(10_000L) { drained.await() }

        assertTrue(results.all { it is PlaylistMutationResult.Success })
        assertEquals(ids, database.userDataDao().playlists().map { it.playlistId })
        assertTrue(store.createPlaylist("After release").await() is PlaylistMutationResult.Failure)
        assertEquals(512, database.userDataDao().playlists().size)
    }

    @Test
    fun acknowledgedEditsReadBackExactlyAndStoreContinuesAfterFailure() = runBlocking {
        val store = RoomUserDataStore(context, database.userDataDao(), FixedClock)
        val created = store.createPlaylist("Interaction Test").await() as PlaylistMutationResult.Success
        val playlistId = created.playlistId ?: error("missing playlist id")
        val expectedOrder = List(500) { index -> index.toLong() + 1L }

        assertTrue(store.updatePlaylistSongIds(playlistId, expectedOrder + expectedOrder.take(20)).await() is PlaylistMutationResult.Success)
        assertEquals(expectedOrder, database.userDataDao().playlistEntries(playlistId).map { it.songId })
        assertEquals((0 until expectedOrder.size).toList(), database.userDataDao().playlistEntries(playlistId).map { it.position })

        assertTrue(store.updatePlaylistSongIds(999_999L, listOf(1L)).await() is PlaylistMutationResult.NotFound)
        assertTrue(store.renamePlaylist(playlistId, "  Interaction   Test  ").await() is PlaylistMutationResult.Success)
        assertEquals("Interaction Test", database.userDataDao().playlist(playlistId)?.name)

        val drained = CompletableDeferred<Unit>()
        store.release { drained.complete(Unit) }
        withTimeout(10_000L) { drained.await() }

        database.close()
        database = Room.databaseBuilder(context, ElovaireDatabase::class.java, databaseName).build()
        assertEquals(expectedOrder, database.userDataDao().playlistEntries(playlistId).map { it.songId })
        assertEquals("Interaction Test", database.userDataDao().playlist(playlistId)?.name)
    }

    private object FixedClock : AppClock {
        override fun wallTimeMs(): Long = 1_000L
        override fun elapsedTimeMs(): Long = 500L
    }
}
