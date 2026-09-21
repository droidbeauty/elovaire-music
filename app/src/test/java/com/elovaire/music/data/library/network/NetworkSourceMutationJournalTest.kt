package elovaire.music.droidbeauty.app.data.library.network

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
}
