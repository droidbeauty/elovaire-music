package elovaire.music.droidbeauty.app.data.settings

import android.net.Uri
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PortableUserDataBackupTest {
    @Test
    fun userDataRevisionRoundTripsWithPortablePayload() {
        val bytes = encodePortableUserData(
            snapshot = UserDataSnapshot(),
            songs = emptyList(),
            createdAtMs = 100L,
            appVersion = "test",
            userDataRevision = 42L,
        )

        assertEquals(42L, requireNotNull(decodePortableUserData(bytes)).userDataRevision)
    }

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
    fun probableMediaIsNotAutomaticallyBound() {
        val source = song(1L).copy(
            album = "Source album",
            durationMs = 0L,
            trackNumber = 0,
            discNumber = 0,
        )
        val bytes = encodePortableUserData(
            UserDataSnapshot(favoriteSongIds = listOf(1L)),
            listOf(source),
            createdAtMs = 100L,
            appVersion = "test",
        )
        val candidate = source.copy(
            id = 500L,
            album = "Different album",
            fileName = "different.mp3",
            uri = Uri.parse("content://media/external/audio/media/500"),
        )

        val imported = requireNotNull(decodePortableUserData(bytes)).mergeInto(
            UserDataSnapshot(),
            listOf(candidate),
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

    @Test
    fun importMatchesNormalPlaylistsByNameAndAllocatesFreshIdsOnCollisions() {
        val backup = encodePortableUserData(
            UserDataSnapshot(
                playlists = listOf(
                    Playlist(7L, "B", listOf(1L)),
                    Playlist(8L, "C", listOf(1L)),
                ),
            ),
            songs = emptyList(),
            createdAtMs = 100L,
            appVersion = "test",
        )
        val current = UserDataSnapshot(
            playlists = listOf(
                Playlist(7L, "A", listOf(100L)),
                Playlist(8L, "B", listOf(101L)),
            ),
        )

        val imported = requireNotNull(decodePortableUserData(backup)).mergeInto(current, emptyList())
        val byName = imported.snapshot.playlists.associateBy(Playlist::name)

        assertEquals(listOf(100L), byName.getValue("A").songIds)
        assertEquals(listOf(101L), byName.getValue("B").songIds)
        assertEquals(3, imported.snapshot.playlists.size)
        assertEquals(listOf(7L, 8L), imported.snapshot.playlists.filter { it.name != "C" }.map(Playlist::id))
        assertTrue(imported.snapshot.playlists.single { it.name == "C" }.id !in setOf(7L, 8L))
    }

    @Test
    fun importPreservesSmartPlaylistWithCollidingSmartId() {
        val importedSmart = smartPlaylist(8L, "Imported Smart")
        val backup = encodePortableUserData(
            UserDataSnapshot(smartPlaylists = listOf(importedSmart)),
            songs = emptyList(),
            createdAtMs = 100L,
            appVersion = "test",
        )
        val currentSmart = smartPlaylist(8L, "Existing Smart")

        val imported = requireNotNull(decodePortableUserData(backup)).mergeInto(
            UserDataSnapshot(smartPlaylists = listOf(currentSmart)),
            emptyList(),
        )

        assertEquals(
            setOf("Existing Smart", "Imported Smart"),
            imported.snapshot.smartPlaylists.map(SmartPlaylist::name).toSet(),
        )
        assertEquals(2, imported.snapshot.smartPlaylists.map(SmartPlaylist::id).toSet().size)
    }

    @Test
    fun importedSmartPlaylistCannotReuseNormalPlaylistId() {
        val backup = encodePortableUserData(
            UserDataSnapshot(smartPlaylists = listOf(smartPlaylist(7L, "Imported Smart"))),
            songs = emptyList(),
            createdAtMs = 100L,
            appVersion = "test",
        )
        val current = UserDataSnapshot(playlists = listOf(Playlist(7L, "Normal")))

        val imported = requireNotNull(decodePortableUserData(backup)).mergeInto(current, emptyList())
        val importedSmart = imported.snapshot.smartPlaylists.single()

        assertTrue(importedSmart.id != 7L)
        val allIds = imported.snapshot.playlists.map(Playlist::id) +
            imported.snapshot.smartPlaylists.map(SmartPlaylist::id)
        assertEquals(2, allIds.toSet().size)
    }

    private fun smartPlaylist(id: Long, name: String) = SmartPlaylist(
        id = id,
        name = name,
        createdAtMs = 1L,
        updatedAtMs = 1L,
    )

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
