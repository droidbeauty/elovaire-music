package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.core.backend.NoOpBackendEventSink
import elovaire.music.droidbeauty.app.data.audio.AudioTagIO
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.mutation.MediaFileMutationRunner
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.tags.EditableAlbumTrack
import elovaire.music.droidbeauty.app.data.tags.TagFieldEdit
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.jaudiotagger.tag.FieldKey
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SmbServerIntegrationTest {
    @Test(timeout = 30000L)
    fun tagEditorWritesRealMp3ThroughSmb() = runSmbIntegration {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        val source = NetworkLibrarySource(id, "Test", NetworkLibraryProtocol.Smb,
            "127.0.0.1:1445", "NAS", "test", "smb-test-$id")
        val sources = NetworkLibrarySourceStore(context)
        val credentials = NetworkCredentialStore(context)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val filesystem = SmbNetworkFileSystem(scope)
        val registry = NetworkFileSystemRegistry(sources, credentials, mapOf(NetworkLibraryProtocol.Smb to filesystem))
        val database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
        val runner = MediaFileMutationRunner(context, networkRegistryProvider = { registry })
        val journal = MediaMutationJournal(database.mediaMutationDao(), backendEventSink = NoOpBackendEventSink,
            recoveryRunner = runner)
        val downloaded = File.createTempFile("smb-tags-", ".mp3", context.cacheDir)
        try {
            sources.upsert(source)
            credentials.put(id, source.credentialKey, NetworkCredentials("test", "test"))
            val song = Song(id = 1, title = "Old", isExplicit = false, artist = "Artist", album = "Album",
                releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null, fileName = "tagged.mp3",
                albumId = 2, durationMs = 1, trackNumber = 1, discNumber = 1, dateAddedSeconds = 0,
                uri = NetworkResourceUri.create(id, "tagged.mp3"), artUri = null)
            val album = Album(song.albumId, song.album, song.artist, null, 1, song.durationMs, listOf(song))
            val request = AlbumTagEditRequest(album, TagFieldEdit.Value("SMB Album"), TagFieldEdit.Value("SMB Artist"),
                TagFieldEdit.Value(2026), TagFieldEdit.Value("Genre"), null,
                tracks = listOf(EditableAlbumTrack(song.id, "SMB Title", "SMB Artist", 1, 1)))
            val result = AlbumTagEditorService(context, mediaMutationJournal = journal,
                networkRegistryProvider = { registry }).applyEdits(request)
            assertTrue(result.failures.toString(), result.failures.isEmpty())
            registry.openBlocking(id, "tagged.mp3", 0, -1, NetworkReadPurpose.Metadata).use { handle ->
                downloaded.outputStream().use { handle.input.copyTo(it) }
            }
            val tag = checkNotNull(AudioTagIO.read(downloaded))
            assertEquals("SMB Title", tag.getFirst(FieldKey.TITLE))
            assertEquals("SMB Album", tag.getFirst(FieldKey.ALBUM))
        } finally {
            downloaded.delete()
            journal.close()
            database.close()
            registry.release()
            scope.cancel()
            sources.remove(id)
            credentials.remove(source.credentialKey)
        }
    }

    @Test(timeout = 30000L)
    fun stagedReplacementAndRecoveryUseSmbServerOperations() = runSmbIntegration {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val source = NetworkLibrarySource("integration", "Test", NetworkLibraryProtocol.Smb,
            "127.0.0.1:1445", "NAS", "test", "integration")
        val credentials = NetworkCredentials("test", "test")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val filesystem = SmbNetworkFileSystem(scope)
        val expected = File.createTempFile("smb-expected-", ".tmp", context.cacheDir).apply { writeText("original") }
        val replacement = File.createTempFile("smb-replacement-", ".tmp", context.cacheDir).apply { writeText("edited") }
        try {
            var temporary: String? = null
            fun readTarget() = filesystem.openBlocking(source, credentials, "song.mp3", 0, -1).use {
                it.input.readBytes().toString(Charsets.UTF_8)
            }
            assertEquals("original", readTarget())
            assertThrows(IOException::class.java) {
                filesystem.replaceBlocking(source, credentials, "song.mp3", expected, replacement, "integration-1",
                    { temporary = it }, { temporary = null }, { throw IOException("Stop before commit") })
            }
            assertEquals("original", readTarget())
            filesystem.cleanupTemporaryBlocking(source, credentials, "song.mp3", checkNotNull(temporary),
                "integration-1", listOf(expected, replacement), {})
            temporary = null
            filesystem.replaceBlocking(source, credentials, "song.mp3", expected, replacement, "integration-2",
                { temporary = it }, { temporary = null }, {})
            assertEquals(null, temporary)
            assertEquals("edited", readTarget())
            val listing = filesystem.listBlocking(source, credentials)
            assertTrue(listing.entries.any { it.path == "song.mp3" })
            assertTrue(listing.entries.none { it.path.startsWith(".elovaire-integration-") })
        } finally {
            filesystem.release()
            scope.cancel()
            expected.delete()
            replacement.delete()
        }
    }

    private fun runSmbIntegration(block: suspend () -> Unit) {
        if (InstrumentationRegistry.getArguments().getString("smbIntegration") != "true") return
        runBlocking { block() }
    }
}
