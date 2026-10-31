package elovaire.music.droidbeauty.app.platform

import java.io.InputStream

internal fun InputStream.readWithProgress(
    buffer: ByteArray,
    offset: Int = 0,
    length: Int = buffer.size - offset,
): Int {
    val count = read(buffer, offset, length)
    if (count != 0 || length == 0) return count
    val byte = read()
    if (byte < 0) return -1
    buffer[offset] = byte.toByte()
    return 1
}
