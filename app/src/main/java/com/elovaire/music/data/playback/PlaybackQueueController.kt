package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.Player
import elovaire.music.droidbeauty.app.domain.model.Song

internal class PlaybackQueueController(
    private val playerProvider: () -> Player,
    private val stateProvider: () -> PlaybackUiState,
    private val publishState: (PlaybackUiState) -> Unit,
    private val updateState: () -> Unit,
    private val requestAudioFocus: () -> Boolean,
    private val effectivePlayerGain: () -> Float,
    private val cancelPauseFade: (Boolean) -> Unit,
    private val clearInterruptionResumeState: () -> Unit,
    private val recordManualPlaybackStart: () -> Unit,
    private val stopAndClearQueue: () -> Unit,
    private val resetAudioPathState: () -> Unit,
    private val resetUnexpectedIdleRecoveryGuard: () -> Unit,
    private val onQueueReplaced: (List<Song>) -> Unit,
    private val queueMetadataRefresher: PlaybackQueueMetadataRefresher,
    private val resolveCurrentQueueIndex: (PlaybackUiState) -> Int,
    private val scheduleAudioPathReevaluation: (String, Long) -> Unit,
    private val requestFormatFailureReset: () -> Unit,
    private val clearFailedPlaybackSongIds: () -> Unit,
) {
    fun setQueue(
        songs: List<Song>,
        startIndex: Int,
        sourceLabel: String?,
        shuffleEnabled: Boolean,
        sourcePlaylistId: Long?,
        audioPathDelayMs: Long,
    ) {
        if (songs.isEmpty()) return
        clearFailedPlaybackSongIds()
        requestFormatFailureReset()
        cancelPauseFade(true)
        clearInterruptionResumeState()
        resetAudioPathState()
        scheduleAudioPathReevaluation("set-queue", audioPathDelayMs)
        resetUnexpectedIdleRecoveryGuard()

        val player = playerProvider()
        player.setMediaItems(songs.map(Song::toPlaybackMediaItem), startIndex, 0L)
        player.shuffleModeEnabled = shuffleEnabled
        player.prepare()
        val shouldAutoPlay = requestAudioFocus()
        if (shouldAutoPlay) {
            player.volume = effectivePlayerGain()
            player.playWhenReady = true
            player.play()
        } else {
            player.playWhenReady = false
        }
        publishState(
            stateProvider().copy(
                queue = songs,
                currentIndex = startIndex.coerceIn(songs.indices),
                sourceLabel = sourceLabel,
                transportShowsPause = shouldAutoPlay,
                sourcePlaylistId = sourcePlaylistId,
            ),
        )
        onQueueReplaced(songs)
        updateState()
    }

    fun playQueueIndex(index: Int) {
        val state = stateProvider()
        if (index !in state.queue.indices) return
        val player = playerProvider()
        cancelPauseFade(true)
        recordManualPlaybackStart()
        clearInterruptionResumeState()
        player.seekToDefaultPosition(index)
        if (requestAudioFocus()) {
            player.volume = effectivePlayerGain()
            player.playWhenReady = true
            if (!player.isPlaying) {
                player.play()
            }
        }
        updateState()
    }

    fun enqueueSong(song: Song) {
        val state = stateProvider()
        val existingQueue = state.queue
        if (existingQueue.isEmpty() || playerProvider().mediaItemCount == 0) {
            setQueue(
                songs = listOf(song),
                startIndex = 0,
                sourceLabel = song.album,
                shuffleEnabled = false,
                sourcePlaylistId = null,
                audioPathDelayMs = 80L,
            )
            return
        }
        val player = playerProvider()
        player.addMediaItem(song.toPlaybackMediaItem())
        publishState(state.copy(queue = existingQueue + song))
        updateState()
    }

    fun removeQueueIndex(index: Int) {
        val state = stateProvider()
        val existingQueue = state.queue
        if (index !in existingQueue.indices) return
        if (existingQueue.size == 1) {
            stopAndClearQueue()
            return
        }
        val player = playerProvider()
        cancelPauseFade(true)
        clearInterruptionResumeState()
        val currentIndex = resolveCurrentQueueIndex(state)
            .takeIf { it in existingQueue.indices }
            ?: state.currentIndex
        val shouldKeepPlaying = state.transportShowsPause || player.isPlaying || player.playWhenReady
        val updatedQueue = existingQueue.toMutableList().apply { removeAt(index) }
        player.removeMediaItem(index)
        val fallbackIndex = when {
            index < currentIndex -> currentIndex - 1
            currentIndex >= updatedQueue.size -> updatedQueue.lastIndex
            else -> currentIndex
        }.coerceIn(0, updatedQueue.lastIndex)
        publishState(
            state.copy(
                queue = updatedQueue,
                currentIndex = fallbackIndex,
                transportShowsPause = shouldKeepPlaying,
            ),
        )
        if (shouldKeepPlaying && requestAudioFocus()) {
            player.volume = effectivePlayerGain()
            player.playWhenReady = true
            if (!player.isPlaying) {
                player.play()
            }
        }
        updateState()
    }

    fun removeSongsFromQueue(songIds: Set<Long>) {
        if (songIds.isEmpty()) return
        val state = stateProvider()
        val existingQueue = state.queue
        val indicesToRemove = existingQueue.indices.filter { existingQueue[it].id in songIds }
        if (indicesToRemove.isEmpty()) return
        if (indicesToRemove.size == existingQueue.size) {
            stopAndClearQueue()
            return
        }
        val player = playerProvider()
        cancelPauseFade(true)
        clearInterruptionResumeState()
        indicesToRemove.asReversed().forEach(player::removeMediaItem)
        val updatedQueue = existingQueue.filterNot { it.id in songIds }
        publishState(
            state.copy(
                queue = updatedQueue,
                currentIndex = player.currentMediaItemIndex.coerceIn(0, updatedQueue.lastIndex),
            ),
        )
        updateState()
    }

    fun refreshQueuedLibraryMetadataIfNeeded(updatedSongs: List<Song>) {
        val state = stateProvider()
        if (state.queue.isEmpty() || updatedSongs.isEmpty()) return
        val queuedSongIds = state.queue.asSequence().mapTo(linkedSetOf(), Song::id)
        val songsById = updatedSongs.associateQueuedSongsById(queuedSongIds)
        if (songsById.isEmpty()) return
        val refreshedQueue = queueMetadataRefresher.refreshQueueIfNeeded(state.queue, songsById) ?: return
        val player = playerProvider()
        refreshedQueue.forEachIndexed { index, refreshedSong ->
            if (
                state.queue.getOrNull(index)?.playbackMetadataSignature() != refreshedSong.playbackMetadataSignature() &&
                index < player.mediaItemCount
            ) {
                player.replaceMediaItem(index, refreshedSong.toPlaybackMediaItem())
            }
        }
        val currentIndex = resolveCurrentQueueIndex(state)
        val previousCurrentSong = state.queue.getOrNull(currentIndex)
        val refreshedCurrentSong = refreshedQueue.getOrNull(currentIndex)
        val refreshedSourceLabel = when {
            state.sourcePlaylistId != null -> state.sourceLabel
            state.sourceLabel == previousCurrentSong?.album -> refreshedCurrentSong?.album
            else -> state.sourceLabel
        }
        publishState(
            state.copy(
                queue = refreshedQueue,
                sourceLabel = refreshedSourceLabel,
            ),
        )
        updateState()
    }
}
