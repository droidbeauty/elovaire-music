package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.provider.DocumentsContract
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.data.library.network.NetworkPathPolicy
import elovaire.music.droidbeauty.app.data.library.network.NetworkResourceUri
import java.util.Locale

internal sealed interface MediaSourceIdentity {
    val stableKey: String

    data class MediaStoreItem(
        val volumeName: String,
        val mediaId: Long,
    ) : MediaSourceIdentity {
        override val stableKey: String = "mediastore:$volumeName:$mediaId"
    }

    data class SafDocument(
        val authority: String,
        val documentId: String,
        val treeId: String?,
    ) : MediaSourceIdentity {
        override val stableKey: String = "saf:$authority:$documentId"
    }

    data class DirectFile(
        val canonicalPath: String,
    ) : MediaSourceIdentity {
        override val stableKey: String = "file:$canonicalPath"
    }

    data class NetworkFile(
        val sourceId: String,
        val relativePath: String,
    ) : MediaSourceIdentity {
        override val stableKey: String = "network:$sourceId:${NetworkPathPolicy.normalizeRelativePath(relativePath)}"
    }
}

internal data class MediaRevision(
    val modifiedAtMs: Long?,
    val sizeBytes: Long?,
    val providerGeneration: Long?,
    val metadataRevision: Long,
) {
    val stableKey: String = listOf(
        modifiedAtMs.orEmptyRevisionPart(),
        sizeBytes.orEmptyRevisionPart(),
        providerGeneration.orEmptyRevisionPart(),
        metadataRevision.toString(),
    ).joinToString(":")
}

@JvmInline
internal value class LogicalTrackId(val value: Long)

/**
 * Portable identity for user-owned references. It deliberately contains no MediaStore/SAF
 * locator, because those identifiers are allowed to change when storage is rebuilt or moved.
 */
internal data class TrackMatchIdentity(
    val version: Int = TRACK_MATCH_IDENTITY_VERSION,
    val sizeBytes: Long?,
    val durationMs: Long?,
    val normalizedTitle: String,
    val normalizedArtist: String,
    val normalizedAlbum: String,
    val normalizedAlbumArtist: String?,
    val normalizedFileName: String?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val sourceStableKey: String? = null,
)

internal enum class TrackMatchConfidence {
    Exact,
    Strong,
    Probable,
    Ambiguous,
    NoMatch,
}

internal data class TrackMatchResolution(
    val confidence: TrackMatchConfidence,
    val song: Song? = null,
)

internal enum class MediaSourceAvailability {
    Available,
    Unavailable,
    Unknown,
}

internal data class MediaSource(
    val identity: MediaSourceIdentity,
    val uri: Uri,
    val revision: MediaRevision,
    val availability: MediaSourceAvailability = MediaSourceAvailability.Unknown,
)

/** A logical track keeps user-facing identity stable while its physical locator can change. */
internal data class LogicalTrack(
    val id: LogicalTrackId,
    val canonicalSong: Song,
    val preferredSource: MediaSource,
    val sources: List<MediaSource>,
)

internal object MediaIdentityResolver {
    fun mediaStore(volumeName: String?, mediaId: Long?): MediaSourceIdentity.MediaStoreItem? {
        val volume = volumeName.normalizedIdentityPart() ?: return null
        val id = mediaId?.takeIf { it >= 0L } ?: return null
        return MediaSourceIdentity.MediaStoreItem(volume, id)
    }

    fun safDocument(
        authority: String?,
        documentId: String?,
        treeId: String? = null,
    ): MediaSourceIdentity.SafDocument? {
        val normalizedAuthority = authority.normalizedIdentityPart() ?: return null
        val normalizedDocumentId = documentId.opaqueIdentityPart() ?: return null
        return MediaSourceIdentity.SafDocument(
            authority = normalizedAuthority,
            documentId = normalizedDocumentId,
            treeId = treeId.opaqueIdentityPart(),
        )
    }

    fun directFile(path: String?): MediaSourceIdentity.DirectFile? {
        val normalizedPath = LibrarySongDuplicateResolver.normalizedRealPath(path) ?: return null
        return MediaSourceIdentity.DirectFile(normalizedPath)
    }

    fun resolve(song: Song): MediaSourceIdentity? {
        if (NetworkResourceUri.isNetworkUri(song.uri)) {
            val sourceId = NetworkResourceUri.sourceId(song.uri)
            val path = NetworkResourceUri.path(song.uri)
            if (sourceId != null && path != null) return MediaSourceIdentity.NetworkFile(sourceId, path)
        }
        resolveContentUri(song.uri)?.let { return it }
        if (song.uri.scheme.equals("content", ignoreCase = true)) return null
        return directFile(song.libraryPath ?: song.uri.path)
    }

