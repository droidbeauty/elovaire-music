package elovaire.music.droidbeauty.app.data.lyrics

import android.app.RecoverableSecurityException
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.DetectedAudioFormat
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationRuntime
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationOperation
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationType
import elovaire.music.droidbeauty.app.data.mutation.MediaFileMutationRunner
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationFaultInjector
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationTransactionPhase
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationStage
import elovaire.music.droidbeauty.app.data.mutation.NoOpMediaMutationFaultInjector
import elovaire.music.droidbeauty.app.data.mutation.VerifiedMediaFileMutationResult
import elovaire.music.droidbeauty.app.data.mutation.VerifiedMediaFileMutationTransaction
import elovaire.music.droidbeauty.app.domain.kernel.MediaMutationStatus
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.platform.MediaWriteTarget
import elovaire.music.droidbeauty.app.platform.MediaWriteTargetClassifier
import elovaire.music.droidbeauty.app.platform.ProviderRejectedWriteModeException
import elovaire.music.droidbeauty.app.platform.mediaStoreWritePendingIntent
import java.io.File

internal enum class EmbeddedLyricsWriteFailure {
    UnsupportedFormat,
    UnsupportedSyncedLyrics,
    LyricsTooLarge,
    InvalidMediaStoreItemUri,
    StoragePermissionMissing,
    WriteAccessUnavailable,
    WriteAccessStillUnavailableAfterGrant,
    ProviderRejectedWriteMode,
    SourceReadFailed,
    TempWriteFailed,
    TagCommitFailed,
    TempVerificationFailed,
    OriginalOverwriteFailed,
    PersistedVerificationFailed,
    RollbackFailed,
}

internal sealed interface EmbeddedLyricsWriteResult {
    data class Success(val payload: LyricsPayload) : EmbeddedLyricsWriteResult
    data class PermissionRequired(
        val mediaUri: Uri,
        val request: android.app.PendingIntent,
    ) : EmbeddedLyricsWriteResult
    data class Failure(
        val failure: EmbeddedLyricsWriteFailure,
        val reason: String,
    ) : EmbeddedLyricsWriteResult
}

