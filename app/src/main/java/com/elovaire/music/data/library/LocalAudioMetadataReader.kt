package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import elovaire.music.droidbeauty.app.data.audio.CanonicalMetadataResolver
import elovaire.music.droidbeauty.app.data.audio.EmbeddedTagMetadataReader
import elovaire.music.droidbeauty.app.data.audio.MetadataSourceValues
import elovaire.music.droidbeauty.app.data.audio.MediaMetadataRetrieverAdmission
import elovaire.music.droidbeauty.app.data.audio.toMetadataSourceValues
import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationMetadata
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.core.backend.BackendResourceTracker
import kotlinx.coroutines.CancellationException
import java.io.File

/** Reads local-file metadata once, then applies the shared source precedence rules. */
internal class LocalAudioMetadataReader(
    context: Context,
    private val failureRegistry: MediaFailureRegistry = MediaFailureRegistry(),
    private val resourceTracker: BackendResourceTracker = BackendResourceRegistry,
    private val retrieverAdmission: MediaMetadataRetrieverAdmission = MediaMetadataRetrieverAdmission(),
) {
    private val appContext = context.applicationContext
    private val embeddedReader = EmbeddedTagMetadataReader(appContext)

    suspend fun read(
        uri: Uri,
        filePath: String?,
        fileName: String,
        indexed: MetadataSourceValues? = null,
        fallbackGenre: String? = null,
        identityKey: String? = null,
        revisionKey: String? = null,
    ): LocalAudioMetadata {
        val metadataResource = resourceTracker.acquire(BackendResourceKind.ActiveMetadataRead)
        try {
            val platformFailureKey = revisionKey?.let {
                MediaFailureKey(
                    identity = identityKey ?: uri.toString(),
                    revision = it,
                    domain = MediaFailureDomain.Metadata,
                )
            }
            val platform = readPlatformMetadata(uri, filePath, platformFailureKey)
            val embedded = embeddedReader.read(uri, filePath, fileName)
            val canonical = CanonicalMetadataResolver.resolve(
                embedded = embedded?.toMetadataSourceValues(),
                platform = platform.toMetadataSourceValues(),
                indexed = indexed,
            )
            return LocalAudioMetadata(
                durationMs = platform.durationMs,
                title = canonical.title,
                artist = canonical.artist,
                albumArtist = canonical.albumArtist,
                album = canonical.album,
                description = canonical.description,
                releaseYear = canonical.releaseYear,
                genre = canonical.genre ?: fallbackGenre,
                trackNumber = canonical.trackNumber,
                discNumber = canonical.discNumber,
                sampleRate = platform.sampleRate,
                bitDepth = platform.bitDepth,
                bitrate = platform.bitrate,
                volumeNormalization = canonical.volumeNormalization,
            )
        } finally {
            metadataResource.close()
        }
    }

    /** Resolve only duration for provider rows whose indexed duration is missing or stale. */
    suspend fun readDuration(uri: Uri): Long {
        val metadataResource = resourceTracker.acquire(BackendResourceKind.ActiveMetadataRead)
        return try {
            retrieverAdmission.withPermit {
                val retriever = MediaMetadataRetriever()
                val resource = resourceTracker.acquire(BackendResourceKind.ActiveRetriever)
                try {
                    retriever.setDataSource(appContext, uri)
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull()
                        ?.takeIf { it > 0L }
                        ?: 0L
                } finally {
                    runCatching { retriever.release() }
                    resource.close()
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            0L
        } finally {
            metadataResource.close()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun readPlatformMetadata(
        uri: Uri,
        filePath: String?,
        failureKey: MediaFailureKey?,
    ): PlatformAudioMetadata {
        if (failureKey != null && failureRegistry.shouldSuppress(failureKey)) return PlatformAudioMetadata()
        return try {
            retrieverAdmission.withPermit {
                val retriever = MediaMetadataRetriever()
                val resource = resourceTracker.acquire(BackendResourceKind.ActiveRetriever)
                try {
                    val localFile = filePath
                        ?.let(::File)
                        ?.takeIf { it.isFile && it.canRead() }
                    if (localFile != null) {
                        retriever.setDataSource(localFile.absolutePath)
                    } else {
                        retriever.setDataSource(appContext, uri)
                    }
                    PlatformAudioMetadata(
                    durationMs = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull()
                        ?.takeIf { it > 0L },
                    title = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                    artist = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                    albumArtist = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                    album = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                    releaseYear = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                        ?.take(4)
                        ?.toIntOrNull()
                        ?: retriever.metadata(MediaMetadataRetriever.METADATA_KEY_DATE)
                            ?.let(::parseYear),
                    genre = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                        ?.substringBefore(';')
                        ?.substringBefore('/')
                        ?.trim()
                        ?.takeIf(String::isNotBlank),
                    trackNumber = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                        ?.let(::parsePositiveNumber),
                    discNumber = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                        ?.let(::parsePositiveNumber),
                    sampleRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        retriever.metadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
                            ?.toIntOrNull()
                    } else {
                        null
                    },
                    bitDepth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        retriever.metadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)
                            ?.toIntOrNull()
                    } else {
                        null
                    },
                    bitrate = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                        ?.toIntOrNull(),
                    ).also { failureKey?.let(failureRegistry::recordSuccess) }
                } finally {
                    runCatching { retriever.release() }
                    resource.close()
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            failureKey?.let { failureRegistry.recordFailure(it, mediaFailureCategory(failure)) }
            PlatformAudioMetadata()
        }
    }

    private fun MediaMetadataRetriever.metadata(key: Int): String? {
        return extractMetadata(key)?.trim()?.takeIf(String::isNotBlank)
    }

    private fun parsePositiveNumber(value: String): Int? {
        return value.substringBefore('/').trim().toIntOrNull()?.takeIf { it > 0 }
    }

    private fun parseYear(value: String): Int? {
        return YEAR_REGEX.find(value)?.value?.toIntOrNull()?.takeIf { it in 1..9_999 }
    }

    private companion object {
        val YEAR_REGEX = Regex("\\b\\d{1,4}\\b")
    }
}

internal data class LocalAudioMetadata(
    val durationMs: Long? = null,
    val title: String? = null,
    val artist: String? = null,
    val albumArtist: String? = null,
    val album: String? = null,
    val description: String? = null,
    val releaseYear: Int? = null,
    val genre: String? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val sampleRate: Int? = null,
    val bitDepth: Int? = null,
    val bitrate: Int? = null,
    val volumeNormalization: VolumeNormalizationMetadata? = null,
)

private typealias PlatformAudioMetadata = LocalAudioMetadata

private fun LocalAudioMetadata.toMetadataSourceValues(): MetadataSourceValues {
    return MetadataSourceValues(
        title = title,
        artist = artist,
        albumArtist = albumArtist,
        album = album,
        description = description,
        releaseYear = releaseYear,
        genre = genre,
        trackNumber = trackNumber?.toString(),
        discNumber = discNumber?.toString(),
        volumeNormalization = volumeNormalization,
    )
}
