package elovaire.music.app.data.playback

import android.media.AudioAttributes
import android.media.AudioManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 34)
class BitPerfectUsbManagerApi34Test {
    @Test
    fun managerCanInitializeAndReleaseWithFrameworkAudioServices() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val audioManager = context.getSystemService(AudioManager::class.java)
        val playbackAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val manager = BitPerfectUsbManager(
            context = context,
            audioManager = audioManager,
            playbackAudioAttributes = playbackAttributes,
        )

        manager.refreshConnectedDevices()

        assertNotNull(manager.status.value.state)

        manager.clearForStop()
        manager.release()
    }
}
