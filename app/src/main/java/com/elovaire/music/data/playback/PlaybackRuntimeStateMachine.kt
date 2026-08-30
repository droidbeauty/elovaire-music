package elovaire.music.droidbeauty.app.data.playback

/** Serializes player rebuild/recovery transitions on the playback application thread. */
internal class PlaybackRuntimeStateMachine {
    var state: PlaybackRuntimeTransition = PlaybackRuntimeTransition.Idle
        private set

    fun beginRebuild(reason: String): Boolean {
        if (state !is PlaybackRuntimeTransition.Idle) return false
        state = PlaybackRuntimeTransition.Rebuilding(reason)
        return true
    }

    fun beginRecovery(attempt: Int): Boolean {
        if (state !is PlaybackRuntimeTransition.Idle) return false
        state = PlaybackRuntimeTransition.Recovering(attempt)
        return true
    }

    fun complete() {
        if (state !is PlaybackRuntimeTransition.Released) {
            state = PlaybackRuntimeTransition.Idle
        }
    }

    fun release() {
        state = PlaybackRuntimeTransition.Released
    }
}
