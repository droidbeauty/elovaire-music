package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioFormat
import androidx.media3.common.C
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BitPerfectPlaybackInternalsTest {
    @Test
    fun directPlaybackEvaluationKeyPreservesRouteAndPcmDetails() {
        val key = DirectPlaybackTrackConfig(
            encoding = C.ENCODING_PCM_24BIT,
            sampleRate = 96_000,
            channelMask = AudioFormat.CHANNEL_OUT_STEREO,
            channelCount = 2,
            tunneling = false,
            offload = false,
        ).toEvaluationKey(
            routeFingerprintHash = 99,
            routeDeviceId = 17,
            routeType = 11,
            effectsActive = false,
        )

        requireNotNull(key)
        assertEquals(99, key.routeFingerprintHash)
        assertEquals(17, key.routeDeviceId)
        assertEquals(11, key.routeType)
        assertEquals(96_000, key.sampleRate)
        assertEquals(AudioFormat.CHANNEL_OUT_STEREO, key.channelMask)
        assertEquals(2, key.channelCount)
        assertEquals(AudioFormat.ENCODING_PCM_24BIT_PACKED, key.encoding)
    }

    @Test
    fun unsupportedEncodingIsIneligibleForDirectPlaybackEvaluation() {
        val key = DirectPlaybackTrackConfig(
            encoding = C.ENCODING_AC3,
            sampleRate = 48_000,
            channelMask = AudioFormat.CHANNEL_OUT_STEREO,
            channelCount = 2,
            tunneling = false,
            offload = false,
        ).toEvaluationKey(
            routeFingerprintHash = 44,
            routeDeviceId = 7,
            routeType = 22,
            effectsActive = false,
        )

        assertNull(key)
    }

    @Test
    fun channelCountHelperHandlesCommonChannelMasks() {
        assertEquals(1, AudioFormat.CHANNEL_OUT_MONO.channelCountFromChannelMask())
        assertEquals(2, AudioFormat.CHANNEL_OUT_STEREO.channelCountFromChannelMask())
        assertTrue(AudioFormat.CHANNEL_OUT_5POINT1.channelCountFromChannelMask() >= 6)
    }

    @Test
    fun hardwareVolumeWriteFailureKeepsCapabilityOwnership() {
        val controller = UsbDacHardwareVolumeController()
        controller.onHardwareVolumeSupported(
            capability = UsbDacHardwareVolumeCapability(
                identity = UsbDacDeviceIdentity(
                    vendorId = 1,
                    productId = 2,
                    manufacturerName = "Acme",
                    productName = "DAC",
                    serialNumber = "123",
                ),
                audioClassVersion = UsbAudioClassVersion.Uac2,
                interfaceNumber = 1,
                featureUnitId = 2,
                range = UsbDacHardwareVolumeRange(minRaw = -12800, maxRaw = 0, stepRaw = 100),
                controlChannels = listOf(0, 1, 2),
                usesMasterChannel = true,
                muteSupported = false,
                canReadCurrent = true,
                canWriteVolume = true,
            ),
            currentRawValue = -6400,
        )

        controller.onHardwareVolumeWriteFailed("transfer failed")

        assertTrue(controller.status().shouldOwnVolumeControls)
        assertEquals(UsbDacHardwareVolumeState.HardwareVolumeActive, controller.status().state)
    }

    @Test
    fun usbAudioRoutingFingerprintUsesArrayContentsNotIdentity() {
        val first = UsbAudioDeviceDescriptor(
            id = 9,
            type = 11,
            isSink = true,
            productName = "DAC",
            address = "usb:1,2",
            sampleRates = intArrayOf(48_000, 96_000),
            encodings = intArrayOf(AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_FLOAT),
            channelCounts = intArrayOf(2, 6),
            channelMasks = intArrayOf(AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.CHANNEL_OUT_5POINT1),
        )
        val second = UsbAudioDeviceDescriptor(
            id = 9,
            type = 11,
            isSink = true,
            productName = "DAC",
            address = "usb:1,2",
            sampleRates = intArrayOf(96_000, 48_000),
            encodings = intArrayOf(AudioFormat.ENCODING_PCM_FLOAT, AudioFormat.ENCODING_PCM_16BIT),
            channelCounts = intArrayOf(6, 2),
            channelMasks = intArrayOf(AudioFormat.CHANNEL_OUT_5POINT1, AudioFormat.CHANNEL_OUT_STEREO),
        )

        assertNotSame(first.sampleRates, second.sampleRates)
        assertEquals(first.routingFingerprint(), second.routingFingerprint())
    }

    @Test
    fun playbackEffectsControllerReusesAudioProcessorArray() {
        val controller = PlaybackEffectsController()

        val first = controller.audioProcessors()
        val second = controller.audioProcessors()

        assertSame(first, second)
        assertEquals(1, first.size)
    }
}
