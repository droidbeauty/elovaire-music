package elovaire.music.droidbeauty.app.data.library

import android.os.FileObserver
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryObserverControllerTest {
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