    fun logicalTrackId(song: Song): LogicalTrackId? {
        return song.id.takeIf { it != 0L }?.let(::LogicalTrackId)
    }

    fun trackMatchIdentity(song: Song, sizeBytes: Long? = null): TrackMatchIdentity {
        return TrackMatchIdentity(
            sizeBytes = sizeBytes?.takeIf { it >= 0L },
            durationMs = song.durationMs.takeIf { it > 0L },
            normalizedTitle = song.title.matchIdentityText(),
            normalizedArtist = song.artist.matchIdentityText(),
            normalizedAlbum = song.album.matchIdentityText(),
            normalizedAlbumArtist = song.albumArtist?.matchIdentityText(),
            normalizedFileName = song.fileName.matchIdentityText().takeIf(String::isNotBlank),
            trackNumber = song.trackNumber.takeIf { it > 0 },
            discNumber = song.discNumber.takeIf { it > 0 },
            sourceStableKey = stableKey(song),
        )
    }

    /** Resolves only a unique candidate; an equally good duplicate remains ambiguous. */
    fun resolveTrackMatch(
        identity: TrackMatchIdentity,
        songs: List<Song>,
    ): TrackMatchResolution {
        if (identity.version != TRACK_MATCH_IDENTITY_VERSION) {
            return TrackMatchResolution(TrackMatchConfidence.NoMatch)
        }
        identity.sourceStableKey?.let { sourceKey ->
            val exact = songs.filter { stableKey(it) == sourceKey }
            if (exact.size == 1) return TrackMatchResolution(TrackMatchConfidence.Exact, exact.single())
            if (exact.size > 1) return TrackMatchResolution(TrackMatchConfidence.Ambiguous)
        }
        val candidates = songs.mapNotNull { song ->
            trackMatchScore(identity, song)?.let { score -> song to score }
        }
        val bestScore = candidates.maxOfOrNull { it.second } ?: return TrackMatchResolution(TrackMatchConfidence.NoMatch)
        val best = candidates.filter { it.second == bestScore }
        if (best.size != 1) return TrackMatchResolution(TrackMatchConfidence.Ambiguous)
        val confidence = when {
            bestScore >= STRONG_TRACK_MATCH_SCORE -> TrackMatchConfidence.Strong
            bestScore >= PROBABLE_TRACK_MATCH_SCORE -> TrackMatchConfidence.Probable
            else -> TrackMatchConfidence.NoMatch
        }
        return TrackMatchResolution(confidence, best.single().first.takeIf { confidence != TrackMatchConfidence.NoMatch })
    }

    fun source(
        song: Song,
        sizeBytes: Long? = null,
        providerGeneration: Long? = null,
        availability: MediaSourceAvailability = MediaSourceAvailability.Unknown,
    ): MediaSource? {
        return resolve(song)?.let { identity ->
            MediaSource(
                identity = identity,
                uri = song.uri,
                revision = revision(song, sizeBytes, providerGeneration),
                availability = availability,
            )
        }
    }

    fun stableKey(song: Song): String {
        val source = resolve(song)
        return source?.stableKey
            ?: song.uri
                .takeIf { it.scheme.equals("content", ignoreCase = true) }
                ?.canonicalOpaqueIdentity()
                ?.let { "uri:$it" }
            ?: LibrarySongDuplicateResolver.normalizedRealPath(song.libraryPath)?.let { "file:$it" }
            ?: song.uri.canonicalOpaqueIdentity().let { "uri:$it" }
    }

    /** Source-local key used by scanner caches; unlike [stableKey], it never follows a path. */
    fun sourceKey(song: Song): String {
        return resolve(song)?.stableKey
            ?: song.uri.canonicalOpaqueIdentity().let { "uri:$it" }
    }

    fun revision(song: Song, sizeBytes: Long? = null, providerGeneration: Long? = null): MediaRevision {
        val modifiedAtMs = song.dateModifiedSeconds
            ?.takeIf { it in 0L..Long.MAX_VALUE / 1_000L }
            ?.times(1_000L)
        return MediaRevision(
            modifiedAtMs = modifiedAtMs,
            sizeBytes = sizeBytes?.takeIf { it >= 0L },
            providerGeneration = providerGeneration?.takeIf { it >= 0L },
            metadataRevision = if (song.metadataResolved) 1L else 0L,
        )
    }

