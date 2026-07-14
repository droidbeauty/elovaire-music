package elovaire.music.droidbeauty.app.data.tags

import android.content.ContentResolver
import android.content.Context
import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.net.Uri
import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.library.queryMediaStoreFilePath
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.TagWriteSupport
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.mutation.MediaFileMutationRunner
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationOperation
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationStatus
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationType
import elovaire.music.droidbeauty.app.platform.ContentIo
import elovaire.music.droidbeauty.app.data.tags.matching.AlbumArtworkResolver
import elovaire.music.droidbeauty.app.data.tags.matching.AlbumTagMatchResult
import elovaire.music.droidbeauty.app.data.tags.matching.AndroidChromaprintFingerprintProvider
import elovaire.music.droidbeauty.app.data.tags.matching.CoverArtArchiveClient
import elovaire.music.droidbeauty.app.data.tags.matching.EmbeddedArtworkProvider
import elovaire.music.droidbeauty.app.data.tags.matching.FingerprintAlbumTagMatcher
import elovaire.music.droidbeauty.app.data.tags.matching.HttpAcoustIdClient
import elovaire.music.droidbeauty.app.data.tags.matching.HttpMusicBrainzClient
import elovaire.music.droidbeauty.app.data.tags.matching.TagMatchCache
import elovaire.music.droidbeauty.app.data.tags.matching.TidalArtworkProvider
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory

internal data class EditableAlbumTrack(
    val songId: Long,
    val title: String,
    val artist: String,
    val trackNumber: Int,
    val discNumber: Int,
    val durationMs: Long? = null,
)

internal sealed interface TagFieldEdit<out T> {
    data object Unchanged : TagFieldEdit<Nothing>
    data object Cleared : TagFieldEdit<Nothing>
    data class Value<T>(val value: T) : TagFieldEdit<T>
}

internal data class AlbumTagEditRequest(
    val album: Album,
    val albumTitle: TagFieldEdit<String>,
    val albumArtist: TagFieldEdit<String>,
    val releaseYear: TagFieldEdit<Int>,
    val genre: TagFieldEdit<String>,
    val coverArtUri: Uri?,
    val coverArtBytes: ByteArray? = null,
    val tracks: List<EditableAlbumTrack>,
)

internal data class AlbumTagMatchSuggestion(
    val albumTitle: String,
    val albumArtist: String,
    val releaseYear: Int?,
    val genre: String,
    val coverArtBytes: ByteArray?,
    val tracks: List<EditableAlbumTrack>,
)

internal sealed interface OnlineTagMatchOutcome {
    data class Success(val suggestion: AlbumTagMatchSuggestion) : OnlineTagMatchOutcome
    data class Unavailable(val reason: String) : OnlineTagMatchOutcome
    data class NoMatch(val reason: String) : OnlineTagMatchOutcome
    data class Failed(val reason: String) : OnlineTagMatchOutcome
}

internal data class TagEditApplyResult(
    val editedSongIds: List<Long>,
    val editedUris: List<Uri>,
    val editedFilePaths: List<String>,
    val editedSongs: List<Song>,
    val artworkChanged: Boolean,
    val failures: List<TagEditFailure> = emptyList(),
    val permissionRequest: PendingIntent? = null,
)

internal data class TagEditFailure(
    val songId: Long,
    val fileName: String,
    val reason: String,
    val cause: TagEditFailureCause = TagEditFailureCause.Unknown(reason),
)

internal sealed interface TagEditFailureCause {
    data object UnsupportedFormat : TagEditFailureCause
    data object UnsupportedArtworkFormat : TagEditFailureCause
    data object CannotReadArtwork : TagEditFailureCause
    data object CannotOpenInput : TagEditFailureCause
    data object CannotOpenOutput : TagEditFailureCause
    data object TempWriteFailed : TagEditFailureCause
    data object TempVerificationFailed : TagEditFailureCause
    data object PersistedVerificationFailed : TagEditFailureCause
    data object PermissionRequired : TagEditFailureCause
    data object PermissionDeniedAfterGrant : TagEditFailureCause
    data object RollbackFailed : TagEditFailureCause
    data class TagLibraryFailure(val message: String) : TagEditFailureCause
    data class Unknown(val message: String?) : TagEditFailureCause
}

