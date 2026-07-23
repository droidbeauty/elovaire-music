package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioDeviceInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsbDacHardwareVolumeManagerTest {
    @Test
    fun unsupportedUsbHostAndReleasedManagerRejectRouteWork() {
        val manager = UsbDacHardwareVolumeManager(
            context = InstrumentationRegistry.getInstrumentation().targetContext,
            audioManager = null,
            usbManager = null,
            usbHostSupported = false,
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
