package elovaire.music.droidbeauty.app.data.update

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateIntegrityTest {
    @Test
    fun parsesChecksumLinesForTheRequestedAsset() {
        val checksum = "a".repeat(64)
        assertEquals(checksum, AppUpdateIntegrity.expectedSha256("$checksum  elovaire.apk", "elovaire.apk"))
        assertEquals(null, AppUpdateIntegrity.expectedSha256("$checksum  other.apk", "elovaire.apk"))
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
}
