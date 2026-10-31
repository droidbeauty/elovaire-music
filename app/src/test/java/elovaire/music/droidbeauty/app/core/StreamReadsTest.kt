package elovaire.music.droidbeauty.app.platform

import java.io.ByteArrayInputStream
import java.io.InputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class StreamReadsTest {
    @Test
    fun normalBulkReadsRetainTheirRequestedRange() {
        val input = ByteArrayInputStream(byteArrayOf(1, 2, 3))
        val buffer = ByteArray(4)
        assertEquals(2, input.readWithProgress(buffer, 1, 2))
        assertArrayEquals(byteArrayOf(0, 1, 2, 0), buffer)
        assertEquals(1, input.readWithProgress(buffer, 0, 4))
        assertEquals(-1, input.readWithProgress(buffer))
    }

    @Test
    fun zeroProgressBulkReadFallsBackToSingleByteAndPreservesOffsets() {
        val input = object : InputStream() {
            private var next = 0
            override fun read(buffer: ByteArray, offset: Int, length: Int) = 0
            override fun read(): Int = if (next++ == 0) 0xAB else -1
        }
        val buffer = ByteArray(3)
        assertEquals(1, input.readWithProgress(buffer, 1, 1))
        assertArrayEquals(byteArrayOf(0, 0xAB.toByte(), 0), buffer)
        assertEquals(-1, input.readWithProgress(buffer, 1, 1))
        assertEquals(0, input.readWithProgress(buffer, 0, 0))
    }
}
