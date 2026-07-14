package elovaire.music.droidbeauty.app.data.mutation

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.platform.MediaWriteTarget
import elovaire.music.droidbeauty.app.platform.MediaWriteTargetClassifier
import elovaire.music.droidbeauty.app.platform.ContentIo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

internal class MediaFileMutationRunner(
    context: Context,
    private val tempDirectoryName: String,
) {
    private val appContext = context.applicationContext
    private val contentResolver: ContentResolver = appContext.contentResolver
    private val contentIo = ContentIo(contentResolver)

    fun requireWritable(uri: Uri) {
        when (val target = MediaWriteTargetClassifier.classify(appContext, uri)) {
            is MediaWriteTarget.MediaStoreItem -> Unit
            is MediaWriteTarget.SafDocument -> contentIo.openReadWriteDescriptor(target.uri).use { }
            is MediaWriteTarget.FileUri -> {
                val path = target.uri.path ?: error("The file path is unavailable.")
                check(File(path).canWrite()) { "The song file is not writable." }
            }
            is MediaWriteTarget.Unsupported -> error(target.reason)
        }
    }

    fun copySongToTemp(
        song: Song,
        purpose: String,
    ): File {
        val destination = createTempFile(song, purpose)
        var complete = false
        try {
            contentIo.copyToFile(song.uri, destination)
            check(destination.length() > 0L) { "The song file is empty." }
            complete = true
            return destination
        } finally {
            if (!complete) destination.delete()
        }
    }

    fun createTempFile(
        song: Song,
        purpose: String,
    ): File {
        val directory = File(appContext.cacheDir, tempDirectoryName)
        check(directory.isDirectory || directory.mkdirs()) { "Unable to create the metadata edit directory." }
        val extension = song.fileName.substringAfterLast('.', "").ifBlank { "tmp" }
        return File.createTempFile("${song.id}-$purpose-", ".$extension", directory)
    }

    fun overwriteOriginal(
        uri: Uri,
        source: File,
    ) {
        check(source.isFile && source.length() > 0L) { "The replacement song file is empty." }
        val target = MediaWriteTargetClassifier.classify(appContext, uri)
        if (target is MediaWriteTarget.Unsupported) error(target.reason)
        if (target is MediaWriteTarget.FileUri) {
            val destination = target.uri.path?.let(::File) ?: error("The file path is unavailable.")
            FileInputStream(source).use { input ->
                FileOutputStream(destination, false).use { output ->
                    input.copyTo(output)
                    output.fd.sync()
                }
            }
            check(destination.length() == source.length()) { "Unable to replace the complete song file." }
            return
        }
        contentIo.replaceFromFile(uri, source)
    }

    fun verifyOriginalBytes(
        uri: Uri,
        expected: File,
    ) {
        val actualDigest = contentResolver.openInputStream(uri)?.use(::sha256)
            ?: error("Unable to verify the restored song.")
        val expectedDigest = expected.inputStream().use(::sha256)
        check(actualDigest.contentEquals(expectedDigest)) { "The restored song does not match the backup." }
    }

    private fun sha256(input: InputStream): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(COMPARE_BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (count > 0) digest.update(buffer, 0, count)
        }
        return digest.digest()
    }

    private companion object {
        const val COMPARE_BUFFER_SIZE = 64 * 1024
    }
}
