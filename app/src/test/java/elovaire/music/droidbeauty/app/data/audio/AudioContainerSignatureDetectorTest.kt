package elovaire.music.droidbeauty.app.data.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class AudioContainerSignatureDetectorTest {
    @Test
    fun detectsSupportedContainerSignatures() {
        assertSignature(AudioContainerSignature.Flac, flacStreamInfoHeader())
        assertSignature(AudioContainerSignature.Wav, "RIFF\u0000\u0000\u0000\u0000WAVE")
        assertSignature(AudioContainerSignature.Mp4, "\u0000\u0000\u0000\u0018ftypM4A ")
        assertSignature(AudioContainerSignature.ThreeGp, "\u0000\u0000\u0000\u0018ftyp3gp6")
        assertSignature(AudioContainerSignature.Amr, "#!AMR\n")
        assertSignature(AudioContainerSignature.Matroska, byteArrayOf(0x1a, 0x45, 0xdf.toByte(), 0xa3.toByte()))
    }

    @Test
    fun distinguishesOggMappingsFromHeaders() {
        assertSignature(AudioContainerSignature.OggOpus, oggPage("OpusHead\u0001\u0000\u0000\u0000".toByteArray()))
        assertSignature(AudioContainerSignature.OggVorbis, oggPage("\u0001vorbis\u0000".toByteArray()))
        assertSignature(AudioContainerSignature.OggFlac, oggPage("\u007fFLAC\u0001".toByteArray()))
        assertNull(AudioContainerSignatureDetector.detect(oggPage("unknown OpusHead".toByteArray())))
    }

    @Test
    fun rejectsCodecMarkersOutsideTheFirstOggPacket() {
        val bytes = oggPage(byteArrayOf(1, 'x'.code.toByte()))
        assertNull(AudioContainerSignatureDetector.detect(bytes + "OpusHead".toByteArray()))
    }

    @Test
    fun validatesMpegAudioHeaderBeyondSyncBits() {
        assertSignature(AudioContainerSignature.Mp3, byteArrayOf(0xff.toByte(), 0xfb.toByte(), 0x90.toByte(), 0x64))
        assertNull(AudioContainerSignatureDetector.detect(byteArrayOf(0xff.toByte(), 0xfb.toByte(), 0x00, 0x00)))
    }

    @Test
    fun rejectsTruncatedId3AndAdtsHeaders() {
        assertNull(AudioContainerSignatureDetector.detect("ID3".toByteArray()))
        assertSignature(AudioContainerSignature.Mp3, byteArrayOf(0x49, 0x44, 0x33, 4, 0, 0, 0, 0, 0, 0))
        assertNull(AudioContainerSignatureDetector.detect(byteArrayOf(0xff.toByte(), 0xf1.toByte(), 0x50.toByte(), 0x80.toByte(), 0, 0, 0)))
        assertSignature(
            AudioContainerSignature.AacAdts,
            byteArrayOf(0xff.toByte(), 0xf1.toByte(), 0x50, 0x80.toByte(), 0, 0xe0.toByte(), 0xfc.toByte()),
        )
    }

    @Test
    fun truncatedOrMalformedInputFailsClosed() {
        assertNull(AudioContainerSignatureDetector.detect(byteArrayOf()))
        assertNull(AudioContainerSignatureDetector.detect(byteArrayOf(0xff.toByte())))
        assertNull(AudioContainerSignatureDetector.detect("not audio".toByteArray()))
    }

    @Test
    fun seededMalformedInputsAlwaysTerminate() {
        val random = Random(0xE10A1E)
        repeat(500) {
            val bytes = random.nextBytes(random.nextInt(0, 2_048))
            AudioContainerSignatureDetector.detect(bytes)
        }
    }

    private fun assertSignature(expected: AudioContainerSignature, input: String) {
        assertSignature(expected, input.toByteArray(Charsets.ISO_8859_1))
    }

    private fun assertSignature(expected: AudioContainerSignature, input: ByteArray) {
        assertEquals(expected, AudioContainerSignatureDetector.detect(input))
    }

    private fun oggPage(packet: ByteArray): ByteArray {
        require(packet.size < 255)
        return ByteArray(28 + packet.size).apply {
            "OggS".toByteArray().copyInto(this)
            this[4] = 0
            this[5] = 0
            this[26] = 1
            this[27] = packet.size.toByte()
            packet.copyInto(this, 28)
        }
    }

    private fun flacStreamInfoHeader(): ByteArray = ByteArray(42).apply {
        "fLaC".toByteArray().copyInto(this)
        this[4] = 0x80.toByte()
        this[7] = 34
    }
}
