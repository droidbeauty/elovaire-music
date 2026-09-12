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

    fun suppressingSongIds(): Set<Long> {
        val pending = pendingSongIds.value
        val confirmed = confirmedSongIds.value
        return when {
            pending.isEmpty() -> confirmed
            confirmed.isEmpty() -> pending
            else -> pending + confirmed
        }
    }

    fun confirmDeletedSongs(ids: Collection<Long>) {
        if (ids.isNotEmpty()) confirmedSongIds.update { it + ids }
    }

    fun retainConfirmedSongsStillIn(scannedSongIds: Set<Long>) {
        if (confirmedSongIds.value.isEmpty()) return
        confirmedSongIds.update { tombstones ->
            if (tombstones.all(scannedSongIds::contains)) {
                tombstones
            } else {
                tombstones.filterTo(linkedSetOf(), scannedSongIds::contains)
            }
        }
    }

    fun clear() {
        pendingSongIds.value = emptySet()
        pendingAlbumIds.value = emptySet()
        confirmedSongIds.value = emptySet()
    }
}
