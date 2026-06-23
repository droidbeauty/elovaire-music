package elovaire.music.droidbeauty.app.data.playback

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
}
