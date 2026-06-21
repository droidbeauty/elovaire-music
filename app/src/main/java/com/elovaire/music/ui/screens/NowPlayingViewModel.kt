package elovaire.music.droidbeauty.app.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLookupMode
import elovaire.music.droidbeauty.app.data.lyrics.EmbeddedLyricsWriteResult
import elovaire.music.droidbeauty.app.data.lyrics.LyricsPayload
import elovaire.music.droidbeauty.app.data.lyrics.LyricsResult
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressState
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal data class LyricsEditorUiState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedRevision: Long = 0L,
)

internal sealed interface LyricsEditorEvent {
    data class RequestWritePermission(val request: android.app.PendingIntent) : LyricsEditorEvent
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
) : ViewModel() {
    private val lyricsVisible = MutableStateFlow(false)
    private val manualLyricsOverride = MutableStateFlow<ManualLyricsOverride?>(null)
    private val _lyricsEditorUiState = MutableStateFlow(LyricsEditorUiState())
    val lyricsEditorUiState: StateFlow<LyricsEditorUiState> = _lyricsEditorUiState
    private val _lyricsEditorEvents = MutableSharedFlow<LyricsEditorEvent>(extraBufferCapacity = 1)
    val lyricsEditorEvents = _lyricsEditorEvents.asSharedFlow()
    private var pendingLyricsSave: PendingLyricsSave? = null

    val uiState: StateFlow<PlayerUiState> = combine(
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
    ) { visible, nowPlaying, manualOverride ->
        val song = nowPlaying.currentSong
        LyricsRequest(
            visible = visible,
            song = song,
            identityKey = song?.let { "${it.id}:${it.uri}:${it.title}:${it.artist}:${it.durationMs / 1_000L}" },
            manualUiState = manualOverride?.takeIf { it.songId == song?.id }?.uiState,
        )
    }
        .distinctUntilChanged { previous, current ->
            previous.visible == current.visible &&
                previous.identityKey == current.identityKey &&
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

                lyricsService.cachedLyrics(request.song, includeNotFound = false)?.let { cached ->
                    val cachedState = cached.toUiState()
                    emit(cachedState)
                    logLyrics("cache song=${request.song.id} state=${cachedState::class.simpleName}")
                    if (cached is LyricsResult.Found && cached.payload.isSynced) {
                        return@flow
                    }
                } ?: emit(LyricsUiState.Loading)

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

    val activeLyricsLineIndex: StateFlow<Int> = combine(
        lyricsUiState,
        playbackManager.progressState,
    ) { lyricsState, progressState ->
        (lyricsState as? LyricsUiState.Ready)
            ?.payload
            ?.takeIf { it.isSynced }
            ?.currentLineIndexAtFast(
                positionMs = progressState.displayPositionMs,
                timingOffsetMs = 0L,
                switchGraceMs = LYRICS_SWITCH_GRACE_MS,
            )
            ?: -1
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = -1,
        )

    init {
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
                .collect { request ->
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
                    if (!request.visible) return@collect
                    request.currentSong?.let(lyricsService::prefetchLyrics)
                    request.queue.getOrNull(request.currentIndex + 1)?.let(lyricsService::prefetchLyrics)
                    request.queue.getOrNull(request.currentIndex - 1)?.let(lyricsService::prefetchLyrics)
                }
        }
    }

    fun setLyricsVisible(visible: Boolean) {
        logLyrics("visibility=$visible song=${playbackManager.nowPlayingState.value.currentSong?.id}")
        lyricsVisible.value = visible
    }

    fun requestSaveLyrics(rawLyrics: String) {
        if (_lyricsEditorUiState.value.isSaving) return
        val song = playbackManager.nowPlayingState.value.currentSong ?: return
        val lyrics = rawLyrics.trim()
        pendingLyricsSave = PendingLyricsSave(song, lyrics)
        lyricsService.createLyricsWritePermissionRequest(song)?.let { request ->
            _lyricsEditorUiState.value = _lyricsEditorUiState.value.copy(
                isSaving = true,
                errorMessage = null,
            )
            _lyricsEditorEvents.tryEmit(LyricsEditorEvent.RequestWritePermission(request))
            return
        }
        savePendingLyrics()
    }

    fun onLyricsWritePermissionResult(granted: Boolean) {
        if (!granted) {
            pendingLyricsSave = null
            _lyricsEditorUiState.value = _lyricsEditorUiState.value.copy(isSaving = false)
            return
        }
        savePendingLyrics()
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
        viewModelScope.launch {
            when (val result = lyricsService.saveEmbeddedLyrics(pending.song, pending.lyrics)) {
                is EmbeddedLyricsWriteResult.Success -> {
                    pendingLyricsSave = null
                    manualLyricsOverride.value = ManualLyricsOverride(
                        songId = pending.song.id,
                        uiState = if (result.payload.lines.isEmpty()) {
                            LyricsUiState.Empty
                        } else {
                            LyricsUiState.Ready(result.payload)
                        },
                    )
                    _lyricsEditorUiState.value = LyricsEditorUiState(
                        savedRevision = _lyricsEditorUiState.value.savedRevision + 1L,
                    )
                }

                is EmbeddedLyricsWriteResult.PermissionRequired -> {
                    _lyricsEditorUiState.value = _lyricsEditorUiState.value.copy(isSaving = false)
                    _lyricsEditorEvents.emit(LyricsEditorEvent.RequestWritePermission(result.request))
                }

                is EmbeddedLyricsWriteResult.Failure -> {
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

    fun playQueueIndex(index: Int) {
        playbackManager.playQueueIndex(index)
    }

    fun removeQueueIndex(index: Int) {
        playbackManager.removeQueueIndex(index)
    }

    fun progressState(): StateFlow<PlaybackProgressState> = playbackManager.progressState

    private data class LyricsRequest(
        val visible: Boolean,
        val song: Song?,
        val identityKey: String?,
        val manualUiState: LyricsUiState?,
    )

    private data class ManualLyricsOverride(
        val songId: Long,
        val uiState: LyricsUiState,
    )

    private data class PendingLyricsSave(
        val song: Song,
        val lyrics: String,
    )

    private data class PrefetchRequest(
        val visible: Boolean,
        val currentSong: Song?,
        val queue: List<Song>,
        val currentIndex: Int,
    )

    private companion object {
        const val LYRICS_SWITCH_GRACE_MS = 120L
        const val TAG = "LyricsPipeline"
    }

    private fun logLyrics(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }
}
