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

internal data class PlaybackEngineInvariantSnapshot(
    val observerBindingCount: Int,
    val authoritativePlayerBound: Boolean,
    val queueSize: Int,
    val currentIndex: Int,
    val playerMediaItemCount: Int,
    val playerCurrentMediaId: Long?,
    val logicalCurrentSongId: Long?,
    val crossfadeState: CrossfadeState,
    val crossfadeHasSecondary: Boolean,
    val released: Boolean,
)

internal fun playbackEngineInvariantViolations(snapshot: PlaybackEngineInvariantSnapshot): List<String> {
    return buildList {
        if (!snapshot.released && (snapshot.observerBindingCount != 1 || !snapshot.authoritativePlayerBound)) {
            add("authoritative player must own exactly one observer binding")
        }
        if (snapshot.queueSize == 0 && snapshot.currentIndex != -1) {
            add("engine queue is empty but current index is set")
        }
        if (snapshot.queueSize > 0 && snapshot.currentIndex !in 0 until snapshot.queueSize) {
            add("engine queue has no valid current index")
        }
        if (snapshot.playerMediaItemCount == 0 && snapshot.queueSize > 0) {
            add("logical queue is non-empty but player has no media items")
        }
        if (
            snapshot.playerCurrentMediaId != null &&
            snapshot.logicalCurrentSongId != null &&
            snapshot.playerCurrentMediaId != snapshot.logicalCurrentSongId
        ) {
            add("authoritative player and logical current song disagree")
        }
        if (
            snapshot.crossfadeState in setOf(
                CrossfadeState.PreparingNext,
                CrossfadeState.Ready,
                CrossfadeState.Fading,
                CrossfadeState.PromotingNext,
            ) && !snapshot.crossfadeHasSecondary
        ) {
            add("active crossfade state must have a secondary player")
        }
    }
}

internal fun assertPlaybackEngineInvariants(snapshot: PlaybackEngineInvariantSnapshot) {
    val violations = playbackEngineInvariantViolations(snapshot)
    check(violations.isEmpty()) { violations.joinToString(separator = "; ") }
}
