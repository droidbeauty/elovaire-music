package elovaire.music.droidbeauty.app.data.playback.library

import elovaire.music.droidbeauty.app.domain.model.Playlist
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaLibraryInvalidationParentsTest {
    @Test
    fun oneCommittedUpdateCoalescesToAffectedParents() {
        val previous = MediaLibraryCommittedState(
            libraryRevision = "one",
            permissionGranted = true,
            favoriteSongIds = listOf(1L),
            playlists = listOf(Playlist(7L, "Mix", listOf(1L))),
        )
        val current = previous.copy(
            libraryRevision = "two",
            favoriteSongIds = listOf(1L, 2L),
            playlists = listOf(Playlist(7L, "Mix", listOf(1L, 2L))),
        )

        assertEquals(
            listOf(
                ElovaireMediaId.Root.value,
                ElovaireMediaId.Songs.value,
                ElovaireMediaId.Albums.value,
                ElovaireMediaId.Artists.value,
                ElovaireMediaId.Genres.value,
                ElovaireMediaId.RecentlyAdded.value,
                ElovaireMediaId.Favorites.value,
                ElovaireMediaIds.playlist(7L),
                ElovaireMediaId.Playlists.value,
            ).sorted(),
            MediaLibraryInvalidationParents.changedParents(previous, current).sorted(),
        )
    }

    @Test
    fun permissionChangeOnlyInvalidatesRoot() {
        val previous = MediaLibraryCommittedState("one", true, emptyList(), emptyList())
        val current = previous.copy(permissionGranted = false)

        assertEquals(
            listOf(ElovaireMediaId.Root.value),
            MediaLibraryInvalidationParents.changedParents(previous, current),
        )
    }
}
