package elovaire.music.droidbeauty.app.data.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PortableSettingsBackupTest {
    @Test
    fun backsUpPortableSettingsButNotDeviceBoundMediaState() {
        assertTrue(isPortableSettingKey("theme_mode"))
        assertTrue(isPortableSettingKey("crossfade_enabled"))
        assertFalse(isPortableSettingKey("library_folders"))
        assertFalse(isPortableSettingKey("favorite_song_ids"))
        assertFalse(isPortableSettingKey("recent_song_ids"))
    }

    @Test
    fun backupChecksumIsIndependentOfMapOrderAndExcludesDeviceState() {
        val first = portableSettingsBackupChecksum(
            linkedMapOf("theme_mode" to "dark", "eq_bands" to setOf("1", "2"), "library_folders" to "private"),
        )
        val second = portableSettingsBackupChecksum(
            linkedMapOf("eq_bands" to setOf("2", "1"), "theme_mode" to "dark"),
        )

        assertEquals(first, second)
    }
}
