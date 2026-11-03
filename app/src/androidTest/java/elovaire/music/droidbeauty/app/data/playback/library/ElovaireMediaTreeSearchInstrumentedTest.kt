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
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.Album
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ElovaireMediaTreeSearchInstrumentedTest {
    @Test
    fun browserSongMetadataTracksLanguageAcrossBrowseAndSearch() {
        var language = AppLanguage.English
        val track = song(1L, "Track", "Unknown Artist").copy(album = "Unknown Album", genre = "Unknown Genre")
        val tree = ElovaireMediaTree(
            libraryRepository = FakeLibraryReader(songs = listOf(track)),
            preferenceStore = FakeUserDataReader(UserDataSnapshot()),
            language = { language },
        )
        val english = tree.childrenOf(ElovaireMediaId.Songs).single()
        language = AppLanguage.Polish
        val polish = tree.childrenOf(ElovaireMediaId.Songs).single()
        assertEquals(english.mediaId, polish.mediaId)
        assertEquals("Nieznany wykonawca", polish.mediaMetadata.artist.toString())
        assertEquals("Nieznany wykonawca", tree.item(polish.mediaId)?.mediaMetadata?.artist.toString())
        val result = tree.search("Track", 10).first { it.mediaId == polish.mediaId }
        assertEquals("Nieznany wykonawca", result.mediaMetadata.artist.toString())
    }
    @Test
    fun songFallbackMetadataIsLocalizedWithoutChangingSourceOrMediaId() {
        val track = song(1L, "Track", "Unknown Artist").copy(album = "Unknown Album", genre = "Unknown Genre")
        val english = ElovaireMediaItems.song(track)
        val polish = ElovaireMediaItems.song(track, AppLanguage.Polish)
        assertEquals(english.mediaId, polish.mediaId)
        assertEquals(english.localConfiguration?.uri, polish.localConfiguration?.uri)
        assertEquals("Nieznany wykonawca", polish.mediaMetadata.artist.toString())
        assertEquals("Nieznany album", polish.mediaMetadata.albumTitle.toString())
        assertEquals("Nieznany gatunek", polish.mediaMetadata.genre.toString())
        assertEquals("Unknown Artist", track.artist)
        assertEquals(polish.mediaMetadata.artist, ElovaireMediaItems.playable(track, AppLanguage.Polish).mediaMetadata.artist)
    }
    @Test
    fun albumFallbacksAreLocalizedWithoutChangingMediaIdentity() {
        val album = Album(7L, "Unknown Album", "Unknown Artist", null, 0, 0L, emptyList())
        val english = ElovaireMediaItems.album(album)
        val polish = ElovaireMediaItems.album(album, AppLanguage.Polish)
        assertEquals(english.mediaId, polish.mediaId)
        assertEquals("Nieznany album", polish.mediaMetadata.title.toString())
        assertEquals("Nieznany wykonawca", polish.mediaMetadata.subtitle.toString())
        assertEquals("Unknown Album", album.title)
    }
    @Test
    fun audiobookAuthorFallbackIsLocalizedWithoutChangingIdentity() {
        val book = Audiobook("book-key", "Book", "Unknown Author", null, 0L, emptyList())
        val english = ElovaireMediaItems.audiobook(book)
        val polish = ElovaireMediaItems.audiobook(book, AppLanguage.Polish)
        assertEquals(english.mediaId, polish.mediaId)
        assertEquals("Nieznany autor", polish.mediaMetadata.subtitle.toString())
        assertEquals("Unknown Author", book.author)
    }
    @Test
    fun unknownArtistAndGenreLabelsAreLocalizedWithoutChangingMediaIds() {
        val englishArtist = ElovaireMediaItems.artist("Unknown Artist")
        val polishArtist = ElovaireMediaItems.artist("Unknown Artist", AppLanguage.Polish)
        assertEquals(englishArtist.mediaId, polishArtist.mediaId)
        assertEquals("Nieznany wykonawca", polishArtist.mediaMetadata.title.toString())
        val englishGenre = ElovaireMediaItems.genre("Unknown Genre")
        val polishGenre = ElovaireMediaItems.genre("Unknown Genre", AppLanguage.Polish)
        assertEquals(englishGenre.mediaId, polishGenre.mediaId)
        assertEquals("Nieznany gatunek", polishGenre.mediaMetadata.title.toString())
    }
    @Test
    fun androidAutoEmptyAndPermissionGuidanceFollowsSelectedLanguage() {
        val permissionTree = ElovaireMediaTree(
            libraryRepository = FakeLibraryReader(songs = emptyList(), permissionGranted = false),
            preferenceStore = FakeUserDataReader(UserDataSnapshot()),
            language = { AppLanguage.Polish },
        )
        val permission = permissionTree.rootChildren().single()
        assertEquals("Otwórz Elovaire na telefonie", permission.mediaMetadata.title.toString())
        assertEquals("Zezwól na dostęp do muzyki, aby przeglądać ją w Android Auto", permission.mediaMetadata.subtitle.toString())
        assertEquals(permission.mediaMetadata.title, permissionTree.item(permission.mediaId)?.mediaMetadata?.title)

        val emptyTree = ElovaireMediaTree(
            libraryRepository = FakeLibraryReader(songs = emptyList()),
            preferenceStore = FakeUserDataReader(UserDataSnapshot()),
            language = { AppLanguage.Polish },
        )
        val empty = emptyTree.rootChildren().single()
        assertEquals("Nie znaleziono muzyki", empty.mediaMetadata.title.toString())
        assertEquals("Dodaj muzykę na telefonie, aby przeglądać ją w Android Auto", empty.mediaMetadata.subtitle.toString())
        assertEquals(empty.mediaMetadata.title, emptyTree.item(empty.mediaId)?.mediaMetadata?.title)
    }
    @Test
    fun browsableCategoriesFollowTheSelectedLanguageWithoutChangingIds() {
        var language = AppLanguage.English
        val tree = ElovaireMediaTree(
            libraryRepository = FakeLibraryReader(songs = listOf(song(1L, "Track", "Artist"))),
            preferenceStore = FakeUserDataReader(UserDataSnapshot()),
            language = { language },
        )
        val english = tree.rootChildren().associate { it.mediaId to it.mediaMetadata.title.toString() }
        val englishSearchRevision = tree.searchRevision()
        language = AppLanguage.Polish
        val polish = tree.rootChildren().associate { it.mediaId to it.mediaMetadata.title.toString() }
        assertTrue(englishSearchRevision != tree.searchRevision())
        assertEquals(english.keys, polish.keys)
        assertEquals("Utwory", polish[ElovaireMediaId.Songs.value])
        assertEquals("Albumy", polish[ElovaireMediaId.Albums.value])
        assertEquals("Ulubione", polish[ElovaireMediaId.Favorites.value])
        assertEquals("Ostatnio dodane", polish[ElovaireMediaId.RecentlyAdded.value])
        assertEquals(polish[ElovaireMediaId.Songs.value], tree.item(ElovaireMediaId.Songs.value)?.mediaMetadata?.title.toString())
        assertEquals("Utwory", tree.resolvePlayableQueue(ElovaireMediaId.Songs.value)?.sourceLabel)
        assertEquals("Ostatnio dodane", tree.resolvePlayableQueue(ElovaireMediaId.RecentlyAdded.value)?.sourceLabel)
    }
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

    @Test
    fun itemHidesCachedContentAfterPermissionRevocation() {
        val tree = ElovaireMediaTree(
            libraryRepository = FakeLibraryReader(
                songs = listOf(song(1L, title = "Private Track", artist = "Artist")),
                permissionGranted = false,
            ),
            preferenceStore = FakeUserDataReader(UserDataSnapshot()),
        )

        assertNull(tree.item(ElovaireMediaIds.song(1L)))
        assertNull(tree.item(ElovaireMediaId.Albums.value))
        assertNotNull(tree.item(ElovaireMediaId.Root.value))
        assertNotNull(tree.item(ElovaireMediaId.PermissionRequired.value))
    }

    private class FakeLibraryReader(
        songs: List<Song>,
        permissionGranted: Boolean = true,
    ) : LibraryReader {
        override val contentState: StateFlow<LibraryContentState> = MutableStateFlow(
            LibraryContentState(songs = songs, contentRevision = "search-test"),
        )
        override val scanState: StateFlow<LibraryScanState> = MutableStateFlow(
            LibraryScanState(permissionGranted = permissionGranted, isAuthoritative = true),
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
