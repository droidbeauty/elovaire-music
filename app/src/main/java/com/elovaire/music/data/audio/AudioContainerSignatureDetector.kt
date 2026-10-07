package elovaire.music.droidbeauty.app.data.audio

import java.io.ByteArrayOutputStream
import java.util.Locale

internal enum class AudioContainerSignature {
    Mp3,
    Mp4,
    ThreeGp,
    AacAdts,
    Flac,
    Wav,
    OggVorbis,
    OggOpus,
    OggFlac,
    Amr,
    Matroska,
}

internal object AudioContainerSignatureDetector {
    fun detect(bytes: ByteArray, length: Int = bytes.size): AudioContainerSignature? {
        val size = length.coerceIn(0, bytes.size)
        if (size >= 12 && bytes.ascii(0, 4) in setOf("RIFF", "RF64") && bytes.ascii(8, 4) == "WAVE") {
            return AudioContainerSignature.Wav
        }
        if (size >= 4 && bytes.ascii(0, 4) == "fLaC" && bytes.hasFlacStreamInfo(size)) {
            return AudioContainerSignature.Flac
        }
        if (size >= 6 && bytes.ascii(0, 6) == "#!AMR\n") return AudioContainerSignature.Amr
        if (size >= 9 && bytes.ascii(0, 9) == "#!AMR-WB\n") return AudioContainerSignature.Amr
        if (size >= 4 && bytes.ascii(0, 4) == "OggS") {
            return detectOggCodec(bytes, size)
        }
        if (size >= 12 && bytes.ascii(4, 4) == "ftyp") {
            val brand = bytes.ascii(8, 4).lowercase(Locale.ROOT)
            return if (brand.startsWith("3g")) AudioContainerSignature.ThreeGp else AudioContainerSignature.Mp4
        }
        if (size >= 4 && bytes[0] == 0x1a.toByte() && bytes[1] == 0x45.toByte() && bytes[2] == 0xdf.toByte() && bytes[3] == 0xa3.toByte()) {
            return AudioContainerSignature.Matroska
        }
        if (size >= 3 && bytes.ascii(0, 3) == "ID3" && bytes.hasValidId3Header(size)) {
            return AudioContainerSignature.Mp3
        }
        if (size >= 2) {
            val first = bytes[0].toInt() and 0xff
            val second = bytes[1].toInt() and 0xff
            if (first == 0xff && second and 0xf6 == 0xf0 && bytes.hasPlausibleAdtsHeader(size)) {
                return AudioContainerSignature.AacAdts
            }
            if (first == 0xff && second and 0xe0 == 0xe0 && bytes.isPlausibleMpegAudioHeader(size)) {
                return AudioContainerSignature.Mp3
            }
        }
        return null
    }

    private fun detectOggCodec(bytes: ByteArray, size: Int): AudioContainerSignature? {
        if (size < OGG_FIXED_HEADER_BYTES || bytes[4] != 0.toByte() || bytes[5].toInt() and OGG_CONTINUED_PACKET_FLAG != 0) {
            return null
        }
        val segmentCount = bytes[26].toInt() and 0xff
        val segmentTableEnd = OGG_FIXED_HEADER_BYTES + segmentCount
        if (segmentTableEnd > size) return null

        val firstPacket = ByteArrayOutputStream()
        for (index in OGG_FIXED_HEADER_BYTES until segmentTableEnd) {
            val segmentLength = bytes[index].toInt() and 0xff
            val packetEnd = segmentTableEnd + segmentLength
            if (packetEnd > size || firstPacket.size() > MAX_OGG_IDENTIFICATION_PACKET_BYTES - segmentLength) {
                return null
            }
            firstPacket.write(bytes, segmentTableEnd, segmentLength)
            if (segmentLength < 255) return firstPacket.toByteArray().oggCodec()
            // Identification headers are bounded and should be complete in the first page.
            if (index == segmentTableEnd - 1) return null
            // The next lacing segment begins after this segment's data.
            return detectOggPacketAcrossSegments(bytes, size, segmentTableEnd, index, segmentCount, firstPacket)
        }
        return null
    }

    private fun detectOggPacketAcrossSegments(
        bytes: ByteArray,
        size: Int,
        segmentTableEnd: Int,
        firstSegmentIndex: Int,
        segmentCount: Int,
        packet: ByteArrayOutputStream,
    ): AudioContainerSignature? {
        var dataOffset = segmentTableEnd + (bytes[firstSegmentIndex].toInt() and 0xff)
        for (index in firstSegmentIndex + 1 until segmentCount) {
            val segmentLength = bytes[OGG_FIXED_HEADER_BYTES + index].toInt() and 0xff
            if (dataOffset + segmentLength > size || packet.size() > MAX_OGG_IDENTIFICATION_PACKET_BYTES - segmentLength) {
                return null
            }
            packet.write(bytes, dataOffset, segmentLength)
            dataOffset += segmentLength
            if (segmentLength < 255) return packet.toByteArray().oggCodec()
        }
        return null
    }

