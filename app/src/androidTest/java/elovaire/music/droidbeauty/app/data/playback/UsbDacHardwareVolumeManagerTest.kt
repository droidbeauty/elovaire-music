package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@RunWith(AndroidJUnit4::class)
class UsbDacHardwareVolumeManagerTest {
    @Test
    fun usbSliderAndSystemVolumeStayInSync() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val audioManager = context.getSystemService(AudioManager::class.java)
        assumeTrue("Device volume is fixed", !audioManager.isVolumeFixed)
        assumeTrue("Test needs no attached USB DAC", audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .none { it.type == AudioDeviceInfo.TYPE_USB_DEVICE || it.type == AudioDeviceInfo.TYPE_USB_HEADSET })
        val maximum = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        assumeTrue("Insufficient volume steps", maximum >= 2)
        val original = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        instrumentation.runOnMainSync {
            val manager = PlaybackManager(context, scope)
            try {
                val controller = PlaybackManager::class.java.getDeclaredField("audioOutputController")
                    .apply { isAccessible = true }.get(manager) as PlaybackAudioOutputController
                controller.currentAudioRouteSnapshot = AudioOutputRouteSnapshot(1L, emptyList(),
                    UsbAudioDeviceDescriptor(1, AudioDeviceInfo.TYPE_USB_DEVICE, true), emptyList())
                val sliderStep = 1
                manager.setVolume(sliderStep.toFloat() / maximum)
                assertEquals(sliderStep, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
                assertEquals(sliderStep.toFloat() / maximum, manager.state.value.volume, 0.011f)

                val buttonStep = 2
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
                assertEquals(buttonStep, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
                PlaybackManager::class.java.getDeclaredMethod("syncFromObservedSystemVolume")
                    .apply { isAccessible = true }.invoke(manager)
                assertEquals(buttonStep.toFloat() / maximum, manager.state.value.volume, 0.011f)
            } finally {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, original, 0)
                manager.release()
                scope.cancel()
            }
        }
    }

    @Test
    fun normalizationPreventsDirectModeWhenItChangesTheSignal() {
        assertTrue(hasRequestedSignalChanges(false, true,
            elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationMetadata(trackGainDb = -6f)))
        assertFalse(hasRequestedSignalChanges(false, false, null))
    }

    @Test
    fun unsupportedUsbHostAndReleasedManagerRejectRouteWork() {
        val manager = UsbDacHardwareVolumeManager(
            context = InstrumentationRegistry.getInstrumentation().targetContext,
            audioManager = null,
            usbManager = null,
            usbHostSupported = false,
            scope = CoroutineScope(Dispatchers.Default),
        )
        val initial = manager.status.value

        manager.updateAudioOutputDevice(
            UsbAudioDeviceDescriptor(
                id = 1,
                type = AudioDeviceInfo.TYPE_USB_DEVICE,
                isSink = true,
            ),
        )
        manager.release()
        manager.release()

        assertEquals(initial, manager.status.value)
        assertFalse(manager.setHardwareVolume(0.5f))
    }
}
