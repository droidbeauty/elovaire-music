package elovaire.music.droidbeauty.app.data.settings

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PortableUserDataBackupTest {
    @Test
    fun backupRebindsPlaylistFavoritesCountsAndRecentsToNewMediaIds() {
        val oldSong = song(id = 1L)
        val backup = encodePortableUserData(
            snapshot = UserDataSnapshot(
                playlists = listOf(Playlist(40L, "Saved", listOf(1L))),
                favoriteSongIds = listOf(1L),
                songPlayCounts = mapOf(1L to 3),
                recentSongIds = listOf(1L),
            ),
            songs = listOf(oldSong),
            createdAtMs = 100L,
            appVersion = "test",
        )
        val payload = decodePortableUserData(backup)
        val newSong = oldSong.copy(id = 500L, uri = Uri.parse("content://media/external/audio/media/500"))

        assertNotNull(payload)
        val imported = requireNotNull(payload).mergeInto(UserDataSnapshot(), listOf(newSong))

        assertEquals(listOf(500L), imported.snapshot.playlists.single().songIds)
        assertEquals(listOf(500L), imported.snapshot.favoriteSongIds)
        assertEquals(3, imported.snapshot.songPlayCounts[500L])
        assertEquals(listOf(500L), imported.snapshot.recentSongIds)
        assertEquals(0, imported.unresolvedReferenceCount)
    }

    @Test
    fun ambiguousMediaIsNotAutomaticallyBound() {
        val source = song(1L)
        val bytes = encodePortableUserData(
            UserDataSnapshot(favoriteSongIds = listOf(1L)),
            listOf(source),
            createdAtMs = 100L,
            appVersion = "test",
        )
        val duplicateA = source.copy(id = 500L, uri = Uri.parse("content://media/external/audio/media/500"))
        val duplicateB = source.copy(id = 900L, uri = Uri.parse("content://media/external/audio/media/900"))

        val imported = requireNotNull(decodePortableUserData(bytes)).mergeInto(
            UserDataSnapshot(),
            listOf(duplicateA, duplicateB),
        )

        assertEquals(emptyList<Long>(), imported.snapshot.favoriteSongIds)
        assertEquals(1, imported.unresolvedReferenceCount)
    }

    @Test
    fun corruptedBackupIsRejected() {
        val bytes = encodePortableUserData(UserDataSnapshot(), emptyList(), 100L, "test")
        assertNull(decodePortableUserData(bytes.copyOf().also { it[it.lastIndex] = 'x'.code.toByte() }))
    }

    @Test
    fun importDoesNotMergeIntoSystemPlaylistWhenIdsCollide() {
        val source = song(1L)
        val bytes = encodePortableUserData(
            UserDataSnapshot(playlists = listOf(Playlist(1L, "Saved", listOf(1L)))),
            listOf(source),
            createdAtMs = 100L,
            appVersion = "test",
        )
        val current = UserDataSnapshot(playlists = listOf(Playlist(1L, "Favorites", isSystem = true)))

        val imported = requireNotNull(decodePortableUserData(bytes)).mergeInto(current, listOf(source))

        assertEquals(emptyList<Long>(), imported.snapshot.playlists.first().songIds)
        assertEquals(true, imported.snapshot.playlists.first().isSystem)
        assertEquals(2, imported.snapshot.playlists.size)
    }

    private fun song(id: Long): Song = Song(
        id = id,
        title = "Title",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "song.mp3",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        dateModifiedSeconds = 10L,
        libraryPath = "/storage/emulated/0/Music/song.mp3",
        uri = Uri.parse("content://media/external/audio/media/$id"),
        artUri = null,
        metadataResolved = true,
    )
}
