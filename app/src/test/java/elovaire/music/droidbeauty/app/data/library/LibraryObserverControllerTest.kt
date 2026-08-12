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
}
