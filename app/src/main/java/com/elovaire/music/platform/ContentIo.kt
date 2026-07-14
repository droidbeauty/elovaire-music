package elovaire.music.droidbeauty.app.platform

import android.content.ContentResolver
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.annotation.WorkerThread
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

internal class ContentIo(
    private val resolver: ContentResolver,
) {
    @WorkerThread
    fun copyToFile(uri: Uri, destination: File): Long {
        var complete = false
        try {
            val copied = resolver.openInputStream(uri)?.use { input ->
                destination.outputStream().buffered().use { output -> input.copyTo(output) }
            } ?: error("Unable to open the source file.")
            complete = true
            return copied
        } finally {
            if (!complete) destination.delete()
        }
    }

    @WorkerThread
    fun replaceFromFile(uri: Uri, source: File) {
        check(source.isFile) { "The replacement file is unavailable." }
        openWritableDescriptor(uri).use { descriptor ->
            FileOutputStream(descriptor.fileDescriptor).channel.use { output ->
                output.position(0L)
                output.truncate(0L)
                FileInputStream(source).channel.use { input ->
                    val expected = input.size()
                    var copied = 0L
                    while (copied < expected) {
                        val count = input.transferTo(copied, expected - copied, output)
                        check(count > 0L) { "The provider stopped before the file was fully replaced." }
                        copied += count
                    }
                    check(copied == expected) { "The provider accepted an incomplete file." }
                }
                output.force(true)
            }
        }
        val persistedSize = resolver.openFileDescriptor(uri, "r")?.use(ParcelFileDescriptor::getStatSize)
        check(persistedSize == null || persistedSize < 0L || persistedSize == source.length()) {
            "The provider persisted an incomplete file."
        }
    }

    @WorkerThread
    fun readBytesBounded(uri: Uri, maxBytes: Int): ByteArray {
        require(maxBytes >= 0)
        return resolver.openInputStream(uri)?.use { input -> input.readBytesBounded(maxBytes) }
            ?: error("Unable to open the source file.")
    }

    @WorkerThread
    fun openReadableDescriptor(uri: Uri): ParcelFileDescriptor {
        return resolver.openFileDescriptor(uri, "r") ?: error("Unable to open the source file.")
    }

    @WorkerThread
    fun openWritableDescriptor(uri: Uri): ParcelFileDescriptor {
        var accessFailure: SecurityException? = null
        writeModes.forEach { mode ->
            try {
                openDescriptorOrNull(uri, mode)?.let { return it }
            } catch (failure: SecurityException) {
                accessFailure = failure
            }
        }
        accessFailure?.let { throw it }
        throw ProviderRejectedWriteModeException(uri)
    }

    @WorkerThread
    fun openReadWriteDescriptor(uri: Uri): ParcelFileDescriptor {
        return openDescriptorOrNull(uri, "rw") ?: error("Unable to open the file for writing.")
    }

    private fun openDescriptorOrNull(uri: Uri, mode: String): ParcelFileDescriptor? {
        return try {
            resolver.openFileDescriptor(uri, mode)
        } catch (_: java.io.FileNotFoundException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: UnsupportedOperationException) {
            null
        }
    }

    private companion object {
        val writeModes = arrayOf("rwt", "rw", "wt", "w")
    }
}

internal class ProviderRejectedWriteModeException(uri: Uri) :
    IllegalStateException("The content provider rejected all supported write modes for ${uri.authority.orEmpty()}.")

internal fun InputStream.readBytesBounded(maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream(minOf(maxBytes, DEFAULT_BUFFER_SIZE))
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0
    while (true) {
        val count = read(buffer)
        if (count < 0) return output.toByteArray()
        total += count
        check(total <= maxBytes) { "The provider response is too large." }
        output.write(buffer, 0, count)
    }
}
