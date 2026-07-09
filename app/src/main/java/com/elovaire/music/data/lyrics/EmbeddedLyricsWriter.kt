package elovaire.music.droidbeauty.app.data.lyrics

import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.TagWriteSupport
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationOperation
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationStatus
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationType
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.platform.isContentUri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.CancellationException
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

internal sealed interface EmbeddedLyricsWriteResult {
    data class Success(val payload: LyricsPayload) : EmbeddedLyricsWriteResult
    data class PermissionRequired(val request: PendingIntent) : EmbeddedLyricsWriteResult
    data class Failure(val reason: String) : EmbeddedLyricsWriteResult
}

internal class EmbeddedLyricsWriter(
    context: Context,
    private val mediaMutationJournal: MediaMutationJournal? = null,
) {
    private val appContext = context.applicationContext
    private val contentResolver: ContentResolver = appContext.contentResolver
    private val audioFormatDetector = AudioFormatDetector(appContext)

    fun createWritePermissionRequest(song: Song): PendingIntent? {
        if (!song.uri.isContentUri()) return null
        return runCatching {
            MediaStore.createWriteRequest(contentResolver, listOf(song.uri))
        }.getOrNull()
    }

    suspend fun write(song: Song, rawLyrics: String): EmbeddedLyricsWriteResult {
        val lyrics = rawLyrics.canonicalEmbeddedLyricsText()
        val mutationId = mediaMutationJournal?.create(
            MediaMutationOperation(
                type = MediaMutationType.EmbeddedLyricsWrite,
                songId = song.id,
                albumId = song.albumId,
                uri = song.uri,
                displayName = song.fileName,
            ),
        )
        val detectedFormat = song.fileName
            .takeIf { AudioFormatPolicy.requiresContainerValidation(it.substringAfterLast('.', "")) }
            ?.let { audioFormatDetector.detect(song.uri, song.fileName, null) }
        if (AudioFormatPolicy.embeddedLyricsWriteSupport(detectedFormat, song.fileName) != TagWriteSupport.Safe) {
            mutationId?.let {
                mediaMutationJournal.mark(it, MediaMutationStatus.Failed, "Unsupported lyrics write format")
            }
            return EmbeddedLyricsWriteResult.Failure("This audio format cannot store lyrics safely.")
        }

        var backupFile: File? = null
        var workingFile: File? = null
        var persistedFile: File? = null
        return try {
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.PreflightPassed) }
            backupFile = copySongToTemp(song, "backup")
            workingFile = createTempFile(song, "working").also { backupFile.copyTo(it, overwrite = true) }

            val audioFile = AudioFileIO.read(workingFile)
            val tag = audioFile.tagOrCreateAndSetDefault
            if (lyrics.isBlank()) {
                tag.deleteField(FieldKey.LYRICS)
            } else {
                tag.setField(FieldKey.LYRICS, lyrics)
            }
            audioFile.commit()
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.TempWritten) }
            verifyLyrics(workingFile, lyrics)
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.TempVerified) }

            try {
                overwriteOriginal(song, workingFile)
            } catch (throwable: Throwable) {
                runCatching { overwriteOriginal(song, backupFile) }
                throw throwable
            }
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.Committed) }

            persistedFile = copySongToTemp(song, "verify")
            try {
                verifyLyrics(persistedFile, lyrics)
            } catch (throwable: Throwable) {
                overwriteOriginal(song, backupFile)
                throw throwable
            }
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.PersistedVerified) }

            val payload = parseLrcOrPlain(lyrics, providerName = "Embedded", confidence = 100)
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.Completed) }
            EmbeddedLyricsWriteResult.Success(
                payload ?: LyricsPayload(
                    lines = emptyList(),
                    isSynced = false,
                    providerName = "Embedded",
                    confidence = 100,
                    sourceTextForEmbedding = lyrics,
                ),
            )
        } catch (throwable: CancellationException) {
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.Cancelled) }
            throw throwable
        } catch (throwable: RecoverableSecurityException) {
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.NeedsPermission) }
            EmbeddedLyricsWriteResult.PermissionRequired(throwable.userAction.actionIntent)
        } catch (throwable: Throwable) {
            mutationId?.let {
                mediaMutationJournal.mark(it, MediaMutationStatus.Failed, throwable.message)
            }
            EmbeddedLyricsWriteResult.Failure(throwable.message ?: "Unable to save lyrics.")
        } finally {
            runCatching { backupFile?.delete() }
            runCatching { workingFile?.delete() }
            runCatching { persistedFile?.delete() }
        }
    }

    private fun verifyLyrics(file: File, expected: String) {
        val actual = AudioFileIO.read(file)
            .tagOrCreateAndSetDefault
            .getFirst(FieldKey.LYRICS)
            .orEmpty()
            .canonicalEmbeddedLyricsText()
        check(actual == expected.canonicalEmbeddedLyricsText()) { "Lyrics verification failed after writing metadata." }
    }

    private fun copySongToTemp(song: Song, purpose: String): File {
        val destination = createTempFile(song, purpose)
        contentResolver.openInputStream(song.uri)?.use { input ->
            destination.outputStream().use(input::copyTo)
        } ?: error("Unable to open ${song.fileName}")
        return destination
    }

    private fun createTempFile(song: Song, purpose: String): File {
        val directory = File(appContext.cacheDir, TEMP_DIRECTORY).apply { mkdirs() }
        val extension = song.fileName.substringAfterLast('.', "").ifBlank { "tmp" }
        return File(directory, "${song.id}-$purpose-${System.nanoTime()}.$extension")
    }

    private fun overwriteOriginal(song: Song, source: File) {
        val descriptor = try {
            contentResolver.openFileDescriptor(song.uri, "rwt")
        } catch (_: IllegalArgumentException) {
            contentResolver.openFileDescriptor(song.uri, "rw")
        } ?: error("Unable to open the song for writing.")

        descriptor.use {
            FileOutputStream(it.fileDescriptor).channel.use { output ->
                output.position(0L)
                output.truncate(0L)
                FileInputStream(source).channel.use { input ->
                    var position = 0L
                    while (position < input.size()) {
                        val copied = input.transferTo(position, input.size() - position, output)
                        check(copied > 0L) { "Unable to replace the song metadata." }
                        position += copied
                    }
                }
                output.force(true)
            }
        }
    }

    private companion object {
        const val TEMP_DIRECTORY = "lyrics-tag-edit"
    }
}
