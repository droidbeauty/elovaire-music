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

    @Test
    fun bootSnapshotUsesStableTypedValuesAndSettingsOnly() {
        val first = settingsBootSnapshotChecksum(
            linkedMapOf("theme_mode" to "dark", "playback_volume" to 0.5f, "favorite_song_ids" to "ignored"),
        )
        val second = settingsBootSnapshotChecksum(
            linkedMapOf("playback_volume" to 0.5f, "theme_mode" to "dark"),
        )

        assertEquals(first, second)
        val snapshot = SettingsSnapshot(mapOf("playback_volume" to 0.5f, "last_automatic_update_check_at_ms" to 7))
        assertEquals(0.5f, snapshot.getFloat("playback_volume", 1f))
        assertEquals(7L, snapshot.getLong("last_automatic_update_check_at_ms", 0L))
    }
}
