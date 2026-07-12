package elovaire.music.droidbeauty.app.data.lyrics

import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.datatype.DataTypes
import org.jaudiotagger.tag.id3.AbstractID3v2Frame
import org.jaudiotagger.tag.id3.AbstractID3v2Tag
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.id3.framebody.FrameBodySYLT

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
                runCatching { tag.deleteField(FieldKey.LYRICS) }
                if (request.canonicalLyrics.isNotBlank()) {
                    tag.setField(tag.createField(FieldKey.LYRICS, request.canonicalLyrics))
                }
            }
        }
        audioFile.commit()
    }

    private fun writeId3(tag: AbstractID3v2Tag, request: EmbeddedLyricsWriteRequest) {
        tag.removeFrame(ID3_SYNCED_LYRICS)
        runCatching { tag.deleteField(FieldKey.LYRICS) }
        if (request.canonicalLyrics.isBlank()) return
        if (request.tagKind == EmbeddedLyricsTagKind.UnsyncedLyrics) {
            tag.setField(tag.createField(FieldKey.LYRICS, request.canonicalLyrics))
            return
        }

        val payload = parseLrcOrPlain(request.canonicalLyrics, providerName = null, confidence = 100)
        check(payload?.isSynced == true && payload.lines.isNotEmpty()) { "Synchronized lyrics contain no timed lines." }
        val frame = tag.createFrame(ID3_SYNCED_LYRICS)
        val body = frame.body as? FrameBodySYLT ?: error("Unable to create synchronized lyrics metadata.")
        val textEncoding = if (tag is ID3v24Tag) ID3_UTF8 else ID3_UTF16
        body.textEncoding = textEncoding
        body.setObjectValue(DataTypes.OBJ_LANGUAGE, "eng")
        body.setObjectValue(DataTypes.OBJ_TIME_STAMP_FORMAT, ID3_TIMESTAMP_MILLISECONDS)
        body.setObjectValue(DataTypes.OBJ_CONTENT_TYPE, ID3_CONTENT_TYPE_LYRICS)
        body.setObjectValue(DataTypes.OBJ_DESCRIPTION, "")
        body.setLyrics(encodeSylt(payload.lines, textEncoding))
        tag.setField(frame)
    }

    private fun writeFlac(tag: FlacTag, request: EmbeddedLyricsWriteRequest) {
        FLAC_LYRICS_KEYS.forEach { key -> runCatching { tag.deleteField(key) } }
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
    private const val ID3_UTF16: Byte = 1
    private const val ID3_UTF8: Byte = 3
    private const val ID3_TIMESTAMP_MILLISECONDS = 2
    private const val ID3_CONTENT_TYPE_LYRICS = 1
    private const val FLAC_SYNCED_LYRICS = "SYNCED_LYRICS"
    private const val FLAC_UNSYNCED_LYRICS = "UNSYNCED_LYRICS"
    private val FLAC_LYRICS_KEYS = listOf("SYNCED_LYRICS", "UNSYNCED_LYRICS", "LYRICS", "LRC")
}
