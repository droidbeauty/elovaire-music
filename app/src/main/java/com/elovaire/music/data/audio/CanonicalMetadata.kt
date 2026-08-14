package elovaire.music.droidbeauty.app.data.audio

import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationMetadata

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
            volumeNormalization = sources.firstNotNullOfOrNull { it.volumeNormalization },
        )
    }

    private fun List<MetadataSourceValues>.firstValue(
        selector: (MetadataSourceValues) -> String?,
    ): String? = firstNotNullOfOrNull { source -> selector(source).canonicalText() }

    private fun String?.canonicalText(): String? = this
        ?.trim()
        ?.takeIf(String::isNotBlank)

    private fun String.parsePositiveTagNumber(): Int? = substringBefore('/')
        .trim()
        .toIntOrNull()
        ?.takeIf { it > 0 }

    private fun isValidYear(year: Int): Boolean = year in 1..9_999
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

