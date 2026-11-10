package elovaire.music.droidbeauty.app.core

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.library.LibraryRefreshRequest
import elovaire.music.droidbeauty.app.data.playback.PlaybackQueueMetadataRefresher
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RuntimeRefinementInstrumentedTest {
    @Test
    fun pendingReleasePreservesActionAndCleanupFailures() {
        val actionFailure = IllegalStateException("action")
        val releaseFailure = IllegalStateException("release")
        lateinit var runtime: AppRuntimeCoordinator
        runtime = AppRuntimeCoordinator(
            startPlaybackAction = {},
            startAction = {},
            memoryPressureAction = { runtime.release(); throw actionFailure },
            releaseAction = { throw releaseFailure },
        )
        val thrown = runCatching { runtime.onMemoryPressure(MemoryPressure.Critical) }.exceptionOrNull()
        assertSame(actionFailure, thrown)
        assertEquals(listOf(releaseFailure), thrown?.suppressed?.toList())
        assertEquals(AppRuntimePhase.Released, runtime.currentPhase())
    }

    @Test
    fun mergedRefreshRetainsOnlyCompatibleGenerationFloors() {
        val first = LibraryRefreshRequest(mediaStoreGenerationFloors = mapOf("primary" to 20L))
        val second = LibraryRefreshRequest(mediaStoreGenerationFloors = mapOf("primary" to 10L, "card" to 7L))
        assertEquals(mapOf("primary" to 10L, "card" to 7L), first.mergedWith(second).mediaStoreGenerationFloors)
        val full = first.mergedWith(LibraryRefreshRequest())
        assertNull(full.mediaStoreGenerationFloor)
        assertEquals(emptyMap<String, Long>(), full.mediaStoreGenerationFloors)
    }

    @Test
    fun reconciliationUpdatesTheMatchingQueueItem() {
        val original = Song(
            id = 1L, title = "Track", isExplicit = false, artist = "Artist", album = "Album",
            releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null,
            fileName = "track.mp3", albumId = 2L, durationMs = 10_000L, trackNumber = 1,
            discNumber = 1, dateAddedSeconds = 1L,
            uri = Uri.parse("content://media/external_primary/audio/media/1"), artUri = null,
        )
        val updated = original.copy(title = "Updated")
        val result = PlaybackQueueMetadataRefresher().reconcileQueue(listOf(original), listOf(updated))
        assertEquals(listOf(updated), result?.queue)
        assertEquals(listOf(0), result?.retainedOriginalIndices)
        assertEquals(emptyList<Int>(), result?.removedOriginalIndices)
    }
}
