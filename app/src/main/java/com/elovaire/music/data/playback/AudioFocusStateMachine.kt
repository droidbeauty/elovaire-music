package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioManager

internal enum class AudioFocusAction {
    Gain,
    TransientLoss,
    PermanentLoss,
    Ignore,
}

internal object AudioFocusStateMachine {
    fun actionFor(focusChange: Int): AudioFocusAction = when (focusChange) {
        AudioManager.AUDIOFOCUS_GAIN -> AudioFocusAction.Gain
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
        -> AudioFocusAction.TransientLoss
        AudioManager.AUDIOFOCUS_LOSS -> AudioFocusAction.PermanentLoss
        else -> AudioFocusAction.Ignore
    }
}
