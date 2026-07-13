package elovaire.music.droidbeauty.app.data.lyrics

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

class EmbeddedLyricsMetadataTest {
    @Test
    fun plainTextUsesUnsyncedLyricsTarget() {
        assertEquals(
            EmbeddedLyricsTagKind.UnsyncedLyrics,
            classifyLyricsTagKind("First line\nSecond line"),
        )
    }

    @Test
    fun timestampedTextUsesSyncedLyricsTarget() {
        listOf("[00:12.34]Line", "[01:02]Line", "[1:02.345]Line").forEach { lyrics ->
            assertEquals(EmbeddedLyricsTagKind.SyncedLyrics, classifyLyricsTagKind(lyrics))
        }
    }

    @Test
    fun unrelatedBracketsDoNotUseSyncedLyricsTarget() {
        assertEquals(
            EmbeddedLyricsTagKind.UnsyncedLyrics,
            classifyLyricsTagKind("[Verse 1]\nFirst line"),
        )
    }

    @Test
    fun mp3AndFlacWritesCommitReadableLyrics() {
        listOf("lyrics-id3v23.mp3", "lyrics-id3v24.mp3", "lyrics-no-id3.mp3", "lyrics.flac").forEach { fixture ->
            val file = copyFixture(fixture)
            val tagVersion = file.inputStream().use { input -> input.readNBytes(4).last().toInt() }
            val plain = "Plain lyrics\nSecond line"
            EmbeddedLyricsMetadata.write(file, request(file, plain))
            assertEquals(listOf(plain), AudioFileLyricsInspection.inspect(file).unsynced)

            val synced = "[00:01.00]First line\n[00:05.50]Second line"
            EmbeddedLyricsMetadata.write(file, request(file, synced))
            val fields = AudioFileLyricsInspection.inspect(file)
            val lines = fields.synced.single()
            assertEquals(listOf(1_000L, 5_500L), lines.mapNotNull(LyricsLine::startTimeMs))
            assertEquals(listOf("First line", "Second line"), lines.map(LyricsLine::text))
            assertTrue(fields.unsynced.isEmpty())
            assertTrue(file.length() > 0L)
            assertEquals(
                if (fixture == "lyrics-no-id3.mp3") "" else "Fixture",
                AudioFileIO.read(file).tag.getFirst(FieldKey.TITLE),
            )
            if (fixture.startsWith("lyrics-id3")) {
                assertEquals(tagVersion, file.inputStream().use { input -> input.readNBytes(4).last().toInt() })
            }

            EmbeddedLyricsMetadata.write(file, request(file, ""))
            assertEquals(EmbeddedLyricsFields(), AudioFileLyricsInspection.inspect(file))
        }
    }

    @Test
    fun mp4WritesOnlyUnsyncedLyrics() {
        val file = copyFixture("lyrics.m4a")
        val plain = "Plain lyrics\nUnicode żółć"
        EmbeddedLyricsMetadata.write(file, request(file, plain))
        assertEquals(listOf(plain), AudioFileLyricsInspection.inspect(file).unsynced)
        assertThrows(IllegalStateException::class.java) {
            val synced = "[00:01.00]First line"
            EmbeddedLyricsMetadata.write(file, request(file, synced))
        }
    }

    private fun request(file: File, lyrics: String): EmbeddedLyricsWriteRequest {
        return EmbeddedLyricsWriteRequest(
            song = Song(
                id = 1L,
                title = "Fixture",
                isExplicit = false,
                artist = "",
                album = "",
                releaseYear = null,
                genre = "",
                audioFormat = "",
                audioQuality = null,
                fileName = file.name,
                albumId = 1L,
                durationMs = 0L,
                trackNumber = 1,
                discNumber = 1,
                dateAddedSeconds = 0L,
                uri = TestUri(),
                artUri = null,
            ),
            rawLyrics = lyrics,
            canonicalLyrics = lyrics,
            tagKind = classifyLyricsTagKind(lyrics),
        )
    }

    private fun copyFixture(name: String): File {
        val output = kotlin.io.path.createTempFile(suffix = ".${name.substringAfterLast('.')}").toFile()
        javaClass.getResourceAsStream("/media/$name")!!.use { input ->
            output.outputStream().use(input::copyTo)
        }
        output.deleteOnExit()
        return output
    }
}
