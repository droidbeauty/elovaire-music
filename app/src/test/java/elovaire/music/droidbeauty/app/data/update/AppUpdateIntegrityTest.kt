package elovaire.music.droidbeauty.app.data.update

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateIntegrityTest {
    @Test
    fun expectedSha256_readsPlainChecksum() {
        assertEquals(
            VALID_SHA,
            AppUpdateIntegrity.expectedSha256(VALID_SHA, "app.apk"),
        )
    }

    @Test
    fun expectedSha256_matchesChecksumForRequestedFile() {
        val text = """
            ${"0".repeat(64)}  other.apk
            $VALID_SHA *elovaire.apk
        """.trimIndent()

        assertEquals(VALID_SHA, AppUpdateIntegrity.expectedSha256(text, "elovaire.apk"))
    }

    @Test
    fun verifySha256_rejectsMismatchedChecksum() {
        val file = File.createTempFile("elovaire-update", ".apk").apply {
            writeText("payload")
            deleteOnExit()
        }

        assertFalse(AppUpdateIntegrity.verifySha256(file, "0".repeat(64)))
    }

    @Test
    fun verifySha256_acceptsMatchingChecksum() {
        val file = File.createTempFile("elovaire-update", ".apk").apply {
            writeText("payload")
            deleteOnExit()
        }

        assertTrue(AppUpdateIntegrity.verifySha256(file, AppUpdateIntegrity.sha256(file)))
    }

    private companion object {
        const val VALID_SHA = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
    }
}
