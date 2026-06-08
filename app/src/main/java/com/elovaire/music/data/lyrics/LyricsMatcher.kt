package elovaire.music.droidbeauty.app.data.lyrics

import kotlin.math.abs

internal fun LyricsCandidate.scoreAgainst(identity: LyricsIdentity): Int {
    val candidateTitle = normalizeTrackTitle(title)
    val candidateArtist = normalizeArtistName(artist)
    val candidateAlbum = normalizeAlbumTitle(album)
    val primaryArtist = normalizeArtistName(extractPrimaryArtist(identity.artist))

    var score = 0
    if (candidateTitle == identity.normalizedTitle) score += 38
    if (candidateArtist == identity.normalizedArtist || candidateArtist == primaryArtist) score += 34
    if (candidateAlbum.isNotBlank() && candidateAlbum == identity.normalizedAlbum) score += 10

    score += tokenOverlapBonus(identity.normalizedTitle, candidateTitle, maxBonus = 12)
    score += tokenOverlapBonus(identity.normalizedArtist.ifBlank { primaryArtist }, candidateArtist, maxBonus = 10)
    score += tokenOverlapBonus(identity.normalizedAlbum, candidateAlbum, maxBonus = 6)

    if (syncedLyrics.isNotBlank()) score += 8
    if (plainLyrics.isNotBlank()) score += 3
    if (looksLikeAlternateVersion(identity.title, title)) score -= 10

    durationMs?.let { candidateDuration ->
        val deltaSeconds = abs(candidateDuration - identity.durationMs) / 1000.0
        when {
            deltaSeconds <= 1.5 -> score += 16
            deltaSeconds <= 4.0 -> score += 12
            deltaSeconds <= 8.0 -> score += 6
            deltaSeconds <= 12.0 -> score += 2
            deltaSeconds > 20.0 -> score -= 45
            else -> score -= 12
        }
    }

    return score.coerceIn(0, 100)
}

internal fun LyricsCandidate.isAcceptableMatchFor(
    identity: LyricsIdentity,
    score: Int,
): Boolean {
    val candidateTitle = normalizeTrackTitle(title)
    val candidateArtist = normalizeArtistName(artist)
    val candidateAlbum = normalizeAlbumTitle(album)
    val primaryArtist = normalizeArtistName(extractPrimaryArtist(identity.artist))

    val exactTitle = candidateTitle.isNotBlank() && candidateTitle == identity.normalizedTitle
    val exactArtist = candidateArtist.isNotBlank() &&
        (candidateArtist == identity.normalizedArtist || candidateArtist == primaryArtist)
    val titleOverlap = tokenOverlapRatio(identity.normalizedTitle, candidateTitle) >= 0.72f
    val artistOverlap = tokenOverlapRatio(
        identity.normalizedArtist.ifBlank { primaryArtist },
        candidateArtist,
    ) >= 0.72f
    val strongTitleOverlap = tokenOverlapRatio(identity.normalizedTitle, candidateTitle) >= 0.84f
    val strongArtistOverlap = tokenOverlapRatio(
        identity.normalizedArtist.ifBlank { primaryArtist },
        candidateArtist,
    ) >= 0.84f
    val albumOverlap = candidateAlbum.isNotBlank() &&
        identity.normalizedAlbum.isNotBlank() &&
        tokenOverlapRatio(identity.normalizedAlbum, candidateAlbum) >= 0.72f
    val hasSyncedLyrics = syncedLyrics.isNotBlank()
    val durationMismatchLimitMs = if (hasSyncedLyrics) 30_000L else 20_000L
    val durationMismatchTooLarge = durationMs?.let { abs(it - identity.durationMs) > durationMismatchLimitMs } == true

    return when {
        durationMismatchTooLarge -> false
        hasSyncedLyrics && exactTitle && exactArtist -> score >= 38
        hasSyncedLyrics && exactTitle && artistOverlap -> score >= 44
        hasSyncedLyrics && exactArtist && titleOverlap -> score >= 46
        hasSyncedLyrics && strongTitleOverlap && strongArtistOverlap -> score >= 50
        hasSyncedLyrics && titleOverlap && artistOverlap && albumOverlap -> score >= 52
        exactTitle && exactArtist -> score >= 42
        exactTitle && artistOverlap -> score >= 50
        exactArtist && titleOverlap -> score >= 52
        titleOverlap && artistOverlap && albumOverlap -> score >= 56
        else -> false
    }
}

private fun tokenOverlapBonus(
    left: String,
    right: String,
    maxBonus: Int,
): Int = (tokenOverlapRatio(left, right) * maxBonus).toInt()

private fun tokenOverlapRatio(left: String, right: String): Float {
    if (left.isBlank() || right.isBlank()) return 0f
    val leftTokens = left.split(' ').filter { it.isNotBlank() }.toSet()
    val rightTokens = right.split(' ').filter { it.isNotBlank() }.toSet()
    if (leftTokens.isEmpty() || rightTokens.isEmpty()) return 0f
    val intersection = leftTokens.intersect(rightTokens).size.toFloat()
    val union = leftTokens.union(rightTokens).size.toFloat().coerceAtLeast(1f)
    return intersection / union
}

private fun looksLikeAlternateVersion(
    originalTitle: String,
    candidateTitle: String,
): Boolean {
    val originalDecorators = lookupDecorators(originalTitle)
    val candidateDecorators = lookupDecorators(candidateTitle)
    return candidateDecorators.isNotEmpty() && candidateDecorators != originalDecorators
}

private fun lookupDecorators(value: String): Set<String> {
    return VERSION_DECORATOR_REGEX.findAll(value)
        .map { it.value.lowercase() }
        .toSet()
}

private val VERSION_DECORATOR_REGEX = Regex("""(?i)\b(remaster(ed)?|live|acoustic|demo|edit|mix|version|mono|stereo|instrumental)\b""")
