package elovaire.music.app.data.playback

import android.media.AudioFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BitPerfectUsbPolicyTest {

    private val policy = BitPerfectUsbPolicy()

    @Test
    fun `exact format active bypasses processing`() {
        val requested = PlatformAudioFormatData(
            sampleRateHz = 96_000,
            channelMask = AudioFormat.CHANNEL_OUT_STEREO,
            encoding = AudioFormat.ENCODING_PCM_24BIT_PACKED,
            channelCount = 2,
        )
        val status = policy.deriveState(
            apiLevel = 33,
            selectedDevice = usbDescriptor(),
            trackFormatKnown = true,
            requestedFormat = requested,
            probeResult = UsbFormatProbeResult(
                requestedFormat = requested,
                exactFormatSupported = true,
                routeStrategy = UsbBitPerfectRouteStrategy.ExactFormatDirect,
                directPlaybackSupported = true,
                probedSuccessfully = true,
            ),
            applySucceeded = true,
            verifiedActive = true,
        )

        assertEquals(BitPerfectUsbState.ExactFormatUsbActive, status.state)
        assertTrue(status.shouldBypassProcessing)
    }

    @Test
    fun `api34 mixer path reports bit perfect active`() {
        val requested = PlatformAudioFormatData(
            sampleRateHz = 44_100,
            channelMask = AudioFormat.CHANNEL_OUT_STEREO,
            encoding = AudioFormat.ENCODING_PCM_16BIT,
            channelCount = 2,
        )
        val status = policy.deriveState(
            apiLevel = 34,
            selectedDevice = usbDescriptor(),
            trackFormatKnown = true,
            requestedFormat = requested,
            probeResult = UsbFormatProbeResult(
                requestedFormat = requested,
                exactFormatSupported = true,
                routeStrategy = UsbBitPerfectRouteStrategy.MixerAttributesBitPerfect,
                directPlaybackSupported = true,
                mixerAttributesSupported = true,
                probedSuccessfully = true,
            ),
            applySucceeded = true,
            verifiedActive = true,
        )

        assertEquals(BitPerfectUsbState.BitPerfectActive, status.state)
        assertTrue(status.shouldBypassProcessing)
    }

    @Test
    fun `unsupported exact format falls back cleanly`() {
        val requested = PlatformAudioFormatData(
            sampleRateHz = 192_000,
            channelMask = AudioFormat.CHANNEL_OUT_STEREO,
            encoding = AudioFormat.ENCODING_PCM_32BIT,
            channelCount = 2,
        )
        val status = policy.deriveState(
            apiLevel = 31,
            selectedDevice = usbDescriptor(),
            trackFormatKnown = true,
            requestedFormat = requested,
            probeResult = UsbFormatProbeResult(
                requestedFormat = requested,
                exactFormatSupported = false,
                routeStrategy = UsbBitPerfectRouteStrategy.None,
                directPlaybackSupported = false,
                probedSuccessfully = false,
                fallbackReason = "Exact USB output format not supported",
            ),
            applySucceeded = false,
            verifiedActive = false,
        )

        assertEquals(BitPerfectUsbState.FallbackNormalPlayback, status.state)
        assertFalse(status.shouldBypassProcessing)
    }

    private fun usbDescriptor() = UsbAudioDeviceDescriptor(
        id = 7,
        type = android.media.AudioDeviceInfo.TYPE_USB_DEVICE,
        isSink = true,
    )
}
