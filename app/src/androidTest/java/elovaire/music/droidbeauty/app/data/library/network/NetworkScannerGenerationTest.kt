package elovaire.music.droidbeauty.app.data.library.network

import androidx.room.Room
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.content.Context
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.playback.NetworkDataSourceFactory
import elovaire.music.droidbeauty.app.data.mutation.MediaFileMutationRunner
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationOperation
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationType
import elovaire.music.droidbeauty.app.data.mutation.NoOpMediaMutationFaultInjector
import elovaire.music.droidbeauty.app.data.mutation.VerifiedMediaFileMutationResult
import elovaire.music.droidbeauty.app.data.mutation.VerifiedMediaFileMutationTransaction
import elovaire.music.droidbeauty.app.core.backend.NoOpBackendEventSink
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.data.audio.AudioTagIO
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.EditableAlbumTrack
import elovaire.music.droidbeauty.app.data.tags.TagFieldEdit
import org.jaudiotagger.tag.FieldKey
import elovaire.music.droidbeauty.app.domain.kernel.MediaMutationStatus
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@androidx.annotation.OptIn(UnstableApi::class)
class NetworkScannerGenerationTest {
    @Test
    fun networkTagEditorWritesAndVerifiesUsingLocalTagImplementation() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        val source = NetworkLibrarySource(id, "NAS", NetworkLibraryProtocol.WebDav, "https://nas.example", "Music", "user", "test-$id")
        val sources = NetworkLibrarySourceStore(context)
        val credentials = NetworkCredentialStore(context)
        val database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
        var contents = InstrumentationRegistry.getInstrumentation().context.assets.open("media-metadata/write-fixture.mp3").use { it.readBytes() }
        val fileSystem = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials) = NetworkProbeResult(NetworkAvailability.Available)
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int) =
                NetworkListingResult.Complete(emptyList())
            override fun openBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String, position: Long, length: Long): NetworkReadHandle {
                val end = if (length < 0) contents.size else minOf(contents.size.toLong(), position + length).toInt()
                val bytes = contents.copyOfRange(position.toInt(), end)
                return NetworkReadHandle(ByteArrayInputStream(bytes), bytes.size.toLong()) { }
            }
            override fun replaceBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String,
                expectedOriginal: File, replacement: File, operationId: String, recordTemporary: (String) -> Unit,
                resolveTemporary: () -> Unit, onCommitStarting: () -> Unit) {
                if (!expectedOriginal.readBytes().contentEquals(contents)) throw IOException("Remote file changed")
                recordTemporary("Music/.elovaire-$operationId.tmp")
                onCommitStarting()
                contents = replacement.readBytes()
                resolveTemporary()
            }
        }
        val registry = NetworkFileSystemRegistry(sources, credentials, mapOf(NetworkLibraryProtocol.WebDav to fileSystem))
        val runner = MediaFileMutationRunner(context, networkRegistryProvider = { registry })
        val journal = MediaMutationJournal(database.mediaMutationDao(), backendEventSink = NoOpBackendEventSink, recoveryRunner = runner)
        val checkFile = File.createTempFile("network-tag-result-", ".mp3", context.cacheDir)
        try {
            sources.upsert(source)
            credentials.put(id, source.credentialKey, NetworkCredentials("user", "secret"))
            val song = Song(id = 1, title = "Old", isExplicit = false, artist = "Artist", album = "Album",
                releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null, fileName = "song.mp3",
                albumId = 2, durationMs = 1, trackNumber = 1, discNumber = 1, dateAddedSeconds = 0,
                uri = NetworkResourceUri.create(id, "Music/song.mp3"), artUri = null)
            val album = Album(song.albumId, song.album, song.artist, null, 1, song.durationMs, listOf(song))
            val request = AlbumTagEditRequest(album, TagFieldEdit.Value("Edited Album"), TagFieldEdit.Value("Edited Artist"),
                TagFieldEdit.Value(2026), TagFieldEdit.Value("Genre"), null,
                tracks = listOf(EditableAlbumTrack(song.id, "Edited Title", "Edited Artist", 1, 1)))
            val inventory = NetworkInventoryStore(context, database.networkInventoryDao())
            inventory.replace(source, listOf(NetworkInventoryEntry(NetworkFileEntry("Music/song.mp3", false,
                sizeBytes = contents.size.toLong(), modifiedAtMs = 1), song)), NetworkAvailability.Available, 1L)
            val editor = AlbumTagEditorService(context, mediaMutationJournal = journal, networkRegistryProvider = { registry })
            val result = editor.applyEdits(request)
            assertTrue(result.failures.toString(), result.failures.isEmpty())
            assertEquals(listOf(song.id), result.editedSongIds)
            checkFile.writeBytes(contents)
            val tag = checkNotNull(AudioTagIO.read(checkFile))
            assertEquals("Edited Title", tag.getFirst(FieldKey.TITLE))
            assertEquals("Edited Album", tag.getFirst(FieldKey.ALBUM))
            NetworkLibraryScanner(context, registry, inventory).applyVerifiedTags(result.editedSongs)
            val cached = inventory.load(source).single().song
            assertEquals("Edited Title", cached.title)
            assertEquals("Edited Album", cached.album)
            assertFalse(cached.metadataResolved)
            inventory.applyVerifiedTags(source.copy(server = "https://other.example"), listOf(song))
            assertEquals("Edited Title", inventory.load(source).single().song.title)
        } finally {
            checkFile.delete()
            journal.close()
            database.close()
            registry.release()
            sources.remove(id)
            credentials.remove(source.credentialKey)
        }
    }

    @Test
    fun interruptedNetworkEditRecoversWithoutOverwritingLaterChanges() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        val source = NetworkLibrarySource(id, "NAS", NetworkLibraryProtocol.WebDav, "https://nas.example", "Music", "user", "test-$id")
        val sources = NetworkLibrarySourceStore(context)
        val credentials = NetworkCredentialStore(context)
        val database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
        var contents = "edited".toByteArray()
        var cleaned = false
        val fileSystem = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials) = NetworkProbeResult(NetworkAvailability.Available)
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int) =
                NetworkListingResult.Complete(emptyList())
            override fun openBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String, position: Long, length: Long): NetworkReadHandle {
                val end = if (length < 0) contents.size else minOf(contents.size.toLong(), position + length).toInt()
                val bytes = contents.copyOfRange(position.toInt(), end)
                return NetworkReadHandle(ByteArrayInputStream(bytes), bytes.size.toLong()) { }
            }
            override fun cleanupTemporaryBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, targetPath: String,
                temporaryPath: String, operationId: String, expectedFiles: List<File>, onDeleteStarting: () -> Unit) {
                onDeleteStarting()
                cleaned = true
            }
            override fun replaceBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String,
                expectedOriginal: File, replacement: File, operationId: String, recordTemporary: (String) -> Unit,
                resolveTemporary: () -> Unit, onCommitStarting: () -> Unit) {
                if (!expectedOriginal.readBytes().contentEquals(contents)) throw IOException("Remote file changed")
                recordTemporary("Music/.elovaire-$operationId.tmp")
                onCommitStarting()
                contents = replacement.readBytes()
                resolveTemporary()
            }
        }
        val registry = NetworkFileSystemRegistry(sources, credentials, mapOf(NetworkLibraryProtocol.WebDav to fileSystem))
        val runner = MediaFileMutationRunner(context, networkRegistryProvider = { registry })
        val journal = MediaMutationJournal(database.mediaMutationDao(), backendEventSink = NoOpBackendEventSink, recoveryRunner = runner)
        val files = mutableListOf<File>()
        try {
            sources.upsert(source)
            credentials.put(id, source.credentialKey, NetworkCredentials("user", "secret"))
            val song = Song(id = 1, title = "Song", isExplicit = false, artist = "Artist", album = "Album",
                releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null, fileName = "song.mp3",
                albumId = 2, durationMs = 1, trackNumber = 1, discNumber = 1, dateAddedSeconds = 0,
                uri = NetworkResourceUri.create(id, "Music/song.mp3"), artUri = null)
            val mutation = journal.create(MediaMutationOperation(type = MediaMutationType.TagEdit, songId = song.id, uri = song.uri))
            listOf(MediaMutationStatus.PreflightPassed, MediaMutationStatus.TempWritten, MediaMutationStatus.TempVerified)
                .forEach { journal.mark(mutation, it) }
            val backup = runner.createTempFile(song, "backup").apply { writeText("original") }.also(files::add)
            val replacement = runner.createTempFile(song, "network-working").apply { writeText("edited") }.also(files::add)
            journal.retainBackup(mutation, backup, replacement, NetworkSourceIdentity.locationFingerprint(source))
            journal.recordNetworkTemporary(mutation, "Music/.elovaire-$mutation.tmp")
            journal.close()
            val recovery = MediaMutationJournal(database.mediaMutationDao(), backendEventSink = NoOpBackendEventSink, recoveryRunner = runner)
            try {
                contents = "external".toByteArray()
                assertEquals(0, recovery.recoverNetworkIncomplete())
                assertEquals("external", contents.toString(Charsets.UTF_8))
                assertTrue(backup.exists())
                contents = "edited".toByteArray()
                assertEquals(1, recovery.recoverNetworkIncomplete())
                assertTrue(cleaned)
                assertEquals("original", contents.toString(Charsets.UTF_8))
                assertFalse(backup.exists())
                assertFalse(replacement.exists())
            } finally {
                recovery.close()
            }
        } finally {
            files.forEach(File::delete)
            journal.close()
            database.close()
            registry.release()
            sources.remove(id)
            credentials.remove(source.credentialKey)
        }
    }

    @Test
    fun networkTagTransactionKeepsOriginalWhenRemoteBytesChange() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        val source = NetworkLibrarySource(id, "NAS", NetworkLibraryProtocol.WebDav, "https://nas.example", "Music", "user", "test-$id")
        val sources = NetworkLibrarySourceStore(context)
        val credentials = NetworkCredentialStore(context)
        val database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
        val journal = MediaMutationJournal(database.mediaMutationDao(), backendEventSink = NoOpBackendEventSink)
        var contents = "original".toByteArray()
        val fileSystem = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials) = NetworkProbeResult(NetworkAvailability.Available)
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int) =
                NetworkListingResult.Complete(emptyList())
            override fun openBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String, position: Long, length: Long): NetworkReadHandle {
                val end = if (length < 0) contents.size else minOf(contents.size.toLong(), position + length).toInt()
                val bytes = contents.copyOfRange(position.toInt(), end)
                return NetworkReadHandle(ByteArrayInputStream(bytes), bytes.size.toLong()) { }
            }
            override fun replaceBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String,
                expectedOriginal: File, replacement: File, operationId: String, recordTemporary: (String) -> Unit,
                resolveTemporary: () -> Unit, onCommitStarting: () -> Unit) {
                if (!expectedOriginal.readBytes().contentEquals(contents)) throw IOException("Remote file changed")
                recordTemporary("Music/.elovaire-$operationId.tmp")
                onCommitStarting()
                contents = replacement.readBytes()
                resolveTemporary()
            }
        }
        val registry = NetworkFileSystemRegistry(sources, credentials, mapOf(NetworkLibraryProtocol.WebDav to fileSystem))
        try {
            sources.upsert(source)
            credentials.put(id, source.credentialKey, NetworkCredentials("user", "secret"))
            val song = Song(id = 1, title = "Song", isExplicit = false, artist = "Artist", album = "Album",
                releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null, fileName = "song.mp3",
                albumId = 2, durationMs = 1, trackNumber = 1, discNumber = 1, dateAddedSeconds = 0,
                uri = NetworkResourceUri.create(id, "Music/song.mp3"), artUri = null)
            val runner = MediaFileMutationRunner(context, networkRegistryProvider = { registry })
            val transaction = VerifiedMediaFileMutationTransaction(runner, journal, NoOpMediaMutationFaultInjector)
            val operation = MediaMutationOperation(type = MediaMutationType.TagEdit, songId = song.id, uri = song.uri)
            val success = transaction.execute(song, operation, mutateWorkingCopy = { it.writeText("edited") },
                verifyWorkingCopy = {}, verifyPersistedCopy = {})
            assertTrue(success is VerifiedMediaFileMutationResult.Success)
            assertEquals("edited", contents.toString(Charsets.UTF_8))
            val failed = transaction.execute(song, operation, mutateWorkingCopy = {
                it.writeText("other edit")
                contents = "external change".toByteArray()
            }, verifyWorkingCopy = {}, verifyPersistedCopy = {})
            assertTrue(failed is VerifiedMediaFileMutationResult.Failure && failed.rollbackFailed)
            assertEquals("external change", contents.toString(Charsets.UTF_8))
            val row = checkNotNull(database.mediaMutationDao().mutation((failed as VerifiedMediaFileMutationResult.Failure).mutationId!!))
            val backup = File(checkNotNull(row.recoveryBackupPath))
            val replacement = File(checkNotNull(row.recoveryReplacementPath))
            assertTrue(backup.isFile)
            assertTrue(replacement.isFile)
            backup.delete()
            replacement.delete()
            Unit
        } finally {
            journal.close()
            database.close()
            registry.release()
            sources.remove(id)
            credentials.remove(source.credentialKey)
        }
    }

    @Test
    fun networkMutationBackupReadsAndVerifiesTheRemoteBytes() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        val source = NetworkLibrarySource(id, "NAS", NetworkLibraryProtocol.Smb, "nas.example", "Music", "user", "test-$id")
        val sources = NetworkLibrarySourceStore(context)
        val credentialStore = NetworkCredentialStore(context)
        var contents = "original audio".toByteArray()
        val fileSystem = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials) = NetworkProbeResult(NetworkAvailability.Available)
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int) =
                NetworkListingResult.Complete(emptyList())
            override fun openBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String, position: Long, length: Long): NetworkReadHandle {
                val end = if (length < 0) contents.size else minOf(contents.size.toLong(), position + length).toInt()
                val bytes = contents.copyOfRange(position.toInt(), end)
                return NetworkReadHandle(ByteArrayInputStream(bytes), bytes.size.toLong()) { }
            }
        }
        val registry = NetworkFileSystemRegistry(sources, credentialStore, mapOf(NetworkLibraryProtocol.Smb to fileSystem))
        var backup: File? = null
        try {
            sources.upsert(source)
            credentialStore.put(id, source.credentialKey, NetworkCredentials("user", "secret"))
            val song = Song(id = 1, title = "Song", isExplicit = false, artist = "Artist", album = "Album",
                releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null, fileName = "song.mp3",
                albumId = 2, durationMs = 1, trackNumber = 1, discNumber = 1, dateAddedSeconds = 0,
                uri = NetworkResourceUri.create(id, "song.mp3"), artUri = null)
            val runner = MediaFileMutationRunner(context, networkRegistryProvider = { registry })
            runner.preflight(song)
            backup = runner.copySongToTemp(song, "backup")
            assertEquals("original audio", backup.readText())
            assertTrue(runner.originalMatchesBackup(song.uri, backup))
            contents = "changed audio!".toByteArray()
            assertFalse(runner.originalMatchesBackup(song.uri, backup))
            assertEquals(NetworkSourceIdentity.locationFingerprint(source), runner.networkSourceFingerprint(song.uri))
        } finally {
            backup?.delete()
            registry.release()
            sources.remove(id)
            credentialStore.remove(source.credentialKey)
        }
    }

    @Test
    fun credentialChangeDuringReplacementPreventsCommit() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        val source = NetworkLibrarySource(id, "NAS", NetworkLibraryProtocol.WebDav, "https://nas.example", "Music", "user", "test-$id")
        val sources = NetworkLibrarySourceStore(context)
        val credentialStore = NetworkCredentialStore(context)
        var committed = false
        val fileSystem = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials) = NetworkProbeResult(NetworkAvailability.Available)
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int) =
                NetworkListingResult.Complete(emptyList())
            override fun openBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String, position: Long, length: Long): NetworkReadHandle =
                error("Not used")
            override fun replaceBlocking(
                source: NetworkLibrarySource,
                credentials: NetworkCredentials,
                path: String,
                expectedOriginal: File,
                replacement: File,
                operationId: String,
                recordTemporary: (String) -> Unit,
                resolveTemporary: () -> Unit,
                onCommitStarting: () -> Unit,
            ) {
                credentialStore.put(id, source.credentialKey, NetworkCredentials("user", "changed"))
                onCommitStarting()
            }
        }
        val registry = NetworkFileSystemRegistry(sources, credentialStore, mapOf(NetworkLibraryProtocol.WebDav to fileSystem))
        val original = File.createTempFile("network-original-", ".tmp", context.cacheDir)
        val replacement = File.createTempFile("network-replacement-", ".tmp", context.cacheDir)
        try {
            sources.upsert(source)
            credentialStore.put(id, source.credentialKey, NetworkCredentials("user", "initial"))
            org.junit.Assert.assertThrows(IOException::class.java) {
                registry.replaceBlocking(id, "Music/song.mp3", original, replacement, "edit-1",
                    NetworkSourceIdentity.locationFingerprint(source), {}, {}) { committed = true }
            }
            assertFalse(committed)
        } finally {
            registry.release()
            sources.remove(id)
            credentialStore.remove(source.credentialKey)
            original.delete()
            replacement.delete()
        }
    }

    @Test
    fun credentialChangeDuringListingCannotPublishOrCommitStaleResults() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        val source = NetworkLibrarySource(id, "NAS", NetworkLibraryProtocol.Smb, "nas.example", "Music", "user", "test-$id")
        val sources = NetworkLibrarySourceStore(context)
        val credentialStore = NetworkCredentialStore(context)
        val database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
        var replaceCredentials = false
        var failListing = false
        var removeSourceBeforeFailure = false
        val fileSystem = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials) = NetworkProbeResult(NetworkAvailability.Available)
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int): NetworkListingResult {
                if (replaceCredentials) credentialStore.put(id, source.credentialKey, NetworkCredentials("user", "new"))
                if (failListing) {
                    if (removeSourceBeforeFailure) runBlocking { sources.remove(id) }
                    throw IOException("old connection failed")
                }
                return NetworkListingResult.Complete(listOf(NetworkFileEntry("song.mp3", false, sizeBytes = 12, modifiedAtMs = 1)))
            }
            override fun openBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String, position: Long, length: Long): NetworkReadHandle {
                assertEquals("song.mp3", path)
                val data = "hello world!".toByteArray()
                val end = if (length < 0) data.size else minOf(data.size.toLong(), position + length).toInt()
                val segment = data.copyOfRange(position.toInt(), end)
                return NetworkReadHandle(ByteArrayInputStream(segment), segment.size.toLong()) { }
            }
        }
        val registry = NetworkFileSystemRegistry(sources, credentialStore, mapOf(NetworkLibraryProtocol.Smb to fileSystem))
        val availability = mutableListOf<NetworkProbeResult>()
        try {
            sources.upsert(source)
            credentialStore.put(id, source.credentialKey, NetworkCredentials("user", "old"))
            val inventory = NetworkInventoryStore(context, database.networkInventoryDao())
            val scanner = NetworkLibraryScanner(context, registry, inventory, onAvailabilityChanged = { _, result -> availability += result })
            val initial = scanner.scanWithStatus(listOf(source), forceRefresh = true, enrichMetadata = false)
            assertTrue(initial.isComplete)
            assertEquals(1, initial.songs.size)
            val playback = NetworkDataSourceFactory(DefaultDataSource.Factory(context)) { registry }.createDataSource()
            try {
                val request = DataSpec.Builder().setUri(initial.songs.single().uri).setPosition(6).setLength(5).build()
                assertEquals(5L, playback.open(request))
                val buffer = ByteArray(5)
                assertEquals(5, playback.read(buffer, 0, buffer.size))
                assertEquals("world", buffer.toString(Charsets.UTF_8))
                assertEquals(-1, playback.read(buffer, 0, buffer.size))
            } finally {
                playback.close()
            }
            availability.clear()
            replaceCredentials = true
            failListing = true
            val result = scanner.scanWithStatus(listOf(source), forceRefresh = true, enrichMetadata = false)
            assertFalse(result.isComplete)
            assertEquals(initial.songs, result.songs)
            assertTrue(availability.isEmpty())
            assertEquals(initial.songs, inventory.load(source).map { it.song })

            replaceCredentials = false
            removeSourceBeforeFailure = true
            val removedSourceResult = scanner.scanWithStatus(listOf(source), forceRefresh = true, enrichMetadata = false)
            assertFalse(removedSourceResult.isComplete)
            assertTrue(removedSourceResult.songs.isEmpty())
        } finally {
            registry.release()
            sources.remove(id)
            credentialStore.remove(source.credentialKey)
            database.close()
        }
    }

    @Test
    fun cancellationAfterSourceCommitPreservesCredentialsAndRecoveryMarker() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        val source = NetworkLibrarySource(id, "NAS", NetworkLibraryProtocol.Smb, "nas.example", "Music", "user", "test-$id")
        val credentials = NetworkCredentials("user", "secret")
        val pendingDispatch = CompletableDeferred<Runnable>()
        val pausedDispatcher = object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                check(pendingDispatch.complete(block))
            }
        }
        val sources = NetworkLibrarySourceStore(context, pausedDispatcher)
        val credentialStore = NetworkCredentialStore(context)
        val database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
        val journal = NetworkSourceMutationJournal(context)
        val coordinator = NetworkSourceCoordinator(
            sourceStore = sources,
            credentialStoreProvider = { credentialStore },
            registryProvider = { error("Probe must not run after cancellation") },
            inventoryStore = NetworkInventoryStore(context, database.networkInventoryDao()),
            mutationJournal = journal,
        )
        try {
            val save = async(start = CoroutineStart.UNDISPATCHED) { coordinator.save(source, credentials) }
            pendingDispatch.await().run()
            save.cancelAndJoin()

            assertEquals(source, sources.sources.value.first { it.id == id })
            assertEquals(NetworkCredentialReadResult.Available(credentials), credentialStore.read(id, source.credentialKey))
            assertTrue(journal.pending().any { it.sourceId == id })
        } finally {
            journal.clear(id)
            NetworkLibrarySourceStore(context).remove(id)
            credentialStore.remove(source.credentialKey)
            database.close()
        }
    }

    @Test
    fun emptyRemoteArtworkDoesNotPublishAnInvalidArtworkFile() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        val source = NetworkLibrarySource(id, "NAS", NetworkLibraryProtocol.Smb, "nas.example", "Music", "user", "test-$id")
        val sources = NetworkLibrarySourceStore(context)
        val credentialStore = NetworkCredentialStore(context)
        val database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
        val fileSystem = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials) = NetworkProbeResult(NetworkAvailability.Available)
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int) =
                NetworkListingResult.Complete(listOf(NetworkFileEntry("song.mp3", false), NetworkFileEntry("cover.jpg", false)))
            override fun openBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String, position: Long, length: Long) =
                NetworkReadHandle(ByteArrayInputStream(byteArrayOf()), 0L) { }
        }
        val registry = NetworkFileSystemRegistry(sources, credentialStore, mapOf(NetworkLibraryProtocol.Smb to fileSystem))
        try {
            sources.upsert(source)
            credentialStore.put(id, source.credentialKey, NetworkCredentials("user", "secret"))
            val inventory = NetworkInventoryStore(context, database.networkInventoryDao())
            val result = NetworkLibraryScanner(context, registry, inventory)
                .scanWithStatus(listOf(source), forceRefresh = true, enrichMetadata = true)
            assertTrue(result.isComplete)
            assertEquals(null, result.songs.single().artUri)
        } finally {
            registry.release()
            sources.remove(id)
            credentialStore.remove(source.credentialKey)
            database.close()
        }
    }

}
