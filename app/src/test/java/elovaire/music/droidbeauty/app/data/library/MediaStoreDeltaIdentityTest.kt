package elovaire.music.droidbeauty.app.data.library

import android.provider.MediaStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class MediaStoreDeltaIdentityTest {
    @Test
    fun scopedMediaStoreSelectionUsesBoundFolderPrefixes() {
        val scoped = MediaStoreAudioQuery.scopedSelection(setOf("Music/Live_100%"))

        assertTrue(scoped.selection.contains("substr(relative_path, 1, length(?)) = ?"))
        assertArrayEquals(
            arrayOf("Music/Live_100%", "Music/Live_100%/", "Music/Live_100%/"),
            scoped.arguments,
        )
    }

    @Test
    fun scopedMediaStoreSelectionLetsSqlCountUnicodePathCharacters() {
        val scoped = MediaStoreAudioQuery.scopedSelection(setOf("Music/🎵"))

        assertTrue(scoped.selection.contains("length(?)"))
        assertArrayEquals(
            arrayOf("Music/🎵", "Music/🎵/", "Music/🎵/"),
            scoped.arguments,
        )
    }

    @Test
    fun providerProjectionFallbackKeepsTheSameBoundLibraryScope() = runTest {
        val calls = mutableListOf<Pair<String?, List<String>?>>()
        val root = "Music/Elovaire%"

        try {
            MediaStoreAudioQuery.query(
                MediaStoreAudioQuery.QueryExecutor { _, selection, arguments, _ ->
                    calls += selection to arguments?.toList()
                    throw IllegalArgumentException("fixture provider rejects the projection")
                },
                setOf(root),
            )
            fail("Both provider projections should fail for this fixture")
        } catch (_: MediaStoreQueryUnavailableException) {
            // The assertion below checks both failed attempts retained the bounded selection.
        }

        assertEquals(2, calls.size)
        assertTrue(calls.all { (selection, _) -> selection?.contains("substr(relative_path, 1, length(?))") == true })
        assertTrue(calls.all { (_, arguments) ->
            arguments == listOf(root, "$root/", "$root/")
        })
    }

    @Test
    fun emptyMediaStoreScopeCannotWidenToWholeProvider() {
        val scoped = MediaStoreAudioQuery.scopedSelection(emptySet())

        assertTrue(scoped.selection.endsWith("AND 0"))
        assertNull(scoped.arguments)
    }

    @Test
    fun compatibilityProjectionRetainsVolumeScopedMediaIdentity() {
        assertEquals(
            true,
            MediaStore.MediaColumns.VOLUME_NAME in MediaStoreAudioQuery.compatibilityProjection,
        )
    }

    @Test
    fun providerFailureFallsBackButCancellationAndPermissionFailuresPropagate() = runTest {
        assertEquals(null, mediaStoreDeltaIdentityOrFallback<String> { throw IllegalStateException("provider") })
        try {
            mediaStoreDeltaIdentityOrFallback<String> { throw CancellationException("scan cancelled") }
            fail("Cancellation must propagate")
        } catch (cancelled: CancellationException) {
            assertEquals("scan cancelled", cancelled.message)
        }
        try {
            mediaStoreDeltaIdentityOrFallback<String> { throw SecurityException("permission revoked") }
            fail("Permission failures must propagate")
        } catch (failure: SecurityException) {
            assertEquals("permission revoked", failure.message)
        }
    }
}
