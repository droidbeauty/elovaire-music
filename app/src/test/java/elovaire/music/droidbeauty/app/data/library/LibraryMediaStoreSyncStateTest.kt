package elovaire.music.droidbeauty.app.data.library

import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryMediaStoreSyncStateTest {
    @Test
    fun decideLibrarySync_reusesCacheWhenStateMatches() {
        val state = syncState(generation = 10L)

        assertEquals(LibrarySyncDecision.ReuseCached, decideLibrarySync(state, state))
    }

    @Test
    fun decideLibrarySync_usesIncrementalScanWhenGenerationAdvances() {
        val cached = syncState(generation = 10L)
        val current = syncState(generation = 11L)

        assertEquals(LibrarySyncDecision.IncrementalScan, decideLibrarySync(cached, current))
    }

    @Test
    fun decideLibrarySync_requiresFullScanWhenGenerationGoesBackwards() {
        val cached = syncState(generation = 11L)
        val current = syncState(generation = 10L)

        assertEquals(LibrarySyncDecision.FullScan, decideLibrarySync(cached, current))
    }

    @Test
    fun decideLibrarySync_requiresFullScanWhenFilterChanges() {
        val cached = syncState(filterFingerprint = "folders-a")
        val current = syncState(filterFingerprint = "folders-b")

        assertEquals(LibrarySyncDecision.FullScan, decideLibrarySync(cached, current))
    }

    private fun syncState(
        filterFingerprint: String = "folders-a",
        generation: Long = 10L,
    ) = LibraryMediaStoreSyncState(
        filterFingerprint = filterFingerprint,
        volumes = listOf(
            LibraryMediaStoreVolumeSyncState(
                volumeName = "external",
                version = "1",
                generation = generation,
            ),
        ),
    )
}
