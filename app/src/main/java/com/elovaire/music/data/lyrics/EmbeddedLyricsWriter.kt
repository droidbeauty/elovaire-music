package elovaire.music.droidbeauty.app.data.lyrics

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.TagWriteSupport
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationOperation
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationStatus
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationType
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

internal enum class EmbeddedLyricsWriteFailure {
    UnsupportedFormat,
    PermissionDenied,
    SourceReadFailed,
    TempWriteFailed,
    TagCommitFailed,
    TempVerificationFailed,
    OriginalOverwriteFailed,
    PersistedVerificationFailed,
    RollbackFailed,
    Unknown,
}

internal sealed interface EmbeddedLyricsWriteResult {
    data class Success(val payload: LyricsPayload) : EmbeddedLyricsWriteResult
    data class PermissionRequired(val request: android.app.PendingIntent) : EmbeddedLyricsWriteResult
    data class Failure(
        val failure: EmbeddedLyricsWriteFailure,
        val reason: String,
    ) : EmbeddedLyricsWriteResult
}

internal class EmbeddedLyricsWriter(
    context: Context,
    private val mediaMutationJournal: MediaMutationJournal? = null,
) {
    private val appContext = context.applicationContext
    private val contentResolver: ContentResolver = appContext.contentResolver
    private val audioFormatDetector = AudioFormatDetector(appContext)
    private val localLyricsResolver = LocalLyricsResolver(appContext)
    private val writeMutex = Mutex()

    suspend fun write(song: Song, rawLyrics: String): EmbeddedLyricsWriteResult = writeMutex.withLock {
        writeLocked(song, rawLyrics)
    }

    private suspend fun writeLocked(song: Song, rawLyrics: String): EmbeddedLyricsWriteResult {
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
            trace(song, "unsupported_format")
            mutationId?.let {
                mediaMutationJournal.mark(it, MediaMutationStatus.Failed, "Unsupported lyrics write format")
            }
            return EmbeddedLyricsWriteResult.Failure(
                failure = EmbeddedLyricsWriteFailure.UnsupportedFormat,
                reason = "This audio format cannot store lyrics safely.",
            )
        }

        var backupFile: File? = null
        var workingFile: File? = null
        var persistedFile: File? = null
        var phase = LyricsWritePhase.SourceRead
        var needsRepair = false
        return try {
            trace(song, "preflight")
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.PreflightPassed) }
            trace(song, "temp_copy")
            backupFile = copySongToTemp(song, "backup")
            phase = LyricsWritePhase.TempWrite
            trace(song, "temp_write")
            workingFile = createTempFile(song, "working").also { backupFile.copyTo(it, overwrite = true) }

            phase = LyricsWritePhase.TagCommit
            trace(song, "tag_commit")
            val audioFile = AudioFileIO.read(workingFile)
            val tag = audioFile.tagOrCreateAndSetDefault
            if (lyrics.isBlank()) {
                tag.deleteField(FieldKey.LYRICS)
            } else {
                tag.setField(FieldKey.LYRICS, lyrics)
            }
            audioFile.commit()
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.TempWritten) }
            phase = LyricsWritePhase.TempVerification
            trace(song, "temp_verify")
            verifyLyrics(workingFile, lyrics)
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.TempVerified) }

            phase = LyricsWritePhase.OriginalOverwrite
            trace(song, "original_overwrite")
            try {
                overwriteOriginal(song, workingFile)
            } catch (throwable: Throwable) {
                needsRepair = runCatching { overwriteOriginal(song, backupFile) }.isFailure
                throw throwable
            }
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.Committed) }

            phase = LyricsWritePhase.PersistedVerification
            trace(song, "persisted_verify")
            persistedFile = copySongToTemp(song, "verify")
            try {
                verifyLyrics(persistedFile, lyrics)
            } catch (throwable: Throwable) {
                needsRepair = runCatching { overwriteOriginal(song, backupFile) }.isFailure
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
            trace(song, "permission_required", throwable)
            mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.NeedsPermission) }
            EmbeddedLyricsWriteResult.PermissionRequired(throwable.userAction.actionIntent)
        } catch (throwable: Throwable) {
            val failure = if (needsRepair) EmbeddedLyricsWriteFailure.RollbackFailed else phase.failure
            trace(song, "failed:${failure.name}", throwable)
            mutationId?.let {
                mediaMutationJournal.mark(
                    it,
                    if (needsRepair) MediaMutationStatus.NeedsRepair else MediaMutationStatus.Failed,
                    "${failure.name}:${throwable.javaClass.simpleName}",
                )
            }
            EmbeddedLyricsWriteResult.Failure(failure, failure.userMessage)
        } finally {
            runCatching { backupFile?.delete() }
            runCatching { workingFile?.delete() }
            runCatching { persistedFile?.delete() }
        }
    }

    private fun verifyLyrics(file: File, expected: String) {
        val verificationSong = Song(
            id = Long.MIN_VALUE,
            title = file.nameWithoutExtension,
            isExplicit = false,
            artist = "",
            album = "",
            releaseYear = null,
            genre = "",
            audioFormat = "",
            audioQuality = null,
            fileName = file.name,
            albumId = Long.MIN_VALUE,
            durationMs = 0L,
            trackNumber = 0,
            discNumber = 1,
            dateAddedSeconds = 0L,
            libraryPath = file.absolutePath,
            uri = Uri.fromFile(file),
            artUri = null,
        )
        val actual = localLyricsResolver.resolve(verificationSong)
            ?.payload
            ?.toEmbeddedLyricsText()
            .orEmpty()
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
                    val sourceSize = input.size()
                    var position = 0L
                    while (position < sourceSize) {
                        val copied = input.transferTo(position, sourceSize - position, output)
                        check(copied > 0L) { "Unable to replace the song metadata." }
                        position += copied
                    }
                }
                output.force(true)
            }
        }
    }

    private fun trace(
        song: Song,
        phase: String,
        throwable: Throwable? = null,
    ) {
        if (!BuildConfig.DEBUG) return
        Log.d(
            LOG_TAG,
            "song=${song.id} scheme=${song.uri.scheme} extension=${song.fileName.substringAfterLast('.', "")} " +
                "phase=$phase error=${throwable?.javaClass?.simpleName.orEmpty()}",
        )
    }

    private companion object {
        const val TEMP_DIRECTORY = "lyrics-tag-edit"
        const val LOG_TAG = "EmbeddedLyricsWriter"
    }
}

