package elovaire.music.droidbeauty.app.data.library

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryRefreshIntentTest {
    @Test
    fun userIntentPreservesFullRefreshSemantics() {
        val intent = LibraryRefreshIntent(
            reason = LibraryRefreshReason.UserInitiated,
            showLoadingIndicator = true,
        )

        assertTrue(intent.forceMediaIndex)
        assertTrue(intent.enrichMetadata)
        assertTrue(intent.showLoadingIndicator)
    }

    @Test
    fun permissionReconciliationDoesNotEscalateToFullScanOrEnrichment() {
        val intent = LibraryRefreshIntent(
            reason = LibraryRefreshReason.PermissionReconciliation,
            showLoadingIndicator = false,
        )

        assertFalse(intent.forceMediaIndex)
        assertFalse(intent.enrichMetadata)
        assertFalse(intent.showLoadingIndicator)
    }
}
