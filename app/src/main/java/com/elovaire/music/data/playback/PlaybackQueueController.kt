package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.Player
import elovaire.music.droidbeauty.app.domain.model.Song

internal interface PlaybackQueueRuntime {
    val player: Player
    val state: PlaybackUiState

    fun publishState(state: PlaybackUiState)
    fun updateState()
    fun requestAudioFocus(): Boolean
    fun effectivePlayerGain(): Float
    fun cancelPauseFade(resetVolume: Boolean = true)
    fun clearInterruptionResumeState()
    fun recordManualPlaybackStart()
    fun stopAndClearQueue()
    fun resetAudioPathState()
    fun resetUnexpectedIdleRecoveryGuard()
    fun onQueueReplaced(songs: List<Song>)
    fun resolveCurrentQueueIndex(state: PlaybackUiState): Int
    fun scheduleAudioPathReevaluation(reason: String, delayMs: Long)
    fun requestFormatFailureReset()
    fun clearFailedPlaybackSongIds()
}

internal class PlaybackQueueController(
    private val runtime: PlaybackQueueRuntime,
    private val queueMetadataRefresher: PlaybackQueueMetadataRefresher,
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
        runtime.clearFailedPlaybackSongIds()
        runtime.requestFormatFailureReset()
        runtime.cancelPauseFade(true)
        runtime.clearInterruptionResumeState()
        runtime.resetAudioPathState()
        runtime.scheduleAudioPathReevaluation("set-queue", audioPathDelayMs)
        runtime.resetUnexpectedIdleRecoveryGuard()

        val player = runtime.player
        player.setMediaItems(songs.mapTo(ArrayList(songs.size), Song::toPlaybackMediaItem), startIndex, 0L)
        player.shuffleModeEnabled = shuffleEnabled
        player.prepare()
        val shouldAutoPlay = runtime.requestAudioFocus()
        if (shouldAutoPlay) {
            player.volume = runtime.effectivePlayerGain()
            player.playWhenReady = true
            player.play()
        } else {
            player.playWhenReady = false
        }
        runtime.publishState(
            runtime.state.copy(
                queue = songs,
                currentIndex = startIndex.coerceIn(songs.indices),
                sourceLabel = sourceLabel,
                transportShowsPause = shouldAutoPlay,
                sourcePlaylistId = sourcePlaylistId,
            ),
        )
        runtime.onQueueReplaced(songs)
        runtime.updateState()
    }

    fun playQueueIndex(index: Int) {
        val state = runtime.state
        if (index !in state.queue.indices) return
        val player = runtime.player
        runtime.cancelPauseFade(true)
        runtime.recordManualPlaybackStart()
        runtime.clearInterruptionResumeState()
        player.seekToDefaultPosition(index)
        if (runtime.requestAudioFocus()) {
            player.volume = runtime.effectivePlayerGain()
            player.playWhenReady = true
            if (!player.isPlaying) {
                player.play()
            }
        }
        runtime.updateState()
    }

    fun enqueueSong(song: Song) {
        val state = runtime.state
        val existingQueue = state.queue
        if (existingQueue.isEmpty() || runtime.player.mediaItemCount == 0) {
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
        val player = runtime.player
        player.addMediaItem(song.toPlaybackMediaItem())
        runtime.publishState(state.copy(queue = existingQueue + song))
        runtime.updateState()
    }

    fun removeQueueIndex(index: Int) {
        val state = runtime.state
        val existingQueue = state.queue
        if (index !in existingQueue.indices) return
        if (existingQueue.size == 1) {
            runtime.stopAndClearQueue()
            return
        }
        val player = runtime.player
        runtime.cancelPauseFade(true)
        runtime.clearInterruptionResumeState()
        val currentIndex = runtime.resolveCurrentQueueIndex(state)
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
        runtime.publishState(
            state.copy(
                queue = updatedQueue,
                currentIndex = fallbackIndex,
                transportShowsPause = shouldKeepPlaying,
            ),
        )
        if (shouldKeepPlaying && runtime.requestAudioFocus()) {
            player.volume = runtime.effectivePlayerGain()
            player.playWhenReady = true
            if (!player.isPlaying) {
                player.play()
            }
        }
        runtime.updateState()
    }

    fun removeSongsFromQueue(songIds: Set<Long>) {
        if (songIds.isEmpty()) return
        val state = runtime.state
        val existingQueue = state.queue
        val indicesToRemove = existingQueue.indices.filter { existingQueue[it].id in songIds }
        if (indicesToRemove.isEmpty()) return
        if (indicesToRemove.size == existingQueue.size) {
            runtime.stopAndClearQueue()
            return
        }
        val player = runtime.player
        runtime.cancelPauseFade(true)
        runtime.clearInterruptionResumeState()
        indicesToRemove.asReversed().forEach(player::removeMediaItem)
        val updatedQueue = existingQueue.filterNot { it.id in songIds }
        runtime.publishState(
            state.copy(
                queue = updatedQueue,
                currentIndex = player.currentMediaItemIndex.coerceIn(0, updatedQueue.lastIndex),
            ),
        )
        runtime.updateState()
    }

    fun refreshQueuedLibraryMetadataIfNeeded(updatedSongs: List<Song>) {
        val state = runtime.state
        if (state.queue.isEmpty() || updatedSongs.isEmpty()) return
        val queuedSongIds = state.queue.asSequence().mapTo(linkedSetOf(), Song::id)
        val songsById = updatedSongs.associateQueuedSongsById(queuedSongIds)
        if (songsById.isEmpty()) return
        val refreshedQueue = queueMetadataRefresher.refreshQueueIfNeeded(state.queue, songsById) ?: return
        val player = runtime.player
        refreshedQueue.forEachIndexed { index, refreshedSong ->
            if (
                state.queue.getOrNull(index)?.playbackMetadataSignature() != refreshedSong.playbackMetadataSignature() &&
                index < player.mediaItemCount
            ) {
                player.replaceMediaItem(index, refreshedSong.toPlaybackMediaItem())
            }
        }
        val currentIndex = runtime.resolveCurrentQueueIndex(state)
        val previousCurrentSong = state.queue.getOrNull(currentIndex)
        val refreshedCurrentSong = refreshedQueue.getOrNull(currentIndex)
        val refreshedSourceLabel = when {
            state.sourcePlaylistId != null -> state.sourceLabel
            state.sourceLabel == previousCurrentSong?.album -> refreshedCurrentSong?.album
            else -> state.sourceLabel
        }
        runtime.publishState(
            state.copy(
                queue = refreshedQueue,
                sourceLabel = refreshedSourceLabel,
            ),
        )
        runtime.updateState()
    }
}
