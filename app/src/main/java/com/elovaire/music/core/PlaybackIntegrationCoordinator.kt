package elovaire.music.droidbeauty.app.core

import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffectsController
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@UnstableApi
@OptIn(FlowPreview::class)
internal class PlaybackIntegrationCoordinator(
    private val scope: CoroutineScope,
    private val preferences: PreferenceStore,
    private val library: LibraryRepository,
    private val playback: PlaybackManager,
    private val effects: PlaybackEffectsController,
) {
    fun start() {
        scope.launch {
            preferences.eqSettings
                .debounce(40L)
                .distinctUntilChanged()
                .collect { settings ->
                    effects.applyEffectSettings(settings)
                    if (playback.hasActiveQueue()) playback.reevaluateAudioOutputPath()
                }
        }
        scope.launch {
            preferences.gaplessPlaybackEnabled
                .distinctUntilChanged()
                .collect(playback::setGaplessPlaybackEnabled)
        }
        scope.launch {
            preferences.volumeNormalizationEnabled
                .distinctUntilChanged()
                .collect(playback::setVolumeNormalizationEnabled)
        }
        scope.launch {
            playback.nowPlayingState
                .map { it.currentSong?.id to it.currentSong?.albumId }
                .distinctUntilChanged()
                .collect { (songId, albumId) -> preferences.recordPlaybackTransition(songId, albumId) }
        }
        scope.launch {
            library.contentState
                .map { it.songs }
                .distinctUntilChanged()
                .collect(playback::refreshQueuedLibraryMetadataIfNeeded)
        }
    }
}
