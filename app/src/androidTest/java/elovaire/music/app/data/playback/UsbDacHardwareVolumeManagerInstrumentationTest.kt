package elovaire.music.app.data.playback

import android.hardware.usb.UsbManager
import android.media.AudioManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsbDacHardwareVolumeManagerInstrumentationTest {
    @Test
    fun managerInitializesAndReleasesWithoutMutatingSystemVolume() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val audioManager = context.getSystemService(AudioManager::class.java)
        val usbManager = context.getSystemService(UsbManager::class.java)
        val before = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)

        val manager = UsbDacHardwareVolumeManager(
            context = context,
            audioManager = audioManager,
            usbManager = usbManager,
        )
        manager.updateAudioOutputDevice(null)
        manager.release()

        val after = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
        assertNotNull(manager.status.value.state)
        assertEqualsNullable(before, after)
    }

    private fun assertEqualsNullable(
        expected: Int?,
        actual: Int?,
    ) {
        if (expected == null) {
            org.junit.Assert.assertNull(actual)
        } else {
            org.junit.Assert.assertEquals(expected, actual)
        }
    }
}
