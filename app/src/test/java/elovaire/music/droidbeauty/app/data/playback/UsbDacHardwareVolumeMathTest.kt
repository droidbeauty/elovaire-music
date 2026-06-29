package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UsbDacHardwareVolumeMathTest {
    @Test
    fun normalizedToRawSnapsAndClampsWithinRange() {
        val range = UsbDacHardwareVolumeRange(
            minRaw = -12800,
            maxRaw = 0,
            stepRaw = 256,
        )

        assertEquals(-12800, UsbDacHardwareVolumeMath.normalizedToRaw(-1f, range))
        assertEquals(0, UsbDacHardwareVolumeMath.normalizedToRaw(2f, range))
        assertEquals(-6400, UsbDacHardwareVolumeMath.normalizedToRaw(0.5f, range))
    }

    @Test
    fun rawToNormalizedUsesClampedSnappedValue() {
        val range = UsbDacHardwareVolumeRange(
            minRaw = -12800,
            maxRaw = 0,
            stepRaw = 100,
        )

        assertEquals(0f, UsbDacHardwareVolumeMath.rawToNormalized(-20000, range), 0.0001f)
        assertEquals(1f, UsbDacHardwareVolumeMath.rawToNormalized(1000, range), 0.0001f)
        assertEquals(0.5f, UsbDacHardwareVolumeMath.rawToNormalized(-6400, range), 0.0001f)
    }

    @Test
    fun storedVolumeCandidateRequiresReliableReadableIdentity() {
        val reliableIdentity = UsbDacDeviceIdentity(
            vendorId = 1,
            productId = 2,
            manufacturerName = "Acme",
            productName = "DAC",
            serialNumber = "123",
        )
        val unreliableIdentity = UsbDacDeviceIdentity(
            vendorId = 0,
            productId = 2,
            manufacturerName = null,
            productName = "DAC",
            serialNumber = null,
        )

        val candidate = UsbDacHardwareVolumeMath.resolveStoredVolumeCandidate(
            identity = reliableIdentity,
            currentVolumeReadable = true,
            storedNormalizedVolume = 0.75f,
        )

        requireNotNull(candidate)
        assertEquals(0.75f, candidate, 0.0001f)
        assertNull(
            UsbDacHardwareVolumeMath.resolveStoredVolumeCandidate(
                identity = reliableIdentity,
                currentVolumeReadable = false,
                storedNormalizedVolume = 0.75f,
            ),
        )
        assertNull(
            UsbDacHardwareVolumeMath.resolveStoredVolumeCandidate(
                identity = unreliableIdentity,
                currentVolumeReadable = true,
                storedNormalizedVolume = 0.75f,
            ),
        )
    }
}