    private fun ByteArray.oggCodec(): AudioContainerSignature? = when {
        startsWithAscii("OpusHead") -> AudioContainerSignature.OggOpus
        startsWith(byteArrayOf(1, 'v'.code.toByte(), 'o'.code.toByte(), 'r'.code.toByte(), 'b'.code.toByte(), 'i'.code.toByte(), 's'.code.toByte())) -> AudioContainerSignature.OggVorbis
        startsWith(byteArrayOf(0x7f, 'F'.code.toByte(), 'L'.code.toByte(), 'A'.code.toByte(), 'C'.code.toByte())) -> AudioContainerSignature.OggFlac
        else -> null
    }

    private fun ByteArray.isPlausibleMpegAudioHeader(size: Int): Boolean {
        if (size < 4) return false
        val second = this[1].toInt() and 0xff
        val third = this[2].toInt() and 0xff
        val version = second ushr 3 and 0x3
        val layer = second ushr 1 and 0x3
        val bitrateIndex = third ushr 4 and 0xf
        val sampleRateIndex = third ushr 2 and 0x3
        val emphasis = this[3].toInt() and 0x3
        return version != 1 && layer != 0 && bitrateIndex != 0 && bitrateIndex != 15 &&
            sampleRateIndex != 3 && emphasis != 2
    }

    private fun ByteArray.hasFlacStreamInfo(size: Int): Boolean {
        if (size < FLAC_STREAMINFO_BYTES || this[4].toInt() and 0x7f != 0) return false
        val blockLength = ((this[5].toInt() and 0xff) shl 16) or
            ((this[6].toInt() and 0xff) shl 8) or
            (this[7].toInt() and 0xff)
        return blockLength == FLAC_STREAMINFO_DATA_BYTES
    }

    private fun ByteArray.hasValidId3Header(size: Int): Boolean {
        if (size < ID3_HEADER_BYTES) return false
        val version = this[3].toInt() and 0xff
        val revision = this[4].toInt() and 0xff
        val flags = this[5].toInt() and 0xff
        val reservedFlags = when (version) {
            2 -> 0x3f
            3 -> 0x1f
            4 -> 0x0f
            else -> return false
        }
        return revision != 0xff && flags and reservedFlags == 0 &&
            (6 until ID3_HEADER_BYTES).all { this[it].toInt() and 0x80 == 0 }
    }

    private fun ByteArray.hasPlausibleAdtsHeader(size: Int): Boolean {
        if (size < ADTS_HEADER_BYTES) return false
        val second = this[1].toInt() and 0xff
        val third = this[2].toInt() and 0xff
        val fourth = this[3].toInt() and 0xff
        val fifth = this[4].toInt() and 0xff
        val sixth = this[5].toInt() and 0xff
        val headerBytes = if (second and 0x01 == 0) ADTS_HEADER_BYTES_WITH_CRC else ADTS_HEADER_BYTES
        if (size < headerBytes || (third ushr 2 and 0x0f) >= ADTS_RESERVED_SAMPLE_RATE_INDEX) return false
        val frameLength = ((fourth and 0x03) shl 11) or (fifth shl 3) or (sixth ushr 5)
        return frameLength >= headerBytes
    }

    private fun ByteArray.startsWithAscii(value: String): Boolean {
        if (size < value.length) return false
        return value.indices.all { index -> this[index] == value[index].code.toByte() }
    }

    private fun ByteArray.startsWith(value: ByteArray): Boolean {
        if (size < value.size) return false
        return value.indices.all { index -> this[index] == value[index] }
    }

    private fun ByteArray.ascii(offset: Int, count: Int): String {
        return String(this, offset, count, Charsets.US_ASCII)
    }


    private const val OGG_FIXED_HEADER_BYTES = 27
    private const val OGG_CONTINUED_PACKET_FLAG = 0x01
    private const val MAX_OGG_IDENTIFICATION_PACKET_BYTES = 4 * 1024
    private const val FLAC_STREAMINFO_BYTES = 42
    private const val FLAC_STREAMINFO_DATA_BYTES = 34
    private const val ID3_HEADER_BYTES = 10
    private const val ADTS_HEADER_BYTES = 7
    private const val ADTS_HEADER_BYTES_WITH_CRC = 9
    private const val ADTS_RESERVED_SAMPLE_RATE_INDEX = 13
}
