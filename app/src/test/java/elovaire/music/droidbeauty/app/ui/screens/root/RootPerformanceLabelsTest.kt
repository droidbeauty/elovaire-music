package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RootPerformanceLabelsTest {
    @Test
    fun rootPerformanceRouteLabel_mapsTopLevelAndDetailRoutes() {
        assertNull(rootPerformanceRouteLabel(null))
        assertEquals("home", rootPerformanceRouteLabel(HOME_ROUTE))
        assertEquals("album_detail", rootPerformanceRouteLabel("album/42"))
        assertEquals("playlist_detail", rootPerformanceRouteLabel("playlist/7"))
        assertEquals("library_collection", rootPerformanceRouteLabel("$LIBRARY_COLLECTION_ROUTE/Albums"))
        assertEquals("tag_editor", rootPerformanceRouteLabel("$ALBUM_TAG_EDITOR_ROUTE/{albumId}"))
        assertEquals("other", rootPerformanceRouteLabel("unexpected"))
    }

    @Test
    fun rootPerformanceLibraryLabel_tracksScanState() {
        assertEquals("library_loading", rootPerformanceLibraryLabel(LibraryUiState(isLoading = true)))
        assertEquals("scan_active", rootPerformanceLibraryLabel(LibraryUiState(scanProgress = 0.5f)))
        assertEquals("scan_idle", rootPerformanceLibraryLabel(LibraryUiState(scanProgress = 1f)))
    }

    @Test
    fun rootPerformanceInteractionLabel_tracksPlayback() {
        assertEquals("playback_progress_active", rootPerformanceInteractionLabel(true))
        assertEquals("idle", rootPerformanceInteractionLabel(false))
    }
}
