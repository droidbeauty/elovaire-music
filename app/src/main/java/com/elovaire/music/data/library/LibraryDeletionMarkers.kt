package elovaire.music.droidbeauty.app.data.library

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class LibraryDeletionMarkers {
    val pendingSongIds = MutableStateFlow<Set<Long>>(emptySet())
    val pendingAlbumIds = MutableStateFlow<Set<Long>>(emptySet())
    val confirmedSongIds = MutableStateFlow<Set<Long>>(emptySet())

    fun markSongs(ids: Collection<Long>) {
        if (ids.isNotEmpty()) pendingSongIds.update { it + ids }
    }

    fun markAlbums(ids: Collection<Long>) {
        if (ids.isNotEmpty()) pendingAlbumIds.update { it + ids }
    }

    fun clearSongs(ids: Collection<Long>) {
        if (ids.isNotEmpty()) pendingSongIds.update { it - ids.toSet() }
    }

    fun clearAlbums(ids: Collection<Long>) {
        if (ids.isNotEmpty()) pendingAlbumIds.update { it - ids.toSet() }
    }

    fun suppressingSongIds(): Set<Long> = pendingSongIds.value + confirmedSongIds.value

    fun confirmDeletedSongs(ids: Collection<Long>) {
        if (ids.isNotEmpty()) confirmedSongIds.update { it + ids }
    }

    fun retainConfirmedSongsStillIn(scannedSongIds: Set<Long>) {
        confirmedSongIds.update { tombstones ->
            tombstones.filterTo(linkedSetOf()) { it in scannedSongIds }
        }
    }

    fun clear() {
        pendingSongIds.value = emptySet()
        pendingAlbumIds.value = emptySet()
        confirmedSongIds.value = emptySet()
    }
}
