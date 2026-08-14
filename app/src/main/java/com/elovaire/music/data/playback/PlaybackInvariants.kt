package elovaire.music.droidbeauty.app.data.playback

internal fun playbackInvariantViolations(state: PlaybackUiState): List<String> {
    val violations = buildList {
        if (state.queue.isEmpty() && state.currentIndex != -1) {
            add("empty queue must not have a current index")
        }
        if (state.queue.isNotEmpty() && state.currentIndex !in state.queue.indices) {
            add("non-empty queue must have a valid current index")
        }
        if (state.isPlaying && state.currentSong == null) {
            add("playing state must have a current song")
        }
    }
    return violations
}

internal fun assertPlaybackInvariants(state: PlaybackUiState) {
    val violations = playbackInvariantViolations(state)
    check(violations.isEmpty()) { violations.joinToString(separator = "; ") }
}

