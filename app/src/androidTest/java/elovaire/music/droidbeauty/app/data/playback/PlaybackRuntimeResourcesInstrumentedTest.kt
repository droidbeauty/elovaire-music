package elovaire.music.droidbeauty.app.data.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioDeviceCallback
import android.os.Handler
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRuntime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackRuntimeResourcesInstrumentedTest {
    @Test
    fun failedReceiverUnregistrationRemainsTrackedAndCanBeRetried() {
        val context = object : ContextWrapper(ApplicationProvider.getApplicationContext<Context>()) {
            var registrations = 0
            var unregistrations = 0
            override fun getApplicationContext(): Context = this
            override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?, flags: Int): Intent? {
                registrations++
                return null
            }
            override fun registerReceiver(
                receiver: BroadcastReceiver?,
                filter: IntentFilter?,
                broadcastPermission: String?,
                scheduler: Handler?,
                flags: Int,
            ): Intent? {
                registrations++
                return null
            }
            override fun unregisterReceiver(receiver: BroadcastReceiver?) {
                unregistrations++
                if (unregistrations == 1) throw IllegalStateException("temporary failure")
            }
        }
        val tracker = BackendResourceRuntime()
        val handler = Handler(Looper.getMainLooper())
        val resources = PlaybackRuntimeResources(
            context = context,
            audioManager = null,
            handler = handler,
            volumeObserver = object : ContentObserver(handler) {},
            audioDeviceCallback = object : AudioDeviceCallback() {},
            noisyReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) = Unit
            },
            resourceTracker = tracker,
        )
        try {
            resources.sync(hasQueue = false, isPlaying = true, playWhenReady = false)
            resources.sync(hasQueue = false, isPlaying = false, playWhenReady = false)
            resources.sync(hasQueue = false, isPlaying = true, playWhenReady = false)
            assertEquals(1, context.registrations)
            assertEquals(1, context.unregistrations)
            resources.release()
            assertEquals(2, context.unregistrations)
            assertEquals(0, tracker.snapshot().values.sum())
        } finally {
            resources.release()
        }
    }
}
