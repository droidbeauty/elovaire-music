package elovaire.music.droidbeauty.app.ui.screens

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import elovaire.music.droidbeauty.app.core.UuidOperationIdGenerator
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLookupMode
import elovaire.music.droidbeauty.app.data.lyrics.EmbeddedLyricsWriteResult
import elovaire.music.droidbeauty.app.data.lyrics.LyricsPayload
import elovaire.music.droidbeauty.app.data.lyrics.LyricsResult
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.lyrics.canonicalEmbeddedLyricsText
import elovaire.music.droidbeauty.app.data.lyrics.toDisplayPayload
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressConsumer
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressState
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.data.playback.PlaybackSleepTimerState
import elovaire.music.droidbeauty.app.data.playback.SleepTimerOption
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal data class LyricsEditorUiState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedRevision: Long = 0L,
)

internal sealed interface LyricsEditorEvent {
    data class RequestWritePermission(
        val operationId: String,
        val mediaUri: Uri,
        val request: android.app.PendingIntent,
    ) : LyricsEditorEvent
}

internal sealed interface LyricsSavePermissionState {
    data object NotRequested : LyricsSavePermissionState
    data class AwaitingResult(val operationId: String, val mediaUri: Uri) : LyricsSavePermissionState
    data class Granted(val operationId: String, val mediaUri: Uri) : LyricsSavePermissionState
    data class Denied(val operationId: String) : LyricsSavePermissionState
}

internal enum class LyricsPermissionResultDecision {
    Granted,
    Denied,
    Stale,
}

internal const val LYRICS_PERMISSION_DENIED_MESSAGE = "Permission to edit this song was denied."
internal const val LYRICS_POST_GRANT_FAILURE_MESSAGE =
    "Android granted access, but the song file still could not be opened for editing."

internal fun resolveLyricsPermissionResult(
    state: LyricsSavePermissionState,
    operationId: String,
    mediaUri: Uri,
    resultCode: Int,
): LyricsPermissionResultDecision {
    val awaiting = state as? LyricsSavePermissionState.AwaitingResult
        ?: return LyricsPermissionResultDecision.Stale
    if (awaiting.operationId != operationId || awaiting.mediaUri.toString() != mediaUri.toString()) {
        return LyricsPermissionResultDecision.Stale
    }
    return if (resultCode == Activity.RESULT_OK) {
        LyricsPermissionResultDecision.Granted
    } else {
        LyricsPermissionResultDecision.Denied
    }
}

internal data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val transportShowsPause: Boolean = false,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val repeatMode: PlaybackRepeatMode = PlaybackRepeatMode.Off,
    val shuffleEnabled: Boolean = false,
    val volume: Float = 1f,
    val sourceLabel: String? = null,
    val gaplessPlaybackEnabled: Boolean = false,
    val sleepTimer: PlaybackSleepTimerState = PlaybackSleepTimerState(),
)

internal data class MiniPlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val transportShowsPause: Boolean = false,
)

