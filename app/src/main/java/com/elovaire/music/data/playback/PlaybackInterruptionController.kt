package elovaire.music.droidbeauty.app.data.playback

import kotlinx.coroutines.Job

/** Owns interruption intent/state and the bounded resume-watch jobs. */
internal class PlaybackInterruptionController {
    var duckedForAudioFocus = false
    var isManualPausePending = false
    var shouldResumeAfterTransientFocusLoss = false
    var pausedForAudioFocusLoss = false
    var pendingResumeAfterExternalInterruption = false
    var interruptionResumeState = InterruptionResumeState()
    var externalInterruptionResumeJob: Job? = null
    var pendingAutoResumeRetryJob: Job? = null

    fun release() {
        externalInterruptionResumeJob?.cancel()
        pendingAutoResumeRetryJob?.cancel()
        externalInterruptionResumeJob = null
        pendingAutoResumeRetryJob = null
    }

    fun clearResumeJobs() {
        externalInterruptionResumeJob?.cancel()
        externalInterruptionResumeJob = null
        pendingAutoResumeRetryJob?.cancel()
        pendingAutoResumeRetryJob = null
    }
}
