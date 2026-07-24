package elovaire.music.droidbeauty.app.data.lyrics

import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.TagTextField
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.datatype.DataTypes
import org.jaudiotagger.tag.id3.AbstractID3v2Frame
import org.jaudiotagger.tag.id3.AbstractID3v2Tag
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.id3.framebody.FrameBodySYLT
import org.jaudiotagger.tag.id3.framebody.FrameBodyUSLT

internal enum class EmbeddedLyricsTagKind {
    SyncedLyrics,
    UnsyncedLyrics,
}

internal data class EmbeddedLyricsWriteRequest(
    val song: Song,
    val rawLyrics: String,
    val canonicalLyrics: String,
    val tagKind: EmbeddedLyricsTagKind,
)

internal val LRC_TIMESTAMP_REGEX = Regex("""(?m)\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?]""")

internal fun classifyLyricsTagKind(text: String): EmbeddedLyricsTagKind =
    if (LRC_TIMESTAMP_REGEX.containsMatchIn(text)) {
        EmbeddedLyricsTagKind.SyncedLyrics
    } else {
        EmbeddedLyricsTagKind.UnsyncedLyrics
    }

internal object EmbeddedLyricsMetadata {
    fun write(file: File, request: EmbeddedLyricsWriteRequest) {
        val audioFile = AudioFileIO.read(file)
        when (val tag = audioFile.tagOrCreateAndSetDefault) {
            is AbstractID3v2Tag -> writeId3(tag, request)
            is FlacTag -> writeFlac(tag, request)
            else -> {
                check(request.tagKind == EmbeddedLyricsTagKind.UnsyncedLyrics) {
                    "Synchronized lyrics are not supported by this audio format."
                }
                tag.deleteField(FieldKey.LYRICS)
                if (request.canonicalLyrics.isNotBlank()) {
                    tag.setField(tag.createField(FieldKey.LYRICS, request.canonicalLyrics))
                }
            }
        }
        audioFile.commit()
    }

    private fun writeId3(tag: AbstractID3v2Tag, request: EmbeddedLyricsWriteRequest) {
        tag.removeFrame(ID3_SYNCED_LYRICS)
        tag.removeFrame(ID3_UNSYNCED_LYRICS)
        if (request.canonicalLyrics.isBlank()) return
        if (request.tagKind == EmbeddedLyricsTagKind.UnsyncedLyrics) {
            val frame = tag.createFrame(ID3_UNSYNCED_LYRICS)
            val body = frame.body as? FrameBodyUSLT ?: error("Unable to create unsynchronized lyrics metadata.")
            body.textEncoding = if (tag is ID3v24Tag) ID3_UTF8 else ID3_UTF16
            body.language = ID3_LANGUAGE
            body.description = ""
            body.lyric = request.canonicalLyrics
            tag.setField(frame)
            return
        }

        val payload = parseLrcOrPlain(request.canonicalLyrics)
        check(payload?.isSynced == true && payload.lines.isNotEmpty()) { "Synchronized lyrics contain no timed lines." }
        val frame = tag.createFrame(ID3_SYNCED_LYRICS)
        val body = frame.body as? FrameBodySYLT ?: error("Unable to create synchronized lyrics metadata.")
        val textEncoding = if (tag is ID3v24Tag) ID3_UTF8 else ID3_UTF16
        body.textEncoding = textEncoding
        body.setObjectValue(DataTypes.OBJ_LANGUAGE, ID3_LANGUAGE)
        body.setObjectValue(DataTypes.OBJ_TIME_STAMP_FORMAT, ID3_TIMESTAMP_MILLISECONDS)
        body.setObjectValue(DataTypes.OBJ_CONTENT_TYPE, ID3_CONTENT_TYPE_LYRICS)
        body.setObjectValue(DataTypes.OBJ_DESCRIPTION, "")
        body.setLyrics(encodeSylt(payload.lines, textEncoding))
        tag.setField(frame)
    }

    private fun writeFlac(tag: FlacTag, request: EmbeddedLyricsWriteRequest) {
        FLAC_LYRICS_KEYS.forEach(tag::deleteField)
        if (request.canonicalLyrics.isBlank()) return
        val key = if (request.tagKind == EmbeddedLyricsTagKind.SyncedLyrics) {
            FLAC_SYNCED_LYRICS
        } else {
            FLAC_UNSYNCED_LYRICS
        }
        tag.setField(key, request.canonicalLyrics)
    }

    private fun encodeSylt(lines: List<LyricsLine>, textEncoding: Byte): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output ->
            lines.forEach { line ->
                val text = line.text.toByteArray(if (textEncoding == ID3_UTF8) Charsets.UTF_8 else Charsets.UTF_16)
                output.write(text)
                if (textEncoding == ID3_UTF8) output.writeByte(0) else output.writeShort(0)
                output.writeInt(line.startTimeMs?.coerceIn(0L, UInt.MAX_VALUE.toLong())?.toInt() ?: 0)
            }
        }
        return bytes.toByteArray()
    }

    private const val ID3_SYNCED_LYRICS = "SYLT"
    private const val ID3_UNSYNCED_LYRICS = "USLT"
    private const val ID3_LANGUAGE = "eng"
    private const val ID3_UTF16: Byte = 1
    private const val ID3_UTF8: Byte = 3
    private const val ID3_TIMESTAMP_MILLISECONDS = 2
    private const val ID3_CONTENT_TYPE_LYRICS = 1
    private const val FLAC_SYNCED_LYRICS = "SYNCED_LYRICS"
    private const val FLAC_UNSYNCED_LYRICS = "UNSYNCED_LYRICS"
    private val FLAC_LYRICS_KEYS = listOf("SYNCED_LYRICS", "UNSYNCED_LYRICS", "LYRICS", "LRC")
}

