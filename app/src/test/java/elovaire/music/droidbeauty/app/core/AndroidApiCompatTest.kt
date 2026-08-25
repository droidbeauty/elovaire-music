package elovaire.music.droidbeauty.app.core

import android.Manifest
import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidApiCompatTest {
    @Test
    fun supportedApiMatrix_isExplicitAndContiguous() {
        val matrix = mapOf(
            30 to "Android 11",
            31 to "Android 12",
            32 to "Android 12L",
            33 to "Android 13",
            34 to "Android 14",
            35 to "Android 15",
            36 to "Android 16",
            37 to "Android 17",
        )

        assertEquals((30..37).toSet(), matrix.keys)
        assertEquals("Android 11", matrix[30])
        assertEquals("Android 17", matrix[37])
    }

    @Test
    fun requiredAudioPermission_matchesSupportedSdkRange() {
        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE, requiredAudioPermission(30))
        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE, requiredAudioPermission(Build.VERSION_CODES.R))
        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE, requiredAudioPermission(Build.VERSION_CODES.S_V2))
        assertEquals(Manifest.permission.READ_MEDIA_AUDIO, requiredAudioPermission(Build.VERSION_CODES.TIRAMISU))
        assertEquals(Manifest.permission.READ_MEDIA_AUDIO, requiredAudioPermission(Build.VERSION_CODES.TIRAMISU + 1))
        assertEquals(Manifest.permission.READ_MEDIA_AUDIO, requiredAudioPermission(37))
    }

    @Test
    fun verifiedDirectPlaybackRouting_onlyOnAndroid13AndNewer() {
        assertFalse(AndroidCapabilities.supportsDirectPlaybackQuery(Build.VERSION_CODES.S_V2))
        assertTrue(AndroidCapabilities.supportsDirectPlaybackQuery(Build.VERSION_CODES.TIRAMISU))
        assertTrue(AndroidCapabilities.supportsDirectPlaybackQuery(37))
    }

    @Test
    fun mediaMutationCapabilities_matchAndroid10And11Boundaries() {
        assertTrue(AndroidCapabilities.usesRecoverableMediaWrite(Build.VERSION_CODES.Q))
        assertFalse(AndroidCapabilities.usesRecoverableMediaWrite(Build.VERSION_CODES.R))
        assertFalse(AndroidCapabilities.supportsGroupedMediaWrite(Build.VERSION_CODES.Q))
        assertTrue(AndroidCapabilities.supportsGroupedMediaWrite(Build.VERSION_CODES.R))
    }

    @Test
    fun imageAndForegroundServiceCapabilities_matchPlatformBoundaries() {
        assertFalse(AndroidCapabilities.supportsImageDecoder(Build.VERSION_CODES.O_MR1))
        assertTrue(AndroidCapabilities.supportsImageDecoder(Build.VERSION_CODES.P))
        assertFalse(AndroidCapabilities.requiresMediaPlaybackForegroundServiceType(Build.VERSION_CODES.P))
        assertTrue(AndroidCapabilities.requiresMediaPlaybackForegroundServiceType(Build.VERSION_CODES.Q))
    }

    @Test
    fun localNetworkPermission_isRequiredOnlyForAndroid17TargetBoundary() {
        assertFalse(AndroidCapabilities.requiresLocalNetworkPermission(36))
        assertTrue(AndroidCapabilities.requiresLocalNetworkPermission(37))
        assertEquals("android.permission.ACCESS_LOCAL_NETWORK", AndroidCapabilities.LOCAL_NETWORK_PERMISSION)
    }
}
