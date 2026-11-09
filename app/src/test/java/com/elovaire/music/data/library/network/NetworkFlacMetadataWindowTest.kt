package elovaire.music.droidbeauty.app.data.library.network

import elovaire.music.droidbeauty.app.data.audio.AudioTagIO
import kotlinx.coroutines.test.runTest
import org.jaudiotagger.tag.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NetworkFlacMetadataWindowTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test
    fun metadataWindowMatchesTheLocalParser() = runTest {
        val source = checkNotNull(javaClass.getResourceAsStream("/media/lyrics.flac")).use { it.readBytes() }
        val full = ByteArray(2 * 1024 * 1024).also { source.copyInto(it) }
        val prefix = checkNotNull(flacMetadataPrefix(full.size.toLong(), 1024 * 1024L) { position ->
            full.copyOfRange(position.toInt(), position.toInt() + 4)
        }).toInt()
        val original = temporary.newFile("full.flac").apply { writeBytes(full) }
        val window = temporary.newFile("window.flac").apply { writeBytes(full.copyOf(prefix)) }
        val fullTag = checkNotNull(AudioTagIO.read(original))
        val windowTag = checkNotNull(AudioTagIO.read(window))
        FieldKey.values().forEach { field ->
            assertEquals("$field", runCatching { fullTag.getFirst(field) }.getOrNull(),
                runCatching { windowTag.getFirst(field) }.getOrNull())
        }
    }

    @Test
    fun rejectsTruncatedOversizedAndUnterminatedBlocks() = runTest {
        val signature = byteArrayOf(102, 76, 97, 67)
        assertNull(flacMetadataPrefix(100_000L, 100_000L) { position ->
            if (position == 0L) signature else byteArrayOf(0, 0, 0, 0)
        })
        assertNull(flacMetadataPrefix(100_000L, 100_000L) { position ->
            if (position == 0L) signature else byteArrayOf(0x80.toByte(), 0x7f, 0, 0)
        })
        assertNull(flacMetadataPrefix(7L, 100_000L) { signature })
    }
}
