package elovaire.music.droidbeauty.app.data.library.network

import elovaire.music.droidbeauty.app.data.audio.AudioTagIO
import java.nio.ByteBuffer
import kotlinx.coroutines.test.runTest
import org.jaudiotagger.tag.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NetworkMp4MetadataWindowTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test
    fun isolatedMoovAtomMatchesTheLocalParser() = runTest {
        val source = checkNotNull(javaClass.getResourceAsStream("/media/lyrics.m4a")).use { it.readBytes() }
        val moovOffset = 101
        val padding = 2 * 1024 * 1024
        val full = ByteArray(source.size + padding)
        source.copyInto(full, 0, 0, moovOffset)
        source.copyInto(full, moovOffset + padding, moovOffset)
        ByteBuffer.wrap(full, 36, 4).putInt(65 + padding)
        val range = checkNotNull(mp4MetadataRange(full.size.toLong(), 1024 * 1024L) { position, length ->
            full.copyOfRange(position.toInt(), position.toInt() + length)
        })
        assertEquals((moovOffset + padding).toLong(), range.first)
        val original = temporary.newFile("full.m4a").apply { writeBytes(full) }
        val window = temporary.newFile("window.m4a").apply {
            writeBytes(full.copyOfRange(range.first.toInt(), (range.first + range.second).toInt()))
        }
        val fullTag = checkNotNull(AudioTagIO.read(original))
        val windowTag = checkNotNull(AudioTagIO.read(window))
        FieldKey.values().forEach { field ->
            assertEquals("$field", runCatching { fullTag.getFirst(field) }.getOrNull(),
                runCatching { windowTag.getFirst(field) }.getOrNull())
        }
    }

    @Test
    fun malformedOrOversizedAtomsAreNotCopied() = runTest {
        val invalid = byteArrayOf(0, 0, 0, 7, 109, 111, 111, 118)
        assertNull(mp4MetadataRange(8, 100) { _, _ -> invalid })
        val moov = byteArrayOf(0, 0, 1, 0, 109, 111, 111, 118)
        assertNull(mp4MetadataRange(256, 128) { _, _ -> moov })
    }

    @Test
    fun skipsA64BitMediaAtomBeforeTheMetadata() = runTest {
        val mediaLength = 5L * 1024L * 1024L * 1024L
        val media = byteArrayOf(0, 0, 0, 1, 109, 100, 97, 116)
        val extended = ByteBuffer.allocate(8).putLong(mediaLength).array()
        val moov = byteArrayOf(0, 0, 0, 16, 109, 111, 111, 118)
        val range = mp4MetadataRange(mediaLength + 16L, 1024) { position, _ ->
            when (position) {
                0L -> media
                8L -> extended
                mediaLength -> moov
                else -> error("Unexpected range $position")
            }
        }
        assertEquals(mediaLength to 16L, range)
    }
}