internal data class EmbeddedLyricsFields(
    val synced: List<List<LyricsLine>> = emptyList(),
    val unsynced: List<String> = emptyList(),
    val compatibility: List<String> = emptyList(),
)

internal object AudioFileLyricsInspection {
    fun inspect(file: File): EmbeddedLyricsFields {
        return when (val tag = AudioFileIO.read(file).tag) {
            is AbstractID3v2Tag -> inspectId3(tag)
            is FlacTag -> inspectFlac(tag)
            null -> EmbeddedLyricsFields()
            else -> EmbeddedLyricsFields(
                unsynced = listOfNotNull(
                    tag.getFirst(FieldKey.LYRICS).canonicalEmbeddedLyricsText().takeIf(String::isNotBlank),
                ),
            )
        }
    }

    private fun inspectId3(tag: AbstractID3v2Tag): EmbeddedLyricsFields {
        val synced = tag.getFields(ID3_SYNCED_LYRICS)
            .mapNotNull { field ->
                val body = (field as? AbstractID3v2Frame)?.body as? FrameBodySYLT ?: return@mapNotNull null
                decodeSylt(body.lyrics, body.textEncoding.toInt())
                    .takeIf(List<LyricsLine>::isNotEmpty)
            }
        val unsynced = tag.getFields(ID3_UNSYNCED_LYRICS)
            .mapNotNull { field ->
                ((field as? AbstractID3v2Frame)?.body as? FrameBodyUSLT)
                    ?.lyric
                    ?.canonicalEmbeddedLyricsText()
                    ?.takeIf(String::isNotBlank)
            }
        return EmbeddedLyricsFields(synced = synced, unsynced = unsynced)
    }

    private fun inspectFlac(tag: FlacTag): EmbeddedLyricsFields {
        val synced = tag.textValues(FLAC_SYNCED_LYRICS)
            .mapNotNull { value ->
                parseLrcOrPlain(value)
                    ?.takeIf(LyricsPayload::isSynced)
                    ?.lines
            }
        return EmbeddedLyricsFields(
            synced = synced,
            unsynced = tag.textValues(FLAC_UNSYNCED_LYRICS),
            compatibility = listOf(FLAC_COMPATIBILITY_LYRICS, FLAC_COMPATIBILITY_LRC)
                .flatMap { key -> tag.textValues(key) },
        )
    }

    private fun FlacTag.textValues(key: String): List<String> {
        return getFields(key)
            .mapNotNull { (it as? TagTextField)?.content?.canonicalEmbeddedLyricsText() }
            .filter(String::isNotBlank)
    }

    private fun decodeSylt(bytes: ByteArray, encoding: Int): List<LyricsLine> {
        val lines = mutableListOf<LyricsLine>()
        var position = 0
        while (position < bytes.size) {
            val textEnd = findTerminator(bytes, position, encoding)
            val timestampStart = textEnd + terminatorLength(encoding)
            if (timestampStart + Int.SIZE_BYTES > bytes.size) break
            val text = decodeText(bytes.copyOfRange(position, textEnd), encoding)
                .trim()
                .takeIf(String::isNotBlank)
            val timestamp = ByteBuffer.wrap(bytes, timestampStart, Int.SIZE_BYTES)
                .order(ByteOrder.BIG_ENDIAN)
                .int
                .toLong() and UInt.MAX_VALUE.toLong()
            if (text != null) lines += LyricsLine(text = text, startTimeMs = timestamp)
            position = timestampStart + Int.SIZE_BYTES
        }
        return lines
    }

    private fun findTerminator(bytes: ByteArray, start: Int, encoding: Int): Int {
        val length = terminatorLength(encoding)
        var index = start
        while (index + length <= bytes.size) {
            if (bytes[index] == 0.toByte() && (length == 1 || bytes[index + 1] == 0.toByte())) return index
            index += length
        }
        return bytes.size
    }

    private fun terminatorLength(encoding: Int): Int = if (encoding == ID3_UTF16.toInt() || encoding == ID3_UTF16_BE) 2 else 1

    private fun decodeText(bytes: ByteArray, encoding: Int): String {
        return when (encoding) {
            ID3_ISO_8859_1 -> bytes.toString(Charsets.ISO_8859_1)
            ID3_UTF16.toInt() -> bytes.toString(Charsets.UTF_16)
            ID3_UTF16_BE -> bytes.toString(Charsets.UTF_16BE)
            else -> bytes.toString(Charsets.UTF_8)
        }.removePrefix("\uFEFF")
    }

    private const val ID3_SYNCED_LYRICS = "SYLT"
    private const val ID3_UNSYNCED_LYRICS = "USLT"
    private const val ID3_ISO_8859_1 = 0
    private const val ID3_UTF16: Byte = 1
    private const val ID3_UTF16_BE = 2
    private const val FLAC_SYNCED_LYRICS = "SYNCED_LYRICS"
    private const val FLAC_UNSYNCED_LYRICS = "UNSYNCED_LYRICS"
    private const val FLAC_COMPATIBILITY_LYRICS = "LYRICS"
    private const val FLAC_COMPATIBILITY_LRC = "LRC"
}
