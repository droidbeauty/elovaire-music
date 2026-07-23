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
        database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        database.close()
        context.getSharedPreferences(PreferenceStorage.PREFERENCE_FILE_NAME, 0)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun releaseDrainsMandatoryWritesAndRejectsLaterCreates() = runBlocking {
        val store = RoomUserDataStore(context, database.userDataDao(), FixedClock)
        val ids = List(64) { index -> store.createPlaylist("Playlist $index") }
        val drained = CompletableDeferred<Unit>()

        store.release { drained.complete(Unit) }
        withTimeout(10_000L) { drained.await() }

        assertTrue(ids.all { it > 0L })
        assertEquals(ids, database.userDataDao().playlists().map { it.playlistId })
        assertEquals(-1L, store.createPlaylist("After release"))
        assertEquals(64, database.userDataDao().playlists().size)
    }

    private object FixedClock : AppClock {
        override fun wallTimeMs(): Long = 1_000L
        override fun elapsedTimeMs(): Long = 500L
    }
}
