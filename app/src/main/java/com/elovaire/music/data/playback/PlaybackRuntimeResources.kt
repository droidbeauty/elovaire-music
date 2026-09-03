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
        unregisterNoisyReceiver()
        unregisterAudioDeviceCallback()
        unregisterVolumeObserver()
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
            unregisterVolumeObserver()
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
            unregisterAudioDeviceCallback()
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
            unregisterNoisyReceiver()
        }
        noisyReceiverRegistered = registered
    }

    private fun unregisterVolumeObserver() {
        if (!volumeObserverRegistered && volumeObserverLease == null) return
        runCatching { appContext.contentResolver.unregisterContentObserver(volumeObserver) }
            .onFailure { Log.w(TAG, "Unable to unregister the playback volume observer.", it) }
        volumeObserverLease?.close()
        volumeObserverLease = null
        volumeObserverRegistered = false
    }

    private fun unregisterAudioDeviceCallback() {
        if (!audioDeviceCallbackRegistered && audioDeviceCallbackLease == null) return
        runCatching { audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback) }
            .onFailure { Log.w(TAG, "Unable to unregister the playback audio-device callback.", it) }
        audioDeviceCallbackLease?.close()
        audioDeviceCallbackLease = null
        audioDeviceCallbackRegistered = false
    }

    private fun unregisterNoisyReceiver() {
        if (!noisyReceiverRegistered && noisyReceiverLease == null) return
        runCatching { appContext.unregisterReceiver(noisyReceiver) }
            .onFailure { Log.w(TAG, "Unable to unregister the playback noisy-route receiver.", it) }
        noisyReceiverLease?.close()
        noisyReceiverLease = null
        noisyReceiverRegistered = false
    }

    private companion object {
        const val TAG = "PlaybackResources"
    }
}
