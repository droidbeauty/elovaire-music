package elovaire.music.droidbeauty.app.data.playback.library

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.library.LibraryReader
import elovaire.music.droidbeauty.app.data.library.LibraryScanState
import elovaire.music.droidbeauty.app.data.settings.MediaLibraryUserDataReader
import elovaire.music.droidbeauty.app.data.settings.RevisionedUserDataSnapshot
import elovaire.music.droidbeauty.app.data.settings.UserDataSnapshot
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ElovaireMediaTreeSearchInstrumentedTest {
    @Test
    fun pagedSearchUsesTheSameOrderedProjectionAsLimitedSearch() {
        val tree = ElovaireMediaTree(
            libraryRepository = FakeLibraryReader(
                songs = listOf(
                    song(1L, title = "Quiet Track", artist = "Northern Lights"),
                    song(2L, title = "Another Song", artist = "Northern Lights"),
                    song(3L, title = "Last Song", artist = "Southern Lights"),
                ),
            ),
            preferenceStore = FakeUserDataReader(
                UserDataSnapshot(playlists = listOf(Playlist(7L, "Light Mix", listOf(1L)))),
            ),
        )

        val expected = tree.search("lights", limit = MediaLibraryRequestPolicy.MAX_SEARCH_RESULT_ITEMS)
            .map { it.mediaId }

        assertEquals(
            expected.drop(1).take(3),
            tree.searchPage("lights", offset = 1, limit = 3).map { it.mediaId },
        )
        assertEquals(expected.size, tree.searchCount("lights"))
    }

    private class FakeLibraryReader(songs: List<Song>) : LibraryReader {
        override val contentState: StateFlow<LibraryContentState> = MutableStateFlow(
            LibraryContentState(songs = songs, contentRevision = "search-test"),
        )
        override val scanState: StateFlow<LibraryScanState> = MutableStateFlow(
            LibraryScanState(permissionGranted = true, isAuthoritative = true),
        )
    }

    private class FakeUserDataReader(snapshot: UserDataSnapshot) : MediaLibraryUserDataReader {
        override val revisionedUserDataSnapshot: StateFlow<RevisionedUserDataSnapshot> =
            MutableStateFlow(RevisionedUserDataSnapshot(0L, snapshot))
        override val userDataSnapshot: StateFlow<UserDataSnapshot> = MutableStateFlow(snapshot)
        override val favoriteSongIds: StateFlow<List<Long>> = MutableStateFlow(snapshot.favoriteSongIds)
        override val playlists: StateFlow<List<Playlist>> = MutableStateFlow(snapshot.playlists)
    }

    private fun song(id: Long, title: String, artist: String): Song = Song(
        id = id,
        title = title,
        isExplicit = false,
        artist = artist,
        album = "Album",
        releaseYear = null,
        genre = "Genre",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "$id.mp3",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = id.toInt(),
        discNumber = 1,
        dateAddedSeconds = id,
        uri = Uri.parse("content://song/$id"),
        artUri = null,
    )
}
