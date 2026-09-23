package elovaire.music.droidbeauty.app.data.audio

import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationMetadata
import java.text.Normalizer

/** Metadata from one source, kept separate until the canonical precedence pass. */
internal data class MetadataSourceValues(
    val title: String? = null,
    val artist: String? = null,
    val albumArtist: String? = null,
    val album: String? = null,
    val description: String? = null,
    val releaseYear: Int? = null,
    val genre: String? = null,
    val trackNumber: String? = null,
    val discNumber: String? = null,
    val volumeNormalization: VolumeNormalizationMetadata? = null,
)

internal data class CanonicalMetadataValues(
    val title: String? = null,
    val artist: String? = null,
    val albumArtist: String? = null,
    val album: String? = null,
    val description: String? = null,
    val releaseYear: Int? = null,
    val genre: String? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val volumeNormalization: VolumeNormalizationMetadata? = null,
)

internal object CanonicalMetadataResolver {
    fun resolve(
        embedded: MetadataSourceValues? = null,
        platform: MetadataSourceValues? = null,
        indexed: MetadataSourceValues? = null,
    ): CanonicalMetadataValues {
        return CanonicalMetadataValues(
            title = firstText(embedded, platform, indexed) { it.title },
            artist = firstText(embedded, platform, indexed) { it.artist },
            albumArtist = firstText(embedded, platform, indexed) { it.albumArtist },
            album = firstText(embedded, platform, indexed) { it.album },
            description = firstText(embedded, platform, indexed) { it.description },
            releaseYear = firstValue(embedded, platform, indexed) { it.releaseYear?.takeIf(::isValidYear) },
            genre = firstText(embedded, platform, indexed) { it.genre },
            trackNumber = firstValue(embedded, platform, indexed) { it.trackNumber?.parsePositiveTagNumber() },
            discNumber = firstValue(embedded, platform, indexed) { it.discNumber?.parsePositiveTagNumber() },
            volumeNormalization = firstValue(embedded, platform, indexed) {
                it.volumeNormalization?.sanitizeVolumeNormalization()
            },
        )
    }

    private inline fun firstText(
        embedded: MetadataSourceValues?,
        platform: MetadataSourceValues?,
        indexed: MetadataSourceValues?,
        selector: (MetadataSourceValues) -> String?,
    ): String? {
        return embedded?.let { selector(it).canonicalText() }
            ?: platform?.let { selector(it).canonicalText() }
            ?: indexed?.let { selector(it).canonicalText() }
    }

    private inline fun <T> firstValue(
        embedded: MetadataSourceValues?,
        platform: MetadataSourceValues?,
        indexed: MetadataSourceValues?,
        selector: (MetadataSourceValues) -> T?,
    ): T? {
        return embedded?.let(selector)
            ?: platform?.let(selector)
            ?: indexed?.let(selector)
    }

    private fun String?.canonicalText(): String? {
        val normalized = this
            ?.takeIf { it.length <= MAX_CANONICAL_METADATA_TEXT_CHARS }
            ?.let { Normalizer.normalize(it, Normalizer.Form.NFC) }
            ?.let(::sanitizeMetadataText)
            ?.takeIf(String::isNotBlank)
            ?: return null
        return normalized.takeIf { value ->
            value.any { character ->
                !character.isWhitespace() &&
                    !isCombiningMark(character)
            }
        }
    }

    private fun sanitizeMetadataText(value: String): String {
        return buildString(value.length) {
            value.forEach { character ->
                when {
                    character == '\t' || character == '\n' || character == '\r' -> append(' ')
                    isIgnorableTextCharacter(character) || isBidiFormattingMark(character) -> Unit
                    Character.getType(character) == Character.CONTROL.toInt() -> Unit
                    else -> append(character)
                }
            }
        }.trim { it.isWhitespace() || Character.isSpaceChar(it) }
    }

    private fun String.parsePositiveTagNumber(): Int? = substringBefore('/')
        .trim()
        .toIntOrNull()
        ?.takeIf { it > 0 }

    private fun VolumeNormalizationMetadata.sanitizeVolumeNormalization(): VolumeNormalizationMetadata? {
        val sanitized = copy(
            trackGainDb = trackGainDb?.takeIf { it.isFinite() },
            albumGainDb = albumGainDb?.takeIf { it.isFinite() },
            trackPeak = trackPeak?.takeIf { it.isFinite() && it > 0f },
            albumPeak = albumPeak?.takeIf { it.isFinite() && it > 0f },
        )
        return sanitized.takeIf {
            it.trackGainDb != null || it.albumGainDb != null ||
                it.trackPeak != null || it.albumPeak != null
        }
    }

    private fun isValidYear(year: Int): Boolean = year in 1..9_999

    private val combiningMarkTypes = setOf<Int>(
        Character.NON_SPACING_MARK.toInt(),
        Character.COMBINING_SPACING_MARK.toInt(),
        Character.ENCLOSING_MARK.toInt(),
    )
    private val ignorableTextCharacters = setOf(
        '\u200B',
        '\u200C',
        '\uFEFF',
    )
    private val bidiFormattingMarks = setOf(
        '\u061C',
        '\u200E',
        '\u200F',
        '\u202A',
        '\u202B',
        '\u202C',
        '\u202D',
        '\u202E',
        '\u2066',
        '\u2067',
        '\u2068',
        '\u2069',
    )

    private fun isIgnorableTextCharacter(character: Char): Boolean = character in ignorableTextCharacters

    private fun isBidiFormattingMark(character: Char): Boolean = character in bidiFormattingMarks

    private fun isCombiningMark(character: Char): Boolean = Character.getType(character) in combiningMarkTypes

    private const val MAX_CANONICAL_METADATA_TEXT_CHARS = 4_096
}

internal fun EmbeddedTagMetadata.toMetadataSourceValues(): MetadataSourceValues {
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
