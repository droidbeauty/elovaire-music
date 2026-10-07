package elovaire.music.droidbeauty.app.data.library.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkSourceMutationJournalTest {
    @Test
    fun savePhasesOnlyAdvanceInDurableOrder() {
        assertTrue(
            isValidNetworkSourceMutationPhaseTransition(
                NetworkSourceMutationKind.Save,
                NetworkSourceMutationPhase.Prepared,
                NetworkSourceMutationPhase.CredentialPersisted,
            ),
        )
        assertTrue(
            isValidNetworkSourceMutationPhaseTransition(
                NetworkSourceMutationKind.Save,
                NetworkSourceMutationPhase.InventoryInvalidated,
                NetworkSourceMutationPhase.RuntimeInvalidated,
            ),
        )
        assertFalse(
            isValidNetworkSourceMutationPhaseTransition(
                NetworkSourceMutationKind.Save,
                NetworkSourceMutationPhase.Prepared,
                NetworkSourceMutationPhase.SourcePersisted,
            ),
        )
    }

    @Test
    fun removePhasesCannotRegressOrSkipSourceRemoval() {
        assertTrue(
            isValidNetworkSourceMutationPhaseTransition(
                NetworkSourceMutationKind.Remove,
                NetworkSourceMutationPhase.Prepared,
                NetworkSourceMutationPhase.RuntimeInvalidated,
            ),
        )
        assertTrue(
            isValidNetworkSourceMutationPhaseTransition(
                NetworkSourceMutationKind.Remove,
                NetworkSourceMutationPhase.SourceRemoved,
                NetworkSourceMutationPhase.InventoryRemoved,
            ),
        )
        assertFalse(
            isValidNetworkSourceMutationPhaseTransition(
                NetworkSourceMutationKind.Remove,
                NetworkSourceMutationPhase.Prepared,
                NetworkSourceMutationPhase.InventoryRemoved,
            ),
        )
    }

    @Test
    fun saveRecoveryRequiresTheWholePersistedSourceConfiguration() {
        val saved = source(username = "new-user")
        val marker = NetworkSourceMutationMarker(
            sourceId = saved.id,
            kind = NetworkSourceMutationKind.Save,
            previousCredentialKey = saved.credentialKey,
            newCredentialKey = saved.credentialKey,
            previousLocationFingerprint = NetworkSourceIdentity.locationFingerprint(saved),
            newLocationFingerprint = NetworkSourceIdentity.locationFingerprint(saved),
            newConfigurationFingerprint = NetworkSourceIdentity.configurationFingerprint(saved),
            phase = NetworkSourceMutationPhase.CredentialPersisted,
        )

        assertTrue(marker.matchesCommittedSource(saved))
        assertFalse(marker.matchesCommittedSource(saved.copy(username = "old-user")))
        assertFalse(marker.matchesCommittedSource(saved.copy(enabled = false)))
    }

    @Test
    fun rollbackKeepsAKeyStillOwnedByThePreviousSource() {
        val sharedKeyMarker = marker(previousCredentialKey = "same", newCredentialKey = "same")
        val rotatedKeyMarker = marker(previousCredentialKey = "old", newCredentialKey = "new")

        assertEquals(null, sharedKeyMarker.credentialKeyToDiscardOnRollback())
        assertEquals("new", rotatedKeyMarker.credentialKeyToDiscardOnRollback())
    }

    private fun marker(
        previousCredentialKey: String,
        newCredentialKey: String,
    ) = NetworkSourceMutationMarker(
        sourceId = "source-a",
        kind = NetworkSourceMutationKind.Save,
        previousCredentialKey = previousCredentialKey,
        newCredentialKey = newCredentialKey,
        previousLocationFingerprint = null,
        newLocationFingerprint = null,
        phase = NetworkSourceMutationPhase.CredentialPersisted,
    )

    private fun source(username: String) = NetworkLibrarySource(
        id = "source-a",
        name = "Source A",
        protocol = NetworkLibraryProtocol.Smb,
        server = "server",
        shareOrPath = "share",
        username = username,
        credentialKey = "key",
    )
}
