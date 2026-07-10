package elovaire.music.droidbeauty.app.data.mutation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaMutationJournalTest {
    @Test
    fun validPipelineTransitions_areAccepted() {
        assertTrue(isValidMutationTransition(MediaMutationStatus.Created, MediaMutationStatus.PreflightPassed))
        assertTrue(isValidMutationTransition(MediaMutationStatus.PreflightPassed, MediaMutationStatus.TempWritten))
        assertTrue(isValidMutationTransition(MediaMutationStatus.TempWritten, MediaMutationStatus.TempVerified))
        assertTrue(isValidMutationTransition(MediaMutationStatus.TempVerified, MediaMutationStatus.Committed))
        assertTrue(isValidMutationTransition(MediaMutationStatus.Committed, MediaMutationStatus.PersistedVerified))
        assertTrue(isValidMutationTransition(MediaMutationStatus.PersistedVerified, MediaMutationStatus.Completed))
    }

    @Test
    fun terminalAndSkippedTransitions_areRejected() {
        assertFalse(isValidMutationTransition(MediaMutationStatus.Created, MediaMutationStatus.Completed))
        assertFalse(isValidMutationTransition(MediaMutationStatus.Completed, MediaMutationStatus.Failed))
        assertFalse(isValidMutationTransition(MediaMutationStatus.Cancelled, MediaMutationStatus.PreflightPassed))
        assertFalse(isValidMutationTransition(MediaMutationStatus.TempWritten, MediaMutationStatus.TempWritten))
    }

    @Test
    fun failureTransitions_remainAvailableBeforeCompletion() {
        assertTrue(isValidMutationTransition(MediaMutationStatus.PreflightPassed, MediaMutationStatus.Failed))
        assertTrue(isValidMutationTransition(MediaMutationStatus.Committed, MediaMutationStatus.NeedsRepair))
        assertTrue(isValidMutationTransition(MediaMutationStatus.Failed, MediaMutationStatus.NeedsRepair))
    }
}
