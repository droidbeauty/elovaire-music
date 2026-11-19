package elovaire.music.droidbeauty.app.data.playback

import android.net.TestUri
import androidx.media3.common.Player
import elovaire.music.droidbeauty.app.domain.model.Song
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PlaybackQueueControllerTest {
    @Test
    fun queueStartIndexClampsOutOfRangeIndices() {
        val songs = listOf(song(1L), song(2L))

        assertEquals(1, songs.safeStartIndex(Int.MAX_VALUE))
        assertEquals(0, songs.safeStartIndex(Int.MIN_VALUE))
    }

    @Test
    fun stageExternalQueue_doesNotStartPlaybackOrRequestAudioFocus() {
        val runtime = RecordingQueueRuntime(
            PlaybackUiState(
                transportShowsPause = false,
                shuffleEnabled = true,
            ),
        )
        val controller = PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher())
        val songs = listOf(song(1L), song(2L))

        controller.stageExternalQueue(
            songs = songs,
            startIndex = 1,
            sourceLabel = "External",
            sourcePlaylistId = 9L,
            audioPathDelayMs = 80L,
        )

        assertFalse(runtime.audioFocusRequested)
        assertFalse(runtime.playerAccessed)
        assertEquals(songs, runtime.state.queue)
        assertEquals(1, runtime.state.currentIndex)
        assertEquals("External", runtime.state.sourceLabel)
        assertEquals(9L, runtime.state.sourcePlaylistId)
        assertFalse(runtime.state.transportShowsPause)
        assertTrue(runtime.state.shuffleEnabled)
    }

    @Test
    fun queueMetadataRefreshUsesUniquePathWhenProviderReindexesAnItem() {
        val before = song(1L).copy(
            libraryPath = "/music/track.mp3",
            uri = TestUri("content://media/external/audio/media/1"),
        )
        val after = before.copy(
            id = 2L,
            uri = TestUri("content://media/external/audio/media/2"),
        )

        val refreshed = PlaybackQueueMetadataRefresher().refreshQueueIfNeeded(
            queue = listOf(before),
            librarySongsById = emptyMap(),
            librarySongsByPath = mapOf("/music/track.mp3" to after),
        )

        assertEquals(listOf(after), refreshed)
    }

    @Test
    fun queueMetadataRefreshDoesNotTreatHashCollisionsAsUnchanged() {
        val before = song(1L).copy(title = "Aa")
        val after = before.copy(title = "BB")
        assertEquals(before.title.hashCode(), after.title.hashCode())
        val refresher = PlaybackQueueMetadataRefresher()
        refresher.onQueueReplaced(listOf(before))

        val refreshed = refresher.refreshQueueIfNeeded(
            queue = listOf(before),
            librarySongsById = mapOf(after.id to after),
        )

        assertEquals(listOf(after), refreshed)
    }

    @Test
    fun queueRefreshRetainsSharedSongsAfterTheFirstChangedEntry() {
        val before = song(1L)
        val after = before.copy(title = "Edited")
        val unchanged = song(2L)
        val refresher = PlaybackQueueMetadataRefresher()
        refresher.onQueueReplaced(listOf(before, unchanged))

        assertEquals(
            listOf(after, unchanged),
            refresher.refreshQueueIfNeeded(listOf(before, unchanged), mapOf(1L to after, 2L to unchanged)),
        )
        assertEquals(null, refresher.refreshQueueIfNeeded(listOf(after, unchanged), mapOf(1L to after, 2L to unchanged)))
    }

    @Test
    fun incrementalRefreshDoesNotRebindAReusedIdToAnotherSource() {
        val before = song(1L).copy(
            libraryPath = "/music/old.mp3",
            uri = TestUri("file:///music/old.mp3"),
        )
        val replacement = before.copy(
            libraryPath = "/music/new.mp3",
            uri = TestUri("file:///music/new.mp3"),
            title = "Different track",
        )

        val refreshed = PlaybackQueueMetadataRefresher().refreshQueueIfNeeded(
            queue = listOf(before),
            librarySongsById = mapOf(replacement.id to replacement),
        )

        assertEquals(null, refreshed)
    }

    @Test
    fun selectingQueueItemWithoutAudioFocusStopsExistingPlayback() {
        var playWhenReady = true
        val player = Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java),
        ) { _, method, args ->
            when (method.name) {
                "seekToDefaultPosition" -> Unit
                "isPlaying" -> true
                "getPlayWhenReady" -> playWhenReady
                "setPlayWhenReady" -> {
                    playWhenReady = args?.get(0) as Boolean
                    Unit
                }
                else -> null
            }
        } as Player
        val songs = listOf(song(1L), song(2L))
        val runtime = RecordingQueueRuntime(
            PlaybackUiState(
                queue = songs,
                currentIndex = 0,
                transportShowsPause = true,
            ),
        ).apply {
            playerDelegate = player
            audioFocusGranted = false
        }

        PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher()).playQueueIndex(1)

        assertFalse(playWhenReady)
    }

    @Test
    fun removingQueueItemWithoutAudioFocusStopsExistingPlayback() {
        var playWhenReady = true
        val mediaItems = mutableListOf(song(1L), song(2L))
        val player = Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java),
        ) { _, method, args ->
            when (method.name) {
                "getMediaItemCount" -> mediaItems.size
                "getCurrentMediaItemIndex" -> 0
                "removeMediaItem" -> {
                    mediaItems.removeAt(args!![0] as Int)
                    Unit
                }
                "isPlaying" -> playWhenReady
                "getPlayWhenReady" -> playWhenReady
                "setPlayWhenReady" -> {
                    playWhenReady = args?.get(0) as Boolean
                    Unit
                }
                else -> null
            }
        } as Player
        val songs = mediaItems.toList()
        val runtime = RecordingQueueRuntime(
            PlaybackUiState(
                queue = songs,
                currentIndex = 0,
                transportShowsPause = true,
            ),
        ).apply {
            playerDelegate = player
            audioFocusGranted = false
        }

        PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher()).removeQueueIndex(0)

        assertFalse(playWhenReady)
    }

    @Test
    fun removingSongsBatchesAdjacentItemsWithoutChangingTheCurrentSong() {
        val songs = (1L..6L).map(::song)
        val mediaItems = songs.toMutableList()
        val removedRanges = mutableListOf<Pair<Int, Int>>()
        var currentIndex = 2
        val player = Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java),
        ) { _, method, args ->
            when (method.name) {
                "getCurrentMediaItemIndex" -> currentIndex
                "removeMediaItems" -> {
                    val from = args!![0] as Int
                    val to = args[1] as Int
                    removedRanges += from to to
                    mediaItems.subList(from, to).clear()
                    if (to <= currentIndex) currentIndex -= to - from
                    Unit
                }
                "isPlaying", "getPlayWhenReady" -> false
                else -> null
            }
        } as Player
        val runtime = RecordingQueueRuntime(PlaybackUiState(queue = songs, currentIndex = currentIndex))
            .apply { playerDelegate = player }

        PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher())
            .removeSongsFromQueue(setOf(1L, 2L, 4L, 5L))

        assertEquals(listOf(3 to 5, 0 to 2), removedRanges)
        assertEquals(listOf(3L, 6L), mediaItems.map(Song::id))
        assertEquals(mediaItems, runtime.state.queue)
        assertEquals(0, runtime.state.currentIndex)
    }

    @Test
    fun removingCurrentSongRetainsIndividualRemovalOrder() {
        val songs = (1L..4L).map(::song)
        val mediaItems = songs.toMutableList()
        val removedIndices = mutableListOf<Int>()
        val player = Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java),
        ) { _, method, args ->
            when (method.name) {
                "getCurrentMediaItemIndex" -> 1
                "removeMediaItem" -> {
                    val index = args!![0] as Int
                    removedIndices += index
                    mediaItems.removeAt(index)
                    Unit
                }
                "isPlaying", "getPlayWhenReady" -> false
                else -> null
            }
        } as Player
        val runtime = RecordingQueueRuntime(PlaybackUiState(queue = songs, currentIndex = 1))
            .apply { playerDelegate = player }

        PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher())
            .removeSongsFromQueue(setOf(2L, 3L))

        assertEquals(listOf(2, 1), removedIndices)
        assertEquals(listOf(1L, 4L), runtime.state.queue.map(Song::id))
        assertEquals(1, runtime.state.currentIndex)
    }

    @Test
    fun authoritativeReconciliationRemovesMissingItemsAndPreservesStableOrder() {
        val first = song(1L).copy(
            libraryPath = "/music/first.mp3",
            uri = TestUri("content://media/external/audio/media/1"),
        )
        val missing = song(2L).copy(
            libraryPath = "/music/missing.mp3",
            uri = TestUri("content://media/external/audio/media/2"),
        )
        val replacement = first.copy(
            title = "Updated first",
            uri = TestUri("content://media/external/audio/media/3"),
        )
        val result = PlaybackQueueMetadataRefresher().reconcileQueue(
            queue = listOf(first, missing),
            librarySongs = listOf(replacement),
        )

        assertEquals(listOf(replacement), result?.queue)
        assertEquals(listOf(0), result?.retainedOriginalIndices)
        assertEquals(listOf(1), result?.removedOriginalIndices)
    }

    @Test
    fun authoritativeReconciliationSelectsNextSurvivingTrackAfterRemovingCurrent() = runBlocking {
        val queue = (1L..5L).map(::song)
        val mediaItems = queue.toMutableList()
        var seekIndex = -1
        val player = Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java),
        ) { _, method, args ->
            when (method.name) {
                "getMediaItemCount" -> mediaItems.size
                "removeMediaItem" -> {
                    mediaItems.removeAt(args!![0] as Int)
                    Unit
                }
                "seekToDefaultPosition" -> {
                    seekIndex = args!![0] as Int
                    Unit
                }
                "isPlaying" -> false
                "getPlayWhenReady" -> false
                else -> null
            }
        } as Player
        val runtime = RecordingQueueRuntime(
            PlaybackUiState(queue = queue, currentIndex = 2),
        ).apply { playerDelegate = player }
        val controller = PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher())
        controller.stageExternalQueue(
            songs = queue,
            startIndex = 2,
            sourceLabel = "External",
            sourcePlaylistId = null,
            audioPathDelayMs = 80L,
            libraryBacked = true,
        )

        controller.refreshQueuedLibraryMetadataIfNeeded(
            updatedSongs = queue.drop(3),
            authoritative = true,
        )

        assertEquals(0, seekIndex)
        assertEquals(listOf(4L, 5L), runtime.state.queue.map(Song::id))
        assertEquals(0, runtime.state.currentIndex)
    }

    @Test
    fun authoritativeReconciliationBatchesMissingItemsWithinThePlayerQueue() = runBlocking {
        val songs = (1L..6L).map(::song)
        val mediaItems = songs.take(5).toMutableList()
        val removedRanges = mutableListOf<Pair<Int, Int>>()
        var currentIndex = 2
        val player = Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java),
        ) { _, method, args ->
            when (method.name) {
                "getMediaItemCount" -> mediaItems.size
                "getCurrentMediaItemIndex" -> currentIndex
                "removeMediaItems" -> {
                    val from = args!![0] as Int
                    val to = args[1] as Int
                    removedRanges += from to to
                    mediaItems.subList(from, to).clear()
                    if (to <= currentIndex) currentIndex -= to - from
                    Unit
                }
                "isPlaying", "getPlayWhenReady" -> false
                else -> null
            }
        } as Player
        val runtime = RecordingQueueRuntime(PlaybackUiState(queue = songs, currentIndex = currentIndex))
            .apply { playerDelegate = player }
        val controller = PlaybackQueueController(runtime, PlaybackQueueMetadataRefresher())
        controller.stageExternalQueue(songs, 2, "External", null, 80L, libraryBacked = true)

        controller.refreshQueuedLibraryMetadataIfNeeded(listOf(songs[2]), authoritative = true)

        assertEquals(listOf(3 to 5, 0 to 2), removedRanges)
        assertEquals(listOf(3L), mediaItems.map(Song::id))
        assertEquals(mediaItems, runtime.state.queue)
        assertEquals(0, runtime.state.currentIndex)
    }

    @Test
    fun authoritativeReconciliationRetriesWhenQueueChangesDuringPreparation() = runTest {
        val songs = listOf(song(1L), song(2L))
        val runtime = RecordingQueueRuntime(PlaybackUiState(queue = songs))
        val controller = PlaybackQueueController(
            runtime, PlaybackQueueMetadataRefresher(), defaultDispatcher = StandardTestDispatcher(testScheduler),
        )
        controller.stageExternalQueue(songs, 0, "External", null, 80L, libraryBacked = true)
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            controller.refreshQueuedLibraryMetadataIfNeeded(listOf(songs[1]), authoritative = true)
        }
        runtime.publishState(runtime.state.copy(queue = listOf(songs[1])))

        advanceUntilIdle()

        assertTrue(job.isCompleted)
        assertEquals(listOf(songs[1]), runtime.state.queue)
        assertFalse(runtime.playerAccessed)
    }

    @Test
    fun incrementalRefreshRetriesWhenQueueChangesDuringPreparation() = runTest {
        val songs = listOf(song(1L), song(2L))
        val runtime = RecordingQueueRuntime(PlaybackUiState(queue = songs))
        val controller = PlaybackQueueController(
            runtime, PlaybackQueueMetadataRefresher(), defaultDispatcher = StandardTestDispatcher(testScheduler),
        )
        controller.stageExternalQueue(songs, 0, "External", null, 80L, libraryBacked = false)
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            controller.refreshQueuedLibraryMetadataIfNeeded(listOf(songs[0].copy(title = "Updated"), songs[1]))
        }
        runtime.publishState(runtime.state.copy(queue = listOf(songs[1])))

        advanceUntilIdle()

        assertTrue(job.isCompleted)
        assertEquals(listOf(songs[1]), runtime.state.queue)
        assertFalse(runtime.playerAccessed)
    }

    @Test
    fun authoritativeReconciliationDoesNotTouchPlayerAfterRelease() = runTest {
        val songs = listOf(song(1L), song(2L))
        val runtime = RecordingQueueRuntime(PlaybackUiState(queue = songs))
        val controller = PlaybackQueueController(
            runtime, PlaybackQueueMetadataRefresher(), defaultDispatcher = StandardTestDispatcher(testScheduler),
        )
        controller.stageExternalQueue(songs, 0, "External", null, 80L, libraryBacked = true)
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            controller.refreshQueuedLibraryMetadataIfNeeded(listOf(songs[1]), authoritative = true)
        }
        runtime.released = true

        advanceUntilIdle()

        assertTrue(job.isCompleted)
        assertEquals(songs, runtime.state.queue)
        assertFalse(runtime.playerAccessed)
    }

    @Test
    fun authoritativeReconciliationStopsWhenCancelledDuringLibraryScan() {
        val songs = (1L..300L).map(::song)
        var checks = 0
        assertThrows(kotlinx.coroutines.CancellationException::class.java) {
            PlaybackQueueMetadataRefresher().reconcileQueue(songs.take(1), songs) {
                if (++checks == 2) throw kotlinx.coroutines.CancellationException("cancelled")
            }
        }
        assertEquals(2, checks)
    }

    @Test
    fun authoritativeReconciliationDoesNotRebindAmbiguousMetadata() {
        val queued = song(1L).copy(
            title = "Duplicate",
            artist = "Same Artist",
            album = "Same Album",
            uri = TestUri("content://media/external/audio/media/1"),
        )
        val candidateA = queued.copy(id = 2L, uri = TestUri("content://media/external/audio/media/2"))
        val candidateB = queued.copy(id = 3L, uri = TestUri("content://media/external/audio/media/3"))

        val result = PlaybackQueueMetadataRefresher().reconcileQueue(
            queue = listOf(queued),
            librarySongs = listOf(candidateA, candidateB),
        )

        assertEquals(emptyList<Song>(), result?.queue)
        assertEquals(listOf(0), result?.unresolvedOriginalIndices)
    }

    @Test
    fun reconciliationDoesNotRestoreAnIdentityAfterMultipleDuplicates() {
        val queued = song(1L)
        val duplicates = (2L..4L).map { id ->
            song(id).copy(uri = queued.uri, title = "Different $id")
        }
        val result = PlaybackQueueMetadataRefresher().reconcileQueue(
            listOf(queued), duplicates + (100L..110L).map(::song),
        )
        assertEquals(emptyList<Song>(), result?.queue)
        assertEquals(listOf(0), result?.unresolvedOriginalIndices)
    }

    @Test
    fun reconciliationDoesNotRestoreAPathAfterMultipleDuplicates() {
        val queued = song(1L).copy(libraryPath = "/music/shared.mp3")
        val duplicates = (2L..4L).map { id ->
            song(id).copy(libraryPath = queued.libraryPath, title = "Different $id")
        }
        val result = PlaybackQueueMetadataRefresher().reconcileQueue(
            listOf(queued), duplicates + (100L..110L).map(::song),
        )
        assertEquals(emptyList<Song>(), result?.queue)
        assertEquals(listOf(0), result?.unresolvedOriginalIndices)
    }

    private fun song(id: Long) = Song(
        id = id,
        title = "Song $id",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "$id.mp3",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = id.toInt(),
        discNumber = 1,
        dateAddedSeconds = 0L,
        uri = TestUri("content://media/$id"),
        artUri = null,
    )
}

private class RecordingQueueRuntime(
    initialState: PlaybackUiState,
) : PlaybackQueueRuntime {
    override var state: PlaybackUiState = initialState
        private set
    var audioFocusRequested = false
    var playerAccessed = false
    var playerDelegate: Player? = null
    var audioFocusGranted = true
    var released = false
    override val isReleased: Boolean get() = released

    override val player: Player
        get() {
            playerAccessed = true
            return playerDelegate ?: error("Staging an external queue must not access the player")
        }

    override fun publishState(state: PlaybackUiState) {
        this.state = state
    }

    override fun updateState() {
        if (!playerAccessed) error("Staging must wait for MediaSession to publish player state")
    }

    override fun requestAudioFocus(): Boolean {
        audioFocusRequested = true
        return audioFocusGranted
    }

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
