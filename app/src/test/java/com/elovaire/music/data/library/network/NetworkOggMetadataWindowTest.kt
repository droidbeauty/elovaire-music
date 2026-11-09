package elovaire.music.droidbeauty.app.data.library.network

import elovaire.music.droidbeauty.app.data.audio.AudioTagIO
import kotlinx.coroutines.test.runTest
import org.jaudiotagger.tag.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NetworkOggMetadataWindowTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test
    fun vorbisWindowMatchesTheLocalTagParser() = runTest {
        val source = checkNotNull(javaClass.getResourceAsStream("/media/metadata-window.ogg")).use { it.readBytes() }
        val full = ByteArray(2 * 1024 * 1024).also {
            source.copyInto(it)
            source.copyInto(it, it.size - source.size)
        }
        val prefix = checkNotNull(oggVorbisMetadataPrefix(full.size.toLong(), 1024 * 1024L) { position, length ->
            full.copyOfRange(position.toInt(), position.toInt() + length)
        }).toInt()
        val original = temporary.newFile("full.ogg").apply { writeBytes(full) }
        val window = temporary.newFile("window.ogg").apply {
            outputStream().use { output ->
                output.write(full, 0, prefix)
                output.write(full, full.size - 64 * 1024, 64 * 1024)
            }
        }
        val fullTag = checkNotNull(AudioTagIO.read(original))
        val windowTag = checkNotNull(AudioTagIO.read(window))
        FieldKey.values().forEach { field ->
            assertEquals("$field", runCatching { fullTag.getFirst(field) }.getOrNull(),
                runCatching { windowTag.getFirst(field) }.getOrNull())
        }
        assertEquals("Window test", windowTag.getFirst(FieldKey.TITLE))
    }

    @Test
    fun malformedOrExcessivePagesAreRejected() = runTest {
        val bad = ByteArray(27)
        assertNull(oggVorbisMetadataPrefix(2_000_000L, 1_000_000L) { _, _ -> bad })
        assertNull(oggVorbisMetadataPrefix(26L, 1_000_000L) { _, _ -> bad })
        val source = checkNotNull(javaClass.getResourceAsStream("/media/metadata-window.ogg")).use { it.readBytes() }
        assertNull(oggVorbisMetadataPrefix(source.size.toLong(), 1L) { position, length ->
            source.copyOfRange(position.toInt(), position.toInt() + length)
        })
    }

    @Test
    fun acceptsACommentPacketContinuedOnAnotherPage() = runTest {
        fun page(flag: Int, body: ByteArray, lace: Int): ByteArray {
            val header = ByteArray(28)
            "OggS".toByteArray().copyInto(header)
            header[5] = flag.toByte()
            header[14] = 7
            header[26] = 1
            header[27] = lace.toByte()
            return header + body
        }
        val identity = page(2, byteArrayOf(1) + "vorbis".toByteArray(), 7)
        val comment = page(0, (byteArrayOf(3) + "vorbis".toByteArray()).copyOf(255), 255)
        val continuation = page(1, ByteArray(10), 10)
        val bytes = (identity + comment + continuation).copyOf(200_000)
        val prefix = oggVorbisMetadataPrefix(bytes.size.toLong(), 200_000L) { position, length ->
            bytes.copyOfRange(position.toInt(), position.toInt() + length)
        }
        assertTrue(prefix != null && prefix >= identity.size + comment.size + continuation.size)
        bytes[identity.size + comment.size + 5] = 0
        assertNull(oggVorbisMetadataPrefix(bytes.size.toLong(), 200_000L) { position, length ->
            bytes.copyOfRange(position.toInt(), position.toInt() + length)
        })
    }
}
