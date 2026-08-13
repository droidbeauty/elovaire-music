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
    private var songPositions = emptyMap<Long, Int>()
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
        val replacementPositions = replacements.keys.mapNotNull(songPositions::get)
        if (replacementPositions.isEmpty()) return current
        val updatedSongs = current.songs.toMutableList()
        replacementPositions.forEach { position ->
            updatedSongs[position] = replacements.getValue(current.songs[position].id)
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
        songPositions = state.songs.mapIndexed { index, song -> song.id to index }.toMap()
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
