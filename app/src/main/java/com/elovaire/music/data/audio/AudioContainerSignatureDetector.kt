package elovaire.music.droidbeauty.app.data.audio

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
        if (size >= 4 && bytes.ascii(0, 4) == "fLaC") return AudioContainerSignature.Flac
        if (size >= 6 && bytes.ascii(0, 6) == "#!AMR\n") return AudioContainerSignature.Amr
        if (size >= 9 && bytes.ascii(0, 9) == "#!AMR-WB\n") return AudioContainerSignature.Amr
        if (size >= 4 && bytes.ascii(0, 4) == "OggS") {
            return when {
                bytes.indexOfAscii("OpusHead", size) >= 0 -> AudioContainerSignature.OggOpus
                bytes.indexOf(byteArrayOf(1, 'v'.code.toByte(), 'o'.code.toByte(), 'r'.code.toByte(), 'b'.code.toByte(), 'i'.code.toByte(), 's'.code.toByte()), size) >= 0 -> AudioContainerSignature.OggVorbis
                bytes.indexOf(byteArrayOf(0x7f, 'F'.code.toByte(), 'L'.code.toByte(), 'A'.code.toByte(), 'C'.code.toByte()), size) >= 0 -> AudioContainerSignature.OggFlac
                else -> null
            }
        }
        if (size >= 12 && bytes.ascii(4, 4) == "ftyp") {
            val brand = bytes.ascii(8, 4).lowercase()
            return if (brand.startsWith("3g")) AudioContainerSignature.ThreeGp else AudioContainerSignature.Mp4
        }
        if (size >= 4 && bytes[0] == 0x1a.toByte() && bytes[1] == 0x45.toByte() && bytes[2] == 0xdf.toByte() && bytes[3] == 0xa3.toByte()) {
            return AudioContainerSignature.Matroska
        }
        if (size >= 3 && bytes.ascii(0, 3) == "ID3") return AudioContainerSignature.Mp3
        if (size >= 2) {
            val first = bytes[0].toInt() and 0xff
            val second = bytes[1].toInt() and 0xff
            if (first == 0xff && second and 0xf6 == 0xf0) return AudioContainerSignature.AacAdts
            if (first == 0xff && second and 0xe0 == 0xe0) return AudioContainerSignature.Mp3
        }
        return null
    }

    private fun ByteArray.ascii(offset: Int, count: Int): String {
        return String(this, offset, count, Charsets.US_ASCII)
    }

    private fun ByteArray.indexOfAscii(value: String, length: Int): Int {
        return indexOf(value.toByteArray(Charsets.US_ASCII), length)
    }

    private fun ByteArray.indexOf(needle: ByteArray, length: Int): Int {
        if (needle.isEmpty() || length < needle.size) return -1
        for (start in 0..length - needle.size) {
            var matches = true
            for (index in needle.indices) {
                if (this[start + index] != needle[index]) {
                    matches = false
                    break
                }
            }
            if (matches) return start
        }
        return -1
    }
}
