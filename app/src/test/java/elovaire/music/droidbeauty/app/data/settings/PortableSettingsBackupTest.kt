package elovaire.music.droidbeauty.app.data.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PortableSettingsBackupTest {
    @Test
    fun backsUpPortableSettingsButNotDeviceBoundMediaState() {
        assertTrue(isPortableSettingKey("theme_mode"))
        assertTrue(isPortableSettingKey("gapless_playback_enabled"))
        assertFalse(isPortableSettingKey("library_folders"))
        assertFalse(isPortableSettingKey("favorite_song_ids"))
        assertFalse(isPortableSettingKey("recent_song_ids"))
    }
}
