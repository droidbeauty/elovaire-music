package elovaire.music.droidbeauty.app.data.update

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class AppUpdateIntegrityTest {
    @Test
    fun parsesChecksumLinesForTheRequestedAsset() {
        val checksum = "a".repeat(64)
        assertEquals(checksum, AppUpdateIntegrity.expectedSha256("$checksum  elovaire.apk", "elovaire.apk"))
        assertEquals(null, AppUpdateIntegrity.expectedSha256("$checksum  other.apk", "elovaire.apk"))
        assertEquals(
            checksum,
            AppUpdateIntegrity.expectedSha256("${"b".repeat(64)}  other.apk\n$checksum  elovaire.apk", "elovaire.apk"),
        )
        assertEquals(null, AppUpdateIntegrity.expectedSha256("${"b".repeat(64)}\n$checksum", "elovaire.apk"))
    }

    @Test
    fun verifiesFileDigest() {
        val file = Files.createTempFile("elovaire-update", ".apk").toFile()
        try {
            file.writeText("update")
            val digest = AppUpdateIntegrity.sha256(file)
            assertTrue(AppUpdateIntegrity.verifySha256(file, digest))
            assertFalse(AppUpdateIntegrity.verifySha256(file, "b".repeat(64)))
        } finally {
            file.delete()
        }
    }

    @Test
    fun rejectsUnsafeStagingNames() {
        assertTrue(AppUpdateIntegrity.isSafeApkFileName("elovaire-github.apk"))
        assertFalse(AppUpdateIntegrity.isSafeApkFileName("../elovaire.apk"))
        assertFalse(AppUpdateIntegrity.isSafeApkFileName("nested/elovaire.apk"))
        assertFalse(AppUpdateIntegrity.isSafeApkFileName("elovaire.apk.part"))
    }

    @Test
    fun boundsDownloadsWithMissingOrDishonestLengths() {
        assertEquals(100L, AppUpdateIntegrity.downloadLimit(contentLengthBytes = null, assetSizeBytes = 100L))
        assertEquals(80L, AppUpdateIntegrity.downloadLimit(contentLengthBytes = 80L, assetSizeBytes = 100L))
        assertEquals(80L, AppUpdateIntegrity.checkedDownloadedByteCount(64L, 16, 80L))
        assertThrows(IllegalArgumentException::class.java) {
            AppUpdateIntegrity.checkedDownloadedByteCount(80L, 1, 80L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AppUpdateIntegrity.downloadLimit(AppUpdateIntegrity.MAX_APK_BYTES + 1L, null)
        }
    }
}
