package elovaire.music.droidbeauty.app.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectionInteractionsTest {
    @get:Rule val rule = createAndroidComposeRule<PlaylistTestActivity>()
    private val songs = (1L..60L).map { id ->
        Song(
            id = id, title = "Track ${id.toString().padStart(2, '0')}", isExplicit = false,
            artist = "Artist ${id.toString().padStart(2, '0')}", album = "Album ${id.toString().padStart(2, '0')}",
            releaseYear = null, genre = "", audioFormat = "mp3", audioQuality = null,
            fileName = "$id.mp3", albumId = id, durationMs = 180_000L, trackNumber = 1,
            discNumber = 1, dateAddedSeconds = id, uri = Uri.parse("content://collection-fixture/$id"), artUri = null,
        )
    }

    @Test
    fun recentlyAddedLongPressEntersSelectionAndDeletesOnlySelectedAlbums() {
        var opened = 0
        var deletedIds = emptyList<Long>()
        setContent {
            RecentlyAddedAlbumsScreen(
                albums = songs.map(::album), playlists = emptyList(), playlistSongsById = songs.associateBy(Song::id),
                favoriteSongIds = emptySet(), bottomPadding = 0.dp, onBack = {},
                onAlbumSelected = { _, _ -> opened++ }, onAddAlbumToQueue = {},
                onAddAlbumToPlaylist = { _, _ -> CompletableDeferred(PlaylistMutationResult.InvalidInput) },
                onCreatePlaylist = { CompletableDeferred(PlaylistMutationResult.InvalidInput) },
                onSetAlbumFavorite = { _, _ -> }, onDeleteAlbumFromDevice = { deletedIds = it.songs.map(Song::id) },
            )
        }
        rule.onNodeWithText("Album 01").performTouchInput { longClick() }
        rule.onNodeWithText("Add to playlist").assertIsDisplayed()
        rule.onNodeWithText("Album 02").performClick()
        rule.onNodeWithText("Delete").performClick()
        rule.runOnIdle {
            assertEquals(listOf(1L, 2L), deletedIds)
            assertEquals(0, opened)
        }
        rule.onNodeWithText("Album 01").performClick()
        rule.runOnIdle { assertEquals(1, opened) }
    }

    @Test
    fun pickerSearchAndClearResetSongsAlbumsAndArtistsToTheTop() {
        setContent {
            AddSongsToPlaylistOverlay(songs, emptySet(), onDismiss = {}, onAddSongs = {})
        }
        listOf("Songs" to "Track", "Albums" to "Album", "Artists" to "Artist").forEach { (tab, term) ->
            rule.onNodeWithText(tab, useUnmergedTree = true).performClick()
            rule.onNode(hasScrollToIndexAction()).performScrollToIndex(30)
            rule.onNode(hasSetTextAction()).performTextInput(term)
            rule.onNodeWithText("$term 01", useUnmergedTree = true).assertIsDisplayed()
            rule.onNode(hasScrollToIndexAction()).performScrollToIndex(25)
            rule.onNodeWithContentDescription("Clear search").performClick()
            rule.onNodeWithText("$term 01", useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    fun dailyAlbumCardOpensTheAlbumAndHasIndependentPlayback() {
        var opened = 0
        var played = 0
        setContent {
            AlbumOfDayModule(album(songs.first()), onOpen = { opened++ }, onPlay = { played++ })
        }
        rule.onNodeWithContentDescription("Play album").performClick()
        rule.runOnIdle {
            assertEquals(1, played)
            assertEquals(0, opened)
        }
        rule.onNodeWithText("Album of the day").performClick()
        rule.runOnIdle { assertEquals(1, opened) }
        capture("album-of-day.png")
    }

    @Test
    fun playlistPreviewKeepsPlaybackBelowTheCompactHeader() {
        var played = 0
        var shuffled = 0
        setContent {
            PlaylistDetailScreen(
                playlist = Playlist(42L, "Evening listening", songs.map(Song::id)),
                librarySongs = songs, songsById = songs.associateBy(Song::id), favoriteSongIds = emptySet(),
                currentSongId = null, isCurrentSongPlaying = false, bottomPadding = 0.dp, onBack = {},
                onPlayPlaylist = { _, _ -> played++ }, onShufflePlaylist = { _, _ -> shuffled++ },
                onSongSelected = { _, _ -> }, onUpdateSongOrder = { CompletableDeferred(PlaylistMutationResult.Success(42L)) },
                onRenamePlaylist = { _, _ -> CompletableDeferred(PlaylistMutationResult.Success(42L)) }, onToggleFavorite = {},
            )
        }
        rule.onNodeWithText("Play").performClick()
        rule.onNodeWithContentDescription("Shuffle playlist").performClick()
        rule.runOnIdle {
            assertEquals(1, played)
            assertEquals(1, shuffled)
        }
        capture("playlist-preview-compact.png")
    }

    private fun setContent(content: @androidx.compose.runtime.Composable () -> Unit) {
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    Box(Modifier.fillMaxSize().testTag("collection_fixture")) { content() }
                }
            }
        }
    }

    private fun capture(name: String) {
        val bitmap = rule.onNodeWithTag("collection_fixture").captureToImage().asAndroidBitmap()
        rule.activity.cacheDir.resolve(name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun album(song: Song) = Album(song.albumId, song.album, song.artist, null, 1, song.durationMs, listOf(song))
}
