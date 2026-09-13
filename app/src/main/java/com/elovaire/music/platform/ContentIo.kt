package elovaire.music.droidbeauty.app.platform

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import elovaire.music.droidbeauty.app.BuildConfig
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

internal enum class ContentIoFailureKind {
    PermissionRequired,
    SourceUnavailable,
    ProviderUnavailable,
    UnsupportedWriteCapability,
    MalformedProviderContract,
    VerificationFailed,
    LocalIo,
    InvariantViolation,
}

internal open class ContentIoException(
    val kind: ContentIoFailureKind,
    message: String,
    cause: Throwable? = null,
) : IllegalStateException(message, cause)

internal class ContentIo(
    private val resolver: ContentResolver,
) {
    @WorkerThread
    fun copyToFile(uri: Uri, destination: File): Long {
        var complete = false
        try {
            val copied = resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destination, false).use { output ->
                    BufferedOutputStream(output, COPY_BUFFER_SIZE).use { buffered ->
                        val result = input.copyTo(buffered, COPY_BUFFER_SIZE)
                        buffered.flush()
                        // The file is used as recovery material. Make the bytes durable before
                        // reporting the copy as complete; closing a buffered stream alone does
                        // not provide that contract on every filesystem.
                        output.fd.sync()
                        result
                    }
                }
            } ?: throw ContentIoException(
                ContentIoFailureKind.SourceUnavailable,
                "Unable to open the source file.",
            )
            complete = true
            return copied
        } finally {
            if (!complete) destination.delete()
        }
    }

    @WorkerThread
    fun replaceFromFile(uri: Uri, source: File) {
        if (!source.isFile) {
            throw ContentIoException(
                ContentIoFailureKind.LocalIo,
                "The replacement file is unavailable.",
            )
        }
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
        if (persistedSize != null && persistedSize >= 0L && persistedSize != source.length()) {
            throw ContentIoException(
                ContentIoFailureKind.VerificationFailed,
                "The provider persisted an incomplete file.",
            )
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
            ?: throw ContentIoException(
                ContentIoFailureKind.SourceUnavailable,
                "Unable to open the source file.",
            )
    }

    @WorkerThread
    fun openReadableDescriptor(uri: Uri): ParcelFileDescriptor {
        return openDescriptor(uri, "r") ?: throw ContentIoException(
            ContentIoFailureKind.SourceUnavailable,
            "Unable to open the source file.",
        )
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
        if (!hasPersistedWritePermission(uri)) {
            throw ContentIoException(
                ContentIoFailureKind.PermissionRequired,
                "The selected document has no persisted write permission.",
            )
        }
        val flags = resolver.query(uri, arrayOf(DocumentsContract.Document.COLUMN_FLAGS), null, null, null)
            ?.use { cursor ->
                if (!cursor.moveToFirst()) null else cursor.getInt(0)
            }
            ?: throw ContentIoException(
                ContentIoFailureKind.MalformedProviderContract,
                "Unable to query document write capability.",
            )
        if (flags and DocumentsContract.Document.FLAG_SUPPORTS_WRITE == 0) {
            throw ContentIoException(
                ContentIoFailureKind.UnsupportedWriteCapability,
                "The selected document provider does not support writing this file.",
            )
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
        return openDescriptorOrNull(uri, "rw") ?: throw ContentIoException(
            ContentIoFailureKind.UnsupportedWriteCapability,
            "Unable to open the file for writing.",
        )
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
        const val COPY_BUFFER_SIZE = 64 * 1024
        val writeModes = arrayOf("rwt", "rw", "wt", "w")
    }
}

internal class ProviderRejectedWriteModeException(uri: Uri) :
    ContentIoException(
        ContentIoFailureKind.UnsupportedWriteCapability,
        "The content provider rejected all supported write modes for ${uri.authority.orEmpty()}.",
    )

internal fun contentIoFailureKind(failure: Throwable): ContentIoFailureKind {
    var current: Throwable? = failure
    var depth = 0
    val visited = HashSet<Throwable>()
    while (current != null && depth++ < 8 && visited.add(current)) {
        when (current) {
            is ContentIoException -> return current.kind
            is SecurityException -> return ContentIoFailureKind.PermissionRequired
            is FileNotFoundException -> return ContentIoFailureKind.SourceUnavailable
            is RemoteException -> return ContentIoFailureKind.ProviderUnavailable
            is IOException -> return ContentIoFailureKind.LocalIo
            is IllegalArgumentException -> return ContentIoFailureKind.MalformedProviderContract
        }
        current = current.cause
    }
    return ContentIoFailureKind.InvariantViolation
}

internal fun InputStream.readBytesBounded(maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream(minOf(maxBytes, DEFAULT_BUFFER_SIZE))
    if (maxBytes == 0) return output.toByteArray()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0
    while (true) {
        val count = read(buffer)
        if (count == 0) {
            val singleByte = read()
            if (singleByte < 0) return output.toByteArray()
            if (total >= maxBytes) {
                throw ContentIoException(
                    ContentIoFailureKind.MalformedProviderContract,
                    "The provider response is too large.",
                )
            }
            output.write(singleByte)
            total += 1
            continue
        }
        if (count < 0) return output.toByteArray()
        total += count
        if (total > maxBytes) {
            throw ContentIoException(
                ContentIoFailureKind.MalformedProviderContract,
                "The provider response is too large.",
            )
        }
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
    var zeroProgressAttempts = 0
    while (copied < expected) {
        val count = input.transferTo(copied, expected - copied, output)
        if (count == 0L) {
            zeroProgressAttempts += 1
            if (zeroProgressAttempts > 3) {
                throw ContentIoException(
                    ContentIoFailureKind.VerificationFailed,
                    "The provider stopped before the file was fully replaced.",
                )
            }
            continue
        }
        zeroProgressAttempts = 0
        copied += count
    }
    if (copied != expected) {
        throw ContentIoException(
            ContentIoFailureKind.VerificationFailed,
            "The provider accepted an incomplete file.",
        )
    }
    output.truncate(expected)
}
