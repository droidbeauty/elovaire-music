package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SafTreeLibraryScannerInstrumentedTest {
    @Test
    fun scan_rejectsRevokedPersistedPermissionWithoutPublishingEmptySource() {
        val selection = LibraryFolderSelection(
            uri = Uri.parse("content://elovaire.test/tree/music"),
            path = "content://elovaire.test/tree/music",
            displayName = "Music",
        )

        val failure = assertThrows(SafProviderUnavailableException::class.java) {
            runBlocking {
                SafTreeLibraryScanner(ApplicationProvider.getApplicationContext()).scan(listOf(selection))
            }
        }

        assertEquals("elovaire.test", failure.authority)
        assertEquals("validate-persisted-permission", failure.operation)
    }
}
