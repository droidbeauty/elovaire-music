package elovaire.music.droidbeauty.app.metadata

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.data.lyrics.EmbeddedLyricsWriteResult
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
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
import java.io.File
import kotlinx.coroutines.runBlocking
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.junit.After
import org.junit.Assert.assertEquals
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
        insertedUris.forEach { uri -> runCatching { resolver.delete(uri, null, null) } }
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
        val persisted = LocalLyricsResolver(context).resolve(song)?.payload
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
        val persisted = LocalLyricsResolver(context).resolve(song)?.payload
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
        val persisted = LocalLyricsResolver(context).resolve(song)?.payload
        assertTrue(persisted?.isSynced == false)
        assertEquals(listOf("Plain embedded lyrics", "Second line"), persisted?.lines?.map { it.text })
        assertEquals(listOf(lyrics), AudioFileLyricsInspection.inspect(copyPersistedToTemp(song)).unsynced)
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

    private fun persistedTag(song: Song) = AudioFileIO.read(copyPersistedToTemp(song)).tagOrCreateAndSetDefault

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
