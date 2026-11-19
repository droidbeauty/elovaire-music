package elovaire.music.droidbeauty.app.data.playback

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.i18n.localizedAlbumTitle
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackSessionPreparationInstrumentedTest {
    @Test
    fun restoredQueuesReconcileAgainstLaterAuthoritativeLibraryUpdates() = runBlocking {
        for (fromLibrary in listOf(false, true)) {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            val manager = withContext(Dispatchers.Main) {
                PlaybackManager(InstrumentationRegistry.getInstrumentation().targetContext, scope)
            }
            try {
                val songs = listOf(song(1L), song(2L))
                withContext(Dispatchers.Main) {
                    if (fromLibrary) {
                        assertTrue(manager.restoreSessionFromLibrary(songs, 0, session(songs[0].id)))
                    } else {
                        manager.restoreSession(songs, 0, session(songs[0].id))
                    }
                    manager.refreshQueuedLibraryMetadataIfNeeded(songs.take(1), authoritative = true)
                    assertEquals(songs.take(1), manager.state.value.queue)
                    assertEquals(1, manager.playerInstance.mediaItemCount)
                }
            } finally {
                withContext(Dispatchers.Main) { manager.release() }
                scope.cancel()
            }
        }
    }

    @Test
    fun userQueueWinsWhileRestoredItemsAreBeingPrepared() = runBlocking {
        val pending = LinkedBlockingQueue<Runnable>()
        val worker = pausedDispatcher(pending)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val manager = withContext(Dispatchers.Main) {
            PlaybackManager(InstrumentationRegistry.getInstrumentation().targetContext, scope, defaultDispatcher = worker)
        }
        val original = song(1L)
        val replacement = song(2L)
        val persisted = session(original.id)
        var work: Runnable? = null
        val result = async(Dispatchers.Main) { manager.restoreSessionFromLibrary(listOf(original), 0, persisted) }
        try {
            work = withContext(Dispatchers.IO) { pending.poll(5, TimeUnit.SECONDS) }
            assertNotNull(work)
            withContext(Dispatchers.Main) { manager.restoreSession(listOf(replacement), 0, session(replacement.id)) }
            withContext(Dispatchers.IO) { work!!.run() }
            work = null
            assertFalse(result.await())
            assertEquals(listOf(replacement), manager.state.value.queue)
        } finally {
            result.cancel()
            withContext(Dispatchers.IO) { (work ?: pending.poll())?.run() }
            withContext(Dispatchers.Main) { manager.release() }
            scope.cancel()
        }
    }

    @Test
    fun languageChangesDuringPreparationUseTheCurrentLanguage() = runBlocking {
        val pending = LinkedBlockingQueue<Runnable>()
        val language = MutableStateFlow(AppLanguage.English)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val manager = withContext(Dispatchers.Main) {
            PlaybackManager(InstrumentationRegistry.getInstrumentation().targetContext, scope,
                appLanguage = language, defaultDispatcher = pausedDispatcher(pending))
        }
        val song = song(1L).copy(album = "Unknown Album")
        var work: Runnable? = null
        val result = async(Dispatchers.Main) { manager.restoreSessionFromLibrary(listOf(song), 0, session(song.id)) }
        try {
            work = withContext(Dispatchers.IO) { pending.poll(5, TimeUnit.SECONDS) }
            assertNotNull(work)
            language.value = AppLanguage.Polish
            withContext(Dispatchers.IO) { work!!.run() }
            work = withContext(Dispatchers.IO) { pending.poll(5, TimeUnit.SECONDS) }
            assertNotNull(work)
            withContext(Dispatchers.IO) { work!!.run() }
            work = null
            assertTrue(result.await())
            val album = withContext(Dispatchers.Main) { manager.playerInstance.currentMediaItem?.mediaMetadata?.albumTitle }
            assertEquals(localizedAlbumTitle("Unknown Album", AppLanguage.Polish), album?.toString())
        } finally {
            result.cancel()
            withContext(Dispatchers.IO) { (work ?: pending.poll())?.run() }
            withContext(Dispatchers.Main) { manager.release() }
            scope.cancel()
        }
    }

    private fun pausedDispatcher(pending: LinkedBlockingQueue<Runnable>) = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) { pending.add(block) }
    }

    private fun session(id: Long) = PersistedPlaybackSession(
        listOf(id), id, 0, 0L, PlaybackRepeatMode.Off, false, null, false, 1L,
    )

    private fun song(id: Long) = Song(
        id = id, title = "Song $id", isExplicit = false, artist = "Artist", album = "Album",
        releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null,
        fileName = "$id.mp3", albumId = 1L, durationMs = 1_000L, trackNumber = id.toInt(),
        discNumber = 1, dateAddedSeconds = 0L, uri = Uri.parse("content://test/$id"), artUri = null,
    )
}