internal class AlbumTagEditorService(
    context: Context,
    private val mediaMutationJournal: MediaMutationJournal? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val appContext = context.applicationContext
    private val contentResolver: ContentResolver = appContext.contentResolver
    private val contentIo = ContentIo(contentResolver)
    private val audioFormatDetector = AudioFormatDetector(appContext)
    private val mutationRunner = MediaFileMutationRunner(appContext, TEMP_TAG_EDIT_DIR_NAME)
    private val matchCache = TagMatchCache(appContext)
    private val albumMatcher = FingerprintAlbumTagMatcher(
        fingerprintProvider = AndroidChromaprintFingerprintProvider(appContext, matchCache),
        acoustIdClient = HttpAcoustIdClient(BuildConfig.ACOUSTID_API_KEY, matchCache),
        musicBrainzClient = HttpMusicBrainzClient(matchCache),
        artworkResolver = AlbumArtworkResolver(
            tidalArtworkProvider = TidalArtworkProvider(),
            coverArtArchiveClient = CoverArtArchiveClient(),
            embeddedArtworkProvider = EmbeddedArtworkProvider(appContext),
        ),
    )

    suspend fun findBestOnlineMatch(album: Album): OnlineTagMatchOutcome = withContext(ioDispatcher) {
        when (val result = albumMatcher.matchAlbum(album)) {
            is AlbumTagMatchResult.Success -> {
                val suggestion = AlbumTagMatchSuggestion(
                    albumTitle = result.match.release.title.ifBlank { album.title },
                    albumArtist = result.match.release.albumArtist.ifBlank { album.artist },
                    releaseYear = result.match.release.releaseYear,
                    genre = album.songs.firstOrNull { it.genre.isNotBlank() }?.genre.orEmpty(),
                    coverArtBytes = result.artwork?.bytes,
                    tracks = album.songs.map { song ->
                        val resolved = result.match.trackMatches.firstOrNull { it.song.id == song.id }
                        EditableAlbumTrack(
                            songId = song.id,
                            title = resolved?.remoteTrack?.title?.takeIf(String::isNotBlank) ?: song.title,
                            artist = resolved?.remoteTrack?.artist?.takeIf(String::isNotBlank) ?: song.artist,
                            trackNumber = resolved?.remoteTrack?.trackNumber?.takeIf { it > 0 }
                                ?: song.trackNumber.coerceAtLeast(1),
                            discNumber = resolved?.remoteTrack?.discNumber?.takeIf { it > 0 }
                                ?: song.discNumber.coerceAtLeast(1),
                            durationMs = song.durationMs,
                        )
                    },
                )
                OnlineTagMatchOutcome.Success(suggestion)
            }

            is AlbumTagMatchResult.Unavailable -> OnlineTagMatchOutcome.Unavailable(result.reason)
            is AlbumTagMatchResult.NoMatch -> OnlineTagMatchOutcome.NoMatch(result.reason)
            is AlbumTagMatchResult.Failed -> OnlineTagMatchOutcome.Failed(result.reason)
        }
    }

    suspend fun applyEdits(
        request: AlbumTagEditRequest,
        writeConsentGranted: Boolean = false,
    ): TagEditApplyResult = withContext(ioDispatcher) {
        logDebug("Applying tag edit album=${request.album.id} tracks=${request.tracks.size}")
        val plans = TagEditPlanner.plansFor(request)
        val coverArtBytes = request.coverArtBytes ?: request.coverArtUri?.let(::readBytes)
        val coverArtMimeType = coverArtBytes?.let(::detectMimeType)
        val editedSongIds = mutableListOf<Long>()
        val editedUris = mutableListOf<Uri>()
        val editedFilePaths = mutableListOf<String>()
        val editedSongs = mutableListOf<Song>()
        val failures = mutableListOf<TagEditFailure>()
        var permissionRequest: PendingIntent? = null
        if (request.coverArtUri != null && coverArtBytes == null) {
            return@withContext TagEditApplyResult(
                editedSongIds = emptyList(),
                editedUris = emptyList(),
                editedFilePaths = emptyList(),
                editedSongs = emptyList(),
                artworkChanged = false,
                failures = plans.map { plan ->
                    TagEditFailure(
                        songId = plan.song.id,
                        fileName = plan.song.fileName,
                        reason = "Unable to read the selected artwork.",
                        cause = TagEditFailureCause.CannotReadArtwork,
                    )
                },
            )
        }
        plans.forEach { plan ->
            val song = plan.song
            val trackEdit = plan.trackEdit
            val mutationId = mediaMutationJournal?.create(
                MediaMutationOperation(
                    type = if (coverArtBytes != null) MediaMutationType.ArtworkWrite else MediaMutationType.TagEdit,
                    songId = song.id,
                    albumId = song.albumId,
                    uri = song.uri,
                    displayName = song.fileName,
                ),
            )
            val detectedFormat = song.fileName
                .takeIf { AudioFormatPolicy.requiresContainerValidation(it.substringAfterLast('.', "")) }
                ?.let { audioFormatDetector.detect(song.uri, song.fileName, null) }
            if (AudioFormatPolicy.tagWriteSupport(detectedFormat, song.fileName) != TagWriteSupport.Safe) {
                val capability = AudioFormatPolicy.capabilityForFileName(song.fileName)
                logDebug("Skipped unsafe tag write for ${song.fileName}: ${capability?.notes ?: "unknown format"}")
                mutationId?.let {
                    mediaMutationJournal.mark(it, MediaMutationStatus.Failed, "Unsupported tag write format")
                }
                failures += TagEditFailure(
                    songId = song.id,
                    fileName = song.fileName,
                    reason = "This audio format cannot be tagged safely.",
                    cause = TagEditFailureCause.UnsupportedFormat,
                )
                return@forEach
            }
            if (coverArtBytes != null && !AudioFormatPolicy.canEmbedArtwork(detectedFormat, song.fileName)) {
                mutationId?.let {
                    mediaMutationJournal.mark(it, MediaMutationStatus.Failed, "Unsupported artwork write format")
                }
                failures += TagEditFailure(
                    songId = song.id,
                    fileName = song.fileName,
                    reason = "Artwork cannot be embedded safely in this audio format.",
                    cause = TagEditFailureCause.UnsupportedArtworkFormat,
                )
                return@forEach
            }
            if (trackEdit == null) {
                logDebug("Missing per-track edit row for songId=${song.id}; applying album-level values only.")
            }
            val effectiveTrack = EffectiveTrackEdit.from(song, trackEdit)
            var tempFile: File? = null
            var backupFile: File? = null
            var persistedVerificationFile: File? = null
            var phase = TagEditWritePhase.SourceRead
            var rollbackFailed = false
            var originalOverwritten = false
            try {
                mutationRunner.requireWritable(song.uri)
                mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.PreflightPassed) }
                val originalBackup = mutationRunner.copySongToTemp(song, "backup")
                backupFile = originalBackup
                val workingFile = mutationRunner.createTempFile(song, "working").also { file ->
                    originalBackup.copyTo(file, overwrite = true)
                }
                tempFile = workingFile
                phase = TagEditWritePhase.TempWrite
                updateTagFile(
                    tempFile = workingFile,
                    originalSong = song,
                    request = request,
                    track = effectiveTrack,
                    coverArtBytes = coverArtBytes,
                    coverArtMimeType = coverArtMimeType,
                )
                mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.TempWritten) }
                phase = TagEditWritePhase.TempVerification
                val verificationFailures = verifyWrittenTags(
                    tempFile = workingFile,
                    expected = ExpectedTagValues(
                        title = trackEdit?.let { effectiveTrack.title },
                        artist = trackEdit?.let { effectiveTrack.artist },
                        album = request.albumTitle.expectedValue(),
                        albumArtist = request.albumArtist.expectedValue(),
                        year = request.releaseYear.expectedYear(),
                        genre = request.genre.expectedValue(),
                        shouldClearAlbum = request.albumTitle is TagFieldEdit.Cleared,
                        shouldClearAlbumArtist = request.albumArtist is TagFieldEdit.Cleared,
                        shouldClearYear = request.releaseYear is TagFieldEdit.Cleared,
                        shouldClearGenre = request.genre is TagFieldEdit.Cleared,
                        trackNumber = trackEdit?.let { effectiveTrack.trackNumber.coerceAtLeast(1).toString() },
                        discNumber = trackEdit?.let { effectiveTrack.discNumber.coerceAtLeast(1).toString() },
                    ),
                    expectArtwork = coverArtBytes != null,
                )
                if (verificationFailures.isNotEmpty()) {
                    mutationId?.let {
                        mediaMutationJournal.mark(it, MediaMutationStatus.Failed, verificationFailures.joinToString())
                    }
                    failures += TagEditFailure(
                        songId = song.id,
                        fileName = song.fileName,
                        reason = verificationFailures.joinToString(),
                        cause = TagEditFailureCause.TempVerificationFailed,
                    )
                    return@forEach
                }
                mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.TempVerified) }
                phase = TagEditWritePhase.OriginalOverwrite
                try {
                    mutationRunner.overwriteOriginal(song.uri, tempFile)
                    originalOverwritten = true
                } catch (writeFailure: Throwable) {
                    rollbackFailed = runCatching {
                        mutationRunner.overwriteOriginal(song.uri, originalBackup)
                    }.isFailure
                    throw writeFailure
                }
                mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.Committed) }
                phase = TagEditWritePhase.PersistedVerification
                persistedVerificationFile = mutationRunner.copySongToTemp(song, "verify")
                val persistedFailures = verifyWrittenTags(
                    tempFile = persistedVerificationFile,
                    expected = ExpectedTagValues(
                        title = trackEdit?.let { effectiveTrack.title },
                        artist = trackEdit?.let { effectiveTrack.artist },
                        album = request.albumTitle.expectedValue(),
                        albumArtist = request.albumArtist.expectedValue(),
                        year = request.releaseYear.expectedYear(),
                        genre = request.genre.expectedValue(),
                        shouldClearAlbum = request.albumTitle is TagFieldEdit.Cleared,
                        shouldClearAlbumArtist = request.albumArtist is TagFieldEdit.Cleared,
                        shouldClearYear = request.releaseYear is TagFieldEdit.Cleared,
                        shouldClearGenre = request.genre is TagFieldEdit.Cleared,
                        trackNumber = trackEdit?.let { effectiveTrack.trackNumber.coerceAtLeast(1).toString() },
                        discNumber = trackEdit?.let { effectiveTrack.discNumber.coerceAtLeast(1).toString() },
                    ),
                    expectArtwork = coverArtBytes != null,
                )
                if (persistedFailures.isNotEmpty()) {
                    mutationId?.let {
                        mediaMutationJournal.mark(it, MediaMutationStatus.NeedsRepair, persistedFailures.joinToString())
                    }
                    rollbackFailed = runCatching {
                        mutationRunner.overwriteOriginal(song.uri, originalBackup)
                    }.isFailure
                    error("Persisted tag verification failed: ${persistedFailures.joinToString()}")
                }
                mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.PersistedVerified) }
                editedSongIds += song.id
                editedUris += song.uri
                resolveFilePath(song)?.let(editedFilePaths::add)
                editedSongs += song.copy(
                    title = trackEdit?.let { effectiveTrack.title } ?: song.title,
                    artist = trackEdit?.let { effectiveTrack.artist } ?: song.artist,
                    album = request.albumTitle.valueOr(song.album).ifBlank { song.album },
                    albumArtist = request.albumArtist.valueOr(song.albumArtist ?: song.artist)
                        .takeIf(String::isNotBlank),
                    releaseYear = request.releaseYear.valueOr(song.releaseYear),
                    genre = request.genre.valueOr(song.genre),
                    trackNumber = trackEdit?.let { effectiveTrack.trackNumber } ?: song.trackNumber,
                    discNumber = trackEdit?.let { effectiveTrack.discNumber } ?: song.discNumber,
                    metadataResolved = true,
                )
                mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.Completed) }
            } catch (throwable: CancellationException) {
                withContext(NonCancellable) {
                    if (originalOverwritten && backupFile != null) {
                        rollbackFailed = runCatching {
                            mutationRunner.overwriteOriginal(song.uri, backupFile)
                        }.isFailure
                    }
                    mutationId?.let {
                        mediaMutationJournal.mark(
                            it,
                            if (rollbackFailed) MediaMutationStatus.NeedsRepair else MediaMutationStatus.Cancelled,
                        )
                    }
                }
                throw throwable
            } catch (throwable: RecoverableSecurityException) {
                mutationId?.let { mediaMutationJournal.mark(it, MediaMutationStatus.NeedsPermission) }
                if (!writeConsentGranted && permissionRequest == null) {
                    permissionRequest = throwable.userAction.actionIntent
                }
                failures += TagEditFailure(
                    songId = song.id,
                    fileName = song.fileName,
                    reason = if (writeConsentGranted) {
                        "The system did not allow this file to be updated after write access was granted."
                    } else {
                        "Additional write access is required for this file."
                    },
                    cause = if (writeConsentGranted) {
                        TagEditFailureCause.PermissionDeniedAfterGrant
                    } else {
                        TagEditFailureCause.PermissionRequired
                    },
                )
            } catch (throwable: Throwable) {
                val failureCause = if (rollbackFailed) TagEditFailureCause.RollbackFailed else phase.cause
                val reason = if (rollbackFailed) {
                    "The song could not be restored after the tag write failed."
                } else {
                    phase.userMessage
                }
                mutationId?.let {
                    mediaMutationJournal.mark(
                        it,
                        if (rollbackFailed) MediaMutationStatus.NeedsRepair else MediaMutationStatus.Failed,
                        "${phase.name}:${throwable.javaClass.simpleName}:${throwable.message.orEmpty()}",
                    )
                }
                failures += TagEditFailure(
                    songId = song.id,
                    fileName = song.fileName,
                    reason = reason,
                    cause = failureCause,
                )
            } finally {
                runCatching { tempFile?.delete() }
                runCatching { backupFile?.delete() }
                runCatching { persistedVerificationFile?.delete() }
            }
        }
        TagEditApplyResult(
            editedSongIds = editedSongIds,
            editedUris = editedUris,
            editedFilePaths = editedFilePaths.distinct(),
            editedSongs = editedSongs,
            artworkChanged = coverArtBytes != null && editedSongIds.isNotEmpty(),
            failures = failures,
            permissionRequest = permissionRequest,
        )
    }

    private fun updateTagFile(
        tempFile: File,
        originalSong: Song,
        request: AlbumTagEditRequest,
        track: EffectiveTrackEdit,
        coverArtBytes: ByteArray?,
        coverArtMimeType: String?,
    ) {
        val audioFile = AudioFileIO.read(tempFile)
        val tag = audioFile.tagOrCreateAndSetDefault
        applyTextEdit(tag, FieldKey.ALBUM, request.albumTitle)
        applyTextEdit(tag, FieldKey.ALBUM_ARTIST, request.albumArtist)
        applyTextEdit(tag, FieldKey.GENRE, request.genre)
        if (request.tracks.any { it.songId == originalSong.id }) {
            setOrDeleteTextField(tag, FieldKey.ARTIST, track.artist.trim().ifBlank { originalSong.artist })
            setOrDeleteTextField(tag, FieldKey.TITLE, track.title.trim().ifBlank { originalSong.title })
            setOrDeleteTextField(tag, FieldKey.TRACK, track.trackNumber.coerceAtLeast(1).toString())
            setOrDeleteTextField(tag, FieldKey.DISC_NO, track.discNumber.coerceAtLeast(1).toString())
        }
        applyReleaseYear(tag, request.releaseYear)
        if (coverArtBytes != null && coverArtMimeType != null) {
            runCatching { tag.deleteArtworkField() }
            val artworkTempFile = createArtworkTempFile(
                bytes = coverArtBytes,
                mimeType = coverArtMimeType,
                songId = originalSong.id,
            )
            try {
                tag.setField(ArtworkFactory.createArtworkFromFile(artworkTempFile))
            } finally {
                runCatching { artworkTempFile.delete() }
            }
        }
        audioFile.commit()
    }

    private fun applyTextEdit(
        tag: org.jaudiotagger.tag.Tag,
        fieldKey: FieldKey,
        edit: TagFieldEdit<String>,
    ) {
        when (edit) {
            TagFieldEdit.Unchanged -> Unit
            TagFieldEdit.Cleared -> runCatching { tag.deleteField(fieldKey) }
            is TagFieldEdit.Value -> setOrDeleteTextField(tag, fieldKey, edit.value)
        }
    }

    private fun applyReleaseYear(
        tag: org.jaudiotagger.tag.Tag,
        edit: TagFieldEdit<Int>,
    ) {
        when (edit) {
            TagFieldEdit.Unchanged -> Unit
            TagFieldEdit.Cleared -> {
                runCatching { tag.deleteField(FieldKey.YEAR) }
                runCatching { tag.deleteField(FieldKey.ORIGINAL_YEAR) }
            }
            is TagFieldEdit.Value -> {
                val year = edit.value.coerceIn(MIN_RELEASE_YEAR, MAX_RELEASE_YEAR).toString()
                runCatching { tag.deleteField(FieldKey.YEAR) }
                tag.setField(tag.createField(FieldKey.YEAR, year))
                runCatching { tag.deleteField(FieldKey.ORIGINAL_YEAR) }
                runCatching { tag.setField(tag.createField(FieldKey.ORIGINAL_YEAR, year)) }
            }
        }
    }

    private fun setOrDeleteTextField(
        tag: org.jaudiotagger.tag.Tag,
        fieldKey: FieldKey,
        value: String?,
    ) {
        val normalizedValue = value?.trim().orEmpty()
        runCatching { tag.deleteField(fieldKey) }
        if (normalizedValue.isNotBlank()) {
            tag.setField(tag.createField(fieldKey, normalizedValue))
        }
    }

    private fun verifyWrittenTags(
        tempFile: File,
        expected: ExpectedTagValues,
        expectArtwork: Boolean,
    ): List<String> {
        val audioFile = AudioFileIO.read(tempFile)
        val tag = audioFile.tagOrCreateAndSetDefault
        val failures = mutableListOf<String>()

        fun check(field: FieldKey, expectedValue: String?, label: String) {
            val normalizedExpected = expectedValue.orEmpty().trim()
            if (normalizedExpected.isBlank()) return
            val actual = tag.getFirst(field).orEmpty().trim()
            if (!tagValuesMatch(field, normalizedExpected, actual)) {
                failures += "$label expected '$normalizedExpected' but was '$actual'"
            }
        }

        fun checkCleared(field: FieldKey, label: String, shouldBeCleared: Boolean) {
            if (!shouldBeCleared) return
            val actual = tag.getFirst(field).orEmpty().trim()
            if (actual.isNotEmpty()) {
                failures += "$label should be cleared but was '$actual'"
            }
        }

        check(FieldKey.TITLE, expected.title, "Title")
        check(FieldKey.ARTIST, expected.artist, "Artist")
        check(FieldKey.ALBUM, expected.album, "Album")
        check(FieldKey.ALBUM_ARTIST, expected.albumArtist, "Album artist")
        check(FieldKey.YEAR, expected.year, "Year")
        check(FieldKey.GENRE, expected.genre, "Genre")
        checkCleared(FieldKey.ALBUM, "Album", expected.shouldClearAlbum)
        checkCleared(FieldKey.ALBUM_ARTIST, "Album artist", expected.shouldClearAlbumArtist)
        checkCleared(FieldKey.YEAR, "Year", expected.shouldClearYear)
        checkCleared(FieldKey.GENRE, "Genre", expected.shouldClearGenre)
        check(FieldKey.TRACK, expected.trackNumber, "Track")
        check(FieldKey.DISC_NO, expected.discNumber, "Disc")
        if (expectArtwork && tag.firstArtwork?.binaryData?.isNotEmpty() != true) {
            failures += "Artwork was not embedded correctly"
        }
        return failures
    }

    private fun tagValuesMatch(field: FieldKey, expected: String, actual: String): Boolean {
        if (field == FieldKey.TRACK || field == FieldKey.DISC_NO) {
            return actual.substringBefore('/').trim().toIntOrNull() == expected.toIntOrNull()
        }
        if (field == FieldKey.YEAR) {
            val expectedYear = YEAR_PATTERN.find(expected)?.value
            val actualYear = YEAR_PATTERN.find(actual)?.value
            return expectedYear != null && expectedYear == actualYear
        }
        return normalizeTagText(actual) == normalizeTagText(expected)
    }

    private fun normalizeTagText(value: String): String =
        Normalizer.normalize(value.trim(), Normalizer.Form.NFC)

    private fun resolveFilePath(song: Song): String? {
        return contentResolver.queryMediaStoreFilePath(appContext, song.uri)
    }

    private fun readBytes(uri: Uri): ByteArray? {
        return runCatching { contentIo.readBytesBounded(uri, MAX_SELECTED_ARTWORK_BYTES) }.getOrNull()
    }

    private fun detectMimeType(bytes: ByteArray): String {
        return when {
            bytes.size >= 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() -> "image/png"
            bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() -> "image/jpeg"
            bytes.size >= 12 && bytes.copyOfRange(0, 4).decodeToString() == "RIFF" && bytes.copyOfRange(8, 12).decodeToString() == "WEBP" -> "image/webp"
            else -> "image/jpeg"
        }
    }

    private fun createArtworkTempFile(
        bytes: ByteArray,
        mimeType: String,
        songId: Long,
    ): File {
        val tempDir = File(appContext.cacheDir, TEMP_TAG_EDIT_DIR_NAME).apply { mkdirs() }
        val extension = when (mimeType.lowercase(Locale.US)) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val artworkFile = File(tempDir, "cover-$songId-${System.nanoTime()}.$extension")
        artworkFile.writeBytes(bytes)
        return artworkFile
    }

    private data class EffectiveTrackEdit(
        val title: String,
        val artist: String,
        val trackNumber: Int,
        val discNumber: Int,
    ) {
        companion object {
            fun from(
                song: Song,
                track: EditableAlbumTrack?,
            ): EffectiveTrackEdit {
                return EffectiveTrackEdit(
                    title = track?.title?.trim().orEmpty().ifBlank { song.title },
                    artist = track?.artist?.trim().orEmpty().ifBlank { song.artist },
                    trackNumber = track?.trackNumber?.takeIf { it > 0 } ?: song.trackNumber.coerceAtLeast(1),
                    discNumber = track?.discNumber?.takeIf { it > 0 } ?: song.discNumber.coerceAtLeast(1),
                )
            }
        }
    }

    private data class ExpectedTagValues(
        val title: String?,
        val artist: String?,
        val album: String?,
        val albumArtist: String?,
        val year: String?,
        val genre: String?,
        val shouldClearAlbum: Boolean,
        val shouldClearAlbumArtist: Boolean,
        val shouldClearYear: Boolean,
        val shouldClearGenre: Boolean,
        val trackNumber: String?,
        val discNumber: String?,
    )

    private fun TagFieldEdit<String>.expectedValue(): String? = (this as? TagFieldEdit.Value)?.value

    private fun TagFieldEdit<Int>.expectedYear(): String? =
        (this as? TagFieldEdit.Value)?.value?.coerceIn(MIN_RELEASE_YEAR, MAX_RELEASE_YEAR)?.toString()

    private fun TagFieldEdit<String>.valueOr(fallback: String): String = when (this) {
        TagFieldEdit.Unchanged -> fallback
        TagFieldEdit.Cleared -> ""
        is TagFieldEdit.Value -> value.trim()
    }

    private fun TagFieldEdit<Int>.valueOr(fallback: Int?): Int? = when (this) {
        TagFieldEdit.Unchanged -> fallback
        TagFieldEdit.Cleared -> null
        is TagFieldEdit.Value -> value.coerceIn(MIN_RELEASE_YEAR, MAX_RELEASE_YEAR)
    }

    private companion object {
        val YEAR_PATTERN = Regex("""\b\d{4}\b""")
        const val TEMP_TAG_EDIT_DIR_NAME = "album-tag-edits"
        const val TAG = "AlbumTagEditor"
        const val MIN_RELEASE_YEAR = 1
        const val MAX_RELEASE_YEAR = 9999
        const val MAX_SELECTED_ARTWORK_BYTES = 16 * 1024 * 1024
    }

    private fun logDebug(message: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, message)
    }
}

private enum class TagEditWritePhase(
    val cause: TagEditFailureCause,
    val userMessage: String,
) {
    SourceRead(TagEditFailureCause.CannotOpenInput, "Unable to read this song for tag editing."),
    TempWrite(TagEditFailureCause.TempWriteFailed, "Unable to write tags safely."),
    TempVerification(TagEditFailureCause.TempVerificationFailed, "Changed tags could not be verified before saving."),
    OriginalOverwrite(TagEditFailureCause.CannotOpenOutput, "Unable to save tags to the song file."),
    PersistedVerification(
        TagEditFailureCause.PersistedVerificationFailed,
        "Changed tags could not be verified after saving.",
    ),
}
