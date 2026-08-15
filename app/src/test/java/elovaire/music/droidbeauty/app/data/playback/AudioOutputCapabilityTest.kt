package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioOutputCapabilityTest {
    @Test
    fun processingRequirementsDisableOffload() {
        val capabilities = AudioOutputCapabilitySnapshot(
            devices = emptyList(),
            platformSdk = 35,
        )

        val decision = AudioOutputPolicy.decide(
            capabilities = capabilities,
            requirements = AudioProcessingRequirements(
                signalAlteringEffectsActive = false,
                normalizationActive = true,
                monoOrChannelMappingActive = false,
                crossfadeActive = false,
            ),
            directPathActive = false,
        )

        assertTrue(decision.signalProcessingRequired)
        assertFalse(decision.offloadAllowed)
    }

    @Test
    fun usbRouteIsRepresentedWithoutInferringBitPerfectSupport() {
        val capabilities = AudioOutputCapabilitySnapshot(
            devices = listOf(
                AudioOutputDeviceCapability(
                    id = 3,
                    type = 11,
                    route = AudioOutputRouteKind.Usb,
                    sampleRates = emptyList(),
                    channelCounts = emptyList(),
                    channelMasks = emptyList(),
                    encodings = emptyList(),
                ),
            ),
            platformSdk = 35,
        )

        val decision = AudioOutputPolicy.decide(
            capabilities = capabilities,
            requirements = AudioProcessingRequirements(false, false, false, false),
            directPathActive = true,
        )

        assertTrue(capabilities.hasActiveUsbOutput)
        assertFalse(decision.offloadAllowed)
        assertFalse(decision.signalProcessingRequired)
    }

    @Test
    fun availableUsbDeviceIsNotTreatedAsTheActiveRoute() {
        val capabilities = AudioOutputCapabilitySnapshot(
            devices = listOf(usbDevice(3)),
            platformSdk = 32,
            source = AudioOutputCapabilitySource.AvailableDevices,
        )

        val decision = AudioOutputPolicy.decide(
            capabilities = capabilities,
            requirements = AudioProcessingRequirements(false, false, false, false),
            directPathActive = false,
        )

        assertFalse(capabilities.hasActiveUsbOutput)
        assertTrue(capabilities.hasAvailableUsbOutput)
        assertTrue(decision.offloadAllowed)
    }

    @Test
    fun normalizationDisablesOffloadLikeOtherSignalProcessing() {
        val decision = AudioOutputPolicy.decide(
            capabilities = AudioOutputCapabilitySnapshot(emptyList(), platformSdk = 35),
            requirements = AudioProcessingRequirements(
                signalAlteringEffectsActive = false,
                normalizationActive = true,
                monoOrChannelMappingActive = false,
                crossfadeActive = false,
            ),
            directPathActive = false,
        )

        assertFalse(decision.offloadAllowed)
    }

    @Test
    fun routeSignatureIgnoresCallbackOrdering() {
        val first = AudioOutputCapabilitySnapshot(
            devices = listOf(usbDevice(3), usbDevice(1)),
            platformSdk = 35,
        )
        val second = AudioOutputCapabilitySnapshot(
            devices = listOf(usbDevice(1), usbDevice(3)),
            platformSdk = 35,
        )

        assertEquals(first.routeSignature, second.routeSignature)
    }

    private fun usbDevice(id: Int) = AudioOutputDeviceCapability(
        id = id,
        type = 11,
        route = AudioOutputRouteKind.Usb,
        sampleRates = listOf(96_000, 48_000),
        channelCounts = listOf(2),
        channelMasks = emptyList(),
        encodings = emptyList(),
    )
}
