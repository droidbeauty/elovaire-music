package elovaire.music.droidbeauty.app.data.audio

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

internal data class EmbeddedTagMetadata(
    val title: String? = null,
    val artist: String? = null,
    val albumArtist: String? = null,
    val album: String? = null,
    val releaseYear: Int? = null,
    val genre: String? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
)

internal class EmbeddedTagMetadataReader {
    fun read(filePath: String?): EmbeddedTagMetadata? {
        val file = filePath?.let(::File)?.takeIf { it.isFile && it.canRead() } ?: return null
        return runCatching {
            val tag = AudioFileIO.read(file).tag ?: return@runCatching null
            EmbeddedTagMetadata(
                title = tag.text(FieldKey.TITLE),
                artist = tag.text(FieldKey.ARTIST),
                albumArtist = tag.text(FieldKey.ALBUM_ARTIST),
                album = tag.text(FieldKey.ALBUM),
                releaseYear = sequenceOf(FieldKey.YEAR, FieldKey.ORIGINAL_YEAR)
                    .mapNotNull { field -> tag.text(field) }
                    .mapNotNull(::parseReleaseYear)
                    .firstOrNull(),
                genre = tag.text(FieldKey.GENRE),
                trackNumber = tag.text(FieldKey.TRACK)?.parsePositiveNumber(),
                discNumber = tag.text(FieldKey.DISC_NO)?.parsePositiveNumber(),
            )
        }.getOrNull()
    }

    private fun org.jaudiotagger.tag.Tag.text(field: FieldKey): String? {
        return runCatching { getFirst(field) }
            .getOrNull()
            ?.trim()
            ?.takeIf(String::isNotBlank)
    }

    private fun parseReleaseYear(value: String): Int? {
        return YEAR_REGEX.find(value)?.value?.toIntOrNull()?.takeIf { it in 1..9999 }
    }

    private fun String.parsePositiveNumber(): Int? {
        return substringBefore('/').trim().toIntOrNull()?.takeIf { it > 0 }
    }

    private companion object {
        val YEAR_REGEX = Regex("""\b\d{1,4}\b""")
    }
}
