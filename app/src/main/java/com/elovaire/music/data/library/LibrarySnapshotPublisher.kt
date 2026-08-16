package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale

internal class LibrarySnapshotPublisher(
    private val publish: (LibraryContentState) -> Unit,
    private val currentState: () -> LibraryContentState,
) {
    private var indexedSongs: List<Song>? = null
    private var albumSongPositions = emptyMap<Long, List<Int>>()
    private var albumPositions = emptyMap<Long, Int>()

    fun prepareSongs(songs: List<Song>): LibrarySnapshot {
        return LibrarySnapshotAssembler.assemble(songs)
    }

    fun publishSnapshot(
        snapshot: LibrarySnapshot,
        removingSongIds: Set<Long>,
        removingAlbumIds: Set<Long>,
    ): LibraryContentState {
        val nextState = stateForSnapshot(snapshot, removingSongIds, removingAlbumIds)
        publishState(nextState)
        return nextState
    }

    fun stateForSnapshot(
        snapshot: LibrarySnapshot,
        removingSongIds: Set<Long>,
        removingAlbumIds: Set<Long>,
    ): LibraryContentState {
        return LibraryContentState(
            songs = snapshot.songs,
            albums = snapshot.albums,
            removingSongIds = removingSongIds,
            removingAlbumIds = removingAlbumIds,
        )
    }

    fun publishState(nextState: LibraryContentState) {
        if (currentState() != nextState) {
            publish(nextState)
        }
    }

    fun publishSongs(
        songs: List<Song>,
        removingSongIds: Set<Long>,
        removingAlbumIds: Set<Long>,
    ): LibraryContentState {
        return publishSnapshot(
            snapshot = prepareSongs(songs),
            removingSongIds = removingSongIds,
            removingAlbumIds = removingAlbumIds,
        )
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
        updateIndices(current)
        val replacements = editedSongs.associateBy(Song::id)
        val replacementsByIdentity = editedSongs.associateBy(MediaIdentityResolver::stableKey)
        val replacementPositions = current.songs.mapIndexedNotNull { position, song ->
            if (song.id in replacements || MediaIdentityResolver.stableKey(song) in replacementsByIdentity) {
                position
            } else {
                null
            }
        }
        if (replacementPositions.isEmpty()) return current
        val updatedSongs = current.songs.toMutableList()
        replacementPositions.forEach { position ->
            val currentSong = current.songs[position]
            updatedSongs[position] = replacements[currentSong.id]
                ?: replacementsByIdentity.getValue(MediaIdentityResolver.stableKey(currentSong))
        }

        val affectedAlbumIds = buildSet {
            replacementPositions.forEach { position -> add(current.songs[position].albumId) }
            replacementPositions.forEach { position -> add(updatedSongs[position].albumId) }
        }
        val affectedPositions = buildSet {
            affectedAlbumIds.forEach { albumId ->
                addAll(albumSongPositions[albumId].orEmpty())
            }
            addAll(replacementPositions)
        }
        val rebuiltAlbums = affectedAlbumIds.flatMap { albumId ->
            buildAlbumsFromSongs(
                affectedPositions.map(updatedSongs::get).filter { it.albumId == albumId },
            )
        }
        val updatedAlbums = current.albums.toMutableList()
        albumPositions.filterKeys(affectedAlbumIds::contains).values.sortedDescending().forEach(updatedAlbums::removeAt)
        rebuiltAlbums.sortedWith(ALBUM_COMPARATOR).forEach { album ->
            val insertionPoint = updatedAlbums.binarySearch(album, ALBUM_COMPARATOR).let { index ->
                if (index >= 0) index else -index - 1
            }
            updatedAlbums.add(insertionPoint, album)
        }
        val nextState = LibraryContentState(
            songs = updatedSongs,
            albums = updatedAlbums,
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

    private fun updateIndices(state: LibraryContentState) {
        if (indexedSongs === state.songs) return
        indexedSongs = state.songs
        albumSongPositions = state.songs.mapIndexed { index, song -> song.albumId to index }
            .groupBy({ it.first }, { it.second })
        albumPositions = state.albums.mapIndexed { index, album -> album.id to index }.toMap()
    }

    private companion object {
        val ALBUM_COMPARATOR: Comparator<Album> = compareBy(
            { it.artist.lowercase(Locale.ROOT) },
            { it.title.lowercase(Locale.ROOT) },
        )
    }
}
