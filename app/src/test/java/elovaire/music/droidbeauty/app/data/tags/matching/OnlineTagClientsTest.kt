package elovaire.music.droidbeauty.app.data.tags.matching

import java.io.ByteArrayInputStream
import java.io.IOException
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class OnlineTagClientsTest {
    @Test
    fun readBytesBounded_acceptsResponseWithinLimit() {
        val bytes = byteArrayOf(1, 2, 3)

        assertArrayEquals(
            bytes,
            readBytesBounded(ByteArrayInputStream(bytes), maxBytes = 3, expectedBytes = bytes.size.toLong()),
        )
    }

    @Test(expected = IOException::class)
    fun readBytesBounded_rejectsOversizedContentLength() {
        readBytesBounded(ByteArrayInputStream(byteArrayOf()), maxBytes = 3, expectedBytes = 4)
    }

    @Test(expected = IOException::class)
    fun readBytesBounded_rejectsStreamThatExceedsLimit() {
        readBytesBounded(ByteArrayInputStream(byteArrayOf(1, 2, 3, 4)), maxBytes = 3)
    }
}
