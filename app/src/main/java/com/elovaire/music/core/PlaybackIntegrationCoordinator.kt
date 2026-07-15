package elovaire.music.droidbeauty.app.core

import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffectsController
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PersistedPlaybackSession
import elovaire.music.droidbeauty.app.data.playback.PlaybackSessionStore
import elovaire.music.droidbeauty.app.data.settings.PlaybackIntegrationSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

@UnstableApi
@OptIn(FlowPreview::class)
internal class PlaybackIntegrationCoordinator(
    private val scope: CoroutineScope,
    private val preferences: PlaybackIntegrationSettings,
    private val library: LibraryRepository,
    private val playback: PlaybackManager,
    private val effects: PlaybackEffectsController,
    private val sessionStore: PlaybackSessionStore,
) {
    private var restorationAttempted = false

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
                .collect(playback::setGaplessPlaybackEnabled)
        }
        scope.launch {
            preferences.volumeNormalizationEnabled
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
                .collect { songs ->
                    restoreSessionIfNeeded(songs)
                    playback.refreshQueuedLibraryMetadataIfNeeded(songs)
                }
        }
        scope.launch {
            playback.queueState
                .collect { persistSession() }
        }
        scope.launch {
            playback.transportState
                .collect { persistSession() }
        }
        scope.launch {
            playback.progressState
                .sample(PLAYBACK_POSITION_PERSIST_INTERVAL_MS)
                .collect { persistSession() }
        }
    }

    fun release() {
        persistSession()
    }

    private fun restoreSessionIfNeeded(songs: List<elovaire.music.droidbeauty.app.domain.model.Song>) {
        if (restorationAttempted || songs.isEmpty()) return
        if (playback.hasActiveQueue()) {
            restorationAttempted = true
            persistSession()
            return
        }
        restorationAttempted = true
        val persisted = sessionStore.load() ?: return
        val songsById = songs.associateBy { it.id }
        val restoredQueue = persisted.queueSongIds.mapNotNull(songsById::get)
        if (restoredQueue.isEmpty()) {
            sessionStore.clear()
            return
        }
        val currentIndex = persisted.currentIndex
            .takeIf { it in restoredQueue.indices && restoredQueue[it].id == persisted.currentSongId }
            ?: persisted.currentSongId
                ?.let { id -> restoredQueue.indexOfFirst { it.id == id } }
                ?.takeIf { it >= 0 }
            ?: persisted.currentIndex.coerceIn(restoredQueue.indices)
        playback.restoreSession(restoredQueue, currentIndex, persisted)
    }

    private fun persistSession() {
        if (!restorationAttempted) return
        val queue = playback.queueState.value
        if (queue.queue.isEmpty()) {
            sessionStore.clear()
            return
        }
        val transport = playback.transportState.value
        sessionStore.save(
            PersistedPlaybackSession(
                queueSongIds = queue.queue.map { it.id },
                currentSongId = queue.queue.getOrNull(queue.currentIndex)?.id,
                currentIndex = queue.currentIndex,
                positionMs = playback.progressState.value.positionMs,
                repeatMode = transport.repeatMode,
                shuffleEnabled = transport.shuffleEnabled,
                sourcePlaylistId = queue.sourcePlaylistId,
                wasPlaying = transport.isPlaying || transport.transportShowsPause,
                savedAtWallTimeMs = 0L,
            ),
        )
    }

    private companion object {
        const val PLAYBACK_POSITION_PERSIST_INTERVAL_MS = 5_000L
    }
}
