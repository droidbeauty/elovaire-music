package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.data.audio.AudioTagIO
import elovaire.music.droidbeauty.app.data.audio.EmbeddedTagMetadataReader
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.jaudiotagger.tag.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkMetadataParityTest {
    @Test
    fun boundedFlacMp4VorbisAndWavTagsUseTheLocalParser() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        val source = NetworkLibrarySource(id, "NAS", NetworkLibraryProtocol.Smb, "nas.example", "Music", "user", "test-$id")
        val sources = NetworkLibrarySourceStore(context)
        val credentials = NetworkCredentialStore(context)
        var bytes = byteArrayOf()
        val fileSystem = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials) = NetworkProbeResult(NetworkAvailability.Available)
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int) =
                NetworkListingResult.Complete(emptyList())
            override fun openBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String,
                position: Long, length: Long): NetworkReadHandle {
                val end = if (length < 0) bytes.size else minOf(bytes.size.toLong(), position + length).toInt()
                val segment = bytes.copyOfRange(position.toInt(), end)
                return NetworkReadHandle(ByteArrayInputStream(segment), segment.size.toLong()) { }
            }
        }
        val registry = NetworkFileSystemRegistry(sources, credentials, mapOf(NetworkLibraryProtocol.Smb to fileSystem))
        try {
            sources.upsert(source)
            credentials.put(id, source.credentialKey, NetworkCredentials("user", "secret"))
            for (extension in listOf("flac", "m4a", "ogg", "wav")) {
                val original = File.createTempFile("metadata-original-", ".$extension", context.cacheDir)
                try {
                    InstrumentationRegistry.getInstrumentation().context.assets.open("media-metadata/write-fixture.$extension")
                        .use { input -> original.outputStream().use(input::copyTo) }
                    val sourceBytes = original.readBytes()
                    val padding = 2 * 1024 * 1024
                    bytes = if (extension == "flac") {
                        ByteArray(sourceBytes.size + padding).also { sourceBytes.copyInto(it) }
                    } else if (extension == "ogg") {
                        ByteArray(sourceBytes.size + padding).also {
                            sourceBytes.copyInto(it)
                            sourceBytes.copyInto(it, it.size - sourceBytes.size)
                        }
                    } else if (extension == "wav") {
                        ByteArray(sourceBytes.size + padding).also {
                            sourceBytes.copyInto(it, 0, 0, 36)
                            sourceBytes.copyInto(it, 36, 110, sourceBytes.size)
                            sourceBytes.copyInto(it, sourceBytes.size + padding - 74, 36, 110)
                            ByteBuffer.wrap(it, 4, 4).order(java.nio.ByteOrder.LITTLE_ENDIAN).putInt(it.size - 8)
                            ByteBuffer.wrap(it, 40, 4).order(java.nio.ByteOrder.LITTLE_ENDIAN).putInt(4410 + padding)
                        }
                    } else {
                        val moovOffset = 4504
                        ByteArray(sourceBytes.size + padding).also {
                            sourceBytes.copyInto(it, 0, 0, moovOffset)
                            sourceBytes.copyInto(it, moovOffset + padding, moovOffset)
                            ByteBuffer.wrap(it, 36, 4).putInt(4468 + padding)
                        }
                    }
                    val reserve = maxOf(512L * 1024L * 1024L, context.cacheDir.totalSpace / 20L)
                    val result = NetworkMetadataReader(registry, context, availableSpace = { reserve + 1024 * 1024L })
                        .read(source, NetworkFileEntry("song.$extension", false, sizeBytes = bytes.size.toLong()))
                    assertEquals(EmbeddedTagMetadataReader(context).read(original.absolutePath), result?.embedded)
                    assertTrue(result?.embedded != null)
                } finally {
                    original.delete()
                }
            }
        } finally {
            registry.release()
            sources.remove(id)
            credentials.remove(source.credentialKey)
        }
    }

    @Test
    fun remoteTagsMatchLocalParserAndForcedScanRereadsUnchangedRevision() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File.createTempFile("nas-tags-", ".mp3", context.cacheDir)
        InstrumentationRegistry.getInstrumentation().context.assets.open("media-metadata/write-fixture.mp3").use { input ->
            file.outputStream().use(input::copyTo)
        }
        AudioTagIO.edit(file) {
            it.setField(FieldKey.TITLE, "Title One")
            it.setField(FieldKey.COMMENT, "Book description")
        }
        var bytes = file.readBytes()
        val id = UUID.randomUUID().toString()
        val source = NetworkLibrarySource(id, "NAS", NetworkLibraryProtocol.Smb, "nas.example", "Music", "user", "test-$id")
        val sources = NetworkLibrarySourceStore(context)
        val credentials = NetworkCredentialStore(context)
        val database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
        val fs = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials) = NetworkProbeResult(NetworkAvailability.Available)
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int) =
                NetworkListingResult.Complete(listOf(NetworkFileEntry("song.mp3", false, sizeBytes = bytes.size.toLong(), modifiedAtMs = 1)))
            override fun openBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String, position: Long, length: Long): NetworkReadHandle {
                val end = if (length < 0) bytes.size else minOf(bytes.size.toLong(), position + length).toInt()
                val segment = bytes.copyOfRange(position.toInt(), end)
                return NetworkReadHandle(ByteArrayInputStream(segment), segment.size.toLong()) { }
            }
        }
        val registry = NetworkFileSystemRegistry(sources, credentials, mapOf(NetworkLibraryProtocol.Smb to fs))
        try {
            sources.upsert(source)
            credentials.put(id, source.credentialKey, NetworkCredentials("user", "secret"))
            val entry = NetworkFileEntry("song.mp3", false, sizeBytes = bytes.size.toLong(), modifiedAtMs = 1)
            val remote = NetworkMetadataReader(registry, context).read(source, entry)
            assertEquals(EmbeddedTagMetadataReader(context).read(file.absolutePath), remote?.embedded)
            assertEquals("Title One", remote?.embedded?.title)
            assertEquals("Title One", NetworkMetadataReader(registry, context).read(source, entry.copy(sizeBytes = null))?.embedded?.title)
            val constrained = NetworkMetadataReader(registry, context, availableSpace = { 0L }).read(source, entry)
            assertEquals(false, constrained?.succeeded)
            assertEquals("Title One", constrained?.title)
            val ordinaryBytes = bytes
            bytes = ByteArray(2 * 1024 * 1024).also { ordinaryBytes.copyInto(it) }
            val reserve = maxOf(512L * 1024L * 1024L, context.cacheDir.totalSpace / 20L)
            val sparse = NetworkMetadataReader(registry, context, availableSpace = { reserve + 1024 * 1024L })
                .read(source, entry.copy(sizeBytes = bytes.size.toLong()))
            assertEquals(true, sparse?.succeeded)
            assertEquals(EmbeddedTagMetadataReader(context).read(file.absolutePath), sparse?.embedded)
            bytes = ordinaryBytes
            val temporaryFiles = context.cacheDir.listFiles().orEmpty().filter { it.name.startsWith("network-tag-") }.toSet()
            assertEquals(null, NetworkMetadataReader(registry, context).read(source, entry.copy(sizeBytes = bytes.size.toLong() + 1)))
            assertEquals(temporaryFiles, context.cacheDir.listFiles().orEmpty().filter { it.name.startsWith("network-tag-") }.toSet())

            val inventory = NetworkInventoryStore(context, database.networkInventoryDao())
            val scanner = NetworkLibraryScanner(context, registry, inventory)
            val first = scanner.scanWithStatus(listOf(source), true, true).songs.single()
            assertEquals("Title One", first.title)
            assertEquals("Book description", first.description)
            assertEquals(first.description, inventory.load(source).single().song.description)
            val cached = inventory.load(source).single()
            inventory.replace(source, listOf(cached.copy(song = cached.song.copy(
                title = "Previously verified", metadataResolved = false,
            ))), NetworkAvailability.Available, System.currentTimeMillis())
            val constrainedScanner = NetworkLibraryScanner(context, registry, inventory,
                metadataReaderOverride = NetworkMetadataReader(registry, context, availableSpace = { 0L }))
            val preserved = constrainedScanner.scanWithStatus(listOf(source), false, true).songs.single()
            assertEquals("Previously verified", preserved.title)
            assertEquals(false, preserved.metadataResolved)
            AudioTagIO.edit(file) { it.setField(FieldKey.TITLE, "Title Two") }
            val updated = file.readBytes()
            assertEquals(bytes.size, updated.size)
            bytes = updated
            val refreshed = scanner.scanWithStatus(listOf(source), true, true)
            assertTrue(refreshed.isComplete)
            assertEquals("Title Two", refreshed.songs.single().title)
        } finally {
            registry.release()
            sources.remove(id)
            credentials.remove(source.credentialKey)
            database.close()
            file.delete()
        }
    }
}
