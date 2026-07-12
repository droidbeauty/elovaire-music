package elovaire.music.droidbeauty.app.metadata

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.data.lyrics.EmbeddedLyricsWriteResult
import elovaire.music.droidbeauty.app.data.lyrics.EmbeddedLyricsWriter
import elovaire.music.droidbeauty.app.data.lyrics.LocalLyricsResolver
import elovaire.music.droidbeauty.app.data.lyrics.toEmbeddedLyricsText
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
        val lyrics = "[00:01.00]Elovaire metadata write test\n[00:02.00]Lyrics persisted line"

        val result = runBlocking { EmbeddedLyricsWriter(context).write(song, lyrics) }

        assertTrue(result is EmbeddedLyricsWriteResult.Success)
        val persisted = LocalLyricsResolver(context)
            .resolve(song)
            ?.payload
            ?.toEmbeddedLyricsText()
            .orEmpty()
        assertEquals(lyrics, persisted)
    }

    @Test
    fun mediaStoreFlacLyricsPersistInEmbeddedMetadata() {
        val song = insertFixtureSong("write-fixture.flac", id = 102L)
        val lyrics = "[00:01.00]Elovaire metadata write test\n[00:02.00]Lyrics persisted line"

        val result = runBlocking { EmbeddedLyricsWriter(context).write(song, lyrics) }

        assertTrue(result is EmbeddedLyricsWriteResult.Success)
        val persisted = LocalLyricsResolver(context)
            .resolve(song)
            ?.payload
            ?.toEmbeddedLyricsText()
            .orEmpty()
        assertEquals(lyrics, persisted)
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

    private fun insertFixtureSong(
        assetName: String,
        id: Long,
    ): Song {
        val mimeType = if (assetName.endsWith(".flac")) "audio/flac" else "audio/mpeg"
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
}
