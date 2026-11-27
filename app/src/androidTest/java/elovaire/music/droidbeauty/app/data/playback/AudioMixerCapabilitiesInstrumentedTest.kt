package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioMixerCapabilitiesInstrumentedTest {
    @Test
    fun reportConnectedUsbMixerCapabilitiesWithoutChangingPreferences() {
        if (Build.VERSION.SDK_INT < 34) {
            assumeTrue("Mixer attributes require Android 14", false)
            return
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mediaAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val routedTypes = manager.getAudioDevicesForAttributes(mediaAttributes).map { it.type }
        Log.i(TAG, "mediaAttributeRouteTypes=$routedTypes")
        val devices = manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).filter {
            it.type == AudioDeviceInfo.TYPE_USB_DEVICE || it.type == AudioDeviceInfo.TYPE_USB_HEADSET
        }
        assumeTrue("No USB output device connected", devices.isNotEmpty())
        devices.forEach { device ->
            val attributes = manager.getSupportedMixerAttributes(device)
            Log.i(TAG, "USB output type=${device.type} supportedMixerAttributes=${attributes.size}")
            attributes.forEach { mixer ->
                val format = mixer.format
                Log.i(
                    TAG,
                    "rate=${format.sampleRate} encoding=${format.encoding} " +
                        "channelMask=${format.channelMask} channelIndexMask=${format.channelIndexMask} " +
                        "behavior=${mixer.mixerBehavior} " +
                        "directSupport=${AudioManager.getDirectPlaybackSupport(format, mediaAttributes)}",
                )
            }
        }
    }

    private companion object {
        const val TAG = "AudioMixerQualification"
    }
}
