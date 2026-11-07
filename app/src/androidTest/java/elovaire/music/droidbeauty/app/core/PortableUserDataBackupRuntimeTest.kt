package elovaire.music.droidbeauty.app.core

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.library.LibraryScanState
import elovaire.music.droidbeauty.app.data.library.LibraryStartupController
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.settings.PortableUserDataBackup
import elovaire.music.droidbeauty.app.data.settings.RoomUserDataStore
import elovaire.music.droidbeauty.app.data.settings.UserDataReadiness
import elovaire.music.droidbeauty.app.data.settings.UserDataSnapshot
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.data.settings.encodePortableUserData
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class PortableUserDataBackupRuntimeTest {
    @Test
    fun automaticBackupWaitsUntilAllLocallyReferencedSongsCanBeEncoded() = runBlocking {
        withRuntime { runtime, store, library, file ->
            val first = song(1, "First")
            val second = song(2, "Second")
            val original = encodePortableUserData(UserDataSnapshot(favoriteSongIds = listOf(1, 2)),
                listOf(first, second), 100, "test")
            file.writeBytes(original)
            assertEquals(true, store.setFavoriteSongs(listOf(1, 2), true).await() is PlaylistMutationResult.Success)
            library.contentState.value = LibraryContentState(songs = listOf(first), portableMediaIdentityRevision = "one")
            runtime.start()
            delay(1_500)
            assertArrayEquals(original, file.readBytes())
            library.contentState.value = LibraryContentState(songs = listOf(first, second), portableMediaIdentityRevision = "two")
            withTimeout(10_000) {
                while (file.readBytes().contentEquals(original)) delay(50)
            }
            val saved = requireNotNull(elovaire.music.droidbeauty.app.data.settings.decodePortableUserData(file.readBytes()))
            assertEquals(2, saved.favoriteSongs.size)
        }
    }

    @Test
    fun partialLibraryDoesNotReplaceBackupAndRestoreResumesWhenMissingSongAppears() = runBlocking {
        withRuntime { runtime, store, library, file ->
            val first = song(1, "First")
            val second = song(2, "Second")
            val original = encodePortableUserData(UserDataSnapshot(favoriteSongIds = listOf(1, 2)),
                listOf(first, second), 100, "test")
            file.writeBytes(original)
            library.contentState.value = LibraryContentState(songs = listOf(first), portableMediaIdentityRevision = "one")
            runtime.start()
            delay(1_500)
            assertEquals(emptyList<Long>(), store.favoriteSongIds.value)
            assertArrayEquals(original, file.readBytes())
            library.contentState.value = LibraryContentState(songs = listOf(first, second), portableMediaIdentityRevision = "two")
            withTimeout(10_000) { store.favoriteSongIds.first { it.size == 2 } }
            assertEquals(listOf(1L, 2L), store.favoriteSongIds.value)
        }
    }

    @Test
    fun unreadableBackupIsNotOverwrittenByAnEmptyLocalStore() = runBlocking {
        withRuntime { runtime, _, _, file ->
            val invalid = "unrecognized backup data".toByteArray()
            file.writeBytes(invalid)
            runtime.start()
            delay(1_500)
            assertArrayEquals(invalid, file.readBytes())
        }
    }

    private suspend fun withRuntime(block: suspend (PortableUserDataBackupRuntime, RoomUserDataStore, FakeLibrary, java.io.File) -> Unit) {
        val target = ApplicationProvider.getApplicationContext<Context>()
        val prefix = "portable-runtime-${System.nanoTime()}-"
        val context = object : ContextWrapper(target) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int) = super.getSharedPreferences(prefix + name, mode)
        }
        val database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
        val store = RoomUserDataStore(context, database.userDataDao(), database = database)
        val file = context.filesDir.resolve(prefix + "backup.json")
        val backup = PortableUserDataBackup(context, fileName = file.name)
        val library = FakeLibrary()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val runtime = PortableUserDataBackupRuntime(scope, backup, store, library, Dispatchers.IO)
        try {
            withTimeout(10_000) { store.userDataReadiness.first { it == UserDataReadiness.Ready } }
            block(runtime, store, library, file)
        } finally {
            runtime.release()
            store.release().await()
            scope.cancel()
            database.close()
            file.delete()
        }
    }

    private class FakeLibrary : LibraryStartupController {
        override val contentState = MutableStateFlow(LibraryContentState())
        override val scanState = MutableStateFlow(LibraryScanState(permissionGranted = true, isAuthoritative = true))
        override fun start() = Unit
        override fun onPermissionChanged(granted: Boolean) = Unit
        override fun blockNetworkSources(sourceIds: Set<String>) = Unit
    }

    private fun song(id: Long, title: String) = Song(
        id = id, title = title, isExplicit = false, artist = "Artist", album = "Album", releaseYear = null,
        genre = "", audioFormat = "MP3", audioQuality = null, fileName = "$title.mp3", albumId = 1,
        durationMs = 1000, trackNumber = id.toInt(), discNumber = 1, dateAddedSeconds = 1,
        dateModifiedSeconds = 10, libraryPath = "Music/$title.mp3",
        uri = Uri.parse("content://media/external/audio/media/$id"), artUri = null, metadataResolved = true,
    )
}
