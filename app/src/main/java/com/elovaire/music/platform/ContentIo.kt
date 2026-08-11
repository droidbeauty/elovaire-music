package elovaire.music.droidbeauty.app.platform

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import elovaire.music.droidbeauty.app.BuildConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.channels.FileChannel

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
        if (Build.VERSION.SDK_INT >= 36 && uri.authority == MediaStore.AUTHORITY) {
            replaceFromDescriptor(uri, source)
        } else {
            val output = try {
                resolver.openOutputStream(uri, "rwt")
            } catch (_: FileNotFoundException) {
                null
            } catch (_: UnsupportedOperationException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }
            if (output != null) {
                output.use { destination ->
                    source.inputStream().use { input ->
                        input.copyTo(destination)
                    }
                    destination.flush()
                }
                logDebug(uri, "replace mode=rwt-stream bytes=${source.length()}")
            } else {
                replaceFromDescriptor(uri, source)
            }
        }
        val persistedSize = openDescriptor(uri, "r")?.use(ParcelFileDescriptor::getStatSize)
        logDebug(uri, "persisted-size bytes=$persistedSize expected=${source.length()}")
        check(persistedSize == null || persistedSize < 0L || persistedSize == source.length()) {
            "The provider persisted an incomplete file."
        }
    }

    private fun replaceFromDescriptor(uri: Uri, source: File) {
        openWritableDescriptor(uri).use { descriptor ->
            FileOutputStream(descriptor.fileDescriptor).channel.use { output ->
                FileInputStream(source).channel.use { input ->
                    replaceFileContents(input, output)
                }
                output.force(true)
            }
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
        return openDescriptor(uri, "r") ?: error("Unable to open the source file.")
    }

    @WorkerThread
    fun openWritableDescriptor(uri: Uri): ParcelFileDescriptor {
        var accessFailure: SecurityException? = null
        writeModes.forEach { mode ->
            try {
                openDescriptorOrNull(uri, mode)?.let {
                    logDebug(uri, "open-write mode=$mode")
                    return it
                }
            } catch (failure: SecurityException) {
                accessFailure = failure
            }
        }
        accessFailure?.let { throw it }
        throw ProviderRejectedWriteModeException(uri)
    }

    @WorkerThread
    fun requireSafWriteAccess(uri: Uri) {
        check(hasPersistedWritePermission(uri)) { "The selected document has no persisted write permission." }
        val flags = resolver.query(uri, arrayOf(DocumentsContract.Document.COLUMN_FLAGS), null, null, null)
            ?.use { cursor ->
                if (!cursor.moveToFirst()) null else cursor.getInt(0)
            }
            ?: error("Unable to query document write capability.")
        check(flags and DocumentsContract.Document.FLAG_SUPPORTS_WRITE != 0) {
            "The selected document provider does not support writing this file."
        }
    }

    private fun hasPersistedWritePermission(uri: Uri): Boolean = resolver.persistedUriPermissions.any { permission ->
        permission.isWritePermission && permissionGrantsUri(permission.uri, uri)
    }

    private fun permissionGrantsUri(grantUri: Uri, uri: Uri): Boolean {
        if (grantUri == uri) return true
        return runCatching {
            DocumentsContract.isTreeUri(grantUri) &&
                grantUri.authority == uri.authority &&
                DocumentsContract.buildDocumentUriUsingTree(
                    grantUri,
                    DocumentsContract.getDocumentId(uri),
                ) == uri
        }.getOrDefault(false)
    }

    @WorkerThread
    fun openReadWriteDescriptor(uri: Uri): ParcelFileDescriptor {
        return openDescriptorOrNull(uri, "rw") ?: error("Unable to open the file for writing.")
    }

    private fun openDescriptorOrNull(uri: Uri, mode: String): ParcelFileDescriptor? {
        return try {
            openDescriptor(uri, mode)
        } catch (_: java.io.FileNotFoundException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: UnsupportedOperationException) {
            null
        }
    }

    private fun openDescriptor(uri: Uri, mode: String): ParcelFileDescriptor? {
        return if (Build.VERSION.SDK_INT >= 36 && uri.authority == MediaStore.AUTHORITY) {
            MediaStore.openFileDescriptor(resolver, uri, mode, null)
        } else {
            resolver.openFileDescriptor(uri, mode)
        }
    }

    private fun logDebug(uri: Uri, message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, "$message authority=${uri.authority.orEmpty()}")
    }

    private companion object {
        const val TAG = "ContentIo"
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

internal fun replaceFileContents(
    input: FileChannel,
    output: FileChannel,
) {
    output.position(0L)
    output.truncate(0L)
    val expected = input.size()
    var copied = 0L
    while (copied < expected) {
        val count = input.transferTo(copied, expected - copied, output)
        check(count > 0L) { "The provider stopped before the file was fully replaced." }
        copied += count
    }
    check(copied == expected) { "The provider accepted an incomplete file." }
    output.truncate(expected)
}
