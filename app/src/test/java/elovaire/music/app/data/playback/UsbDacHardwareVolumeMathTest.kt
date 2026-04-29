package elovaire.music.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsbDacHardwareVolumeMathTest {
    private val range = UsbDacHardwareVolumeRange(
        minRaw = -12800,
        maxRaw = 0,
        stepRaw = 64,
    )

    @Test
    fun mapsNormalizedVolumeToNativeRange() {
        assertEquals(-12800, UsbDacHardwareVolumeMath.normalizedToRaw(0f, range))
        assertEquals(0, UsbDacHardwareVolumeMath.normalizedToRaw(1f, range))
        assertEquals(-6400, UsbDacHardwareVolumeMath.normalizedToRaw(0.5f, range))
    }

    @Test
    fun clampsAndSnapsToSupportedStepResolution() {
        assertEquals(-12800, range.clamp(-14000))
        assertEquals(0, range.clamp(100))
        assertEquals(-6336, range.clamp(-6333))
    }

    @Test
    fun convertsRawVolumeBackToNormalizedValue() {
        assertEquals(0.5f, UsbDacHardwareVolumeMath.rawToNormalized(-6400, range), 0.0001f)
    }

    @Test
    fun buildsStablePersistenceKey() {
        val identity = UsbDacDeviceIdentity(
            vendorId = 1234,
            productId = 5678,
            manufacturerName = "Acme",
            productName = "Reference DAC",
            serialNumber = "0001",
        )

        assertEquals("1234:5678:acme:reference dac:0001", identity.persistenceKey())
        assertTrue(identity.isReliable)
    }

    @Test
    fun doesNotConsiderBareVidPidReliableEnoughForRestore() {
        val identity = UsbDacDeviceIdentity(
            vendorId = 1234,
            productId = 5678,
            manufacturerName = null,
            productName = null,
            serialNumber = null,
        )

        assertFalse(identity.isReliable)
        assertFalse(
            UsbDacHardwareVolumeMath.shouldAutoRestoreStoredVolume(
                identity = identity,
                currentVolumeReadable = true,
                storedNormalizedVolume = 0.7f,
            ),
        )
    }

    @Test
    fun restoreIsAllowedOnlyForReliableReadableDevices() {
        val reliableIdentity = UsbDacDeviceIdentity(
            vendorId = 1234,
            productId = 5678,
            manufacturerName = "Acme",
            productName = "Reference DAC",
            serialNumber = null,
        )

        assertTrue(
            UsbDacHardwareVolumeMath.shouldAutoRestoreStoredVolume(
                identity = reliableIdentity,
                currentVolumeReadable = true,
                storedNormalizedVolume = 0.4f,
            ),
        )
        assertFalse(
            UsbDacHardwareVolumeMath.shouldAutoRestoreStoredVolume(
                identity = reliableIdentity,
                currentVolumeReadable = false,
                storedNormalizedVolume = 0.4f,
            ),
        )
    }

    @Test
    fun controllerTransitionsThroughDetectionSupportActivationAndRemoval() {
        val controller = UsbDacHardwareVolumeController()
        val identity = UsbDacDeviceIdentity(
            vendorId = 1234,
            productId = 5678,
            manufacturerName = "Acme",
            productName = "Reference DAC",
            serialNumber = "0001",
        )
        val capability = UsbDacHardwareVolumeCapability(
            identity = identity,
            audioClassVersion = UsbAudioClassVersion.Uac1,
            interfaceNumber = 0,
            featureUnitId = 5,
            range = range,
            controlChannels = listOf(0),
            usesMasterChannel = true,
            muteSupported = true,
            canReadCurrent = true,
            canWriteVolume = true,
        )

        controller.onExternalDacDetected(identity)
        assertEquals(UsbDacHardwareVolumeState.ExternalDacDetected, controller.status().state)

        controller.onHardwareVolumeSupported(capability, currentRawValue = -6400)
        assertEquals(UsbDacHardwareVolumeState.HardwareVolumeActive, controller.status().state)
        assertEquals(0.5f, controller.status().currentNormalizedVolume ?: 0f, 0.0001f)

        controller.onHardwareVolumeApplied(-3200)
        assertEquals(0.75f, controller.status().currentNormalizedVolume ?: 0f, 0.01f)

        controller.onNoExternalDac()
        assertEquals(UsbDacHardwareVolumeState.NoExternalDac, controller.status().state)
        assertNull(controller.status().currentNormalizedVolume)
    }

    @Test
    fun controllerFallsBackCleanlyWhenHardwareVolumeIsUnavailable() {
        val controller = UsbDacHardwareVolumeController()

        controller.onHardwareVolumeUnsupported("No feature unit")
        assertEquals(UsbDacHardwareVolumeState.HardwareVolumeUnsupported, controller.status().state)

        controller.onHardwareVolumeUnavailable("Permission denied")
        assertEquals(UsbDacHardwareVolumeState.HardwareVolumeUnavailable, controller.status().state)
    }
}
