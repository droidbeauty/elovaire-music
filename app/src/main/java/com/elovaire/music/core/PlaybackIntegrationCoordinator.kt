package elovaire.music.droidbeauty.app.core

import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.data.library.LibraryReader
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffects
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playback.PlaybackIntegrationPort
import elovaire.music.droidbeauty.app.data.playback.PersistedPlaybackSession
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.data.playback.PlaybackSessionPersistence
import elovaire.music.droidbeauty.app.data.settings.PlaybackIntegrationSettings
import elovaire.music.droidbeauty.app.data.settings.PlaybackHistoryStore
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
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
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticRecorder
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics

@UnstableApi
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Suppress("TooGenericExceptionCaught")
internal class PlaybackIntegrationCoordinator(
    private val scope: CoroutineScope,
    private val preferences: PlaybackIntegrationSettings,
    private val history: PlaybackHistoryStore,
    private val library: LibraryReader,
    private val playback: PlaybackIntegrationPort,
    private val effects: PlaybackEffects,
    private val sessionStore: PlaybackSessionPersistence,
    private val diagnostics: BackendDiagnosticRecorder = BackendDiagnostics,
    private val clock: AppClock = AndroidAppClock,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private var restorationState = SessionRestorationState.NotAttempted
    @Volatile
    private var lastDurableSessionKey: SessionKey? = null
    @Volatile
    private var lastSubmittedSessionKey: SessionKey? = null
    private val released = AtomicBoolean(false)
    private val started = AtomicBoolean(false)
    private val sessionWriterScope = CoroutineScope(
        SupervisorJob() +
            ioDispatcher +
            CoroutineName("playback-session-writer") +
            CoroutineExceptionHandler { _, failure ->
                if (failure !is kotlinx.coroutines.CancellationException) {
                    diagnostics.recordWorkerFailure("playback-session-writer", failure)
                }
            },
    )
    private val sessionWrites = Channel<PendingSessionWrite>(Channel.CONFLATED)
    @Volatile
    private var lastSessionWriteFailure: Throwable? = null
    private val sessionDrainResult = CompletableDeferred<PlaybackSessionDrainResult>()
    private val sessionWriterJob: Job = sessionWriterScope.launch(start = CoroutineStart.LAZY) {
        for (pending in sessionWrites) {
            try {
                if (pending.session == null) sessionStore.clear() else sessionStore.save(pending.session)
                lastDurableSessionKey = pending.key
            } catch (failure: kotlinx.coroutines.CancellationException) {
                throw failure
            } catch (failure: RuntimeException) {
                if (lastSubmittedSessionKey == pending.key) lastSubmittedSessionKey = null
                lastSessionWriteFailure = failure
                Log.w(TAG, "Playback session checkpoint failed.", failure)
            }
        }
    }

    init {
        sessionWriterJob.invokeOnCompletion { cause ->
            if (released.get()) {
                val result = when {
                    cause is kotlinx.coroutines.CancellationException -> PlaybackSessionDrainResult.TimedOut(cause)
                    cause != null -> PlaybackSessionDrainResult.Failed(cause)
                    lastSessionWriteFailure != null -> PlaybackSessionDrainResult.Failed(lastSessionWriteFailure)
                    else -> PlaybackSessionDrainResult.Drained
                }
                sessionDrainResult.complete(result)
            }
            sessionWriterScope.cancel()
        }
    }

    fun start() {
        if (released.get() || !started.compareAndSet(false, true)) return
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
                history.recentSongIds,
                history.recentAlbumIds,
                history.lastPlayedCollectionKind,
                history.lastPlayedCollectionId,
            ) { songIds, albumIds, collectionKind, collectionId ->
                PersistedRecentPlayback(songIds, albumIds, collectionKind, collectionId)
            }
                .distinctUntilChanged()
                .collect { recent ->
                    playback.hydrateRecentPlayback(
                        recent.songIds,
                        recent.albumIds,
                        recent.collectionKind,
                        recent.collectionId,
                    )
                }
        }
        scope.launch {
            playback.nowPlayingState
                .map { it.currentSong?.id to it.currentSong?.albumId }
                .distinctUntilChanged()
                .collect { (songId, albumId) -> history.recordPlaybackTransition(songId, albumId) }
        }
        scope.launch {
            combine(library.contentState, library.scanState) { content, scan ->
                content.songs to scan.isAuthoritative
            }
                .distinctUntilChanged()
                .collect { (songs, isAuthoritative) ->
                    restoreSessionIfNeeded(songs, isAuthoritative)
                    playback.refreshQueuedLibraryMetadataIfNeeded(
                        updatedSongs = songs,
                        authoritative = isAuthoritative,
                    )
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
                            delay(PLAYBACK_RECOVERY_CHECKPOINT_INTERVAL_MS)
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

    fun release(): Deferred<PlaybackSessionDrainResult> {
        if (!released.compareAndSet(false, true)) return sessionDrainResult
        persistSessionNow(allowAfterRelease = true)
        sessionWriterJob.start()
        sessionWrites.close()
        sessionWriterScope.launch {
            withTimeoutOrNull(SESSION_DRAIN_TIMEOUT_MS) { sessionWriterJob.join() }
                ?: sessionWriterJob.cancel(PlaybackSessionDrainTimeout())
        }
        return sessionDrainResult
    }

    private suspend fun restoreSessionIfNeeded(
        songs: List<elovaire.music.droidbeauty.app.domain.model.Song>,
        isAuthoritative: Boolean,
    ) {
        if (!isAuthoritative || restorationState == SessionRestorationState.Completed ||
            restorationState == SessionRestorationState.NoSession ||
            restorationState == SessionRestorationState.TerminallyRejected
        ) return
        if (restorationState == SessionRestorationState.InProgress) return
        if (playback.hasActiveQueue()) {
            restorationState = SessionRestorationState.Completed
            persistSession()
            return
        }
        restorationState = SessionRestorationState.InProgress
        try {
            val persisted = withContext(ioDispatcher) { sessionStore.load() }
            if (persisted == null || persisted.queueSongIds.isEmpty()) {
                withContext(ioDispatcher) { sessionStore.clear() }
                restorationState = SessionRestorationState.NoSession
                return
            }
            val requestedSongIds = persisted.queueSongIds.toHashSet()
            val songsById = songs.asSequence()
                .filter { it.id in requestedSongIds }
                .associateBy { it.id }
            val restoredQueue = persisted.queueSongIds.mapNotNull(songsById::get)
            if (!isPlaybackSessionFullyResolved(persisted.queueSongIds, songsById.keys) && restoredQueue.isEmpty()) {
                withContext(ioDispatcher) { sessionStore.clear() }
                restorationState = SessionRestorationState.NoSession
                return
            }
            val currentIndex = persisted.currentIndex
                .takeIf { it in restoredQueue.indices && restoredQueue[it].id == persisted.currentSongId }
                ?: persisted.currentSongId
                    ?.let { id -> restoredQueue.indexOfFirst { it.id == id } }
                    ?.takeIf { it >= 0 }
                ?: persisted.currentIndex.coerceIn(restoredQueue.indices)
            playback.restoreSession(restoredQueue, currentIndex, persisted)
            restorationState = SessionRestorationState.Completed
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            restorationState = SessionRestorationState.RetryableFailure
            throw cancelled
        } catch (failure: RuntimeException) {
            restorationState = SessionRestorationState.RetryableFailure
            Log.w(TAG, "Playback session restoration will be retried.", failure)
        }
    }

    private suspend fun persistSession(
        positionOverrideMs: Long? = null,
        allowAfterRelease: Boolean = false,
    ) {
        val snapshot = withContext(Dispatchers.Main.immediate) {
            playback.capturePersistenceSnapshot()
        }
        enqueueSession(snapshot, positionOverrideMs, allowAfterRelease)
    }

    private fun persistSessionNow(allowAfterRelease: Boolean) {
        enqueueSession(
            snapshot = playback.capturePersistenceSnapshot(),
            positionOverrideMs = null,
            allowAfterRelease = allowAfterRelease,
        )
    }

    private fun enqueueSession(
        snapshot: elovaire.music.droidbeauty.app.data.playback.PlaybackPersistenceSnapshot,
        positionOverrideMs: Long?,
        allowAfterRelease: Boolean,
    ) {
        if (restorationState == SessionRestorationState.NotAttempted ||
            restorationState == SessionRestorationState.InProgress ||
            released.get() && !allowAfterRelease
        ) return
        if (snapshot.queueSongIds.isEmpty()) {
            if (!allowAfterRelease && (lastDurableSessionKey == SessionKey.Empty ||
                    lastSubmittedSessionKey == SessionKey.Empty)
            ) return
            playback.checkpointAudiobookProgress()
            lastSubmittedSessionKey = SessionKey.Empty
            enqueueSessionWrite(SessionKey.Empty, null)
            return
        }
        val sessionKey = SessionKey(
            queueSongIds = snapshot.queueSongIds,
            currentSongId = snapshot.currentSongId,
            currentIndex = snapshot.currentIndex,
            repeatMode = snapshot.repeatMode,
            shuffleEnabled = snapshot.shuffleEnabled,
            sourcePlaylistId = snapshot.sourcePlaylistId,
            wasPlaying = snapshot.wasPlaying,
        )
        if (positionOverrideMs == null && !allowAfterRelease &&
            (sessionKey == lastDurableSessionKey || sessionKey == lastSubmittedSessionKey)
        ) return
        lastSubmittedSessionKey = sessionKey
        playback.checkpointAudiobookProgress()
        enqueueSessionWrite(
            key = sessionKey,
            session = PersistedPlaybackSession(
                queueSongIds = sessionKey.queueSongIds,
                currentSongId = sessionKey.currentSongId,
                currentIndex = sessionKey.currentIndex,
                positionMs = positionOverrideMs ?: snapshot.positionMs,
                repeatMode = sessionKey.repeatMode,
                shuffleEnabled = sessionKey.shuffleEnabled,
                sourcePlaylistId = sessionKey.sourcePlaylistId,
                wasPlaying = sessionKey.wasPlaying,
                savedAtWallTimeMs = clock.wallTimeMs(),
            ),
        )
    }

    private fun enqueueSessionWrite(key: SessionKey, session: PersistedPlaybackSession?) {
        val result = sessionWrites.trySend(PendingSessionWrite(key, session))
        if (result.isFailure) {
            diagnostics.recordWorkerFailure(
                "playback-session-writer",
                IllegalStateException("Playback session checkpoint was rejected after writer shutdown."),
            )
        }
    }

    private companion object {
        const val TAG = "PlaybackSession"
        const val PLAYBACK_POSITION_PERSIST_INTERVAL_MS = 5_000L
        const val PLAYBACK_RECOVERY_CHECKPOINT_INTERVAL_MS = 10_000L
        const val SESSION_DRAIN_TIMEOUT_MS = 15_000L
    }
}

internal sealed interface PlaybackSessionDrainResult {
    data object Drained : PlaybackSessionDrainResult
    data class Failed(val cause: Throwable?) : PlaybackSessionDrainResult
    data class TimedOut(val cause: Throwable?) : PlaybackSessionDrainResult
}

private class PlaybackSessionDrainTimeout : kotlinx.coroutines.CancellationException(
    "Playback session drain timed out.",
)

private data class PendingSessionWrite(
    val key: SessionKey,
    val session: PersistedPlaybackSession?,
)

private enum class SessionRestorationState {
    NotAttempted,
    InProgress,
    Completed,
    NoSession,
    TerminallyRejected,
    RetryableFailure,
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
    return persistedSongIds.all(resolvedSongIds::contains)
}

private data class PersistedRecentPlayback(
    val songIds: List<Long>,
    val albumIds: List<Long>,
    val collectionKind: PlaybackCollectionKind?,
    val collectionId: Long?,
)

private data class SessionKey(
    val queueSongIds: List<Long>,
    val currentSongId: Long?,
    val currentIndex: Int,
    val repeatMode: PlaybackRepeatMode,
    val shuffleEnabled: Boolean,
    val sourcePlaylistId: Long?,
    val wasPlaying: Boolean,
) {
    companion object {
        val Empty = SessionKey(
            queueSongIds = emptyList(),
            currentSongId = null,
            currentIndex = -1,
            repeatMode = PlaybackRepeatMode.Off,
            shuffleEnabled = false,
            sourcePlaylistId = null,
            wasPlaying = false,
        )
    }
}
