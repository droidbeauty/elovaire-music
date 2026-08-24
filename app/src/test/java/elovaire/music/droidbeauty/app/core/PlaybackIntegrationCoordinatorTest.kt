package elovaire.music.droidbeauty.app.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackIntegrationCoordinatorTest {
    @Test
    fun partialLibraryResolutionDoesNotQualifySessionForRestore() {
        assertFalse(isPlaybackSessionFullyResolved(listOf(1L, 2L), listOf(1L)))
    }

    @Test
    fun allPersistedIdsMustBeResolvedBeforeRestore() {
        assertTrue(isPlaybackSessionFullyResolved(listOf(1L, 2L), listOf(2L, 1L, 3L)))
    }
}
