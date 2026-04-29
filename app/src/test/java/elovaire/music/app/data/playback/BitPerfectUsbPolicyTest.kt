package elovaire.music.app.data.playback

import android.media.AudioDeviceInfo
import android.media.AudioFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BitPerfectUsbPolicyTest {
    private val policy = BitPerfectUsbPolicy(
        bitPerfectBehavior = BitPerfectUsbPolicy.BIT_PERFECT_MIXER_BEHAVIOR,
        minimumSupportedApi = 34,
    )

    @Test
    fun selectsUsbCandidateFromEligibleOutputs() {
        val selected = policy.selectUsbDevice(
            devices = listOf(
                UsbAudioDeviceDescriptor(
                    id = 7,
                    type = AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    isSink = true,
                ),
                UsbAudioDeviceDescriptor(
                    id = 3,
                    type = AudioDeviceInfo.TYPE_USB_DEVICE,
                    isSink = true,
                    productName = "DAC",
                ),
            ),
        )

        assertEquals(3, selected?.id)
    }

    @Test
    fun mapsTrackFormatToExactPlatformAudioFormat() {
        val mapped = policy.mapTrackFormat(
            TrackPlaybackFormat(
                sampleRateHz = 96_000,
                channelCount = 2,
                encoding = UsbPcmEncoding.Pcm24BitPacked,
                sourceBitDepth = 24,
            ),
        )

        assertEquals(
            PlatformAudioFormatData(
                sampleRateHz = 96_000,
                channelMask = AudioFormat.CHANNEL_OUT_STEREO,
                encoding = AudioFormat.ENCODING_PCM_24BIT_PACKED,
            ),
            mapped,
        )
    }

    @Test
    fun rejectsUnsupportedChannelLayouts() {
        val mapped = policy.mapTrackFormat(
            TrackPlaybackFormat(
                sampleRateHz = 44_100,
                channelCount = 3,
                encoding = UsbPcmEncoding.Pcm16Bit,
            ),
        )

        assertNull(mapped)
    }

    @Test
    fun selectsOnlyExactBitPerfectMixerProfile() {
        val requested = PlatformAudioFormatData(
            sampleRateHz = 44_100,
            channelMask = AudioFormat.CHANNEL_OUT_STEREO,
            encoding = AudioFormat.ENCODING_PCM_16BIT,
        )

        val selected = policy.selectSupportedProfile(
            requestedFormat = requested,
            supportedProfiles = listOf(
                SupportedMixerProfile(
                    format = requested.copy(sampleRateHz = 48_000),
                    mixerBehavior = BitPerfectUsbPolicy.BIT_PERFECT_MIXER_BEHAVIOR,
                ),
                SupportedMixerProfile(
                    format = requested,
                    mixerBehavior = BitPerfectUsbPolicy.BIT_PERFECT_MIXER_BEHAVIOR,
                ),
            ),
        )

        assertEquals(requested, selected?.format)
    }

    @Test
    fun fallsBackBelowAndroid14() {
        val status = policy.deriveState(
            apiLevel = 33,
            selectedDevice = UsbAudioDeviceDescriptor(
                id = 1,
                type = AudioDeviceInfo.TYPE_USB_DEVICE,
                isSink = true,
            ),
            trackFormatKnown = true,
            requestedFormat = PlatformAudioFormatData(
                sampleRateHz = 44_100,
                channelMask = AudioFormat.CHANNEL_OUT_STEREO,
                encoding = AudioFormat.ENCODING_PCM_16BIT,
            ),
            matchedProfile = null,
            applySucceeded = false,
            verifiedActive = false,
        )

        assertEquals(BitPerfectUsbState.UnsupportedAndroidVersion, status.state)
    }

    @Test
    fun transitionsAcrossPlugApplyActiveUnplugAndFormatChange() {
        val device = UsbAudioDeviceDescriptor(
            id = 9,
            type = AudioDeviceInfo.TYPE_USB_HEADSET,
            isSink = true,
        )
        val track44 = policy.mapTrackFormat(
            TrackPlaybackFormat(
                sampleRateHz = 44_100,
                channelCount = 2,
                encoding = UsbPcmEncoding.Pcm16Bit,
            ),
        )
        val track96 = policy.mapTrackFormat(
            TrackPlaybackFormat(
                sampleRateHz = 96_000,
                channelCount = 2,
                encoding = UsbPcmEncoding.Pcm24BitPacked,
                sourceBitDepth = 24,
            ),
        )
        val matched44 = SupportedMixerProfile(
            format = track44!!,
            mixerBehavior = BitPerfectUsbPolicy.BIT_PERFECT_MIXER_BEHAVIOR,
        )

        val detected = policy.deriveState(
            apiLevel = 34,
            selectedDevice = device,
            trackFormatKnown = false,
            requestedFormat = null,
            matchedProfile = null,
            applySucceeded = false,
            verifiedActive = false,
        )
        val active = policy.deriveState(
            apiLevel = 34,
            selectedDevice = device,
            trackFormatKnown = true,
            requestedFormat = track44,
            matchedProfile = matched44,
            applySucceeded = true,
            verifiedActive = true,
        )
        val unsupportedAfterTrackChange = policy.deriveState(
            apiLevel = 34,
            selectedDevice = device,
            trackFormatKnown = true,
            requestedFormat = track96,
            matchedProfile = null,
            applySucceeded = false,
            verifiedActive = false,
        )
        val unplugged = policy.deriveState(
            apiLevel = 34,
            selectedDevice = null,
            trackFormatKnown = false,
            requestedFormat = null,
            matchedProfile = null,
            applySucceeded = false,
            verifiedActive = false,
        )

        assertEquals(BitPerfectUsbState.UsbDetected, detected.state)
        assertEquals(BitPerfectUsbState.BitPerfectActive, active.state)
        assertEquals(BitPerfectUsbState.UnsupportedDeviceOrFormat, unsupportedAfterTrackChange.state)
        assertEquals(BitPerfectUsbState.NoUsbDevice, unplugged.state)
    }

    @Test
    fun exposesFallbackStateWhenRequestIsRejected() {
        val requested = PlatformAudioFormatData(
            sampleRateHz = 44_100,
            channelMask = AudioFormat.CHANNEL_OUT_STEREO,
            encoding = AudioFormat.ENCODING_PCM_16BIT,
        )
        val status = policy.deriveState(
            apiLevel = 34,
            selectedDevice = UsbAudioDeviceDescriptor(
                id = 4,
                type = AudioDeviceInfo.TYPE_USB_DEVICE,
                isSink = true,
            ),
            trackFormatKnown = true,
            requestedFormat = requested,
            matchedProfile = SupportedMixerProfile(
                format = requested,
                mixerBehavior = BitPerfectUsbPolicy.BIT_PERFECT_MIXER_BEHAVIOR,
            ),
            applySucceeded = false,
            verifiedActive = false,
        )

        assertEquals(BitPerfectUsbState.FallbackActive, status.state)
        assertTrue(status.fallbackReason.orEmpty().isNotBlank())
    }

    @Test
    fun mapsUnsupportedTrackToInternalUnsupportedState() {
        val status = policy.deriveState(
            apiLevel = 34,
            selectedDevice = UsbAudioDeviceDescriptor(
                id = 5,
                type = AudioDeviceInfo.TYPE_USB_DEVICE,
                isSink = true,
            ),
            trackFormatKnown = true,
            requestedFormat = null,
            matchedProfile = null,
            applySucceeded = false,
            verifiedActive = false,
        )

        assertEquals(BitPerfectUsbState.UnsupportedDeviceOrFormat, status.state)
        assertNotNull(status.fallbackReason)
    }

    @Test
    fun ignoresBluetoothAndWiredRoutesWhenSelectingUsbDevice() {
        val selected = policy.selectUsbDevice(
            devices = listOf(
                UsbAudioDeviceDescriptor(
                    id = 11,
                    type = AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    isSink = true,
                ),
                UsbAudioDeviceDescriptor(
                    id = 12,
                    type = AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    isSink = true,
                ),
            ),
        )

        assertNull(selected)
    }

}
