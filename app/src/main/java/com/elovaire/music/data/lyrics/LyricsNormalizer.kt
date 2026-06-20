package elovaire.music.droidbeauty.app.data.lyrics

import elovaire.music.droidbeauty.app.domain.model.Song
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

    val cacheKeys = buildList {
        add(normalizedLookupKey)
        val metadataSignature = "$normalizedArtist::$normalizedTitle::$durationBucketSeconds"
        if (id > 0L) add("media::$id::$metadataSignature")
        uri.toString().takeIf { it.isNotBlank() }?.let { add("uri::${it.hashCode()}::$metadataSignature") }
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
    val baseTitle = baseTitleForLyricsMatch(identity.title)
    val cleanedTitle = cleanedTitleForFallback(identity.title)
    val album = identity.album.takeIf { it.isNotBlank() }

    return buildList {
        add(LyricsQueryVariant(identity.artist, identity.title, album))
        add(LyricsQueryVariant(primaryArtist, identity.title, album))
        add(LyricsQueryVariant(identity.artist, baseTitle, album))
        add(LyricsQueryVariant(primaryArtist, baseTitle, album))
        if (cleanedTitle.isNotBlank()) {
            add(LyricsQueryVariant(identity.artist, cleanedTitle, null))
            add(LyricsQueryVariant(primaryArtist, cleanedTitle, null))
        }
        add(LyricsQueryVariant(identity.artist, identity.title, null))
        add(LyricsQueryVariant(primaryArtist, identity.title, null))
    }
        .map { variant ->
            variant.copy(
                artist = variant.artist.trim(),
                title = variant.title.trim(),
                album = variant.album?.trim()?.takeIf { it.isNotBlank() },
            )
        }
        .filter { it.title.isNotBlank() && it.artist.isNotBlank() }
        .distinct()
}

internal fun normalizeTrackTitle(value: String): String = normalizeForLyricsMatch(value)

internal fun normalizeArtistName(value: String): String = normalizeForLyricsMatch(value)

internal fun normalizeAlbumTitle(value: String): String = normalizeForLyricsMatch(value)

internal fun simplifyLookupTitle(value: String): String = baseTitleForLyricsMatch(value)

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

internal fun baseTitleForLyricsMatch(title: String): String {
    return title
        .normalizeUnicodeForLyrics()
        .replace(BRACKETED_NOISE_REGEX) { match ->
            val content = match.value.removeSurrounding("(", ")").removeSurrounding("[", "]")
            if (containsVariantToken(content)) match.value else " "
        }
        .replace(FEAT_TRAILING_REGEX, " ")
        .replace(NOISE_REGEX, " ")
        .replace(SEPARATOR_REGEX, " ")
        .replace(NON_ALNUM_REGEX, " ")
        .replace(WHITESPACE_REGEX, " ")
        .trim()
}

internal fun cleanedTitleForFallback(title: String): String {
    return baseTitleForLyricsMatch(title)
        .replace(VARIANT_WORD_REGEX, " ")
        .replace(WHITESPACE_REGEX, " ")
        .trim()
}

internal fun extractPrimaryArtist(value: String): String {
    return value
        .split(ARTIST_SPLIT_REGEX)
        .map { it.trim() }
        .firstOrNull { it.isNotBlank() }
        ?: value.trim()
}

internal fun titleScore(local: String, remote: String): Int? {
    val localBase = baseTitleForLyricsMatch(local)
    val remoteBase = baseTitleForLyricsMatch(remote)
    if (localBase.isBlank() || remoteBase.isBlank()) return null
    if (!variantCompatible(local, remote)) return null

    return when {
        localBase == remoteBase -> 52
        tokenOverlapRatio(localBase, remoteBase) >= 0.92f -> 46
        tokenOverlapRatio(localBase, remoteBase) >= 0.82f -> 40
        tokenOverlapRatio(localBase, remoteBase) >= 0.72f -> 34
        else -> null
    }
}

internal fun artistScore(local: String, remote: String): Int? {
    val localPrimary = normalizeForLyricsMatch(extractPrimaryArtist(local))
    val remotePrimary = normalizeForLyricsMatch(extractPrimaryArtist(remote))
    if (localPrimary.isBlank() || remotePrimary.isBlank()) return null

    return when {
        localPrimary == remotePrimary -> 40
        tokenOverlapRatio(localPrimary, remotePrimary) >= 0.9f -> 35
        tokenOverlapRatio(localPrimary, remotePrimary) >= 0.75f -> 28
        else -> null
    }
}

internal fun albumScore(local: String, remote: String?): Int {
    val localNormalized = normalizeForLyricsMatch(local)
    val remoteNormalized = normalizeForLyricsMatch(remote.orEmpty())
    if (localNormalized.isBlank() || remoteNormalized.isBlank()) return 0
    return when {
        localNormalized == remoteNormalized -> 8
        tokenOverlapRatio(localNormalized, remoteNormalized) >= 0.82f -> 5
        else -> 0
    }
}

