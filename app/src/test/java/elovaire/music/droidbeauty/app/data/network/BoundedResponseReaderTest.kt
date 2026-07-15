package elovaire.music.droidbeauty.app.data.network

import java.io.ByteArrayInputStream
import java.io.IOException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BoundedResponseReaderTest {
    @Test
    fun readsCompleteResponseWithinLimit() {
        val bytes = byteArrayOf(1, 2, 3, 4)

        assertArrayEquals(bytes, ByteArrayInputStream(bytes).readBytesBounded(maxBytes = 4, expectedBytes = 4))
    }

    @Test
    fun rejectsDeclaredAndStreamingOverflow() {
        assertThrows(IOException::class.java) {
            ByteArrayInputStream(byteArrayOf()).readBytesBounded(maxBytes = 4, expectedBytes = 5)
        }
        assertThrows(IOException::class.java) {
            ByteArrayInputStream(ByteArray(5)).readBytesBounded(maxBytes = 4)
        }
    }

    @Test
    fun rejectsInvalidNegativeLimit() {
        assertThrows(IllegalArgumentException::class.java) {
            ByteArrayInputStream(byteArrayOf()).readBytesBounded(maxBytes = -1)
        }
    }
}
