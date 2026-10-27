package elovaire.music.droidbeauty.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import java.time.LocalDate
import kotlin.random.Random
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyAlbumSelectionTest {
    private val date = LocalDate.of(2026, 10, 27)

    @Test
    fun sameDateKeepsTheChoiceAcrossReorderingAndNewAlbums() = runBlocking {
        val store = MemoryPreferences()
        val chosen = store.albumOfDayId(listOf(1L, 2L, 3L), date, Random(7))
        assertEquals(chosen, store.albumOfDayId(listOf(4L, 3L, 2L, 1L), date, Random(99)))
        // A new owner reading the persisted snapshot also keeps today's choice.
        val restored = MemoryPreferences(store.data.value)
        assertEquals(chosen, restored.albumOfDayId(listOf(1L, 2L, 3L), date, Random(98)))
    }

    @Test
    fun newCalendarDateChoosesAnotherAlbumEvenWithOnlyTwoAlbums() = runBlocking {
        val store = MemoryPreferences()
        var previous: Long? = null
        repeat(10) { day ->
            val chosen = store.albumOfDayId(listOf(1L, 2L), date.plusDays(day.toLong()), Random(1))
            assertNotEquals(previous, chosen)
            previous = chosen
        }
    }

    @Test
    fun emptyLibraryDoesNotEraseTheSavedChoice() = runBlocking {
        val store = MemoryPreferences()
        val chosen = store.albumOfDayId(listOf(1L, 2L), date)
        assertNull(store.albumOfDayId(emptyList(), date))
        assertEquals(chosen, store.albumOfDayId(listOf(1L, 2L), date))
    }

    @Test
    fun deletedChoiceIsReplacedAndSingletonRemainsUsable() = runBlocking {
        val store = MemoryPreferences()
        val chosen = store.albumOfDayId(listOf(1L, 2L, 3L), date)!!
        val remaining = listOf(1L, 2L, 3L).filterNot { it == chosen }
        assertTrue(store.albumOfDayId(remaining, date) in remaining)
        assertEquals(9L, store.albumOfDayId(listOf(9L, 9L), date))
        assertEquals(9L, store.albumOfDayId(listOf(9L), date.plusDays(1)))
    }

    @Test
    fun simultaneousRequestsAgreeOnOneChoice() = runBlocking {
        val store = MemoryPreferences()
        val results = (1..20).map { seed ->
            async { store.albumOfDayId((1L..10L).toList(), date, Random(seed)) }
        }.awaitAll()
        assertEquals(1, results.distinct().size)
    }

    private class MemoryPreferences(initial: Preferences = emptyPreferences()) : DataStore<Preferences> {
        override val data = MutableStateFlow(initial)
        private val mutex = Mutex()

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            mutex.withLock { transform(data.value).also { data.value = it } }
    }
}
