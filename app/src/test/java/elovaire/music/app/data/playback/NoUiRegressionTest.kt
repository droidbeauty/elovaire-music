package elovaire.music.app.data.playback

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class NoUiRegressionTest {
    @Test
    fun noBitPerfectUserFacingStringsWereAdded() {
        val projectRoot = locateProjectRoot()
        val stringsFile = File(projectRoot, "app/src/main/res/values/strings.xml")
        val content = stringsFile.readText().lowercase()

        assertFalse(content.contains("bit-perfect"))
        assertFalse(content.contains("bit perfect"))
        assertFalse(content.contains("usb dac"))
    }

    @Test
    fun noBitPerfectSettingKeyWasAdded() {
        val projectRoot = locateProjectRoot()
        val preferenceStore = File(projectRoot, "app/src/main/java/com/elovaire/music/data/settings/PreferenceStore.kt")
        val content = preferenceStore.readText().lowercase()

        assertFalse(content.contains("bit_perfect"))
        assertFalse(content.contains("usb_dac"))
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