internal class EmbeddedLyricsWriter(
    context: Context,
    private val mediaMutationJournal: MediaMutationJournal? = null,
    mediaMutationRuntime: MediaMutationRuntime? = null,
    private val faultInjector: MediaMutationFaultInjector = NoOpMediaMutationFaultInjector,
) {
    private val appContext = context.applicationContext
    private val audioFormatDetector = AudioFormatDetector(appContext)
    private val mutationRunner = MediaFileMutationRunner(appContext, TEMP_DIRECTORY, faultInjector)
    private val mutationTransaction = VerifiedMediaFileMutationTransaction(
        mutationRunner = mutationRunner,
        mediaMutationJournal = mediaMutationJournal,
        faultInjector = faultInjector,
    )
    private val mutationRuntime = mediaMutationRuntime ?: MediaMutationRuntime()

    suspend fun write(
        song: Song,
        rawLyrics: String,
        operationId: String? = null,
        approvedMediaUri: Uri? = null,
    ): EmbeddedLyricsWriteResult = mutationRuntime.withTarget(song.uri) {
        writeLocked(song, rawLyrics, operationId, approvedMediaUri)
    }

    @Suppress("LongMethod", "TooGenericExceptionCaught")
    private suspend fun writeLocked(
        song: Song,
        rawLyrics: String,
        operationId: String?,
        approvedMediaUri: Uri?,
    ): EmbeddedLyricsWriteResult {
        val lyrics = if (rawLyrics.length <= MAX_LYRICS_CHARACTERS) {
            rawLyrics.canonicalEmbeddedLyricsText()
        } else {
            rawLyrics
        }
        val request = EmbeddedLyricsWriteRequest(
            song = song,
            rawLyrics = rawLyrics,
            canonicalLyrics = lyrics,
            tagKind = classifyLyricsTagKind(lyrics),
        )
        faultInjector.checkpoint(MediaMutationTransactionPhase.BeforeJournal)
        val mutationId = createMutation(song, operationId)
        tracePermissionContext(song, operationId, approvedMediaUri)
        if (approvedMediaUri != null) {
            mutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.PermissionGranted) }
        }
        preflightFailure(request, approvedMediaUri)?.let { failure ->
            return failPreflight(song, mutationId, failure)
        }

        return when (val transaction = mutationTransaction.execute(
            song = song,
            operation = MediaMutationOperation(
                mutationId = mutationId,
                type = MediaMutationType.EmbeddedLyricsWrite,
                songId = song.id,
                albumId = song.albumId,
                uri = song.uri,
                displayName = song.fileName,
            ),
            mutationId = mutationId,
            mutateWorkingCopy = { file -> EmbeddedLyricsMetadata.write(file, request) },
            verifyWorkingCopy = { file -> verifyLyrics(file, request) },
            verifyPersistedCopy = { file -> verifyLyrics(file, request) },
        )) {
            is VerifiedMediaFileMutationResult.Success -> successResult(lyrics)
            is VerifiedMediaFileMutationResult.Failure -> mapTransactionFailure(
                song = song,
                approvedMediaUri = approvedMediaUri,
                transaction = transaction,
            )
        }
    }

    private suspend fun mapTransactionFailure(
        song: Song,
        approvedMediaUri: Uri?,
        transaction: VerifiedMediaFileMutationResult.Failure,
    ): EmbeddedLyricsWriteResult {
        val mutationId = transaction.mutationId
        return when (val throwable = transaction.cause) {
            is RecoverableSecurityException -> {
                if (approvedMediaUri != null) {
                    handlePostGrantSecurityFailure(song, mutationId, throwable)
                } else {
                    trace(song, "permission_required", throwable)
                    mutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.NeedsPermission) }
                    EmbeddedLyricsWriteResult.PermissionRequired(song.uri, throwable.userAction.actionIntent)
                }
            }
            is SecurityException -> {
                if (approvedMediaUri != null) {
                    handlePostGrantSecurityFailure(song, mutationId, throwable)
                } else {
                    handleSecurityFailure(song, mutationId, throwable)
                }
            }
            is ProviderRejectedWriteModeException -> {
                val failure = EmbeddedLyricsWriteFailure.ProviderRejectedWriteMode
                trace(song, "failed:${failure.name}", throwable)
                mutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.Failed, failure.name) }
                EmbeddedLyricsWriteResult.Failure(failure, failure.userMessage)
            }
            else -> {
                val failure = if (transaction.rollbackFailed) {
                    EmbeddedLyricsWriteFailure.RollbackFailed
                } else {
                    transaction.stage.toEmbeddedLyricsFailure()
                }
                trace(song, "failed:${failure.name}", throwable)
                EmbeddedLyricsWriteResult.Failure(failure, failure.userMessage)
            }
        }
    }

    private fun verifyLyrics(file: File, request: EmbeddedLyricsWriteRequest) {
        val fields = AudioFileLyricsInspection.inspect(file)
        if (request.canonicalLyrics.isBlank()) {
            check(fields.synced.isEmpty() && fields.unsynced.isEmpty() && fields.compatibility.isEmpty()) {
                "Lyrics metadata was not cleared."
            }
            return
        }
        val expectedPayload = parseLrcOrPlain(request.canonicalLyrics)
        when (request.tagKind) {
            EmbeddedLyricsTagKind.SyncedLyrics -> {
                val expectedLines = expectedPayload
                    ?.takeIf(LyricsPayload::isSynced)
                    ?.lines
                    ?.map { it.startTimeMs to it.text.trim() }
                    .orEmpty()
                check(expectedLines.isNotEmpty()) { "Synchronized lyrics contain no timed lines." }
                check(fields.synced.any { lines -> lines.map { it.startTimeMs to it.text.trim() } == expectedLines }) {
                    "The synchronized lyrics field was not persisted."
                }
            }
            EmbeddedLyricsTagKind.UnsyncedLyrics -> {
                check(fields.unsynced.any { it == request.canonicalLyrics }) {
                    "The unsynchronized lyrics field was not persisted."
                }
            }
        }
    }

    private fun successResult(lyrics: String): EmbeddedLyricsWriteResult.Success {
        val payload = parseLrcOrPlain(lyrics)
            ?: LyricsPayload(
                lines = emptyList(),
                isSynced = false,
                sourceTextForEmbedding = lyrics,
            )
        return EmbeddedLyricsWriteResult.Success(payload)
    }

    private suspend fun createMutation(song: Song, operationId: String?): String? {
        return mediaMutationJournal?.create(
            MediaMutationOperation(
                mutationId = operationId,
                type = MediaMutationType.EmbeddedLyricsWrite,
                songId = song.id,
                albumId = song.albumId,
                uri = song.uri,
                displayName = song.fileName,
            ),
        )
    }

    private suspend fun handleSecurityFailure(
        song: Song,
        mutationId: String?,
        throwable: SecurityException,
    ): EmbeddedLyricsWriteResult {
        val target = MediaWriteTargetClassifier.classify(appContext, song.uri)
        if (target is MediaWriteTarget.MediaStoreItem) {
            mediaStoreWritePendingIntent(appContext, listOf(song.uri))?.let { requestIntent ->
                trace(song, "permission_required", throwable)
                mutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.NeedsPermission) }
                return EmbeddedLyricsWriteResult.PermissionRequired(song.uri, requestIntent)
            }
        }
        val failure = if (target is MediaWriteTarget.SafDocument) {
            EmbeddedLyricsWriteFailure.StoragePermissionMissing
        } else {
            EmbeddedLyricsWriteFailure.WriteAccessUnavailable
        }
        trace(song, "failed:${failure.name}", throwable)
        mutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.Failed, failure.name) }
        return EmbeddedLyricsWriteResult.Failure(failure, failure.userMessage)
    }

    private suspend fun handlePostGrantSecurityFailure(
        song: Song,
        mutationId: String?,
        throwable: SecurityException,
    ): EmbeddedLyricsWriteResult {
        val failure = EmbeddedLyricsWriteFailure.WriteAccessStillUnavailableAfterGrant
        trace(song, "failed:${failure.name}", throwable)
        mutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.Failed, failure.name) }
        return EmbeddedLyricsWriteResult.Failure(failure, failure.userMessage)
    }

    private suspend fun failPreflight(
        song: Song,
        mutationId: String?,
        failure: EmbeddedLyricsWriteFailure,
    ): EmbeddedLyricsWriteResult {
        trace(song, "failed:${failure.name}")
        mutationId?.let { mediaMutationJournal?.mark(it, MediaMutationStatus.Failed, failure.name) }
        return EmbeddedLyricsWriteResult.Failure(failure, failure.userMessage)
    }

    private fun unsupportedFailure(
        request: EmbeddedLyricsWriteRequest,
        detectedFormat: DetectedAudioFormat?,
    ): EmbeddedLyricsWriteFailure? {
        val capability = AudioFormatPolicy.lyricsWriteCapability(detectedFormat, request.song.fileName)
        if (
            request.tagKind == EmbeddedLyricsTagKind.SyncedLyrics &&
            capability.synced != elovaire.music.droidbeauty.app.data.audio.CapabilityLevel.Strong
        ) {
            return EmbeddedLyricsWriteFailure.UnsupportedSyncedLyrics
        }
        return EmbeddedLyricsWriteFailure.UnsupportedFormat.takeIf {
            capability.unsynced != elovaire.music.droidbeauty.app.data.audio.CapabilityLevel.Strong
        }
    }

    private fun preflightFailure(
        request: EmbeddedLyricsWriteRequest,
        approvedMediaUri: Uri?,
    ): EmbeddedLyricsWriteFailure? {
        if (request.rawLyrics.length > MAX_LYRICS_CHARACTERS || !request.canonicalLyrics.isLyricsTextWithinBounds()) {
            return EmbeddedLyricsWriteFailure.LyricsTooLarge
        }
        val song = request.song
        val target = MediaWriteTargetClassifier.classify(appContext, song.uri)
        if (song.uri.authority == android.provider.MediaStore.AUTHORITY && target !is MediaWriteTarget.MediaStoreItem) {
            return EmbeddedLyricsWriteFailure.InvalidMediaStoreItemUri
        }
        if (
            approvedMediaUri != null &&
            (target !is MediaWriteTarget.MediaStoreItem || approvedMediaUri.toString() != song.uri.toString())
        ) {
            return EmbeddedLyricsWriteFailure.InvalidMediaStoreItemUri
        }
        val detectedFormat = song.fileName
            .takeIf { AudioFormatPolicy.requiresContainerValidation(it.substringAfterLast('.', "")) }
            ?.let { audioFormatDetector.detect(song.uri, song.fileName, null) }
        return unsupportedFailure(request, detectedFormat)
    }

    private fun trace(
        song: Song,
        phase: String,
        throwable: Throwable? = null,
    ) {
        if (!BuildConfig.DEBUG) return
        Log.d(
            LOG_TAG,
            "song=${song.id} scheme=${song.uri.scheme} authority=${song.uri.authority.orEmpty()} " +
                "extension=${song.fileName.substringAfterLast('.', "")} " +
                "phase=$phase error=${throwable?.javaClass?.simpleName.orEmpty()}",
        )
    }

    private fun tracePermissionContext(song: Song, operationId: String?, approvedMediaUri: Uri?) {
        if (!BuildConfig.DEBUG) return
        val target = MediaWriteTargetClassifier.classify(appContext, song.uri)
        Log.d(
            LOG_TAG,
            "operation=${operationId.orEmpty()} api=${Build.VERSION.SDK_INT} song=${song.id} " +
                "target=${target::class.simpleName} uriScheme=${song.uri.scheme.orEmpty()} " +
                "uriAuthority=${song.uri.authority.orEmpty()} uriDepth=${song.uri.pathSegments.size} " +
                "retryProvided=${approvedMediaUri != null}",
        )
    }

    private companion object {
        const val TEMP_DIRECTORY = "lyrics-tag-edit"
        const val LOG_TAG = "EmbeddedLyricsWriter"
    }
}

