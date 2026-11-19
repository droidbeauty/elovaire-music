package elovaire.music.droidbeauty.app.data.library

import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryDeletionMarkersTest {
    @Test
    fun reconciliationKeepsConfirmationsAddedAfterTheScanStarted() {
        val markers = LibraryDeletionMarkers()
        markers.confirmDeletedSongs(listOf(1L, 2L))
        val observed = markers.confirmedSongIds.value
        markers.confirmDeletedSongs(listOf(3L))

        markers.clearConfirmedSongs(observed, observed - setOf(2L))

        assertEquals(setOf(1L, 2L, 3L), markers.confirmedSongIds.value)
        val current = markers.confirmedSongIds.value
        markers.clearConfirmedSongs(current, current - setOf(2L, 3L))
        assertEquals(setOf(2L, 3L), markers.confirmedSongIds.value)
    }
}
