package elovaire.music.droidbeauty.app.quality

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.dp
import android.net.Uri
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.data.artist.ArtistDiscographyAlbum
import elovaire.music.droidbeauty.app.data.artist.ArtistDiscographyReader
import elovaire.music.droidbeauty.app.data.artist.ArtistDiscographyResult
import elovaire.music.droidbeauty.app.data.artist.ArtistBackdropState
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.screens.ArtistDiscographyPopup
import elovaire.music.droidbeauty.app.ui.screens.ArtistDetailScreen
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import androidx.compose.runtime.CompositionLocalProvider
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtistInformationUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun artistPreviewShowsLibrarySectionsAndOpensInformation() {
        val song = Song(
            id = 1, title = "Example song", isExplicit = false, artist = "Example artist",
            album = "Example album", releaseYear = 2020, genre = "Rock", audioFormat = "AAC",
            audioQuality = null, fileName = "example.m4a", albumId = 1, durationMs = 120_000,
            trackNumber = 1, discNumber = 1, dateAddedSeconds = 1, uri = Uri.EMPTY, artUri = null,
        )
        val album = Album(1, "Example album", "Example artist", null, 1, 120_000, listOf(song))
        var openedArtist: String? = null
        rule.setContent {
            ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                    ArtistDetailScreen(
                        artistName = "Example artist", artistSongs = listOf(song), artistAlbums = listOf(album),
                        artistBackdropState = ArtistBackdropState.Fallback(null, "example"),
                        onOpenArtistInfo = { openedArtist = it }, songPlayCounts = emptyMap(),
                        favoriteSongIds = emptySet(), currentSongId = null, isCurrentSongPlaying = false,
                        bottomPadding = 0.dp, onBack = {}, onSongSelected = { _, _ -> },
                        onPlayArtist = {}, onShuffleArtist = {}, onAlbumSelected = { _, _ -> }, onToggleFavorite = {},
                    )
                }
            }
        }
        rule.onAllNodesWithText("Example artist").onFirst().assertIsDisplayed()
        rule.onNodeWithText("Example song").assertIsDisplayed()
        capture("artist-preview-test.png")
        rule.onNodeWithTag("artist_detail_list").performScrollToKey("artist_information")
        rule.onNodeWithText("Artist information").performClick()
        rule.runOnIdle { assertEquals("Example artist", openedArtist) }
    }

    @Test
    fun biographyAndExpandableDiscographyAreVisible() {
        val visible = mutableStateOf(true)
        val reader = object : ArtistDiscographyReader {
            override suspend fun discography(artistName: String): ArtistDiscographyResult =
                ArtistDiscographyResult.Found(
                    albums = listOf(ArtistDiscographyAlbum("Example album", "2020")),
                    description = "An example artist biography with a clear description",
                    genres = listOf("Rock"),
                    descriptionSourceUrl = "https://en.wikipedia.org/wiki/Example",
                )
        }
        rule.setContent {
            ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                    ArtistDiscographyPopup("Example artist", reader, visible.value, onDismiss = { visible.value = false })
                }
            }
        }
        rule.waitUntil(5_000) {
            rule.onAllNodes(androidx.compose.ui.test.hasText("An example artist biography with a clear description"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("An example artist biography with a clear description").assertIsDisplayed()
        rule.onNodeWithText("Rock").assertIsDisplayed()
        capture("artist-info-test.png")
        rule.onNodeWithText("Discography").performClick()
        rule.onNodeWithText("Example album").assertIsDisplayed()
        rule.onNodeWithText("2020").assertIsDisplayed()
    }

    private fun capture(name: String) {
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        InstrumentationRegistry.getInstrumentation().targetContext.cacheDir.resolve(name).outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
