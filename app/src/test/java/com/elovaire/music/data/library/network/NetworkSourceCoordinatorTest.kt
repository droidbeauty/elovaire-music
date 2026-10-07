package elovaire.music.droidbeauty.app.data.library.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkSourceCoordinatorTest {
    @Test
    fun credentialKeyConflictIsDetectedBeforeCredentialWrite() {
        val existing = source(id = "existing", credentialKey = "shared-key")
        val candidate = source(id = "new", credentialKey = "shared-key")

        assertTrue(hasCredentialKeyConflict(listOf(existing), candidate))
        assertFalse(hasCredentialKeyConflict(listOf(existing), existing.copy(name = "Renamed")))
        assertFalse(hasCredentialKeyConflict(listOf(existing), source(id = "new", credentialKey = "new-key")))
    }

    @Test
    fun changingStoredCredentialsRotatesTheKeyForCrashSafeRollback() {
        val existing = source(id = "existing", credentialKey = "old-key")
        val oldCredentials = NetworkCredentials("user", "old-password")
        val newCredentials = NetworkCredentials("user", "new-password")

        assertTrue(shouldRotateCredentialKey(existing, "old-key", oldCredentials, newCredentials))
        assertFalse(shouldRotateCredentialKey(existing, "old-key", oldCredentials, oldCredentials))
        assertFalse(shouldRotateCredentialKey(null, "new-key", null, newCredentials))
    }

    private fun source(id: String, credentialKey: String) = NetworkLibrarySource(
        id = id,
        name = id,
        protocol = NetworkLibraryProtocol.Smb,
        server = "server",
        shareOrPath = "share",
        username = "user",
        credentialKey = credentialKey,
    )
}
