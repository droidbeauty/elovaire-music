package elovaire.music.droidbeauty.app.data.playlists

import elovaire.music.droidbeauty.app.domain.model.Playlist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistHelpersTest {
    @Test
    fun createPlaylistEntries_trimsNameAndProducesUniquePositiveId() {
        val existing = listOf(
            Playlist(id = 100L, name = "Existing"),
            Playlist(id = 101L, name = "Another"),
        )

        val result = createPlaylistEntries(
            playlists = existing,
            name = "  New   Playlist  ",
            nextPlaylistId = 100L,
        )

        requireNotNull(result)
        assertEquals("New Playlist", result.createdPlaylist.name)
        assertTrue(result.createdPlaylist.id > 0L)
        assertTrue(result.createdPlaylist.id !in existing.map { it.id }.toSet())
        assertEquals(result.createdPlaylist, result.playlists.first())
    }

    @Test
    fun createPlaylistEntries_rejectsBlankName() {
        val result = createPlaylistEntries(
            playlists = emptyList(),
            name = "   ",
            nextPlaylistId = 1L,
        )

        assertNull(result)
    }

    @Test
    fun addSongsToPlaylistEntries_noOpsWhenPlaylistMissing() {
        val result = addSongsToPlaylistEntries(
            playlists = listOf(Playlist(id = 1L, name = "Test")),
            playlistId = 2L,
            songIds = listOf(1L, 2L),
        )

        assertNull(result)
    }

    @Test
    fun addSongsToPlaylistEntries_normalizesNonZeroDistinctIdsAndPreservesOrder() {
        val result = addSongsToPlaylistEntries(
            playlists = listOf(Playlist(id = 1L, name = "Test", songIds = listOf(3L, 1L))),
            playlistId = 1L,
            songIds = listOf(1L, -1L, 2L, 3L, 2L, 0L, 4L),
        )

        requireNotNull(result)
        assertEquals(listOf(3L, 1L, -1L, 2L, 4L), result.single().songIds)
    }

    @Test
    fun updatePlaylistSongIdsEntry_normalizesIds() {
        val result = updatePlaylistSongIdsEntry(
            playlists = listOf(Playlist(id = 1L, name = "Test", songIds = listOf(1L))),
            playlistId = 1L,
            songIds = listOf(-5L, 3L, 3L, 2L, 0L, 1L),
        )

        requireNotNull(result)
        assertEquals(listOf(-5L, 3L, 2L, 1L), result.single().songIds)
    }

    @Test
    fun renamePlaylistEntry_noOpsWhenPlaylistMissing() {
        val result = renamePlaylistEntry(
            playlists = listOf(Playlist(id = 1L, name = "Test")),
            playlistId = 9L,
            name = "Renamed",
        )

        assertNull(result)
    }

    @Test
    fun deletePlaylistEntries_ignoresMissingIdsAndSystemPlaylists() {
        val result = deletePlaylistEntries(
            playlists = listOf(
                Playlist(id = 1L, name = "User"),
                Playlist(id = 2L, name = "System", isSystem = true),
            ),
            playlistIds = setOf(1L, 2L, 9L),
        )

        requireNotNull(result)
        assertEquals(listOf(Playlist(id = 2L, name = "System", isSystem = true)), result)
    }

    @Test
    fun removeSongReferencesFromPlaylists_removesNegativeSafSongIds() {
        val result = removeSongReferencesFromPlaylists(
            playlists = listOf(Playlist(id = 1L, name = "Test", songIds = listOf(4L, -8L, 2L))),
            songIds = setOf(-8L),
        )

        requireNotNull(result)
        assertEquals(listOf(4L, 2L), result.single().songIds)
    }

    @Test
    fun removeSongReferencesFromPlaylists_removesBatchInOnePass() {
        val result = removeSongReferencesFromPlaylists(
            playlists = listOf(Playlist(id = 1L, name = "Test", songIds = listOf(1L, 2L, 3L, 4L))),
            songIds = setOf(2L, 4L),
        )

        requireNotNull(result)
        assertEquals(listOf(1L, 3L), result.single().songIds)
    }

    @Test
    fun deserializePlaylists_readsLegacyEntries() {
        val legacy = listOf(
            "1\u001FPlaylist One\u001F7,3,3,-4\u001Ffalse",
            "2\u001FPlaylist Two\u001F\u001Ffalse",
        ).joinToString("\u001E")

        val result = deserializePlaylists(legacy)

        assertEquals(
            listOf(
                Playlist(id = 1L, name = "Playlist One", songIds = listOf(7L, 3L, -4L), isSystem = false),
                Playlist(id = 2L, name = "Playlist Two", songIds = emptyList(), isSystem = false),
            ),
            result,
        )
    }

    @Test
    fun deserializePlaylists_readsVersionTwoSeparatorCharactersAndEmoji() {
        val legacyV2 = "v2:1\u001FUm9hZCAfIFRyaXAgHiBNaXg\u001F5,5,2,-4,0\u001Ffalse" +
            "\u001E2\u001FTGF0ZSBOaWdodCDwn4yZ\u001F9,1\u001Ffalse"
        val deserialized = deserializePlaylists(legacyV2)

        assertEquals(
            listOf(
                Playlist(id = 1L, name = "Road \u001F Trip \u001E Mix", songIds = listOf(5L, 2L, -4L), isSystem = false),
                Playlist(id = 2L, name = "Late Night \uD83C\uDF19", songIds = listOf(9L, 1L), isSystem = false),
            ),
            deserialized,
        )
    }
}
