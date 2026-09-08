import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhysicalDeviceQualificationTest {
    @Test
    fun parsesAdbDeviceStatesWithoutTreatingHeaderAsDevice() {
        assertEquals(
            listOf(AdbDeviceEntry("phone", "device"), AdbDeviceEntry("offline", "offline")),
            parseAdbDevices(
                "List of devices attached\nphone\tdevice product:pixel model:Pixel\noffline\toffline\n",
            ),
        )
    }

    @Test
    fun parsesPropertiesAndRejectsKnownEmulatorShapes() {
        val physical = mapOf(
            "ro.build.fingerprint" to "google/panther/panther:15/AP3A/user/release-keys",
            "ro.hardware" to "tensor",
            "ro.product.model" to "Pixel 8",
            "ro.kernel.qemu" to "0",
        )
        assertEquals("ro.product.model" to "Pixel 8", parseAdbProperty("[ro.product.model]: [Pixel 8]"))
        assertTrue(isPhysicalDeviceProperties(physical))
        assertFalse(isPhysicalDeviceProperties(physical + ("ro.kernel.qemu" to "1")))
        assertNull(parseAdbProperty("not-a-property"))
    }
}
