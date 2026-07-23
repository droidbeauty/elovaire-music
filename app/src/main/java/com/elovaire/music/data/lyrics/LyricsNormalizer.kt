package elovaire.music.droidbeauty.app.data.lyrics

import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.data.library.MediaIdentityResolver
import java.text.Normalizer
import java.util.Locale

internal fun Song.toLyricsIdentity(): LyricsIdentity {
    val normalizedTitle = normalizeForLyricsMatch(title)
    val normalizedArtist = normalizeForLyricsMatch(artist)
    val normalizedAlbum = normalizeForLyricsMatch(album)
    val durationBucketSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val normalizedLookupKey = listOf(
        normalizedArtist,
        normalizedTitle,
        durationBucketSeconds.toString(),
        normalizedAlbum,
    ).joinToString("::")

    val metadataSignature = "$normalizedArtist::$normalizedTitle::$durationBucketSeconds"
    val sourceRevision = MediaIdentityResolver.revision(this)
    val cacheKeys = listOf(
        "source::${MediaIdentityResolver.stableKey(this)}::${sourceRevision.stableKey}::$metadataSignature",
    )

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

internal fun normalizeForLyricsMatch(value: String): String {
    return value
        .normalizeUnicodeForLyrics()
        .replace('&', ' ')
        .replace(NOISE_REGEX, " ")
        .replace(BRACKETED_NOISE_REGEX, " ")
        .replace(FEAT_TRAILING_REGEX, " ")
        .replace(SEPARATOR_REGEX, " ")
        .replace(NON_ALNUM_REGEX, " ")
        .replace(WHITESPACE_REGEX, " ")
        .trim()
}

internal fun String.normalizeDiacritics(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(COMBINING_DIACRITICS_REGEX, "")
}

private fun String.normalizeUnicodeForLyrics(): String {
    return normalizeDiacritics()
        .lowercase(Locale.US)
        .replace('’', '\'')
        .replace('`', '\'')
        .replace('“', '"')
        .replace('”', '"')
        .replace('–', '-')
        .replace('—', '-')
        .replace('／', '/')
        .replace('＆', '&')
}

private val FEAT_TRAILING_REGEX = Regex("""(?i)\b(feat\.?|ft\.?|featuring)\b.*$""")
private val COMBINING_DIACRITICS_REGEX = Regex("""\p{InCombiningDiacriticalMarks}+""")
private val BRACKETED_NOISE_REGEX = Regex("""\([^)]*\)|\[[^]]*]""")
private val SEPARATOR_REGEX = Regex("""[/|_:;,]+""")
private val NON_ALNUM_REGEX = Regex("""[^\p{L}\p{N}]+""")
private val WHITESPACE_REGEX = Regex("""\s{2,}""")
private val NOISE_REGEX = Regex(
    """(?i)\b(official audio|official video|lyric video|lyrics video|visualizer|audio|video|explicit|clean|hi[- ]?res|hq|hd)\b""",
)
