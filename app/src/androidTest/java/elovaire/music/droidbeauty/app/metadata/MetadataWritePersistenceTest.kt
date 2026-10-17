package elovaire.music.droidbeauty.app.metadata

import android.content.ContentResolver
import android.content.ContentValues
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.data.lyrics.EmbeddedLyricsWriteResult
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.AudioTagIO
import elovaire.music.droidbeauty.app.data.audio.PlaybackSupport
import elovaire.music.droidbeauty.app.data.audio.EmbeddedTagMetadataReader
import elovaire.music.droidbeauty.app.data.lyrics.EmbeddedLyricsWriter
import elovaire.music.droidbeauty.app.data.lyrics.AudioFileLyricsInspection
import elovaire.music.droidbeauty.app.data.lyrics.LocalLyricsResolver
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.tags.EditableAlbumTrack
import elovaire.music.droidbeauty.app.data.tags.TagFieldEdit
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.quality.deleteTestOwnedRows
import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlinx.coroutines.runBlocking
import org.jaudiotagger.tag.FieldKey
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MetadataWritePersistenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val testContext = InstrumentationRegistry.getInstrumentation().context
    private val resolver: ContentResolver = context.contentResolver
    private val insertedUris = mutableListOf<Uri>()

    @After
    fun tearDown() {
        try {
            resolver.deleteTestOwnedRows(insertedUris, "metadata persistence")
        } finally {
            insertedUris.clear()
        }
    }

    @Test
    fun mediaStoreMp3LyricsPersistInEmbeddedMetadata() {
        val song = insertFixtureSong("write-fixture.mp3", id = 101L)
        val plainLyrics = "Plain embedded lyrics\nSecond line"
        val plainResult = runBlocking { EmbeddedLyricsWriter(context).write(song, plainLyrics) }
        assertTrue(plainResult is EmbeddedLyricsWriteResult.Success)
        assertEquals(
            listOf(plainLyrics),
            AudioFileLyricsInspection.inspect(copyPersistedToTemp(song)).unsynced,
        )

        val lyrics = "[00:01.00]Elovaire metadata write test\n[00:02.00]Lyrics persisted line"

        val result = runBlocking { EmbeddedLyricsWriter(context).write(song, lyrics) }

        assertTrue(result is EmbeddedLyricsWriteResult.Success)
        val persisted = runBlocking { LocalLyricsResolver(context).resolve(song) }?.payload
        assertTrue(persisted?.isSynced == true)
        assertEquals(listOf(1_000L, 2_000L), persisted?.lines?.map { it.startTimeMs })
        assertEquals(
            listOf("Elovaire metadata write test", "Lyrics persisted line"),
            persisted?.lines?.map { it.text },
        )
        val raw = AudioFileLyricsInspection.inspect(copyPersistedToTemp(song))
        assertEquals(listOf(1_000L, 2_000L), raw.synced.single().map { it.startTimeMs })
        assertTrue(raw.unsynced.isEmpty())
    }

    @Test
    fun mediaStoreFlacLyricsPersistInEmbeddedMetadata() {
        val song = insertFixtureSong("write-fixture.flac", id = 102L)
        val lyrics = "[00:01.00]Elovaire metadata write test\n[00:02.00]Lyrics persisted line"

        val result = runBlocking { EmbeddedLyricsWriter(context).write(song, lyrics) }

        assertTrue(result is EmbeddedLyricsWriteResult.Success)
        val persisted = runBlocking { LocalLyricsResolver(context).resolve(song) }?.payload
        assertTrue(persisted?.isSynced == true)
        assertEquals(listOf(1_000L, 2_000L), persisted?.lines?.map { it.startTimeMs })
        val raw = AudioFileLyricsInspection.inspect(copyPersistedToTemp(song))
        assertEquals(listOf(1_000L, 2_000L), raw.synced.single().map { it.startTimeMs })
        assertTrue(raw.unsynced.isEmpty())
    }

    @Test
    fun mediaStoreM4aPlainLyricsPersistInEmbeddedMetadata() {
        val song = insertFixtureSong("write-fixture.m4a", id = 103L)
        val lyrics = "Plain embedded lyrics\nSecond line"

        val result = runBlocking { EmbeddedLyricsWriter(context).write(song, lyrics) }

        assertTrue(result is EmbeddedLyricsWriteResult.Success)
        val persisted = runBlocking { LocalLyricsResolver(context).resolve(song) }?.payload
        assertTrue(persisted?.isSynced == false)
        assertEquals(listOf("Plain embedded lyrics", "Second line"), persisted?.lines?.map { it.text })
        assertEquals(listOf(lyrics), AudioFileLyricsInspection.inspect(copyPersistedToTemp(song)).unsynced)
    }

    @Test
    fun mediaStoreM4aSyncedLyricsPersistAsLrcText() {
        val song = insertFixtureSong("write-fixture.m4a", id = 104L)
        val lyrics = "[00:01.00]Elovaire metadata write test\n[00:02.00]Lyrics persisted line"

        val result = runBlocking { EmbeddedLyricsWriter(context).write(song, lyrics) }

        assertTrue(result is EmbeddedLyricsWriteResult.Success)
        val persisted = runBlocking { LocalLyricsResolver(context).resolve(song) }?.payload
        assertTrue(persisted?.isSynced == true)
        assertEquals(listOf(1_000L, 2_000L), persisted?.lines?.map { it.startTimeMs })
        assertEquals(
            listOf("Elovaire metadata write test", "Lyrics persisted line"),
            persisted?.lines?.map { it.text },
        )
        val raw = AudioFileLyricsInspection.inspect(copyPersistedToTemp(song))
        assertTrue(raw.synced.isEmpty())
        assertEquals(listOf(lyrics), raw.unsynced)
    }

    @Test
    fun mediaStoreMp3TagEditsPersistInEmbeddedMetadata() {
        val song = insertFixtureSong("write-fixture.mp3", id = 201L)

        val result = runBlocking { AlbumTagEditorService(context).applyEdits(editRequest(song)) }

        assertTrue(result.failures.joinToString { it.reason }, result.failures.isEmpty())
        assertEquals(listOf(song.id), result.editedSongIds)
        val tag = persistedTag(song)
        assertEquals("Persisted Track Title", tag.getFirst(FieldKey.TITLE))
        assertEquals("Persisted Track Artist", tag.getFirst(FieldKey.ARTIST))
        assertEquals("Persisted Album Title", tag.getFirst(FieldKey.ALBUM))
        assertEquals("Persisted Album Artist", tag.getFirst(FieldKey.ALBUM_ARTIST))
        assertEquals("2026", tag.getFirst(FieldKey.YEAR))
        assertEquals("Persistence Genre", tag.getFirst(FieldKey.GENRE))
    }

    @Test
    fun mediaStoreFlacTagEditsPersistInEmbeddedMetadata() {
        val song = insertFixtureSong("write-fixture.flac", id = 202L)

        val result = runBlocking { AlbumTagEditorService(context).applyEdits(editRequest(song)) }

        assertTrue(result.failures.joinToString { it.reason }, result.failures.isEmpty())
        assertEquals(listOf(song.id), result.editedSongIds)
        val tag = persistedTag(song)
        assertEquals("Persisted Track Title", tag.getFirst(FieldKey.TITLE))
        assertEquals("Persisted Track Artist", tag.getFirst(FieldKey.ARTIST))
        assertEquals("Persisted Album Title", tag.getFirst(FieldKey.ALBUM))
        assertEquals("Persisted Album Artist", tag.getFirst(FieldKey.ALBUM_ARTIST))
        assertEquals("2026", tag.getFirst(FieldKey.YEAR))
        assertEquals("Persistence Genre", tag.getFirst(FieldKey.GENRE))
    }

    @Test
    fun mediaStoreM4aTagEditsPersistInEmbeddedMetadata() {
        val song = insertFixtureSong("write-fixture.m4a", id = 203L)

        val result = runBlocking { AlbumTagEditorService(context).applyEdits(editRequest(song)) }

        assertTrue(result.failures.joinToString { it.reason }, result.failures.isEmpty())
        assertEquals(listOf(song.id), result.editedSongIds)
        val tag = persistedTag(song)
        assertEquals("Persisted Track Title", tag.getFirst(FieldKey.TITLE))
        assertEquals("Persisted Track Artist", tag.getFirst(FieldKey.ARTIST))
        assertEquals("Persisted Album Title", tag.getFirst(FieldKey.ALBUM))
        assertEquals("Persisted Album Artist", tag.getFirst(FieldKey.ALBUM_ARTIST))
        assertEquals("2026", tag.getFirst(FieldKey.YEAR))
        assertEquals("Persistence Genre", tag.getFirst(FieldKey.GENRE))
    }

    @Test
    fun verifiedWritableFormatsPersistArtwork() {
        listOf("write-fixture.mp3", "write-fixture.flac", "write-fixture.m4a")
            .forEachIndexed { index, fixture ->
                val song = insertFixtureSong(fixture, id = 300L + index)
                val request = editRequest(song).copy(coverArtBytes = TEST_PNG)

                val result = runBlocking { AlbumTagEditorService(context).applyEdits(request) }

                assertTrue("$fixture: ${result.failures.joinToString { it.reason }}", result.failures.isEmpty())
                assertTrue(persistedTag(song).firstArtwork?.binaryData?.isNotEmpty() == true)
            }
    }

    @Test
    fun writableFixturesAreAudioOnlyAndRuntimeDecodable() {
        listOf("write-fixture.mp3", "write-fixture.flac", "write-fixture.m4a")
            .forEachIndexed { index, fixture ->
                val song = insertFixtureSong(fixture, id = 400L + index)

                val detected = AudioFormatDetector(context).detect(song.uri, song.fileName, resolver.getType(song.uri))

                assertTrue(detected.detectionSucceeded)
                assertTrue(detected.hasAudioTrack)
                assertTrue(!detected.hasVideoTrack)
                assertEquals(
                    "$fixture: $detected",
                    PlaybackSupport.Supported,
                    AudioFormatPolicy.playbackSupport(detected),
                )
            }
    }

    @Test
    fun embeddedMetadataReadsFromContentUriWithoutFilePath() {
        val song = insertFixtureSong("write-fixture.flac", id = 500L)

        val metadata = EmbeddedTagMetadataReader(context).read(song.uri, filePath = null, fileName = song.fileName)

        assertEquals("Original FLAC Title", metadata?.title)
        assertEquals("Original FLAC Artist", metadata?.artist)
    }

    @Test
    fun surroundMp4TagArtworkAndLyricsEditsPreserveEncodedAudioAndChannelConfiguration() {
        listOf("aac-6ch.m4a", "aac-7ch.m4a", "aac-8ch.m4a", "ac3-6ch.m4a", "eac3-6ch.m4a")
            .forEachIndexed { index, fixture ->
                val song = insertFixtureSong(fixture, id = 600L + index)
                val channels = fixture.substringAfterLast('-').substringBefore("ch").toInt()
                val format = AudioFormatDetector(context).detect(song.uri, song.fileName, "audio/mp4")
                assertTrue("$fixture: $format", format.detectionSucceeded)
                assertEquals(channels, format.channelCount)
                val originalAudio = encodedAudioDigest(song.uri)
                val metadata = EmbeddedTagMetadataReader(context).read(song.uri, null, song.fileName)
                assertEquals(fixture, "Multichannel fixture", metadata?.title)
                assertEquals(fixture, "Fixture Artist", metadata?.artist)

                val result = runBlocking {
                    AlbumTagEditorService(context).applyEdits(editRequest(song).copy(coverArtBytes = TEST_PNG))
                }

                assertTrue("$fixture: ${result.failures}", result.failures.isEmpty())
                val tag = persistedTag(song)
                assertEquals("Persisted Track Title", tag.getFirst(FieldKey.TITLE))
                assertEquals("Persisted Track Artist", tag.getFirst(FieldKey.ARTIST))
                assertEquals("Persisted Album Artist", tag.getFirst(FieldKey.ALBUM_ARTIST))
                assertTrue(tag.firstArtwork?.binaryData?.isNotEmpty() == true)
                assertArrayEquals("$fixture tag edit changed audio", originalAudio, encodedAudioDigest(song.uri))

                val lyrics = "[00:00.10]Surround lyrics\n[00:01.00]Second line"
                val lyricsResult = runBlocking { EmbeddedLyricsWriter(context).write(song, lyrics) }
                assertTrue("$fixture: $lyricsResult", lyricsResult is EmbeddedLyricsWriteResult.Success)
                val persistedLyrics = runBlocking { LocalLyricsResolver(context).resolve(song) }?.payload
                assertTrue(persistedLyrics?.isSynced == true)
                assertEquals(listOf(100L, 1_000L), persistedLyrics?.lines?.map { it.startTimeMs })
                assertArrayEquals("$fixture lyrics edit changed audio", originalAudio, encodedAudioDigest(song.uri))
            }
    }

    private fun encodedAudioDigest(uri: Uri): ByteArray {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, emptyMap())
            assertEquals(1, extractor.trackCount)
            val format = extractor.getTrackFormat(0)
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(checkNotNull(format.getString(MediaFormat.KEY_MIME)).toByteArray())
            listOf(
                MediaFormat.KEY_CHANNEL_COUNT, MediaFormat.KEY_SAMPLE_RATE,
                MediaFormat.KEY_PROFILE, MediaFormat.KEY_AAC_PROFILE, MediaFormat.KEY_CHANNEL_MASK,
            ).forEach { key ->
                if (format.containsKey(key)) digest.update("$key=${format.getInteger(key)}\n".toByteArray())
            }
            repeat(3) { index -> format.getByteBuffer("csd-$index")?.duplicate()?.let(digest::update) }
            extractor.selectTrack(0)
            val buffer = ByteBuffer.allocate(64 * 1024)
            val timing = ByteBuffer.allocate(12)
            while (true) {
                buffer.clear()
                val size = extractor.readSampleData(buffer, 0)
                if (size < 0) break
                buffer.position(0).limit(size)
                digest.update(buffer)
                timing.clear()
                timing.putLong(extractor.sampleTime).putInt(extractor.sampleFlags)
                digest.update(timing.array())
                extractor.advance()
            }
            return digest.digest()
        } finally {
            extractor.release()
        }
    }

    private fun insertFixtureSong(
        assetName: String,
        id: Long,
    ): Song {
        val mimeType = when {
            assetName.endsWith(".flac") -> "audio/flac"
            assetName.endsWith(".m4a") -> "audio/mp4"
            else -> "audio/mpeg"
        }
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "elovaire-$id-$assetName")
            put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/ElovaireWriteTest")
            if (Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Unable to create MediaStore fixture.")
        insertedUris += uri
        resolver.openOutputStream(uri)?.use { output ->
            testContext.assets.open("media-metadata/$assetName").use { input -> input.copyTo(output) }
        } ?: error("Unable to write MediaStore fixture.")
        if (Build.VERSION.SDK_INT >= 29) {
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) },
                null,
                null,
            )
        }
        return Song(
            id = id,
            title = "Original Title",
            isExplicit = false,
            artist = "Original Artist",
            album = "Original Album",
            releaseYear = 2024,
            genre = "Original Genre",
            audioFormat = assetName.substringAfterLast('.').uppercase(),
            audioQuality = null,
            fileName = assetName,
            albumId = 1L,
            durationMs = 500L,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = 0L,
            uri = uri,
            artUri = null,
            albumArtist = "Original Album Artist",
        )
    }

    private fun editRequest(song: Song): AlbumTagEditRequest {
        val album = Album(
            id = song.albumId,
            title = song.album,
            artist = song.albumArtist ?: song.artist,
            artUri = null,
            songCount = 1,
            durationMs = song.durationMs,
            songs = listOf(song),
        )
        return AlbumTagEditRequest(
            album = album,
            albumTitle = TagFieldEdit.Value("Persisted Album Title"),
            albumArtist = TagFieldEdit.Value("Persisted Album Artist"),
            releaseYear = TagFieldEdit.Value(2026),
            genre = TagFieldEdit.Value("Persistence Genre"),
            coverArtUri = null,
            tracks = listOf(
                EditableAlbumTrack(
                    songId = song.id,
                    title = "Persisted Track Title",
                    artist = "Persisted Track Artist",
                    trackNumber = 1,
                    discNumber = 1,
                    durationMs = song.durationMs,
                ),
            ),
        )
    }

    private fun persistedTag(song: Song) = checkNotNull(AudioTagIO.read(copyPersistedToTemp(song)))

    private fun copyPersistedToTemp(song: Song): File {
        val extension = song.fileName.substringAfterLast('.', "tmp")
        val temp = File.createTempFile("persisted-${song.id}-", ".$extension", context.cacheDir)
        resolver.openInputStream(song.uri)?.use { input ->
            temp.outputStream().use(input::copyTo)
        } ?: error("Unable to read persisted fixture.")
        return temp
    }

    private companion object {
        val TEST_PNG: ByteArray = Base64.decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=",
            Base64.DEFAULT,
        )
    }
}
