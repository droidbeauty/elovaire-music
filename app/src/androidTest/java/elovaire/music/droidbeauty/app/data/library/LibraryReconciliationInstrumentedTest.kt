package elovaire.music.droidbeauty.app.data.library

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryReconciliationInstrumentedTest {
    @Test
    fun aResetVolumeRequiresFullReconciliationEvenIfAnotherVolumeAdvanced() {
        val cached = LibraryMediaStoreSyncState("folders", listOf(
            LibraryMediaStoreVolumeSyncState("external_primary", "v1", 10L),
            LibraryMediaStoreVolumeSyncState("secondary", "v1", 20L),
        ))
        listOf(
            cached.volumes[1].copy(version = "v2", generation = 1L),
            cached.volumes[1].copy(generation = 1L),
            cached.volumes[1].copy(volumeName = "replacement"),
        ).forEach { changed ->
            val current = cached.copy(volumes = listOf(cached.volumes[0].copy(generation = 11L), changed))
            assertEquals(LibrarySyncDecision.FullScan, decideLibrarySync(cached, current))
            assertEquals(LibrarySyncDecision.FullScan, decideLibrarySync(
                cached.copy(volumes = cached.volumes.reversed()), current.copy(volumes = current.volumes.reversed()),
            ))
        }
    }

    @Test
    fun fullRefreshCannotBeNarrowedByAnAlreadyQueuedDelta() {
        listOf(
            LibraryRefreshRequest(mediaStoreGenerationFloor = 10L),
            LibraryRefreshRequest(mediaStoreGenerationFloors = mapOf("external_primary" to 10L)),
        ).forEach { delta ->
            listOf(delta.mergedWith(LibraryRefreshRequest()), LibraryRefreshRequest().mergedWith(delta)).forEach { result ->
                assertNull(result.mediaStoreGenerationFloor)
                assertTrue(result.mediaStoreGenerationFloors.isEmpty())
            }
        }
    }

    @Test
    fun sourceOnlyRefreshDoesNotClaimToHaveReconciledMediaStore() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val coordinator = LibraryScanCoordinator(
            localScanner = MediaStoreScanner(context),
            safScanner = SafTreeLibraryScanner(context),
            networkScannerProvider = { error("No network source was selected") },
        )
        val base = LibrarySnapshot(emptyList(), emptyList())
        val networkOnly = coordinator.scanWithStatus(
            enrichMetadata = false, targetedNetworkSourceIds = emptySet(), baseSnapshot = base,
        )
        val safOnly = coordinator.scanWithStatus(
            enrichMetadata = false, targetedSafTreeIds = emptySet(), baseSnapshot = base,
        )
        assertFalse(networkOnly.mediaStoreReconciled)
        assertFalse(safOnly.mediaStoreReconciled)
    }
}
