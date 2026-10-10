package elovaire.music.droidbeauty.app.data.playback

import android.net.TestUri
import androidx.media3.common.Player
import elovaire.music.droidbeauty.app.domain.model.Song
import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueControllerTest {
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
    fun authoritativeReconciliationSelectsNextSurvivingTrackAfterRemovingCurrent() {
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
