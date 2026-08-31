package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SafTreeLibraryScannerInstrumentedTest {
    @Test
    fun providerCursorExtras_preserveLoadingAndErrorSignals() {
        val cursor = MatrixCursor(emptyArray<String>()).apply {
            setExtras(
                Bundle().apply {
                    putBoolean(DocumentsContract.EXTRA_LOADING, true)
                    putString(DocumentsContract.EXTRA_ERROR, "provider unavailable")
                },
            )
        }

        cursor.use {
            assertEquals(
                SafProviderCursorStatus(loading = true, error = true),
                safProviderCursorStatus(it.extras),
            )
        }
    }

    @Test
    fun providerCursorExtras_infoOnlyDoesNotMakeAQueryUnavailable() {
        val cursor = MatrixCursor(emptyArray<String>()).apply {
            setExtras(
                Bundle().apply {
                    putString(DocumentsContract.EXTRA_INFO, "results are cached")
                },
            )
        }

        cursor.use {
            assertEquals(
                SafProviderCursorStatus(loading = false, error = false),
                safProviderCursorStatus(it.extras),
            )
        }
    }

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
