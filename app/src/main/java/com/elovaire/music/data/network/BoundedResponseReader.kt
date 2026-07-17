package elovaire.music.droidbeauty.app.data.network

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

internal enum class BoundedReadFailure {
    TooLarge,
    Incomplete,
    Stalled,
}

internal class BoundedResponseException(
    val kind: BoundedReadFailure,
    message: String,
) : IOException(message)

internal fun InputStream.readBytesBounded(
    maxBytes: Int,
    expectedBytes: Long = -1L,
): ByteArray {
    require(maxBytes >= 0) { "The response limit must not be negative." }
    if (expectedBytes > maxBytes) {
        throw BoundedResponseException(BoundedReadFailure.TooLarge, "Remote response is too large.")
    }
    val output = ByteArrayOutputStream(expectedBytes.takeIf { it in 0..maxBytes }?.toInt() ?: DEFAULT_BUFFER_SIZE)
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0L
    while (true) {
        val read = read(buffer)
        if (read < 0) break
        if (read == 0) {
            throw BoundedResponseException(BoundedReadFailure.Stalled, "Remote response made no progress.")
        }
        total += read.toLong()
        if (total > maxBytes) {
            throw BoundedResponseException(BoundedReadFailure.TooLarge, "Remote response is too large.")
        }
        output.write(buffer, 0, read)
    }
    if (expectedBytes >= 0L && total != expectedBytes) {
        throw BoundedResponseException(BoundedReadFailure.Incomplete, "Remote response ended before its declared size.")
    }
    return output.toByteArray()
}

internal fun InputStream.readUtf8Bounded(
    maxBytes: Int,
    expectedBytes: Long = -1L,
): String = readBytesBounded(maxBytes, expectedBytes).toString(Charsets.UTF_8)
