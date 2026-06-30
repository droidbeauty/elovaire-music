package elovaire.music.droidbeauty.app.data.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SleepTimerOption(
    val durationMs: Long?,
) {
    Off(null),
    FifteenMinutes(15 * 60_000L),
    ThirtyMinutes(30 * 60_000L),
    FortyFiveMinutes(45 * 60_000L),
    SixtyMinutes(60 * 60_000L),
    EndOfSong(null),
}

data class PlaybackSleepTimerState(
    val option: SleepTimerOption = SleepTimerOption.Off,
    val expiresAtElapsedMs: Long? = null,
    val targetSongId: Long? = null,
)

internal class PlaybackSleepTimerController(
    private val scope: CoroutineScope,
    private val elapsedRealtimeMs: () -> Long,
    private val onTimerFired: () -> Unit,
    private val setPauseAtEndOfMediaItems: (Boolean) -> Unit,
) {
    private val _state = MutableStateFlow(PlaybackSleepTimerState())
    val state: StateFlow<PlaybackSleepTimerState> = _state.asStateFlow()
    private var timerJob: Job? = null

    fun setTimer(
        option: SleepTimerOption,
        currentSongId: Long?,
    ) {
        timerJob?.cancel()
        timerJob = null
        when (option) {
            SleepTimerOption.Off -> clear()
            SleepTimerOption.EndOfSong -> {
                setPauseAtEndOfMediaItems(true)
                _state.value = PlaybackSleepTimerState(
                    option = option,
                    targetSongId = currentSongId,
                )
            }
            else -> {
                setPauseAtEndOfMediaItems(false)
                val durationMs = option.durationMs ?: return clear()
                val expiresAt = elapsedRealtimeMs() + durationMs
                _state.value = PlaybackSleepTimerState(
                    option = option,
                    expiresAtElapsedMs = expiresAt,
                )
                timerJob = scope.launch {
                    delay(durationMs)
                    fire()
                }
            }
        }
    }

    fun clear() {
        timerJob?.cancel()
        timerJob = null
        setPauseAtEndOfMediaItems(false)
        _state.value = PlaybackSleepTimerState()
    }

    fun updateEndOfSongTarget(songId: Long?) {
        val current = _state.value
        if (current.option != SleepTimerOption.EndOfSong || current.targetSongId == songId) return
        _state.value = current.copy(targetSongId = songId)
    }

    fun onPlaybackStoppedByUser() {
        clear()
    }

    fun onEndOfSongReached() {
        if (_state.value.option != SleepTimerOption.EndOfSong) return
        fire()
    }

    fun release() {
        timerJob?.cancel()
        timerJob = null
        setPauseAtEndOfMediaItems(false)
    }

    private fun fire() {
        timerJob?.cancel()
        timerJob = null
        setPauseAtEndOfMediaItems(false)
        _state.value = PlaybackSleepTimerState()
        onTimerFired()
    }
}
