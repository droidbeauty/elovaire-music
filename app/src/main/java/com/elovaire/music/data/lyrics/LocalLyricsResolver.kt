package elovaire.music.app.data.lyrics

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import elovaire.music.app.domain.model.Song
import java.io.BufferedInputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.util.Locale

internal class LocalLyricsResolver(
    context: Context,
) {
    private val contentResolver: ContentResolver = context.applicationContext.contentResolver

    fun resolve(song: Song): LocalLyricsMatch? {
        return readEmbeddedLyrics(song) ?: readSidecarLyrics(song)
    }

    private fun readEmbeddedLyrics(song: Song): LocalLyricsMatch? {
        val headerBytes = contentResolver.openInputStream(song.uri)?.use { input ->
            input.readNBytes(4)
        } ?: return null

        return when {
            headerBytes.startsWithAscii("ID3") -> readId3Lyrics(song)
            headerBytes.startsWithAscii("fLaC") -> readFlacLyrics(song)
            else -> null
        }
    }

    private fun readId3Lyrics(song: Song): LocalLyricsMatch? {
        return contentResolver.openInputStream(song.uri)?.use { rawInput ->
            val input = BufferedInputStream(rawInput, EMBEDDED_TAG_BUFFER_BYTES)
            val header = input.readNBytes(10)
            if (header.size < 10 || !header.copyOfRange(0, 3).startsWithAscii("ID3")) {
                return@use null
            }
            val majorVersion = header[3].toInt() and 0xFF
            val flags = header[5].toInt() and 0xFF
            val tagSize = synchsafeInt(header, 6)
            if (tagSize <= 0 || tagSize > MAX_EMBEDDED_TAG_BYTES) {
                return@use null
            }
            val tagData = input.readNBytes(tagSize)
            if (tagData.size < tagSize) return@use null
            val normalizedData = if ((flags and ID3_UNSYNCHRONIZATION_FLAG) != 0) {
                removeId3Unsynchronization(tagData)
            } else {
                tagData
            }
            parseId3Frames(normalizedData, majorVersion)
        }
    }

    private fun parseId3Frames(
        tagData: ByteArray,
        majorVersion: Int,
    ): LocalLyricsMatch? {
        var position = 0
        val headerSize = if (majorVersion == 2) 6 else 10
        val syncedLines = mutableListOf<LyricsLine>()
        val plainLyrics = mutableListOf<String>()

        while (position + headerSize <= tagData.size) {
            val header = parseId3FrameHeader(tagData, position, majorVersion) ?: break
            val (frameId, frameSize, nextPosition) = header
            if (frameId.isBlank() || frameSize <= 0 || nextPosition + frameSize > tagData.size) break
            val frameData = tagData.copyOfRange(nextPosition, nextPosition + frameSize)
            when (frameId) {
                "USLT", "ULT" -> parseUsltFrame(frameData)?.let(plainLyrics::add)
                "SYLT", "SLT" -> syncedLines += parseSyltFrame(frameData)
            }
            position = nextPosition + frameSize
        }

        parseTimedPayload(syncedLines)?.let { payload ->
            return LocalLyricsMatch(payload)
        }
        parsePlainLyrics(plainLyrics.joinToString("\n"))?.let { lines ->
            return LocalLyricsMatch(
                LyricsPayload(
                    lines = lines,
                    isSynced = false,
                ),
            )
        }
        return null
    }

    private fun parseId3FrameHeader(
        tagData: ByteArray,
        position: Int,
        majorVersion: Int,
    ): Triple<String, Int, Int>? {
        return when (majorVersion) {
            2 -> {
                val frameId = String(tagData, position, 3, Charsets.ISO_8859_1)
                val size = ((tagData[position + 3].toInt() and 0xFF) shl 16) or
                    ((tagData[position + 4].toInt() and 0xFF) shl 8) or
                    (tagData[position + 5].toInt() and 0xFF)
                Triple(frameId, size, position + 6)
            }
            3 -> {
                val frameId = String(tagData, position, 4, Charsets.ISO_8859_1)
                val size = ByteBuffer.wrap(tagData, position + 4, 4).int
                Triple(frameId, size, position + 10)
            }
            4 -> {
                val frameId = String(tagData, position, 4, Charsets.ISO_8859_1)
                val size = synchsafeInt(tagData, position + 4)
                Triple(frameId, size, position + 10)
            }
            else -> null
        }
    }

    private fun parseUsltFrame(frameData: ByteArray): String? {
        if (frameData.size < 5) return null
        val encoding = frameData[0].toInt() and 0xFF
        val descriptorEnd = findEncodedTerminator(frameData, 4, encoding)
        val lyricsStart = descriptorEnd + terminatorLengthForEncoding(encoding)
        if (lyricsStart !in 0..frameData.size) return null
        return decodeTextPayload(frameData.copyOfRange(lyricsStart, frameData.size), encoding)
            ?.removeBom()
            ?.takeIf { it.isNotBlank() }
    }

    private fun parseSyltFrame(frameData: ByteArray): List<LyricsLine> {
        if (frameData.size < 7) return emptyList()
        val encoding = frameData[0].toInt() and 0xFF
        val timestampFormat = frameData[4].toInt() and 0xFF
        if (timestampFormat != ID3_TIMESTAMP_MILLISECONDS) return emptyList()

        var position = findEncodedTerminator(frameData, 6, encoding) + terminatorLengthForEncoding(encoding)
        val lines = mutableListOf<LyricsLine>()
        while (position < frameData.size) {
            val textEnd = findEncodedTerminator(frameData, position, encoding).coerceAtMost(frameData.size)
            val text = decodeTextPayload(frameData.copyOfRange(position, textEnd), encoding)?.let(::sanitizeLyricLine)
            val timestampStart = textEnd + terminatorLengthForEncoding(encoding)
            if (timestampStart + 4 > frameData.size) break
            val timestamp = ByteBuffer.wrap(frameData, timestampStart, 4).order(ByteOrder.BIG_ENDIAN).int.toLong()
            if (text != null) {
                lines += LyricsLine(
                    text = text,
                    startTimeMs = timestamp.coerceAtLeast(0L),
                )
            }
            position = timestampStart + 4
        }
        return lines
    }

    private fun readFlacLyrics(song: Song): LocalLyricsMatch? {
        return contentResolver.openInputStream(song.uri)?.use { rawInput ->
            val input = BufferedInputStream(rawInput, EMBEDDED_TAG_BUFFER_BYTES)
            val magic = input.readNBytes(4)
            if (magic.size < 4 || !magic.startsWithAscii("fLaC")) return@use null

            var isLastBlock = false
            while (!isLastBlock) {
                val header = input.readNBytes(4)
                if (header.size < 4) break
                isLastBlock = (header[0].toInt() and 0x80) != 0
                val blockType = header[0].toInt() and 0x7F
                val blockSize = ((header[1].toInt() and 0xFF) shl 16) or
                    ((header[2].toInt() and 0xFF) shl 8) or
                    (header[3].toInt() and 0xFF)
                if (blockSize < 0 || blockSize > MAX_VORBIS_COMMENT_BYTES) {
                    input.skip(blockSize.toLong())
                    continue
                }
                val blockData = input.readNBytes(blockSize)
                if (blockData.size < blockSize) break
                if (blockType == FLAC_BLOCK_VORBIS_COMMENT) {
                    parseFlacVorbisLyrics(blockData)?.let { return@use it }
                }
            }
            null
        }
    }

    private fun parseFlacVorbisLyrics(blockData: ByteArray): LocalLyricsMatch? {
        val buffer = ByteBuffer.wrap(blockData).order(ByteOrder.LITTLE_ENDIAN)
        if (buffer.remaining() < 8) return null
        val vendorLength = buffer.int.coerceAtLeast(0)
        if (vendorLength > buffer.remaining()) return null
        buffer.position(buffer.position() + vendorLength)
        if (buffer.remaining() < 4) return null
        val commentCount = buffer.int.coerceAtLeast(0)
        var syncedPayload: LyricsPayload? = null
        var plainPayload: LyricsPayload? = null

        repeat(commentCount) {
            if (buffer.remaining() < 4) return@repeat
            val commentLength = buffer.int.coerceAtLeast(0)
            if (commentLength <= 0 || commentLength > buffer.remaining()) return@repeat
            val commentBytes = ByteArray(commentLength)
            buffer.get(commentBytes)
            val comment = commentBytes.toString(Charsets.UTF_8)
            val separatorIndex = comment.indexOf('=')
            if (separatorIndex <= 0) return@repeat
            val key = comment.substring(0, separatorIndex)
                .uppercase(Locale.US)
                .replace(" ", "")
                .replace("_", "")
            val value = comment.substring(separatorIndex + 1).removeBom()
            when {
                key in FLAC_SYNCED_KEYS || looksLikeTimedLyrics(value) -> {
                    parseSyncedLyrics(value)?.let { lines ->
                        syncedPayload = LyricsPayload(lines = lines, isSynced = true)
                    }
                }
                key in FLAC_PLAIN_KEYS -> {
                    parsePlainLyrics(value)?.let { lines ->
                        plainPayload = LyricsPayload(lines = lines, isSynced = false)
                    }
                }
            }
        }

        return syncedPayload?.let(::LocalLyricsMatch) ?: plainPayload?.let(::LocalLyricsMatch)
    }

    private fun readSidecarLyrics(song: Song): LocalLyricsMatch? {
        val localFile = resolveSongFile(song) ?: return null
        val parent = localFile.parentFile ?: return null
        if (!parent.isDirectory) return null

        val baseNames = linkedSetOf(
            localFile.nameWithoutExtension,
            song.fileName.substringBeforeLast('.', song.fileName),
            sanitizeFileStem(song.title),
        ).filter { it.isNotBlank() }

        baseNames.forEach { baseName ->
            val lrcFile = File(parent, "$baseName.lrc")
            if (lrcFile.isFile) {
                parseSyncedLyrics(readTextFile(lrcFile))?.let { lines ->
                    if (lines.isNotEmpty()) {
                        return LocalLyricsMatch(LyricsPayload(lines = lines, isSynced = true))
                    }
                }
            }
            val txtFile = File(parent, "$baseName.txt")
            if (txtFile.isFile) {
                parsePlainLyrics(readTextFile(txtFile))?.let { lines ->
                    if (lines.isNotEmpty()) {
                        return LocalLyricsMatch(LyricsPayload(lines = lines, isSynced = false))
                    }
                }
            }
        }

        return null
    }

    @SuppressLint("Range")
    private fun resolveSongFile(song: Song): File? {
        if (song.uri.scheme == "file") {
            return song.uri.path?.let(::File)?.takeIf(File::exists)
        }
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val resolvedPath = runCatching {
            contentResolver.query(song.uri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
            }
        }.getOrNull()
        return resolvedPath?.let(::File)?.takeIf(File::exists)
    }

    private fun parseTimedPayload(lines: List<LyricsLine>): LyricsPayload? {
        val validLines = lines
            .filter { !it.text.isBlank() && it.startTimeMs != null }
            .sortedBy { it.startTimeMs }
        if (validLines.isEmpty()) return null
        return LyricsPayload(
            lines = validLines.mapIndexed { index, line ->
                line.copy(
                    index = index,
                    endTimeMs = validLines.getOrNull(index + 1)?.startTimeMs,
                )
            },
            isSynced = true,
        )
    }

    private fun readTextFile(file: File): String? {
        val bytes = runCatching {
            file.takeIf { it.length() in 1..MAX_SIDECAR_FILE_BYTES }?.readBytes()
        }.getOrNull() ?: return null
        return decodeBestEffortText(bytes)
    }

    private fun sanitizeFileStem(value: String): String {
        return value.replace(Regex("""[\\/:*?"<>|]"""), "").trim()
    }

    private fun decodeTextPayload(bytes: ByteArray, encoding: Int): String? {
        if (bytes.isEmpty()) return null
        return when (encoding) {
            0 -> bytes.toString(Charsets.ISO_8859_1)
            1 -> decodeUtf16(bytes)
            2 -> bytes.toString(Charsets.UTF_16BE)
            3 -> bytes.toString(Charsets.UTF_8)
            else -> bytes.toString(Charsets.UTF_8)
        }.removeBom()
    }

    private fun decodeUtf16(bytes: ByteArray): String {
        return when {
            bytes.startsWith(byteArrayOf(0xFF.toByte(), 0xFE.toByte())) -> bytes.copyOfRange(2, bytes.size).toString(Charsets.UTF_16LE)
            bytes.startsWith(byteArrayOf(0xFE.toByte(), 0xFF.toByte())) -> bytes.copyOfRange(2, bytes.size).toString(Charsets.UTF_16BE)
            else -> bytes.toString(Charsets.UTF_16)
        }
    }

    private fun findEncodedTerminator(
        bytes: ByteArray,
        startIndex: Int,
        encoding: Int,
    ): Int {
        val delimiterLength = terminatorLengthForEncoding(encoding)
        var index = startIndex
        while (index + delimiterLength <= bytes.size) {
            val terminated = if (delimiterLength == 1) {
                bytes[index] == 0.toByte()
            } else {
                bytes[index] == 0.toByte() && bytes.getOrNull(index + 1) == 0.toByte()
            }
            if (terminated) return index
            index += delimiterLength
        }
        return bytes.size
    }

    private fun terminatorLengthForEncoding(encoding: Int): Int {
        return when (encoding) {
            1, 2 -> 2
            else -> 1
        }
    }

    private fun synchsafeInt(bytes: ByteArray, offset: Int): Int {
        if (offset + 4 > bytes.size) return 0
        return ((bytes[offset].toInt() and 0x7F) shl 21) or
            ((bytes[offset + 1].toInt() and 0x7F) shl 14) or
            ((bytes[offset + 2].toInt() and 0x7F) shl 7) or
            (bytes[offset + 3].toInt() and 0x7F)
    }

    private fun removeId3Unsynchronization(data: ByteArray): ByteArray {
        val output = ArrayList<Byte>(data.size)
        var index = 0
        while (index < data.size) {
            val current = data[index]
            if (current == 0xFF.toByte() && index + 1 < data.size && data[index + 1] == 0x00.toByte()) {
                output += current
                index += 2
            } else {
                output += current
                index += 1
            }
        }
        return output.toByteArray()
    }

    private fun ByteArray.startsWith(other: ByteArray): Boolean {
        if (size < other.size) return false
        return other.indices.all { index -> this[index] == other[index] }
    }

    private fun ByteArray.startsWithAscii(prefix: String): Boolean {
        if (size < prefix.length) return false
        return prefix.indices.all { index -> this[index].toInt().toChar() == prefix[index] }
    }

    private fun String.removeBom(): String = removePrefix("\uFEFF")

    private fun looksLikeTimedLyrics(value: String): Boolean {
        return Regex("""\[(?:(\d{1,2}):)?(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?]""").containsMatchIn(value)
    }

    private companion object {
        const val EMBEDDED_TAG_BUFFER_BYTES = 64 * 1024
        const val MAX_EMBEDDED_TAG_BYTES = 1_500_000
        const val MAX_SIDECAR_FILE_BYTES = 256 * 1024L
        const val MAX_VORBIS_COMMENT_BYTES = 1_000_000
        const val FLAC_BLOCK_VORBIS_COMMENT = 4
        const val ID3_UNSYNCHRONIZATION_FLAG = 0x80
        const val ID3_TIMESTAMP_MILLISECONDS = 0x02
        val FLAC_SYNCED_KEYS = setOf("SYNCEDLYRICS", "LRC", "LYRICSTIMED")
        val FLAC_PLAIN_KEYS = setOf("LYRICS", "UNSYNCEDLYRICS", "UNSYNCEDTEXT", "TEXT")
    }
}
