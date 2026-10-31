package elovaire.music.droidbeauty.app.data.library.network

import androidx.room.Room
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.playback.NetworkDataSourceFactory
import java.io.ByteArrayInputStream
import java.io.IOException
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
    fun credentialChangeDuringListingCannotPublishOrCommitStaleResults() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        val source = NetworkLibrarySource(id, "NAS", NetworkLibraryProtocol.Smb, "nas.example", "Music", "user", "test-$id")
        val sources = NetworkLibrarySourceStore(context)
        val credentialStore = NetworkCredentialStore(context)
        val database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
        var replaceCredentials = false
        var failListing = false
        val fileSystem = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials) = NetworkProbeResult(NetworkAvailability.Available)
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int): NetworkListingResult {
                if (replaceCredentials) credentialStore.put(id, source.credentialKey, NetworkCredentials("user", "new"))
                if (failListing) throw IOException("old connection failed")
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
