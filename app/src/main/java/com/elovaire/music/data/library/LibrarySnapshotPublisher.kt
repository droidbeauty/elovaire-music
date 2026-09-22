package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale

internal class LibrarySnapshotPublisher(
    private val publish: (LibraryContentState) -> Unit,
    private val currentState: () -> LibraryContentState,
) {
    private var indexedSongs: List<Song>? = null
    private var albumSongPositions = emptyMap<Long, List<Int>>()
    private var albumPositions = emptyMap<Long, Int>()
    private var songPositionsById = emptyMap<Long, Int>()
    private var songPositionsByStableKey = emptyMap<String, Int>()
    private var lastPatchChangeSet = LibraryChangeSet.Empty

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
            audiobooks = snapshot.audiobooks,
            removingSongIds = removingSongIds,
            removingAlbumIds = removingAlbumIds,
            contentRevision = snapshot.contentRevision.ifBlank {
                librarySongsContentRevision(snapshot.songs)
            },
            portableMediaIdentityRevision = MediaIdentityResolver.portableIdentityRevision(snapshot.songs),
        )
    }

    fun publishState(nextState: LibraryContentState) {
        if (!hasSamePublishedState(currentState(), nextState)) {
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
        publishResult: Boolean = true,
    ): LibraryContentState {
        lastPatchChangeSet = LibraryChangeSet.Empty
        if (editedSongs.isEmpty()) return currentState()
        val current = currentState()
        updateIndices(current)
        val replacements = editedSongs.associateBy(Song::id)
        val replacementsByIdentity = editedSongs.associateBy(MediaIdentityResolver::stableKey)
        val replacementPositions = editedSongs.asSequence()
            .mapNotNull { edited ->
                songPositionsById[edited.id]
                    ?: songPositionsByStableKey[MediaIdentityResolver.stableKey(edited)]
            }
            .distinct()
            .sorted()
            .toList()
        if (replacementPositions.isEmpty()) return current
        val updatedSongs = current.songs.toMutableList()
        replacementPositions.forEach { position ->
            val currentSong = current.songs[position]
            updatedSongs[position] = replacements[currentSong.id]
                ?: replacementsByIdentity.getValue(MediaIdentityResolver.stableKey(currentSong))
        }
        val canonicalUpdatedSongs = LibrarySnapshotAssembler.canonicalizeAlbumIdsAfterPatch(
            previousSongs = current.songs,
            updatedSongs = updatedSongs,
            changedPositions = replacementPositions,
        )
        val patches = replacementPositions.map { position ->
            LibrarySongPatch(
                before = current.songs[position],
                after = canonicalUpdatedSongs[position],
            )
        }
        lastPatchChangeSet = LibraryChangeSetCalculator.fromPatches(patches)

        val affectedAlbumIds = buildSet {
            replacementPositions.forEach { position -> add(current.songs[position].albumId) }
            replacementPositions.forEach { position -> add(canonicalUpdatedSongs[position].albumId) }
        }
        val affectedPositions = buildSet {
            affectedAlbumIds.forEach { albumId ->
                addAll(albumSongPositions[albumId].orEmpty())
            }
            addAll(replacementPositions)
        }
        val affectedSongsByAlbum = affectedPositions
            .asSequence()
            .map(canonicalUpdatedSongs::get)
            .filter { it.mediaKind == AudioMediaKind.Music }
            .groupBy(Song::albumId)
        val rebuiltAlbums = affectedAlbumIds.flatMap { albumId ->
            buildAlbumsFromSongs(affectedSongsByAlbum[albumId].orEmpty())
        }
        val updatedAlbums = current.albums.toMutableList()
        albumPositions.filterKeys(affectedAlbumIds::contains).values.sortedDescending().forEach(updatedAlbums::removeAt)
        rebuiltAlbums.sortedWith(ALBUM_COMPARATOR).forEach { album ->
            val insertionPoint = updatedAlbums.binarySearch(album, ALBUM_COMPARATOR).let { index ->
                if (index >= 0) index else -index - 1
            }
            updatedAlbums.add(insertionPoint, album)
        }
        val audiobookCatalog = if (
                audiobookContentChanged(
                    previousSongs = current.songs,
                    updatedSongs = canonicalUpdatedSongs,
                    changedPositions = replacementPositions,
                    globalCanonicalization = canonicalUpdatedSongs !== updatedSongs,
                ) ||
                (current.audiobooks.isEmpty() && canonicalUpdatedSongs.any { it.mediaKind == AudioMediaKind.Audiobook })
        ) {
            AudiobookCatalog.build(canonicalUpdatedSongs)
        } else {
            current.audiobooks
        }
        val nextState = LibraryContentState(
            songs = canonicalUpdatedSongs,
            albums = updatedAlbums,
            audiobooks = audiobookCatalog,
            removingSongIds = removingSongIds,
            removingAlbumIds = removingAlbumIds,
            contentRevision = libraryPatchedContentRevision(
                previousRevision = current.contentRevision,
                patches = patches,
            ),
            portableMediaIdentityRevision = MediaIdentityResolver.portableIdentityRevision(canonicalUpdatedSongs),
        )
        if (publishResult && !hasSamePublishedState(current, nextState)) publish(nextState)
        return nextState
    }

    fun takeLastPatchChangeSet(): LibraryChangeSet {
        return lastPatchChangeSet.also { lastPatchChangeSet = LibraryChangeSet.Empty }
    }

    fun snapshotOf(state: LibraryContentState): LibrarySnapshot {
        return LibrarySnapshot(
            songs = state.songs,
            albums = state.albums,
            audiobooks = state.audiobooks,
            contentRevision = state.contentRevision.ifBlank {
                librarySongsContentRevision(state.songs)
            },
        )
    }

    private fun hasSamePublishedState(
        current: LibraryContentState,
        next: LibraryContentState,
    ): Boolean {
        // The revision is calculated from the complete canonical song representation. Once it
        // is present, comparing it avoids walking every song and every derived album on each
        // publication. Removal markers remain independent transient state.
        return if (
            current.contentRevision.isNotBlank() &&
                current.contentRevision == next.contentRevision
        ) {
            current.removingSongIds == next.removingSongIds &&
                current.removingAlbumIds == next.removingAlbumIds
        } else {
            current == next
        }
    }

    private fun updateIndices(state: LibraryContentState) {
        if (indexedSongs === state.songs) return
        indexedSongs = state.songs
        val nextAlbumSongPositions = hashMapOf<Long, MutableList<Int>>()
        val nextSongPositionsById = hashMapOf<Long, Int>()
        val nextSongPositionsByStableKey = hashMapOf<String, Int>()
        state.songs.forEachIndexed { index, song ->
            nextAlbumSongPositions.getOrPut(song.albumId) { mutableListOf() }.add(index)
            nextSongPositionsById.putIfAbsent(song.id, index)
            nextSongPositionsByStableKey.putIfAbsent(MediaIdentityResolver.stableKey(song), index)
        }
        albumSongPositions = nextAlbumSongPositions
        songPositionsById = nextSongPositionsById
        songPositionsByStableKey = nextSongPositionsByStableKey
        albumPositions = state.albums.mapIndexed { index, album -> album.id to index }.toMap()
    }

    private fun audiobookContentChanged(
        previousSongs: List<Song>,
        updatedSongs: List<Song>,
        changedPositions: List<Int>,
        globalCanonicalization: Boolean,
    ): Boolean {
        val positions = if (globalCanonicalization) previousSongs.indices else changedPositions
        return positions.any { index ->
            val previous = previousSongs[index]
            val updated = updatedSongs[index]
            (previous.mediaKind == AudioMediaKind.Audiobook || updated.mediaKind == AudioMediaKind.Audiobook) &&
                previous != updated
        }
    }

    private companion object {
        val ALBUM_COMPARATOR: Comparator<Album> = compareBy(
            { it.artist.lowercase(Locale.ROOT) },
            { it.title.lowercase(Locale.ROOT) },
        )
    }
}
