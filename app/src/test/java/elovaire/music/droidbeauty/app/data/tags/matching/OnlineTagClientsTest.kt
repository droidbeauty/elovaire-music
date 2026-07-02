package elovaire.music.droidbeauty.app.data.tags.matching

import elovaire.music.droidbeauty.app.data.network.readBytesBounded
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
            ByteArrayInputStream(bytes).readBytesBounded(maxBytes = 3, expectedBytes = bytes.size.toLong()),
        )
    }

    @Test(expected = IOException::class)
    fun readBytesBounded_rejectsOversizedContentLength() {
        ByteArrayInputStream(byteArrayOf()).readBytesBounded(maxBytes = 3, expectedBytes = 4)
    }

    @Test(expected = IOException::class)
    fun readBytesBounded_rejectsStreamThatExceedsLimit() {
        ByteArrayInputStream(byteArrayOf(1, 2, 3, 4)).readBytesBounded(maxBytes = 3)
    }
}
