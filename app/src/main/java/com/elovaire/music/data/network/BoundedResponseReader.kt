package elovaire.music.droidbeauty.app.data.network

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

internal fun InputStream.readBytesBounded(
    maxBytes: Int,
    expectedBytes: Long = -1L,
): ByteArray {
    if (expectedBytes > maxBytes) {
        throw IOException("Remote response is too large.")
    }
    val output = ByteArrayOutputStream(expectedBytes.takeIf { it in 0..maxBytes }?.toInt() ?: DEFAULT_BUFFER_SIZE)
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0
    while (true) {
        val read = read(buffer)
        if (read < 0) break
        total += read
        if (total > maxBytes) {
            throw IOException("Remote response is too large.")
        }
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}

internal fun InputStream.readUtf8Bounded(
    maxBytes: Int,
    expectedBytes: Long = -1L,
): String = readBytesBounded(maxBytes, expectedBytes).toString(Charsets.UTF_8)
