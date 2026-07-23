package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioFocusRequest
import android.media.AudioManager

internal class PlaybackAudioFocusController(
    private val audioManager: AudioManager?,
    audioAttributes: android.media.AudioAttributes,
    private val onFocusChange: (Int) -> Unit,
) {
    private val callbackFilter = AudioFocusChangeFilter()
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
        callbackFilter.reset()
        hasFocus = audioManager?.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        isActive = hasFocus
        return hasFocus
    }

    fun abandon() {
        if (isActive) audioManager?.abandonAudioFocusRequest(request)
        hasFocus = false
        isActive = false
        callbackFilter.reset()
    }

    private fun handleFocusChange(change: Int) {
        if (!isActive || !callbackFilter.accept(change)) return
        hasFocus = change == AudioManager.AUDIOFOCUS_GAIN
        onFocusChange(change)
    }
}

internal class AudioFocusChangeFilter {
    private var lastChange: Int? = null

    fun accept(change: Int): Boolean {
        if (lastChange == change) return false
        lastChange = change
        return true
    }

    fun reset() {
        lastChange = null
    }
}
