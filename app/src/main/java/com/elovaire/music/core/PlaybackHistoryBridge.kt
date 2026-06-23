package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class PlaybackHistoryBridge(
    private val scope: CoroutineScope,
    private val preferenceStore: PreferenceStore,
    private val playbackManager: PlaybackManager,
) {
    fun start() {
        scope.launch {
            playbackManager.nowPlayingState
                .map { it.currentSong?.id to it.currentSong?.albumId }
                .distinctUntilChanged()
                .collect { (songId, albumId) ->
                    preferenceStore.recordPlaybackTransition(songId, albumId)
                }
        }
    }
}
