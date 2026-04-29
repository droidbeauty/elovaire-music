package elovaire.music.app.data.playback

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class UsbDacHardwareVolumeImplementationTest {
    @Test
    fun hardwareVolumeManagerNeverUsesSystemVolumeMutationApis() {
        val content = File(
            locateProjectRoot(),
            "app/src/main/java/com/elovaire/music/data/playback/UsbDacHardwareVolumeManager.kt",
        ).readText()

        assertFalse(content.contains("setStreamVolume("))
        assertFalse(content.contains("adjustStreamVolume("))
        assertFalse(content.contains("adjustVolume("))
    }

    @Test
    fun hardwareVolumeManagerNeverUsesSoftwarePlayerGainAsPrimaryControl() {
        val content = File(
            locateProjectRoot(),
            "app/src/main/java/com/elovaire/music/data/playback/UsbDacHardwareVolumeManager.kt",
        ).readText()

        assertFalse(content.contains("player.volume"))
    }

    private fun locateProjectRoot(): File {
        var current = File(System.getProperty("user.dir") ?: ".").absoluteFile
        repeat(6) {
            if (File(current, "settings.gradle.kts").exists()) {
                return current
            }
            current = current.parentFile ?: return current
        }
        return current
    }
}
