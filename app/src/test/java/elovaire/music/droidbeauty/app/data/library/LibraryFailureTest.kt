package elovaire.music.droidbeauty.app.data.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryFailureTest {
    @Test
    fun safProviderFailureRetainsProviderContext() {
        val cause = IllegalStateException("provider process died")

        val failure = SafProviderUnavailableException(
            authority = "example.documents",
            operation = "query children",
            cause = cause,
        ).toLibraryScanFailure("refresh")

        assertTrue(failure is LibraryFailure.SafProviderFailure)
        failure as LibraryFailure.SafProviderFailure
        assertEquals("example.documents", failure.authority)
        assertEquals("query children", failure.operation)
        assertSame(cause, failure.cause)
    }
}
