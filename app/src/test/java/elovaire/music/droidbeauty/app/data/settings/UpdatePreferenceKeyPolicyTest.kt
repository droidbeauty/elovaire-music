package elovaire.music.droidbeauty.app.data.settings

import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePreferenceKeyPolicyTest {
    @Test
    fun updateStateBelongsToTheCanonicalSettingsMigration() {
        assertTrue(settingsPreferenceKeys.contains("dismissed_update_version"))
        assertTrue(settingsPreferenceKeys.contains("last_automatic_update_check_at_ms"))
    }
}
