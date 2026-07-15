package elovaire.music.droidbeauty.app.data.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioDeviceCallback
import android.media.AudioManager
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

internal class PlaybackRuntimeResources(
    context: Context,
    private val audioManager: AudioManager?,
    private val handler: Handler,
    private val volumeObserver: ContentObserver,
    private val audioDeviceCallback: AudioDeviceCallback,
    private val noisyReceiver: BroadcastReceiver,
) {
    private val appContext = context.applicationContext
    private var volumeObserverRegistered = false
    private var audioDeviceCallbackRegistered = false
    private var noisyReceiverRegistered = false
    private var released = false

    fun sync(
        hasQueue: Boolean,
        isPlaying: Boolean,
        playWhenReady: Boolean,
        hasUsbOutputRoute: Boolean,
    ) {
        if (released) return
        val playbackRuntimeActive = hasQueue || isPlaying || playWhenReady
        setVolumeObserverRegistered(playbackRuntimeActive)
        setAudioDeviceCallbackRegistered(playbackRuntimeActive || hasUsbOutputRoute)
        setNoisyReceiverRegistered(isPlaying || playWhenReady)
    }

    fun release() {
        if (released) return
        released = true
        setNoisyReceiverRegistered(false)
        setAudioDeviceCallbackRegistered(false)
        setVolumeObserverRegistered(false)
    }

    private fun setVolumeObserverRegistered(registered: Boolean) {
        if (volumeObserverRegistered == registered) return
        if (registered) {
            val failure = runCatching {
                appContext.contentResolver.registerContentObserver(
                    Settings.System.CONTENT_URI,
                    true,
                    volumeObserver,
                )
            }.exceptionOrNull()
            if (failure != null) {
                Log.w(TAG, "Unable to register the playback volume observer.", failure)
                return
            }
        } else {
            runCatching { appContext.contentResolver.unregisterContentObserver(volumeObserver) }
                .onFailure { Log.w(TAG, "Unable to unregister the playback volume observer.", it) }
        }
        volumeObserverRegistered = registered
    }

    private fun setAudioDeviceCallbackRegistered(registered: Boolean) {
        if (audioDeviceCallbackRegistered == registered) return
        if (registered) {
            val manager = audioManager ?: return
            val failure = runCatching { manager.registerAudioDeviceCallback(audioDeviceCallback, handler) }
                .exceptionOrNull()
            if (failure != null) {
                Log.w(TAG, "Unable to register the playback audio-device callback.", failure)
                return
            }
        } else {
            runCatching { audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback) }
                .onFailure { Log.w(TAG, "Unable to unregister the playback audio-device callback.", it) }
        }
        audioDeviceCallbackRegistered = registered
    }

    private fun setNoisyReceiverRegistered(registered: Boolean) {
        if (noisyReceiverRegistered == registered) return
        if (registered) {
            val failure = runCatching {
                ContextCompat.registerReceiver(
                    appContext,
                    noisyReceiver,
                    IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
                    ContextCompat.RECEIVER_NOT_EXPORTED,
                )
            }.exceptionOrNull()
            if (failure != null) {
                Log.w(TAG, "Unable to register the playback noisy-route receiver.", failure)
                return
            }
        } else {
            runCatching { appContext.unregisterReceiver(noisyReceiver) }
                .onFailure { Log.w(TAG, "Unable to unregister the playback noisy-route receiver.", it) }
        }
        noisyReceiverRegistered = registered
    }

    private companion object {
        const val TAG = "PlaybackResources"
    }
}
