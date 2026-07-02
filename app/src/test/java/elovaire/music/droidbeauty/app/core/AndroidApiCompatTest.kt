package elovaire.music.droidbeauty.app.core

import android.Manifest
import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidApiCompatTest {
    @Test
    fun requiredAudioPermission_matchesSupportedSdkRange() {
        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE, requiredAudioPermission(Build.VERSION_CODES.R))
        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE, requiredAudioPermission(Build.VERSION_CODES.S_V2))
        assertEquals(Manifest.permission.READ_MEDIA_AUDIO, requiredAudioPermission(Build.VERSION_CODES.TIRAMISU))
        assertEquals(Manifest.permission.READ_MEDIA_AUDIO, requiredAudioPermission(37))
    }

    @Test
    fun notificationPermissionRequired_onlyOnAndroid13AndNewer() {
        assertFalse(requiresNotificationPostingPermission(Build.VERSION_CODES.S_V2))
        assertTrue(requiresNotificationPostingPermission(Build.VERSION_CODES.TIRAMISU))
        assertTrue(requiresNotificationPostingPermission(37))
    }

    @Test
    fun verifiedDirectPlaybackRouting_onlyOnAndroid13AndNewer() {
        assertFalse(supportsVerifiedDirectPlaybackRouting(Build.VERSION_CODES.S_V2))
        assertTrue(supportsVerifiedDirectPlaybackRouting(Build.VERSION_CODES.TIRAMISU))
        assertTrue(supportsVerifiedDirectPlaybackRouting(37))
    }
}
