package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song

internal object LibrarySnapshotAssembler {
    fun assemble(songs: List<Song>): LibrarySnapshot {
        val canonicalSongs = LibrarySongDuplicateResolver.dedupeLoadedSnapshotSongs(songs)
        return LibrarySnapshot(
            songs = canonicalSongs,
            albums = buildAlbumsFromSongs(canonicalSongs),
        )
    }
}
