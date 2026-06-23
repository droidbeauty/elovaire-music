package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.Player
import elovaire.music.droidbeauty.app.domain.model.Song

internal class PlaybackStateReducer(
    private val playerProvider: () -> Player,
    private val currentDisplayedVolume: () -> Float,
    private val onRecentPlaybackChanged: (
        songIds: List<Long>,
        albumIds: List<Long>,
        lastPlayedCollectionKind: PlaybackCollectionKind?,
        lastPlayedCollectionId: Long?,
    ) -> Unit,
) {
    var lastRecordedSongId: Long? = null
        private set
    var lastKnownQueueIndex: Int = -1
        private set
    var lastKnownPositionMs: Long = 0L
        private set

    fun clearCurrentSongTracking() {
        lastRecordedSongId = null
    }

    fun reduce(
        existingState: PlaybackUiState,
        isPauseTransitioningToStopped: Boolean,
    ): PlaybackUiState {
        val player = playerProvider()
        val currentIndex = resolveCurrentQueueIndex(existingState)
        val currentSong = existingState.queue.getOrNull(currentIndex)
        if (currentIndex >= 0) {
            lastKnownQueueIndex = currentIndex
            lastKnownPositionMs = player.currentPosition.coerceAtLeast(0L)
        }
        val hasNewSong = currentSong != null && currentSong.id != lastRecordedSongId
        val recentSongIds = if (hasNewSong) {
            lastRecordedSongId = currentSong.id
            pushRecentId(currentSong.id, existingState.recentSongIds)
        } else {
            existingState.recentSongIds
        }
        val recentAlbumIds = if (hasNewSong) {
            pushRecentId(currentSong.albumId, existingState.recentAlbumIds)
        } else {
            existingState.recentAlbumIds
        }
        val lastPlayedCollectionKind = if (hasNewSong) {
            if (existingState.sourcePlaylistId != null) PlaybackCollectionKind.Playlist else PlaybackCollectionKind.Album
        } else {
            existingState.lastPlayedCollectionKind
        }
        val lastPlayedCollectionId = if (hasNewSong) {
            existingState.sourcePlaylistId ?: currentSong.albumId
        } else {
            existingState.lastPlayedCollectionId
        }
        if (currentSong == null) {
            lastRecordedSongId = null
        }

        return existingState.copy(
            currentIndex = currentIndex,
            isPlaying = if (isPauseTransitioningToStopped) false else player.isPlaying,
            transportShowsPause = !isPauseTransitioningToStopped &&
                currentSong != null &&
                player.playWhenReady,
            repeatMode = player.repeatMode.toPlaybackRepeatMode(),
            shuffleEnabled = player.shuffleModeEnabled,
            sourceLabel = existingState.sourceLabel ?: currentSong?.album,
            volume = currentDisplayedVolume(),
            audioSessionId = player.audioSessionId.takeIf { it > 0 } ?: 0,
            recentSongIds = recentSongIds,
            recentAlbumIds = recentAlbumIds,
            lastPlayedCollectionKind = lastPlayedCollectionKind,
            lastPlayedCollectionId = lastPlayedCollectionId,
        )
    }

    fun notifyRecentPlaybackChanged(
        updatedState: PlaybackUiState,
        existingState: PlaybackUiState,
    ) {
        if (
            updatedState.recentSongIds != existingState.recentSongIds ||
            updatedState.recentAlbumIds != existingState.recentAlbumIds ||
            updatedState.lastPlayedCollectionKind != existingState.lastPlayedCollectionKind ||
            updatedState.lastPlayedCollectionId != existingState.lastPlayedCollectionId
        ) {
            onRecentPlaybackChanged(
                updatedState.recentSongIds,
                updatedState.recentAlbumIds,
                updatedState.lastPlayedCollectionKind,
                updatedState.lastPlayedCollectionId,
            )
        }
    }

    fun resolveCurrentQueueIndex(existingState: PlaybackUiState): Int {
        val player = playerProvider()
        val playerIndex = player.currentMediaItemIndex.takeIf { it >= 0 }
        if (playerIndex != null) return playerIndex

        val playerMediaId = player.currentMediaItem?.mediaId?.toLongOrNull()
        if (playerMediaId != null) {
            val matchedQueueIndex = existingState.queue.indexOfFirst { it.id == playerMediaId }
            if (matchedQueueIndex >= 0) return matchedQueueIndex
        }

        val fallbackIndex = existingState.currentIndex
        return fallbackIndex.takeIf { it in existingState.queue.indices } ?: -1
    }

    private fun pushRecentId(
        id: Long,
        existing: List<Long>,
    ): List<Long> {
        return buildList {
            add(id)
            existing.asSequence()
                .filter { it != id }
                .take(MAX_HISTORY_ITEMS - 1)
                .forEach(::add)
        }
    }

    private companion object {
        const val MAX_HISTORY_ITEMS = 12
    }
}

internal fun List<Song>.associateQueuedSongsById(queuedSongIds: Set<Long>): Map<Long, Song> {
    if (queuedSongIds.isEmpty()) return emptyMap()
    val remainingIds = queuedSongIds.toMutableSet()
    val songsById = LinkedHashMap<Long, Song>(queuedSongIds.size)
    for (song in this) {
        if (!remainingIds.remove(song.id)) continue
        songsById[song.id] = song
        if (remainingIds.isEmpty()) break
    }
    return songsById
}
