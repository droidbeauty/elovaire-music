package elovaire.music.droidbeauty.app.data.lyrics

import elovaire.music.droidbeauty.app.domain.model.Song
import kotlin.math.abs

internal fun rankLrcLibMatches(
    song: Song,
    responses: List<LrcLibResponse>,
): List<RankedLyricsCandidate> {
    val songDurationSec = song.durationMs / 1000.0
    if (songDurationSec <= 0.0) return emptyList()

    return responses
        .distinctBy { it.id }
        .mapNotNull { response ->
            val score = scoreLrcLibMatch(song, response, songDurationSec) ?: return@mapNotNull null
            RankedLyricsCandidate(response = response, score = score)
        }
        .sortedWith(
            compareByDescending<RankedLyricsCandidate> { it.score }
                .thenByDescending { !it.response.syncedLyrics.isNullOrBlank() }
                .thenBy { abs(it.response.duration - songDurationSec) },
        )
}

internal fun scoreLrcLibMatch(
    song: Song,
    response: LrcLibResponse,
    songDurationSec: Double,
): Int? {
    if (!response.hasLyrics() || response.duration <= 0.0 || response.instrumental) return null
    if (!variantCompatible(song.title, response.name)) return null

    val hasSynced = !response.syncedLyrics.isNullOrBlank()
    val durationTolerance = if (hasSynced) {
        (songDurationSec * 0.02).coerceIn(5.0, 8.0)
    } else {
        (songDurationSec * 0.04).coerceIn(8.0, 15.0)
    }

    val durationDiff = abs(response.duration - songDurationSec)
    if (durationDiff > durationTolerance) return null

    val titleScore = titleScore(song.title, response.name) ?: return null
    val artistScore = artistScore(song.artist, response.artistName) ?: return null
    val albumScore = albumScore(song.album, response.albumName)
    val durationScore = ((durationTolerance - durationDiff) / durationTolerance * 14.0).toInt().coerceAtLeast(0)
    val syncedBonus = if (hasSynced) 12 else 0

    return titleScore + artistScore + albumScore + durationScore + syncedBonus
}

internal fun LrcLibResponse.hasLyrics(): Boolean {
    return !syncedLyrics.isNullOrBlank() || !plainLyrics.isNullOrBlank()
}

internal fun LyricsCandidate.scoreAgainst(identity: LyricsIdentity): Int {
    val titleScore = titleScore(identity.title, title) ?: 0
    val artistScore = artistScore(identity.artist, artist) ?: 0
    val albumScore = albumScore(identity.album, album)
    val syncedBonus = if (syncedLyrics.isNotBlank()) 12 else if (plainLyrics.isNotBlank()) 4 else 0
    val durationScore = durationMs?.let { candidateDuration ->
        val deltaSeconds = abs(candidateDuration - identity.durationMs) / 1000.0
        when {
            deltaSeconds <= 2.0 -> 12
            deltaSeconds <= 5.0 -> 8
            deltaSeconds <= 10.0 -> 4
            else -> 0
        }
    } ?: 0
    return (titleScore + artistScore + albumScore + syncedBonus + durationScore).coerceIn(0, 100)
}

internal fun LyricsCandidate.isAcceptableMatchFor(
    identity: LyricsIdentity,
    score: Int,
): Boolean {
    if (!variantCompatible(identity.title, title)) return false
    val titleScore = titleScore(identity.title, title) ?: return false
    val artistScore = artistScore(identity.artist, artist) ?: return false
    return score >= 62 && titleScore >= 34 && artistScore >= 28
}
