package elovaire.music.droidbeauty.app.data.library.network

import elovaire.music.droidbeauty.app.data.audio.AudioTagIO
import java.io.File
import org.jaudiotagger.tag.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NetworkMp3MetadataWindowTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test
    fun windowPreservesTheLocalParserFieldsForId3Versions() {
        listOf("lyrics-id3v23.mp3", "lyrics-id3v24.mp3").forEachIndexed { index, name ->
            val original = checkNotNull(javaClass.getResourceAsStream("/media/$name")).use { it.readBytes() }
            val full = ByteArray(2 * 1024 * 1024).also { original.copyInto(it) }
            val prefix = checkNotNull(mp3MetadataPrefix(full.copyOf(10), full.size.toLong(), 1024 * 1024L)).toInt()
            val fullFile = temporary.newFile("full-$index.mp3").apply { writeBytes(full) }
            val windowFile = temporary.newFile("window-$index.mp3").apply {
                outputStream().use { output ->
                    output.write(full, 0, prefix)
                    output.write(full, full.size - 128, 128)
                }
            }
            val fullTag = checkNotNull(AudioTagIO.read(fullFile))
            val windowTag = checkNotNull(AudioTagIO.read(windowFile))
            FieldKey.values().forEach { field -> assertEquals("$name: $field", fullTag.getFirst(field), windowTag.getFirst(field)) }
        }
    }

    @Test
    fun malformedOrOversizedHeadersDoNotProduceAWindow() {
        val header = byteArrayOf(73, 68, 51, 4, 0, 0, 0, 0, 0, 0)
        assertNull(mp3MetadataPrefix(header, 2_000_000L, 100L))
        assertNull(mp3MetadataPrefix(header.copyOf().apply { this[6] = 0x80.toByte() }, 2_000_000L, 1_000_000L))
        assertNull(mp3MetadataPrefix(header.copyOf().apply { this[3] = 5 }, 2_000_000L, 1_000_000L))
        assertNull(mp3MetadataPrefix(header, 9L, 1_000_000L))
    }

    @Test
    fun windowPreservesId3v1AtTheEndOfAFile() {
        val audio = checkNotNull(javaClass.getResourceAsStream("/media/lyrics-no-id3.mp3")).use { it.readBytes() }
        val full = ByteArray(2 * 1024 * 1024).also { audio.copyInto(it) }
        val tag = ByteArray(128)
        "TAGBook title".toByteArray().copyInto(tag)
        tag.copyInto(full, full.size - tag.size)
        val prefix = checkNotNull(mp3MetadataPrefix(full.copyOf(10), full.size.toLong(), 1024 * 1024L)).toInt()
        val fullFile = temporary.newFile("id3v1-full.mp3").apply { writeBytes(full) }
        val windowFile = temporary.newFile("id3v1-window.mp3").apply {
            outputStream().use { output ->
                output.write(full, 0, prefix)
                output.write(full, full.size - 128, 128)
            }
        }
        val fullTag = checkNotNull(AudioTagIO.read(fullFile))
        val windowTag = checkNotNull(AudioTagIO.read(windowFile))
        assertEquals("Book title", fullTag.getFirst(FieldKey.TITLE))
        assertEquals(fullTag.getFirst(FieldKey.TITLE), windowTag.getFirst(FieldKey.TITLE))
    }
}
