package elovaire.music.droidbeauty.app.data.mutation

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.platform.MediaWriteTarget
import elovaire.music.droidbeauty.app.platform.MediaWriteTargetClassifier
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

internal class MediaFileMutationRunner(
    context: Context,
    private val tempDirectoryName: String,
) {
    private val appContext = context.applicationContext
    private val contentResolver: ContentResolver = appContext.contentResolver

    fun requireWritable(uri: Uri) {
        when (val target = MediaWriteTargetClassifier.classify(appContext, uri)) {
            is MediaWriteTarget.MediaStoreItem -> Unit
            is MediaWriteTarget.SafDocument -> contentResolver.openFileDescriptor(target.uri, "rw")
                ?.use { }
                ?: error("The selected document is not writable.")
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
        contentResolver.openInputStream(song.uri)?.use { input ->
            destination.outputStream().use(input::copyTo)
        } ?: error("Unable to open ${song.fileName}")
        return destination
    }

    fun createTempFile(
        song: Song,
        purpose: String,
    ): File {
        val directory = File(appContext.cacheDir, tempDirectoryName).apply { mkdirs() }
        val extension = song.fileName.substringAfterLast('.', "").ifBlank { "tmp" }
        return File(directory, "${song.id}-$purpose-${System.nanoTime()}.$extension")
    }

    fun overwriteOriginal(
        uri: Uri,
        source: File,
    ) {
        requireWritable(uri)
        val target = MediaWriteTargetClassifier.classify(appContext, uri)
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
        val descriptor = try {
            contentResolver.openFileDescriptor(uri, "rwt")
        } catch (_: java.io.FileNotFoundException) {
            contentResolver.openFileDescriptor(uri, "rw")
        } catch (_: IllegalArgumentException) {
            contentResolver.openFileDescriptor(uri, "rw")
        } ?: error("Unable to open the song for writing.")

        descriptor.use {
            FileOutputStream(it.fileDescriptor).channel.use { output ->
                output.position(0L)
                output.truncate(0L)
                FileInputStream(source).channel.use { input ->
                    var transferred = 0L
                    val totalBytes = input.size()
                    while (transferred < totalBytes) {
                        val copied = input.transferTo(transferred, totalBytes - transferred, output)
                        check(copied > 0L) { "Unable to replace the song metadata." }
                        transferred += copied
                    }
                }
                output.force(true)
            }
        }
        val persistedSize = contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize }
        check(persistedSize == null || persistedSize < 0L || persistedSize == source.length()) {
            "Unable to replace the complete song file."
        }
    }
}