internal class NowPlayingViewModel(
    private val playbackManager: PlaybackManager,
    private val preferenceStore: PreferenceStore,
    private val lyricsService: LyricsService,
    private val operationIdGenerator: OperationIdGenerator = UuidOperationIdGenerator,
) : ViewModel() {
    private val lyricsVisible = MutableStateFlow(false)
    private val manualLyricsOverride = MutableStateFlow<ManualLyricsOverride?>(null)
    private val _lyricsEditorUiState = MutableStateFlow(LyricsEditorUiState())
    val lyricsEditorUiState: StateFlow<LyricsEditorUiState> = _lyricsEditorUiState
    private val lyricsEditorEventChannel = Channel<LyricsEditorEvent>(Channel.BUFFERED)
    val lyricsEditorEvents = lyricsEditorEventChannel.receiveAsFlow()
    private var pendingLyricsSave: PendingLyricsSave? = null
    private var lyricsSaveJob: Job? = null

    val uiState: StateFlow<PlayerUiState> = combine(
        combine(
            playbackManager.nowPlayingState,
            playbackManager.transportState,
            playbackManager.queueState,
            playbackManager.volumeState,
            preferenceStore.gaplessPlaybackEnabled,
        ) { nowPlaying, transport, queue, volume, gaplessPlaybackEnabled ->
            PlayerUiState(
                currentSong = nowPlaying.currentSong,
                isPlaying = transport.isPlaying,
                transportShowsPause = transport.transportShowsPause,
                queue = queue.queue,
                currentIndex = queue.currentIndex,
                repeatMode = transport.repeatMode,
                shuffleEnabled = transport.shuffleEnabled,
                volume = volume.volume,
                sourceLabel = nowPlaying.sourceLabel,
                gaplessPlaybackEnabled = gaplessPlaybackEnabled,
            )
        },
        playbackManager.sleepTimerState,
    ) { uiState, sleepTimer ->
        uiState.copy(sleepTimer = sleepTimer)
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PlayerUiState(),
        )

    val miniPlayerUiState: StateFlow<MiniPlayerUiState> = combine(
        playbackManager.nowPlayingState,
        playbackManager.transportState,
    ) { nowPlaying, transport ->
        MiniPlayerUiState(
            currentSong = nowPlaying.currentSong,
            isPlaying = transport.isPlaying,
            transportShowsPause = transport.transportShowsPause,
        )
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MiniPlayerUiState(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val lyricsUiState: StateFlow<LyricsUiState> = combine(
        lyricsVisible,
        playbackManager.nowPlayingState,
        manualLyricsOverride,
        preferenceStore.onlineLyricsLookupEnabled,
    ) { visible, nowPlaying, manualOverride, onlineLyricsLookupEnabled ->
        val song = nowPlaying.currentSong
        LyricsRequest(
            visible = visible,
            song = song,
            onlineLyricsLookupEnabled = onlineLyricsLookupEnabled,
            identityKey = song?.let { "${it.id}:${it.uri}:${it.title}:${it.artist}:${it.durationMs / 1_000L}" },
            manualUiState = manualOverride?.takeIf { it.songId == song?.id }?.uiState,
        )
    }
        .distinctUntilChanged { previous, current ->
            previous.visible == current.visible &&
                previous.identityKey == current.identityKey &&
                previous.onlineLyricsLookupEnabled == current.onlineLyricsLookupEnabled &&
                previous.manualUiState == current.manualUiState
        }
        .flatMapLatest { request ->
            flow {
                if (!request.visible || request.song == null) {
                    logLyrics("hidden visible=${request.visible} song=${request.song?.id}")
                    emit(LyricsUiState.Hidden)
                    return@flow
                }

                request.manualUiState?.let { uiState ->
                    emit(uiState)
                    return@flow
                }

                lyricsService.localLyrics(request.song)?.let { local ->
                    val localState = local.toUiState()
                    emit(localState)
                    logLyrics("local song=${request.song.id} state=${localState::class.simpleName}")
                    return@flow
                }

                lyricsService.cachedLyrics(request.song, includeNotFound = false)?.let { cached ->
                    val cachedState = cached.toUiState()
                    emit(cachedState)
                    logLyrics("cache song=${request.song.id} state=${cachedState::class.simpleName}")
                    if (cached is LyricsResult.Found) {
                        return@flow
                    }
                }

                if (!request.onlineLyricsLookupEnabled) {
                    logLyrics("online disabled song=${request.song.id}")
                    emit(LyricsUiState.Empty)
                    return@flow
                }

                emit(LyricsUiState.Loading)

                lyricsService.lyricsForSong(request.song, LyricsLookupMode.Full).collect { fetchedResult ->
                    val state = fetchedResult.toUiState()
                    logLyrics("remote song=${request.song.id} state=${state::class.simpleName}")
                    emit(state)
                }
            }.flowOn(Dispatchers.IO)
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = LyricsUiState.Hidden,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeLyricsLineIndex: StateFlow<Int> = lyricsUiState
        .flatMapLatest { lyricsState ->
            val payload = (lyricsState as? LyricsUiState.Ready)?.payload
            if (payload == null || !payload.isSynced) {
                flowOf(-1)
            } else {
                playbackManager.progressState.map { progressState ->
                    payload.currentLineIndexAtFast(
                        positionMs = progressState.displayPositionMs,
                        timingOffsetMs = 0L,
                        switchGraceMs = LYRICS_SWITCH_GRACE_MS,
                    ) ?: -1
                }
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = -1,
        )

    init {
        viewModelScope.launch {
            playbackManager.nowPlayingState
                .map { state -> state.currentSong?.let { it.id to it.uri } }
                .distinctUntilChanged()
                .collect { currentSongIdentity ->
                    val pending = pendingLyricsSave ?: return@collect
                    if ((pending.song.id to pending.song.uri) == currentSongIdentity) return@collect
                    if (pending.permissionState is LyricsSavePermissionState.AwaitingResult) return@collect
                    if (lyricsSaveJob?.isActive != true) {
                        pendingLyricsSave = null
                    }
                    _lyricsEditorUiState.value = LyricsEditorUiState(
                        savedRevision = _lyricsEditorUiState.value.savedRevision,
                    )
                }
        }
        viewModelScope.launch {
            combine(
                lyricsVisible,
                playbackManager.nowPlayingState,
                playbackManager.queueState,
            ) { visible, nowPlaying, queue ->
                PrefetchRequest(
                    visible = visible,
                    currentSong = nowPlaying.currentSong,
                    queue = queue.queue,
                    currentIndex = queue.currentIndex,
                )
            }
                .distinctUntilChanged()
                .collectLatest { request ->
                    lyricsService.cancelObsoleteRequests(
                        if (request.visible) {
                            listOf(
                                request.currentSong,
                                request.queue.getOrNull(request.currentIndex + 1),
                                request.queue.getOrNull(request.currentIndex - 1),
                            )
                        } else {
                            emptyList()
                        },
                    )
                    if (!request.visible) return@collectLatest
                    request.currentSong?.let(lyricsService::prefetchLyrics)
                    delay(LYRICS_QUEUE_PREFETCH_STABILITY_DELAY_MS)
                    request.queue.getOrNull(request.currentIndex + 1)?.let(lyricsService::prefetchLyrics)
                    request.queue.getOrNull(request.currentIndex - 1)?.let(lyricsService::prefetchLyrics)
                }
        }
        viewModelScope.launch {
            combine(
                lyricsVisible,
                lyricsUiState,
            ) { visible, state ->
                visible && (state as? LyricsUiState.Ready)?.payload?.isSynced == true
            }
                .distinctUntilChanged()
                .collect { active ->
                    playbackManager.setProgressConsumerActive(PlaybackProgressConsumer.SyncedLyrics, active)
                }
        }
    }

    fun setLyricsVisible(visible: Boolean) {
        logLyrics("visibility=$visible song=${playbackManager.nowPlayingState.value.currentSong?.id}")
        lyricsVisible.value = visible
    }

    fun setProgressConsumerActive(
        consumer: PlaybackProgressConsumer,
        active: Boolean,
    ) {
        playbackManager.setProgressConsumerActive(consumer, active)
    }

    fun requestSaveLyrics(rawLyrics: String) {
        if (_lyricsEditorUiState.value.isSaving || pendingLyricsSave != null) return
        val song = playbackManager.nowPlayingState.value.currentSong ?: return
        val lyrics = rawLyrics.canonicalEmbeddedLyricsText()
        pendingLyricsSave = PendingLyricsSave(
            operationId = operationIdGenerator.nextId(),
            song = song,
            lyrics = lyrics,
        )
        savePendingLyrics()
    }

    fun onLyricsWritePermissionResult(operationId: String, mediaUri: Uri, resultCode: Int) {
        val pending = pendingLyricsSave ?: return
        logLyrics("permission_result operation=$operationId uri=$mediaUri result=$resultCode")
        when (resolveLyricsPermissionResult(pending.permissionState, operationId, mediaUri, resultCode)) {
            LyricsPermissionResultDecision.Stale -> return
            LyricsPermissionResultDecision.Denied -> {
                pendingLyricsSave = null
                _lyricsEditorUiState.value = _lyricsEditorUiState.value.copy(
                    isSaving = false,
                    errorMessage = LYRICS_PERMISSION_DENIED_MESSAGE,
                )
            }
            LyricsPermissionResultDecision.Granted -> {
                pendingLyricsSave = pending.copy(
                    permissionState = LyricsSavePermissionState.Granted(operationId, mediaUri),
                )
                savePendingLyrics()
            }
        }
    }

    fun clearLyricsEditorError() {
        if (_lyricsEditorUiState.value.errorMessage != null) {
            _lyricsEditorUiState.value = _lyricsEditorUiState.value.copy(errorMessage = null)
        }
    }

    private fun savePendingLyrics() {
        val pending = pendingLyricsSave ?: return
        _lyricsEditorUiState.value = _lyricsEditorUiState.value.copy(
            isSaving = true,
            errorMessage = null,
        )
        lyricsSaveJob = viewModelScope.launch {
            val approvedMediaUri = (pending.permissionState as? LyricsSavePermissionState.Granted)?.mediaUri
            val result = lyricsService.saveEmbeddedLyrics(
                song = pending.song,
                lyrics = pending.lyrics,
                operationId = pending.operationId,
                approvedMediaUri = approvedMediaUri,
            )
            if (pendingLyricsSave?.operationId != pending.operationId) return@launch
            when (result) {
                is EmbeddedLyricsWriteResult.Success -> {
                    pendingLyricsSave = null
                    val displayPayload = result.payload.toDisplayPayload()
                    val currentSong = playbackManager.nowPlayingState.value.currentSong
                    if (currentSong?.id == pending.song.id && currentSong.uri == pending.song.uri) {
                        manualLyricsOverride.value = ManualLyricsOverride(
                            songId = pending.song.id,
                            uiState = if (displayPayload.lines.isEmpty()) {
                                LyricsUiState.Empty
                            } else {
                                LyricsUiState.Ready(displayPayload)
                            },
                        )
                    }
                    _lyricsEditorUiState.value = LyricsEditorUiState(
                        savedRevision = _lyricsEditorUiState.value.savedRevision + 1L,
                    )
                }

                is EmbeddedLyricsWriteResult.PermissionRequired -> {
                    if (pending.permissionState is LyricsSavePermissionState.Granted) {
                        pendingLyricsSave = null
                        _lyricsEditorUiState.value = _lyricsEditorUiState.value.copy(
                            isSaving = false,
                            errorMessage = LYRICS_POST_GRANT_FAILURE_MESSAGE,
                        )
                    } else if (pending.permissionState is LyricsSavePermissionState.NotRequested) {
                        logLyrics("permission_request operation=${pending.operationId} uri=${result.mediaUri}")
                        pendingLyricsSave = pending.copy(
                            permissionState = LyricsSavePermissionState.AwaitingResult(
                                operationId = pending.operationId,
                                mediaUri = result.mediaUri,
                            ),
                        )
                        _lyricsEditorUiState.value = _lyricsEditorUiState.value.copy(isSaving = false)
                        lyricsEditorEventChannel.send(
                            LyricsEditorEvent.RequestWritePermission(
                                operationId = pending.operationId,
                                mediaUri = result.mediaUri,
                                request = result.request,
                            ),
                        )
                    }
                }

                is EmbeddedLyricsWriteResult.Failure -> {
                    pendingLyricsSave = null
                    _lyricsEditorUiState.value = _lyricsEditorUiState.value.copy(
                        isSaving = false,
                        errorMessage = result.reason,
                    )
                }
            }
        }
    }

    fun togglePlayback() {
        playbackManager.togglePlayback()
    }

    fun skipPrevious() {
        playbackManager.skipPrevious()
    }

    fun skipNext() {
        playbackManager.skipNext()
    }

    fun cycleRepeatMode() {
        playbackManager.cycleRepeatMode()
    }

    fun toggleShuffle() {
        playbackManager.toggleShuffle()
    }

    fun toggleGaplessPlayback() {
        preferenceStore.setGaplessPlaybackEnabled(!preferenceStore.gaplessPlaybackEnabled.value)
    }

    fun setVolume(volume: Float) {
        playbackManager.setVolume(volume)
    }

    fun setSleepTimer(option: SleepTimerOption) {
        playbackManager.setSleepTimer(option)
    }

    fun playQueueIndex(index: Int) {
        playbackManager.playQueueIndex(index)
    }

    fun removeQueueIndex(index: Int) {
        playbackManager.removeQueueIndex(index)
    }

    fun progressState(): StateFlow<PlaybackProgressState> = playbackManager.progressState

    override fun onCleared() {
        playbackManager.setProgressConsumerActive(PlaybackProgressConsumer.NowPlaying, false)
        playbackManager.setProgressConsumerActive(PlaybackProgressConsumer.CompactDock, false)
        playbackManager.setProgressConsumerActive(PlaybackProgressConsumer.SyncedLyrics, false)
    }

    private data class LyricsRequest(
        val visible: Boolean,
        val song: Song?,
        val onlineLyricsLookupEnabled: Boolean,
        val identityKey: String?,
        val manualUiState: LyricsUiState?,
    )

    private data class ManualLyricsOverride(
        val songId: Long,
        val uiState: LyricsUiState,
    )

    private data class PendingLyricsSave(
        val operationId: String,
        val song: Song,
        val lyrics: String,
        val permissionState: LyricsSavePermissionState = LyricsSavePermissionState.NotRequested,
    )

    private data class PrefetchRequest(
        val visible: Boolean,
        val currentSong: Song?,
        val queue: List<Song>,
        val currentIndex: Int,
    )

    private companion object {
        const val LYRICS_SWITCH_GRACE_MS = 120L
        const val LYRICS_QUEUE_PREFETCH_STABILITY_DELAY_MS = 450L
        const val TAG = "LyricsPipeline"
    }

    private fun logLyrics(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }
}
