package elovaire.music.droidbeauty.app.data.tags.matching

import elovaire.music.droidbeauty.app.data.artwork.isArtworkBoundsSafe

import elovaire.music.droidbeauty.app.domain.model.Song

internal interface AudioFingerprintProvider {
    suspend fun fingerprint(song: Song): Result<AudioFingerprint>
}

internal data class AudioFingerprint(
    val songId: Long,
    val durationSeconds: Int,
    val fingerprint: String,
    val fileSignature: String,
)

internal interface AcoustIdClient {
    val isConfigured: Boolean

    suspend fun lookup(
        fingerprint: String,
        durationSeconds: Int,
    ): Result<AcoustIdLookupResponse>
}

internal data class AcoustIdLookupResponse(
    val recordings: List<AcoustIdRecordingMatch>,
)

internal data class AcoustIdRecordingMatch(
    val recordingId: String,
    val score: Double,
    val title: String,
    val artist: String,
    val releaseIds: List<String>,
)

internal interface MusicBrainzClient {
    suspend fun getRecording(recordingMbid: String): Result<MusicBrainzRecording>
    suspend fun getRelease(releaseMbid: String): Result<MusicBrainzRelease>
    suspend fun searchRelease(albumTitle: String, albumArtist: String): Result<List<String>>
}

internal data class MusicBrainzRecording(
    val id: String,
    val title: String,
    val artist: String,
    val releaseIds: List<String>,
)

internal data class MusicBrainzRelease(
    val id: String,
    val title: String,
    val albumArtist: String,
    val releaseYear: Int?,
    val tracks: List<MusicBrainzTrack>,
    val relatedUrls: List<String>,
)

internal data class MusicBrainzTrack(
    val id: String,
    val recordingId: String,
    val title: String,
    val artist: String,
    val trackNumber: Int,
    val discNumber: Int,
    val durationMs: Long?,
)

internal interface AlbumArtworkProvider {
    suspend fun findArtwork(match: ResolvedAlbumMatch): Result<AlbumArtworkResult?>
}

internal data class AlbumArtworkResult(
    val bytes: ByteArray,
    val width: Int?,
    val height: Int?,
    val source: ArtworkSource,
) {
    override fun equals(other: Any?): Boolean {
        return this === other || (
            other is AlbumArtworkResult &&
                bytes.contentEquals(other.bytes) &&
                width == other.width &&
                height == other.height &&
                source == other.source
            )
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + (width ?: 0)
        result = 31 * result + (height ?: 0)
        result = 31 * result + source.hashCode()
        return result
    }

    fun isAcceptableForEmbedding(): Boolean {
        val imageWidth = width ?: return false
        val imageHeight = height ?: return false
        if (imageWidth < 600 || imageHeight < 600 || !isArtworkBoundsSafe(imageWidth, imageHeight)) return false
        return imageWidth.toFloat() / imageHeight.toFloat() in 0.95f..1.05f
    }
}

internal enum class ArtworkSource {
    Tidal,
    CoverArtArchive,
    Embedded,
}

internal data class ResolvedAlbumMatch(
    val release: MusicBrainzRelease,
    val trackMatches: List<ResolvedTrackMatch>,
    val confidence: MatchConfidence,
    val score: Int,
)

internal data class ResolvedTrackMatch(
    val song: Song,
    val remoteTrack: MusicBrainzTrack,
    val confidence: MatchConfidence,
    val score: Int,
    val source: TrackMatchSource,
)

internal enum class MatchConfidence {
    High,
    Medium,
    Low,
}

internal enum class TrackMatchSource {
    FingerprintRecordingId,
    DiscAndTrackNumber,
    TitleDurationArtist,
}

internal sealed interface AlbumTagMatchResult {
    data class Success(
        val match: ResolvedAlbumMatch,
        val artwork: AlbumArtworkResult?,
    ) : AlbumTagMatchResult

    data class Unavailable(val reason: String) : AlbumTagMatchResult
    data class NoMatch(val reason: String) : AlbumTagMatchResult
    data class Failed(val reason: String) : AlbumTagMatchResult
}
