package elovaire.music.droidbeauty.app.data.library

import android.provider.MediaStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class MediaStoreDeltaIdentityTest {
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