private fun MediaMutationStage.toEmbeddedLyricsFailure(): EmbeddedLyricsWriteFailure = when (this) {
    MediaMutationStage.SourceRead -> EmbeddedLyricsWriteFailure.SourceReadFailed
    MediaMutationStage.TempWrite -> EmbeddedLyricsWriteFailure.TempWriteFailed
    MediaMutationStage.WorkingMutation -> EmbeddedLyricsWriteFailure.TagCommitFailed
    MediaMutationStage.WorkingVerification -> EmbeddedLyricsWriteFailure.TempVerificationFailed
    MediaMutationStage.OriginalOverwrite -> EmbeddedLyricsWriteFailure.OriginalOverwriteFailed
    MediaMutationStage.PersistedVerification -> EmbeddedLyricsWriteFailure.PersistedVerificationFailed
}

internal val EmbeddedLyricsWriteFailure.userMessage: String
    get() = when (this) {
        EmbeddedLyricsWriteFailure.UnsupportedFormat -> "This audio format cannot store lyrics safely."
        EmbeddedLyricsWriteFailure.UnsupportedSyncedLyrics -> "This audio format cannot store synchronized lyrics safely."
        EmbeddedLyricsWriteFailure.LyricsTooLarge -> "These lyrics are too large to store safely."
        EmbeddedLyricsWriteFailure.InvalidMediaStoreItemUri -> "This song does not have a valid MediaStore item address."
        EmbeddedLyricsWriteFailure.StoragePermissionMissing -> "Access to this music folder was lost. Add the folder again to save lyrics."
        EmbeddedLyricsWriteFailure.WriteAccessUnavailable -> "Android could not open this song for editing."
        EmbeddedLyricsWriteFailure.WriteAccessStillUnavailableAfterGrant ->
            "Android granted access, but the song file still could not be opened for editing."
        EmbeddedLyricsWriteFailure.ProviderRejectedWriteMode -> "The storage provider does not support safely replacing this song."
        EmbeddedLyricsWriteFailure.SourceReadFailed -> "Unable to read this song for lyrics editing."
        EmbeddedLyricsWriteFailure.TempWriteFailed -> "Unable to prepare this song for lyrics editing."
        EmbeddedLyricsWriteFailure.TagCommitFailed -> "This song's lyrics metadata could not be updated."
        EmbeddedLyricsWriteFailure.TempVerificationFailed -> "Lyrics metadata could not be verified before saving."
        EmbeddedLyricsWriteFailure.OriginalOverwriteFailed -> "Unable to save lyrics to the song file."
        EmbeddedLyricsWriteFailure.PersistedVerificationFailed -> "Lyrics could not be verified after saving."
        EmbeddedLyricsWriteFailure.RollbackFailed -> "The song could not be restored after the lyrics write failed."
    }
