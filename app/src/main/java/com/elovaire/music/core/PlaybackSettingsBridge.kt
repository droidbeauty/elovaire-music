package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.playback.PlaybackEffectsController
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class PlaybackSettingsBridge(
    private val scope: CoroutineScope,
    private val preferenceStore: PreferenceStore,
    private val playbackManager: PlaybackManager,
    private val playbackEffectsController: PlaybackEffectsController,
) {
    fun start() {
        scope.launch {
            preferenceStore.eqSettings
                .debounce(40L)
                .collect { settings ->
                    playbackEffectsController.applyEffectSettings(settings)
                    if (playbackManager.hasActiveQueue()) {
                        playbackManager.reevaluateAudioOutputPath()
                    }
                }
        }
        scope.launch {
            preferenceStore.gaplessPlaybackEnabled
                .collect(playbackManager::setGaplessPlaybackEnabled)
        }
    }
}
