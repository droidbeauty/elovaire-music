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
import kotlinx.coroutines.runBlocking
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

}
