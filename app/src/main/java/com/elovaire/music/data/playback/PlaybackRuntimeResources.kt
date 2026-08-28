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
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import java.io.Closeable

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
    private var volumeObserverLease: Closeable? = null
    private var audioDeviceCallbackLease: Closeable? = null
    private var noisyReceiverLease: Closeable? = null
    private var released = false

    fun sync(
        hasQueue: Boolean,
        isPlaying: Boolean,
        playWhenReady: Boolean,
    ) {
        if (released) return
        val playbackRuntimeActive = hasQueue || isPlaying || playWhenReady
        val outputRouteActive = isPlaying || playWhenReady
        setVolumeObserverRegistered(playbackRuntimeActive)
        setAudioDeviceCallbackRegistered(outputRouteActive)
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
            volumeObserverLease = BackendResourceRegistry.acquire(BackendResourceKind.ActiveObserver)
        } else {
            val failure = runCatching { appContext.contentResolver.unregisterContentObserver(volumeObserver) }
                .exceptionOrNull()
            if (failure != null) {
                Log.w(TAG, "Unable to unregister the playback volume observer.", failure)
                return
            }
            volumeObserverLease?.close()
            volumeObserverLease = null
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
            audioDeviceCallbackLease = BackendResourceRegistry.acquire(BackendResourceKind.ActiveObserver)
        } else {
            val failure = runCatching { audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback) }
                .exceptionOrNull()
            if (failure != null) {
                Log.w(TAG, "Unable to unregister the playback audio-device callback.", failure)
                return
            }
            audioDeviceCallbackLease?.close()
            audioDeviceCallbackLease = null
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
            noisyReceiverLease = BackendResourceRegistry.acquire(BackendResourceKind.ActiveObserver)
        } else {
            val failure = runCatching { appContext.unregisterReceiver(noisyReceiver) }
                .exceptionOrNull()
            if (failure != null) {
                Log.w(TAG, "Unable to unregister the playback noisy-route receiver.", failure)
                return
            }
            noisyReceiverLease?.close()
            noisyReceiverLease = null
        }
        noisyReceiverRegistered = registered
    }

    private companion object {
        const val TAG = "PlaybackResources"
    }
}
