package elovaire.music.droidbeauty.app.data.playback.library

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaLibraryRequestPolicyTest {
    @Test
    fun pageBoundsRejectUnboundedExternalRequests() {
        assertTrue(MediaLibraryRequestPolicy.acceptsPage(0, 1))
        assertTrue(MediaLibraryRequestPolicy.acceptsPage(4, MediaLibraryRequestPolicy.MAX_PAGE_SIZE))
        assertFalse(MediaLibraryRequestPolicy.acceptsPage(-1, 20))
        assertFalse(MediaLibraryRequestPolicy.acceptsPage(0, 0))
        assertFalse(MediaLibraryRequestPolicy.acceptsPage(0, MediaLibraryRequestPolicy.MAX_PAGE_SIZE + 1))
    }

    @Test
    fun searchBoundsRejectControlCharactersAndOversizedQueries() {
        assertTrue(MediaLibraryRequestPolicy.acceptsSearchQuery("artist title"))
        assertTrue(MediaLibraryRequestPolicy.acceptsSearchQuery("line one\nline two"))
        assertFalse(MediaLibraryRequestPolicy.acceptsSearchQuery("artist\u0000title"))
        assertFalse(
            MediaLibraryRequestPolicy.acceptsSearchQuery(
                "a".repeat(MediaLibraryRequestPolicy.MAX_SEARCH_QUERY_LENGTH + 1),
            ),
        )
    }

    @Test
    fun startPositionBoundsRejectExtremeExternalRequests() {
        assertTrue(MediaLibraryRequestPolicy.acceptsStartPositionMs(0L))
        assertTrue(MediaLibraryRequestPolicy.acceptsStartPositionMs(90_000L))
        assertTrue(MediaLibraryRequestPolicy.acceptsStartPositionMs(androidx.media3.common.C.TIME_UNSET))
        assertFalse(MediaLibraryRequestPolicy.acceptsStartPositionMs(-1L))
        assertFalse(MediaLibraryRequestPolicy.acceptsStartPositionMs(Long.MAX_VALUE))
    }
}