private enum class LyricsWritePhase(
    val failure: EmbeddedLyricsWriteFailure,
) {
    SourceRead(EmbeddedLyricsWriteFailure.SourceReadFailed),
    TempWrite(EmbeddedLyricsWriteFailure.TempWriteFailed),
    TagCommit(EmbeddedLyricsWriteFailure.TagCommitFailed),
    TempVerification(EmbeddedLyricsWriteFailure.TempVerificationFailed),
    OriginalOverwrite(EmbeddedLyricsWriteFailure.OriginalOverwriteFailed),
    PersistedVerification(EmbeddedLyricsWriteFailure.PersistedVerificationFailed),
}

private val EmbeddedLyricsWriteFailure.userMessage: String
    get() = when (this) {
        EmbeddedLyricsWriteFailure.UnsupportedFormat -> "This audio format cannot store lyrics safely."
        EmbeddedLyricsWriteFailure.PermissionDenied -> "Permission to edit this song was denied."
        EmbeddedLyricsWriteFailure.SourceReadFailed -> "Unable to read this song for lyrics editing."
        EmbeddedLyricsWriteFailure.TempWriteFailed,
        EmbeddedLyricsWriteFailure.TagCommitFailed,
        -> "Unable to write lyrics metadata safely."
        EmbeddedLyricsWriteFailure.TempVerificationFailed -> "Lyrics metadata could not be verified before saving."
        EmbeddedLyricsWriteFailure.OriginalOverwriteFailed -> "Unable to save lyrics to the song file."
        EmbeddedLyricsWriteFailure.PersistedVerificationFailed -> "Lyrics could not be verified after saving."
        EmbeddedLyricsWriteFailure.RollbackFailed -> "The song could not be restored after the lyrics write failed."
        EmbeddedLyricsWriteFailure.Unknown -> "Unable to save lyrics."
    }
