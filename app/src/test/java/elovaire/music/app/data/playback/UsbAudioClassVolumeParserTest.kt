package elovaire.music.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsbAudioClassVolumeParserTest {
    private val parser = UsbAudioClassVolumeParser()
    private val identity = UsbDacDeviceIdentity(
        vendorId = 1111,
        productId = 2222,
        manufacturerName = "Acme",
        productName = "DAC",
        serialNumber = "A1",
    )

    @Test
    fun parsesUac1FeatureUnitWithMasterVolume() {
        val capability = parser.parse(uac1MasterDescriptorBytes(), identity)

        assertNotNull(capability)
        assertEquals(UsbAudioClassVersion.Uac1, capability?.audioClassVersion)
        assertTrue(capability?.usesMasterChannel == true)
        assertEquals(listOf(0), capability?.controlChannels)
        assertTrue(capability?.muteSupported == false)
    }

    @Test
    fun parsesUac1PerChannelVolumeWhenMasterMissing() {
        val capability = parser.parse(uac1PerChannelDescriptorBytes(), identity)

        assertNotNull(capability)
        assertFalse(capability?.usesMasterChannel == true)
        assertEquals(listOf(1, 2), capability?.controlChannels)
    }

    @Test
    fun parsesUac2FeatureUnitAndPrefersMasterVolume() {
        val capability = parser.parse(uac2MasterDescriptorBytes(), identity)

        assertNotNull(capability)
        assertEquals(UsbAudioClassVersion.Uac2, capability?.audioClassVersion)
        assertTrue(capability?.usesMasterChannel == true)
        assertTrue(capability?.canWriteVolume == true)
    }

    @Test
    fun parsesReadOnlyUac2VolumeControls() {
        val capability = parser.parse(uac2ReadOnlyDescriptorBytes(), identity)

        assertNotNull(capability)
        assertTrue(capability?.canReadCurrent == true)
        assertFalse(capability?.canWriteVolume == true)
    }

    @Test
    fun returnsNullForMalformedDescriptors() {
        assertNull(parser.parse(byteArrayOf(0x02, 0x24, 0x06), identity))
    }

    private fun uac1MasterDescriptorBytes(): ByteArray {
        return byteArrayOf(
            0x09, 0x04, 0x00, 0x00, 0x00, 0x01, 0x01, 0x00, 0x00,
            0x09, 0x24, 0x01, 0x00, 0x01, 0x09, 0x00, 0x01, 0x01,
            0x0A, 0x24, 0x06, 0x05, 0x02, 0x01, 0x02, 0x02, 0x02, 0x00,
        )
    }

    private fun uac1PerChannelDescriptorBytes(): ByteArray {
        return byteArrayOf(
            0x09, 0x04, 0x00, 0x00, 0x00, 0x01, 0x01, 0x00, 0x00,
            0x09, 0x24, 0x01, 0x00, 0x01, 0x09, 0x00, 0x01, 0x01,
            0x0A, 0x24, 0x06, 0x07, 0x02, 0x01, 0x00, 0x02, 0x02, 0x00,
        )
    }

    private fun uac2MasterDescriptorBytes(): ByteArray {
        return byteArrayOf(
            0x09, 0x04, 0x00, 0x00, 0x00, 0x01, 0x01, 0x20, 0x00,
            0x09, 0x24, 0x01, 0x00, 0x02, 0x09, 0x00, 0x00, 0x00,
            0x12, 0x24, 0x06, 0x09, 0x02,
            0x30, 0x00, 0x00, 0x00,
            0x30, 0x00, 0x00, 0x00,
            0x30, 0x00, 0x00, 0x00,
            0x00,
        )
    }

    private fun uac2ReadOnlyDescriptorBytes(): ByteArray {
        return byteArrayOf(
            0x09, 0x04, 0x00, 0x00, 0x00, 0x01, 0x01, 0x20, 0x00,
            0x09, 0x24, 0x01, 0x00, 0x02, 0x09, 0x00, 0x00, 0x00,
            0x12, 0x24, 0x06, 0x0A, 0x02,
            0x10, 0x00, 0x00, 0x00,
            0x10, 0x00, 0x00, 0x00,
            0x10, 0x00, 0x00, 0x00,
            0x00,
        )
    }
}
