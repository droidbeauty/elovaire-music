package elovaire.music.droidbeauty.app.data.audio

import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationMetadata
import java.text.Normalizer

/** Metadata from one source, kept separate until the canonical precedence pass. */
internal data class MetadataSourceValues(
    val title: String? = null,
    val artist: String? = null,
    val albumArtist: String? = null,
    val album: String? = null,
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
        val sources = listOfNotNull(embedded, platform, indexed)
        return CanonicalMetadataValues(
            title = sources.firstValue { it.title },
            artist = sources.firstValue { it.artist },
            albumArtist = sources.firstValue { it.albumArtist },
            album = sources.firstValue { it.album },
            releaseYear = sources.firstNotNullOfOrNull { it.releaseYear?.takeIf(::isValidYear) },
            genre = sources.firstValue { it.genre },
            trackNumber = sources.firstNotNullOfOrNull { it.trackNumber?.parsePositiveTagNumber() },
            discNumber = sources.firstNotNullOfOrNull { it.discNumber?.parsePositiveTagNumber() },
            volumeNormalization = sources.resolveVolumeNormalization(),
        )
    }

    private fun List<MetadataSourceValues>.firstValue(
        selector: (MetadataSourceValues) -> String?,
    ): String? = firstNotNullOfOrNull { source -> selector(source).canonicalText() }

    private fun String?.canonicalText(): String? {
        val normalized = this
            ?.takeIf { it.length <= MAX_CANONICAL_METADATA_TEXT_CHARS }
            ?.let { Normalizer.normalize(it, Normalizer.Form.NFC) }
            ?.filterNot(::isIgnorableTextCharacter)
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: return null
        return normalized.takeIf { value ->
            value.any { character ->
                !character.isWhitespace() &&
                    !isCombiningMark(character)
            }
        }
    }

    private fun String.parsePositiveTagNumber(): Int? = substringBefore('/')
        .trim()
        .toIntOrNull()
        ?.takeIf { it > 0 }

    private fun List<MetadataSourceValues>.resolveVolumeNormalization(): VolumeNormalizationMetadata? {
        // Gain and peak values describe one mastering record. Do not combine fields
        // from different sources: a stale peak could otherwise constrain a newer gain.
        return firstNotNullOfOrNull { values -> values.volumeNormalization }
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

    private fun isIgnorableTextCharacter(character: Char): Boolean = character in ignorableTextCharacters

    private fun isCombiningMark(character: Char): Boolean = Character.getType(character) in combiningMarkTypes

    private const val MAX_CANONICAL_METADATA_TEXT_CHARS = 4_096
}

internal fun EmbeddedTagMetadata.toMetadataSourceValues(): MetadataSourceValues {
    return MetadataSourceValues(
        title = title,
        artist = artist,
        albumArtist = albumArtist,
        album = album,
        releaseYear = releaseYear,
        genre = genre,
        trackNumber = trackNumber?.toString(),
        discNumber = discNumber?.toString(),
        volumeNormalization = volumeNormalization,
    )
}
