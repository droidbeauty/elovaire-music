package elovaire.music.droidbeauty.app.data.lyrics

import android.content.ContentResolver
import android.content.Context
import elovaire.music.droidbeauty.app.data.library.queryMediaStoreFilePath
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.BufferedInputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.util.Locale
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import kotlinx.coroutines.CancellationException

internal class LocalLyricsResolver(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val contentResolver: ContentResolver = context.applicationContext.contentResolver

    fun resolve(song: Song): LocalLyricsMatch? {
        return readEmbeddedLyrics(song) ?: readSidecarLyrics(song)
    }

    private fun readEmbeddedLyrics(song: Song): LocalLyricsMatch? {
        val headerBytes = contentResolver.openInputStream(song.uri)?.use { input ->
            input.readBytesCompat(4)
        } ?: return null

        val specializedMatch = when {
            headerBytes.startsWithAscii("ID3") -> readId3Lyrics(song)
            headerBytes.startsWithAscii("fLaC") -> readFlacLyrics(song)
            else -> null
        }
        return specializedMatch ?: readGenericEmbeddedLyrics(song)
    }

    private fun readGenericEmbeddedLyrics(song: Song): LocalLyricsMatch? {
        val extension = song.fileName.substringAfterLast('.', "").ifBlank { "tmp" }
        val tempFile = File.createTempFile("lyrics-${song.id}-", ".$extension", appContext.cacheDir)
        return try {
            contentResolver.openInputStream(song.uri)?.use { input ->
                tempFile.outputStream().use { output ->
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
            } ?: return null
            val rawLyrics = AudioFileIO.read(tempFile)
                .tag
                ?.getFirst(FieldKey.LYRICS)
                .orEmpty()
                .canonicalEmbeddedLyricsText()
            parseLrcOrPlain(rawLyrics)
                ?.takeIf { it.lines.isNotEmpty() }
                ?.let(::LocalLyricsMatch)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        } finally {
            runCatching { tempFile.delete() }
        }
    }

    private fun readId3Lyrics(song: Song): LocalLyricsMatch? {
        return contentResolver.openInputStream(song.uri)?.use { rawInput ->
            val input = BufferedInputStream(rawInput, EMBEDDED_TAG_BUFFER_BYTES)
            val header = input.readBytesCompat(10)
            if (header.size < 10 || !header.copyOfRange(0, 3).startsWithAscii("ID3")) {
                return@use null
            }
            val majorVersion = header[3].toInt() and 0xFF
            val flags = header[5].toInt() and 0xFF
            val tagSize = synchsafeInt(header, 6)
            if (tagSize <= 0 || tagSize > MAX_EMBEDDED_TAG_BYTES) {
                return@use null
            }
            val tagData = input.readBytesCompat(tagSize)
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
        parseLrcOrPlain(
            raw = plainLyrics.joinToString("\n"),
        )?.let { payload ->
            return LocalLyricsMatch(payload)
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

        val encodedDescriptorEnd = findEncodedTerminator(frameData, 6, encoding)
        val payloadStarts = buildList {
            add(encodedDescriptorEnd + terminatorLengthForEncoding(encoding))
            if (encoding == ID3_UTF16 || encoding == ID3_UTF16_BE) {
                findSingleByteTerminator(frameData, 6)
                    .takeIf { it >= 0 }
                    ?.plus(1)
                    ?.let(::add)
            }
        }.distinct()
        var bestLines = emptyList<LyricsLine>()
        payloadStarts.forEach { start ->
            val lines = parseSyltPayload(frameData, start, encoding)
            if (lines.size > bestLines.size) bestLines = lines
        }
        return bestLines
    }

    private fun parseSyltPayload(
        frameData: ByteArray,
        start: Int,
        encoding: Int,
    ): List<LyricsLine> {
        var position = start
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

    private fun findSingleByteTerminator(
        bytes: ByteArray,
        startIndex: Int,
    ): Int {
        for (index in startIndex until bytes.size) {
            if (bytes[index] == 0.toByte()) return index
        }
        return -1
    }

    private fun readFlacLyrics(song: Song): LocalLyricsMatch? {
        return contentResolver.openInputStream(song.uri)?.use { rawInput ->
            val input = BufferedInputStream(rawInput, EMBEDDED_TAG_BUFFER_BYTES)
            val magic = input.readBytesCompat(4)
            if (magic.size < 4 || !magic.startsWithAscii("fLaC")) return@use null

            var isLastBlock = false
            while (!isLastBlock) {
                val header = input.readBytesCompat(4)
                if (header.size < 4) break
                isLastBlock = (header[0].toInt() and 0x80) != 0
                val blockType = header[0].toInt() and 0x7F
                val blockSize = ((header[1].toInt() and 0xFF) shl 16) or
                    ((header[2].toInt() and 0xFF) shl 8) or
                    (header[3].toInt() and 0xFF)
                if (blockSize < 0) {
                    break
                }
                if (blockSize > MAX_VORBIS_COMMENT_BYTES) {
                    if (!input.discardExactly(blockSize)) break
                    continue
                }
                val blockData = input.readBytesCompat(blockSize)
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

        var parsedComments = 0
        val boundedCommentCount = boundedVorbisCommentCount(commentCount, buffer.remaining())
        while (parsedComments < boundedCommentCount && buffer.remaining() >= 4) {
            val commentLength = buffer.int.coerceAtLeast(0)
            if (commentLength <= 0 || commentLength > buffer.remaining()) break
            val commentBytes = ByteArray(commentLength)
            buffer.get(commentBytes)
            val comment = commentBytes.toString(Charsets.UTF_8)
            val separatorIndex = comment.indexOf('=')
            if (separatorIndex <= 0) {
                parsedComments += 1
                continue
            }
            val key = comment.substring(0, separatorIndex)
                .uppercase(Locale.US)
                .replace(" ", "")
                .replace("_", "")
            val value = comment.substring(separatorIndex + 1).removeBom()
            when {
                key in FLAC_SYNCED_KEYS || looksLikeTimedLyrics(value) -> {
                    parseLrcOrPlain(
                        raw = value,
                    )?.takeIf { it.isSynced }?.let { payload ->
                        syncedPayload = payload
                    }
                }
                key in FLAC_PLAIN_KEYS -> {
                    parseLrcOrPlain(
                        raw = value,
                    )?.takeIf { !it.isSynced }?.let { payload ->
                        plainPayload = payload
                    }
                }
            }
            parsedComments += 1
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
                parseLrcOrPlain(
                    raw = readTextFile(lrcFile).orEmpty(),
                )?.takeIf { it.lines.isNotEmpty() }?.let { payload ->
                    return LocalLyricsMatch(payload)
                }
            }
            val txtFile = File(parent, "$baseName.txt")
            if (txtFile.isFile) {
                parseLrcOrPlain(
                    raw = readTextFile(txtFile).orEmpty(),
                )?.takeIf { !it.isSynced && it.lines.isNotEmpty() }?.let { payload ->
                    return LocalLyricsMatch(payload)
                }
            }
        }

        return null
    }

    private fun resolveSongFile(song: Song): File? {
        val resolvedPath = contentResolver.queryMediaStoreFilePath(appContext, song.uri)
        return resolvedPath?.let(::File)?.takeIf(File::exists)
    }

    private fun parseTimedPayload(lines: List<LyricsLine>): LyricsPayload? {
        val validLines = lines
            .filter { !it.text.isBlank() && it.startTimeMs != null }
            .sortedBy { it.startTimeMs }
        if (validLines.isEmpty()) return null
        val nextDistinctStarts = arrayOfNulls<Long>(validLines.size)
        var nextDistinctStart: Long? = null
        var currentStart: Long? = null
        var currentGroupNext: Long? = null
        for (index in validLines.indices.reversed()) {
            val start = validLines[index].startTimeMs
            if (start != currentStart) {
                currentGroupNext = nextDistinctStart
                nextDistinctStart = start
                currentStart = start
            }
            nextDistinctStarts[index] = currentGroupNext
        }
        return LyricsPayload(
            lines = validLines.mapIndexed { index, line ->
                line.copy(
                    index = index,
                    endTimeMs = nextDistinctStarts[index],
                )
            },
            isSynced = true,
        )
    }

    private fun readTextFile(file: File): String? {
        val bytes = try {
            file.takeIf { it.length() in 1..MAX_SIDECAR_FILE_BYTES }?.readBytes()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        } ?: return null
        return decodeBestEffortText(bytes)
    }

    private fun sanitizeFileStem(value: String): String {
        return value.replace(INVALID_SIDECAR_FILE_NAME_REGEX, "").trim()
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

    private fun java.io.InputStream.readBytesCompat(byteCount: Int): ByteArray {
        if (byteCount <= 0) return ByteArray(0)
        val buffer = ByteArray(byteCount)
        var offset = 0
        while (offset < byteCount) {
            val readCount = read(buffer, offset, byteCount - offset)
            if (readCount <= 0) break
            offset += readCount
        }
        return if (offset == buffer.size) buffer else buffer.copyOf(offset)
    }

    private fun java.io.InputStream.discardExactly(byteCount: Int): Boolean {
        var remaining = byteCount
        val buffer = ByteArray(8 * 1024)
        while (remaining > 0) {
            val skipped = skip(remaining.toLong()).coerceAtMost(remaining.toLong()).toInt()
            if (skipped > 0) {
                remaining -= skipped
                continue
            }
            val read = read(buffer, 0, minOf(buffer.size, remaining))
            if (read <= 0) return false
            remaining -= read
        }
        return true
    }

    private fun String.removeBom(): String = removePrefix("\uFEFF")

    private fun looksLikeTimedLyrics(value: String): Boolean {
        return EXTENDED_LRC_TIMESTAMP_REGEX.containsMatchIn(value)
    }

    private companion object {
        const val EMBEDDED_TAG_BUFFER_BYTES = 64 * 1024
        const val MAX_EMBEDDED_TAG_BYTES = 1_500_000
        const val MAX_SIDECAR_FILE_BYTES = 256 * 1024L
        const val MAX_VORBIS_COMMENT_BYTES = 1_000_000
        const val MAX_TEMP_COPY_BYTES = 512L * 1024L * 1024L
        const val COPY_BUFFER_BYTES = 64 * 1024
        const val FLAC_BLOCK_VORBIS_COMMENT = 4
        const val ID3_UNSYNCHRONIZATION_FLAG = 0x80
        const val ID3_UTF16 = 1
        const val ID3_UTF16_BE = 2
        const val ID3_TIMESTAMP_MILLISECONDS = 0x02
        val FLAC_SYNCED_KEYS = setOf("SYNCEDLYRICS", "LRC", "LYRICSTIMED")
        val FLAC_PLAIN_KEYS = setOf("LYRICS", "UNSYNCEDLYRICS", "UNSYNCEDTEXT", "TEXT")
        val INVALID_SIDECAR_FILE_NAME_REGEX = Regex("""[\\/:*?"<>|]""")
        val EXTENDED_LRC_TIMESTAMP_REGEX =
            Regex("""\[(?:(\d{1,2}):)?(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?]""")
    }
}

internal fun boundedVorbisCommentCount(declaredCount: Int, remainingBytes: Int): Int {
    if (declaredCount <= 0 || remainingBytes < 4) return 0
    return minOf(declaredCount, MAX_VORBIS_COMMENT_COUNT, remainingBytes / 4)
}

private const val MAX_VORBIS_COMMENT_COUNT = 10_000
