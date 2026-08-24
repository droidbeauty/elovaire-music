package elovaire.music.droidbeauty.app.data.settings

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.data.library.db.UserPlaylistEntity
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.library.db.FavoriteSongEntity
import elovaire.music.droidbeauty.app.data.library.db.PlaybackCollectionStateEntity
import elovaire.music.droidbeauty.app.data.library.db.RecentPlaybackEntity
import elovaire.music.droidbeauty.app.data.library.db.SongPlayCountEntity
import elovaire.music.droidbeauty.app.data.library.db.UserPlaylistEntryEntity
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playlists.serializePlaylists
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryKind
import android.net.Uri
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomUserDataStoreTest {
    private val databaseName = "playlist_reliability_test.db"
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: ElovaireDatabase

    @Before
    fun setUp() {
        assertTrue(
            context.getSharedPreferences(PreferenceStorage.PREFERENCE_FILE_NAME, 0)
                .edit()
                .clear()
                .commit(),
        )
        context.deleteDatabase(databaseName)
        database = Room.databaseBuilder(context, ElovaireDatabase::class.java, databaseName).build()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(databaseName)
        context.getSharedPreferences(PreferenceStorage.PREFERENCE_FILE_NAME, 0)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun releaseDrainsMandatoryWritesAndRejectsLaterCreates() = runBlocking {
        val store = RoomUserDataStore(context, database.userDataDao(), FixedClock)
        val results = List(512) { index -> store.createPlaylist("Playlist $index").await() }
        val ids = results.map { (it as PlaylistMutationResult.Success).playlistId ?: error("missing playlist id") }
        val drained = CompletableDeferred<Unit>()

        store.release { drained.complete(Unit) }
        withTimeout(10_000L) { drained.await() }

        assertTrue(results.all { it is PlaylistMutationResult.Success })
        assertEquals(ids, database.userDataDao().playlists().map { it.playlistId })
        assertTrue(store.createPlaylist("After release").await() is PlaylistMutationResult.Failure)
        assertEquals(512, database.userDataDao().playlists().size)
    }

    @Test
    fun acknowledgedEditsReadBackExactlyAndStoreContinuesAfterFailure() = runBlocking {
        val store = RoomUserDataStore(context, database.userDataDao(), FixedClock)
        val created = store.createPlaylist("Interaction Test").await() as PlaylistMutationResult.Success
        val playlistId = created.playlistId ?: error("missing playlist id")
        val expectedOrder = List(500) { index -> index.toLong() + 1L }

        assertTrue(store.updatePlaylistSongIds(playlistId, expectedOrder + expectedOrder.take(20)).await() is PlaylistMutationResult.Success)
        assertEquals(expectedOrder, database.userDataDao().playlistEntries(playlistId).map { it.songId })
        assertEquals((0 until expectedOrder.size).toList(), database.userDataDao().playlistEntries(playlistId).map { it.position })

        assertTrue(store.updatePlaylistSongIds(999_999L, listOf(1L)).await() is PlaylistMutationResult.NotFound)
        assertTrue(store.renamePlaylist(playlistId, "  Interaction   Test  ").await() is PlaylistMutationResult.Success)
        assertEquals("Interaction Test", database.userDataDao().playlist(playlistId)?.name)

        val drained = CompletableDeferred<Unit>()
        store.release { drained.complete(Unit) }
        withTimeout(10_000L) { drained.await() }

        database.close()
        database = Room.databaseBuilder(context, ElovaireDatabase::class.java, databaseName).build()
        assertEquals(expectedOrder, database.userDataDao().playlistEntries(playlistId).map { it.songId })
        assertEquals("Interaction Test", database.userDataDao().playlist(playlistId)?.name)
    }

    @Test
    fun provenMediaRelocationPreservesAllSongReferences() = runBlocking {
        database.userDataDao().insertPlaylist(UserPlaylistEntity(41L, "Saved", false))
        database.userDataDao().insertPlaylistEntries(listOf(UserPlaylistEntryEntity(41L, 11L, 0)))
        database.userDataDao().insertFavorite(FavoriteSongEntity(11L, 0))
        database.userDataDao().insertSongPlayCounts(listOf(SongPlayCountEntity(11L, 4)))
        database.userDataDao().replaceRecentPlayback(
            entries = listOf(RecentPlaybackEntity("song", 11L, 0)),
            state = PlaybackCollectionStateEntity(kind = null, collectionId = null),
        )

        val store = RoomUserDataStore(context, database.userDataDao(), FixedClock)
        val result = store.relocateSongReferences(mapOf(11L to 22L)).await()

        assertTrue(result is PlaylistMutationResult.Success)
        assertEquals(listOf(22L), database.userDataDao().playlistEntries(41L).map(UserPlaylistEntryEntity::songId))
        assertEquals(listOf(22L), database.userDataDao().favorites().map(FavoriteSongEntity::songId))
        assertEquals(listOf(22L), database.userDataDao().songPlayCounts().map(SongPlayCountEntity::songId))
        assertEquals(listOf(22L), database.userDataDao().recentPlayback().map(RecentPlaybackEntity::itemId))
        assertEquals(listOf(22L), store.playlists.value.single { it.id == 41L }.songIds)
        assertEquals(listOf(22L), store.favoriteSongIds.value)
        assertEquals(listOf(22L), store.recentSongIds.value)
        store.release()
    }

    @Test
    fun firstCreateWaitsForInitializationBeforeAllocatingAnId() = runBlocking {
        database.userDataDao().insertPlaylist(UserPlaylistEntity(1_000L, "Existing", false))
        val store = RoomUserDataStore(context, database.userDataDao(), FixedClock)

        val result = store.createPlaylist("Created after initialization").await()

        assertTrue(result is PlaylistMutationResult.Success)
        val createdId = (result as PlaylistMutationResult.Success).playlistId ?: error("missing playlist id")
        assertTrue(createdId > 1_000L)
        assertEquals("Created after initialization", database.userDataDao().playlist(createdId)?.name)
        store.release()
    }

    @Test
    fun legacyPlaylistStateMigratesBeforeNewMutationsAndIsClearedAfterCommit() = runBlocking {
        val legacyPlaylist = Playlist(
            id = 37L,
            name = "Legacy Mix",
            songIds = listOf(11L, 9L, 9L),
        )
        val preferences = context.getSharedPreferences(PreferenceStorage.PREFERENCE_FILE_NAME, 0)
        assertTrue(
            preferences.edit()
                .putString("playlists", serializePlaylists(listOf(legacyPlaylist)))
                .commit(),
        )

        val store = RoomUserDataStore(context, database.userDataDao(), FixedClock)
        val created = store.createPlaylist("Created after migration").await()

        assertTrue(created is PlaylistMutationResult.Success)
        assertEquals(
            listOf("Legacy Mix", "Created after migration"),
            database.userDataDao().playlists().map(UserPlaylistEntity::name),
        )
        assertFalse(preferences.contains("playlists"))
        store.release()
    }

    @Test
    fun acceptedMutationCompletesWhenTheDatabaseOwnerFails() = runBlocking {
        val store = RoomUserDataStore(context, database.userDataDao(), FixedClock)
        database.close()

        val result = withTimeout(10_000L) { store.createPlaylist("Database is closed").await() }

        assertFalse(result is PlaylistMutationResult.Success)
        store.release()
    }

    @Test
    fun bulkFavoriteChangesPreserveOrderWithoutRebuildingTheTable() = runBlocking {
        val store = RoomUserDataStore(context, database.userDataDao(), FixedClock)
        val songIds = (1L..1_000L).toList()

        store.setFavoriteSongs(songIds, favorite = true)
        store.createPlaylist("Flush").await()

        assertEquals(songIds, database.userDataDao().favorites().map { it.songId })

        val removed = (401L..600L).toSet()
        store.setFavoriteSongs(removed.toList(), favorite = false)
        store.createPlaylist("Flush removal").await()

        val remaining = songIds.filterNot(removed::contains)
        assertEquals(remaining, database.userDataDao().favorites().map { it.songId })
        assertEquals(0, database.userDataDao().favorites().first().position)
        assertEquals(999, database.userDataDao().favorites().last().position)

        val drained = CompletableDeferred<Unit>()
        store.release { drained.complete(Unit) }
        withTimeout(10_000L) { drained.await() }
    }

    @Test
    fun playbackHistoryBatchPersistsCountsAndLatestRecentState() = runBlocking {
        val store = RoomUserDataStore(context, database.userDataDao(), FixedClock)
        store.recordPlaybackTransition(songId = 7L, albumId = 70L)
        store.recordPlaybackTransition(songId = 7L, albumId = 70L)
        store.setRecentPlaybackIds(
            songIds = listOf(7L),
            albumIds = listOf(70L),
            lastPlayedCollectionKind = PlaybackCollectionKind.Album,
            lastPlayedCollectionId = 70L,
        )

        store.createPlaylist("Flush playback history").await()

        assertEquals(2, database.userDataDao().songPlayCounts().single().playCount)
        assertEquals(2, database.userDataDao().albumPlayCounts().single().playCount)
        assertEquals(listOf(7L), database.userDataDao().recentPlayback().filter { it.kind == "song" }.map { it.itemId })
        assertEquals("Album", database.userDataDao().playbackCollectionState()?.kind)
        store.release()
    }

    @Test
    fun userDataRecoverySnapshotRoundTripsAndRejectsCorruption() {
        val fileName = "user_data_recovery_test.json"
        val file = context.filesDir.resolve(fileName)
        file.delete()
        val recovery = UserDataRecoverySnapshot(context, FixedClock, fileName)
        val expected = UserDataSnapshot(
            playlists = listOf(Playlist(41L, "Saved", listOf(11L, 22L))),
            favoriteSongIds = listOf(11L, 22L),
            songPlayCounts = mapOf(11L to 3),
            albumPlayCounts = mapOf(7L to 4),
            recentSongIds = listOf(22L, 11L),
            recentAlbumIds = listOf(7L),
            lastPlayedCollectionKind = PlaybackCollectionKind.Album,
            lastPlayedCollectionId = 7L,
            searchHistory = listOf(
                SearchHistoryEntry(
                    key = "album:7",
                    kind = SearchHistoryKind.Album,
                    title = "Saved",
                    subtitle = "Artist",
                    artUri = Uri.parse("content://art/7"),
                    albumId = 7L,
                    query = "saved",
                ),
            ),
        )

        recovery.write(expected)
        assertEquals(expected, recovery.read())

        file.writeText(file.readText().replace("\"checksum\":\"", "\"checksum\":\"broken"))
        assertEquals(null, recovery.read())
        file.delete()
    }

    private object FixedClock : AppClock {
        override fun wallTimeMs(): Long = 1_000L
        override fun elapsedTimeMs(): Long = 500L
    }
}
