package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsbAudioClassVolumeParserTest {
    private val parser = UsbAudioClassVolumeParser()
    private val identity = UsbDacDeviceIdentity(
        vendorId = 1,
        productId = 2,
        manufacturerName = "Acme",
        productName = "DAC",
        serialNumber = "123",
    )

    @Test
    fun parsesUac1MasterVolumeFeatureUnit() {
        val capability = parser.parse(
            rawDescriptors = byteArrayOf(
                9, 4, 1, 0, 0, 1, 1, 0, 0,
                8, 36, 1, 0, 1, 0, 0, 0,
                8, 36, 6, 5, 1, 1, 3, 0,
            ),
            identity = identity,
        )

        assertNotNull(capability)
        requireNotNull(capability)
        assertEquals(UsbAudioClassVersion.Uac1, capability.audioClassVersion)
        assertEquals(5, capability.featureUnitId)
        assertTrue(capability.usesMasterChannel)
        assertEquals(listOf(0), capability.controlChannels)
        assertTrue(capability.muteSupported)
        assertTrue(capability.canWriteVolume)
    }

    @Test
    fun parsesUac2ReadableWritableMasterVolumeFeatureUnit() {
        val capability = parser.parse(
            rawDescriptors = byteArrayOf(
                9, 4, 3, 0, 0, 1, 1, 0, 0,
                9, 36, 1, 0, 2, 0, 0, 0, 0,
                14, 36, 6, 9, 0, 48, 0, 0, 0, 48, 0, 0, 0, 0,
            ),
            identity = identity,
        )

        assertNotNull(capability)
        requireNotNull(capability)
        assertEquals(UsbAudioClassVersion.Uac2, capability.audioClassVersion)
        assertEquals(9, capability.featureUnitId)
        assertTrue(capability.usesMasterChannel)
        assertEquals(listOf(0), capability.controlChannels)
        assertTrue(capability.canReadCurrent)
        assertTrue(capability.canWriteVolume)
    }

    @Test
    fun parserRejectsDescriptorsWithoutVolumeControl() {
        val capability = parser.parse(
            rawDescriptors = byteArrayOf(
                9, 4, 1, 0, 0, 1, 1, 0, 0,
                8, 36, 1, 0, 1, 0, 0, 0,
                8, 36, 6, 5, 1, 1, 0, 0,
            ),
            identity = identity,
        )

        assertNull(capability)
    }
}
