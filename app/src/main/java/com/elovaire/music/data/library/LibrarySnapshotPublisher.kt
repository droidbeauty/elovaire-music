package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song

internal class LibrarySnapshotPublisher(
    private val publish: (LibraryContentState) -> Unit,
    private val currentState: () -> LibraryContentState,
) {
    fun publishSongs(
        songs: List<Song>,
        removingSongIds: Set<Long>,
        removingAlbumIds: Set<Long>,
    ): LibraryContentState {
        val snapshot = LibrarySnapshotAssembler.assemble(songs)
        val nextState = LibraryContentState(
            songs = snapshot.songs,
            albums = snapshot.albums,
            removingSongIds = removingSongIds,
            removingAlbumIds = removingAlbumIds,
        )
        if (currentState() != nextState) {
            publish(nextState)
        }
        return nextState
    }

    /**
     * Applies verified metadata changes without rebuilding unrelated albums.
     * The normal scan path still uses [publishSongs], while mutation results
     * already contain authoritative replacement Song instances.
     */
    fun patchSongs(
        editedSongs: List<Song>,
        removingSongIds: Set<Long>,
        removingAlbumIds: Set<Long>,
    ): LibraryContentState {
        if (editedSongs.isEmpty()) return currentState()
        val current = currentState()
        val replacements = editedSongs.associateBy(Song::id)
        val updatedSongs = current.songs.map { replacements[it.id] ?: it }
        if (updatedSongs == current.songs) return current

        val affectedAlbumIds = buildSet {
            current.songs.filter { it.id in replacements }.forEach { add(it.albumId) }
            editedSongs.forEach { add(it.albumId) }
        }
        val unchangedAlbums = current.albums.filterNot { it.id in affectedAlbumIds }
        val rebuiltAlbums = buildAlbumsFromSongs(updatedSongs.filter { it.albumId in affectedAlbumIds })
        val nextState = LibraryContentState(
            songs = updatedSongs,
            albums = (unchangedAlbums + rebuiltAlbums).sortedWith(
                compareBy(
                    { it.artist.lowercase(java.util.Locale.ROOT) },
                    { it.title.lowercase(java.util.Locale.ROOT) },
                ),
            ),
            removingSongIds = removingSongIds,
            removingAlbumIds = removingAlbumIds,
        )
        if (current != nextState) publish(nextState)
        return nextState
    }

    fun snapshotOf(state: LibraryContentState): LibrarySnapshot {
        return LibrarySnapshot(
            songs = state.songs,
            albums = state.albums,
        )
    }
}
