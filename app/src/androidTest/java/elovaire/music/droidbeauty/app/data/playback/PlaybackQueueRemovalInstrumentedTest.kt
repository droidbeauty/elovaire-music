package elovaire.music.droidbeauty.app.data.playback

import android.net.Uri
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

@RunWith(AndroidJUnit4::class)
class PlaybackQueueRemovalInstrumentedTest {
    @Test
    fun incrementalRefreshUpdatesTheQueuedPlayerMetadata() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val player = ExoPlayer.Builder(instrumentation.targetContext).build()
            try {
                val songs = (1L..3L).map(::song)
                player.setMediaItems(songs.map { MediaItem.fromUri(it.uri) }, 0, 0L)
                val runtime = QueueRuntime(player, PlaybackUiState(queue = songs))
                val controller = PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher())
                controller.stageExternalQueue(songs, 0, "External", null, 80L, libraryBacked = false)
                val updated = songs[1].copy(title = "Updated title")

                runBlocking { controller.refreshQueuedLibraryMetadataIfNeeded(listOf(updated)) }

                assertEquals(updated, runtime.state.queue[1])
                assertEquals("Updated title", player.getMediaItemAt(1).mediaMetadata.title.toString())
            } finally {
                player.release()
            }
        }
    }

    @Test
    fun reconciliationLeavesMainThreadAvailableDuringPreparation() = runBlocking {
        val pending = LinkedBlockingQueue<Runnable>()
        val worker = object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) { pending.add(block) }
        }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val songs = (1L..200L).map(::song)
        val player = withContext(Dispatchers.Main) {
            ExoPlayer.Builder(instrumentation.targetContext).build().apply {
                setMediaItems(songs.map { MediaItem.fromUri(it.uri) }, 0, 0L)
            }
        }
        var job: kotlinx.coroutines.Deferred<Unit>? = null
        var pendingWork: Runnable? = null
        try {
            val controller = withContext(Dispatchers.Main) {
                val runtime = QueueRuntime(player, PlaybackUiState(queue = songs))
                PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher(), defaultDispatcher = worker).also {
                    it.stageExternalQueue(songs, 0, "External", null, 80L, libraryBacked = true)
                }
            }
            for (authoritative in listOf(false, true)) {
                val reconciliation = async(Dispatchers.Main) {
                    controller.refreshQueuedLibraryMetadataIfNeeded(songs, authoritative = authoritative)
                }
                job = reconciliation
                val work = withContext(Dispatchers.IO) { pending.poll(5, TimeUnit.SECONDS) }
                pendingWork = work
                assertNotNull(work)
                withContext(Dispatchers.Main) { assertFalse(reconciliation.isCompleted) }
                withContext(Dispatchers.IO) { work!!.run() }
                pendingWork = null
                reconciliation.await()
            }
        } finally {
            job?.cancel()
            withContext(Dispatchers.IO) { (pendingWork ?: pending.poll())?.run() }
            withContext(Dispatchers.Main) { player.release() }
        }
    }

    @Test
    fun reconciliationHandlesSmallQueueInLargeLibrary() {
        val library = (1L..5_000L).map { id ->
            song(id).copy(libraryPath = if (id <= 50) null else "/storage/emulated/0/Music/$id.mp3")
        }
        val queue = library.take(50)
        val refresher = PlaybackQueueMetadataRefresher()
        val times = mutableListOf<Long>()
        val allocatedBytes = mutableListOf<Long>()
        repeat(5) { sample ->
            val before = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
            val start = SystemClock.elapsedRealtimeNanos()
            assertEquals(null, refresher.reconcileQueue(queue, library))
            val elapsed = SystemClock.elapsedRealtimeNanos() - start
            val allocated = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong() - before
            if (sample > 0) {
                times += elapsed
                allocatedBytes += allocated
            }
        }
        Log.i("QueueReconciliationTest", "timeNs=$times allocatedBytes=$allocatedBytes")
    }

    @Test
    fun libraryReconciliationBatchesRemovedItemsAroundTheCurrentSong() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val player = ExoPlayer.Builder(instrumentation.targetContext).build()
            try {
                val songs = (1L..200L).map(::song)
                player.setMediaItems(songs.map { MediaItem.fromUri(it.uri) }, 99, 0L)
                val runtime = QueueRuntime(player, PlaybackUiState(queue = songs, currentIndex = 99))
                val controller = PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher())
                controller.stageExternalQueue(songs, 99, "External", null, 80L, libraryBacked = true)
                var updates = 0
                player.addListener(object : Player.Listener {
                    override fun onTimelineChanged(timeline: Timeline, reason: Int) { updates++ }
                })
                val retained = songs.subList(90, 110)

                runBlocking { controller.refreshQueuedLibraryMetadataIfNeeded(retained, authoritative = true) }

                assertEquals(2, updates)
                assertEquals(retained, runtime.state.queue)
                assertEquals(9, runtime.state.currentIndex)
                assertEquals(songs[99].uri, player.currentMediaItem?.localConfiguration?.uri)
                assertEquals(retained.map(Song::uri), (0 until player.mediaItemCount).map {
                    player.getMediaItemAt(it).localConfiguration?.uri
                })
            } finally {
                player.release()
            }
        }
    }

    @Test
    fun adjacentRemovalReducesPlayerTimelineUpdates() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val songs = (1L..512L).map(::song)
            val removedIds = (1L..240L).toSet() + (273L..512L).toSet()
            val times = mutableListOf<Long>()
            val timelineUpdates = mutableListOf<Int>()
            for (batched in listOf(false, true)) {
                val player = ExoPlayer.Builder(instrumentation.targetContext).build()
                try {
                    player.setMediaItems(songs.map { MediaItem.fromUri(it.uri) }, 255, 0L)
                    var updates = 0
                    player.addListener(object : Player.Listener {
                        override fun onTimelineChanged(timeline: Timeline, reason: Int) { updates++ }
                    })
                    val runtime = QueueRuntime(player, PlaybackUiState(queue = songs, currentIndex = 255))
                    val start = SystemClock.elapsedRealtimeNanos()
                    if (batched) {
                        PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher()).removeSongsFromQueue(removedIds)
                    } else {
                        for (index in songs.indices.reversed()) {
                            if (songs[index].id in removedIds) player.removeMediaItem(index)
                        }
                    }
                    times += SystemClock.elapsedRealtimeNanos() - start
                    timelineUpdates += updates
                    assertEquals(15, player.currentMediaItemIndex)
                    assertEquals(songs.filterNot { it.id in removedIds }.map(Song::uri),
                        (0 until player.mediaItemCount).map { player.getMediaItemAt(it).localConfiguration?.uri })
                } finally {
                    player.release()
                }
            }
            assertEquals(listOf(480, 2), timelineUpdates)
            Log.i("QueueRemovalTest", "individualNs=${times[0]} batchedNs=${times[1]}")
        }
    }

    @Test
    fun removingRangesKeepsTheCurrentItemWithAndWithoutShuffle() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            for (shuffle in listOf(false, true)) {
                val player = ExoPlayer.Builder(instrumentation.targetContext).build()
                try {
                    val songs = (1L..200L).map(::song)
                    player.setMediaItems(songs.map { MediaItem.fromUri(it.uri) }, 99, 0L)
                    player.shuffleModeEnabled = shuffle
                    val runtime = QueueRuntime(player, PlaybackUiState(queue = songs, currentIndex = 99))
                    val removedIds = (1L..90L).toSet() + (110L..190L).toSet()

                    PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher()).removeSongsFromQueue(removedIds)

                    val remaining = songs.filterNot { it.id in removedIds }
                    assertEquals(remaining, runtime.state.queue)
                    assertEquals(9, player.currentMediaItemIndex)
                    assertEquals(9, runtime.state.currentIndex)
                    assertEquals(songs[99].uri, player.currentMediaItem?.localConfiguration?.uri)
                    assertEquals(remaining.map(Song::uri), (0 until player.mediaItemCount).map {
                        player.getMediaItemAt(it).localConfiguration?.uri
                    })
                } finally {
                    player.release()
                }
            }
        }
    }

    private fun song(id: Long) = Song(
        id = id, title = "Song $id", isExplicit = false, artist = "Artist", album = "Album",
        releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null,
        fileName = "$id.mp3", albumId = 1L, durationMs = 1_000L, trackNumber = id.toInt(),
        discNumber = 1, dateAddedSeconds = 0L, uri = Uri.parse("content://test/$id"), artUri = null,
    )
}

private class QueueRuntime(
    override val player: Player,
    override var state: PlaybackUiState,
) : PlaybackQueueRuntime {
    override fun publishState(state: PlaybackUiState) { this.state = state }
    override fun updateState() = Unit
    override fun requestAudioFocus() = true
    override fun effectivePlayerGain() = 1f
    override fun cancelPauseFade(resetVolume: Boolean) = Unit
    override fun clearInterruptionResumeState() = Unit
    override fun recordManualPlaybackStart() = Unit
    override fun stopAndClearQueue() = Unit
    override fun resetAudioPathState() = Unit
    override fun resetUnexpectedIdleRecoveryGuard() = Unit
    override fun onQueueReplaced(songs: List<Song>) = Unit
    override fun resolveCurrentQueueIndex(state: PlaybackUiState) = state.currentIndex
    override fun scheduleAudioPathReevaluation(reason: String, delayMs: Long) = Unit
    override fun requestFormatFailureReset() = Unit
    override fun clearFailedPlaybackSongIds() = Unit
}
