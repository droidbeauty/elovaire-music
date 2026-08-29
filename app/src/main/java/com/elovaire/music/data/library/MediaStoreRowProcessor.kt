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

internal data class MediaStoreProcessedSong(
    val song: Song,
    val identityKey: String,
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
    fun process(row: MediaStoreAudioRow): MediaStoreProcessedSong? {
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
        val uriKey = MediaIdentityResolver.mediaStore(row.volumeName, row.id)
            ?.stableKey
            ?: row.uri.toString()
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
        val detectedFormat = detectFormat(row)
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
                    )
                }
            } else {
                SongMetadata(
                    title = row.title,
                    artist = row.artist,
                    albumArtist = null,
                    album = row.album,
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
        metadataCache.put(
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
                releaseYear = songMetadata.releaseYear,
                genre = songMetadata.genre.orUnknown("Unknown Genre"),
                audioFormat = songMetadata.format,
                audioQuality = songMetadata.quality,
                fileName = row.fileName,
                albumId = row.albumId,
                durationMs = effectiveDurationMs,
                trackNumber = songMetadata.trackNumber ?: normalizeTrackNumber(rawTrack),
                discNumber = songMetadata.discNumber ?: normalizeDiscNumber(rawTrack),
                dateAddedSeconds = row.dateAddedSeconds,
                dateModifiedSeconds = row.dateModifiedSeconds,
                libraryPath = row.filePath,
                uri = row.uri,
                artUri = mediaStoreAlbumArtworkUri(row.volumeName, row.albumId),
                metadataResolved = enrichMetadata || cachedMetadata?.isEnriched == true,
                albumArtist = songMetadata.albumArtist,
                volumeNormalization = songMetadata.volumeNormalization,
            ),
            identityKey = uriKey,
        )
    }

    private fun effectiveDuration(row: MediaStoreAudioRow): Long {
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

    private fun detectFormat(row: MediaStoreAudioRow): DetectedAudioFormat {
        return if (
            row.extension.isBlank() ||
            row.extension !in AudioFormatPolicy.scannerExtensions ||
            AudioFormatPolicy.shouldDetectContainer(row.extension, enrichMetadata)
        ) {
            audioFormatDetector.detect(
                uri = row.uri,
                fileName = row.fileName,
                mediaStoreMimeType = row.mimeType,
                revisionKey = sourceRevisionKey(row),
                identityKey = MediaIdentityResolver.mediaStore(row.volumeName, row.id)?.stableKey,
            )
        } else {
            AudioScanCandidateMapper.fastDetectedFormat(
                extension = row.extension,
                mimeType = row.mimeType,
            )
        }
    }

    private fun readSongMetadata(
        row: MediaStoreAudioRow,
        durationMs: Long,
        detectedFormat: DetectedAudioFormat,
        identityKey: String,
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
            revisionKey = sourceRevisionKey(row),
        )
        val resolvedGenre = resolveMediaStoreGenre(metadata.genre) {
            genreCache.getOrPut(MediaStoreGenreKey(row.id, row.volumeName)) {
                decisionMap.recordMediaStoreGenreLookup()
                queryGenre(row.id, row.volumeName)
            }
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

    private fun queryGenre(songId: Long, volumeName: String?): String? {
        if (!canQueryMediaStoreGenre(songId)) return null
        return mediaStoreGenreVolumes(volumeName).firstNotNullOfOrNull { volume ->
            val genreUri = android.provider.MediaStore.Audio.Genres
                .getContentUriForAudioId(volume, songId.toInt())
            runCatching {
                context.contentResolver.query(
                    genreUri,
                    arrayOf(android.provider.MediaStore.Audio.Genres.NAME),
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Genres.NAME)
                    generateSequence {
                        if (cursor.moveToNext()) cursor.getString(nameIndex) else null
                    }.map(String::trim).firstOrNull { it.isNotBlank() }
                }
            }.getOrNull()
        }
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
        val normalizedTitle = title.lowercase(Locale.ROOT)
        val normalizedFileName = fileName.lowercase(Locale.ROOT)
        return EXPLICIT_MARKERS.any { marker ->
            normalizedTitle.contains(marker) || normalizedFileName.contains(marker)
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
