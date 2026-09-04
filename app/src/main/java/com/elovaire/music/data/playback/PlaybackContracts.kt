package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

interface PlaybackReader {
    val nowPlayingState: StateFlow<PlaybackNowPlayingState>
    val transportState: StateFlow<PlaybackTransportState>
    val queueState: StateFlow<PlaybackQueueState>
    val volumeState: StateFlow<PlaybackVolumeState>
    val recentPlaybackState: StateFlow<RecentPlaybackState>
}

interface PlaybackController {
    fun togglePlayback()
    fun skipNext()
    fun skipPrevious()
    fun seekTo(positionMs: Long)
    fun setPlaybackSpeed(speed: Float)
}

/** Commands that can change the shared playback queue. */
interface PlaybackQueueCommands {
    fun playSong(
        song: Song,
        collection: List<Song>,
        sourceLabel: String? = song.album,
        shuffleEnabled: Boolean = false,
        sourcePlaylistId: Long? = null,
    )

    fun playAlbum(
        album: Album,
        startSongId: Long? = null,
        sourceLabel: String? = album.title,
        shuffleEnabled: Boolean = false,
        sourcePlaylistId: Long? = null,
    )

    fun enqueueSong(song: Song)
}

/** The smallest playback surface needed by the player screen and its state holder. */
interface NowPlayingPlayback : PlaybackReader, PlaybackController, PlaybackQueueCommands {
    val progressState: StateFlow<PlaybackProgressState>
    val sleepTimerState: StateFlow<PlaybackSleepTimerState>

    fun setProgressConsumerActive(consumer: PlaybackProgressConsumer, active: Boolean)
    fun cycleRepeatMode()
    fun toggleShuffle()
    fun setVolume(volume: Float)
    fun setSleepTimer(option: SleepTimerOption)
    fun playQueueIndex(index: Int)
    fun removeQueueIndex(index: Int)
    fun beginScrub()
    fun updateScrubPosition(positionMs: Long)
    fun finishScrub(positionMs: Long)
    fun cancelScrub()
    fun playSongAtPosition(
        song: Song,
        collection: List<Song>,
        positionMs: Long,
        sourceLabel: String? = song.album,
        shuffleEnabled: Boolean = false,
        sourcePlaylistId: Long? = null,
    )
    fun audiobookProgress(bookKey: String, songId: Long): Long
    fun audiobookResumeSongId(bookKey: String): Long?
    fun audiobookProgress(bookKey: String): AudiobookProgress?
}
