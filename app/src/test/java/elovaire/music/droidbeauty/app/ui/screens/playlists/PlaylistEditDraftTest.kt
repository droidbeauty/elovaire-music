package elovaire.music.droidbeauty.app.ui.screens.playlists

import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.screens.PlaylistEditDraft
import elovaire.music.droidbeauty.app.ui.screens.formatPlaylistDuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistEditDraftTest {
    @Test
    fun playlistDurationUsesSelectedLanguageUnits() {
        val duration = 3_720_000L
        assertEquals("1h 2m", formatPlaylistDuration(duration, AppLanguage.English))
        assertEquals("1 godz 2 min", formatPlaylistDuration(duration, AppLanguage.Polish))
        assertEquals("1時間 2分", formatPlaylistDuration(duration, AppLanguage.Japanese))
        assertEquals("1 ч 2 мин", formatPlaylistDuration(duration, AppLanguage.Russian))
        assertEquals("1 گھنٹہ 2 منٹ", formatPlaylistDuration(duration, AppLanguage.Urdu))
        assertEquals("2 گھنٹے", formatPlaylistDuration(7_200_000L, AppLanguage.Urdu))
        AppLanguage.entries.forEach { assertTrue(formatPlaylistDuration(duration, it).isNotBlank()) }
    }

    private val playlist = Playlist(id = 7L, name = "Interaction Test", songIds = listOf(1L, 2L, 3L))

    @Test
    fun addRemoveAndReorderProduceOneFinalOrderedList() {
        val draft = PlaylistEditDraft.fromPersisted(playlist)
            .addSongs(listOf(3L, 4L, 5L))
            .toggleRemoval(2L)
            .move(5L, -3)

        assertEquals(listOf(1L, 5L, 3L, 4L), draft.finalSongIds)
        assertTrue(draft.dirty)
    }

    @Test
    fun cancelResetsDraftToAuthoritativePlaylist() {
        val draft = PlaylistEditDraft.fromPersisted(playlist).addSongs(listOf(4L))
        val reset = PlaylistEditDraft.fromPersisted(playlist)

        assertEquals(listOf(1L, 2L, 3L), reset.finalSongIds)
        assertFalse(reset.dirty)
        assertEquals(listOf(1L, 2L, 3L, 4L), draft.finalSongIds)
    }

    @Test
    fun duplicateAddsDoNotDuplicateSongIds() {
        assertEquals(
            listOf(1L, 2L, 3L, 4L),
            PlaylistEditDraft.fromPersisted(playlist).addSongs(listOf(2L, 4L, 4L)).songIds,
        )
    }
}
