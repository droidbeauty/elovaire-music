package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsbDacDeviceMatcherTest {
    @Test
    fun exactAddressAndProductNameProduceUniqueMatch() {
        val descriptor = UsbAudioDeviceDescriptor(
            id = 9,
            type = 11,
            isSink = true,
            productName = "Acme DAC",
            address = "/dev/bus/usb/001/005",
        )

        val result = UsbDacDeviceMatcher.match(
            descriptor = descriptor,
            candidates = listOf(
                UsbDacMatchCandidate(
                    deviceId = 1,
                    deviceName = "/dev/bus/usb/001/004",
                    productName = "Other DAC",
                    manufacturerName = "Other",
                    vendorId = 11,
                    productId = 22,
                    audioInterfaceCount = 1,
                ),
                UsbDacMatchCandidate(
                    deviceId = 2,
                    deviceName = "/dev/bus/usb/001/005",
                    productName = "Acme DAC",
                    manufacturerName = "Acme",
                    vendorId = 33,
                    productId = 44,
                    audioInterfaceCount = 2,
                ),
            ),
        )

        assertEquals(UsbDacMatchResult.Matched(2), result)
    }

    @Test
    fun ambiguousDevicesDoNotPickArbitraryFirstMatch() {
        val descriptor = UsbAudioDeviceDescriptor(
            id = 9,
            type = 11,
            isSink = true,
            productName = "Shared DAC",
            address = null,
        )

        val result = UsbDacDeviceMatcher.match(
            descriptor = descriptor,
            candidates = listOf(
                UsbDacMatchCandidate(
                    deviceId = 1,
                    deviceName = "/dev/bus/usb/001/004",
                    productName = "Shared DAC",
                    manufacturerName = "Acme",
                    vendorId = 11,
                    productId = 22,
                    audioInterfaceCount = 1,
                ),
                UsbDacMatchCandidate(
                    deviceId = 2,
                    deviceName = "/dev/bus/usb/001/005",
                    productName = "Shared DAC",
                    manufacturerName = "Acme",
                    vendorId = 11,
                    productId = 22,
                    audioInterfaceCount = 1,
                ),
            ),
        )

        assertTrue(result is UsbDacMatchResult.Ambiguous)
    }
}
