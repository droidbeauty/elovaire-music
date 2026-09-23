package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.provider.DocumentsContract
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.data.library.network.NetworkPathPolicy
import elovaire.music.droidbeauty.app.data.library.network.NetworkResourceUri
import java.util.Locale
import java.security.MessageDigest

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

internal data class PortableMediaIdentityProjection(
    val revision: String,
    val identitiesBySongId: Map<Long, TrackMatchIdentity>,
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

    fun trackMatchIdentity(
        song: Song,
        sizeBytes: Long? = null,
        includeSourceStableKey: Boolean = true,
    ): TrackMatchIdentity {
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
            sourceStableKey = if (includeSourceStableKey) stableKey(song) else null,
        )
    }

    /** Revision for the exact locator-free projection consumed by portable user-data backup. */
    fun portableIdentityRevision(songs: List<Song>): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val appender = PortableIdentityDigestAppender(digest)
        songs.asSequence()
            .sortedBy(Song::id)
            .forEach { song ->
                appender.appendLong(song.id)
                appender.appendInt(TRACK_MATCH_IDENTITY_VERSION)
                appender.appendNullableLong(null)
                appender.appendNullableLong(song.durationMs.takeIf { it > 0L })
                appender.appendString(song.title.matchIdentityText())
                appender.appendString(song.artist.matchIdentityText())
                appender.appendString(song.album.matchIdentityText())
                appender.appendString(song.albumArtist?.matchIdentityText().orEmpty())
                appender.appendString(song.fileName.matchIdentityText().takeIf(String::isNotBlank).orEmpty())
                appender.appendNullableInt(song.trackNumber.takeIf { it > 0 })
                appender.appendNullableInt(song.discNumber.takeIf { it > 0 })
            }
        appender.finish()
        return digest.digest().let { bytes ->
            buildString(bytes.size * 2) {
                bytes.forEach { byte ->
                    val value = byte.toInt() and 0xff
                    append(HEX_DIGITS[value ushr 4])
                    append(HEX_DIGITS[value and 0x0f])
                }
            }
        }
    }

    fun portableIdentityProjection(
        songs: List<Song>,
        revision: String = portableIdentityRevision(songs),
    ): PortableMediaIdentityProjection = PortableMediaIdentityProjection(
        revision = revision,
        identitiesBySongId = songs.associate { song ->
            song.id to trackMatchIdentity(song, includeSourceStableKey = false)
        },
    )

    fun portableIdentityChanged(before: Song, after: Song): Boolean {
        return before.id != after.id ||
            before.durationMs.takeIf { it > 0L } != after.durationMs.takeIf { it > 0L } ||
            before.title.matchIdentityText() != after.title.matchIdentityText() ||
            before.artist.matchIdentityText() != after.artist.matchIdentityText() ||
            before.album.matchIdentityText() != after.album.matchIdentityText() ||
            before.albumArtist?.matchIdentityText().orEmpty() != after.albumArtist?.matchIdentityText().orEmpty() ||
            before.fileName.matchIdentityText().takeIf(String::isNotBlank).orEmpty() !=
            after.fileName.matchIdentityText().takeIf(String::isNotBlank).orEmpty() ||
            before.trackNumber.takeIf { it > 0 } != after.trackNumber.takeIf { it > 0 } ||
            before.discNumber.takeIf { it > 0 } != after.discNumber.takeIf { it > 0 }
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
            var exactSong: Song? = null
            var exactCount = 0
            for (song in songs) {
                if (stableKey(song) == sourceKey) {
                    exactSong = song
                    exactCount += 1
                }
            }
            if (exactCount == 1) return TrackMatchResolution(TrackMatchConfidence.Exact, exactSong)
            if (exactCount > 1) return TrackMatchResolution(TrackMatchConfidence.Ambiguous)
        }
        var bestSong: Song? = null
        var bestScore: Int? = null
        var tied = false
        for (song in songs) {
            val score = trackMatchScore(identity, song) ?: continue
            when {
                bestScore == null || score > bestScore -> {
                    bestSong = song
                    bestScore = score
                    tied = false
                }
                score == bestScore -> tied = true
            }
        }
        if (bestSong == null || tied) {
            return if (bestSong == null) {
                TrackMatchResolution(TrackMatchConfidence.NoMatch)
            } else {
                TrackMatchResolution(TrackMatchConfidence.Ambiguous)
            }
        }
        val confidence = trackMatchConfidence(bestScore ?: return TrackMatchResolution(TrackMatchConfidence.NoMatch))
        return TrackMatchResolution(confidence, bestSong.takeIf { confidence != TrackMatchConfidence.NoMatch })
    }

    fun prepareTrackMatcher(songs: List<Song>): PreparedTrackMatcher = PreparedTrackMatcher(songs)

    internal fun trackMatchConfidence(score: Int): TrackMatchConfidence = when {
        score >= STRONG_TRACK_MATCH_SCORE -> TrackMatchConfidence.Strong
        score >= PROBABLE_TRACK_MATCH_SCORE -> TrackMatchConfidence.Probable
        else -> TrackMatchConfidence.NoMatch
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

internal class PreparedTrackMatcher(songs: List<Song>) {
    private val candidates = songs.map(::PreparedTrackCandidate)
    private val uniqueBySourceKey = HashMap<String, PreparedTrackCandidate>(candidates.size)
    private val ambiguousSourceKeys = HashSet<String>()

    init {
        candidates.forEach { candidate ->
            if (candidate.sourceKey in ambiguousSourceKeys) return@forEach
            val previous = uniqueBySourceKey.putIfAbsent(candidate.sourceKey, candidate)
            if (previous != null) {
                uniqueBySourceKey.remove(candidate.sourceKey)
                ambiguousSourceKeys += candidate.sourceKey
            }
        }
    }

    fun resolve(identity: TrackMatchIdentity): TrackMatchResolution {
        if (identity.version != TRACK_MATCH_IDENTITY_VERSION) {
            return TrackMatchResolution(TrackMatchConfidence.NoMatch)
        }
        identity.sourceStableKey?.let { sourceKey ->
            uniqueBySourceKey[sourceKey]?.let { candidate ->
                return TrackMatchResolution(TrackMatchConfidence.Exact, candidate.song)
            }
            if (sourceKey in ambiguousSourceKeys) {
                return TrackMatchResolution(TrackMatchConfidence.Ambiguous)
            }
        }
        var bestCandidate: PreparedTrackCandidate? = null
        var bestScore: Int? = null
        var tied = false
        candidates.forEach { candidate ->
            val score = candidate.score(identity) ?: return@forEach
            when {
                bestScore == null || score > bestScore -> {
                    bestCandidate = candidate
                    bestScore = score
                    tied = false
                }
                score == bestScore -> tied = true
            }
        }
        if (bestCandidate == null || tied) {
            return if (bestCandidate == null) {
                TrackMatchResolution(TrackMatchConfidence.NoMatch)
            } else {
                TrackMatchResolution(TrackMatchConfidence.Ambiguous)
            }
        }
        val confidence = MediaIdentityResolver.trackMatchConfidence(
            bestScore ?: return TrackMatchResolution(TrackMatchConfidence.NoMatch),
        )
        return TrackMatchResolution(
            confidence,
            bestCandidate.song.takeIf { confidence != TrackMatchConfidence.NoMatch },
        )
    }
}

private class PreparedTrackCandidate(val song: Song) {
    val sourceKey: String = MediaIdentityResolver.stableKey(song)
    private val normalizedTitle = song.title.matchIdentityText()
    private val normalizedArtist = song.artist.matchIdentityText()
    private val normalizedAlbum = song.album.matchIdentityText()
    private val normalizedAlbumArtist = song.albumArtist?.matchIdentityText()
    private val normalizedFileName = song.fileName.matchIdentityText()

    fun score(identity: TrackMatchIdentity): Int? {
        if (identity.normalizedTitle.isBlank() || normalizedTitle != identity.normalizedTitle) return null

        var score = 5
        if (identity.normalizedArtist.isNotBlank() && normalizedArtist == identity.normalizedArtist) score += 4
        else if (identity.normalizedArtist.isNotBlank()) return null

        if (identity.normalizedAlbum.isNotBlank() && normalizedAlbum == identity.normalizedAlbum) score += 2
        if (!identity.normalizedAlbumArtist.isNullOrBlank() && normalizedAlbumArtist == identity.normalizedAlbumArtist) score += 1

        identity.durationMs?.let { duration ->
            if (song.durationMs <= 0L || kotlin.math.abs(duration - song.durationMs) > DURATION_TOLERANCE_MS) return null
            score += if (duration == song.durationMs) 4 else 2
        }
        identity.trackNumber?.let { track ->
            if (song.trackNumber == track) score += 1 else if (song.trackNumber > 0) return null
        }
        identity.discNumber?.let { disc ->
            if (song.discNumber == disc) score += 1 else if (song.discNumber > 0) return null
        }
        if (!identity.normalizedFileName.isNullOrBlank() && normalizedFileName == identity.normalizedFileName) score += 2
        return score
    }
}

private const val TRACK_MATCH_IDENTITY_VERSION = 1
private const val STRONG_TRACK_MATCH_SCORE = 12
private const val PROBABLE_TRACK_MATCH_SCORE = 8
private const val DURATION_TOLERANCE_MS = 2_000L
private val MATCH_IDENTITY_WHITESPACE = Regex("\\s+")
private const val DIGEST_STRING_TAG: Byte = 1
private const val DIGEST_INT_TAG: Byte = 2
private const val DIGEST_LONG_TAG: Byte = 3
private const val DIGEST_NULL_TAG: Byte = 4
private const val HEX_DIGITS = "0123456789abcdef"

private class PortableIdentityDigestAppender(
    private val digest: MessageDigest,
) {
    private val buffer = ByteArray(1024)
    private var bufferedBytes = 0

    fun appendInt(value: Int) {
        put(DIGEST_INT_TAG)
        appendFixedInt(value)
    }

    fun appendNullableInt(value: Int?) {
        if (value == null) {
            put(DIGEST_NULL_TAG)
        } else {
            appendInt(value)
        }
    }

    fun appendLong(value: Long) {
        put(DIGEST_LONG_TAG)
        appendFixedLong(value)
    }

    fun appendNullableLong(value: Long?) {
        if (value == null) {
            put(DIGEST_NULL_TAG)
        } else {
            appendLong(value)
        }
    }

    fun appendString(value: String) {
        put(DIGEST_STRING_TAG)
        appendFixedInt(utf8Length(value))
        var index = 0
        while (index < value.length) {
            val character = value[index]
            if (character.isHighSurrogate() && index + 1 < value.length && value[index + 1].isLowSurrogate()) {
                appendCodePoint(Character.toCodePoint(character, value[index + 1]))
                index += 2
            } else {
                appendCodePoint(if (character.isSurrogate()) 0xfffd else character.code)
                index += 1
            }
        }
        flush()
    }

    private fun appendFixedInt(value: Int) {
        put((value ushr 24).toByte())
        put((value ushr 16).toByte())
        put((value ushr 8).toByte())
        put(value.toByte())
    }

    private fun appendFixedLong(value: Long) {
        put((value ushr 56).toByte())
        put((value ushr 48).toByte())
        put((value ushr 40).toByte())
        put((value ushr 32).toByte())
        put((value ushr 24).toByte())
        put((value ushr 16).toByte())
        put((value ushr 8).toByte())
        put(value.toByte())
    }

    private fun appendCodePoint(codePoint: Int) {
        when {
            codePoint <= 0x7f -> put(codePoint.toByte())
            codePoint <= 0x7ff -> {
                put((0xc0 or (codePoint ushr 6)).toByte())
                put((0x80 or (codePoint and 0x3f)).toByte())
            }
            codePoint <= 0xffff -> {
                put((0xe0 or (codePoint ushr 12)).toByte())
                put((0x80 or ((codePoint ushr 6) and 0x3f)).toByte())
                put((0x80 or (codePoint and 0x3f)).toByte())
            }
            else -> {
                put((0xf0 or (codePoint ushr 18)).toByte())
                put((0x80 or ((codePoint ushr 12) and 0x3f)).toByte())
                put((0x80 or ((codePoint ushr 6) and 0x3f)).toByte())
                put((0x80 or (codePoint and 0x3f)).toByte())
            }
        }
    }

    private fun put(value: Byte) {
        buffer[bufferedBytes++] = value
        if (bufferedBytes == buffer.size) flush()
    }

    private fun flush() {
        if (bufferedBytes == 0) return
        digest.update(buffer, 0, bufferedBytes)
        bufferedBytes = 0
    }

    fun finish() = flush()
}

private fun utf8Length(value: String): Int {
    var length = 0
    var index = 0
    while (index < value.length) {
        val character = value[index]
        when {
            character.code <= 0x7f -> length += 1
            character.code <= 0x7ff -> length += 2
            character.isHighSurrogate() && index + 1 < value.length && value[index + 1].isLowSurrogate() -> {
                length += 4
                index += 1
            }
            character.isSurrogate() -> length += 3
            else -> length += 3
        }
        index += 1
    }
    return length
}

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
