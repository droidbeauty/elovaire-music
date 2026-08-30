package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsbDacConnectionStateTest {
    @Test
    fun stalePermissionCallbackCannotTargetReusedDeviceId() {
        val current = UsbDacConnectionToken(2L, 7, "new-device")

        assertFalse(isCurrentUsbDacCallback(current, 1L, 7, "old-device"))
        assertFalse(isCurrentUsbDacCallback(current, 2L, 7, "old-device"))
        assertTrue(isCurrentUsbDacCallback(current, 2L, 7, "new-device"))
    }

    @Test
    fun capabilityCacheEvictsLeastRecentlyUsedEntries() {
        val cache = UsbDacCapabilityCache(maxEntries = 2)
        val capability = capability("first")
        cache["first"] = capability
        cache["second"] = capability("second")
        assertEquals(capability, cache["first"])
        cache["third"] = capability("third")

        assertEquals(2, cache.size())
        assertEquals(null, cache["second"])
        assertEquals(capability, cache["first"])
    }

    private fun capability(name: String): UsbDacHardwareVolumeCapability {
        return UsbDacHardwareVolumeCapability(
            identity = UsbDacDeviceIdentity(1, 2, "maker", name, "serial-$name"),
            audioClassVersion = UsbAudioClassVersion.Uac2,
            interfaceNumber = 1,
            featureUnitId = 2,
            range = UsbDacHardwareVolumeRange(-100, 0, 1),
            controlChannels = listOf(0),
            usesMasterChannel = true,
            muteSupported = false,
            canReadCurrent = true,
            canWriteVolume = true,
        )
    }
}
