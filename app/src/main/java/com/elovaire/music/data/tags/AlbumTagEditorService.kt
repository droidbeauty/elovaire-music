package elovaire.music.droidbeauty.app.data.tags

import android.content.ContentResolver
import android.content.Context
import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.net.Uri
import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.queryMediaStoreFilePath
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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
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

internal data class AlbumTagEditRequest(
    val album: Album,
    val albumTitle: String,
    val albumArtist: String,
    val releaseYear: Int?,
    val shouldClearYear: Boolean = false,
    val coverArtUri: Uri?,
    val coverArtBytes: ByteArray? = null,
    val tracks: List<EditableAlbumTrack>,
)

internal data class AlbumTagMatchSuggestion(
    val albumTitle: String,
    val albumArtist: String,
    val releaseYear: Int?,
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
    val artworkChanged: Boolean,
    val failures: List<TagEditFailure> = emptyList(),
    val permissionRequest: PendingIntent? = null,
)

internal data class TagEditFailure(
    val songId: Long,
    val fileName: String,
    val reason: String,
)

internal class AlbumTagEditorService(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val contentResolver: ContentResolver = appContext.contentResolver
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

    suspend fun findBestOnlineMatch(album: Album): OnlineTagMatchOutcome = withContext(Dispatchers.IO) {
        when (val result = albumMatcher.matchAlbum(album)) {
            is AlbumTagMatchResult.Success -> {
                val suggestion = AlbumTagMatchSuggestion(
                    albumTitle = result.match.release.title.ifBlank { album.title },
                    albumArtist = result.match.release.albumArtist.ifBlank { album.artist },
                    releaseYear = result.match.release.releaseYear,
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

    suspend fun applyEdits(request: AlbumTagEditRequest): TagEditApplyResult = withContext(Dispatchers.IO) {
        val trackEditsById = request.tracks.associateBy { it.songId }
        val coverArtBytes = request.coverArtBytes ?: request.coverArtUri?.let(::readBytes)
        val coverArtMimeType = coverArtBytes?.let(::detectMimeType)
        val editedSongIds = mutableListOf<Long>()
        val editedUris = mutableListOf<Uri>()
        val editedFilePaths = mutableListOf<String>()
        val failures = mutableListOf<TagEditFailure>()
        var permissionRequest: PendingIntent? = null
        request.album.songs.forEach { song ->
            val extension = song.fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
            if (extension !in WRITABLE_TAG_EXTENSIONS) {
                failures += TagEditFailure(
                    songId = song.id,
                    fileName = song.fileName,
                    reason = "This audio format cannot be tagged safely.",
                )
                return@forEach
            }
            val trackEdit = trackEditsById[song.id]
            if (trackEdit == null) {
                logDebug("Missing per-track edit row for songId=${song.id}; applying album-level values only.")
            }
            val effectiveTrack = EffectiveTrackEdit.from(song, trackEdit)
            var tempFile: File? = null
            var backupFile: File? = null
            var persistedVerificationFile: File? = null
            try {
                val originalBackup = copySongToTempFile(song, "backup")
                backupFile = originalBackup
                val workingFile = createTagEditTempFile(song, "working").also { file ->
                    originalBackup.copyTo(file, overwrite = true)
                }
                tempFile = workingFile
                updateTagFile(
                    tempFile = workingFile,
                    originalSong = song,
                    request = request,
                    track = effectiveTrack,
                    coverArtBytes = coverArtBytes,
                    coverArtMimeType = coverArtMimeType,
                )
                val verificationFailures = verifyWrittenTags(
                    tempFile = workingFile,
                    expected = ExpectedTagValues(
                        title = effectiveTrack.title,
                        artist = effectiveTrack.artist,
                        album = request.albumTitle.trim().ifBlank { song.album },
                        albumArtist = request.albumArtist.trim().ifBlank { song.artist },
                        year = request.releaseYear?.takeIf { it > 0 }?.toString(),
                        shouldClearYear = request.shouldClearYear,
                        trackNumber = effectiveTrack.trackNumber.coerceAtLeast(1).toString(),
                        discNumber = effectiveTrack.discNumber.coerceAtLeast(1).toString(),
                    ),
                    expectArtwork = coverArtBytes != null,
                )
                if (verificationFailures.isNotEmpty()) {
                    failures += TagEditFailure(
                        songId = song.id,
                        fileName = song.fileName,
                        reason = verificationFailures.joinToString(),
                    )
                    return@forEach
                }
                try {
                    overwriteSongFromTemp(song.uri, tempFile)
                } catch (writeFailure: Throwable) {
                    runCatching { overwriteSongFromTemp(song.uri, originalBackup) }
                    throw writeFailure
                }
                persistedVerificationFile = copySongToTempFile(song, "verify")
                val persistedFailures = verifyWrittenTags(
                    tempFile = persistedVerificationFile,
                    expected = ExpectedTagValues(
                        title = effectiveTrack.title,
                        artist = effectiveTrack.artist,
                        album = request.albumTitle.trim().ifBlank { song.album },
                        albumArtist = request.albumArtist.trim().ifBlank { song.artist },
                        year = request.releaseYear?.takeIf { it > 0 }?.toString(),
                        shouldClearYear = request.shouldClearYear,
                        trackNumber = effectiveTrack.trackNumber.coerceAtLeast(1).toString(),
                        discNumber = effectiveTrack.discNumber.coerceAtLeast(1).toString(),
                    ),
                    expectArtwork = coverArtBytes != null,
                )
                if (persistedFailures.isNotEmpty()) {
                    overwriteSongFromTemp(song.uri, originalBackup)
                    error("Persisted tag verification failed: ${persistedFailures.joinToString()}")
                }
                editedSongIds += song.id
                editedUris += song.uri
                resolveFilePath(song)?.let(editedFilePaths::add)
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: RecoverableSecurityException) {
                if (permissionRequest == null) {
                    permissionRequest = throwable.userAction.actionIntent
                }
                failures += TagEditFailure(
                    songId = song.id,
                    fileName = song.fileName,
                    reason = "Additional write access is required for this file.",
                )
            } catch (throwable: Throwable) {
                failures += TagEditFailure(
                    songId = song.id,
                    fileName = song.fileName,
                    reason = throwable.message ?: "Unable to save tags.",
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
        val normalizedAlbumTitle = request.albumTitle.trim().ifBlank { originalSong.album }
        val normalizedAlbumArtist = request.albumArtist.trim().ifBlank { originalSong.artist }
        val normalizedTrackArtist = track.artist.trim().ifBlank { originalSong.artist }
        setOrDeleteTextField(tag, FieldKey.ALBUM, normalizedAlbumTitle)
        setOrDeleteTextField(tag, FieldKey.ALBUM_ARTIST, normalizedAlbumArtist)
        setOrDeleteTextField(tag, FieldKey.ARTIST, normalizedTrackArtist)
        setOrDeleteTextField(tag, FieldKey.TITLE, track.title.trim().ifBlank { originalSong.title })
        setOrDeleteTextField(tag, FieldKey.TRACK, track.trackNumber.coerceAtLeast(1).toString())
        setOrDeleteTextField(tag, FieldKey.DISC_NO, track.discNumber.coerceAtLeast(1).toString())
        if (request.releaseYear != null && request.releaseYear > 0) {
            setOrDeleteTextField(tag, FieldKey.YEAR, request.releaseYear.toString())
        } else if (request.shouldClearYear) {
            tag.deleteField(FieldKey.YEAR)
        }
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

    private fun setOrDeleteTextField(
        tag: org.jaudiotagger.tag.Tag,
        fieldKey: FieldKey,
        value: String?,
    ) {
        val normalizedValue = value?.trim().orEmpty()
        if (normalizedValue.isBlank()) {
            runCatching { tag.deleteField(fieldKey) }
        } else {
            tag.setField(fieldKey, normalizedValue)
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
            if (actual != normalizedExpected) {
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
        checkCleared(FieldKey.YEAR, "Year", expected.shouldClearYear)
        check(FieldKey.TRACK, expected.trackNumber, "Track")
        check(FieldKey.DISC_NO, expected.discNumber, "Disc")
        if (expectArtwork && tag.firstArtwork?.binaryData?.isNotEmpty() != true) {
            failures += "Artwork was not embedded correctly"
        }
        return failures
    }

    private fun copySongToTempFile(song: Song, purpose: String): File {
        val tempFile = createTagEditTempFile(song, purpose)
        contentResolver.openInputStream(song.uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open ${song.fileName}")
        return tempFile
    }

    private fun createTagEditTempFile(song: Song, purpose: String): File {
        val tempDir = File(appContext.cacheDir, TEMP_TAG_EDIT_DIR_NAME).apply { mkdirs() }
        val suffix = song.fileName.substringAfterLast('.', "").ifBlank { "tmp" }
        return File(tempDir, "${song.id}-$purpose-${System.nanoTime()}.$suffix")
    }

    private fun overwriteSongFromTemp(
        songUri: Uri,
        tempFile: File,
    ) {
        val descriptor = try {
            contentResolver.openFileDescriptor(songUri, "rwt")
        } catch (_: IllegalArgumentException) {
            contentResolver.openFileDescriptor(songUri, "rw")
        } ?: error("Unable to write updated tags")
        descriptor.use {
            FileOutputStream(descriptor.fileDescriptor).channel.use { outputChannel ->
                outputChannel.position(0L)
                outputChannel.truncate(0L)
                FileInputStream(tempFile).channel.use { inputChannel ->
                    var transferred = 0L
                    val totalBytes = inputChannel.size()
                    while (transferred < totalBytes) {
                        val copiedBytes = inputChannel.transferTo(
                            transferred,
                            totalBytes - transferred,
                            outputChannel,
                        )
                        if (copiedBytes <= 0L) {
                            error("Unable to copy updated tags")
                        }
                        transferred += copiedBytes
                    }
                }
                outputChannel.force(true)
            }
        }
    }

    private fun resolveFilePath(song: Song): String? {
        return contentResolver.queryMediaStoreFilePath(appContext, song.uri)
    }

    private fun readBytes(uri: Uri): ByteArray? {
        return contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        }
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
        val title: String,
        val artist: String,
        val album: String,
        val albumArtist: String,
        val year: String?,
        val shouldClearYear: Boolean,
        val trackNumber: String,
        val discNumber: String,
    )

    private companion object {
        const val TEMP_TAG_EDIT_DIR_NAME = "album-tag-edits"
        val WRITABLE_TAG_EXTENSIONS = setOf("mp3", "m4a", "mp4", "aac", "flac", "ogg", "opus", "wav")
        const val TAG = "AlbumTagEditor"
    }

    private fun logDebug(message: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, message)
    }
}
