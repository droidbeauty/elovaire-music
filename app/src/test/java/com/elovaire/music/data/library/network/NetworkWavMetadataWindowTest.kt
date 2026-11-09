package elovaire.music.droidbeauty.app.data.library.network

import elovaire.music.droidbeauty.app.data.audio.AudioTagIO
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.test.runTest
import org.jaudiotagger.tag.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NetworkWavMetadataWindowTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test
    fun metadataAfterAudioMatchesTheLocalParser() = runTest {
        val source = checkNotNull(javaClass.getResourceAsStream("/media/metadata-window.wav")).use { it.readBytes() }
        val padding = 2 * 1024 * 1024
        val full = ByteArray(source.size + padding)
        source.copyInto(full, 0, 0, 36)
        source.copyInto(full, 36, 110, source.size)
        source.copyInto(full, source.size + padding - 74, 36, 110)
        ByteBuffer.wrap(full, 4, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(full.size - 8)
        ByteBuffer.wrap(full, 40, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(4410 + padding)
        val plan = checkNotNull(wavMetadataWindow(full.size.toLong(), 1024 * 1024L) { position, length ->
            full.copyOfRange(position.toInt(), position.toInt() + length)
        })
        val original = temporary.newFile("full.wav").apply { writeBytes(full) }
        val window = temporary.newFile("window.wav").apply {
            outputStream().use { output ->
                val header = full.copyOfRange(0, 12)
                ByteBuffer.wrap(header, 4, 4).order(ByteOrder.LITTLE_ENDIAN).putInt((plan.size - 8).toInt())
                output.write(header)
                plan.chunks.forEach { chunk ->
                    if (chunk.audio) {
                        output.write(byteArrayOf(100, 97, 116, 97, chunk.audioBytes.toByte(), 0, 0, 0))
                        output.write(full, chunk.offset.toInt() + 8, chunk.audioBytes)
                        if (chunk.audioBytes % 2 != 0) output.write(0)
                    } else output.write(full, chunk.offset.toInt(), chunk.length.toInt())
                }
            }
        }
        val fullTag = checkNotNull(AudioTagIO.read(original))
        val windowTag = checkNotNull(AudioTagIO.read(window))
        assertTrue(fullTag.getFirst(FieldKey.TITLE).startsWith("Window test"))
        FieldKey.values().forEach { field ->
            assertEquals("$field", runCatching { fullTag.getFirst(field) }.getOrNull(),
                runCatching { windowTag.getFirst(field) }.getOrNull())
        }
    }

    @Test
    fun rejectsTruncatedChunksAndInvalidHeaders() = runTest {
        assertNull(wavMetadataWindow(12, 1024) { _, length -> ByteArray(length) })
        val source = checkNotNull(javaClass.getResourceAsStream("/media/metadata-window.wav")).use { it.readBytes() }
        assertNull(wavMetadataWindow(source.size.toLong() - 1, 1024 * 1024) { position, length ->
            source.copyOfRange(position.toInt(), position.toInt() + length)
        })
    }
}
