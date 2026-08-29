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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
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
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log

@UnstableApi
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Suppress("TooGenericExceptionCaught")
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
    private val released = AtomicBoolean(false)
    private val sessionWriterScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    // Keep the writer alive long enough to drain the final checkpoint during normal release,
    // but cancel it if its owning bridge scope is terminated without calling release.
    private val ownerCompletionHandle: DisposableHandle? =
        scope.coroutineContext[Job]?.invokeOnCompletion { sessionWriterScope.cancel() }
    private val sessionWrites = Channel<PersistedPlaybackSession?>(Channel.CONFLATED)
    private val sessionWriterJob: Job = sessionWriterScope.launch(start = CoroutineStart.LAZY) {
        for (session in sessionWrites) {
            try {
                if (session == null) sessionStore.clear() else sessionStore.save(session)
            } catch (failure: kotlinx.coroutines.CancellationException) {
                throw failure
            } catch (failure: RuntimeException) {
                Log.w(TAG, "Playback session checkpoint failed.", failure)
            }
        }
    }

    fun start() {
        if (released.get()) return
        sessionWriterJob.start()
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
            combine(library.contentState, library.scanState) { content, scan ->
                content.songs to scan.isAuthoritative
            }
                .distinctUntilChanged()
                .collect { (songs, isAuthoritative) ->
                    restoreSessionIfNeeded(songs, isAuthoritative)
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
        if (!released.compareAndSet(false, true)) return
        ownerCompletionHandle?.dispose()
        persistSession()
        if (!sessionWriterJob.isActive) {
            sessionWriterScope.cancel()
            return
        }
        sessionWrites.close()
        sessionWriterJob.invokeOnCompletion { sessionWriterScope.cancel() }
    }

    private suspend fun restoreSessionIfNeeded(
        songs: List<elovaire.music.droidbeauty.app.domain.model.Song>,
        isAuthoritative: Boolean,
    ) {
        if (restorationAttempted || !isAuthoritative) return
        if (playback.hasActiveQueue()) {
            restorationAttempted = true
            persistSession()
            return
        }
        restorationAttempted = true
        val persisted = withContext(Dispatchers.IO) { sessionStore.load() } ?: return
        if (persisted.queueSongIds.isEmpty()) {
            withContext(Dispatchers.IO) { sessionStore.clear() }
            return
        }
        val songsById = songs.associateBy { it.id }
        val restoredQueue = persisted.queueSongIds.mapNotNull(songsById::get)
        if (!isPlaybackSessionFullyResolved(persisted.queueSongIds, songsById.keys)) {
            // The scan is authoritative here: missing entries are no longer merely
            // unresolved remote media. Preserve the valid order, or clear an empty queue,
            // and checkpoint only after the decision has been made.
            if (restoredQueue.isEmpty()) {
                withContext(Dispatchers.IO) { sessionStore.clear() }
                return
            }
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
        if (!restorationAttempted || released.get() && !sessionWriterJob.isActive) return
        val queue = playback.queueState.value
        if (queue.queue.isEmpty()) {
            sessionWrites.trySend(null)
            return
        }
        val transport = playback.transportState.value
        sessionWrites.trySend(
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
        const val TAG = "PlaybackSession"
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
