package elovaire.music.droidbeauty.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLookupMode
import elovaire.music.droidbeauty.app.data.lyrics.LyricsResult
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressState
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

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
)

internal data class MiniPlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val transportShowsPause: Boolean = false,
)

internal class NowPlayingViewModel(
    private val playbackManager: PlaybackManager,
    private val lyricsService: LyricsService,
) : ViewModel() {
    private val lyricsVisible = MutableStateFlow(false)

    val uiState: StateFlow<PlayerUiState> = combine(
        playbackManager.nowPlayingState,
        playbackManager.transportState,
        playbackManager.queueState,
        playbackManager.volumeState,
    ) { nowPlaying, transport, queue, volume ->
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
        playbackManager.queueState,
    ) { visible, nowPlaying, queue ->
        LyricsRequest(
            visible = visible,
            song = nowPlaying.currentSong,
            queue = queue.queue,
            currentIndex = queue.currentIndex,
        )
    }
        .distinctUntilChanged()
        .flatMapLatest { request ->
            flow {
                if (!request.visible || request.song == null) {
                    emit(LyricsUiState.Hidden)
                    return@flow
                }

                lyricsService.cachedLyrics(request.song, includeNotFound = false)?.let { cached ->
                    emit(cached.toUiState())
                    if (cached is LyricsResult.Found && cached.payload.isSynced) {
                        return@flow
                    }
                } ?: emit(LyricsUiState.Loading)

                val fetchedResult = withTimeoutOrNull(LYRICS_LOOKUP_TIMEOUT_MS) {
                    lyricsService.fetchLyrics(
                        song = request.song,
                        allowCachedNotFound = false,
                        lookupMode = LyricsLookupMode.Full,
                    )
                } ?: LyricsResult.Timeout

                emit(
                    when (fetchedResult) {
                        is LyricsResult.Found -> LyricsUiState.Ready(fetchedResult.payload)
                        LyricsResult.Timeout -> {
                            lyricsService.cachedLyrics(request.song, includeNotFound = false)?.toUiState()
                                ?: if (lyricsService.isLookupInFlight(request.song)) LyricsUiState.Loading else LyricsUiState.Empty
                        }

                        LyricsResult.NotFound -> {
                            if (lyricsService.isLookupInFlight(request.song)) LyricsUiState.Loading else LyricsUiState.Empty
                        }
                    },
                )
            }
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
                        listOf(
                            request.currentSong,
                            request.queue.getOrNull(request.currentIndex + 1),
                            request.queue.getOrNull(request.currentIndex - 1),
                        ),
                    )
                    if (!request.visible) return@collect
                    request.currentSong?.let(lyricsService::prefetchLyrics)
                    request.queue.getOrNull(request.currentIndex + 1)?.let(lyricsService::prefetchLyrics)
                    request.queue.getOrNull(request.currentIndex - 1)?.let(lyricsService::prefetchLyrics)
                }
        }
    }

    fun setLyricsVisible(visible: Boolean) {
        lyricsVisible.value = visible
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
        val queue: List<Song>,
        val currentIndex: Int,
    )

    private data class PrefetchRequest(
        val visible: Boolean,
        val currentSong: Song?,
        val queue: List<Song>,
        val currentIndex: Int,
    )

    private companion object {
        const val LYRICS_LOOKUP_TIMEOUT_MS = 4_800L
        const val LYRICS_SWITCH_GRACE_MS = 120L
    }
}
