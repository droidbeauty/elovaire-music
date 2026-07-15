package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
    fun removingSongsCompactsPlaylistAndFavoriteOrdering() = runBlocking {
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
            listOf(10L to 0, 30L to 1),
            dao.favorites().map { it.songId to it.position },
        )
    }
}
