package elovaire.music.droidbeauty.app.platform

import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ContentIoTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun boundedReadAcceptsExactLimit() {
        val bytes = byteArrayOf(1, 2, 3)
        assertArrayEquals(bytes, ByteArrayInputStream(bytes).readBytesBounded(3))
    }

    @Test
    fun boundedReadRejectsOneByteOverLimit() {
        assertThrows(IllegalStateException::class.java) {
            ByteArrayInputStream(byteArrayOf(1, 2, 3, 4)).readBytesBounded(3)
        }
    }

    @Test
    fun boundedReadWithZeroLimitDoesNotConsumeProvider() {
        var reads = 0
        val input = object : ByteArrayInputStream(byteArrayOf(1)) {
            override fun read(buffer: ByteArray): Int {
                reads += 1
                return super.read(buffer)
            }
        }

        assertArrayEquals(ByteArray(0), input.readBytesBounded(0))
        org.junit.Assert.assertEquals(0, reads)
    }

    @Test
    fun boundedReadRecoversFromZeroLengthRead() {
        val input = object : ByteArrayInputStream(byteArrayOf(4, 5)) {
            private var first = true

            override fun read(buffer: ByteArray): Int {
                if (first) {
                    first = false
                    return 0
                }
                return super.read(buffer)
            }
        }

        assertArrayEquals(byteArrayOf(4, 5), input.readBytesBounded(2))
    }

    @Test
    fun replacementTruncatesContentsWhenSourceIsShorter() {
        assertReplacement(
            original = byteArrayOf(1, 2, 3, 4, 5, 6),
            replacement = byteArrayOf(9, 8),
        )
    }

    @Test
    fun replacementCopiesCompleteContentsWhenSourceIsLonger() {
        assertReplacement(
            original = byteArrayOf(1, 2),
            replacement = byteArrayOf(9, 8, 7, 6, 5, 4),
        )
    }

    private fun assertReplacement(
        original: ByteArray,
        replacement: ByteArray,
    ) {
        val source = temporaryFolder.newFile().apply { writeBytes(replacement) }
        val destination = temporaryFolder.newFile().apply { writeBytes(original) }

        FileInputStream(source).channel.use { input ->
            FileOutputStream(destination, true).channel.use { output ->
                replaceFileContents(input, output)
            }
        }

        assertArrayEquals(replacement, destination.readBytes())
    }
}
