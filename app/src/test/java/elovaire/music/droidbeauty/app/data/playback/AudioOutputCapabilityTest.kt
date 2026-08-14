package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertFalse
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

        assertTrue(capabilities.hasUsbOutput)
        assertFalse(decision.offloadAllowed)
        assertFalse(decision.signalProcessingRequired)
    }
}

