package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.net.Uri
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.AudioQualityFormatter
import elovaire.music.droidbeauty.app.data.audio.DetectedAudioFormat
import elovaire.music.droidbeauty.app.data.audio.MetadataSourceValues
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal data class MediaStoreProcessedSong(
    val song: Song,
    val identityKey: String,
)

private fun MediaStoreAudioRow.mediaKind() = AudioMediaKindClassifier.classify(
    isAudiobook = isAudiobook,
    extension = extension,
    relativePath = relativePath,
    absolutePath = filePath,
)

/** Converts an accepted MediaStore row into one library song. */
internal class MediaStoreRowProcessor(
    private val context: Context,
    private val metadataCache: ScannerMetadataCache,
    private val audioFormatDetector: AudioFormatDetector,
    private val localMetadataReader: LocalAudioMetadataReader,
    private val audioFileFilter: LibraryAudioFileFilter,
    private val enrichMetadata: Boolean,
    private val genreCache: MutableMap<MediaStoreGenreKey, String?>,
    private val decisionMap: ScannerDebugLogger.ScannerDecisionMap,
) {
    private val albumArtworkUris = HashMap<String?, HashMap<Long, Uri>>()

    @Suppress("LongMethod")
    suspend fun process(row: MediaStoreAudioRow): MediaStoreProcessedSong? {
        val preflightCandidate = AudioScanCandidateMapper.toCandidate(row, detectedFormat = null)
        if (preflightCandidate.relativePath.isNullOrBlank() &&
            preflightCandidate.absolutePath.isNullOrBlank()
        ) {
            decisionMap.recordFolderMetadataUnavailable()
        }
        decisionMap.recordMediaStoreRow(preflightCandidate, row.durationMs)
        val preflightRejection = MediaStoreScanPreflight
            .rejectionBeforeContainerDetection(preflightCandidate, audioFileFilter)
        if (preflightRejection != null) {
            decisionMap.recordMediaStoreExclude(preflightRejection.reason)
            return null
        }
        decisionMap.recordPreflightPassed()

        val effectiveDurationMs = effectiveDuration(row)
        val mediaStoreIdentityKey = MediaIdentityResolver.mediaStore(row.volumeName, row.id)?.stableKey
        val uriKey = mediaStoreIdentityKey ?: row.uri.toString()
        val revisionKey = sourceRevisionKey(row)
        val cachedMetadata = metadataCache[uriKey]
            ?.takeIf { cached ->
                cached.matches(
                    fileName = row.fileName,
                    filePath = row.filePath,
                    dateAddedSeconds = row.dateAddedSeconds,
                    dateModifiedSeconds = row.dateModifiedSeconds,
                    fileSizeBytes = row.fileSizeBytes,
                    durationMs = effectiveDurationMs,
                    requireEnriched = enrichMetadata,
                )
            }
        val detectedFormat = detectFormat(row, revisionKey, mediaStoreIdentityKey)
        val candidate = AudioScanCandidateMapper
            .toCandidate(row, detectedFormat)
            .copy(durationMs = effectiveDurationMs)
        when (val decision = audioFileFilter.evaluate(candidate)) {
            AudioFileFilterDecision.Include -> {
                decisionMap.recordMediaStoreInclude()
                ScannerDebugLogger.logPlatformDependentCandidate(candidate)
            }
            is AudioFileFilterDecision.Exclude -> {
                decisionMap.recordMediaStoreExclude(decision.reason)
                return null
            }
        }

        val songMetadata = cachedMetadata
            ?.metadata
            ?: if (enrichMetadata) {
                ElovaireTrace.section("mediastore_metadata_enrichment") {
                    readSongMetadata(
                        row = row,
                        durationMs = effectiveDurationMs,
                        detectedFormat = detectedFormat,
                        identityKey = uriKey,
                        revisionKey = revisionKey,
                    )
                }
            } else {
                SongMetadata(
                    title = row.title,
                    artist = row.artist,
                    albumArtist = null,
                    album = row.album,
                    description = null,
                    releaseYear = row.mediaStoreYear,
                    genre = null,
                    format = detectedFormat.displayName,
                    quality = null,
                    trackNumber = null,
                    discNumber = null,
                    volumeNormalization = null,
                )
            }
        val resolvedTitle = songMetadata.title ?: row.title
        val resolvedArtist = songMetadata.artist ?: row.artist
        val resolvedAlbum = songMetadata.album ?: row.album
        val isExplicit = detectExplicit(resolvedTitle, row.fileName)
        val title = sanitizeDisplayTitle(resolvedTitle, isExplicit)
        val storedMetadata = metadataCache.put(
            uriKey,
            CachedSongMetadata(
                songId = row.id,
                fileName = row.fileName,
                filePath = row.filePath,
                dateAddedSeconds = row.dateAddedSeconds,
                dateModifiedSeconds = row.dateModifiedSeconds,
                isEnriched = enrichMetadata || cachedMetadata?.isEnriched == true,
                metadata = songMetadata,
                fileSizeBytes = row.fileSizeBytes,
                durationMs = effectiveDurationMs,
            ),
        )
        val rawTrack = row.track
        return MediaStoreProcessedSong(
            song = Song(
                id = row.id,
                title = title,
                isExplicit = isExplicit,
                artist = resolvedArtist,
                album = resolvedAlbum,
                description = songMetadata.description,
                releaseYear = songMetadata.releaseYear,
                genre = songMetadata.genre.orUnknown("Unknown Genre"),
                audioFormat = songMetadata.format,
                audioQuality = storedMetadata.metadata.quality,
                fileName = row.fileName,
                albumId = row.albumId,
                durationMs = effectiveDurationMs,
                trackNumber = songMetadata.trackNumber ?: normalizeTrackNumber(rawTrack),
                discNumber = songMetadata.discNumber ?: normalizeDiscNumber(rawTrack),
                dateAddedSeconds = row.dateAddedSeconds,
                dateModifiedSeconds = row.dateModifiedSeconds,
                libraryPath = row.filePath,
                uri = row.uri,
                artUri = if (row.albumId >= 0L) {
                    albumArtworkUris
                        .getOrPut(row.volumeName) { HashMap() }
                        .getOrPut(row.albumId) { mediaStoreAlbumArtworkUri(row.volumeName, row.albumId)!! }
                } else {
                    null
                },
                metadataResolved = enrichMetadata || cachedMetadata?.isEnriched == true,
                albumArtist = songMetadata.albumArtist,
                volumeNormalization = songMetadata.volumeNormalization,
                mediaKind = row.mediaKind(),
                bookmarkMs = row.bookmarkMs,
            ),
            identityKey = uriKey,
        )
    }

    private suspend fun effectiveDuration(row: MediaStoreAudioRow): Long {
        if (row.durationMs > 0L) return row.durationMs
        return if (
            row.extension.isBlank() ||
            row.extension in AudioFormatPolicy.scannerExtensions ||
            AudioFormatPolicy.capabilityForMimeType(row.mimeType) != null
        ) {
            localMetadataReader.readDuration(row.uri)
        } else {
            0L
        }
    }

    private fun detectFormat(
        row: MediaStoreAudioRow,
        revisionKey: String?,
        identityKey: String?,
    ): DetectedAudioFormat {
        return if (
            row.extension.isBlank() ||
            row.extension !in AudioFormatPolicy.scannerExtensions ||
            AudioFormatPolicy.shouldDetectContainer(row.extension, enrichMetadata)
        ) {
            audioFormatDetector.detect(
                uri = row.uri,
                fileName = row.fileName,
                mediaStoreMimeType = row.mimeType,
                revisionKey = revisionKey,
                identityKey = identityKey,
            )
        } else {
            AudioScanCandidateMapper.fastDetectedFormat(
                extension = row.extension,
                mimeType = row.mimeType,
            )
        }
    }

    private suspend fun readSongMetadata(
        row: MediaStoreAudioRow,
        durationMs: Long,
        detectedFormat: DetectedAudioFormat,
        identityKey: String,
        revisionKey: String?,
    ): SongMetadata {
        val metadata = localMetadataReader.read(
            uri = row.uri,
            filePath = row.filePath,
            fileName = row.fileName,
            indexed = MetadataSourceValues(
                title = row.title,
                artist = row.artist,
                album = row.album,
                releaseYear = row.mediaStoreYear,
            ),
            identityKey = identityKey,
            revisionKey = revisionKey,
        )
        val resolvedGenre = metadata.genre ?: run {
            val genreKey = MediaStoreGenreKey(row.id, row.volumeName)
            if (!genreCache.containsKey(genreKey)) {
                decisionMap.recordMediaStoreGenreLookup()
                genreCache[genreKey] = queryGenre(row.id, row.volumeName)
            }
            genreCache[genreKey]
        }
        val resolvedFormat = detectedFormat.displayName
        val sampleRate = metadata.sampleRate ?: detectedFormat.sampleRate
        val bitrate = metadata.bitrate
            ?: detectedFormat.bitrate
            ?: estimateBitrateBitsPerSecond(
                fileSizeBytes = row.fileSizeBytes,
                durationMs = durationMs,
                resolvedFormat = resolvedFormat,
            )
        return SongMetadata(
            title = metadata.title,
            artist = metadata.artist,
            albumArtist = metadata.albumArtist,
            album = metadata.album,
            description = metadata.description,
            releaseYear = metadata.releaseYear,
            genre = resolvedGenre,
            format = resolvedFormat,
            quality = AudioQualityFormatter.format(
                container = detectedFormat.container,
                bitDepth = metadata.bitDepth,
                sampleRate = sampleRate,
                bitrate = bitrate,
                codecMimeType = detectedFormat.codecMimeType,
            ),
            trackNumber = metadata.trackNumber,
            discNumber = metadata.discNumber,
            volumeNormalization = metadata.volumeNormalization,
        )
    }

    private suspend fun queryGenre(songId: Long, volumeName: String?): String? {
        if (!canQueryMediaStoreGenre(songId)) return null
        for (volume in mediaStoreGenreVolumes(volumeName)) {
            currentCoroutineContext().ensureActive()
            val genreUri = android.provider.MediaStore.Audio.Genres
                .getContentUriForAudioId(volume, songId.toInt())
            val genre = try {
                context.contentResolver.queryCancellable(
                    genreUri,
                    arrayOf(android.provider.MediaStore.Audio.Genres.NAME),
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Genres.NAME)
                    var resolved: String? = null
                    while (cursor.moveToNext()) {
                        val candidate = cursor.getString(nameIndex).trim()
                        if (candidate.isNotBlank()) {
                            resolved = candidate
                            break
                        }
                    }
                    resolved
                }
            } catch (failure: CancellationException) {
                throw failure
            } catch (_: RuntimeException) {
                null
            }
            if (genre != null) return genre
        }
        return null
    }

    private fun sourceRevisionKey(row: MediaStoreAudioRow): String? {
        return if (row.dateModifiedSeconds != null || row.fileSizeBytes != null) {
            MediaIdentityResolver.sourceRevisionKey(
                modifiedAtMs = row.dateModifiedSeconds?.times(1_000L),
                sizeBytes = row.fileSizeBytes,
            )
        } else {
            null
        }
    }

    private fun String?.orUnknown(fallback: String): String {
        val value = this?.trim().orEmpty()
        return if (value.isBlank() || value == "<unknown>") fallback else value
    }

    private fun normalizeTrackNumber(rawTrack: Int): Int {
        if (rawTrack <= 0) return 0
        return rawTrack % 1000
    }

    private fun normalizeDiscNumber(rawTrack: Int): Int {
        if (rawTrack <= 0) return 1
        return (rawTrack / 1000).coerceAtLeast(1)
    }

    private fun detectExplicit(title: String, fileName: String): Boolean {
        return EXPLICIT_MARKERS.any { marker ->
            title.contains(marker, ignoreCase = true) || fileName.contains(marker, ignoreCase = true)
        } || EXPLICIT_ADVISORY_SUFFIX.containsMatchIn(title)
    }

    private fun sanitizeDisplayTitle(title: String, isExplicit: Boolean): String {
        if (!isExplicit) return title
        return title
            .replace(EXPLICIT_ADVISORY_SUFFIX, "")
            .replace(TRAILING_REPLACEMENT_MARKERS, "")
            .trim()
            .ifBlank { title }
    }

    private fun estimateBitrateBitsPerSecond(
        fileSizeBytes: Long?,
        durationMs: Long,
        resolvedFormat: String,
    ): Int? {
        if (fileSizeBytes == null || fileSizeBytes <= 0L || durationMs <= 0L) return null
        if (resolvedFormat.uppercase(Locale.ROOT) in NON_BITRATE_ESTIMATED_FORMATS) return null
        val seconds = durationMs / 1000.0
        if (seconds <= 0.0) return null
        return ((fileSizeBytes * 8.0) / seconds).toInt().takeIf { it > 0 }
    }

    private companion object {
        val EXPLICIT_MARKERS = listOf(
            "(explicit)",
            "[explicit]",
            " - explicit",
            " explicit version",
        )
        val EXPLICIT_ADVISORY_SUFFIX = Regex(
            pattern = """(?:\s|^)(?:[\[(]\s*explicit\s*[\])]|🅴|[\uFFFD?]{3,})\s*$""",
            option = RegexOption.IGNORE_CASE,
        )
        val TRAILING_REPLACEMENT_MARKERS = Regex("""\s*[\uFFFD?]{3,}\s*$""")
        val NON_BITRATE_ESTIMATED_FORMATS = setOf("WAV", "FLAC")
    }
}

internal inline fun resolveMediaStoreGenre(
    metadataGenre: String?,
    fallbackGenre: () -> String?,
): String? = metadataGenre ?: fallbackGenre()
