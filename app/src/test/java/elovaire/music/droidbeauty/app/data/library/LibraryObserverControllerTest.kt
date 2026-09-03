package elovaire.music.droidbeauty.app.data.library

import android.os.FileObserver
import android.net.TestUri
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryObserverControllerTest {
    @Test
    fun selfMutationLedgerMatchesOnlyTheExpectedTarget() {
        val uri = "content://media/external/audio/media/7"

        assertTrue(expectedMutationTargetMatches(null, uri, null, uri))
        assertFalse(expectedMutationTargetMatches(null, uri, null, "content://media/external/audio/media/8"))
        assertTrue(expectedMutationTargetMatches("/music/track.mp3", null, "/music/track.mp3", null))
        assertFalse(expectedMutationTargetMatches("/music/track.mp3", null, "/music/other.mp3", null))
    }

    @Test
    fun deletedAudioIsReportedEvenWhenThePathNoLongerExists() {
        assertTrue(
            shouldNotifyForObservedDirectoryEvent(
                event = FileObserver.DELETE,
                changedFileExists = false,
                changedFileIsDirectory = false,
                changedFileHasSupportedAudioExtension = false,
            ),
        )
    }

    @Test
    fun completedFileWritesAreNotCoalescedWithTheirCreateEvent() {
        assertFalse(shouldCoalesceObservedDirectoryEvent(FileObserver.CLOSE_WRITE))
        assertTrue(shouldCoalesceObservedDirectoryEvent(FileObserver.CREATE))
    }

    @Test
    fun mediaStoreObserverUrisIncludeAggregateAndKnownVolumes() {
        val aggregate = TestUri("content://media/external/audio/media")
        val primary = TestUri("content://media/external_primary/audio/media")
        val secondary = TestUri("content://media/external_secondary/audio/media")
        val uris = mediaStoreObserverUris(aggregate, listOf(primary, secondary, primary))

        assertTrue(uris.contains(aggregate))
        assertTrue(uris.contains(primary))
        assertTrue(uris.contains(secondary))
    }

    @Test
    fun unrelatedExistingFileModificationIsIgnored() {
        assertFalse(
            shouldNotifyForObservedDirectoryEvent(
                event = FileObserver.MODIFY,
                changedFileExists = true,
                changedFileIsDirectory = false,
                changedFileHasSupportedAudioExtension = false,
            ),
        )
    }

    @Test
    fun observerIdentityIsIndependentOfDirectoryEnumerationOrder() {
        assertEquals(
            listOf("/music/a", "/music/b"),
            observerTreeIdentity(listOf("/music/b", "/music/a")),
        )
    }

    @Test
    fun recentObservedPathCacheIsBoundedAndEvictsOldestEntries() {
        val paths = linkedMapOf<String, Long>()
        repeat(513) { index -> paths["/music/$index.mp3"] = index.toLong() }

        trimRecentObservedPaths(paths, maxEntries = 512)

        assertEquals(512, paths.size)
        assertFalse(paths.containsKey("/music/0.mp3"))
        assertTrue(paths.containsKey("/music/512.mp3"))
    }
}
