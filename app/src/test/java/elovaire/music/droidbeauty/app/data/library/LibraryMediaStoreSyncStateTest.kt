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
    fun decideLibrarySync_rescansEmptyCacheEvenWhenStateMatches() {
        val state = syncState(generation = 10L)

        assertEquals(
            LibrarySyncDecision.FullScan,
            decideLibrarySync(
                cached = state,
                current = state,
                cachedSongCount = 0,
            ),
        )
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

    @Test
    fun startupSyncAlwaysReconcilesSelectedSafTrees() {
        val state = syncState()

        assertEquals(
            LibrarySyncDecision.FullScan,
            decideLibrarySyncAtStartup(
                cached = state,
                current = state,
                cachedSongCount = 10,
                hasSafSelections = true,
            ),
        )
    }

    @Test
    fun foregroundReconcileSkipsUnchangedMediaStoreWhenNoOtherSourceNeedsWork() {
        val state = syncState()

        assertEquals(
            null,
            decideForegroundReconcile(
                cached = state,
                current = state,
                cachedSongCount = 10,
                hasSafSelections = false,
                staleNetworkSourceIds = emptySet(),
            ),
        )
    }

    @Test
    fun foregroundReconcileUsesGenerationDeltaWhenMediaStoreChanged() {
        val cached = syncState(generation = 10L)
        val current = syncState(generation = 11L)

        assertEquals(
            LibraryRefreshRequest(mediaStoreGenerationFloor = 10L),
            decideForegroundReconcile(
                cached = cached,
                current = current,
                cachedSongCount = 10,
                hasSafSelections = false,
                staleNetworkSourceIds = emptySet(),
            ),
        )
    }

    @Test
    fun foregroundReconcileKeepsFullScanForIndependentSources() {
        val state = syncState()

        assertEquals(
            LibraryRefreshRequest(),
            decideForegroundReconcile(
                cached = state,
                current = state,
                cachedSongCount = 10,
                hasSafSelections = true,
                staleNetworkSourceIds = emptySet(),
            ),
        )
    }

    @Test
    fun foregroundReconcileTargetsOnlyStaleNetworkSources() {
        val state = syncState()

        assertEquals(
            LibraryRefreshRequest(targetedNetworkSourceIds = setOf("nas-a")),
            decideForegroundReconcile(
                cached = state,
                current = state,
                cachedSongCount = 10,
                hasSafSelections = false,
                staleNetworkSourceIds = setOf("nas-a"),
            ),
        )
    }

    @Test
    fun foregroundReconcileDoesNotMaskMissingMediaStoreStateWithNetworkRefresh() {
        assertEquals(
            LibraryRefreshRequest(),
            decideForegroundReconcile(
                cached = null,
                current = null,
                cachedSongCount = 0,
                hasSafSelections = false,
                staleNetworkSourceIds = setOf("nas-a"),
            ),
        )
    }

    @Test
    fun foregroundReconcileDoesNotMaskMediaStoreDeltaWithNetworkRefresh() {
        assertEquals(
            LibraryRefreshRequest(),
            decideForegroundReconcile(
                cached = syncState(generation = 10L),
                current = syncState(generation = 11L),
                cachedSongCount = 10,
                hasSafSelections = false,
                staleNetworkSourceIds = setOf("nas-a"),
            ),
        )
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
