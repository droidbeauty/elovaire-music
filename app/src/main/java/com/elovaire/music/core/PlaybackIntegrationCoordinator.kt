package elovaire.music.droidbeauty.app.core

import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffectsController
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PersistedPlaybackSession
import elovaire.music.droidbeauty.app.data.playback.PlaybackSessionStore
import elovaire.music.droidbeauty.app.data.settings.PlaybackIntegrationSettings
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class PlaybackIntegrationCoordinator(
    private val scope: CoroutineScope,
    private val preferences: PlaybackIntegrationSettings,
    private val library: LibraryRepository,
    private val playback: PlaybackManager,
    private val effects: PlaybackEffectsController,
    private val sessionStore: PlaybackSessionStore,
    private val clock: AppClock = AndroidAppClock,
) {
    private var restorationAttempted = false
    private var cachedQueue: List<elovaire.music.droidbeauty.app.domain.model.Song>? = null
    private var cachedQueueIds: List<Long> = emptyList()

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
            preferences.crossfadeEnabled
                .collect(playback::setCrossfadeEnabled)
        }
        scope.launch {
            preferences.crossfadeDurationMs
                .collect(playback::setCrossfadeDurationMs)
        }
        scope.launch {
            preferences.crossfadeSilenceThresholdDb
                .collect(playback::setCrossfadeSilenceThresholdDb)
        }
        scope.launch {
            preferences.volumeNormalizationEnabled
                .collect(playback::setVolumeNormalizationEnabled)
        }
        scope.launch {
            combine(
                preferences.recentSongIds,
                preferences.recentAlbumIds,
                preferences.lastPlayedCollectionKind,
                preferences.lastPlayedCollectionId,
            ) { songIds, albumIds, collectionKind, collectionId ->
                PersistedRecentPlayback(songIds, albumIds, collectionKind, collectionId)
            }
                .distinctUntilChanged()
                .collect { recent ->
                    playback.hydrateRecentPlayback(
                        songIds = recent.songIds,
                        albumIds = recent.albumIds,
                        lastPlayedCollectionKind = recent.collectionKind,
                        lastPlayedCollectionId = recent.collectionId,
                    )
                }
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
            val stateChanges = merge(
                combine(playback.queueState, playback.transportState) { _, _ -> Unit },
                combine(playback.progressState, playback.transportState) { _, transport -> transport.isPlaying }
                    .filter { isPlaying -> !isPlaying }
                    .sample(PLAYBACK_POSITION_PERSIST_INTERVAL_MS)
                    .map { Unit },
            )
                .map { PlaybackCheckpoint.StateChange }
            val playingCheckpoints = combine(playback.queueState, playback.transportState) { queue, transport ->
                queue.queue.isNotEmpty() && transport.isPlaying
            }
                .distinctUntilChanged()
                .flatMapLatest { isPlaying ->
                    if (!isPlaying) {
                        emptyFlow()
                    } else {
                        flow {
                            while (true) {
                                emit(PlaybackCheckpoint.RecoveryPosition)
                                delay(PLAYBACK_RECOVERY_CHECKPOINT_INTERVAL_MS)
                            }
                        }
                    }
                }
            merge(stateChanges, playingCheckpoints)
                .collect { checkpoint ->
                    when (checkpoint) {
                        PlaybackCheckpoint.StateChange -> persistSession()
                        PlaybackCheckpoint.RecoveryPosition -> {
                            val positionMs = withContext(Dispatchers.Main.immediate) {
                                playback.currentPositionForPersistence()
                            }
                            persistSession(positionMs)
                        }
                    }
                }
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
        if (!isPlaybackSessionFullyResolved(persisted.queueSongIds, songsById.keys)) {
            // A non-empty library snapshot may still represent only local media while
            // SAF/NAS sources are bootstrapping or temporarily unavailable. Never turn a
            // partial resolution into a destructive queue rewrite; a later authoritative
            // source publication can resolve the remaining IDs.
            restorationAttempted = false
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

    private fun persistSession(positionOverrideMs: Long? = null) {
        if (!restorationAttempted) return
        val queue = playback.queueState.value
        if (queue.queue.isEmpty()) {
            sessionStore.clear()
            return
        }
        val transport = playback.transportState.value
        sessionStore.save(
            PersistedPlaybackSession(
                queueSongIds = queueSongIds(queue.queue),
                currentSongId = queue.queue.getOrNull(queue.currentIndex)?.id,
                currentIndex = queue.currentIndex,
                positionMs = positionOverrideMs ?: playback.progressState.value.positionMs,
                repeatMode = transport.repeatMode,
                shuffleEnabled = transport.shuffleEnabled,
                sourcePlaylistId = queue.sourcePlaylistId,
                wasPlaying = transport.isPlaying || transport.transportShowsPause,
                savedAtWallTimeMs = clock.wallTimeMs(),
            ),
        )
    }

    private fun queueSongIds(queue: List<elovaire.music.droidbeauty.app.domain.model.Song>): List<Long> {
        if (cachedQueue === queue) return cachedQueueIds
        return queue.map { it.id }.also {
            cachedQueue = queue
            cachedQueueIds = it
        }
    }

    private companion object {
        const val PLAYBACK_POSITION_PERSIST_INTERVAL_MS = 5_000L
        const val PLAYBACK_RECOVERY_CHECKPOINT_INTERVAL_MS = 10_000L
    }
}

private enum class PlaybackCheckpoint {
    StateChange,
    RecoveryPosition,
}

internal fun isPlaybackSessionFullyResolved(
    persistedSongIds: List<Long>,
    resolvedSongIds: Collection<Long>,
): Boolean {
    if (persistedSongIds.isEmpty()) return false
    val resolved = resolvedSongIds.toSet()
    return persistedSongIds.all(resolved::contains)
}

private data class PersistedRecentPlayback(
    val songIds: List<Long>,
    val albumIds: List<Long>,
    val collectionKind: PlaybackCollectionKind?,
    val collectionId: Long?,
)
