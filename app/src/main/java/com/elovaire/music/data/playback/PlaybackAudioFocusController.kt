package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioFocusRequest
import android.media.AudioManager

internal class PlaybackAudioFocusController(
    private val audioManager: AudioManager?,
    audioAttributes: android.media.AudioAttributes,
    private val onFocusChange: (Int) -> Unit,
) {
    private val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(audioAttributes)
        .setOnAudioFocusChangeListener(::handleFocusChange)
        .setAcceptsDelayedFocusGain(false)
        .setWillPauseWhenDucked(false)
        .build()

    var hasFocus: Boolean = false
        private set
    var isActive: Boolean = false
        private set

    fun request(): Boolean {
        if (hasFocus) return true
        if (isActive) return false
        hasFocus = audioManager?.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        isActive = hasFocus
        return hasFocus
    }

    fun abandon() {
        if (isActive) audioManager?.abandonAudioFocusRequest(request)
        hasFocus = false
        isActive = false
    }

    private fun handleFocusChange(change: Int) {
        if (!isActive) return
        hasFocus = change == AudioManager.AUDIOFOCUS_GAIN
        onFocusChange(change)
    }
}
