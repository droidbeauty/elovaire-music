package elovaire.music.droidbeauty.app.data.tags.matching

import elovaire.music.droidbeauty.app.core.runSuspendCatching
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.withTimeoutOrNull

internal class FingerprintAlbumTagMatcher(
    private val fingerprintProvider: AudioFingerprintProvider,
    private val acoustIdClient: AcoustIdClient,
    private val musicBrainzClient: MusicBrainzClient,
    private val artworkResolver: AlbumArtworkResolver,
) {
    suspend fun matchAlbum(album: Album): AlbumTagMatchResult {
        if (album.songs.isEmpty()) return AlbumTagMatchResult.NoMatch("This album has no readable songs.")

        val fingerprintMatches = linkedMapOf<Long, List<AcoustIdRecordingMatch>>()
        if (acoustIdClient.isConfigured) {
            album.songs.forEach { song ->
                val fingerprint = withTimeoutOrNull(FINGERPRINT_TIMEOUT_MS) {
                    fingerprintProvider.fingerprint(song).getOrNull()
                } ?: return@forEach
                val lookup = withTimeoutOrNull(LOOKUP_TIMEOUT_MS) {
                    acoustIdClient.lookup(
                        fingerprint = fingerprint.fingerprint,
                        durationSeconds = fingerprint.durationSeconds,
                    ).getOrNull()
                } ?: return@forEach
                val accepted = lookup.recordings.filter { it.score >= MIN_ACOUSTID_SCORE }
                if (accepted.isNotEmpty()) fingerprintMatches[song.id] = accepted
            }
        }

        val resolved = if (fingerprintMatches.isNotEmpty()) {
            resolveFingerprintCandidates(album, fingerprintMatches)
        } else {
            resolveTextFallback(album)
        }

        if (resolved == null) {
            return if (!acoustIdClient.isConfigured) {
                AlbumTagMatchResult.Unavailable(
                    "AcoustID is not configured and no safe MusicBrainz fallback match was found.",
                )
            } else {
                AlbumTagMatchResult.NoMatch("No sufficiently reliable online match was found.")
            }
        }
        val artwork = runSuspendCatching { artworkResolver.resolve(resolved) }.getOrNull()
        return AlbumTagMatchResult.Success(resolved, artwork)
    }

    private suspend fun resolveFingerprintCandidates(
        album: Album,
        fingerprintMatches: Map<Long, List<AcoustIdRecordingMatch>>,
    ): ResolvedAlbumMatch? {
        val expandedMatches = fingerprintMatches.mapValues { (_, matches) ->
            matches.map { match ->
                if (match.releaseIds.isNotEmpty()) {
                    match
                } else {
                    val releaseIds = musicBrainzClient.getRecording(match.recordingId)
                        .getOrNull()
                        ?.releaseIds
                        .orEmpty()
                    match.copy(releaseIds = releaseIds)
                }
            }
        }
        val releaseVotes = mutableMapOf<String, Double>()
        expandedMatches.values.flatten().forEach { recording ->
            recording.releaseIds.forEach { releaseId ->
                releaseVotes[releaseId] = (releaseVotes[releaseId] ?: 0.0) + recording.score
            }
        }
        val releaseIds = releaseVotes.entries
            .sortedByDescending(Map.Entry<String, Double>::value)
            .take(MAX_RELEASE_CANDIDATES)
            .map(Map.Entry<String, Double>::key)
        return releaseIds.mapNotNull { releaseId ->
            musicBrainzClient.getRelease(releaseId).getOrNull()?.let { release ->
                scoreRelease(album, release, expandedMatches, releaseVotes[releaseId] ?: 0.0)
            }
        }.filter { match ->
            match.confidence != MatchConfidence.Low &&
                match.trackMatches.size.toFloat() / album.songs.size.toFloat() >= MIN_ALBUM_COVERAGE_TO_APPLY
        }.maxByOrNull(ResolvedAlbumMatch::score)
    }

    private suspend fun resolveTextFallback(album: Album): ResolvedAlbumMatch? {
        val releaseIds = musicBrainzClient.searchRelease(album.title, album.artist)
            .getOrNull()
            .orEmpty()
            .take(MAX_TEXT_FALLBACK_CANDIDATES)
        return releaseIds.mapNotNull { releaseId ->
            musicBrainzClient.getRelease(releaseId).getOrNull()?.let { release ->
                scoreRelease(album, release, emptyMap(), 0.0)
            }
        }.filter { match ->
            val coverage = match.trackMatches.size.toFloat() / album.songs.size.toFloat()
            match.confidence != MatchConfidence.Low &&
                coverage >= HIGH_CONFIDENCE_ALBUM_COVERAGE &&
                normalizedSimilarity(album.title, match.release.title) >= 0.78f &&
                normalizedSimilarity(album.artist, match.release.albumArtist) >= 0.70f
        }.maxByOrNull(ResolvedAlbumMatch::score)
    }

    private fun scoreRelease(
        album: Album,
        release: MusicBrainzRelease,
        fingerprintMatches: Map<Long, List<AcoustIdRecordingMatch>>,
        fingerprintVote: Double,
    ): ResolvedAlbumMatch {
        val remainingTracks = release.tracks.toMutableList()
        val trackMatches = album.songs
            .sortedWith(compareBy<Song>({ it.discNumber }, { it.trackNumber }, { it.fileName }))
            .mapNotNull { song ->
                val recordingIds = fingerprintMatches[song.id].orEmpty().mapTo(mutableSetOf()) { it.recordingId }
                val best = remainingTracks.map { remote ->
                    val source = when {
                        remote.recordingId.isNotBlank() && remote.recordingId in recordingIds ->
                            TrackMatchSource.FingerprintRecordingId
                        song.discNumber > 0 && song.trackNumber > 0 &&
                            remote.discNumber == song.discNumber && remote.trackNumber == song.trackNumber ->
                            TrackMatchSource.DiscAndTrackNumber
                        else -> TrackMatchSource.TitleDurationArtist
                    }
                    Triple(remote, scoreTrack(song, remote, source), source)
                }.maxByOrNull { it.second } ?: return@mapNotNull null
                if (best.second < MIN_TRACK_SCORE_TO_APPLY) return@mapNotNull null
                remainingTracks.remove(best.first)
                ResolvedTrackMatch(
                    song = song,
                    remoteTrack = best.first,
                    confidence = when {
                        best.second >= 100 -> MatchConfidence.High
                        best.second >= 75 -> MatchConfidence.Medium
                        else -> MatchConfidence.Low
                    },
                    score = best.second,
                    source = best.third,
                )
            }
        val coverage = trackMatches.size.toFloat() / album.songs.size.coerceAtLeast(1).toFloat()
        val averageTrackScore = trackMatches.map(ResolvedTrackMatch::score).average().takeIf { !it.isNaN() } ?: 0.0
        val titleScore = normalizedSimilarity(album.title, release.title)
        val artistScore = normalizedSimilarity(album.artist, release.albumArtist)
        val score = (
            coverage * 100f +
                averageTrackScore * 0.65 +
                titleScore * 30f +
                artistScore * 20f +
                fingerprintVote.coerceAtMost(album.songs.size.toDouble()) * 8.0 -
                abs(release.tracks.size - album.songs.size).coerceAtMost(12) * 2.0
            ).toInt()
        val confidence = when {
            coverage >= HIGH_CONFIDENCE_ALBUM_COVERAGE && averageTrackScore >= 85.0 -> MatchConfidence.High
            coverage >= MIN_ALBUM_COVERAGE_TO_APPLY && averageTrackScore >= 68.0 -> MatchConfidence.Medium
            else -> MatchConfidence.Low
        }
        return ResolvedAlbumMatch(release, trackMatches, confidence, score)
    }

    private fun scoreTrack(
        local: Song,
        remote: MusicBrainzTrack,
        source: TrackMatchSource,
    ): Int {
        var score = when (source) {
            TrackMatchSource.FingerprintRecordingId -> 100
            TrackMatchSource.DiscAndTrackNumber -> 35
            TrackMatchSource.TitleDurationArtist -> 0
        }
        val titleSimilarity = normalizedSimilarity(local.title, remote.title)
        val artistSimilarity = normalizedSimilarity(local.artist, remote.artist)
        if (source != TrackMatchSource.FingerprintRecordingId && hasVariantMismatch(local.title, remote.title)) {
            return Int.MIN_VALUE
        }
        score += (titleSimilarity * 35f).toInt()
        score += (artistSimilarity * 10f).toInt()
        remote.durationMs?.let { duration ->
            val difference = abs(duration - local.durationMs)
            score += when {
                difference <= 2_500L -> 20
                difference <= 7_500L -> 10
                difference <= 15_000L -> 2
                else -> -20
            }
        }
        if (local.discNumber > 0 && local.discNumber == remote.discNumber) score += 8
        if (local.trackNumber > 0 && local.trackNumber == remote.trackNumber) score += 12
        return score
    }

    private fun hasVariantMismatch(local: String, remote: String): Boolean {
        val normalizedLocal = normalize(local)
        val normalizedRemote = normalize(remote)
        return VARIANT_TOKENS.any { token -> (token in normalizedLocal) != (token in normalizedRemote) }
    }

    private fun normalizedSimilarity(left: String, right: String): Float {
        val normalizedLeft = normalize(left)
        val normalizedRight = normalize(right)
        if (normalizedLeft.isBlank() || normalizedRight.isBlank()) return 0f
        if (normalizedLeft == normalizedRight) return 1f
        if (normalizedLeft.contains(normalizedRight) || normalizedRight.contains(normalizedLeft)) return 0.85f
        val leftTokens = normalizedLeft.split(' ').toSet()
        val rightTokens = normalizedRight.split(' ').toSet()
        return (leftTokens.intersect(rightTokens).size.toFloat() /
            leftTokens.union(rightTokens).size.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    }

    private fun normalize(value: String): String {
        return value.lowercase(Locale.ROOT)
            .replace(Regex("""\([^)]*\)|\[[^]]*]"""), " ")
            .replace(Regex("""[^\p{L}\p{N}]+"""), " ")
            .trim()
            .replace(Regex("""\s+"""), " ")
    }

    private companion object {
        const val MIN_ACOUSTID_SCORE = 0.55
        const val MIN_TRACK_SCORE_TO_APPLY = 65
        const val MIN_ALBUM_COVERAGE_TO_APPLY = 0.60f
        const val HIGH_CONFIDENCE_ALBUM_COVERAGE = 0.75f
        const val MAX_RELEASE_CANDIDATES = 5
        const val MAX_TEXT_FALLBACK_CANDIDATES = 3
        const val FINGERPRINT_TIMEOUT_MS = 45_000L
        const val LOOKUP_TIMEOUT_MS = 12_000L
        val VARIANT_TOKENS = setOf("live", "remix", "karaoke", "acoustic", "instrumental", "bonus", "deluxe")
    }
}
