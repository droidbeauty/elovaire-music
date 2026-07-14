package elovaire.music.droidbeauty.app.platform

import java.io.ByteArrayInputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ContentIoTest {
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
}
