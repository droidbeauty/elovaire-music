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
        val nextState = LibraryContentState(
            songs = songs,
            albums = buildAlbumsFromSongs(songs),
            removingSongIds = removingSongIds,
            removingAlbumIds = removingAlbumIds,
        )
        if (currentState() != nextState) {
            publish(nextState)
        }
        return nextState
    }

    fun snapshotOf(state: LibraryContentState): LibrarySnapshot {
        return LibrarySnapshot(
            songs = state.songs,
            albums = state.albums,
        )
    }
}
