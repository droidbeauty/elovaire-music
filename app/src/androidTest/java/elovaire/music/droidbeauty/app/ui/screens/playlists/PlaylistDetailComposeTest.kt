package elovaire.music.droidbeauty.app.ui.screens.playlists

import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.screens.PlaylistDetailScreen
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaylistDetailComposeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun addManySongsThenSaveSubmitsExactFinalDraft() {
        val initialIds = (1L..20L).toList()
        val librarySongs = (1L..80L).map(::testSong)
        var submittedSongIds: List<Long>? = null
        val playlist = Playlist(42L, "Interaction Test", initialIds)

        composeRule.setContent {
            MaterialTheme {
                PlaylistDetailScreen(
                    playlist = playlist,
                    librarySongs = librarySongs,
                    favoriteSongIds = emptySet(),
                    currentSongId = null,
                    isCurrentSongPlaying = false,
                    bottomPadding = 0.dp,
                    onBack = {},
                    onPlayPlaylist = { _, _ -> },
                    onShufflePlaylist = { _, _ -> },
                    onSongSelected = { _, _ -> },
                    onUpdateSongOrder = { ids ->
                        submittedSongIds = ids
                        CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success(42L))
                    },
                    onRenamePlaylist = { _, _ ->
                        CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success(42L))
                    },
                    onToggleFavorite = {},
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Edit playlist").assertIsDisplayed().performClick()
        composeRule.onNodeWithContentDescription("Add songs").performClick()
        val searchField = composeRule.onNode(hasSetTextAction())
        (21L..80L).forEach { id ->
            searchField.performTextClearance()
            searchField.performTextInput("Song $id")
            composeRule.waitForIdle()
            composeRule.onNode(hasText("Song $id", substring = true) and hasSetTextAction().not()).performClick()
        }
        composeRule.onNodeWithContentDescription("Confirm added songs").performClick()
        composeRule.onNodeWithContentDescription("Save playlist changes").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000L) { submittedSongIds != null }
        assertEquals((1L..80L).toList(), submittedSongIds)
    }

    private fun testSong(id: Long) = Song(
        id = id,
        title = "Song $id",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "Genre",
        audioFormat = "mp3",
        audioQuality = null,
        fileName = "song-$id.mp3",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = id.toInt(),
        discNumber = 1,
        dateAddedSeconds = id,
        uri = Uri.parse("content://test/song/$id"),
        artUri = null,
    )
}