    fun sourceRevisionKey(
        modifiedAtMs: Long?,
        sizeBytes: Long?,
        providerGeneration: Long? = null,
    ): String {
        return MediaRevision(
            modifiedAtMs = modifiedAtMs?.takeIf { it >= 0L },
            sizeBytes = sizeBytes?.takeIf { it >= 0L },
            providerGeneration = providerGeneration?.takeIf { it >= 0L },
            metadataRevision = 0L,
        ).stableKey
    }

    private fun resolveContentUri(uri: Uri): MediaSourceIdentity? {
        if (uri.scheme.equals("file", ignoreCase = true)) return directFile(uri.path)
        if (!uri.scheme.equals("content", ignoreCase = true)) return null
        if (uri.authority.equals("media", ignoreCase = true)) {
            val segments = uri.pathSegments
            val mediaId = segments.lastOrNull()?.toLongOrNull()
            val volume = segments.firstOrNull()
            mediaStore(volume, mediaId)?.let { return it }
        }
        val isDocumentUri = uri.pathSegments.any { segment ->
            segment.equals("document", ignoreCase = true) || segment.equals("tree", ignoreCase = true)
        }
        if (!isDocumentUri) return null
        val documentId = runCatching { DocumentsContract.getDocumentId(uri) }.getOrNull()
        val treeId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
        return safDocument(uri.authority, documentId, treeId)
    }
}

private const val TRACK_MATCH_IDENTITY_VERSION = 1
private const val STRONG_TRACK_MATCH_SCORE = 12
private const val PROBABLE_TRACK_MATCH_SCORE = 8
private const val DURATION_TOLERANCE_MS = 2_000L
private val MATCH_IDENTITY_WHITESPACE = Regex("\\s+")

private fun String.matchIdentityText(): String {
    return trim().lowercase(Locale.ROOT).replace(MATCH_IDENTITY_WHITESPACE, " ")
}

private fun trackMatchScore(identity: TrackMatchIdentity, song: Song): Int? {
    val title = song.title.matchIdentityText()
    if (identity.normalizedTitle.isBlank() || title != identity.normalizedTitle) return null

    var score = 5
    val artist = song.artist.matchIdentityText()
    if (identity.normalizedArtist.isNotBlank() && artist == identity.normalizedArtist) score += 4
    else if (identity.normalizedArtist.isNotBlank()) return null

    val album = song.album.matchIdentityText()
    if (identity.normalizedAlbum.isNotBlank() && album == identity.normalizedAlbum) score += 2
    val albumArtist = song.albumArtist?.matchIdentityText()
    if (!identity.normalizedAlbumArtist.isNullOrBlank() && albumArtist == identity.normalizedAlbumArtist) score += 1

    val candidateDuration = song.durationMs
    identity.durationMs?.let { duration ->
        if (candidateDuration <= 0L || kotlin.math.abs(duration - candidateDuration) > DURATION_TOLERANCE_MS) return null
        score += if (duration == candidateDuration) 4 else 2
    }
    identity.trackNumber?.let { track ->
        if (song.trackNumber == track) score += 1 else if (song.trackNumber > 0) return null
    }
    identity.discNumber?.let { disc ->
        if (song.discNumber == disc) score += 1 else if (song.discNumber > 0) return null
    }
    if (!identity.normalizedFileName.isNullOrBlank() && song.fileName.matchIdentityText() == identity.normalizedFileName) {
        score += 2
    }
    return score
}

private fun Long?.orEmptyRevisionPart(): String = this?.toString() ?: "-"

private fun String?.normalizedIdentityPart(): String? {
    return this?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotBlank() }
}

private fun String?.opaqueIdentityPart(): String? {
    return this?.trim()?.takeIf { it.isNotBlank() }
}

internal fun Uri.canonicalOpaqueIdentity(): String {
    val raw = toString().trim()
    val schemeEnd = raw.indexOf(':')
    if (schemeEnd <= 0) return raw
    val scheme = raw.substring(0, schemeEnd).lowercase(Locale.ROOT)
    val remainder = raw.substring(schemeEnd + 1)
    if (!remainder.startsWith("//")) return "$scheme:$remainder"
    val authorityStart = 2
    val authorityEnd = remainder.indexOfAny(charArrayOf('/', '?', '#'), authorityStart)
        .takeIf { it >= 0 }
        ?: remainder.length
    return buildString {
        append(scheme)
        append("://")
        append(remainder.substring(authorityStart, authorityEnd).lowercase(Locale.ROOT))
        append(remainder.substring(authorityEnd))
    }
}