internal fun variantCompatible(localTitle: String, remoteTitle: String): Boolean {
    val localVariants = extractVariantTags(localTitle)
    val remoteVariants = extractVariantTags(remoteTitle)

    val incompatiblePair = INCOMPATIBLE_VARIANT_GROUPS.any { group ->
        localVariants.intersect(group).isNotEmpty() && remoteVariants.intersect(group).isNotEmpty() &&
            localVariants.intersect(group) != remoteVariants.intersect(group)
    }
    if (incompatiblePair) return false

    if (remoteVariants.isEmpty()) return true
    if (localVariants.isEmpty()) {
        return remoteVariants.none { it in REQUIRES_EXPLICIT_LOCAL_VARIANT }
    }
    return remoteVariants.all { it in localVariants || it in BENIGN_REMOTE_VARIANTS }
}

internal fun tokenOverlapRatio(left: String, right: String): Float {
    if (left.isBlank() || right.isBlank()) return 0f
    val leftTokens = left.split(' ').filter { it.isNotBlank() }.toSet()
    val rightTokens = right.split(' ').filter { it.isNotBlank() }.toSet()
    if (leftTokens.isEmpty() || rightTokens.isEmpty()) return 0f
    val intersection = leftTokens.intersect(rightTokens).size.toFloat()
    val union = leftTokens.union(rightTokens).size.toFloat().coerceAtLeast(1f)
    return intersection / union
}

internal fun String.normalizeDiacritics(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
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

private fun extractVariantTags(title: String): Set<String> {
    val normalized = title.normalizeUnicodeForLyrics()
    return VARIANT_KEYWORDS.entries
        .filter { (_, regex) -> regex.containsMatchIn(normalized) }
        .mapTo(linkedSetOf()) { it.key }
}

private fun containsVariantToken(value: String): Boolean {
    val normalized = value.normalizeUnicodeForLyrics()
    return VARIANT_WORD_REGEX.containsMatchIn(normalized)
}

private val FEAT_TRAILING_REGEX = Regex("""(?i)\b(feat\.?|ft\.?|featuring)\b.*$""")
private val BRACKETED_NOISE_REGEX = Regex("""\([^)]*\)|\[[^]]*]""")
private val SEPARATOR_REGEX = Regex("""[/|_:;,]+""")
private val NON_ALNUM_REGEX = Regex("""[^\p{L}\p{N}]+""")
private val WHITESPACE_REGEX = Regex("""\s{2,}""")
private val ARTIST_SPLIT_REGEX = Regex("""(?i)\b(feat\.?|ft\.?|featuring|with|x)\b|,|&|;|/""")
private val NOISE_REGEX = Regex(
    """(?i)\b(official audio|official video|lyric video|lyrics video|visualizer|audio|video|explicit|clean|hi[- ]?res|hq|hd)\b""",
)
private val VARIANT_WORD_REGEX = Regex(
    """(?i)\b(remix|mix|live|acoustic|sped up|sped-up|slowed|nightcore|instrumental|karaoke|cover|demo|radio edit|extended|movie|tv|opening|ending|op|ed|short ver|short version|full ver|full version)\b""",
)

private val VARIANT_KEYWORDS = mapOf(
    "remix" to Regex("""(?i)\b(remix|mix)\b"""),
    "live" to Regex("""(?i)\blive\b"""),
    "acoustic" to Regex("""(?i)\bacoustic\b"""),
    "sped" to Regex("""(?i)\b(sped up|sped-up|nightcore)\b"""),
    "slowed" to Regex("""(?i)\bslowed\b"""),
    "instrumental" to Regex("""(?i)\b(instrumental|karaoke)\b"""),
    "cover" to Regex("""(?i)\bcover\b"""),
    "demo" to Regex("""(?i)\bdemo\b"""),
    "radio_edit" to Regex("""(?i)\b(radio edit|edit)\b"""),
    "extended" to Regex("""(?i)\b(extended|full version|full ver)\b"""),
    "short" to Regex("""(?i)\b(short version|short ver|tv size|movie size|edit ver)\b"""),
    "movie_tv" to Regex("""(?i)\b(movie|tv|soundtrack|ost|opening|ending|op|ed)\b"""),
)

private val INCOMPATIBLE_VARIANT_GROUPS = listOf(
    setOf("remix"),
    setOf("live"),
    setOf("acoustic"),
    setOf("sped", "slowed"),
    setOf("instrumental"),
    setOf("cover"),
    setOf("demo"),
    setOf("radio_edit", "extended", "short"),
    setOf("movie_tv", "short", "extended"),
)

private val REQUIRES_EXPLICIT_LOCAL_VARIANT = setOf(
    "remix",
    "live",
    "acoustic",
    "sped",
    "slowed",
    "instrumental",
    "cover",
    "demo",
    "movie_tv",
    "short",
    "extended",
)

private val BENIGN_REMOTE_VARIANTS = setOf("radio_edit")
