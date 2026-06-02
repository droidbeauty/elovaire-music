package elovaire.music.droidbeauty.app.data.lyrics

import elovaire.music.droidbeauty.app.domain.model.Song
import java.text.Normalizer
import java.util.Locale

internal fun Song.toLyricsIdentity(): LyricsIdentity {
    val normalizedTitle = normalizeTrackTitle(title)
    val normalizedArtist = normalizeArtistName(artist)
    val normalizedAlbum = normalizeAlbumTitle(album)
    val durationBucketSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val normalizedLookupKey = listOf(
        normalizedArtist,
        normalizedTitle,
        durationBucketSeconds.toString(),
        normalizedAlbum,
    ).joinToString("::")

    val cacheKeys = buildList {
        add(normalizedLookupKey)
        if (id > 0L) add("media::$id")
        uri.toString().takeIf { it.isNotBlank() }?.let { add("uri::${it.hashCode()}") }
    }.distinct()

    return LyricsIdentity(
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        mediaId = id.takeIf { it > 0L }?.toString(),
        contentUri = uri.toString().takeIf { it.isNotBlank() },
        normalizedTitle = normalizedTitle,
        normalizedArtist = normalizedArtist,
        normalizedAlbum = normalizedAlbum,
        normalizedLookupKey = normalizedLookupKey,
        cacheKeys = cacheKeys,
    )
}

internal fun buildLyricsQueryVariants(identity: LyricsIdentity): List<LyricsQueryVariant> {
    val primaryArtist = extractPrimaryArtist(identity.artist)
    val normalizedArtist = normalizeArtistName(identity.artist)
    val normalizedTitle = normalizeTrackTitle(identity.title)
    val simplifiedTitle = simplifyLookupTitle(identity.title)
    val normalizedAlbum = normalizeAlbumTitle(identity.album).takeIf { it.isNotBlank() }
    val originalAlbum = identity.album.takeIf { it.isNotBlank() }

    return buildList {
        add(LyricsQueryVariant(identity.artist, identity.title, originalAlbum))
        add(LyricsQueryVariant(identity.artist, identity.title, null))
        if (primaryArtist != identity.artist) {
            add(LyricsQueryVariant(primaryArtist, identity.title, originalAlbum))
            add(LyricsQueryVariant(primaryArtist, identity.title, null))
        }
        if (normalizedArtist.isNotBlank() && normalizedTitle.isNotBlank()) {
            add(LyricsQueryVariant(normalizedArtist, normalizedTitle, normalizedAlbum))
            add(LyricsQueryVariant(normalizedArtist, normalizedTitle, null))
        }
        if (simplifiedTitle.isNotBlank() && simplifiedTitle != identity.title) {
            add(LyricsQueryVariant(identity.artist, simplifiedTitle, originalAlbum))
            add(LyricsQueryVariant(identity.artist, simplifiedTitle, null))
            if (primaryArtist != identity.artist) {
                add(LyricsQueryVariant(primaryArtist, simplifiedTitle, originalAlbum))
                add(LyricsQueryVariant(primaryArtist, simplifiedTitle, null))
            }
            if (normalizedArtist.isNotBlank()) {
                add(LyricsQueryVariant(normalizedArtist, simplifiedTitle, normalizedAlbum))
                add(LyricsQueryVariant(normalizedArtist, simplifiedTitle, null))
            }
        }
    }.distinct()
}

internal fun normalizeTrackTitle(value: String): String {
    return value
        .normalizeDiacritics()
        .lowercase(Locale.US)
        .replace("&", "and")
        .replace(Regex("""(?i)\b(feat|ft|featuring)\b.*$"""), "")
        .replace(
            Regex(
                """(?i)(official video|official audio|lyric video|lyrics video|visualizer|remaster(ed)?|mono|stereo)""",
            ),
            " ",
        )
        .replace(Regex("""\([^)]*\)|\[[^]]*]"""), " ")
        .replace(Regex("""[^a-z0-9]+"""), " ")
        .replace(Regex("""\s{2,}"""), " ")
        .trim()
}

internal fun normalizeArtistName(value: String): String {
    return value.normalizeForMatch()
}

internal fun normalizeAlbumTitle(value: String): String {
    return value
        .normalizeDiacritics()
        .lowercase(Locale.US)
        .replace(Regex("""\([^)]*\)|\[[^]]*]"""), " ")
        .replace(Regex("""(?i)\b(deluxe|expanded|edition|remaster(ed)?|version)\b"""), " ")
        .replace(Regex("""[^a-z0-9]+"""), " ")
        .replace(Regex("""\s{2,}"""), " ")
        .trim()
}

internal fun simplifyLookupTitle(value: String): String {
    return value
        .replace(Regex("""(?i)\s+-\s+.*$"""), "")
        .replace(Regex("""(?i)\s*/\s+.*$"""), "")
        .replace(Regex("""(?i)\b(remaster(ed)?|live|version|edit|mix|deluxe)\b.*$"""), "")
        .trim()
}

internal fun extractPrimaryArtist(value: String): String {
    return value.split(Regex("""(?i)\b(feat\.?|ft\.?|featuring|with)\b|,|&|;|/"""))
        .map { it.trim() }
        .firstOrNull { it.isNotBlank() }
        ?: value
}

internal fun String.normalizeForMatch(): String {
    return normalizeDiacritics()
        .lowercase(Locale.US)
        .replace("&", "and")
        .replace(Regex("""[^a-z0-9]+"""), " ")
        .replace(Regex("""\s{2,}"""), " ")
        .trim()
}

internal fun String.normalizeDiacritics(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
}
