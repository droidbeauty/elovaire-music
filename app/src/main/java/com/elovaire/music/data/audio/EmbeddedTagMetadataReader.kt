package elovaire.music.droidbeauty.app.data.audio

import android.content.Context
import android.net.Uri
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationMetadata
import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationPolicy
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
    val volumeNormalization: VolumeNormalizationMetadata? = null,
)

internal class EmbeddedTagMetadataReader(context: Context) {
    private val appContext = context.applicationContext

    fun read(filePath: String?): EmbeddedTagMetadata? {
        val file = filePath?.let(::File)?.takeIf { it.isFile && it.canRead() } ?: return null
        return readFile(file)
    }

    fun read(uri: Uri, filePath: String?, fileName: String): EmbeddedTagMetadata? {
        filePath?.let(::File)?.takeIf { it.isFile && it.canRead() }?.let(::readFile)?.let { return it }
        val extension = fileName.substringAfterLast('.', "tmp").take(12)
        val temp = File.createTempFile("tag-read-", ".$extension", appContext.cacheDir)
        return runCatching {
            appContext.contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                val declaredLength = descriptor.length
                check(declaredLength < 0L || declaredLength <= MAX_TEMP_COPY_BYTES) { "Audio input is too large." }
            }
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output ->
                    val buffer = ByteArray(COPY_BUFFER_BYTES)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        check(total <= MAX_TEMP_COPY_BYTES) { "Audio input is too large." }
                        output.write(buffer, 0, read)
                    }
                }
            } ?: return@runCatching null
            readFile(temp)
        }.getOrNull().also { temp.delete() }
    }

    private fun readFile(file: File): EmbeddedTagMetadata? {
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
                volumeNormalization = tag.volumeNormalizationMetadata(),
            )
        }.getOrNull()
    }

    private fun org.jaudiotagger.tag.Tag.text(field: FieldKey): String? {
        return runCatching { getFirst(field) }
            .getOrNull()
            ?.trim()
            ?.takeIf(String::isNotBlank)
    }

    private fun org.jaudiotagger.tag.Tag.volumeNormalizationMetadata(): VolumeNormalizationMetadata? {
        val metadata = VolumeNormalizationMetadata(
            trackGainDb = textByAnyName(TRACK_GAIN_TAGS)?.let(VolumeNormalizationPolicy::parseGain),
            albumGainDb = textByAnyName(ALBUM_GAIN_TAGS)?.let(VolumeNormalizationPolicy::parseGain),
            trackPeak = textByAnyName(TRACK_PEAK_TAGS)?.let(VolumeNormalizationPolicy::parsePeak),
            albumPeak = textByAnyName(ALBUM_PEAK_TAGS)?.let(VolumeNormalizationPolicy::parsePeak),
        )
        val r128TrackGain = textByAnyName(R128_TRACK_GAIN_TAGS)
            ?.let { value -> VolumeNormalizationPolicy.parseGain(value, r128 = true) }
        val r128AlbumGain = textByAnyName(R128_ALBUM_GAIN_TAGS)
            ?.let { value -> VolumeNormalizationPolicy.parseGain(value, r128 = true) }
        return metadata
            .copy(
                trackGainDb = metadata.trackGainDb ?: r128TrackGain,
                albumGainDb = metadata.albumGainDb ?: r128AlbumGain,
            )
            .takeIf { it.hasUsableGain || it.trackPeak != null || it.albumPeak != null }
    }

    private fun org.jaudiotagger.tag.Tag.textByAnyName(names: Set<String>): String? {
        names.firstNotNullOfOrNull { name ->
            runCatching { getFirst(name) }
                .getOrNull()
                ?.trim()
                ?.takeIf(String::isNotBlank)
        }?.let { return it }
        val normalizedNames = names.mapTo(mutableSetOf(), ::normalizeTagName)
        val fields = runCatching { getFields() }.getOrNull() ?: return null
        while (fields.hasNext()) {
            val field = fields.next()
            if (normalizeTagName(field.id) !in normalizedNames) continue
            val fieldText = field.toString()
            val separatorIndex = sequenceOf(
                fieldText.indexOf('='),
                fieldText.indexOf(':'),
            )
                .filter { it >= 0 }
                .minOrNull()
                ?: continue
            return fieldText
                .substring(separatorIndex + 1)
                .trim()
                .takeIf(String::isNotBlank)
        }
        return null
    }

    private fun parseReleaseYear(value: String): Int? {
        return YEAR_REGEX.find(value)?.value?.toIntOrNull()?.takeIf { it in 1..9999 }
    }

    private fun String.parsePositiveNumber(): Int? {
        return substringBefore('/').trim().toIntOrNull()?.takeIf { it > 0 }
    }

    private companion object {
        const val MAX_TEMP_COPY_BYTES = 512L * 1024L * 1024L
        const val COPY_BUFFER_BYTES = 64 * 1024
        val YEAR_REGEX = Regex("""\b\d{1,4}\b""")
        val TRACK_GAIN_TAGS = setOf(
            "REPLAYGAIN_TRACK_GAIN",
            "REPLAYGAIN TRACK GAIN",
            "replaygain_track_gain",
            "replaygain track gain",
        )
        val ALBUM_GAIN_TAGS = setOf(
            "REPLAYGAIN_ALBUM_GAIN",
            "REPLAYGAIN ALBUM GAIN",
            "replaygain_album_gain",
            "replaygain album gain",
        )
        val TRACK_PEAK_TAGS = setOf(
            "REPLAYGAIN_TRACK_PEAK",
            "REPLAYGAIN TRACK PEAK",
            "replaygain_track_peak",
            "replaygain track peak",
        )
        val ALBUM_PEAK_TAGS = setOf(
            "REPLAYGAIN_ALBUM_PEAK",
            "REPLAYGAIN ALBUM PEAK",
            "replaygain_album_peak",
            "replaygain album peak",
        )
        val R128_TRACK_GAIN_TAGS = setOf(
            "R128_TRACK_GAIN",
            "R128 TRACK GAIN",
            "r128_track_gain",
            "r128 track gain",
        )
        val R128_ALBUM_GAIN_TAGS = setOf(
            "R128_ALBUM_GAIN",
            "R128 ALBUM GAIN",
            "r128_album_gain",
            "r128 album gain",
        )

        fun normalizeTagName(name: String): String {
            return name
                .trim()
                .uppercase()
                .replace(Regex("""[\s-]+"""), "_")
        }
    }
}
